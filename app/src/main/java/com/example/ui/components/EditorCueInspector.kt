package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubtitleCue
import com.example.model.SubtitleStyle
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveOnPrimary
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceCard
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorCueInspector(
    selectedCue: SubtitleCue?,
    totalCuesCount: Int,
    currentCueIndex: Int,
    onSelectPreviousCue: () -> Unit,
    onSelectNextCue: () -> Unit,
    onDuplicateCue: () -> Unit,
    onDeleteCue: () -> Unit,
    onUpdateCueText: (SubtitleCue, String) -> Unit,
    onUpdateCueStyle: (SubtitleCue, SubtitleStyle) -> Unit,
    onJumpToCue: (SubtitleCue) -> Unit,
    onAddFirstCue: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedCue == null || totalCuesCount == 0) {
        // Empty State: No cues in track
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ImmersiveSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ImmersiveActionBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = StudioIcons.Subtitles,
                        contentDescription = null,
                        tint = ImmersivePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "No Subtitle Cues in Track",
                    color = ImmersiveTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Add your first subtitle segment at the current video timestamp",
                    color = ImmersiveTextMuted,
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Button(
                    onClick = onAddFirstCue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersivePrimary,
                        contentColor = ImmersiveOnPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_first_cue_btn")
                ) {
                    Icon(StudioIcons.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add First Cue", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val style = selectedCue.style
    val hasDarkBg = style.hasBackground

    val quickColors = listOf(
        0xFFFFFFFFL to "White",
        0xFF00E5FFL to "Cyan",
        0xFFFCD34DL to "Amber",
        0xFFA7F3D0L to "Emerald",
        0xFFF43F5EL to "Rose"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ImmersiveSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Cue Index, Timing, Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Previous Cue button (48dp interactive component size compliant)
                    IconButton(
                        onClick = onSelectPreviousCue,
                        modifier = Modifier
                            .size(36.dp)
                            .background(ImmersiveActionBg, RoundedCornerShape(8.dp))
                            .testTag("inspector_prev_cue_btn")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Rewind,
                            contentDescription = "Previous Cue",
                            tint = ImmersivePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Cue #${currentCueIndex + 1}",
                                color = ImmersivePrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "of $totalCuesCount",
                                color = ImmersiveTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "${SubtitleCue.formatTimestampShort(selectedCue.startTimeMs)} ➔ ${SubtitleCue.formatTimestampShort(selectedCue.endTimeMs)}",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Next Cue button
                    IconButton(
                        onClick = onSelectNextCue,
                        modifier = Modifier
                            .size(36.dp)
                            .background(ImmersiveActionBg, RoundedCornerShape(8.dp))
                            .testTag("inspector_next_cue_btn")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Forward,
                            contentDescription = "Next Cue",
                            tint = ImmersivePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Actions: Jump to start, Duplicate, Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quick jump to cue
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ImmersiveActionBg,
                        modifier = Modifier
                            .clickable { onJumpToCue(selectedCue) }
                            .testTag("jump_to_cue_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(StudioIcons.Play, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                            Text("Jump", color = ImmersiveTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Duplicate button
                    IconButton(
                        onClick = onDuplicateCue,
                        modifier = Modifier
                            .size(32.dp)
                            .background(ImmersiveActionBg, RoundedCornerShape(8.dp))
                            .testTag("duplicate_cue_btn")
                    ) {
                        Icon(StudioIcons.Copy, contentDescription = "Duplicate Cue", tint = AccentEmerald, modifier = Modifier.size(14.dp))
                    }

                    // Delete button
                    IconButton(
                        onClick = onDeleteCue,
                        modifier = Modifier
                            .size(32.dp)
                            .background(ImmersiveActionBg, RoundedCornerShape(8.dp))
                            .testTag("delete_cue_btn")
                    ) {
                        Icon(StudioIcons.Delete, contentDescription = "Delete Cue", tint = AccentRose, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Subtitle Text Field
            OutlinedTextField(
                value = selectedCue.text,
                onValueChange = { newText ->
                    onUpdateCueText(selectedCue, newText)
                },
                placeholder = { Text("Enter subtitle text for this cue...", color = ImmersiveTextMuted, fontSize = 13.sp) },
                singleLine = false,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ImmersiveTextPrimary,
                    unfocusedTextColor = ImmersiveTextPrimary,
                    focusedBorderColor = ImmersivePrimary,
                    unfocusedBorderColor = ImmersiveBorder,
                    focusedContainerColor = ImmersiveBg,
                    unfocusedContainerColor = ImmersiveBg
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cue_text_input")
            )

            // Quick Single-Cue Styling Strip (Bold, Italic, Dark Background Box, Colors)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Bold Toggle for this single cue
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (style.isBold) ImmersivePrimary else ImmersiveActionBg,
                    modifier = Modifier
                        .clickable {
                            val updated = style.copy(isBold = !style.isBold)
                            onUpdateCueStyle(selectedCue, updated)
                        }
                        .testTag("cue_bold_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = StudioIcons.Bold,
                            contentDescription = null,
                            tint = if (style.isBold) ImmersiveOnPrimary else ImmersiveTextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Bold",
                            color = if (style.isBold) ImmersiveOnPrimary else ImmersiveTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Italic Toggle for this single cue
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (style.isItalic) ImmersivePrimary else ImmersiveActionBg,
                    modifier = Modifier
                        .clickable {
                            val updated = style.copy(isItalic = !style.isItalic)
                            onUpdateCueStyle(selectedCue, updated)
                        }
                        .testTag("cue_italic_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = StudioIcons.Italic,
                            contentDescription = null,
                            tint = if (style.isItalic) ImmersiveOnPrimary else ImmersiveTextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Italic",
                            color = if (style.isItalic) ImmersiveOnPrimary else ImmersiveTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Dark Background Box Toggle for this single cue
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (hasDarkBg) ImmersivePrimary else ImmersiveActionBg,
                    modifier = Modifier
                        .clickable {
                            val newBg = if (hasDarkBg) 0x00000000L else 0x99000000L
                            val updated = style.copy(backgroundColorArgb = newBg)
                            onUpdateCueStyle(selectedCue, updated)
                        }
                        .testTag("cue_background_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(if (hasDarkBg) Color.Black else Color.Gray, RoundedCornerShape(2.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = if (hasDarkBg) "Box: On" else "Box: Off",
                            color = if (hasDarkBg) ImmersiveOnPrimary else ImmersiveTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick Color Swatches for this single cue
                quickColors.forEach { (colorVal, _) ->
                    val isSelected = style.textColorArgb == colorVal
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) ImmersivePrimary else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable {
                                val updated = style.copy(textColorArgb = colorVal)
                                onUpdateCueStyle(selectedCue, updated)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = StudioIcons.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
