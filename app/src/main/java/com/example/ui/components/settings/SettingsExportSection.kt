package com.example.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProcessingSettings
import com.example.model.QualityPreset
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun SettingsExportSection(
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
                        .background(AccentEmerald.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(StudioIcons.Export, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "EXPORT & ENCODING DEFAULTS",
                    color = ImmersiveTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Container format
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Default Video Container", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Preferred wrapper format for export", color = ImmersiveTextSecondary, fontSize = 10.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("mp4", "mkv", "webm").forEach { ext ->
                        val selected = settings.defaultExportContainer.equals(ext, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected) AccentCyan else ImmersiveActionBg,
                            modifier = Modifier.clickable {
                                onUpdateSettings(settings.copy(defaultExportContainer = ext))
                            }
                        ) {
                            Text(
                                text = ext.uppercase(),
                                color = if (selected) Color.Black else ImmersiveTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Target FPS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Target Export Framerate", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Output encoding frame frequency", color = ImmersiveTextSecondary, fontSize = 10.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(24, 30, 60).forEach { fps ->
                        val selected = settings.targetFps == fps
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected) AccentCyan else ImmersiveActionBg,
                            modifier = Modifier.clickable {
                                onUpdateSettings(settings.copy(targetFps = fps))
                            }
                        ) {
                            Text(
                                text = "$fps FPS",
                                color = if (selected) Color.Black else ImmersiveTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Quality Preset Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Default Bitrate Quality", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${settings.defaultQuality.targetBitrateBps / 1_000_000} Mbps target bitrate", color = ImmersiveTextSecondary, fontSize = 10.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(QualityPreset.HD_720P, QualityPreset.FULL_HD_1080P, QualityPreset.NATIVE_ULTRA).forEach { q ->
                        val selected = settings.defaultQuality == q
                        val label = when (q) {
                            QualityPreset.HD_720P -> "720p"
                            QualityPreset.FULL_HD_1080P -> "1080p"
                            QualityPreset.NATIVE_ULTRA -> "4K/Max"
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected) ImmersivePrimary else ImmersiveActionBg,
                            modifier = Modifier.clickable {
                                onUpdateSettings(settings.copy(defaultQuality = q))
                            }
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color.Black else ImmersiveTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Auto Save State
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Save Studio Sessions", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Saves state and undo history automatically", color = ImmersiveTextSecondary, fontSize = 10.sp)
                }
                Switch(
                    checked = settings.autoSaveProject,
                    onCheckedChange = { isChecked ->
                        onUpdateSettings(settings.copy(autoSaveProject = isChecked))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentCyan,
                        checkedTrackColor = AccentCyan.copy(alpha = 0.3f),
                        uncheckedThumbColor = ImmersiveTextSecondary,
                        uncheckedTrackColor = ImmersiveActionBg
                    )
                )
            }
        }
    }
}
