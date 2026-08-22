package com.example.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProcessingSettings
import com.example.model.SubtitleFormat
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun SettingsSubtitleDefaultsSection(
    settings: ProcessingSettings,
    onUpdateSettings: (ProcessingSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(AccentPurple.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(StudioIcons.Subtitles, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "SUBTITLE STYLING & FORMAT DEFAULTS",
                    color = ImmersiveTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Default Subtitle Format
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Default Subtitle Format", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Standard for newly created subtitle tracks", color = ImmersiveTextSecondary, fontSize = 10.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(SubtitleFormat.SRT, SubtitleFormat.VTT, SubtitleFormat.ASS).forEach { fmt ->
                        val isSelected = settings.defaultSubtitleFormat == fmt
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) AccentPurple else ImmersiveActionBg,
                            modifier = Modifier.clickable {
                                onUpdateSettings(settings.copy(defaultSubtitleFormat = fmt))
                            }
                        ) {
                            Text(
                                text = fmt.extension.uppercase(),
                                color = if (isSelected) Color.Black else ImmersiveTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Default Font Family
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Default Font Family", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Sans-Serif", "Serif", "Monospace", "Condensed").forEach { family ->
                        val isSelected = settings.defaultFontFamily.equals(family, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) ImmersivePrimary else ImmersiveActionBg,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onUpdateSettings(settings.copy(defaultFontFamily = family))
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = family,
                                    color = if (isSelected) Color.Black else ImmersiveTextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = when (family) {
                                        "Serif" -> FontFamily.Serif
                                        "Monospace" -> FontFamily.Monospace
                                        else -> FontFamily.SansSerif
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Default Font Size Slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Default Font Size", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${settings.defaultFontSize.toInt()} sp", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = settings.defaultFontSize,
                    onValueChange = { onUpdateSettings(settings.copy(defaultFontSize = it)) },
                    valueRange = 12f..48f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentCyan,
                        activeTrackColor = AccentCyan,
                        inactiveTrackColor = ImmersiveActionBg
                    )
                )
            }

            // Subtitle Preview Live Chip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sample Subtitle Text Preview",
                    color = Color.White,
                    fontSize = settings.defaultFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = when (settings.defaultFontFamily) {
                        "Serif" -> FontFamily.Serif
                        "Monospace" -> FontFamily.Monospace
                        else -> FontFamily.SansSerif
                    }
                )
            }
        }
    }
}
