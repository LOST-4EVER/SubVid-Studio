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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
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
import com.example.model.SubtitleAlignment
import com.example.model.SubtitleCue
import com.example.player.PlayerUiState
import com.example.player.VideoPlayerController
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    playerController: VideoPlayerController,
    playerState: PlayerUiState,
    activeCue: SubtitleCue?,
    isDraggingSubtitle: Boolean,
    onSubtitlePositionChanged: (Float, Float) -> Unit,
    onSubtitleTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showControls by remember { mutableStateOf(true) }
    var isUserDragging by remember { mutableStateOf(false) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black)
            .border(1.dp, ImmersiveBorder, RoundedCornerShape(20.dp))
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clickable { showControls = !showControls }
    ) {
        // 1. ExoPlayer Video Canvas
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

        // 2. Subtle Cinematic Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                        radius = 1200f
                    )
                )
        )

        // 3. Subtitle Overlay with Frame-Accurate Aspect Ratio Placement
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val containerWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            val containerHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

            // Calculate actual video frame dimensions within RESIZE_MODE_FIT
            val videoAspect = if (playerState.videoHeight > 0) {
                playerState.videoWidth.toFloat() / playerState.videoHeight.toFloat()
            } else {
                16f / 9f
            }
            val containerAspect = containerWidthPx / containerHeightPx

            val videoRenderWidth: Float
            val videoRenderHeight: Float
            val videoOffsetX: Float
            val videoOffsetY: Float

            if (videoAspect > containerAspect) {
                videoRenderWidth = containerWidthPx
                videoRenderHeight = containerWidthPx / videoAspect
                videoOffsetX = 0f
                videoOffsetY = (containerHeightPx - videoRenderHeight) / 2f
            } else {
                videoRenderHeight = containerHeightPx
                videoRenderWidth = containerHeightPx * videoAspect
                videoOffsetX = (containerWidthPx - videoRenderWidth) / 2f
                videoOffsetY = 0f
            }

            var subtitleWidthPx by remember { mutableFloatStateOf(160f) }
            var subtitleHeightPx by remember { mutableFloatStateOf(44f) }

            // Visual guide lines when dragging subtitle
            if (isUserDragging && activeCue != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val targetX = videoOffsetX + (activeCue.posX * videoRenderWidth)
                    val targetY = videoOffsetY + (activeCue.posY * videoRenderHeight)
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    // Video bounds safe-area rectangle
                    drawRect(
                        color = AccentCyan.copy(alpha = 0.35f),
                        topLeft = Offset(videoOffsetX, videoOffsetY),
                        size = androidx.compose.ui.geometry.Size(videoRenderWidth, videoRenderHeight),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f, pathEffect = dashEffect)
                    )

                    // Crosshair guides
                    drawLine(
                        color = ImmersivePrimary.copy(alpha = 0.5f),
                        start = Offset(videoOffsetX, targetY),
                        end = Offset(videoOffsetX + videoRenderWidth, targetY),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = ImmersivePrimary.copy(alpha = 0.5f),
                        start = Offset(targetX, videoOffsetY),
                        end = Offset(targetX, videoOffsetY + videoRenderHeight),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )
                }
            }

            if (activeCue != null) {
                // Calculate anchor coordinates relative to active video frame
                val anchorCenterX = videoOffsetX + (activeCue.posX * videoRenderWidth)
                val anchorCenterY = videoOffsetY + (activeCue.posY * videoRenderHeight)

                // Anchor positioning based on alignment
                val (rawLeftPx, rawTopPx) = when (activeCue.alignment) {
                    SubtitleAlignment.BOTTOM_START -> {
                        Pair(anchorCenterX, anchorCenterY - subtitleHeightPx)
                    }
                    SubtitleAlignment.BOTTOM_CENTER -> {
                        Pair(anchorCenterX - (subtitleWidthPx / 2f), anchorCenterY - subtitleHeightPx)
                    }
                    SubtitleAlignment.BOTTOM_END -> {
                        Pair(anchorCenterX - subtitleWidthPx, anchorCenterY - subtitleHeightPx)
                    }
                    SubtitleAlignment.TOP_START -> {
                        Pair(anchorCenterX, anchorCenterY)
                    }
                    SubtitleAlignment.TOP_CENTER -> {
                        Pair(anchorCenterX - (subtitleWidthPx / 2f), anchorCenterY)
                    }
                    SubtitleAlignment.TOP_END -> {
                        Pair(anchorCenterX - subtitleWidthPx, anchorCenterY)
                    }
                    SubtitleAlignment.CENTER_START -> {
                        Pair(anchorCenterX, anchorCenterY - (subtitleHeightPx / 2f))
                    }
                    SubtitleAlignment.CENTER_END -> {
                        Pair(anchorCenterX - subtitleWidthPx, anchorCenterY - (subtitleHeightPx / 2f))
                    }
                    SubtitleAlignment.CENTER, SubtitleAlignment.CUSTOM -> {
                        Pair(anchorCenterX - (subtitleWidthPx / 2f), anchorCenterY - (subtitleHeightPx / 2f))
                    }
                }

                val leftOffsetPx = rawLeftPx.coerceIn(
                    videoOffsetX + 4f,
                    (videoOffsetX + videoRenderWidth - subtitleWidthPx - 4f).coerceAtLeast(videoOffsetX + 4f)
                )
                val topOffsetPx = rawTopPx.coerceIn(
                    videoOffsetY + 4f,
                    (videoOffsetY + videoRenderHeight - subtitleHeightPx - 4f).coerceAtLeast(videoOffsetY + 4f)
                )

                // Draggable Subtitle Element
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
                        .pointerInput(activeCue.id, videoRenderWidth, videoRenderHeight) {
                            detectDragGestures(
                                onDragStart = { isUserDragging = true },
                                onDragEnd = { isUserDragging = false },
                                onDragCancel = { isUserDragging = false }
                            ) { change, dragAmount ->
                                change.consume()
                                val deltaX = dragAmount.x / videoRenderWidth
                                val deltaY = dragAmount.y / videoRenderHeight
                                val newX = (activeCue.posX + deltaX).coerceIn(0.02f, 0.98f)
                                val newY = (activeCue.posY + deltaY).coerceIn(0.02f, 0.98f)
                                onSubtitlePositionChanged(newX, newY)
                            }
                        }
                        .clickable { onSubtitleTapped() }
                        .testTag("subtitle_overlay_draggable")
                ) {
                    val style = activeCue.style
                    val textColor = Color(style.textColorArgb)
                    val bgColor = Color(style.backgroundColorArgb)
                    val strokeColor = Color(style.strokeColorArgb)

                    Box(
                        modifier = Modifier
                            .background(bgColor, RoundedCornerShape(style.cornerRadiusDp.dp))
                            .border(
                                width = if (isUserDragging) 2.dp else 1.2.dp,
                                color = if (isUserDragging) AccentCyan else ImmersivePrimary.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(style.cornerRadiusDp.dp)
                            )
                            .padding(
                                horizontal = style.paddingHorizontalDp.dp,
                                vertical = style.paddingVerticalDp.dp
                            )
                    ) {
                        Text(
                            text = activeCue.text,
                            color = textColor,
                            fontSize = style.fontSizeSp.sp,
                            fontWeight = if (style.isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (style.isItalic) FontStyle.Italic else FontStyle.Normal,
                            textDecoration = if (style.isUnderline) TextDecoration.Underline else TextDecoration.None,
                            textAlign = when (activeCue.alignment) {
                                SubtitleAlignment.BOTTOM_START, SubtitleAlignment.TOP_START, SubtitleAlignment.CENTER_START -> TextAlign.Start
                                SubtitleAlignment.BOTTOM_END, SubtitleAlignment.TOP_END, SubtitleAlignment.CENTER_END -> TextAlign.End
                                else -> TextAlign.Center
                            },
                            modifier = Modifier.padding(2.dp)
                        )

                        // Anchor Dots for Visual Precision Feedback
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .offset((-3).dp, (-3).dp)
                                .background(ImmersivePrimary, CircleShape)
                                .align(Alignment.TopStart)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .offset(3.dp, 3.dp)
                                .background(ImmersivePrimary, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }

        // 4. Video Resolution & Timecode Badge (Top Header inside player)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.65f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(AccentEmerald, CircleShape))
                    Text(
                        text = "${playerState.videoWidth}x${playerState.videoHeight}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 5. Floating Glass Playback Controller Pill
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                shadowElevation = 12.dp,
                modifier = Modifier.testTag("floating_playback_controls")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // Speed Selector Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.clickable {
                            val nextIdx = (speeds.indexOf(playerState.playbackSpeed) + 1) % speeds.size
                            playerController.setPlaybackSpeed(speeds[nextIdx])
                        }
                    ) {
                        Text(
                            text = "${playerState.playbackSpeed}x",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // Mute / Unmute Toggle
                    IconButton(
                        onClick = { playerController.toggleMute() },
                        modifier = Modifier.size(30.dp).testTag("mute_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (playerState.isMuted) StudioIcons.VolumeOff else StudioIcons.VolumeUp,
                            contentDescription = if (playerState.isMuted) "Unmute" else "Mute",
                            tint = if (playerState.isMuted) AccentRose else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Rewind 5s
                    IconButton(
                        onClick = { playerController.seekBy(-5000L) },
                        modifier = Modifier.size(32.dp).testTag("rewind_5s_button")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Rewind,
                            contentDescription = "Rewind 5 Seconds",
                            tint = Color.White.copy(alpha = 0.90f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Previous Frame (33ms)
                    IconButton(
                        onClick = { playerController.stepFrame(-1) },
                        modifier = Modifier.size(28.dp).testTag("prev_frame_button")
                    ) {
                        Text(
                            text = "-1f",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Main Play / Pause Circle
                    Surface(
                        shape = CircleShape,
                        color = ImmersivePrimary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        IconButton(
                            onClick = { playerController.togglePlayPause() },
                            modifier = Modifier.fillMaxSize().testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) StudioIcons.Pause else StudioIcons.Play,
                                contentDescription = if (playerState.isPlaying) "Pause Video" else "Play Video",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Next Frame (33ms)
                    IconButton(
                        onClick = { playerController.stepFrame(1) },
                        modifier = Modifier.size(28.dp).testTag("next_frame_button")
                    ) {
                        Text(
                            text = "+1f",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Fast Forward 5s
                    IconButton(
                        onClick = { playerController.seekBy(5000L) },
                        modifier = Modifier.size(32.dp).testTag("forward_5s_button")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Forward,
                            contentDescription = "Forward 5 Seconds",
                            tint = Color.White.copy(alpha = 0.90f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Loop Toggle
                    IconButton(
                        onClick = { playerController.toggleLooping() },
                        modifier = Modifier.size(30.dp).testTag("loop_toggle_btn")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Refresh,
                            contentDescription = "Toggle Loop",
                            tint = if (playerState.isLooping) ImmersivePrimary else Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
