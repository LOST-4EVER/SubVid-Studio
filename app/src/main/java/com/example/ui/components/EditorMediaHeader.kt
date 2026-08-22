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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioOption
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
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun EditorMediaHeader(
    videoFileName: String,
    hasVideoLoaded: Boolean,
    subtitleTrack: SubtitleTrack,
    aspectRatio: AspectRatioOption,
    canUndo: Boolean,
    canRedo: Boolean,
    onSelectAspectRatio: (AspectRatioOption) -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onSaveProjectClick: () -> Unit,
    onExportClick: () -> Unit,
    onOptimizeClick: () -> Unit,
    onLoadVideoClick: () -> Unit,
    onUnloadVideoClick: () -> Unit,
    onLoadSubtitleClick: () -> Unit,
    onCreateNewSubtitleTrack: () -> Unit,
    onSaveSubtitleTrack: (SubtitleFormat) -> Unit,
    onUnloadSubtitleTrack: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAspectMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RectangleShape,
        color = ImmersiveSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group: Back Button + Project Title & Cue count
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(34.dp).testTag("editor_back_btn")
                ) {
                    Icon(
                        imageVector = StudioIcons.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = ImmersivePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = if (videoFileName.isNotEmpty()) videoFileName else "SubVid Studio",
                        color = ImmersiveTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = if (subtitleTrack.cues.isNotEmpty()) {
                            "${subtitleTrack.cues.size} Cues • ${aspectRatio.shortLabel}"
                        } else {
                            "Draft • ${aspectRatio.shortLabel}"
                        },
                        color = AccentCyan,
                        fontSize = 9.sp
                    )
                }
            }

            // Center / Right Group: Aspect Ratio, Undo, Redo, Optimize, Save, Export
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Aspect Ratio Dropdown Pill
                Box {
                    Surface(
                        shape = RectangleShape,
                        color = ImmersiveActionBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        modifier = Modifier
                            .clickable { showAspectMenu = true }
                            .testTag("editor_aspect_ratio_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                StudioIcons.Crop,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = aspectRatio.shortLabel,
                                color = ImmersiveTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showAspectMenu,
                        onDismissRequest = { showAspectMenu = false },
                        shape = RectangleShape,
                        modifier = Modifier.background(ImmersiveSurface)
                    ) {
                        AspectRatioOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        color = if (option == aspectRatio) ImmersivePrimary else ImmersiveTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (option == aspectRatio) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    showAspectMenu = false
                                    onSelectAspectRatio(option)
                                }
                            )
                        }
                    }
                }

                // 2. Undo Button
                IconButton(
                    onClick = onUndoClick,
                    enabled = canUndo,
                    modifier = Modifier.size(30.dp).testTag("editor_undo_btn")
                ) {
                    Icon(
                        imageVector = StudioIcons.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) Color.White else ImmersiveTextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // 3. Redo Button
                IconButton(
                    onClick = onRedoClick,
                    enabled = canRedo,
                    modifier = Modifier.size(30.dp).testTag("editor_redo_btn")
                ) {
                    Icon(
                        imageVector = StudioIcons.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) Color.White else ImmersiveTextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // 4. Quick Optimize Button
                IconButton(
                    onClick = onOptimizeClick,
                    modifier = Modifier.size(30.dp).testTag("editor_optimize_btn")
                ) {
                    Icon(
                        imageVector = StudioIcons.Speed,
                        contentDescription = "Performance & Optimization Options",
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // 5. Save Project Button (Disk Icon)
                IconButton(
                    onClick = onSaveProjectClick,
                    modifier = Modifier.size(30.dp).testTag("editor_save_project_btn")
                ) {
                    Icon(
                        imageVector = StudioIcons.Save,
                        contentDescription = "Save Project",
                        tint = AccentEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // 6. Export Action Button (Share / Export Icon)
                Surface(
                    shape = RectangleShape,
                    color = ImmersivePrimary,
                    modifier = Modifier
                        .clickable { onExportClick() }
                        .testTag("editor_export_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = StudioIcons.Export,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Export",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 7. More Options Dropdown (Media Management)
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.size(30.dp).testTag("editor_more_options_btn")
                    ) {
                        Icon(
                            imageVector = StudioIcons.MoreVert,
                            contentDescription = "Media Options",
                            tint = ImmersiveTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        shape = RectangleShape,
                        modifier = Modifier.background(ImmersiveSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Load Video File...", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                            leadingIcon = { Icon(StudioIcons.Import, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMoreMenu = false
                                onLoadVideoClick()
                            }
                        )
                        if (hasVideoLoaded) {
                            DropdownMenuItem(
                                text = { Text("Unload Video", color = AccentRose, fontSize = 12.sp) },
                                leadingIcon = { Icon(StudioIcons.Delete, contentDescription = null, tint = AccentRose, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMoreMenu = false
                                    onUnloadVideoClick()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Import Subtitle File...", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                            leadingIcon = { Icon(StudioIcons.Subtitles, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMoreMenu = false
                                onLoadSubtitleClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("New Blank Subtitle Track", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                            leadingIcon = { Icon(StudioIcons.Add, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMoreMenu = false
                                onCreateNewSubtitleTrack()
                            }
                        )
                        if (subtitleTrack.cues.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Export Subtitles (.SRT)...", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                                leadingIcon = { Icon(StudioIcons.Save, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMoreMenu = false
                                    onSaveSubtitleTrack(SubtitleFormat.SRT)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Subtitles (.VTT)...", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                                leadingIcon = { Icon(StudioIcons.Save, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMoreMenu = false
                                    onSaveSubtitleTrack(SubtitleFormat.VTT)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Subtitles (.ASS)...", color = ImmersiveTextPrimary, fontSize = 12.sp) },
                                leadingIcon = { Icon(StudioIcons.Save, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMoreMenu = false
                                    onSaveSubtitleTrack(SubtitleFormat.ASS)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Unload Subtitles", color = AccentRose, fontSize = 12.sp) },
                                leadingIcon = { Icon(StudioIcons.Delete, contentDescription = null, tint = AccentRose, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMoreMenu = false
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
