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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import com.example.model.SubtitleCue
import com.example.model.SubtitleTrack
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceCard
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
    onSeek: (Long) -> Unit,
    onSelectCue: (SubtitleCue) -> Unit,
    onAddCueAtCurrentPosition: () -> Unit,
    onSplitCueAtCurrentPosition: () -> Unit,
    onDeleteSelectedCue: () -> Unit,
    onNudgeTiming: (Long, Long) -> Unit,
    onSetCueStartTime: (SubtitleCue, Long) -> Unit,
    onSetCueEndTime: (SubtitleCue, Long) -> Unit,
    onShiftCueTiming: (SubtitleCue, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDuration = if (durationMs > 0) durationMs else 1L
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = ImmersivePrimary
    val accentCyan = AccentCyan

    var zoomLevel by remember { mutableFloatStateOf(1.0f) } // 1.0f (fit), 2.0f, 4.0f, 8.0f
    val zoomOptions = listOf(1.0f, 2.0f, 4.0f, 8.0f)
    val scrollState = rememberScrollState()
    var isSnapToGridEnabled by remember { mutableStateOf(true) }

    var activeDragMode by remember { mutableStateOf(DragMode.NONE) }
    var dragTargetCue by remember { mutableStateOf<SubtitleCue?>(null) }
    var dragFeedbackMessage by remember { mutableStateOf("") }

    // Auto-scroll timeline to keep playhead in view when zoomed
    LaunchedEffect(currentPositionMs, zoomLevel) {
        if (zoomLevel > 1.0f && totalDuration > 0 && activeDragMode == DragMode.NONE) {
            val progress = currentPositionMs.toFloat() / totalDuration
            val targetScroll = (progress * scrollState.maxValue).toInt()
            scrollState.scrollTo((targetScroll - 200).coerceIn(0, scrollState.maxValue))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ImmersiveSurface)
            .border(1.dp, ImmersiveBorder, RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Header: Track Tag, Zoom Controls, and Timecode Display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(AccentCyan, CircleShape)
                )
                Text(
                    text = "TIMELINE • ${subtitleTrack.format.extension.uppercase()} (${subtitleTrack.cues.size} CUES)",
                    color = ImmersivePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Controls Strip (Snap & Zoom)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Magnet Snap Toggle
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSnapToGridEnabled) AccentCyan.copy(alpha = 0.2f) else ImmersiveActionBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSnapToGridEnabled) AccentCyan else ImmersiveBorder),
                    modifier = Modifier.clickable { isSnapToGridEnabled = !isSnapToGridEnabled }
                ) {
                    Text(
                        text = if (isSnapToGridEnabled) "Snap On" else "Snap Off",
                        color = if (isSnapToGridEnabled) AccentCyan else ImmersiveTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                zoomOptions.forEach { zoom ->
                    val isSelected = zoomLevel == zoom
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) ImmersivePrimary else ImmersiveActionBg,
                        modifier = Modifier.clickable { zoomLevel = zoom }
                    ) {
                        Text(
                            text = if (zoom == 1.0f) "Fit" else "${zoom.toInt()}x",
                            color = if (isSelected) Color.Black else ImmersiveTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Time Counter Row & Live Drag Feedback Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = SubtitleCue.formatTimestamp(currentPositionMs),
                color = AccentCyan,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            if (activeDragMode != DragMode.NONE && dragFeedbackMessage.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentCyan.copy(alpha = 0.20f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan)
                ) {
                    Text(
                        text = dragFeedbackMessage,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = "Total: ${SubtitleCue.formatTimestamp(durationMs)}",
                    color = ImmersiveTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 2. Interactive Scrollable Multi-Track Waveform & Cue Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            val baseWidth = 340.dp
            val canvasWidth = (baseWidth * zoomLevel).coerceAtLeast(baseWidth)

            var dragStartX by remember { mutableFloatStateOf(0f) }
            var dragInitialStartMs by remember { mutableStateOf(0L) }
            var dragInitialEndMs by remember { mutableStateOf(0L) }

            // Cache text layout measurements to avoid expensive CPU layout calls during 25fps draw ticks
            val textLayoutCache = remember(subtitleTrack.cues, selectedCue?.id) {
                mutableMapOf<String, TextLayoutResult>()
            }

            Canvas(
                modifier = Modifier
                    .width(canvasWidth)
                    .height(84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ImmersiveBg)
                    .border(1.dp, ImmersiveBorder, RoundedCornerShape(12.dp))
                    .pointerInput(totalDuration, subtitleTrack.cues, selectedCue) {
                        detectTapGestures { offset ->
                            val widthPx = size.width.toFloat()
                            val ratio = (offset.x / widthPx).coerceIn(0f, 1f)
                            val tappedMs = (ratio * totalDuration).toLong()

                            // Check if tapped on a cue
                            val clickedCue = subtitleTrack.cues.firstOrNull { cue ->
                                val sX = (cue.startTimeMs.toFloat() / totalDuration * widthPx)
                                val eX = (cue.endTimeMs.toFloat() / totalDuration * widthPx)
                                val bW = (eX - sX).coerceAtLeast(14f)
                                offset.x >= sX && offset.x <= (sX + bW)
                            }

                            if (clickedCue != null) {
                                onSelectCue(clickedCue)
                            } else {
                                onSeek(tappedMs)
                            }
                        }
                    }
                    .pointerInput(totalDuration, subtitleTrack.cues, selectedCue, isSnapToGridEnabled) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragStartX = offset.x
                                val widthPx = size.width.toFloat()

                                // Check if user touched edge handles of the selected cue or cue body
                                if (selectedCue != null) {
                                    val sX = (selectedCue.startTimeMs.toFloat() / totalDuration * widthPx)
                                    val eX = (selectedCue.endTimeMs.toFloat() / totalDuration * widthPx)
                                    val bW = (eX - sX).coerceAtLeast(14f)

                                    if (offset.x in (sX - 12f)..(sX + 14f)) {
                                        activeDragMode = DragMode.TRIM_START
                                        dragTargetCue = selectedCue
                                        dragInitialStartMs = selectedCue.startTimeMs
                                        dragInitialEndMs = selectedCue.endTimeMs
                                        dragFeedbackMessage = "Trim Start: ${SubtitleCue.formatTimestampShort(selectedCue.startTimeMs)}"
                                        return@detectDragGestures
                                    } else if (offset.x in (sX + bW - 14f)..(sX + bW + 12f)) {
                                        activeDragMode = DragMode.TRIM_END
                                        dragTargetCue = selectedCue
                                        dragInitialStartMs = selectedCue.startTimeMs
                                        dragInitialEndMs = selectedCue.endTimeMs
                                        dragFeedbackMessage = "Trim End: ${SubtitleCue.formatTimestampShort(selectedCue.endTimeMs)}"
                                        return@detectDragGestures
                                    } else if (offset.x in sX..(sX + bW)) {
                                        activeDragMode = DragMode.MOVE_CUE
                                        dragTargetCue = selectedCue
                                        dragInitialStartMs = selectedCue.startTimeMs
                                        dragInitialEndMs = selectedCue.endTimeMs
                                        dragFeedbackMessage = "Move Cue #${selectedCue.id}"
                                        return@detectDragGestures
                                    }
                                }

                                // Fallback: scrub playhead
                                activeDragMode = DragMode.SCRUB_PLAYHEAD
                                val ratio = (offset.x / widthPx).coerceIn(0f, 1f)
                                val targetSeek = (ratio * totalDuration).toLong()
                                dragFeedbackMessage = "Playhead: ${SubtitleCue.formatTimestampShort(targetSeek)}"
                                onSeek(targetSeek)
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
                        ) { change, dragAmount ->
                            change.consume()
                            val widthPx = size.width.toFloat()
                            val deltaPx = change.position.x - dragStartX
                            val deltaMs = ((deltaPx / widthPx) * totalDuration).toLong()

                            val cue = dragTargetCue
                            val snapThresholdMs = 120L

                            when (activeDragMode) {
                                DragMode.MOVE_CUE -> {
                                    if (cue != null) {
                                        val shiftMs = ((dragAmount.x / widthPx) * totalDuration).toLong()
                                        if (shiftMs != 0L) {
                                            onShiftCueTiming(cue, shiftMs)
                                            dragFeedbackMessage = "Shift: ${SubtitleCue.formatTimestampShort(cue.startTimeMs)}"
                                        }
                                    }
                                }
                                DragMode.TRIM_START -> {
                                    if (cue != null) {
                                        var newStart = (dragInitialStartMs + deltaMs).coerceIn(0L, cue.endTimeMs - 100L)
                                        if (isSnapToGridEnabled) {
                                            if (abs(newStart - currentPositionMs) < snapThresholdMs) {
                                                newStart = currentPositionMs
                                            }
                                        }
                                        onSetCueStartTime(cue, newStart)
                                        dragFeedbackMessage = "Start: ${SubtitleCue.formatTimestampShort(newStart)}"
                                    }
                                }
                                DragMode.TRIM_END -> {
                                    if (cue != null) {
                                        var newEnd = (dragInitialEndMs + deltaMs).coerceIn(cue.startTimeMs + 100L, totalDuration)
                                        if (isSnapToGridEnabled) {
                                            if (abs(newEnd - currentPositionMs) < snapThresholdMs) {
                                                newEnd = currentPositionMs
                                            }
                                        }
                                        onSetCueEndTime(cue, newEnd)
                                        dragFeedbackMessage = "End: ${SubtitleCue.formatTimestampShort(newEnd)}"
                                    }
                                }
                                DragMode.SCRUB_PLAYHEAD -> {
                                    val ratio = (change.position.x / widthPx).coerceIn(0f, 1f)
                                    var scrubMs = (ratio * totalDuration).toLong()
                                    if (isSnapToGridEnabled) {
                                        subtitleTrack.cues.forEach { c ->
                                            if (abs(scrubMs - c.startTimeMs) < snapThresholdMs) scrubMs = c.startTimeMs
                                            else if (abs(scrubMs - c.endTimeMs) < snapThresholdMs) scrubMs = c.endTimeMs
                                        }
                                    }
                                    dragFeedbackMessage = "Playhead: ${SubtitleCue.formatTimestampShort(scrubMs)}"
                                    onSeek(scrubMs)
                                }
                                DragMode.NONE -> {}
                            }
                        }
                    }
                    .testTag("timeline_scrubber_track")
            ) {
                val widthPx = size.width
                val heightPx = size.height

                // A. Top Timecode Ruler
                val rulerHeight = 18f
                drawLine(
                    color = ImmersiveBorder,
                    start = Offset(0f, rulerHeight),
                    end = Offset(widthPx, rulerHeight),
                    strokeWidth = 1f
                )

                val tickCount = (16 * zoomLevel).toInt().coerceAtLeast(8)
                for (i in 0..tickCount) {
                    val x = (i.toFloat() / tickCount) * widthPx
                    val isMajor = i % 2 == 0
                    val tickH = if (isMajor) 10f else 5f

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

                // B. Procedural Audio Speech Waveform Simulation Track (18px to 42px height)
                val waveTop = 22f
                val waveHeight = 20f
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

                // C. Render Subtitle Cue Blocks Track (46px to 80px height)
                val cueTrackTop = 46f
                val cueTrackHeight = 32f

                cuesList.forEachIndexed { index, cue ->
                    val startX = (cue.startTimeMs.toFloat() / totalDuration * widthPx).coerceIn(0f, widthPx)
                    val endX = (cue.endTimeMs.toFloat() / totalDuration * widthPx).coerceIn(0f, widthPx)
                    val blockWidth = (endX - startX).coerceAtLeast(12f)

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

                    val corner = CornerRadius(6f, 6f)

                    // Draw block background
                    drawRoundRect(
                        color = fillColor,
                        topLeft = Offset(startX, cueTrackTop),
                        size = Size(blockWidth, cueTrackHeight),
                        cornerRadius = corner
                    )

                    // Draw block border
                    drawRoundRect(
                        color = strokeColor,
                        topLeft = Offset(startX, cueTrackTop),
                        size = Size(blockWidth, cueTrackHeight),
                        cornerRadius = corner,
                        style = Stroke(width = if (isSelected) 2.5f else 1.2f)
                    )

                    // If selected, draw trim handle grips on left and right
                    if (isSelected) {
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(startX, cueTrackTop + 4f),
                            size = Size(4f, cueTrackHeight - 8f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(startX + blockWidth - 4f, cueTrackTop + 4f),
                            size = Size(4f, cueTrackHeight - 8f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }

                    // Render text snippet inside block using cache
                    if (blockWidth > 20f) {
                        val label = cue.text.replace("\n", " ")
                        val cacheKey = "${cue.id}_${cue.text}_${isSelected}_${isActive}"
                        val layoutResult = textLayoutCache.getOrPut(cacheKey) {
                            textMeasurer.measure(
                                text = label,
                                style = TextStyle(
                                    color = if (isSelected || isActive) Color.White else Color.White.copy(alpha = 0.80f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val textX = (startX + 6f).coerceIn(0f, widthPx - 10f)
                        drawText(
                            textLayoutResult = layoutResult,
                            topLeft = Offset(textX, cueTrackTop + (cueTrackHeight - layoutResult.size.height) / 2f)
                        )
                    }
                }

                // D. Glowing Precision Playhead Needle
                val playheadX = (currentPositionMs.toFloat() / totalDuration * widthPx).coerceIn(0f, widthPx)
                drawLine(
                    color = primaryColor,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, heightPx),
                    strokeWidth = 3f
                )
                drawCircle(
                    color = primaryColor,
                    radius = 6f,
                    center = Offset(playheadX, rulerHeight)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = Offset(playheadX, rulerHeight)
                )
            }
        }

        // 3. Selected Cue Quick Timeline Action Bar
        if (selectedCue != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ImmersiveSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cue Info & Playhead Sync
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val cueIdx = subtitleTrack.cues.indexOfFirst { it.id == selectedCue.id }
                        Column {
                            Text(
                                text = "CUE #${if (cueIdx >= 0) cueIdx + 1 else 1} TIMING",
                                color = ImmersivePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${SubtitleCue.formatTimestampShort(selectedCue.startTimeMs)} ➔ ${SubtitleCue.formatTimestampShort(selectedCue.endTimeMs)} (${selectedCue.durationMs}ms)",
                                color = ImmersiveTextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Align with Playhead Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ImmersiveActionBg,
                                modifier = Modifier
                                    .clickable { onSetCueStartTime(selectedCue, currentPositionMs) }
                                    .testTag("set_start_now_btn")
                            ) {
                                Text(
                                    text = "[ Start=Now",
                                    color = AccentCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ImmersiveActionBg,
                                modifier = Modifier
                                    .clickable { onSetCueEndTime(selectedCue, currentPositionMs) }
                                    .testTag("set_end_now_btn")
                                    
                            ) {
                                Text(
                                    text = "End=Now ]",
                                    color = AccentCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Operations Strip: Split, Add, Delete, Micro-Nudge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Split
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ImmersiveActionBg,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSplitCueAtCurrentPosition() }
                                .testTag("split_cue_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp)) {
                                Text("Split", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Add
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ImmersiveActionBg,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onAddCueAtCurrentPosition() }
                                .testTag("timeline_add_cue_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp)) {
                                Text("+ Cue", color = ImmersivePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // -0.1s Nudge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ImmersiveActionBg,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNudgeTiming(-100L, -100L) }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp)) {
                                Text("-0.1s", color = ImmersiveTextSecondary, fontSize = 10.sp)
                            }
                        }

                        // +0.1s Nudge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ImmersiveActionBg,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNudgeTiming(100L, 100L) }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp)) {
                                Text("+0.1s", color = ImmersiveTextSecondary, fontSize = 10.sp)
                            }
                        }

                        // Delete
                        IconButton(
                            onClick = onDeleteSelectedCue,
                            modifier = Modifier.size(28.dp).testTag("delete_cue_button")
                        ) {
                            Icon(
                                imageVector = StudioIcons.Delete,
                                contentDescription = "Delete Cue",
                                tint = AccentRose,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
