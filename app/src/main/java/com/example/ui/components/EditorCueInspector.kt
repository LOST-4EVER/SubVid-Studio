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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubtitleCue
import com.example.model.SubtitleStyle
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveOnPrimary
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
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
    onDeleteCue: () -> Unit,
    onUpdateCueText: (SubtitleCue, String) -> Unit,
    onUpdateCuePosition: (SubtitleCue, Float, Float) -> Unit = { _, _, _ -> },
    onUpdateCueStyle: (SubtitleCue, SubtitleStyle) -> Unit,
    onUpdateCueFontSize: (SubtitleCue, Float) -> Unit = { cue, sz -> onUpdateCueStyle(cue, cue.style.copy(fontSizeSp = sz)) },
    onJumpToCue: (SubtitleCue) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedCue == null || totalCuesCount == 0) {
        // Empty State: No cues in track
        Surface(
            shape = RectangleShape,
            color = ImmersiveSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = StudioIcons.Subtitles,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "Import a Subtitle File to Review & Edit",
                    color = ImmersiveTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Load SRT, VTT, or ASS files to adjust text, sync timing, and change position.",
                    color = ImmersiveTextMuted,
                    fontSize = 10.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
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
        shape = RectangleShape,
        color = ImmersiveSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: Cue Navigation, Timing Info, Jump & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Previous Cue button
                    IconButton(
                        onClick = onSelectPreviousCue,
                        modifier = Modifier
                            .size(30.dp)
                            .background(ImmersiveActionBg, RectangleShape)
                            .testTag("inspector_prev_cue_btn")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Rewind,
                            contentDescription = "Previous Cue",
                            tint = ImmersivePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "Cue #${currentCueIndex + 1}",
                                color = ImmersivePrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "of $totalCuesCount",
                                color = ImmersiveTextMuted,
                                fontSize = 9.sp
                            )
                        }
                        Text(
                            text = "${SubtitleCue.formatTimestampShort(selectedCue.startTimeMs)} ➔ ${SubtitleCue.formatTimestampShort(selectedCue.endTimeMs)}",
                            color = AccentCyan,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Next Cue button
                    IconButton(
                        onClick = onSelectNextCue,
                        modifier = Modifier
                            .size(30.dp)
                            .background(ImmersiveActionBg, RectangleShape)
                            .testTag("inspector_next_cue_btn")
                    ) {
                        Icon(
                            imageVector = StudioIcons.Forward,
                            contentDescription = "Next Cue",
                            tint = ImmersivePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Actions: Jump to start, Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RectangleShape,
                        color = ImmersiveActionBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        modifier = Modifier
                            .clickable { onJumpToCue(selectedCue) }
                            .testTag("jump_to_cue_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(StudioIcons.Play, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(10.dp))
                            Text("Jump", color = ImmersiveTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onDeleteCue,
                        modifier = Modifier
                            .size(28.dp)
                            .background(ImmersiveActionBg, RectangleShape)
                            .testTag("delete_cue_btn")
                    ) {
                        Icon(StudioIcons.Delete, contentDescription = "Delete Cue", tint = AccentRose, modifier = Modifier.size(13.dp))
                    }
                }
            }

            // Row 2: Subtitle Text Input (Auto-saves instantaneously to ViewModel and file)
            OutlinedTextField(
                value = selectedCue.text,
                onValueChange = { newText ->
                    onUpdateCueText(selectedCue, newText)
                },
                placeholder = { Text("Edit subtitle text...", color = ImmersiveTextMuted, fontSize = 11.sp) },
                singleLine = false,
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ImmersiveTextPrimary,
                    unfocusedTextColor = ImmersiveTextPrimary,
                    focusedBorderColor = ImmersivePrimary,
                    unfocusedBorderColor = ImmersiveBorder,
                    focusedContainerColor = ImmersiveBg,
                    unfocusedContainerColor = ImmersiveBg
                ),
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cue_text_input")
            )

            // Row 3: Quick Position Anchors (Top, Center, Bottom, Left, Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "POS:",
                    color = ImmersivePrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )

                // Top
                Surface(
                    shape = RectangleShape,
                    color = if (selectedCue.posY < 0.35f) AccentCyan.copy(alpha = 0.25f) else ImmersiveActionBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCue.posY < 0.35f) AccentCyan else ImmersiveBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUpdateCuePosition(selectedCue, 0.50f, 0.12f) }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 3.dp)) {
                        Text("Top", color = if (selectedCue.posY < 0.35f) AccentCyan else ImmersiveTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Center
                Surface(
                    shape = RectangleShape,
                    color = if (selectedCue.posY in 0.35f..0.65f) AccentCyan.copy(alpha = 0.25f) else ImmersiveActionBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCue.posY in 0.35f..0.65f) AccentCyan else ImmersiveBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUpdateCuePosition(selectedCue, 0.50f, 0.50f) }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 3.dp)) {
                        Text("Mid", color = if (selectedCue.posY in 0.35f..0.65f) AccentCyan else ImmersiveTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Bottom
                Surface(
                    shape = RectangleShape,
                    color = if (selectedCue.posY > 0.65f) AccentCyan.copy(alpha = 0.25f) else ImmersiveActionBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCue.posY > 0.65f) AccentCyan else ImmersiveBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUpdateCuePosition(selectedCue, 0.50f, 0.85f) }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 3.dp)) {
                        Text("Bottom", color = if (selectedCue.posY > 0.65f) AccentCyan else ImmersiveTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Left
                Surface(
                    shape = RectangleShape,
                    color = if (selectedCue.posX < 0.35f) AccentCyan.copy(alpha = 0.25f) else ImmersiveActionBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCue.posX < 0.35f) AccentCyan else ImmersiveBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUpdateCuePosition(selectedCue, 0.25f, selectedCue.posY) }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 3.dp)) {
                        Text("Left", color = if (selectedCue.posX < 0.35f) AccentCyan else ImmersiveTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Right
                Surface(
                    shape = RectangleShape,
                    color = if (selectedCue.posX > 0.65f) AccentCyan.copy(alpha = 0.25f) else ImmersiveActionBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCue.posX > 0.65f) AccentCyan else ImmersiveBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUpdateCuePosition(selectedCue, 0.75f, selectedCue.posY) }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 3.dp)) {
                        Text("Right", color = if (selectedCue.posX > 0.65f) AccentCyan else ImmersiveTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Row 4: Styling (Font Size Steppers, Bold, Italic, Box, Colors)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Size Steppers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Surface(
                        shape = RectangleShape,
                        color = ImmersiveActionBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        modifier = Modifier
                            .clickable {
                                val newSize = (style.fontSizeSp - 2f).coerceIn(10f, 60f)
                                onUpdateCueFontSize(selectedCue, newSize)
                            }
                            .testTag("cue_font_decrease_btn")
                    ) {
                        Text(
                            text = "A-",
                            color = ImmersiveTextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "${style.fontSizeSp.toInt()}sp",
                        color = AccentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Surface(
                        shape = RectangleShape,
                        color = ImmersiveActionBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        modifier = Modifier
                            .clickable {
                                val newSize = (style.fontSizeSp + 2f).coerceIn(10f, 60f)
                                onUpdateCueFontSize(selectedCue, newSize)
                            }
                            .testTag("cue_font_increase_btn")
                    ) {
                        Text(
                            text = "A+",
                            color = ImmersiveTextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Bold & Italic
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Surface(
                        shape = RectangleShape,
                        color = if (style.isBold) ImmersivePrimary else ImmersiveActionBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        modifier = Modifier
                            .clickable {
                                val updated = style.copy(isBold = !style.isBold)
                                onUpdateCueStyle(selectedCue, updated)
                            }
                            .testTag("cue_bold_toggle")
                    ) {
                        Text(
                            text = "B",
                            color = if (style.isBold) ImmersiveOnPrimary else ImmersiveTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RectangleShape,
                        color = if (style.isItalic) ImmersivePrimary else ImmersiveActionBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        modifier = Modifier
                            .clickable {
                                val updated = style.copy(isItalic = !style.isItalic)
                                onUpdateCueStyle(selectedCue, updated)
                            }
                            .testTag("cue_italic_toggle")
                    ) {
                        Text(
                            text = "I",
                            color = if (style.isItalic) ImmersiveOnPrimary else ImmersiveTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RectangleShape,
                        color = if (hasDarkBg) ImmersivePrimary else ImmersiveActionBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        modifier = Modifier
                            .clickable {
                                val newBg = if (hasDarkBg) 0x00000000L else 0x99000000L
                                val updated = style.copy(backgroundColorArgb = newBg)
                                onUpdateCueStyle(selectedCue, updated)
                            }
                            .testTag("cue_background_toggle")
                    ) {
                        Text(
                            text = if (hasDarkBg) "Box" else "NoBox",
                            color = if (hasDarkBg) ImmersiveOnPrimary else ImmersiveTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                        )
                    }
                }

                // Quick Colors
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    quickColors.forEach { (colorVal, _) ->
                        val isSelected = style.textColorArgb == colorVal
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color(colorVal), RectangleShape)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ImmersivePrimary else Color.Gray,
                                    shape = RectangleShape
                                )
                                .clickable {
                                    val updated = style.copy(textColorArgb = colorVal)
                                    onUpdateCueStyle(selectedCue, updated)
                                }
                        )
                    }
                }
            }
        }
    }
}
