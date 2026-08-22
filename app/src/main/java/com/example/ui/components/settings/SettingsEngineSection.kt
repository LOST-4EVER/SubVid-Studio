package com.example.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProcessingEngine
import com.example.model.ProcessingSettings
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun SettingsEngineSection(
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
                        .background(AccentCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(StudioIcons.Cpu, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "PROCESSING & RENDER ENGINE",
                    color = ImmersiveTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // GPU Hardware Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (settings.engine == ProcessingEngine.GPU_HARDWARE) ImmersiveActionBg else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan.copy(alpha = 0.4f) else ImmersiveBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onUpdateSettings(settings.copy(engine = ProcessingEngine.GPU_HARDWARE)) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RadioButton(
                    selected = settings.engine == ProcessingEngine.GPU_HARDWARE,
                    onClick = { onUpdateSettings(settings.copy(engine = ProcessingEngine.GPU_HARDWARE)) },
                    colors = RadioButtonDefaults.colors(selectedColor = AccentCyan, unselectedColor = ImmersiveTextSecondary)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "GPU Hardware MediaCodec",
                            color = ImmersiveTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AccentCyan.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "FASTEST",
                                color = AccentCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Hardware Surface encoder for zero-copy encoding, frame-accurate rendering and high battery efficiency.",
                        color = ImmersiveTextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            // CPU Multi-threaded Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (settings.engine == ProcessingEngine.CPU_MULTITHREAD) ImmersiveActionBg else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (settings.engine == ProcessingEngine.CPU_MULTITHREAD) ImmersivePrimary.copy(alpha = 0.4f) else ImmersiveBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onUpdateSettings(settings.copy(engine = ProcessingEngine.CPU_MULTITHREAD)) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RadioButton(
                    selected = settings.engine == ProcessingEngine.CPU_MULTITHREAD,
                    onClick = { onUpdateSettings(settings.copy(engine = ProcessingEngine.CPU_MULTITHREAD)) },
                    colors = RadioButtonDefaults.colors(selectedColor = ImmersivePrimary, unselectedColor = ImmersiveTextSecondary)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CPU Multi-Core Processing",
                        color = ImmersiveTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Multi-threaded software worker pool for pixel-exact glyph rasterization and custom typography fallback.",
                        color = ImmersiveTextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            // CPU Thread Selector (when CPU active)
            if (settings.engine == ProcessingEngine.CPU_MULTITHREAD) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Software Worker Threads:", color = ImmersiveTextSecondary, fontSize = 11.sp)
                        Text("${settings.cpuThreads} Cores", color = ImmersivePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 4, 8, 16).forEach { count ->
                            val isSelected = settings.cpuThreads == count
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) ImmersivePrimary else ImmersiveActionBg,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateSettings(settings.copy(cpuThreads = count)) }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                    Text(
                                        text = "$count T",
                                        color = if (isSelected) Color.Black else ImmersiveTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hardware Surface Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("OpenGL Surface Texture Streaming", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Direct GPU texture binding for zero-copy viewport rendering", color = ImmersiveTextSecondary, fontSize = 10.sp)
                }
                Switch(
                    checked = settings.enableHardwareSurface,
                    onCheckedChange = { isChecked ->
                        onUpdateSettings(settings.copy(enableHardwareSurface = isChecked))
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
