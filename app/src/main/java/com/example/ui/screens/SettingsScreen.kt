package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.settings.SettingsDiagnosticsDialog
import com.example.ui.components.settings.SettingsEngineSection
import com.example.ui.components.settings.SettingsExportSection
import com.example.ui.components.settings.SettingsSubtitleDefaultsSection
import com.example.ui.components.settings.SettingsTimelineSection
import com.example.ui.icons.StudioIcons
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
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.processingSettings.collectAsState()
    val videoMetadata by viewModel.videoMetadata.collectAsState()
    val subtitleTrack by viewModel.subtitleTrack.collectAsState()
    val showDiagnostics by viewModel.showDiagnosticsDialog.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ImmersivePrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(StudioIcons.Settings, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(20.dp))
                        }

                        Column {
                            Text(
                                text = "Settings & Preferences",
                                color = ImmersiveTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hardware engine, timeline & styling",
                                color = ImmersiveTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ImmersiveActionBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        modifier = Modifier.clickable { viewModel.setShowDiagnosticsDialog(true) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(StudioIcons.Diagnostics, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                            Text("Diagnostics", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Modular Engine Section
        item {
            SettingsEngineSection(
                settings = settings,
                onUpdateSettings = { viewModel.updateProcessingSettings(it) }
            )
        }

        // 3. Modular Export Section
        item {
            SettingsExportSection(
                settings = settings,
                onUpdateSettings = { viewModel.updateProcessingSettings(it) }
            )
        }

        // 4. Subtitle Typography & Defaults Section
        item {
            SettingsSubtitleDefaultsSection(
                settings = settings,
                onUpdateSettings = { viewModel.updateProcessingSettings(it) }
            )
        }

        // 5. Timeline & Scrubber Section
        item {
            SettingsTimelineSection(
                settings = settings,
                onUpdateSettings = { viewModel.updateProcessingSettings(it) }
            )
        }

        // 6. Maintenance & Cache Actions
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("STUDIO MAINTENANCE", color = ImmersiveTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearTemporaryCache() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = ImmersiveActionBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(StudioIcons.Delete, contentDescription = null, tint = AccentRose, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Purge Cache", color = AccentRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetSettingsToDefault() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = ImmersiveActionBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(StudioIcons.Refresh, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reset Defaults", color = ImmersivePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 7. App Info Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(StudioIcons.Info, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(16.dp))
                        Text("SubVid Studio v${com.example.BuildConfig.VERSION_NAME} • Pro Studio Edition", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Hardware-accelerated subtitle synchronizer, parser, and video hardcoder. Built with Android MediaCodec, Jetpack Compose Material 3, and Kotlin Coroutines.",
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

    // Diagnostics Dialog
    if (showDiagnostics) {
        SettingsDiagnosticsDialog(
            videoMetadata = videoMetadata,
            subtitleTrack = subtitleTrack,
            settings = settings,
            onDismiss = { viewModel.setShowDiagnosticsDialog(false) },
            onClearCache = { viewModel.clearTemporaryCache() },
            onResetDefaults = { viewModel.resetSettingsToDefault() }
        )
    }
}
