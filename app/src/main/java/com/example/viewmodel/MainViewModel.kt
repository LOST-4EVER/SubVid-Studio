package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.export.BatchProcessingManager
import com.example.export.VideoExportManager
import com.example.model.AppTab
import com.example.model.AspectRatioOption
import com.example.model.BatchOperationType
import com.example.model.BatchTask
import com.example.model.ExportConfig
import com.example.model.ExportState
import com.example.model.ProcessingSettings
import com.example.model.ProjectRepository
import com.example.model.RenderOptimizationLevel
import com.example.model.StudioProject
import com.example.model.SubtitleAlignment
import com.example.model.SubtitleCue
import com.example.model.SubtitleFormat
import com.example.model.SubtitleStyle
import com.example.model.SubtitleTrack
import com.example.model.VideoMetadata
import com.example.parser.SubtitleParser
import com.example.parser.SubtitleWriter
import com.example.player.PlayerUiState
import com.example.player.VideoPlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class AutoSaveStatus {
    SAVED,
    SAVING,
    IDLE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val playerController = VideoPlayerController(application, viewModelScope)
    val exportManager = VideoExportManager(application)
    val projectRepository = ProjectRepository(application)
    val batchManager = BatchProcessingManager(application, viewModelScope)

    val playerState: StateFlow<PlayerUiState> = playerController.uiState
    val exportState: StateFlow<ExportState> = exportManager.exportState

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Aspect Ratio Framing (Original, 16:9, 9:16, 1:1, 4:5, 21:9)
    private val _aspectRatio = MutableStateFlow(AspectRatioOption.ORIGINAL)
    val aspectRatio: StateFlow<AspectRatioOption> = _aspectRatio.asStateFlow()

    // Subtitle source URI and Auto-Save Status
    private val _subtitleSourceUri = MutableStateFlow<Uri?>(null)
    val subtitleSourceUri: StateFlow<Uri?> = _subtitleSourceUri.asStateFlow()

    private val _autoSaveStatus = MutableStateFlow(AutoSaveStatus.SAVED)
    val autoSaveStatus: StateFlow<AutoSaveStatus> = _autoSaveStatus.asStateFlow()

    // Undo / Redo History Stacks
    private val undoStack = mutableListOf<SubtitleTrack>()
    private val redoStack = mutableListOf<SubtitleTrack>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Projects list
    private val _projects = MutableStateFlow<List<StudioProject>>(emptyList())
    val projects: StateFlow<List<StudioProject>> = _projects.asStateFlow()

    private val _activeProject = MutableStateFlow<StudioProject?>(null)
    val activeProject: StateFlow<StudioProject?> = _activeProject.asStateFlow()

    // Settings
    private val _processingSettings = MutableStateFlow(ProcessingSettings())
    val processingSettings: StateFlow<ProcessingSettings> = _processingSettings.asStateFlow()

    // Batch tasks
    val batchTasks: StateFlow<List<BatchTask>> = batchManager.tasks
    val isBatchRunning: StateFlow<Boolean> = batchManager.isRunning

    // Active Editor State
    private val _videoMetadata = MutableStateFlow(
        VideoMetadata(
            uriString = "",
            fileName = "",
            durationMs = 0L
        )
    )
    val videoMetadata: StateFlow<VideoMetadata> = _videoMetadata.asStateFlow()

    private val _subtitleTrack = MutableStateFlow(
        SubtitleTrack(
            title = "Untitled Track",
            format = SubtitleFormat.SRT,
            cues = emptyList()
        )
    )
    val subtitleTrack: StateFlow<SubtitleTrack> = _subtitleTrack.asStateFlow()

    private val _selectedCue = MutableStateFlow<SubtitleCue?>(null)
    val selectedCue: StateFlow<SubtitleCue?> = _selectedCue.asStateFlow()

    private val _activeCue = MutableStateFlow<SubtitleCue?>(null)
    val activeCue: StateFlow<SubtitleCue?> = _activeCue.asStateFlow()

    // Dialog & Sheet States
    private val _showPlacementDialog = MutableStateFlow(false)
    val showPlacementDialog: StateFlow<Boolean> = _showPlacementDialog.asStateFlow()

    private val _showStyleDialog = MutableStateFlow(false)
    val showStyleDialog: StateFlow<Boolean> = _showStyleDialog.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _showSubtitleListSheet = MutableStateFlow(false)
    val showSubtitleListSheet: StateFlow<Boolean> = _showSubtitleListSheet.asStateFlow()

    private val _showOptimizationSheet = MutableStateFlow(false)
    val showOptimizationSheet: StateFlow<Boolean> = _showOptimizationSheet.asStateFlow()

    private val _isFullscreenVideo = MutableStateFlow(false)
    val isFullscreenVideo: StateFlow<Boolean> = _isFullscreenVideo.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Load existing saved projects
        refreshProjects()

        // Periodically sync active playing cue with O(1) fast-path and O(log N) binary search
        viewModelScope.launch {
            playerState.collect { state ->
                val currentMs = state.currentPositionMs
                val cues = _subtitleTrack.value.cues
                val active = findActiveCueAt(cues, currentMs)
                if (_activeCue.value?.id != active?.id) {
                    _activeCue.value = active
                }
            }
        }
    }

    private fun findActiveCueAt(cues: List<SubtitleCue>, currentMs: Long): SubtitleCue? {
        if (cues.isEmpty()) return null
        val currentActive = _activeCue.value
        if (currentActive != null && currentActive.isActiveAt(currentMs)) {
            return currentActive
        }
        // Direct search handles unsorted, trimmed, or overlapping cues with 100% reliability
        return cues.firstOrNull { it.isActiveAt(currentMs) }
    }

    fun setTab(tab: AppTab) {
        if (tab != AppTab.EDITOR && playerState.value.isPlaying) {
            playerController.pause()
        }
        if (_videoMetadata.value.uriString.isNotEmpty() || _subtitleTrack.value.cues.isNotEmpty()) {
            saveCurrentProject()
        }
        _currentTab.value = tab
    }

    fun setFullscreenVideo(isFullscreen: Boolean) {
        _isFullscreenVideo.value = isFullscreen
    }

    fun toggleFullscreenVideo() {
        _isFullscreenVideo.value = !_isFullscreenVideo.value
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun refreshProjects() {
        _projects.value = projectRepository.getAllProjects()
    }

    fun createNewProject(name: String = "Project ${_projects.value.size + 1}") {
        val newProj = StudioProject(
            id = UUID.randomUUID().toString(),
            name = name,
            subtitleTrack = SubtitleTrack(title = name, format = SubtitleFormat.SRT, cues = emptyList()),
            lastModifiedMs = System.currentTimeMillis()
        )
        projectRepository.saveProject(newProj)
        refreshProjects()
        openProject(newProj)
    }

    fun openProject(project: StudioProject) {
        // Fetch full fresh project instance with loaded cues from repository if available
        val fullProject = projectRepository.getProjectById(project.id) ?: project
        _activeProject.value = fullProject
        _videoMetadata.value = VideoMetadata(
            uriString = fullProject.videoUriString,
            fileName = fullProject.videoFileName,
            durationMs = fullProject.videoDurationMs
        )
        _subtitleTrack.value = fullProject.subtitleTrack
        _selectedCue.value = fullProject.subtitleTrack.cues.firstOrNull()
        _activeCue.value = null

        if (fullProject.videoUriString.isNotEmpty()) {
            try {
                val uri = Uri.parse(fullProject.videoUriString)
                playerController.loadMedia(uri)
                if (fullProject.currentPositionMs > 0) {
                    playerController.seekTo(fullProject.currentPositionMs)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Could not load project video URI", e)
            }
        }
        _currentTab.value = AppTab.EDITOR
    }

    fun deleteProject(projectId: String) {
        projectRepository.deleteProject(projectId)
        if (_activeProject.value?.id == projectId) {
            _activeProject.value = null
            _videoMetadata.value = VideoMetadata(uriString = "", fileName = "", durationMs = 0L)
            _subtitleTrack.value = SubtitleTrack()
            _selectedCue.value = null
            _activeCue.value = null
            playerController.unloadMedia()
        }
        refreshProjects()
    }

    fun saveCurrentProject() {
        val currentTrack = _subtitleTrack.value
        val currentMeta = _videoMetadata.value
        if (currentTrack.cues.isEmpty() && currentMeta.uriString.isEmpty()) return

        _autoSaveStatus.value = AutoSaveStatus.SAVING

        val fallbackName = when {
            currentTrack.title.isNotBlank() && currentTrack.title != "Untitled Track" && currentTrack.title != "Empty Subtitle Track" -> currentTrack.title
            currentMeta.fileName.isNotBlank() && currentMeta.fileName != "No Video" && currentMeta.fileName != "Video File" -> currentMeta.fileName
            else -> "Project ${_projects.value.size + 1}"
        }

        val current = _activeProject.value ?: StudioProject(
            id = UUID.randomUUID().toString(),
            name = fallbackName
        )

        val playerDuration = playerState.value.durationMs
        val resolvedDuration = if (playerDuration > 0) playerDuration else currentMeta.durationMs
        val currentPos = playerState.value.currentPositionMs

        val updated = current.copy(
            name = fallbackName,
            videoUriString = currentMeta.uriString,
            videoFileName = currentMeta.fileName.ifEmpty { current.videoFileName },
            videoDurationMs = resolvedDuration,
            currentPositionMs = currentPos,
            subtitleFileName = currentTrack.title.ifEmpty { current.subtitleFileName },
            subtitleFormat = currentTrack.format,
            cueCount = currentTrack.cues.size,
            subtitleTrack = currentTrack,
            lastModifiedMs = System.currentTimeMillis()
        )
        _activeProject.value = updated
        projectRepository.saveProject(updated)
        refreshProjects()

        // Write directly back to source subtitle URI if accessible
        val sourceUri = _subtitleSourceUri.value
        if (sourceUri != null && currentTrack.cues.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val context = getApplication<Application>()
                    val text = SubtitleWriter.generate(
                        track = currentTrack,
                        format = currentTrack.format,
                        videoWidth = playerState.value.videoWidth,
                        videoHeight = playerState.value.videoHeight
                    )
                    context.contentResolver.openOutputStream(sourceUri, "wt")?.use { stream ->
                        stream.write(text.toByteArray(Charsets.UTF_8))
                    }
                } catch (e: Exception) {
                    Log.d("MainViewModel", "Auto-write to URI skipped or not writable: ${e.message}")
                } finally {
                    _autoSaveStatus.value = AutoSaveStatus.SAVED
                }
            }
        } else {
            _autoSaveStatus.value = AutoSaveStatus.SAVED
        }
    }

    fun loadVideoFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}

                var fileName = "Video File"
                var mimeType = "video/mp4"

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    }
                }
                mimeType = context.contentResolver.getType(uri) ?: mimeType

                _videoMetadata.value = VideoMetadata(
                    uriString = uri.toString(),
                    fileName = fileName,
                    durationMs = 0L,
                    mimeType = mimeType
                )
                playerController.loadMedia(uri)
                saveCurrentProject()
                _toastMessage.value = "Loaded video: $fileName"
                _currentTab.value = AppTab.EDITOR
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading video URI", e)
                _toastMessage.value = "Failed to load video file"
            }
        }
    }

    fun unloadVideo() {
        playerController.unloadMedia()
        _videoMetadata.value = VideoMetadata(uriString = "", fileName = "", durationMs = 0L)
        saveCurrentProject()
        _toastMessage.value = "Video unloaded"
    }

    fun loadSubtitleFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (_: Exception) {}

                var fileName = "subtitles.srt"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    }
                }

                val track = context.contentResolver.openInputStream(uri)?.use { stream ->
                    SubtitleParser.parse(stream, fileName)
                } ?: SubtitleTrack(title = fileName, cues = emptyList())

                _subtitleSourceUri.value = uri
                _subtitleTrack.value = track
                val firstCue = track.cues.firstOrNull()
                _selectedCue.value = firstCue
                if (firstCue != null) {
                    jumpToCue(firstCue)
                }
                saveCurrentProject()
                _toastMessage.value = "Loaded ${track.cues.size} cues from $fileName"
                _currentTab.value = AppTab.EDITOR
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading subtitle file", e)
                _toastMessage.value = "Failed to load subtitle file"
            }
        }
    }

    fun unloadSubtitleTrack() {
        _subtitleTrack.value = SubtitleTrack(
            title = "Empty Subtitle Track",
            format = SubtitleFormat.SRT,
            cues = emptyList()
        )
        _selectedCue.value = null
        _activeCue.value = null
        saveCurrentProject()
        _toastMessage.value = "Subtitles unloaded"
    }

    fun createNewSubtitleTrack() {
        val currentMs = playerState.value.currentPositionMs
        val initialCue = SubtitleCue(
            id = UUID.randomUUID().toString(),
            startTimeMs = currentMs,
            endTimeMs = currentMs + 2500L,
            text = "First Subtitle",
            posX = 0.50f,
            posY = 0.85f,
            alignment = SubtitleAlignment.BOTTOM_CENTER
        )
        _subtitleTrack.value = SubtitleTrack(
            title = "New Subtitles",
            format = SubtitleFormat.SRT,
            cues = listOf(initialCue)
        )
        _selectedCue.value = initialCue
        saveCurrentProject()
        _toastMessage.value = "Created new subtitle track"
    }

    fun exportSubtitleToFile(uri: Uri, format: SubtitleFormat) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val text = SubtitleWriter.generate(
                    track = _subtitleTrack.value,
                    format = format,
                    videoWidth = playerState.value.videoWidth,
                    videoHeight = playerState.value.videoHeight
                )
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(text.toByteArray(Charsets.UTF_8))
                }
                _toastMessage.value = "Saved subtitles as ${format.extension.uppercase()}"
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error saving subtitle file", e)
                _toastMessage.value = "Failed to save subtitle file"
            }
        }
    }

    fun selectNextCue() {
        val cues = _subtitleTrack.value.cues
        if (cues.isEmpty()) return
        val currentIdx = cues.indexOfFirst { it.id == _selectedCue.value?.id }
        if (currentIdx in 0 until cues.size - 1) {
            val nextCue = cues[currentIdx + 1]
            jumpToCue(nextCue)
        } else if (currentIdx == -1 && cues.isNotEmpty()) {
            jumpToCue(cues.first())
        }
    }

    fun selectPreviousCue() {
        val cues = _subtitleTrack.value.cues
        if (cues.isEmpty()) return
        val currentIdx = cues.indexOfFirst { it.id == _selectedCue.value?.id }
        if (currentIdx > 0) {
            val prevCue = cues[currentIdx - 1]
            jumpToCue(prevCue)
        }
    }

    fun setAspectRatio(option: AspectRatioOption) {
        _aspectRatio.value = option
    }

    private fun pushUndoHistory() {
        undoStack.add(_subtitleTrack.value)
        if (undoStack.size > 50) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = false
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.add(_subtitleTrack.value)
        val previous = undoStack.removeAt(undoStack.lastIndex)
        _subtitleTrack.value = previous
        _selectedCue.value = previous.cues.firstOrNull { it.id == _selectedCue.value?.id } ?: previous.cues.firstOrNull()
        _activeCue.value = _selectedCue.value
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        saveCurrentProject()
        _toastMessage.value = "Action Undone"
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.add(_subtitleTrack.value)
        val next = redoStack.removeAt(redoStack.lastIndex)
        _subtitleTrack.value = next
        _selectedCue.value = next.cues.firstOrNull { it.id == _selectedCue.value?.id } ?: next.cues.firstOrNull()
        _activeCue.value = _selectedCue.value
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        saveCurrentProject()
        _toastMessage.value = "Action Redone"
    }

    fun selectCue(cue: SubtitleCue) {
        _selectedCue.value = cue
        _activeCue.value = cue // Immediately display this cue's text overlay on video
    }

    fun jumpToCue(cue: SubtitleCue) {
        _selectedCue.value = cue
        _activeCue.value = cue // Immediately display this cue's text overlay on video
        playerController.seekTo(cue.startTimeMs)
    }

    fun updateCueFontSize(cue: SubtitleCue, newSizeSp: Float, applyToAll: Boolean = false) {
        pushUndoHistory()
        val clampedSize = newSizeSp.coerceIn(10f, 60f)
        val targetStyle = cue.style.copy(fontSizeSp = clampedSize)
        updateSubtitleStyle(targetStyle, applyToAll)
    }

    fun nudgeCueFontSize(deltaSp: Float) {
        val targetCue = _selectedCue.value ?: _activeCue.value ?: return
        val newSize = (targetCue.style.fontSizeSp + deltaSp).coerceIn(10f, 60f)
        updateCueFontSize(targetCue, newSize, applyToAll = false)
    }

    fun setShowOptimizationSheet(show: Boolean) {
        if (show && playerState.value.isPlaying) playerController.pause()
        _showOptimizationSheet.value = show
    }

    fun updateSubtitlePosition(
        posX: Float,
        posY: Float,
        alignment: SubtitleAlignment = SubtitleAlignment.CUSTOM,
        applyToAll: Boolean = false
    ) {
        val targetCue = _activeCue.value ?: _selectedCue.value
        val cues = _subtitleTrack.value.cues
        val updatedCues = cues.map { cue ->
            if (applyToAll || (targetCue != null && cue.id == targetCue.id)) {
                cue.copy(posX = posX, posY = posY, alignment = alignment)
            } else {
                cue
            }
        }
        _subtitleTrack.update { it.copy(cues = updatedCues) }
        if (targetCue != null) {
            val refreshed = updatedCues.firstOrNull { it.id == targetCue.id }
            if (refreshed != null) {
                _selectedCue.value = refreshed
                if (_activeCue.value?.id == refreshed.id) {
                    _activeCue.value = refreshed
                }
            }
        }
        saveCurrentProject()
    }

    fun updateSubtitleStyle(style: SubtitleStyle, applyToAll: Boolean) {
        val targetCue = _selectedCue.value ?: _activeCue.value
        val updatedCues = _subtitleTrack.value.cues.map { cue ->
            if (applyToAll || (targetCue != null && cue.id == targetCue.id)) {
                cue.copy(style = style)
            } else {
                cue
            }
        }
        _subtitleTrack.update {
            if (applyToAll) it.copy(cues = updatedCues, defaultStyle = style)
            else it.copy(cues = updatedCues)
        }
        if (targetCue != null) {
            val refreshed = updatedCues.firstOrNull { it.id == targetCue.id }
            if (refreshed != null) {
                _selectedCue.value = refreshed
                if (_activeCue.value?.id == refreshed.id) {
                    _activeCue.value = refreshed
                }
            }
        }
        saveCurrentProject()
    }

    fun updateCueText(cue: SubtitleCue, newText: String) {
        val updatedCues = _subtitleTrack.value.cues.map {
            if (it.id == cue.id) it.copy(text = newText) else it
        }
        _subtitleTrack.update { it.copy(cues = updatedCues) }
        if (_selectedCue.value?.id == cue.id) {
            _selectedCue.value = _selectedCue.value?.copy(text = newText)
        }
        if (_activeCue.value?.id == cue.id) {
            _activeCue.value = _activeCue.value?.copy(text = newText)
        }
        saveCurrentProject()
    }

    fun setCueStartTime(cue: SubtitleCue, newStartMs: Long) {
        val boundedStart = newStartMs.coerceIn(0L, cue.endTimeMs - 50L)
        val updatedCue = cue.copy(startTimeMs = boundedStart)
        val updatedCues = _subtitleTrack.value.cues.map {
            if (it.id == cue.id) updatedCue else it
        }.sortedBy { it.startTimeMs }
        _subtitleTrack.update { it.copy(cues = updatedCues) }
        _selectedCue.value = updatedCue
        saveCurrentProject()
    }

    fun setCueEndTime(cue: SubtitleCue, newEndMs: Long) {
        val maxDuration = playerState.value.durationMs.takeIf { it > 0 } ?: (newEndMs + 10000L)
        val boundedEnd = newEndMs.coerceIn(cue.startTimeMs + 50L, maxDuration)
        val updatedCue = cue.copy(endTimeMs = boundedEnd)
        val updatedCues = _subtitleTrack.value.cues.map {
            if (it.id == cue.id) updatedCue else it
        }.sortedBy { it.startTimeMs }
        _subtitleTrack.update { it.copy(cues = updatedCues) }
        _selectedCue.value = updatedCue
        saveCurrentProject()
    }

    fun shiftCueTiming(cue: SubtitleCue, deltaMs: Long) {
        val dur = cue.endTimeMs - cue.startTimeMs
        val newStart = (cue.startTimeMs + deltaMs).coerceAtLeast(0L)
        val newEnd = newStart + dur
        val updatedCue = cue.copy(startTimeMs = newStart, endTimeMs = newEnd)
        val updatedCues = _subtitleTrack.value.cues.map {
            if (it.id == cue.id) updatedCue else it
        }.sortedBy { it.startTimeMs }
        _subtitleTrack.update { it.copy(cues = updatedCues) }
        _selectedCue.value = updatedCue
        saveCurrentProject()
    }

    fun nudgeTiming(deltaStartMs: Long, deltaEndMs: Long) {
        val cue = _selectedCue.value ?: return
        val newStart = (cue.startTimeMs + deltaStartMs).coerceAtLeast(0L)
        val newEnd = (cue.endTimeMs + deltaEndMs).coerceAtLeast(newStart + 50L)
        val updatedCue = cue.copy(startTimeMs = newStart, endTimeMs = newEnd)

        val updatedCues = _subtitleTrack.value.cues.map {
            if (it.id == cue.id) updatedCue else it
        }.sortedBy { it.startTimeMs }
        _subtitleTrack.update { it.copy(cues = updatedCues) }
        _selectedCue.value = updatedCue
        saveCurrentProject()
    }

    fun batchShiftTiming(deltaMs: Long) {
        val updatedCues = _subtitleTrack.value.cues.map { cue ->
            cue.copy(
                startTimeMs = (cue.startTimeMs + deltaMs).coerceAtLeast(0L),
                endTimeMs = (cue.endTimeMs + deltaMs).coerceAtLeast(50L)
            )
        }.sortedBy { it.startTimeMs }
        _subtitleTrack.update { it.copy(cues = updatedCues) }
        _selectedCue.value?.let { current ->
            _selectedCue.value = updatedCues.firstOrNull { it.id == current.id }
        }
        saveCurrentProject()
        _toastMessage.value = "Shifted all cues by ${if (deltaMs >= 0) "+$deltaMs" else "$deltaMs"}ms"
    }

    fun addCueAtCurrentPosition() {
        val currentMs = playerState.value.currentPositionMs
        val maxDuration = playerState.value.durationMs.takeIf { it > 0 } ?: (currentMs + 2500L)
        val newCue = SubtitleCue(
            id = UUID.randomUUID().toString(),
            startTimeMs = currentMs,
            endTimeMs = (currentMs + 2500L).coerceAtMost(maxDuration).coerceAtLeast(currentMs + 100L),
            text = "New Subtitle Text",
            posX = _selectedCue.value?.posX ?: 0.50f,
            posY = _selectedCue.value?.posY ?: 0.85f,
            alignment = _selectedCue.value?.alignment ?: SubtitleAlignment.BOTTOM_CENTER,
            style = _selectedCue.value?.style ?: SubtitleStyle()
        )
        val newCues = (_subtitleTrack.value.cues + newCue).sortedBy { it.startTimeMs }
        _subtitleTrack.update { it.copy(cues = newCues) }
        _selectedCue.value = newCue
        _activeCue.value = newCue
        saveCurrentProject()
    }

    fun splitCueAtCurrentPosition() {
        val cue = _selectedCue.value ?: _activeCue.value ?: return
        val currentMs = playerState.value.currentPositionMs
        if (currentMs <= cue.startTimeMs + 100L || currentMs >= cue.endTimeMs - 100L) {
            addCueAtCurrentPosition()
            return
        }

        val firstPart = cue.copy(endTimeMs = currentMs)
        val secondPart = SubtitleCue(
            id = UUID.randomUUID().toString(),
            startTimeMs = currentMs,
            endTimeMs = cue.endTimeMs,
            text = cue.text,
            posX = cue.posX,
            posY = cue.posY,
            alignment = cue.alignment,
            style = cue.style
        )

        val updatedCues = _subtitleTrack.value.cues.map {
            if (it.id == cue.id) firstPart else it
        } + secondPart
        val sortedCues = updatedCues.sortedBy { it.startTimeMs }
        _subtitleTrack.update { it.copy(cues = sortedCues) }
        _selectedCue.value = secondPart
        _activeCue.value = secondPart
        saveCurrentProject()
    }

    fun duplicateSelectedCue() {
        val cue = _selectedCue.value ?: return
        val dur = cue.endTimeMs - cue.startTimeMs
        val newCue = cue.copy(
            id = UUID.randomUUID().toString(),
            startTimeMs = cue.endTimeMs + 50L,
            endTimeMs = cue.endTimeMs + 50L + dur
        )
        val newCues = (_subtitleTrack.value.cues + newCue).sortedBy { it.startTimeMs }
        _subtitleTrack.update { it.copy(cues = newCues) }
        _selectedCue.value = newCue
        _activeCue.value = newCue
        playerController.seekTo(newCue.startTimeMs)
        saveCurrentProject()
    }

    fun deleteSelectedCue() {
        val target = _selectedCue.value ?: return
        val updatedCues = _subtitleTrack.value.cues.filter { it.id != target.id }
        _subtitleTrack.update { it.copy(cues = updatedCues) }
        _selectedCue.value = updatedCues.firstOrNull()
        if (_activeCue.value?.id == target.id) {
            _activeCue.value = _selectedCue.value
        }
        saveCurrentProject()
    }

    fun deleteCue(cue: SubtitleCue) {
        val updatedCues = _subtitleTrack.value.cues.filter { it.id != cue.id }
        _subtitleTrack.update { it.copy(cues = updatedCues) }
        if (_selectedCue.value?.id == cue.id) {
            _selectedCue.value = updatedCues.firstOrNull()
        }
        if (_activeCue.value?.id == cue.id) {
            _activeCue.value = _selectedCue.value
        }
        saveCurrentProject()
    }

    fun startExport(config: ExportConfig) {
        viewModelScope.launch {
            if (playerState.value.isPlaying) {
                playerController.pause()
            }
            val uri = Uri.parse(_videoMetadata.value.uriString.ifEmpty { "android.resource://dummy" })
            exportManager.export(
                videoUri = uri,
                subtitleTrack = _subtitleTrack.value,
                config = config
            )
        }
    }

    fun resetExportState() {
        exportManager.resetState()
    }

    // Batch processing triggers
    fun addBatchSubtitleConversion(uris: List<Uri>, targetFormat: SubtitleFormat) {
        val tasks = uris.map { uri ->
            val fileName = uri.lastPathSegment ?: "subtitle.srt"
            BatchTask(
                title = "Convert: $fileName ➔ ${targetFormat.extension.uppercase()}",
                sourceSubtitleUri = uri.toString(),
                operationType = BatchOperationType.CONVERT_FORMAT,
                targetFormat = targetFormat,
                outputFileName = fileName
            )
        }
        batchManager.addTasks(tasks)
    }

    fun addBatchTimingShift(uris: List<Uri>, shiftMs: Long) {
        val tasks = uris.map { uri ->
            val fileName = uri.lastPathSegment ?: "subtitle.srt"
            BatchTask(
                title = "Sync: $fileName (${if (shiftMs >= 0) "+$shiftMs" else "$shiftMs"}ms)",
                sourceSubtitleUri = uri.toString(),
                operationType = BatchOperationType.TIME_SHIFT,
                timeShiftMs = shiftMs,
                outputFileName = fileName
            )
        }
        batchManager.addTasks(tasks)
    }

    fun startBatchProcessing() {
        batchManager.startBatch(_processingSettings.value)
    }

    fun cancelBatchProcessing() {
        batchManager.cancelBatch()
    }

    fun clearBatchTasks() {
        batchManager.clearTasks()
    }

    fun updateProcessingSettings(settings: ProcessingSettings) {
        _processingSettings.value = settings
    }

    private val _showFindReplaceDialog = MutableStateFlow(false)
    val showFindReplaceDialog: StateFlow<Boolean> = _showFindReplaceDialog.asStateFlow()

    private val _showSanitizerDialog = MutableStateFlow(false)
    val showSanitizerDialog: StateFlow<Boolean> = _showSanitizerDialog.asStateFlow()

    private val _showPresetsSheet = MutableStateFlow(false)
    val showPresetsSheet: StateFlow<Boolean> = _showPresetsSheet.asStateFlow()

    private val _showDiagnosticsDialog = MutableStateFlow(false)
    val showDiagnosticsDialog: StateFlow<Boolean> = _showDiagnosticsDialog.asStateFlow()

    fun setShowFindReplaceDialog(show: Boolean) {
        if (show && playerState.value.isPlaying) playerController.pause()
        _showFindReplaceDialog.value = show
    }

    fun setShowSanitizerDialog(show: Boolean) {
        if (show && playerState.value.isPlaying) playerController.pause()
        _showSanitizerDialog.value = show
    }

    fun setShowPresetsSheet(show: Boolean) {
        if (show && playerState.value.isPlaying) playerController.pause()
        _showPresetsSheet.value = show
    }

    fun setShowDiagnosticsDialog(show: Boolean) {
        _showDiagnosticsDialog.value = show
    }

    fun findAndReplace(findText: String, replaceText: String, matchCase: Boolean = false, useRegex: Boolean = false): Int {
        if (findText.isEmpty()) return 0
        pushUndoHistory()
        var replaceCount = 0
        val currentCues = _subtitleTrack.value.cues

        val updatedCues = currentCues.map { cue ->
            val originalText = cue.text
            val newText = try {
                if (useRegex) {
                    val regexOptions = if (matchCase) setOf() else setOf(RegexOption.IGNORE_CASE)
                    val regex = Regex(findText, regexOptions)
                    val matches = regex.findAll(originalText).count()
                    replaceCount += matches
                    regex.replace(originalText, replaceText)
                } else {
                    if (matchCase) {
                        val occurrences = originalText.windowed(findText.length, 1).count { it == findText }
                        replaceCount += occurrences
                        originalText.replace(findText, replaceText)
                    } else {
                        // Case-insensitive literal replace
                        val regex = Regex(Regex.escape(findText), RegexOption.IGNORE_CASE)
                        val matches = regex.findAll(originalText).count()
                        replaceCount += matches
                        regex.replace(originalText, replaceText)
                    }
                }
            } catch (e: Exception) {
                originalText
            }

            cue.copy(text = newText)
        }

        if (replaceCount > 0) {
            _subtitleTrack.update { it.copy(cues = updatedCues) }
            val currentSelected = _selectedCue.value
            if (currentSelected != null) {
                _selectedCue.value = updatedCues.firstOrNull { it.id == currentSelected.id }
            }
            saveCurrentProject()
            _toastMessage.value = "Replaced $replaceCount occurrences"
        } else {
            _toastMessage.value = "No matches found for \"$findText\""
        }
        return replaceCount
    }

    fun autoRepairOverlaps(minGapMs: Long = 50L): Int {
        val currentCues = _subtitleTrack.value.cues
        if (currentCues.isEmpty()) return 0

        pushUndoHistory()
        val sorted = currentCues.sortedBy { it.startTimeMs }
        var repairedCount = 0
        val repairedCues = mutableListOf<SubtitleCue>()

        for (i in sorted.indices) {
            var cue = sorted[i]
            // Ensure end time is strictly greater than start time
            if (cue.endTimeMs <= cue.startTimeMs) {
                cue = cue.copy(endTimeMs = cue.startTimeMs + 1000L)
                repairedCount++
            }

            // Check overlap with next cue
            if (i < sorted.size - 1) {
                val nextStart = sorted[i + 1].startTimeMs
                if (cue.endTimeMs + minGapMs > nextStart) {
                    val newEnd = (nextStart - minGapMs).coerceAtLeast(cue.startTimeMs + 200L)
                    cue = cue.copy(endTimeMs = newEnd)
                    repairedCount++
                }
            }
            repairedCues.add(cue)
        }

        _subtitleTrack.update { it.copy(cues = repairedCues) }
        val currentSelected = _selectedCue.value
        if (currentSelected != null) {
            _selectedCue.value = repairedCues.firstOrNull { it.id == currentSelected.id }
        }
        saveCurrentProject()
        _toastMessage.value = "Sanitized $repairedCount cues (min gap: ${minGapMs}ms)"
        return repairedCount
    }

    fun reorderCuesChronologically() {
        pushUndoHistory()
        val sorted = _subtitleTrack.value.cues.sortedBy { it.startTimeMs }
        _subtitleTrack.update { it.copy(cues = sorted) }
        saveCurrentProject()
        _toastMessage.value = "Cues sorted chronologically"
    }

    fun applyPresetStyle(presetName: String) {
        pushUndoHistory()
        val style = when (presetName.lowercase()) {
            "cinema_gold" -> SubtitleStyle(
                fontSizeSp = 24f,
                textColorArgb = 0xFFFFD700, // Gold
                strokeColorArgb = 0xFF000000,
                strokeWidthDp = 3f,
                backgroundColorArgb = 0x00000000,
                isBold = true,
                shadowRadiusDp = 6f
            )
            "tiktok_neon" -> SubtitleStyle(
                fontSizeSp = 26f,
                textColorArgb = 0xFFFFF500, // Neon yellow
                strokeColorArgb = 0xFF000000,
                strokeWidthDp = 4f,
                backgroundColorArgb = 0x00000000,
                isBold = true,
                shadowRadiusDp = 4f
            )
            "clean_minimal" -> SubtitleStyle(
                fontSizeSp = 20f,
                textColorArgb = 0xFFFFFFFF,
                strokeColorArgb = 0x88000000,
                strokeWidthDp = 1.5f,
                backgroundColorArgb = 0x00000000,
                isBold = false,
                shadowRadiusDp = 2f
            )
            "closed_caption" -> SubtitleStyle(
                fontSizeSp = 20f,
                textColorArgb = 0xFFFFFFFF,
                strokeColorArgb = 0x00000000,
                strokeWidthDp = 0f,
                backgroundColorArgb = 0xD9000000, // 85% black box
                cornerRadiusDp = 4f,
                paddingHorizontalDp = 12f,
                paddingVerticalDp = 6f,
                isBold = true
            )
            "anime_ass" -> SubtitleStyle(
                fontSizeSp = 23f,
                textColorArgb = 0xFFE0FFFF, // Light Cyan
                strokeColorArgb = 0xFF191970, // Midnight Blue
                strokeWidthDp = 3.5f,
                backgroundColorArgb = 0x00000000,
                isBold = true,
                shadowRadiusDp = 5f
            )
            else -> SubtitleStyle()
        }
        updateSubtitleStyle(style, applyToAll = true)
        _toastMessage.value = "Applied preset: $presetName"
    }

    fun resetSettingsToDefault() {
        _processingSettings.value = ProcessingSettings()
        _toastMessage.value = "Settings reset to studio defaults"
    }

    fun clearTemporaryCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheDir = getApplication<Application>().cacheDir
                cacheDir.deleteRecursively()
                cacheDir.mkdirs()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error clearing cache", e)
            }
        }
        _toastMessage.value = "Cache Purged Successfully"
    }

    fun setShowPlacementDialog(show: Boolean) {
        if (show && playerState.value.isPlaying) playerController.pause()
        _showPlacementDialog.value = show
    }

    fun setShowStyleDialog(show: Boolean) {
        if (show && playerState.value.isPlaying) playerController.pause()
        _showStyleDialog.value = show
    }

    fun setShowExportDialog(show: Boolean) {
        if (show && playerState.value.isPlaying) playerController.pause()
        _showExportDialog.value = show
    }

    fun setShowSubtitleListSheet(show: Boolean) {
        if (show && playerState.value.isPlaying) playerController.pause()
        _showSubtitleListSheet.value = show
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
