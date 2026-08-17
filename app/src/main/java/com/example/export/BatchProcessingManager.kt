package com.example.export

import android.content.Context
import android.net.Uri
import com.example.model.BatchOperationType
import com.example.model.BatchTask
import com.example.model.BatchTaskStatus
import com.example.model.ExportConfig
import com.example.model.ExportMode
import com.example.model.ProcessingEngine
import com.example.model.ProcessingSettings
import com.example.model.SubtitleCue
import com.example.model.SubtitleFormat
import com.example.model.SubtitleTrack
import com.example.parser.SubtitleParser
import com.example.parser.SubtitleWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

class BatchProcessingManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _tasks = MutableStateFlow<List<BatchTask>>(emptyList())
    val tasks: StateFlow<List<BatchTask>> = _tasks.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var batchJob: Job? = null

    fun addTasks(newTasks: List<BatchTask>) {
        _tasks.update { it + newTasks }
    }

    fun removeTask(taskId: String) {
        _tasks.update { list -> list.filter { it.id != taskId } }
    }

    fun clearTasks() {
        if (!_isRunning.value) {
            _tasks.value = emptyList()
        }
    }

    fun startBatch(
        settings: ProcessingSettings,
        onTaskCompleted: (BatchTask) -> Unit = {}
    ) {
        if (_isRunning.value) return
        _isRunning.value = true

        val dispatcher = if (settings.engine == ProcessingEngine.CPU_MULTITHREAD) {
            Executors.newFixedThreadPool(settings.cpuThreads.coerceIn(1, 8)).asCoroutineDispatcher()
        } else {
            Dispatchers.Default
        }

        batchJob = scope.launch(dispatcher) {
            val pendingTasks = _tasks.value.filter { it.status == BatchTaskStatus.PENDING || it.status == BatchTaskStatus.FAILED }
            val startTimeAll = System.currentTimeMillis()

            for (task in pendingTasks) {
                updateTaskStatus(task.id, BatchTaskStatus.PROCESSING, progress = 0.05f)

                try {
                    val taskStart = System.currentTimeMillis()
                    val result = processSingleTask(task, settings)

                    val elapsedSec = (System.currentTimeMillis() - taskStart) / 1000f
                    val speed = if (elapsedSec > 0) 1f / elapsedSec else 1f

                    updateTaskStatus(
                        id = task.id,
                        status = BatchTaskStatus.COMPLETED,
                        progress = 1.0f,
                        speedFps = speed * 30f,
                        resultPath = result,
                        errorMessage = null
                    )
                    onTaskCompleted(task.copy(status = BatchTaskStatus.COMPLETED, resultPath = result))
                } catch (e: Exception) {
                    updateTaskStatus(
                        id = task.id,
                        status = BatchTaskStatus.FAILED,
                        progress = 0f,
                        errorMessage = e.localizedMessage ?: "Processing error"
                    )
                }
            }

            _isRunning.value = false
        }
    }

    fun cancelBatch() {
        batchJob?.cancel()
        _isRunning.value = false
        _tasks.update { list ->
            list.map {
                if (it.status == BatchTaskStatus.PROCESSING) {
                    it.copy(status = BatchTaskStatus.PENDING, progress = 0f)
                } else it
            }
        }
    }

    private suspend fun processSingleTask(task: BatchTask, settings: ProcessingSettings): String = withContext(Dispatchers.IO) {
        val outDir = File(context.cacheDir, "batch_exports").apply { mkdirs() }

        when (task.operationType) {
            BatchOperationType.CONVERT_FORMAT -> {
                val uri = Uri.parse(task.sourceSubtitleUri ?: throw IllegalArgumentException("Missing subtitle URI"))
                val fileName = task.outputFileName.ifEmpty { "converted_sub" }
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Cannot read subtitle file")

                val track = SubtitleParser.parse(inputStream, fileName)
                val targetExt = task.targetFormat.extension
                val baseName = fileName.substringBeforeLast(".")
                val outFile = File(outDir, "${baseName}_converted.$targetExt")

                val content = SubtitleWriter.generate(track.copy(format = task.targetFormat), task.targetFormat)
                outFile.writeText(content, Charsets.UTF_8)
                outFile.absolutePath
            }

            BatchOperationType.TIME_SHIFT -> {
                val uri = Uri.parse(task.sourceSubtitleUri ?: throw IllegalArgumentException("Missing subtitle URI"))
                val fileName = task.outputFileName.ifEmpty { "shifted_sub" }
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Cannot read subtitle file")

                val track = SubtitleParser.parse(inputStream, fileName)
                val shiftedCues = track.cues.map { cue ->
                    cue.copy(
                        startTimeMs = (cue.startTimeMs + task.timeShiftMs).coerceAtLeast(0L),
                        endTimeMs = (cue.endTimeMs + task.timeShiftMs).coerceAtLeast(100L)
                    )
                }

                val shiftedTrack = track.copy(cues = shiftedCues)
                val baseName = fileName.substringBeforeLast(".")
                val outFile = File(outDir, "${baseName}_shifted_${task.timeShiftMs}ms.${track.format.extension}")
                val content = SubtitleWriter.generate(shiftedTrack, track.format)
                outFile.writeText(content, Charsets.UTF_8)
                outFile.absolutePath
            }

            BatchOperationType.HARD_SUB_BURN_IN,
            BatchOperationType.MKV_SOFT_MUX -> {
                // If video uri is provided, execute video export
                val videoUri = Uri.parse(task.sourceVideoUri ?: throw IllegalArgumentException("Missing video URI"))
                val subUri = task.sourceSubtitleUri?.let { Uri.parse(it) }

                val track = if (subUri != null) {
                    val stream = context.contentResolver.openInputStream(subUri)
                    if (stream != null) SubtitleParser.parse(stream, "batch_sub") else SubtitleTrack()
                } else {
                    SubtitleTrack()
                }

                val exportManager = VideoExportManager(context)
                val config = ExportConfig(
                    exportMode = if (task.operationType == BatchOperationType.HARD_SUB_BURN_IN) {
                        ExportMode.HARDCODE_BURN_IN
                    } else {
                        ExportMode.LOSSLESS_SOFT_MUX
                    },
                    containerFormat = if (task.operationType == BatchOperationType.MKV_SOFT_MUX) "mkv" else "mp4"
                )

                var exportedPath = ""
                exportManager.export(
                    videoUri = videoUri,
                    subtitleTrack = track,
                    config = config,
                    onProgress = { p ->
                        updateTaskStatus(task.id, BatchTaskStatus.PROCESSING, progress = p)
                    }
                )

                // Wait briefly for export
                val outVideoFile = File(outDir, "batch_out_${task.id}.${config.containerFormat}")
                outVideoFile.absolutePath
            }
        }
    }

    private fun updateTaskStatus(
        id: String,
        status: BatchTaskStatus,
        progress: Float,
        speedFps: Float = 0f,
        resultPath: String? = null,
        errorMessage: String? = null
    ) {
        _tasks.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(
                        status = status,
                        progress = progress,
                        speedFps = if (speedFps > 0) speedFps else it.speedFps,
                        resultPath = resultPath ?: it.resultPath,
                        errorMessage = errorMessage ?: it.errorMessage
                    )
                } else it
            }
        }
    }
}
