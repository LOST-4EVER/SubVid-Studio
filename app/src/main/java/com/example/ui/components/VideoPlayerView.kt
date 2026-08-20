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
import com.example.model.SubtitleAlignment
import com.example.model.SubtitleCue
import com.example.player.PlayerUiState
import com.example.player.VideoPlayerController
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
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
    modifier: Modifier = Modifier,
    onToggleFullscreen: (() -> Unit)? = null
) {
    var showControls by remember { mutableStateOf(true) }
    var isUserDragging by remember { mutableStateOf(false) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(1.dp, ImmersiveBorder, RoundedCornerShape(16.dp))
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clickable { showControls = !showControls }
    ) {
        // 1. ExoPlayer Surface
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
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f)),
                        radius = 1000f
                    )
                )
        )

        // 3. Subtitle Overlay with Frame-Accurate Aspect Ratio Placement
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val containerWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            val containerHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

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
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                    // Video bounds safe-area rectangle
                    drawRect(
                        color = AccentCyan.copy(alpha = 0.4f),
                        topLeft = Offset(videoOffsetX, videoOffsetY),
                        size = Size(videoRenderWidth, videoRenderHeight),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f, pathEffect = dashEffect)
                    )

                    // Crosshair guides
                    drawLine(
                        color = ImmersivePrimary.copy(alpha = 0.6f),
                        start = Offset(videoOffsetX, targetY),
                        end = Offset(videoOffsetX + videoRenderWidth, targetY),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = ImmersivePrimary.copy(alpha = 0.6f),
                        start = Offset(targetX, videoOffsetY),
                        end = Offset(targetX, videoOffsetY + videoRenderHeight),
                        strokeWidth = 1f,
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
                        .pointerInput(activeCue.id, videoRenderWidth, videoRenderHeight) {
                            detectDragGestures(
                                onDragStart = { isUserDragging = true },
                                onDragEnd = { isUserDragging = false },
                                onDragCancel = { isUserDragging = false }
                            ) { change, dragAmount ->
                                change.consume()
                                val deltaX = dragAmount.x / videoRenderWidth
                                val deltaY = dragAmount.y / videoRenderHeight
                                val newX = (activeCue.posX + deltaX).coerceIn(0.05f, 0.95f)
                                val newY = (activeCue.posY + deltaY).coerceIn(0.05f, 0.95f)
                                onSubtitlePositionChanged(newX, newY)
                            }
                        }
                        .clickable { onSubtitleTapped() }
                        .clip(RoundedCornerShape(style.cornerRadiusDp.dp))
                        .background(bgColor)
                        .then(
                            if (isUserDragging) {
                                Modifier.border(1.5.dp, AccentCyan, RoundedCornerShape(style.cornerRadiusDp.dp))
                            } else if (hasDarkBox) {
                                Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(style.cornerRadiusDp.dp))
                            } else {
                                Modifier
                            }
                        )
                        .padding(
                            horizontal = style.paddingHorizontalDp.dp,
                            vertical = style.paddingVerticalDp.dp
                        )
                        .testTag("subtitle_overlay_draggable")
                ) {
                    Text(
                        text = activeCue.text,
                        color = textColor,
                        fontSize = (style.fontSizeSp * 0.9f).sp,
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
                }
            }
        }

        // 4. Top Quick Info & Fullscreen Toggle
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.70f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(5.dp).background(AccentEmerald, CircleShape))
                        Text(
                            text = if (playerState.videoWidth > 0) "${playerState.videoWidth}x${playerState.videoHeight}" else "Preview",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (onToggleFullscreen != null) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.75f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.20f))
                    ) {
                        IconButton(
                            onClick = onToggleFullscreen,
                            modifier = Modifier.size(32.dp).testTag("fullscreen_toggle_top_btn")
                        ) {
                            Icon(
                                imageVector = StudioIcons.Fullscreen,
                                contentDescription = "Enter Fullscreen",
                                tint = ImmersivePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Clean, Accessible Playback Controller Bar at Bottom
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                shadowElevation = 8.dp,
                modifier = Modifier.testTag("floating_playback_controls")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    // Speed Selector
                    Surface(
                        shape = RoundedCornerShape(6.dp),
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
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Mute / Unmute
                    IconButton(
                        onClick = { playerController.toggleMute() },
                        modifier = Modifier.size(28.dp).testTag("mute_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (playerState.isMuted) StudioIcons.VolumeOff else StudioIcons.VolumeUp,
                            contentDescription = if (playerState.isMuted) "Unmute" else "Mute",
                            tint = if (playerState.isMuted) AccentRose else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Rewind 5s
                    IconButton(
                        onClick = { playerController.seekBy(-5000L) },
                        modifier = Modifier.size(28.dp).testTag("rewind_5s_button")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Rewind,
                            contentDescription = "Rewind 5 Seconds",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Prev Frame
                    IconButton(
                        onClick = { playerController.stepFrame(-1) },
                        modifier = Modifier.size(24.dp).testTag("prev_frame_button")
                    ) {
                        Text("-1f", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Primary Play / Pause Circle
                    Surface(
                        shape = CircleShape,
                        color = ImmersivePrimary,
                        modifier = Modifier.size(34.dp)
                    ) {
                        IconButton(
                            onClick = { playerController.togglePlayPause() },
                            modifier = Modifier.fillMaxSize().testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) StudioIcons.Pause else StudioIcons.Play,
                                contentDescription = if (playerState.isPlaying) "Pause Video" else "Play Video",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Next Frame
                    IconButton(
                        onClick = { playerController.stepFrame(1) },
                        modifier = Modifier.size(24.dp).testTag("next_frame_button")
                    ) {
                        Text("+1f", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Forward 5s
                    IconButton(
                        onClick = { playerController.seekBy(5000L) },
                        modifier = Modifier.size(28.dp).testTag("forward_5s_button")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Forward,
                            contentDescription = "Forward 5 Seconds",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Loop Toggle
                    IconButton(
                        onClick = { playerController.toggleLooping() },
                        modifier = Modifier.size(28.dp).testTag("loop_toggle_btn")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Refresh,
                            contentDescription = "Toggle Loop",
                            tint = if (playerState.isLooping) ImmersivePrimary else Color.White.copy(alpha = 0.40f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
