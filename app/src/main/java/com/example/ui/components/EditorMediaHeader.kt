package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubtitleFormat
import com.example.model.SubtitleTrack
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary

@Composable
fun EditorMediaHeader(
    videoFileName: String,
    hasVideoLoaded: Boolean,
    subtitleTrack: SubtitleTrack,
    onSaveProjectClick: () -> Unit,
    onLoadVideoClick: () -> Unit,
    onUnloadVideoClick: () -> Unit,
    onLoadSubtitleClick: () -> Unit,
    onCreateNewSubtitleTrack: () -> Unit,
    onSaveSubtitleTrack: (SubtitleFormat) -> Unit,
    onUnloadSubtitleTrack: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showVideoMenu by remember { mutableStateOf(false) }
    var showSubtitleMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RectangleShape,
        color = ImmersiveSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group: Back Button + Project Info
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = StudioIcons.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = ImmersivePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = if (videoFileName.isNotEmpty()) videoFileName else "SubVid Studio Editor",
                        color = ImmersiveTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = if (subtitleTrack.cues.isNotEmpty()) {
                            "${subtitleTrack.cues.size} Cues • ${subtitleTrack.format.extension.uppercase()}"
                        } else {
                            "No Subtitles Loaded"
                        },
                        color = if (subtitleTrack.cues.isNotEmpty()) AccentCyan else ImmersiveTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Right Group: Media & Project Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save Project Button
                Surface(
                    shape = RectangleShape,
                    color = AccentEmerald.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { onSaveProjectClick() }
                        .testTag("editor_save_project_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(StudioIcons.Save, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(13.dp))
                        Text("Save", color = AccentEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Video Dropdown Menu Button
                Box {
                    Surface(
                        shape = RectangleShape,
                        color = ImmersiveActionBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        modifier = Modifier
                            .clickable { showVideoMenu = true }
                            .testTag("editor_video_menu_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(StudioIcons.Video, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(13.dp))
                            Text("Video", color = ImmersivePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    DropdownMenu(
                        expanded = showVideoMenu,
                        onDismissRequest = { showVideoMenu = false },
                        shape = RectangleShape,
                        modifier = Modifier.background(ImmersiveSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Load Video File...", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                            leadingIcon = { Icon(StudioIcons.Import, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showVideoMenu = false
                                onLoadVideoClick()
                            }
                        )
                        if (hasVideoLoaded) {
                            DropdownMenuItem(
                                text = { Text("Unload Video", color = AccentRose, fontSize = 12.sp) },
                                leadingIcon = { Icon(StudioIcons.Delete, contentDescription = null, tint = AccentRose, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showVideoMenu = false
                                    onUnloadVideoClick()
                                }
                            )
                        }
                    }
                }

                // Subtitles Dropdown Menu Button
                Box {
                    Surface(
                        shape = RectangleShape,
                        color = ImmersiveActionBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        modifier = Modifier
                            .clickable { showSubtitleMenu = true }
                            .testTag("editor_subtitles_menu_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(StudioIcons.Subtitles, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(13.dp))
                            Text("Subtitles", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    DropdownMenu(
                        expanded = showSubtitleMenu,
                        onDismissRequest = { showSubtitleMenu = false },
                        shape = RectangleShape,
                        modifier = Modifier.background(ImmersiveSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Load Subtitle File (SRT, VTT, ASS)...", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                            leadingIcon = { Icon(StudioIcons.Import, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showSubtitleMenu = false
                                onLoadSubtitleClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("New Blank Subtitle Track", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                            leadingIcon = { Icon(StudioIcons.Add, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showSubtitleMenu = false
                                onCreateNewSubtitleTrack()
                            }
                        )
                        if (subtitleTrack.cues.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Export Subtitles (.SRT)...", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                                leadingIcon = { Icon(StudioIcons.Save, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showSubtitleMenu = false
                                    onSaveSubtitleTrack(SubtitleFormat.SRT)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Subtitles (.VTT)...", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                                leadingIcon = { Icon(StudioIcons.Save, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showSubtitleMenu = false
                                    onSaveSubtitleTrack(SubtitleFormat.VTT)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Subtitles (.ASS)...", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                                leadingIcon = { Icon(StudioIcons.Save, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showSubtitleMenu = false
                                    onSaveSubtitleTrack(SubtitleFormat.ASS)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Unload Subtitles", color = AccentRose, fontSize = 12.sp) },
                                leadingIcon = { Icon(StudioIcons.Delete, contentDescription = null, tint = AccentRose, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showSubtitleMenu = false
                                    onUnloadSubtitleTrack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
