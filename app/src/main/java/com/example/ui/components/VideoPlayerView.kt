package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.AspectRatioOption
import com.example.model.SubtitleAlignment
import com.example.model.SubtitleCue
import com.example.player.PlayerUiState
import com.example.player.VideoPlayerController
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    playerController: VideoPlayerController,
    playerState: PlayerUiState,
    activeCue: SubtitleCue?,
    aspectRatioOption: AspectRatioOption = AspectRatioOption.ORIGINAL,
    hasVideoLoaded: Boolean = true,
    isMediaLost: Boolean = false,
    onReplaceMediaClick: () -> Unit = {},
    onSubtitlePositionChanged: (Float, Float) -> Unit,
    onSubtitleFontSizeChanged: (Float) -> Unit = {},
    onSubtitleTapped: () -> Unit,
    onDeleteSelectedCue: () -> Unit = {},
    onDuplicateSelectedCue: () -> Unit = {},
    onAlignSelectedCueCenter: () -> Unit = {},
    onToggleFullscreen: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showControls by remember { mutableStateOf(true) }
    var isUserDragging by remember { mutableStateOf(false) }
    var isPinchingText by remember { mutableStateOf(false) }
    var isResizingCorner by remember { mutableStateOf(false) }
    var isSelectedBoxVisible by remember { mutableStateOf(true) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .background(Color.Black)
            .border(1.dp, ImmersiveBorder, RectangleShape)
            .shadow(12.dp, RectangleShape)
            .clickable {
                showControls = !showControls
                isSelectedBoxVisible = true
            }
    ) {
        if (isMediaLost) {
            // "Original media was lost - Replace" State (from screenshot)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = StudioIcons.Warning,
                    contentDescription = null,
                    tint = AccentAmber,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Original media was lost",
                    color = ImmersiveTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onReplaceMediaClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveActionBg),
                    shape = RectangleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Replace", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else if (!hasVideoLoaded) {
            // Empty Video State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(ImmersiveActionBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(StudioIcons.Video, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "No Video Loaded",
                    color = ImmersiveTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onReplaceMediaClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                    shape = RectangleShape,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Add Video", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // 1. ExoPlayer Surface with Aspect Ratio framing
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = playerController.getPlayer()
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.player = playerController.getPlayer()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("video_player_surface")
            )
        }

        // 2. Subtle Cinematic Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.20f)),
                        radius = 1000f
                    )
                )
        )

        // 3. Interactive Subtitle Canvas with Drag-to-Position and Pinch/Handle-to-Resize
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val containerWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            val containerHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

            val sourceAspect = if (playerState.videoHeight > 0) {
                playerState.videoWidth.toFloat() / playerState.videoHeight.toFloat()
            } else {
                16f / 9f
            }

            val targetAspect = aspectRatioOption.ratio ?: sourceAspect
            val containerAspect = containerWidthPx / containerHeightPx

            val videoRenderWidth: Float
            val videoRenderHeight: Float
            val videoOffsetX: Float
            val videoOffsetY: Float

            if (targetAspect > containerAspect) {
                videoRenderWidth = containerWidthPx
                videoRenderHeight = containerWidthPx / targetAspect
                videoOffsetX = 0f
                videoOffsetY = (containerHeightPx - videoRenderHeight) / 2f
            } else {
                videoRenderHeight = containerHeightPx
                videoRenderWidth = containerHeightPx * targetAspect
                videoOffsetX = (containerWidthPx - videoRenderWidth) / 2f
                videoOffsetY = 0f
            }

            var subtitleWidthPx by remember { mutableFloatStateOf(160f) }
            var subtitleHeightPx by remember { mutableFloatStateOf(44f) }

            // Guidance lines when dragging or resizing subtitle
            if ((isUserDragging || isResizingCorner || isPinchingText) && activeCue != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val targetX = videoOffsetX + (activeCue.posX * videoRenderWidth)
                    val targetY = videoOffsetY + (activeCue.posY * videoRenderHeight)
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                    // Safe-area bounding box
                    drawRect(
                        color = AccentCyan.copy(alpha = 0.35f),
                        topLeft = Offset(videoOffsetX + 10f, videoOffsetY + 10f),
                        size = Size((videoRenderWidth - 20f).coerceAtLeast(1f), (videoRenderHeight - 20f).coerceAtLeast(1f)),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f, pathEffect = dashEffect)
                    )

                    // Center Crosshair lines (Snap helpers)
                    val centerX = videoOffsetX + (videoRenderWidth / 2f)
                    val centerY = videoOffsetY + (videoRenderHeight / 2f)

                    drawLine(
                        color = if (kotlin.math.abs(targetX - centerX) < 15f) AccentEmerald else ImmersivePrimary.copy(alpha = 0.4f),
                        start = Offset(centerX, videoOffsetY),
                        end = Offset(centerX, videoOffsetY + videoRenderHeight),
                        strokeWidth = if (kotlin.math.abs(targetX - centerX) < 15f) 2f else 1f,
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = if (kotlin.math.abs(targetY - centerY) < 15f) AccentEmerald else ImmersivePrimary.copy(alpha = 0.4f),
                        start = Offset(videoOffsetX, centerY),
                        end = Offset(videoOffsetX + videoRenderWidth, centerY),
                        strokeWidth = if (kotlin.math.abs(targetY - centerY) < 15f) 2f else 1f,
                        pathEffect = dashEffect
                    )
                }
            }

            if (activeCue != null && activeCue.text.isNotEmpty()) {
                val anchorCenterX = videoOffsetX + (activeCue.posX * videoRenderWidth)
                val anchorCenterY = videoOffsetY + (activeCue.posY * videoRenderHeight)

                val (rawLeftPx, rawTopPx) = when (activeCue.alignment) {
                    SubtitleAlignment.BOTTOM_START -> Pair(anchorCenterX, anchorCenterY - subtitleHeightPx)
                    SubtitleAlignment.BOTTOM_CENTER -> Pair(anchorCenterX - (subtitleWidthPx / 2f), anchorCenterY - subtitleHeightPx)
                    SubtitleAlignment.BOTTOM_END -> Pair(anchorCenterX - subtitleWidthPx, anchorCenterY - subtitleHeightPx)
                    SubtitleAlignment.TOP_START -> Pair(anchorCenterX, anchorCenterY)
                    SubtitleAlignment.TOP_CENTER -> Pair(anchorCenterX - (subtitleWidthPx / 2f), anchorCenterY)
                    SubtitleAlignment.TOP_END -> Pair(anchorCenterX - subtitleWidthPx, anchorCenterY)
                    SubtitleAlignment.CENTER_START -> Pair(anchorCenterX, anchorCenterY - (subtitleHeightPx / 2f))
                    SubtitleAlignment.CENTER_END -> Pair(anchorCenterX - subtitleWidthPx, anchorCenterY - (subtitleHeightPx / 2f))
                    SubtitleAlignment.CENTER, SubtitleAlignment.CUSTOM -> Pair(anchorCenterX - (subtitleWidthPx / 2f), anchorCenterY - (subtitleHeightPx / 2f))
                }

                val leftOffsetPx = rawLeftPx.coerceIn(
                    videoOffsetX + 4f,
                    (videoOffsetX + videoRenderWidth - subtitleWidthPx - 4f).coerceAtLeast(videoOffsetX + 4f)
                )
                val topOffsetPx = rawTopPx.coerceIn(
                    videoOffsetY + 4f,
                    (videoOffsetY + videoRenderHeight - subtitleHeightPx - 4f).coerceAtLeast(videoOffsetY + 4f)
                )

                val style = activeCue.style
                val hasDarkBox = style.hasBackground
                val textColor = Color(style.textColorArgb)
                val bgColor = if (hasDarkBox) Color(style.backgroundColorArgb) else Color.Transparent

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(leftOffsetPx.roundToInt(), topOffsetPx.roundToInt())
                        }
                        .onGloballyPositioned { layoutCoordinates ->
                            val size = layoutCoordinates.size
                            if (size.width > 0 && size.height > 0) {
                                subtitleWidthPx = size.width.toFloat()
                                subtitleHeightPx = size.height.toFloat()
                            }
                        }
                        .wrapContentSize()
                        // 1. Pinch-to-zoom gesture on the subtitle text
                        .pointerInput(activeCue.id) {
                            detectTransformGestures { _, _, zoom, _ ->
                                if (zoom != 1f) {
                                    isPinchingText = true
                                    val newSize = (style.fontSizeSp * zoom).coerceIn(10f, 60f)
                                    onSubtitleFontSizeChanged(newSize)
                                }
                            }
                        }
                        // 2. Drag-to-move gesture anywhere on subtitle box
                        .pointerInput(activeCue.id, videoRenderWidth, videoRenderHeight) {
                            detectDragGestures(
                                onDragStart = { isUserDragging = true; isSelectedBoxVisible = true },
                                onDragEnd = { isUserDragging = false; isPinchingText = false },
                                onDragCancel = { isUserDragging = false; isPinchingText = false }
                            ) { change, dragAmount ->
                                change.consume()
                                val deltaX = dragAmount.x / videoRenderWidth
                                val deltaY = dragAmount.y / videoRenderHeight
                                var newX = (activeCue.posX + deltaX).coerceIn(0.05f, 0.95f)
                                var newY = (activeCue.posY + deltaY).coerceIn(0.05f, 0.95f)

                                // Snap to center X if within tolerance
                                if (kotlin.math.abs(newX - 0.50f) < 0.03f) {
                                    newX = 0.50f
                                }

                                onSubtitlePositionChanged(newX, newY)
                            }
                        }
                        .clickable {
                            isSelectedBoxVisible = true
                            onSubtitleTapped()
                        }
                        .clip(RectangleShape)
                        .background(bgColor)
                        .then(
                            if (isSelectedBoxVisible || isUserDragging || isResizingCorner) {
                                Modifier.border(1.5.dp, AccentCyan, RectangleShape)
                            } else if (hasDarkBox) {
                                Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), RectangleShape)
                            } else {
                                Modifier
                            }
                        )
                        .padding(
                            horizontal = (style.paddingHorizontalDp + 4).dp,
                            vertical = (style.paddingVerticalDp + 4).dp
                        )
                        .testTag("subtitle_overlay_draggable")
                ) {
                    Text(
                        text = activeCue.text,
                        color = textColor,
                        fontSize = (style.fontSizeSp * 0.95f).sp,
                        fontWeight = if (style.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (style.isItalic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = if (style.isUnderline) TextDecoration.Underline else TextDecoration.None,
                        style = TextStyle(
                            shadow = if (!hasDarkBox) Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 4f) else null
                        ),
                        textAlign = when (activeCue.alignment) {
                            SubtitleAlignment.BOTTOM_START, SubtitleAlignment.TOP_START, SubtitleAlignment.CENTER_START -> TextAlign.Start
                            SubtitleAlignment.BOTTOM_END, SubtitleAlignment.TOP_END, SubtitleAlignment.CENTER_END -> TextAlign.End
                            else -> TextAlign.Center
                        }
                    )

                    // On-Screen Bounding Box Controls (Resize Corner Handle & Size Badge)
                    if (isSelectedBoxVisible || isUserDragging || isResizingCorner) {
                        // Corner Resize Handle (Bottom-End corner)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 6.dp, y = 6.dp)
                                .size(18.dp)
                                .background(AccentCyan, RectangleShape)
                                .pointerInput(activeCue.id) {
                                    detectDragGestures(
                                        onDragStart = { isResizingCorner = true },
                                        onDragEnd = { isResizingCorner = false },
                                        onDragCancel = { isResizingCorner = false }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        val delta = (dragAmount.x + dragAmount.y) / 4f
                                        val newSize = (style.fontSizeSp + delta).coerceIn(10f, 60f)
                                        onSubtitleFontSizeChanged(newSize)
                                    }
                                }
                                .testTag("subtitle_corner_resize_handle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = StudioIcons.Crop,
                                contentDescription = "Resize Text Size",
                                tint = Color.Black,
                                modifier = Modifier.size(10.dp)
                            )
                        }

                        // Top-End Quick Edit / Delete button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                                .size(18.dp)
                                .background(ImmersivePrimary, RectangleShape)
                                .clickable { onSubtitleTapped() }
                                .testTag("subtitle_corner_edit_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = StudioIcons.Edit,
                                contentDescription = "Edit Subtitle Text",
                                tint = Color.Black,
                                modifier = Modifier.size(10.dp)
                            )
                        }

                        // Top-Start Center-X snap button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = (-6).dp, y = (-6).dp)
                                .size(18.dp)
                                .background(ImmersiveActionBg, RectangleShape)
                                .border(1.dp, ImmersiveBorder, RectangleShape)
                                .clickable { onAlignSelectedCueCenter() }
                                .testTag("subtitle_corner_center_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = StudioIcons.Placement,
                                contentDescription = "Center Subtitle",
                                tint = AccentCyan,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Viewport Top Tag & Info
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Surface(
                shape = RectangleShape,
                color = Color.Black.copy(alpha = 0.70f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(5.dp).background(AccentEmerald, RectangleShape))
                    Text(
                        text = if (playerState.videoWidth > 0) "${playerState.videoWidth}x${playerState.videoHeight}" else aspectRatioOption.shortLabel,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 5. Fullscreen Expand Icon (Bottom-Right corner of player, as in screenshot)
        if (onToggleFullscreen != null) {
            Surface(
                shape = RectangleShape,
                color = Color.Black.copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = onToggleFullscreen,
                    modifier = Modifier.size(34.dp).testTag("fullscreen_expand_btn")
                ) {
                    Icon(
                        imageVector = StudioIcons.Fullscreen,
                        contentDescription = "Expand Fullscreen",
                        tint = ImmersivePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 6. Fast Transport Bar (Play/Pause, Step Frames, Speed, Mute)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        ) {
            Surface(
                shape = RectangleShape,
                color = Color.Black.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                shadowElevation = 8.dp,
                modifier = Modifier.testTag("floating_playback_controls")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    // Speed Selector
                    Surface(
                        shape = RectangleShape,
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.clickable {
                            val nextIdx = (speeds.indexOf(playerState.playbackSpeed) + 1) % speeds.size
                            playerController.setPlaybackSpeed(speeds[nextIdx])
                        }
                    ) {
                        Text(
                            text = "${playerState.playbackSpeed}x",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    // Mute / Unmute
                    IconButton(
                        onClick = { playerController.toggleMute() },
                        modifier = Modifier.size(26.dp).testTag("mute_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (playerState.isMuted) StudioIcons.VolumeOff else StudioIcons.VolumeUp,
                            contentDescription = if (playerState.isMuted) "Unmute" else "Mute",
                            tint = if (playerState.isMuted) AccentRose else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Rewind 1s / step back
                    IconButton(
                        onClick = { playerController.seekBy(-1000L) },
                        modifier = Modifier.size(26.dp).testTag("rewind_1s_button")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Rewind,
                            contentDescription = "Rewind 1 Second",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Primary Play / Pause
                    Surface(
                        shape = RectangleShape,
                        color = ImmersivePrimary,
                        modifier = Modifier.size(30.dp)
                    ) {
                        IconButton(
                            onClick = { playerController.togglePlayPause() },
                            modifier = Modifier.fillMaxSize().testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) StudioIcons.Pause else StudioIcons.Play,
                                contentDescription = if (playerState.isPlaying) "Pause Video" else "Play Video",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Forward 1s / step forward
                    IconButton(
                        onClick = { playerController.seekBy(1000L) },
                        modifier = Modifier.size(26.dp).testTag("forward_1s_button")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Forward,
                            contentDescription = "Forward 1 Second",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Loop Toggle
                    IconButton(
                        onClick = { playerController.toggleLooping() },
                        modifier = Modifier.size(26.dp).testTag("loop_toggle_btn")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Refresh,
                            contentDescription = "Toggle Loop",
                            tint = if (playerState.isLooping) ImmersivePrimary else Color.White.copy(alpha = 0.40f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
