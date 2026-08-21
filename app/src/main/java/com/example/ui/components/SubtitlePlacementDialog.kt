package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.SubtitleAlignment
import com.example.model.SubtitleCue
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveOnPrimary
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersivePrimaryContainer
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceCard
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun SubtitlePlacementDialog(
    initialCue: SubtitleCue?,
    onDismiss: () -> Unit,
    onApply: (posX: Float, posY: Float, alignment: SubtitleAlignment, applyToAll: Boolean) -> Unit
) {
    var posX by remember { mutableFloatStateOf(initialCue?.posX ?: 0.50f) }
    var posY by remember { mutableFloatStateOf(initialCue?.posY ?: 0.88f) }
    var selectedAlignment by remember { mutableStateOf(initialCue?.alignment ?: SubtitleAlignment.BOTTOM_CENTER) }
    var applyToAll by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RectangleShape,
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("subtitle_placement_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ImmersivePrimary.copy(alpha = 0.2f), RectangleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = StudioIcons.Position,
                                contentDescription = null,
                                tint = ImmersivePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Text Placement",
                                color = ImmersiveTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Screen Anchor & Coordinates",
                                color = ImmersiveTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(StudioIcons.Close, contentDescription = "Close", tint = ImmersiveTextSecondary)
                    }
                }

                // 9-Point Alignment Matrix
                Text(
                    text = "QUICK ANCHOR PRESETS",
                    color = ImmersivePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val anchors = listOf(
                    listOf(SubtitleAlignment.TOP_START, SubtitleAlignment.TOP_CENTER, SubtitleAlignment.TOP_END),
                    listOf(SubtitleAlignment.CENTER_START, SubtitleAlignment.CENTER, SubtitleAlignment.CENTER_END),
                    listOf(SubtitleAlignment.BOTTOM_START, SubtitleAlignment.BOTTOM_CENTER, SubtitleAlignment.BOTTOM_END)
                )

                Surface(
                    shape = RectangleShape,
                    color = ImmersiveBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        anchors.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { align ->
                                    val isSelected = selectedAlignment == align
                                    Surface(
                                        shape = RectangleShape,
                                        color = if (isSelected) ImmersivePrimary else ImmersiveActionBg,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clickable {
                                                selectedAlignment = align
                                                // Adjust X/Y coordinates to standard anchor positions
                                                when (align) {
                                                    SubtitleAlignment.TOP_START -> { posX = 0.15f; posY = 0.12f }
                                                    SubtitleAlignment.TOP_CENTER -> { posX = 0.50f; posY = 0.12f }
                                                    SubtitleAlignment.TOP_END -> { posX = 0.85f; posY = 0.12f }
                                                    SubtitleAlignment.CENTER_START -> { posX = 0.15f; posY = 0.50f }
                                                    SubtitleAlignment.CENTER -> { posX = 0.50f; posY = 0.50f }
                                                    SubtitleAlignment.CENTER_END -> { posX = 0.85f; posY = 0.50f }
                                                    SubtitleAlignment.BOTTOM_START -> { posX = 0.15f; posY = 0.88f }
                                                    SubtitleAlignment.BOTTOM_CENTER -> { posX = 0.50f; posY = 0.88f }
                                                    SubtitleAlignment.BOTTOM_END -> { posX = 0.85f; posY = 0.88f }
                                                    SubtitleAlignment.CUSTOM -> {}
                                                }
                                            }
                                            .testTag("anchor_${align.name.lowercase()}"),
                                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = align.displayName().substringBefore(" "),
                                                color = if (isSelected) ImmersiveOnPrimary else ImmersiveTextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Fine-tune X / Y Sliders
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Horizontal Position (X)", color = ImmersiveTextSecondary, fontSize = 12.sp)
                        Text("${(posX * 100).toInt()}%", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = posX,
                        onValueChange = {
                            posX = it
                            selectedAlignment = SubtitleAlignment.CUSTOM
                        },
                        valueRange = 0.05f..0.95f,
                        colors = SliderDefaults.colors(
                            thumbColor = ImmersivePrimary,
                            activeTrackColor = ImmersivePrimary,
                            inactiveTrackColor = ImmersiveActionBg
                        ),
                        modifier = Modifier.testTag("slider_pos_x")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vertical Position (Y)", color = ImmersiveTextSecondary, fontSize = 12.sp)
                        Text("${(posY * 100).toInt()}%", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = posY,
                        onValueChange = {
                            posY = it
                            selectedAlignment = SubtitleAlignment.CUSTOM
                        },
                        valueRange = 0.05f..0.95f,
                        colors = SliderDefaults.colors(
                            thumbColor = ImmersivePrimary,
                            activeTrackColor = ImmersivePrimary,
                            inactiveTrackColor = ImmersiveActionBg
                        ),
                        modifier = Modifier.testTag("slider_pos_y")
                    )
                }

                // Apply to All Cues Switch
                Surface(
                    shape = RectangleShape,
                    color = ImmersiveSurfaceCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Apply to Entire Video", color = ImmersiveTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Syncs this position across all cues", color = ImmersiveTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = applyToAll,
                            onCheckedChange = { applyToAll = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ImmersivePrimary,
                                checkedTrackColor = ImmersivePrimaryContainer
                            )
                        )
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = ImmersiveTextSecondary)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = {
                            onApply(posX, posY, selectedAlignment, applyToAll)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImmersivePrimary,
                            contentColor = ImmersiveOnPrimary
                        ),
                        shape = RectangleShape,
                        modifier = Modifier.testTag("apply_placement_button")
                    ) {
                        Text("Apply Placement", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
