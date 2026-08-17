package com.example.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import com.example.model.ExportConfig
import com.example.model.ExportMode
import com.example.model.ExportState
import com.example.model.SubtitleCue
import com.example.model.SubtitleTrack
import com.example.parser.SubtitleWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class VideoExportManager(private val context: Context) {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    suspend fun export(
        videoUri: Uri,
        subtitleTrack: SubtitleTrack,
        config: ExportConfig,
        onProgress: ((Float) -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        _exportState.value = ExportState.Exporting(0f, 0L, 100L, "Preparing export...")
        onProgress?.invoke(0f)

        try {
            when (config.exportMode) {
                ExportMode.SUBTITLE_ONLY -> {
                    exportSubtitleOnly(subtitleTrack, config)
                    onProgress?.invoke(1f)
                }
                ExportMode.LOSSLESS_SOFT_MUX -> {
                    exportLosslessSoftMux(videoUri, subtitleTrack, config, onProgress)
                }
                ExportMode.HARDCODE_BURN_IN -> {
                    exportHardcodedBurnIn(videoUri, subtitleTrack, config, onProgress)
                }
            }
        } catch (e: Exception) {
            Log.e("VideoExportManager", "Export failed", e)
            _exportState.value = ExportState.Error(e.message ?: "Export failed due to unknown error")
        }
    }

    private fun exportSubtitleOnly(subtitleTrack: SubtitleTrack, config: ExportConfig) {
        val format = config.subtitleExportFormat
        val content = SubtitleWriter.generate(subtitleTrack, format)

        val outputDir = context.getExternalFilesDir(null) ?: context.filesDir
        val timeStamp = System.currentTimeMillis()
        val outputFile = File(outputDir, "subvid_${subtitleTrack.title.replace(" ", "_")}_$timeStamp.${format.extension}")

        FileOutputStream(outputFile).use { fos ->
            fos.write(content.toByteArray(Charsets.UTF_8))
        }

        val sizeFormatted = "${outputFile.length()} bytes"
        _exportState.value = ExportState.Success(
            outputFilePath = outputFile.absolutePath,
            fileSizeFormatted = sizeFormatted,
            mode = ExportMode.SUBTITLE_ONLY
        )
    }

    private fun exportLosslessSoftMux(
        videoUri: Uri,
        subtitleTrack: SubtitleTrack,
        config: ExportConfig,
        onProgress: ((Float) -> Unit)? = null
    ) {
        _exportState.value = ExportState.Exporting(0.1f, 0, 100, "Multiplexing container with subtitle stream...")
        onProgress?.invoke(0.1f)

        val outputDir = context.getExternalFilesDir(null) ?: context.filesDir
        val timeStamp = System.currentTimeMillis()
        val ext = if (config.containerFormat.equals("mkv", ignoreCase = true)) "mkv" else "mp4"
        val outputFile = File(outputDir, "subvid_lossless_mux_$timeStamp.$ext")
        val subFile = File(outputDir, "subvid_lossless_mux_$timeStamp.srt")

        // 1. Write companion subtitle file
        val srtContent = SubtitleWriter.generateSrt(subtitleTrack)
        subFile.writeText(srtContent)

        // 2. Remux video and audio streams into output container
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, videoUri, null)
        } catch (e: Exception) {
            extractor.setDataSource(videoUri.path ?: "")
        }

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val trackMap = mutableMapOf<Int, Int>()
        val numTracks = extractor.trackCount

        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("video/") || mime.startsWith("audio/")) {
                val muxerTrackIndex = muxer.addTrack(format)
                trackMap[i] = muxerTrackIndex
                extractor.selectTrack(i)
            }
        }

        muxer.start()
        val buffer = ByteBuffer.allocate(1024 * 1024 * 2) // 2MB buffer
        val bufferInfo = MediaCodec.BufferInfo()

        var sampleCount = 0L
        while (true) {
            val trackIndex = extractor.sampleTrackIndex
            if (trackIndex < 0) break

            val muxerTrack = trackMap[trackIndex]
            if (muxerTrack != null) {
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) break
                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags
                muxer.writeSampleData(muxerTrack, buffer, bufferInfo)
            }
            sampleCount++
            if (sampleCount % 100 == 0L) {
                _exportState.value = ExportState.Exporting(0.5f, sampleCount, 0, "Lossless muxing stream data...")
            }
            extractor.advance()
        }

        muxer.stop()
        muxer.release()
        extractor.release()

        val sizeFormatted = "${outputFile.length() / (1024 * 1024)} MB"
        _exportState.value = ExportState.Success(
            outputFilePath = outputFile.absolutePath,
            fileSizeFormatted = sizeFormatted,
            mode = ExportMode.LOSSLESS_SOFT_MUX
        )
    }

    private fun exportHardcodedBurnIn(
        videoUri: Uri,
        subtitleTrack: SubtitleTrack,
        config: ExportConfig,
        onProgress: ((Float) -> Unit)? = null
    ) {
        _exportState.value = ExportState.Exporting(0.05f, 0, 100, "Initializing high-bitrate video encoder...")
        onProgress?.invoke(0.05f)

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
        } catch (e: Exception) {
            retriever.setDataSource(videoUri.path ?: "")
        }

        val durationUs = (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 10_000L) * 1000L
        val originalWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 1920
        val originalHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 1080
        retriever.release()

        val targetWidth: Int
        val targetHeight: Int
        when (config.qualityPreset) {
            com.example.model.QualityPreset.NATIVE_ULTRA -> {
                targetWidth = originalWidth
                targetHeight = originalHeight
            }
            com.example.model.QualityPreset.FULL_HD_1080P -> {
                targetWidth = 1920
                targetHeight = 1080
            }
            com.example.model.QualityPreset.HD_720P -> {
                targetWidth = 1280
                targetHeight = 720
            }
        }

        val bitrate = config.qualityPreset.targetBitrateBps
        val fps = config.qualityPreset.targetFps

        val outputDir = context.getExternalFilesDir(null) ?: context.filesDir
        val timeStamp = System.currentTimeMillis()
        val outputFile = File(outputDir, "subvid_burned_native_$timeStamp.mp4")

        // Setup MediaCodec Encoder
        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
        val format = MediaFormat.createVideoFormat(mimeType, targetWidth, targetHeight).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val encoder = MediaCodec.createEncoderByType(mimeType)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = encoder.createInputSurface()
        encoder.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var videoTrackIndex = -1
        var muxerStarted = false
        val bufferInfo = MediaCodec.BufferInfo()

        // Prepare frame renderer paints
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
        }
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        // Frame generation loop
        val totalFrames = ((durationUs / 1_000_000f) * fps).toLong().coerceAtLeast(30L)
        val frameDurationUs = 1_000_000L / fps

        // Also extract background frames or render pristine canvas
        val retrieverFrame = MediaMetadataRetriever()
        try {
            retrieverFrame.setDataSource(context, videoUri)
        } catch (e: Exception) {
            retrieverFrame.setDataSource(videoUri.path ?: "")
        }

        for (frameIndex in 0 until totalFrames) {
            val presentationTimeUs = frameIndex * frameDurationUs
            val presentationTimeMs = presentationTimeUs / 1000L

            // Get source video frame bitmap
            val sourceBitmap = try {
                retrieverFrame.getFrameAtTime(presentationTimeUs, MediaMetadataRetriever.OPTION_CLOSEST)
            } catch (e: Exception) {
                null
            }

            val canvas = inputSurface.lockCanvas(null)

            if (sourceBitmap != null) {
                val destRect = android.graphics.Rect(0, 0, targetWidth, targetHeight)
                canvas.drawBitmap(sourceBitmap, null, destRect, null)
                sourceBitmap.recycle()
            } else {
                // Fallback background
                canvas.drawColor(Color.rgb(15, 17, 23))
            }

            // Find active subtitles at this millisecond
            val activeCues = subtitleTrack.cues.filter { it.isActiveAt(presentationTimeMs) }
            for (cue in activeCues) {
                renderSubtitleOnCanvas(
                    canvas = canvas,
                    cue = cue,
                    videoWidth = targetWidth,
                    videoHeight = targetHeight,
                    textPaint = textPaint,
                    strokePaint = strokePaint,
                    bgPaint = bgPaint
                )
            }

            inputSurface.unlockCanvasAndPost(canvas)

            // Drain encoder
            while (true) {
                val status = encoder.dequeueOutputBuffer(bufferInfo, 0)
                if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (!muxerStarted) {
                        videoTrackIndex = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                } else if (status >= 0) {
                    val outputBuffer = encoder.getOutputBuffer(status)
                    if (outputBuffer != null && bufferInfo.size > 0 && muxerStarted) {
                        bufferInfo.presentationTimeUs = presentationTimeUs
                        muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(status, false)
                }
            }

            val progress = (frameIndex.toFloat() / totalFrames).coerceIn(0f, 1f)
            onProgress?.invoke(progress)
            _exportState.value = ExportState.Exporting(
                progress = progress,
                currentFrame = frameIndex + 1,
                totalFrames = totalFrames,
                message = "Encoding frame ${frameIndex + 1}/$totalFrames (${(progress * 100).toInt()}%)"
            )
        }

        retrieverFrame.release()
        encoder.signalEndOfInputStream()

        var eos = false
        while (!eos) {
            val status = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
            if (status >= 0) {
                val outputBuffer = encoder.getOutputBuffer(status)
                if (outputBuffer != null && bufferInfo.size > 0 && muxerStarted) {
                    muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
                }
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    eos = true
                }
                encoder.releaseOutputBuffer(status, false)
            } else if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break
            }
        }

        encoder.stop()
        encoder.release()
        inputSurface.release()

        if (muxerStarted) {
            muxer.stop()
            muxer.release()
        }

        val sizeFormatted = String.format("%.1f MB", outputFile.length() / (1024f * 1024f))
        _exportState.value = ExportState.Success(
            outputFilePath = outputFile.absolutePath,
            fileSizeFormatted = sizeFormatted,
            mode = ExportMode.HARDCODE_BURN_IN
        )
    }

    private fun renderSubtitleOnCanvas(
        canvas: Canvas,
        cue: SubtitleCue,
        videoWidth: Int,
        videoHeight: Int,
        textPaint: Paint,
        strokePaint: Paint,
        bgPaint: Paint
    ) {
        val style = cue.style
        val scaleFactor = videoHeight / 720f // Scale typography relative to 720p base
        val scaledFontSize = style.fontSizeSp * 2.2f * scaleFactor

        val typefaceStyle = when {
            style.isBold && style.isItalic -> Typeface.BOLD_ITALIC
            style.isBold -> Typeface.BOLD
            style.isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        val typeface = Typeface.create(Typeface.SANS_SERIF, typefaceStyle)

        textPaint.typeface = typeface
        textPaint.textSize = scaledFontSize
        textPaint.color = style.textColorArgb.toInt()

        strokePaint.typeface = typeface
        strokePaint.textSize = scaledFontSize
        strokePaint.color = style.strokeColorArgb.toInt()
        strokePaint.strokeWidth = style.strokeWidthDp * 2.5f * scaleFactor

        val lines = cue.text.split("\n")
        val fontMetrics = textPaint.fontMetrics
        val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * 1.15f
        val totalTextHeight = lineHeight * lines.size

        var maxLineWidth = 0f
        for (line in lines) {
            val w = textPaint.measureText(line)
            if (w > maxLineWidth) maxLineWidth = w
        }

        val targetX = cue.posX * videoWidth
        val targetY = cue.posY * videoHeight

        // Background Box if enabled
        if ((style.backgroundColorArgb ushr 24) > 0) {
            bgPaint.color = style.backgroundColorArgb.toInt()
            val padX = style.paddingHorizontalDp * 2.5f * scaleFactor
            val padY = style.paddingVerticalDp * 2.5f * scaleFactor
            val boxLeft = targetX - (maxLineWidth / 2f) - padX
            val boxRight = targetX + (maxLineWidth / 2f) + padX
            val boxTop = targetY - (totalTextHeight / 2f) - padY
            val boxBottom = targetY + (totalTextHeight / 2f) + padY
            val radius = style.cornerRadiusDp * 2f * scaleFactor

            canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxBottom), radius, radius, bgPaint)
        }

        // Draw each line centered around targetX, targetY
        var currentBaselineY = targetY - (totalTextHeight / 2f) - fontMetrics.ascent
        for (line in lines) {
            if (style.strokeWidthDp > 0) {
                canvas.drawText(line, targetX, currentBaselineY, strokePaint)
            }
            canvas.drawText(line, targetX, currentBaselineY, textPaint)
            currentBaselineY += lineHeight
        }
    }

    fun resetState() {
        _exportState.value = ExportState.Idle
    }
}
