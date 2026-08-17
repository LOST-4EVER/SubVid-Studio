package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.model.ExportConfig
import com.example.model.ExportMode
import com.example.model.ExportState
import com.example.model.QualityPreset
import com.example.model.SubtitleFormat
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
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
import java.io.File

@Composable
fun ExportDialog(
    exportState: ExportState,
    onDismiss: () -> Unit,
    onStartExport: (ExportConfig) -> Unit,
    onResetState: () -> Unit
) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(ExportMode.HARDCODE_BURN_IN) }
    var selectedQuality by remember { mutableStateOf(QualityPreset.NATIVE_ULTRA) }
    var containerFormat by remember { mutableStateOf("mp4") }
    var subtitleFormat by remember { mutableStateOf(SubtitleFormat.SRT) }

    Dialog(onDismissRequest = {
        if (exportState !is ExportState.Exporting) {
            onResetState()
            onDismiss()
        }
    }) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("export_dialog")
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
                            Icon(StudioIcons.Export, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Export Video & Subtitles", color = ImmersiveTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Ultra-High Native Bitrate Pipeline", color = ImmersiveTextMuted, fontSize = 12.sp)
                        }
                    }

                    if (exportState !is ExportState.Exporting) {
                        IconButton(onClick = {
                            onResetState()
                            onDismiss()
                        }) {
                            Icon(StudioIcons.Close, contentDescription = "Close", tint = ImmersiveTextSecondary)
                        }
                    }
                }

                when (exportState) {
                    is ExportState.Idle -> {
                        // Export Mode Selection Cards
                        Text("SELECT EXPORT MODE", color = ImmersivePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                        ExportMode.values().forEach { mode ->
                            val isSelected = selectedMode == mode
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) ImmersivePrimary.copy(alpha = 0.15f) else ImmersiveBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ImmersivePrimary else ImmersiveBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMode = mode }
                                    .testTag("export_mode_${mode.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedMode = mode },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = ImmersivePrimary,
                                            unselectedColor = ImmersiveTextSecondary
                                        )
                                    )
                                    Column {
                                        Text(mode.title, color = ImmersiveTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(mode.description, color = ImmersiveTextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                                    }
                                }
                            }
                        }

                        // Options for Burn-In
                        if (selectedMode == ExportMode.HARDCODE_BURN_IN) {
                            Text("QUALITY & BITRATE PRESET", color = ImmersivePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            QualityPreset.values().forEach { preset ->
                                val isSelected = selectedQuality == preset
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) ImmersiveActionBg else ImmersiveBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) AccentCyan else ImmersiveBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedQuality = preset }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(preset.label, color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text("${preset.targetBitrateBps / 1_000_000} Mbps", color = AccentCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }

                        // Options for Lossless Mux
                        if (selectedMode == ExportMode.LOSSLESS_SOFT_MUX) {
                            Text("TARGET CONTAINER", color = ImmersivePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                listOf("mp4" to "MP4 Container", "mkv" to "Matroska (MKV)").forEach { (ext, label) ->
                                    val isSelected = containerFormat == ext
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) ImmersivePrimary else ImmersiveActionBg,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { containerFormat = ext }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                            Text(label, color = if (isSelected) ImmersiveOnPrimary else ImmersiveTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Options for Subtitle Only
                        if (selectedMode == ExportMode.SUBTITLE_ONLY) {
                            Text("SUBTITLE FORMAT", color = ImmersivePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(SubtitleFormat.SRT, SubtitleFormat.VTT, SubtitleFormat.ASS).forEach { fmt ->
                                    val isSelected = subtitleFormat == fmt
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) ImmersivePrimary else ImmersiveActionBg,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { subtitleFormat = fmt }
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                            Text(fmt.displayName.substringBefore(" "), color = if (isSelected) ImmersiveOnPrimary else ImmersiveTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Start Export Button
                        Button(
                            onClick = {
                                val config = ExportConfig(
                                    exportMode = selectedMode,
                                    qualityPreset = selectedQuality,
                                    containerFormat = containerFormat,
                                    subtitleExportFormat = subtitleFormat
                                )
                                onStartExport(config)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ImmersivePrimary,
                                contentColor = ImmersiveOnPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_export_button")
                        ) {
                            Icon(StudioIcons.Export, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Export Now (High Quality)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    is ExportState.Exporting -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = exportState.message,
                                color = ImmersiveTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            LinearProgressIndicator(
                                progress = { exportState.progress },
                                color = ImmersivePrimary,
                                trackColor = ImmersiveActionBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .border(1.dp, ImmersiveBorder, RoundedCornerShape(4.dp))
                            )

                            Text(
                                text = "${(exportState.progress * 100).toInt()}% completed",
                                color = AccentCyan,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    is ExportState.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(AccentEmerald.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(StudioIcons.Check, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(32.dp))
                            }

                            Text("Export Completed Successfully!", color = ImmersiveTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("File Size: ${exportState.fileSizeFormatted}", color = AccentCyan, fontSize = 13.sp)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ImmersiveBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = exportState.outputFilePath,
                                    color = ImmersiveTextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // Share file via standard Android intent
                                        val file = File(exportState.outputFilePath)
                                        if (file.exists()) {
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.provider",
                                                file
                                            )
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = if (file.name.endsWith(".mp4") || file.name.endsWith(".mkv")) "video/*" else "text/plain"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Exported Media"))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ImmersivePrimary,
                                        contentColor = ImmersiveOnPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Share File", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        onResetState()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ImmersiveActionBg,
                                        contentColor = ImmersiveTextPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Done")
                                }
                            }
                        }
                    }

                    is ExportState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(AccentRose.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(StudioIcons.Close, contentDescription = null, tint = AccentRose, modifier = Modifier.size(32.dp))
                            }

                            Text("Export Error", color = AccentRose, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(exportState.errorMessage, color = ImmersiveTextSecondary, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

                            Button(
                                onClick = onResetState,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ImmersivePrimary,
                                    contentColor = ImmersiveOnPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
}
