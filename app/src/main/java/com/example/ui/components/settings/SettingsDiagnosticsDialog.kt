package com.example.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProcessingSettings
import com.example.model.SubtitleTrack
import com.example.model.VideoMetadata
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun SettingsDiagnosticsDialog(
    videoMetadata: VideoMetadata,
    subtitleTrack: SubtitleTrack,
    settings: ProcessingSettings,
    onDismiss: () -> Unit,
    onClearCache: () -> Unit,
    onResetDefaults: () -> Unit
) {
    val totalCues = subtitleTrack.cues.size
    val totalDurationSec = if (totalCues > 0) {
        val totalMs = subtitleTrack.cues.sumOf { it.durationMs }
        totalMs / 1000f
    } else 0f

    val totalWords = subtitleTrack.cues.sumOf { it.text.split(Regex("\\s+")).filter { w -> w.isNotBlank() }.size }
    val wpm = if (totalDurationSec > 0) ((totalWords / totalDurationSec) * 60f).toInt() else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(StudioIcons.Cpu, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                Text("Studio Diagnostics & Health", color = ImmersiveTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Video & Project Stat Card
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Active Project Session", color = ImmersivePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Video: ${videoMetadata.fileName.ifEmpty { "None loaded" }}", color = ImmersiveTextSecondary, fontSize = 10.sp)
                        Text("Resolution: ${videoMetadata.width}x${videoMetadata.height} (${videoMetadata.mimeType})", color = ImmersiveTextSecondary, fontSize = 10.sp)
                        Text("Subtitles: $totalCues cues (${subtitleTrack.format.displayName})", color = ImmersiveTextSecondary, fontSize = 10.sp)
                        Text("Reading Speed: ~$wpm Words/Min", color = if (wpm in 120..220) AccentEmerald else AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Engine Spec
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Hardware Pipeline", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Engine: ${settings.engine.title}", color = ImmersiveTextSecondary, fontSize = 10.sp)
                        Text("Target FPS: ${settings.targetFps} FPS", color = ImmersiveTextSecondary, fontSize = 10.sp)
                        Text("Frame Cache: ${settings.memoryCacheSizeMb} MB", color = ImmersiveTextSecondary, fontSize = 10.sp)
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onClearCache()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ImmersiveActionBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(StudioIcons.Delete, contentDescription = null, tint = AccentRose, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Purge Cache", color = AccentRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onResetDefaults()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ImmersiveActionBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(StudioIcons.Refresh, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reset Defaults", color = ImmersivePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}
