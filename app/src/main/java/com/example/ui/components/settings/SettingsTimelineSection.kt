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
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun SettingsTimelineSection(
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
                        .background(AccentAmber.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(StudioIcons.Timer, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "TIMELINE & SCRUBBER CONTROLS",
                    color = ImmersiveTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Magnetic Cue Snapping
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Magnetic Playhead Snapping", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Snaps playhead when seeking near subtitle boundaries", color = ImmersiveTextSecondary, fontSize = 10.sp)
                }
                Switch(
                    checked = settings.snapToCues,
                    onCheckedChange = { isChecked ->
                        onUpdateSettings(settings.copy(snapToCues = isChecked))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentCyan,
                        checkedTrackColor = AccentCyan.copy(alpha = 0.3f),
                        uncheckedThumbColor = ImmersiveTextSecondary,
                        uncheckedTrackColor = ImmersiveActionBg
                    )
                )
            }

            // Micro-Nudge Step Picker
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Micro-Nudge Step Interval", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(100L, 250L, 500L, 1000L).forEach { ms ->
                        val isSelected = settings.nudgeStepMs == ms
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) AccentAmber else ImmersiveActionBg,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onUpdateSettings(settings.copy(nudgeStepMs = ms))
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = "±${ms}ms",
                                    color = if (isSelected) Color.Black else ImmersiveTextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Procedural Audio Waveform Rendering
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fast Speech Waveform Visualizer", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Hardware canvas acceleration for waveform peaks", color = ImmersiveTextSecondary, fontSize = 10.sp)
                }
                Switch(
                    checked = settings.fastWaveformRendering,
                    onCheckedChange = { isChecked ->
                        onUpdateSettings(settings.copy(fastWaveformRendering = isChecked))
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
