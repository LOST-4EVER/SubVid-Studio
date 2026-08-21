package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProcessingEngine
import com.example.model.QualityPreset
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceCard
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.processingSettings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        item {
            Surface(
                shape = RectangleShape,
                color = ImmersiveSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(ImmersivePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(StudioIcons.Settings, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(20.dp))
                    }

                    Column {
                        Text(
                            text = "Settings & Engine",
                            color = ImmersiveTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hardware acceleration & export preferences",
                            color = ImmersiveTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 2. CPU / GPU Processing Mode Card
        item {
            Text(
                text = "PROCESSING ENGINE (CPU / GPU)",
                color = ImmersiveTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))

            Card(
                shape = RectangleShape,
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // GPU Hardware Accelerated Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (settings.engine == ProcessingEngine.GPU_HARDWARE) ImmersiveActionBg else Color.Transparent)
                            .clickable { viewModel.updateProcessingSettings(settings.copy(engine = ProcessingEngine.GPU_HARDWARE)) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioButton(
                            selected = settings.engine == ProcessingEngine.GPU_HARDWARE,
                            onClick = { viewModel.updateProcessingSettings(settings.copy(engine = ProcessingEngine.GPU_HARDWARE)) },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentCyan, unselectedColor = ImmersiveTextSecondary)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "GPU Hardware Acceleration",
                                    color = ImmersiveTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RectangleShape,
                                    color = AccentCyan.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "RECOMMENDED",
                                        color = AccentCyan,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "MediaCodec hardware surface encoder for fastest throughput and minimum power draw.",
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
                            .background(if (settings.engine == ProcessingEngine.CPU_MULTITHREAD) ImmersiveActionBg else Color.Transparent)
                            .clickable { viewModel.updateProcessingSettings(settings.copy(engine = ProcessingEngine.CPU_MULTITHREAD)) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioButton(
                            selected = settings.engine == ProcessingEngine.CPU_MULTITHREAD,
                            onClick = { viewModel.updateProcessingSettings(settings.copy(engine = ProcessingEngine.CPU_MULTITHREAD)) },
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
                                text = "Multi-threaded worker threads for pixel-exact fallback software rasterization.",
                                color = ImmersiveTextSecondary,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // CPU Threads Picker if CPU mode is active
                    if (settings.engine == ProcessingEngine.CPU_MULTITHREAD) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Allocated Worker Threads:", color = ImmersiveTextSecondary, fontSize = 11.sp)
                                Text("${settings.cpuThreads} Threads", color = ImmersivePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(2, 4, 8).forEach { count ->
                                    Surface(
                                        shape = RectangleShape,
                                        color = if (settings.cpuThreads == count) ImmersivePrimary else ImmersiveActionBg,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.updateProcessingSettings(settings.copy(cpuThreads = count)) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                            Text(
                                                text = "$count Threads",
                                                color = if (settings.cpuThreads == count) Color.Black else ImmersiveTextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Export Defaults
        item {
            Text(
                text = "DEFAULT EXPORT PREFERENCES",
                color = ImmersiveTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))

            Card(
                shape = RectangleShape,
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Container format
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Default Container Format", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Preferred video container format", color = ImmersiveTextSecondary, fontSize = 10.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("mp4", "mkv").forEach { ext ->
                                val selected = settings.defaultExportContainer.equals(ext, ignoreCase = true)
                                Surface(
                                    shape = RectangleShape,
                                    color = if (selected) AccentCyan else ImmersiveActionBg,
                                    modifier = Modifier.clickable {
                                        viewModel.updateProcessingSettings(settings.copy(defaultExportContainer = ext))
                                    }
                                ) {
                                    Text(
                                        text = ext.uppercase(),
                                        color = if (selected) Color.Black else ImmersiveTextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Target Frame Rate (FPS)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Target Frame Rate", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Default export encoding rate", color = ImmersiveTextSecondary, fontSize = 10.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(30, 60).forEach { fps ->
                                val selected = settings.targetFps == fps
                                Surface(
                                    shape = RectangleShape,
                                    color = if (selected) AccentCyan else ImmersiveActionBg,
                                    modifier = Modifier.clickable {
                                        viewModel.updateProcessingSettings(settings.copy(targetFps = fps))
                                    }
                                ) {
                                    Text(
                                        text = "$fps FPS",
                                        color = if (selected) Color.Black else ImmersiveTextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quality Preset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Default Encoding Profile", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Native ultra (20Mbps) or 1080p", color = ImmersiveTextSecondary, fontSize = 10.sp)
                        }

                        Surface(
                            shape = RectangleShape,
                            color = ImmersiveActionBg
                        ) {
                            Text(
                                text = settings.defaultQuality.resolutionName,
                                color = ImmersivePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Auto-Save Project Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Save Project State", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Saves state on every edit event", color = ImmersiveTextSecondary, fontSize = 10.sp)
                        }
                        Switch(
                            checked = settings.autoSaveProject,
                            onCheckedChange = { isChecked ->
                                viewModel.updateProcessingSettings(settings.copy(autoSaveProject = isChecked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentCyan,
                                checkedTrackColor = AccentCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = ImmersiveTextSecondary,
                                uncheckedTrackColor = ImmersiveActionBg
                            )
                        )
                    }

                    // Hardware Surface Rendering Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hardware Surface Rendering", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("OpenGL buffer streaming for playback", color = ImmersiveTextSecondary, fontSize = 10.sp)
                        }
                        Switch(
                            checked = settings.enableHardwareSurface,
                            onCheckedChange = { isChecked ->
                                viewModel.updateProcessingSettings(settings.copy(enableHardwareSurface = isChecked))
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

        // 4. App Info & Zero Demo Status
        item {
            Card(
                shape = RectangleShape,
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(StudioIcons.Info, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(16.dp))
                        Text("SubVid Studio v${com.example.BuildConfig.VERSION_NAME} • Pro Build", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Clean Studio Edition: All sample media removed. Fully ready for your custom MP4, MKV, WebM videos and SRT, WebVTT, ASS/SSA subtitle files.",
                        color = ImmersiveTextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(60.dp))
        }
    }
}
