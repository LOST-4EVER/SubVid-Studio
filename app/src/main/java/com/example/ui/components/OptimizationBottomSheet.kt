package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProcessingEngine
import com.example.model.ProcessingSettings
import com.example.model.QualityPreset
import com.example.model.RenderOptimizationLevel
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceCard
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizationBottomSheet(
    settings: ProcessingSettings,
    onDismiss: () -> Unit,
    onApplySettings: (ProcessingSettings) -> Unit,
    onClearCache: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentSettings by remember { mutableStateOf(settings) }
    var cacheClearedMessage by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ImmersiveSurface,
        shape = RectangleShape,
        dragHandle = {
            Surface(
                shape = RectangleShape,
                color = ImmersiveBorder,
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                            .size(32.dp)
                            .background(AccentCyan.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(StudioIcons.Speed, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = "Engine & Rendering Optimization",
                            color = ImmersiveTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hardware acceleration, memory tuning & high-refresh rates",
                            color = ImmersiveTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(StudioIcons.Close, contentDescription = "Close", tint = ImmersiveTextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            // 1. Performance Profile Preset
            Card(
                shape = RectangleShape,
                colors = CardDefaults.cardColors(containerColor = ImmersiveBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("OPTIMIZATION PROFILE", color = ImmersivePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RenderOptimizationLevel.values().forEach { level ->
                            val isSelected = currentSettings.optimizationLevel == level
                            Surface(
                                shape = RectangleShape,
                                color = if (isSelected) AccentCyan.copy(alpha = 0.25f) else ImmersiveSurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentCyan else ImmersiveBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        currentSettings = currentSettings.copy(
                                            optimizationLevel = level,
                                            targetFps = if (level == RenderOptimizationLevel.HIGH_PERFORMANCE) 60 else 30
                                        )
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = level.label.split(" ").first(),
                                        color = if (isSelected) AccentCyan else ImmersiveTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (level == RenderOptimizationLevel.HIGH_PERFORMANCE) "60 FPS" else if (level == RenderOptimizationLevel.BALANCED) "Auto" else "Eco",
                                        color = ImmersiveTextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Hardware Video Engine (GPU MediaCodec vs CPU Multicore)
            Card(
                shape = RectangleShape,
                colors = CardDefaults.cardColors(containerColor = ImmersiveBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("VIDEO PROCESSING ENGINE", color = ImmersivePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                    ProcessingEngine.values().forEach { engine ->
                        val isSelected = currentSettings.engine == engine
                        Surface(
                            shape = RectangleShape,
                            color = if (isSelected) ImmersivePrimary.copy(alpha = 0.15f) else ImmersiveSurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ImmersivePrimary else ImmersiveBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentSettings = currentSettings.copy(engine = engine) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(engine.title, color = ImmersiveTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Surface(
                                            shape = RectangleShape,
                                            color = if (isSelected) ImmersivePrimary else ImmersiveActionBg
                                        ) {
                                            Text(
                                                engine.badge,
                                                color = if (isSelected) Color.Black else ImmersiveTextSecondary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(engine.description, color = ImmersiveTextMuted, fontSize = 9.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .border(1.5.dp, if (isSelected) ImmersivePrimary else ImmersiveBorder, RectangleShape)
                                        .background(if (isSelected) ImmersivePrimary else Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Toggles Grid: Hardware Surface, Fast Waveform, Low Latency, Anti-Aliasing
            Card(
                shape = RectangleShape,
                colors = CardDefaults.cardColors(containerColor = ImmersiveBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("RENDERING ACCELERATION & MEMORY", color = ImmersivePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                    // Hardware Direct Surface Buffer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Direct Surface Hardware Rendering", color = ImmersiveTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Zero-copy texture streaming directly to display surface", color = ImmersiveTextMuted, fontSize = 9.sp)
                        }
                        Switch(
                            checked = currentSettings.enableHardwareSurface,
                            onCheckedChange = { currentSettings = currentSettings.copy(enableHardwareSurface = it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = AccentCyan,
                                uncheckedTrackColor = ImmersiveActionBg
                            )
                        )
                    }

                    // Fast Waveform Simulation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fast Procedural Timeline Waveform", color = ImmersiveTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Sub-millisecond audio peak rendering without decoding full audio", color = ImmersiveTextMuted, fontSize = 9.sp)
                        }
                        Switch(
                            checked = currentSettings.fastWaveformRendering,
                            onCheckedChange = { currentSettings = currentSettings.copy(fastWaveformRendering = it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = AccentEmerald,
                                uncheckedTrackColor = ImmersiveActionBg
                            )
                        )
                    }

                    // Text Glyph Anti-Aliasing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("High-Precision Subtitle Anti-Aliasing", color = ImmersiveTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Sub-pixel text rasterization with high-DPI text shadow shaders", color = ImmersiveTextMuted, fontSize = 9.sp)
                        }
                        Switch(
                            checked = currentSettings.hardwareTextAntiAliasing,
                            onCheckedChange = { currentSettings = currentSettings.copy(hardwareTextAntiAliasing = it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = ImmersivePrimary,
                                uncheckedTrackColor = ImmersiveActionBg
                            )
                        )
                    }

                    // Memory Cache Size Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Video Frame Memory Cache", color = ImmersiveTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${currentSettings.memoryCacheSizeMb} MB", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = currentSettings.memoryCacheSizeMb.toFloat(),
                            onValueChange = { currentSettings = currentSettings.copy(memoryCacheSizeMb = it.toInt()) },
                            valueRange = 128f..1024f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyan,
                                activeTrackColor = AccentCyan,
                                inactiveTrackColor = ImmersiveActionBg
                            )
                        )
                    }
                }
            }

            // 4. Cache Purge & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RectangleShape,
                    color = ImmersiveActionBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onClearCache()
                            cacheClearedMessage = true
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(StudioIcons.Delete, contentDescription = null, tint = if (cacheClearedMessage) AccentEmerald else AccentRose, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (cacheClearedMessage) "Cache Cleaned!" else "Purge Video Cache",
                            color = if (cacheClearedMessage) AccentEmerald else ImmersiveTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        onApplySettings(currentSettings)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                    shape = RectangleShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(StudioIcons.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Apply & Optimize", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
