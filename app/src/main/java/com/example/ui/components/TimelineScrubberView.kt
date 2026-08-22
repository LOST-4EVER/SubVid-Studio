package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProcessingSettings
import com.example.model.SubtitleCue
import com.example.model.SubtitleTrack
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import kotlin.math.abs
import kotlin.math.sin

private enum class DragMode { NONE, MOVE_CUE, TRIM_START, TRIM_END, SCRUB_PLAYHEAD }

@Composable
fun TimelineScrubberView(
    currentPositionMs: Long,
    durationMs: Long,
    subtitleTrack: SubtitleTrack,
    selectedCue: SubtitleCue?,
    processingSettings: ProcessingSettings = ProcessingSettings(),
    onSeek: (Long) -> Unit,
    onSelectCue: (SubtitleCue) -> Unit,
    onSetCueStartTime: (SubtitleCue, Long) -> Unit,
    onSetCueEndTime: (SubtitleCue, Long) -> Unit,
    onShiftCueTiming: (SubtitleCue, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDuration = if (durationMs > 0) durationMs else 1L
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = ImmersivePrimary
    val accentCyan = AccentCyan

    var zoomLevel by remember(processingSettings.timelineZoomLevel) {
        mutableFloatStateOf(processingSettings.timelineZoomLevel.coerceIn(1.0f, 8.0f))
    }
    val zoomOptions = listOf(1.0f, 2.0f, 4.0f)
    val scrollState = rememberScrollState()
    var isSnapToGridEnabled by remember(processingSettings.snapToCues) {
        mutableStateOf(processingSettings.snapToCues)
    }

    var activeDragMode by remember { mutableStateOf(DragMode.NONE) }
    var dragTargetCue by remember { mutableStateOf<SubtitleCue?>(null) }
    var dragFeedbackMessage by remember { mutableStateOf("") }

    // Auto-scroll timeline to keep playhead in view when zoomed
    LaunchedEffect(currentPositionMs, zoomLevel) {
        if (zoomLevel > 1.0f && totalDuration > 0 && activeDragMode == DragMode.NONE && processingSettings.autoScrollTimeline) {
            val progress = currentPositionMs.toFloat() / totalDuration
            val targetScroll = (progress * scrollState.maxValue).toInt()
            scrollState.scrollTo((targetScroll - 200).coerceIn(0, scrollState.maxValue))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RectangleShape)
            .background(ImmersiveSurface)
            .border(1.dp, ImmersiveBorder, RectangleShape)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Header: Status Tag, Snap Toggle, Zoom Controls, Timecode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AccentCyan, RectangleShape)
                )
                Text(
                    text = "TIMELINE • ${subtitleTrack.format.extension.uppercase()} (${subtitleTrack.cues.size})",
                    color = ImmersivePrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Controls Strip (Snap & Zoom + Timecode)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Snap Toggle
                Surface(
                    shape = RectangleShape,
                    color = if (isSnapToGridEnabled) AccentCyan.copy(alpha = 0.2f) else ImmersiveActionBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSnapToGridEnabled) AccentCyan else ImmersiveBorder),
                    modifier = Modifier.clickable { isSnapToGridEnabled = !isSnapToGridEnabled }
                ) {
                    Text(
                        text = if (isSnapToGridEnabled) "Snap: ON" else "Snap: OFF",
                        color = if (isSnapToGridEnabled) AccentCyan else ImmersiveTextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                zoomOptions.forEach { zoom ->
                    val isSelected = zoomLevel == zoom
                    Surface(
                        shape = RectangleShape,
                        color = if (isSelected) ImmersivePrimary else ImmersiveActionBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ImmersivePrimary else ImmersiveBorder),
                        modifier = Modifier.clickable { zoomLevel = zoom }
                    ) {
                        Text(
                            text = if (zoom == 1.0f) "Fit" else "${zoom.toInt()}x",
                            color = if (isSelected) Color.Black else ImmersiveTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Timecode
                Text(
                    text = "${SubtitleCue.formatTimestampShort(currentPositionMs)} / ${SubtitleCue.formatTimestampShort(totalDuration)}",
                    color = ImmersiveTextPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Live Drag Status Pill
        if (dragFeedbackMessage.isNotEmpty()) {
            Surface(
                shape = RectangleShape,
                color = AccentCyan.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dragFeedbackMessage,
                    color = AccentCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // 2. High-Performance Multi-Track Canvas
        val rulerHeight = 16f
        val cueTrackHeight = 28f
        val waveHeight = 16f
        val totalCanvasHeight = 64.dp

        val textLayoutCache = remember { mutableMapOf<String, TextLayoutResult>() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalCanvasHeight)
                .horizontalScroll(scrollState)
                .background(ImmersiveBg)
                .border(1.dp, ImmersiveBorder, RectangleShape)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalCanvasHeight)
                    .width(if (zoomLevel > 1.0f) (360 * zoomLevel).dp else 360.dp)
                    .pointerInput(totalDuration, subtitleTrack, isSnapToGridEnabled, zoomLevel) {
                        // A. Tap gesture
                        detectTapGestures { offset ->
                            val widthPx = size.width.toFloat()
                            val clickX = offset.x
                            val clickMs = ((clickX / widthPx) * totalDuration).toLong().coerceIn(0L, totalDuration)

                            val clickedCue = subtitleTrack.cues.firstOrNull { cue ->
                                val cueStartX = (cue.startTimeMs.toFloat() / totalDuration) * widthPx
                                val cueEndX = (cue.endTimeMs.toFloat() / totalDuration) * widthPx
                                clickX in (cueStartX - 4f)..(cueEndX + 4f)
                            }

                            if (clickedCue != null) {
                                onSelectCue(clickedCue)
                                onSeek(clickedCue.startTimeMs)
                            } else {
                                var seekTargetMs = clickMs
                                if (isSnapToGridEnabled) {
                                    val snapThresholdPx = 14f
                                    for (cue in subtitleTrack.cues) {
                                        val cueStartX = (cue.startTimeMs.toFloat() / totalDuration) * widthPx
                                        val cueEndX = (cue.endTimeMs.toFloat() / totalDuration) * widthPx
                                        if (abs(clickX - cueStartX) < snapThresholdPx) {
                                            seekTargetMs = cue.startTimeMs
                                            break
                                        } else if (abs(clickX - cueEndX) < snapThresholdPx) {
                                            seekTargetMs = cue.endTimeMs
                                            break
                                        }
                                    }
                                }
                                onSeek(seekTargetMs)
                            }
                        }
                    }
                    .pointerInput(totalDuration, subtitleTrack, selectedCue, isSnapToGridEnabled, zoomLevel) {
                        // B. Drag & Scrub Gestures
                        detectDragGestures(
                            onDragStart = { offset ->
                                val widthPx = size.width.toFloat()
                                val startX = offset.x
                                val startY = offset.y
                                val handleTolerance = 14f

                                if (startY >= rulerHeight && selectedCue != null) {
                                    val cueStartX = (selectedCue.startTimeMs.toFloat() / totalDuration) * widthPx
                                    val cueEndX = (selectedCue.endTimeMs.toFloat() / totalDuration) * widthPx

                                    if (abs(startX - cueStartX) <= handleTolerance) {
                                        activeDragMode = DragMode.TRIM_START
                                        dragTargetCue = selectedCue
                                    } else if (abs(startX - cueEndX) <= handleTolerance) {
                                        activeDragMode = DragMode.TRIM_END
                                        dragTargetCue = selectedCue
                                    } else if (startX in cueStartX..cueEndX) {
                                        activeDragMode = DragMode.MOVE_CUE
                                        dragTargetCue = selectedCue
                                    } else {
                                        activeDragMode = DragMode.SCRUB_PLAYHEAD
                                        val seekMs = ((startX / widthPx) * totalDuration).toLong().coerceIn(0L, totalDuration)
                                        onSeek(seekMs)
                                    }
                                } else {
                                    activeDragMode = DragMode.SCRUB_PLAYHEAD
                                    val seekMs = ((startX / widthPx) * totalDuration).toLong().coerceIn(0L, totalDuration)
                                    onSeek(seekMs)
                                }
                            },
                            onDragEnd = {
                                activeDragMode = DragMode.NONE
                                dragTargetCue = null
                                dragFeedbackMessage = ""
                            },
                            onDragCancel = {
                                activeDragMode = DragMode.NONE
                                dragTargetCue = null
                                dragFeedbackMessage = ""
                            }
                        ) { change, _ ->
                            change.consume()
                            val widthPx = size.width.toFloat()
                            val currX = change.position.x
                            val targetMs = ((currX / widthPx) * totalDuration).toLong().coerceIn(0L, totalDuration)
                            val cue = dragTargetCue

                            when (activeDragMode) {
                                DragMode.SCRUB_PLAYHEAD -> {
                                    onSeek(targetMs)
                                }
                                DragMode.TRIM_START -> {
                                    if (cue != null) {
                                        val newStart = targetMs.coerceIn(0L, cue.endTimeMs - 100L)
                                        onSetCueStartTime(cue, newStart)
                                        onSeek(newStart)
                                        dragFeedbackMessage = "Start: ${SubtitleCue.formatTimestampShort(newStart)}"
                                    }
                                }
                                DragMode.TRIM_END -> {
                                    if (cue != null) {
                                        val newEnd = targetMs.coerceIn(cue.startTimeMs + 100L, totalDuration)
                                        onSetCueEndTime(cue, newEnd)
                                        onSeek(newEnd)
                                        dragFeedbackMessage = "End: ${SubtitleCue.formatTimestampShort(newEnd)}"
                                    }
                                }
                                DragMode.MOVE_CUE -> {
                                    if (cue != null) {
                                        val cueDuration = cue.durationMs
                                        val newStart = (targetMs - (cueDuration / 2)).coerceIn(0L, totalDuration - cueDuration)
                                        val shiftDelta = newStart - cue.startTimeMs
                                        onShiftCueTiming(cue, shiftDelta)
                                        onSeek(newStart)
                                        dragFeedbackMessage = "Shift: ${SubtitleCue.formatTimestampShort(newStart)}"
                                    }
                                }
                                DragMode.NONE -> {}
                            }
                        }
                    }
            ) {
                val widthPx = size.width
                val heightPx = size.height

                // A. Time Ruler Ticks & Labels
                val tickCount = (20 * zoomLevel).toInt().coerceAtLeast(10)
                for (i in 0..tickCount) {
                    val x = (i.toFloat() / tickCount) * widthPx
                    val isMajor = i % 2 == 0
                    val tickH = if (isMajor) 8f else 4f

                    drawLine(
                        color = if (isMajor) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.15f),
                        start = Offset(x, 0f),
                        end = Offset(x, tickH),
                        strokeWidth = if (isMajor) 1.5f else 1f
                    )

                    if (isMajor && (x + 30f) <= widthPx) {
                        val tickMs = ((i.toFloat() / tickCount) * totalDuration).toLong()
                        val label = SubtitleCue.formatTimestampShort(tickMs)
                        val layout = textMeasurer.measure(
                            text = label,
                            style = TextStyle(
                                color = ImmersiveTextSecondary,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        drawText(layout, topLeft = Offset(x + 2f, 1f))
                    }
                }

                // B. Procedural Audio Waveform simulation
                val waveTop = 18f
                val waveBars = (widthPx / 4f).toInt()
                val cuesList = subtitleTrack.cues
                var sweepIdx = 0
                for (b in 0 until waveBars) {
                    val barX = b * 4f
                    val msAtBar = ((barX / widthPx) * totalDuration).toLong()
                    while (sweepIdx < cuesList.size && cuesList[sweepIdx].endTimeMs < msAtBar) {
                        sweepIdx++
                    }
                    val hasCue = sweepIdx < cuesList.size && msAtBar >= cuesList[sweepIdx].startTimeMs
                    val baseAmp = if (hasCue) 0.65f else 0.20f
                    val sinFactor = (sin(b * 0.45) * 0.35f + 0.65f).toFloat()
                    val h = (waveHeight * baseAmp * sinFactor).coerceIn(2f, waveHeight)

                    drawLine(
                        color = if (hasCue) AccentCyan.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.08f),
                        start = Offset(barX, waveTop + (waveHeight - h) / 2f),
                        end = Offset(barX, waveTop + (waveHeight + h) / 2f),
                        strokeWidth = 2.5f
                    )
                }

                // C. Render Subtitle Cue Blocks Track
                val cueTrackTop = 36f

                cuesList.forEachIndexed { index, cue ->
                    val startX = (cue.startTimeMs.toFloat() / totalDuration * widthPx).coerceIn(0f, widthPx)
                    val endX = (cue.endTimeMs.toFloat() / totalDuration * widthPx).coerceIn(0f, widthPx)
                    val blockWidth = (endX - startX).coerceAtLeast(10f)

                    val isSelected = selectedCue?.id == cue.id
                    val isActive = cue.isActiveAt(currentPositionMs)
                    val hasOverlap = index > 0 && cuesList[index - 1].endTimeMs > cue.startTimeMs

                    val fillColor = if (isSelected) primaryColor.copy(alpha = 0.70f)
                    else if (hasOverlap) AccentRose.copy(alpha = 0.50f)
                    else if (isActive) accentCyan.copy(alpha = 0.45f)
                    else primaryColor.copy(alpha = 0.25f)

                    val strokeColor = if (isSelected) primaryColor
                    else if (hasOverlap) AccentRose
                    else if (isActive) accentCyan
                    else primaryColor.copy(alpha = 0.45f)

                    // Draw block background
                    drawRect(
                        color = fillColor,
                        topLeft = Offset(startX, cueTrackTop),
                        size = Size(blockWidth, cueTrackHeight)
                    )

                    // Draw block border
                    drawRect(
                        color = strokeColor,
                        topLeft = Offset(startX, cueTrackTop),
                        size = Size(blockWidth, cueTrackHeight),
                        style = Stroke(width = if (isSelected) 2.dp.toPx() else 1.dp.toPx())
                    )

                    // If selected, draw trim handle grips on left and right
                    if (isSelected) {
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(startX, cueTrackTop + 2f),
                            size = Size(3f, cueTrackHeight - 4f)
                        )
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(startX + blockWidth - 3f, cueTrackTop + 2f),
                            size = Size(3f, cueTrackHeight - 4f)
                        )
                    }

                    // Render text snippet inside block
                    if (blockWidth > 18f) {
                        val label = cue.text.replace("\n", " ")
                        val cacheKey = "${cue.id}_${cue.text}_${isSelected}_${isActive}"
                        val layoutResult = textLayoutCache.getOrPut(cacheKey) {
                            textMeasurer.measure(
                                text = label,
                                style = TextStyle(
                                    color = if (isSelected || isActive) Color.White else Color.White.copy(alpha = 0.80f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val textX = (startX + 4f).coerceIn(0f, widthPx - 8f)
                        drawText(
                            textLayoutResult = layoutResult,
                            topLeft = Offset(textX, cueTrackTop + (cueTrackHeight - layoutResult.size.height) / 2f)
                        )
                    }
                }

                // D. Precision Playhead Needle
                val playheadX = (currentPositionMs.toFloat() / totalDuration * widthPx).coerceIn(0f, widthPx)
                drawLine(
                    color = primaryColor,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, heightPx),
                    strokeWidth = 2.5f
                )
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(playheadX - 3f, rulerHeight - 3f),
                    size = Size(6f, 6f)
                )
            }
        }
    }
}
