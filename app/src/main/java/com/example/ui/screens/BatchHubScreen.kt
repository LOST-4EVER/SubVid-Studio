package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatchOperationType
import com.example.model.BatchTask
import com.example.model.BatchTaskStatus
import com.example.model.ProcessingEngine
import com.example.model.SubtitleFormat
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
fun BatchHubScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.batchTasks.collectAsState()
    val isRunning by viewModel.isBatchRunning.collectAsState()
    val settings by viewModel.processingSettings.collectAsState()

    var showShiftDialog by remember { mutableStateOf(false) }
    var shiftMsInput by remember { mutableStateOf("500") }
    var selectedFormatForBatch by remember { mutableStateOf(SubtitleFormat.SRT) }

    // Multi-file picker for format conversion
    val convertMultiPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addBatchSubtitleConversion(uris, selectedFormatForBatch)
        }
    }

    // Multi-file picker for timing shift
    val shiftMultiPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val delta = shiftMsInput.toLongOrNull() ?: 500L
            viewModel.addBatchTimingShift(uris, delta)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Hub Header & Engine Switcher
        item {
            Surface(
                shape = RectangleShape,
                color = ImmersiveSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                .background(AccentEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(StudioIcons.BatchQueue, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(18.dp))
                            }

                            Column {
                                Text(
                                    text = "Batch Processing Hub",
                                    color = ImmersiveTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Multi-file conversion & sync queue",
                                    color = ImmersiveTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Processing Mode Badge / Toggle
                        Surface(
                            shape = RectangleShape,
                            color = if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan.copy(alpha = 0.15f) else ImmersivePrimary.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan.copy(alpha = 0.4f) else ImmersivePrimary.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.clickable {
                                val nextEngine = if (settings.engine == ProcessingEngine.GPU_HARDWARE) {
                                    ProcessingEngine.CPU_MULTITHREAD
                                } else {
                                    ProcessingEngine.GPU_HARDWARE
                                }
                                viewModel.updateProcessingSettings(settings.copy(engine = nextEngine))
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (settings.engine == ProcessingEngine.GPU_HARDWARE) StudioIcons.Gpu else StudioIcons.Cpu,
                                    contentDescription = null,
                                    tint = if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan else ImmersivePrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (settings.engine == ProcessingEngine.GPU_HARDWARE) "GPU Engine" else "CPU (${settings.cpuThreads} threads)",
                                    color = if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan else ImmersivePrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Add to Batch Quick Actions
        item {
            Text(
                text = "QUEUE OPERATIONS",
                color = ImmersiveTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Batch Convert SRT/VTT/ASS Card
                Card(
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { convertMultiPicker.launch("*/*") }
                        .testTag("batch_add_convert_btn")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(ImmersivePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(StudioIcons.SyncAudio, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(16.dp))
                            }

                            // Target format selector pill
                            Surface(
                                shape = RectangleShape,
                                color = AccentCyan.copy(alpha = 0.2f),
                                modifier = Modifier.clickable {
                                    selectedFormatForBatch = when (selectedFormatForBatch) {
                                        SubtitleFormat.SRT -> SubtitleFormat.VTT
                                        SubtitleFormat.VTT -> SubtitleFormat.ASS
                                        SubtitleFormat.ASS -> SubtitleFormat.SRT
                                        SubtitleFormat.SSA -> SubtitleFormat.SRT
                                    }
                                }
                            ) {
                                Text(
                                    text = "➔ ${selectedFormatForBatch.extension.uppercase()}",
                                    color = AccentCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text("Batch Convert", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Select files to convert", color = ImmersiveTextSecondary, fontSize = 10.sp)
                    }
                }

                // Batch Time Shift Card
                Card(
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showShiftDialog = true }
                        .testTag("batch_add_shift_btn")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(AccentEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(StudioIcons.Timer, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                        }

                        Text("Batch Time Shift", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Offset timestamps +/- ms", color = ImmersiveTextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }

        // 3. Execution Bar (Start Batch / Cancel / Clear)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "TASK QUEUE",
                        color = ImmersiveTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = RectangleShape,
                        color = ImmersiveActionBg
                    ) {
                        Text(
                            text = "${tasks.size}",
                            color = ImmersivePrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (tasks.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearBatchTasks() },
                            enabled = !isRunning,
                            shape = RectangleShape
                        ) {
                            Text("Clear", color = ImmersiveTextSecondary, fontSize = 11.sp)
                        }
                    }

                    if (isRunning) {
                        Button(
                            onClick = { viewModel.cancelBatchProcessing() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                            shape = RectangleShape,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(StudioIcons.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Stop", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startBatchProcessing() },
                            enabled = tasks.any { it.status == BatchTaskStatus.PENDING || it.status == BatchTaskStatus.FAILED },
                            colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                            shape = RectangleShape,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("start_batch_button")
                        ) {
                            Icon(StudioIcons.Play, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Process All", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Task Queue Items
        if (tasks.isEmpty()) {
            item {
                Surface(
                    shape = RectangleShape,
                    color = ImmersiveSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = StudioIcons.BatchQueue,
                            contentDescription = null,
                            tint = ImmersiveTextSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Batch queue is empty",
                            color = ImmersiveTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add subtitle or video files above to batch convert formats, shift timings, or encode simultaneously.",
                            color = ImmersiveTextSecondary,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                BatchTaskItemCard(task = task)
            }
        }

        item {
            Spacer(Modifier.height(60.dp))
        }
    }

    // Time Shift Config Dialog
    if (showShiftDialog) {
        AlertDialog(
            onDismissRequest = { showShiftDialog = false },
            containerColor = ImmersiveSurface,
            shape = RectangleShape,
            title = {
                Text("Batch Timing Offset (Audio Sync)", color = ImmersiveTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter offset in milliseconds (+500 for delay, -500 for earlier):", color = ImmersiveTextSecondary, fontSize = 11.sp)
                    OutlinedTextField(
                        value = shiftMsInput,
                        onValueChange = { shiftMsInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ImmersiveTextPrimary,
                            unfocusedTextColor = ImmersiveTextPrimary,
                            focusedBorderColor = ImmersivePrimary,
                            unfocusedBorderColor = ImmersiveBorder,
                            focusedContainerColor = ImmersiveBg,
                            unfocusedContainerColor = ImmersiveBg
                        ),
                        shape = RectangleShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShiftDialog = false
                        shiftMultiPicker.launch("*/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                    shape = RectangleShape
                ) {
                    Text("Select Files", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showShiftDialog = false }, shape = RectangleShape) {
                    Text("Cancel", color = ImmersiveTextSecondary, fontSize = 11.sp)
                }
            }
        )
    }
}

@Composable
fun BatchTaskItemCard(
    task: BatchTask,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    color = ImmersiveTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                // Status Pill
                val (statusText, statusBg, statusColor) = when (task.status) {
                    BatchTaskStatus.PENDING -> Triple("Pending", ImmersiveActionBg, ImmersiveTextSecondary)
                    BatchTaskStatus.PROCESSING -> Triple("Processing", AccentCyan.copy(alpha = 0.2f), AccentCyan)
                    BatchTaskStatus.COMPLETED -> Triple("Done", AccentEmerald.copy(alpha = 0.2f), AccentEmerald)
                    BatchTaskStatus.FAILED -> Triple("Failed", AccentRose.copy(alpha = 0.2f), AccentRose)
                }

                Surface(
                    shape = RectangleShape,
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (task.status == BatchTaskStatus.PROCESSING) {
                LinearProgressIndicator(
                    progress = { task.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = AccentCyan,
                    trackColor = ImmersiveActionBg
                )
            }

            if (task.resultPath != null) {
                Text(
                    text = "Saved: ${task.resultPath.substringAfterLast("/")}",
                    color = AccentEmerald,
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            } else if (task.errorMessage != null) {
                Text(
                    text = task.errorMessage,
                    color = AccentRose,
                    fontSize = 10.sp
                )
            }
        }
    }
}
