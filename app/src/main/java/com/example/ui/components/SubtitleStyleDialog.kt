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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.SubtitleStyle
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentAmber
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
fun SubtitleStyleDialog(
    currentStyle: SubtitleStyle,
    onDismiss: () -> Unit,
    onApplyStyle: (SubtitleStyle, applyToAll: Boolean) -> Unit
) {
    var fontSize by remember { mutableFloatStateOf(currentStyle.fontSizeSp) }
    var selectedTextColor by remember { mutableLongStateOf(currentStyle.textColorArgb) }
    var isBold by remember { mutableStateOf(currentStyle.isBold) }
    var isItalic by remember { mutableStateOf(currentStyle.isItalic) }
    var strokeWidth by remember { mutableFloatStateOf(currentStyle.strokeWidthDp) }
    var hasBackgroundBox by remember { mutableStateOf((currentStyle.backgroundColorArgb ushr 24) > 0) }
    var applyToAll by remember { mutableStateOf(true) }

    val colorPalette = listOf(
        0xFFFFFFFFL to "White",
        0xFF00E5FFL to "Neon Cyan",
        0xFFFCD34DL to "Amber",
        0xFFA7F3D0L to "Emerald",
        0xFFD0BCFFL to "Lavender",
        0xFFF43F5EL to "Rose",
        0xFFFEF08AL to "Lemon"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("subtitle_style_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .background(ImmersivePrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(StudioIcons.Style, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Subtitle Style", color = ImmersiveTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Typography, Contrast & Fill", color = ImmersiveTextMuted, fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(StudioIcons.Close, contentDescription = "Close", tint = ImmersiveTextSecondary)
                    }
                }

                // Live Preview Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ImmersiveBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val previewBg = if (hasBackgroundBox) Color(0x99000000) else Color.Transparent
                        Box(
                            modifier = Modifier
                                .background(previewBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Preview Subtitle Text",
                                color = Color(selectedTextColor),
                                fontSize = fontSize.sp,
                                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Font Size Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Font Size", color = ImmersiveTextSecondary, fontSize = 12.sp)
                        Text("${fontSize.toInt()} sp", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 14f..36f,
                        colors = SliderDefaults.colors(
                            thumbColor = ImmersivePrimary,
                            activeTrackColor = ImmersivePrimary,
                            inactiveTrackColor = ImmersiveActionBg
                        )
                    )
                }

                // Text Color Palette
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("TEXT COLOR", color = ImmersivePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorPalette.forEach { (colorVal, _) ->
                            val isSelected = selectedTextColor == colorVal
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) ImmersivePrimary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedTextColor = colorVal },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(StudioIcons.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Font Weight & Italic Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isBold) ImmersivePrimary else ImmersiveActionBg,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { isBold = !isBold }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(StudioIcons.Bold, contentDescription = null, tint = if (isBold) ImmersiveOnPrimary else ImmersiveTextSecondary)
                            Spacer(Modifier.width(6.dp))
                            Text("Bold", color = if (isBold) ImmersiveOnPrimary else ImmersiveTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isItalic) ImmersivePrimary else ImmersiveActionBg,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { isItalic = !isItalic }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(StudioIcons.Italic, contentDescription = null, tint = if (isItalic) ImmersiveOnPrimary else ImmersiveTextSecondary)
                            Spacer(Modifier.width(6.dp))
                            Text("Italic", color = if (isItalic) ImmersiveOnPrimary else ImmersiveTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Background Box Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
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
                            Text("Dark Background Box", color = ImmersiveTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Improves subtitle legibility over bright video", color = ImmersiveTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = hasBackgroundBox,
                            onCheckedChange = { hasBackgroundBox = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ImmersivePrimary,
                                checkedTrackColor = ImmersivePrimaryContainer
                            )
                        )
                    }
                }

                // Apply to All Switch
                Surface(
                    shape = RoundedCornerShape(12.dp),
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
                            Text("Apply to All Cues", color = ImmersiveTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Applies formatting across the entire video", color = ImmersiveTextMuted, fontSize = 11.sp)
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

                // Action Buttons
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
                            val newStyle = currentStyle.copy(
                                fontSizeSp = fontSize,
                                textColorArgb = selectedTextColor,
                                isBold = isBold,
                                isItalic = isItalic,
                                strokeWidthDp = strokeWidth,
                                backgroundColorArgb = if (hasBackgroundBox) 0x99000000L else 0x00000000L
                            )
                            onApplyStyle(newStyle, applyToAll)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImmersivePrimary,
                            contentColor = ImmersiveOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("apply_style_button")
                    ) {
                        Text("Apply Style", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
