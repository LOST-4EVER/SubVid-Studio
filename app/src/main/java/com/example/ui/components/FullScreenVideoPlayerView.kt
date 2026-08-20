package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.model.SubtitleCue
import com.example.model.SubtitleTrack
import com.example.model.VideoMetadata
import com.example.player.PlayerUiState
import com.example.player.VideoPlayerController
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersivePrimary
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun FullScreenVideoPlayerView(
    playerController: VideoPlayerController,
    playerState: PlayerUiState,
    videoMetadata: VideoMetadata,
    subtitleTrack: SubtitleTrack,
    activeCue: SubtitleCue?,
    onSubtitlePositionChanged: (Float, Float) -> Unit,
    onOpenStyleDialog: () -> Unit,
    onOpenPlacementDialog: () -> Unit,
    onOpenSubtitleList: () -> Unit,
    onExitFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onExitFullscreen()
    }

    var showControls by remember { mutableStateOf(true) }
    var isUserDragging by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    // Auto-hide controls after 4 seconds of inactivity when playing
    LaunchedEffect(showControls, playerState.isPlaying, isUserDragging) {
        if (showControls && playerState.isPlaying && !isUserDragging) {
            delay(4000)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
            .testTag("fullscreen_video_container")
    ) {
        // 1. Fullscreen Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerController.getPlayer()
                    useController = false
                    this.resizeMode = resizeMode
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                view.player = playerController.getPlayer()
                view.resizeMode = resizeMode
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("fullscreen_player_surface")
        )

        // 2. Interactive Drag-and-Drop Subtitle Overlay
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val containerWidthPx = constraints.maxWidth.toFloat()
            val containerHeightPx = constraints.maxHeight.toFloat()

            // Calculate actual video frame within container based on aspect ratio
            val videoAspect = if (playerState.videoHeight > 0) {
                playerState.videoWidth.toFloat() / playerState.videoHeight.toFloat()
            } else 16f / 9f

            val containerAspect = if (containerHeightPx > 0) containerWidthPx / containerHeightPx else 16f / 9f

            val actualVideoWidth: Float
            val actualVideoHeight: Float
            val videoOffsetX: Float
            val videoOffsetY: Float

            if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
                actualVideoWidth = containerWidthPx
                actualVideoHeight = containerHeightPx
                videoOffsetX = 0f
                videoOffsetY = 0f
            } else if (containerAspect > videoAspect) {
                actualVideoHeight = containerHeightPx
                actualVideoWidth = containerHeightPx * videoAspect
                videoOffsetX = (containerWidthPx - actualVideoWidth) / 2f
                videoOffsetY = 0f
            } else {
                actualVideoWidth = containerWidthPx
                actualVideoHeight = containerWidthPx / videoAspect
                videoOffsetX = 0f
                videoOffsetY = (containerHeightPx - actualVideoHeight) / 2f
            }

            // Draw Positioning Safe Area and Crosshairs when actively dragging in fullscreen
            if (isUserDragging) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Safe-area dashed boundary
                    drawRect(
                        color = ImmersivePrimary.copy(alpha = 0.5f),
                        topLeft = Offset(videoOffsetX + actualVideoWidth * 0.05f, videoOffsetY + actualVideoHeight * 0.05f),
                        size = Size(actualVideoWidth * 0.90f, actualVideoHeight * 0.90f),
                        style = Stroke(width = 2f)
                    )
                    // Center guide lines
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = Offset(videoOffsetX + actualVideoWidth * 0.5f, videoOffsetY),
                        end = Offset(videoOffsetX + actualVideoWidth * 0.5f, videoOffsetY + actualVideoHeight),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = Offset(videoOffsetX, videoOffsetY + actualVideoHeight * 0.5f),
                        end = Offset(videoOffsetX + actualVideoWidth, videoOffsetY + actualVideoHeight * 0.5f),
                        strokeWidth = 1f
                    )
                }
            }

            // Render Active Cue
            if (activeCue != null && activeCue.text.isNotEmpty()) {
                val cue = activeCue
                val style = cue.style

                val posXInVideo = videoOffsetX + (cue.posX * actualVideoWidth)
                val posYInVideo = videoOffsetY + (cue.posY * actualVideoHeight)

                var cueWidthPx by remember { mutableFloatStateOf(200f) }
                var cueHeightPx by remember { mutableFloatStateOf(60f) }

                val clampedOffsetX = (posXInVideo - (cueWidthPx / 2f)).coerceIn(
                    videoOffsetX,
                    videoOffsetX + actualVideoWidth - cueWidthPx
                )
                val clampedOffsetY = (posYInVideo - (cueHeightPx / 2f)).coerceIn(
                    videoOffsetY,
                    videoOffsetY + actualVideoHeight - cueHeightPx
                )

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(clampedOffsetX.roundToInt(), clampedOffsetY.roundToInt())
                        }
                        .pointerInput(cue.id, actualVideoWidth, actualVideoHeight, videoOffsetX, videoOffsetY) {
                            detectDragGestures(
                                onDragStart = { isUserDragging = true },
                                onDragEnd = { isUserDragging = false },
                                onDragCancel = { isUserDragging = false },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val currentPixelX = cue.posX * actualVideoWidth
                                    val currentPixelY = cue.posY * actualVideoHeight
                                    val newPixelX = currentPixelX + dragAmount.x
                                    val newPixelY = currentPixelY + dragAmount.y
                                    val newNormX = (newPixelX / actualVideoWidth).coerceIn(0.05f, 0.95f)
                                    val newNormY = (newPixelY / actualVideoHeight).coerceIn(0.05f, 0.95f)
                                    onSubtitlePositionChanged(newNormX, newNormY)
                                }
                            )
                        }
                        .clip(RoundedCornerShape(style.cornerRadiusDp.dp))
                        .drawBehind {
                            cueWidthPx = size.width
                            cueHeightPx = size.height
                        }
                        .background(Color(style.backgroundColorArgb))
                        .then(
                            if (isUserDragging) {
                                Modifier.border(1.5.dp, ImmersivePrimary, RoundedCornerShape(style.cornerRadiusDp.dp))
                            } else Modifier
                        )
                        .padding(
                            horizontal = style.paddingHorizontalDp.dp,
                            vertical = style.paddingVerticalDp.dp
                        )
                ) {
                    Text(
                        text = cue.text,
                        color = Color(style.textColorArgb),
                        fontSize = (style.fontSizeSp * 1.15f).sp,
                        fontWeight = if (style.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (style.isItalic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = if (style.isUnderline) TextDecoration.Underline else TextDecoration.None,
                        textAlign = TextAlign.Center,
                        lineHeight = (style.fontSizeSp * 1.4f).sp
                    )
                }
            }
        }

        // 3. Top Glass Toolbar Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Back button & Video metadata
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onExitFullscreen,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                                .testTag("fullscreen_exit_back_btn")
                        ) {
                            Icon(
                                imageVector = StudioIcons.FullscreenExit,
                                contentDescription = "Exit Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = videoMetadata.fileName.ifEmpty { "Live Fullscreen Video" },
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(AccentEmerald, CircleShape))
                                Text(
                                    text = "${playerState.videoWidth}x${playerState.videoHeight} • ${subtitleTrack.format.name} (${subtitleTrack.cues.size} cues)",
                                    color = Color.White.copy(alpha = 0.70f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Right: Quick Action Buttons (Placement, Style, Subtitles List, Aspect Ratio)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Aspect Ratio Mode Switcher
                        IconButton(
                            onClick = {
                                resizeMode = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                } else {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) ImmersivePrimary.copy(alpha = 0.3f)
                                    else Color.White.copy(alpha = 0.10f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = StudioIcons.AspectRatio,
                                contentDescription = "Toggle Fit / Zoom",
                                tint = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) ImmersivePrimary else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Placement Dialog
                        IconButton(
                            onClick = onOpenPlacementDialog,
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White.copy(alpha = 0.10f), CircleShape)
                        ) {
                            Icon(
                                imageVector = StudioIcons.Position,
                                contentDescription = "Placement",
                                tint = AccentAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Style Dialog
                        IconButton(
                            onClick = onOpenStyleDialog,
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White.copy(alpha = 0.10f), CircleShape)
                        ) {
                            Icon(
                                imageVector = StudioIcons.Style,
                                contentDescription = "Subtitle Style",
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Subtitle List
                        IconButton(
                            onClick = onOpenSubtitleList,
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White.copy(alpha = 0.10f), CircleShape)
                        ) {
                            Icon(
                                imageVector = StudioIcons.Layers,
                                contentDescription = "Cues List",
                                tint = ImmersivePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Bottom Controls & Scrubber Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Timecode & Progress Slider Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.80f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        // Timecodes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTimecode(playerState.currentPositionMs),
                                color = ImmersivePrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            if (activeCue != null) {
                                val cueIndex = subtitleTrack.cues.indexOfFirst { it.id == activeCue.id }
                                val cueLabel = if (cueIndex >= 0) "#${cueIndex + 1}" else "Live"
                                Text(
                                    text = "Active Cue: $cueLabel",
                                    color = AccentCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = formatTimecode(playerState.durationMs),
                                color = Color.White.copy(alpha = 0.70f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Smooth Scrubber Slider
                        val duration = playerState.durationMs.coerceAtLeast(1L).toFloat()
                        val progress = (playerState.currentPositionMs.toFloat() / duration).coerceIn(0f, 1f)

                        Slider(
                            value = progress,
                            onValueChange = { newNorm ->
                                val seekTargetMs = (newNorm * duration).toLong()
                                playerController.seekTo(seekTargetMs)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = ImmersivePrimary,
                                activeTrackColor = ImmersivePrimary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .testTag("fullscreen_timeline_slider")
                        )
                    }
                }

                // Floating Glass Controller Pill
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = Color.Black.copy(alpha = 0.90f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                    shadowElevation = 14.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        // Speed Selector
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Mute / Unmute
                        IconButton(
                            onClick = { playerController.toggleMute() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (playerState.isMuted) StudioIcons.VolumeOff else StudioIcons.VolumeUp,
                                contentDescription = if (playerState.isMuted) "Unmute" else "Mute",
                                tint = if (playerState.isMuted) AccentRose else Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Rewind 5s
                        IconButton(
                            onClick = { playerController.seekBy(-5000L) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = StudioIcons.Rewind,
                                contentDescription = "Rewind 5s",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Prev Frame (-33ms)
                        IconButton(
                            onClick = { playerController.stepFrame(-1) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text(
                                text = "-1f",
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Main Play / Pause Circle
                        Surface(
                            shape = CircleShape,
                            color = ImmersivePrimary,
                            modifier = Modifier.size(46.dp)
                        ) {
                            IconButton(
                                onClick = { playerController.togglePlayPause() },
                                modifier = Modifier.fillMaxSize().testTag("fullscreen_play_pause_btn")
                            ) {
                                Icon(
                                    imageVector = if (playerState.isPlaying) StudioIcons.Pause else StudioIcons.Play,
                                    contentDescription = if (playerState.isPlaying) "Pause Video" else "Play Video",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Next Frame (+33ms)
                        IconButton(
                            onClick = { playerController.stepFrame(1) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text(
                                text = "+1f",
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Forward 5s
                        IconButton(
                            onClick = { playerController.seekBy(5000L) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = StudioIcons.Forward,
                                contentDescription = "Forward 5s",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Loop Toggle
                        IconButton(
                            onClick = { playerController.toggleLooping() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = StudioIcons.Refresh,
                                contentDescription = "Toggle Loop",
                                tint = if (playerState.isLooping) ImmersivePrimary else Color.White.copy(alpha = 0.45f),
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Exit Fullscreen
                        IconButton(
                            onClick = onExitFullscreen,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = StudioIcons.FullscreenExit,
                                contentDescription = "Exit Fullscreen",
                                tint = ImmersivePrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimecode(ms: Long): String {
    val totalSec = ms / 1000
    val millis = ms % 1000
    val s = totalSec % 60
    val m = (totalSec / 60) % 60
    val h = totalSec / 3600
    return if (h > 0) {
        String.format(Locale.US, "%02d:%02d:%02d.%03d", h, m, s, millis)
    } else {
        String.format(Locale.US, "%02d:%02d.%03d", m, s, millis)
    }
}
