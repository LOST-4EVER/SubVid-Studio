package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatchTaskStatus
import com.example.model.ProcessingEngine
import com.example.model.SubtitleFormat
import com.example.ui.components.batch.BatchHeader
import com.example.ui.components.batch.BatchTaskItem
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
        // 1. Modular Header & Processing Engine Badge
        item {
            BatchHeader(
                settings = settings,
                onToggleEngine = {
                    val next = if (settings.engine == ProcessingEngine.GPU_HARDWARE) {
                        ProcessingEngine.CPU_MULTITHREAD
                    } else {
                        ProcessingEngine.GPU_HARDWARE
                    }
                    viewModel.updateProcessingSettings(settings.copy(engine = next))
                }
            )
        }

        // 2. Queue Operations Cards
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
                    shape = RoundedCornerShape(12.dp),
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
                                    .background(AccentCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(StudioIcons.SyncAudio, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
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
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text("Batch Convert", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Select files to convert", color = ImmersiveTextSecondary, fontSize = 10.sp)
                    }
                }

                // Batch Time Shift Card
                Card(
                    shape = RoundedCornerShape(12.dp),
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
                                .background(AccentAmber.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(StudioIcons.Timer, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
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
                        shape = RoundedCornerShape(4.dp),
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
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear", color = ImmersiveTextSecondary, fontSize = 11.sp)
                        }
                    }

                    if (isRunning) {
                        Button(
                            onClick = { viewModel.cancelBatchProcessing() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                            shape = RoundedCornerShape(8.dp),
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
                            shape = RoundedCornerShape(8.dp),
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
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
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
                            imageVector = StudioIcons.Layers,
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
                            text = "Add multiple files above to convert formats or shift timestamps concurrently in the background.",
                            color = ImmersiveTextSecondary,
                            fontSize = 10.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                BatchTaskItem(task = task)
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
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Batch Timestamp Offset", color = ImmersiveTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter offset in milliseconds (positive to delay, negative to advance):", color = ImmersiveTextSecondary, fontSize = 11.sp)
                    OutlinedTextField(
                        value = shiftMsInput,
                        onValueChange = { shiftMsInput = it },
                        label = { Text("Offset (ms)", color = ImmersiveTextSecondary) },
                        placeholder = { Text("e.g. 500 or -250") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ImmersiveTextPrimary,
                            unfocusedTextColor = ImmersiveTextPrimary,
                            focusedBorderColor = ImmersivePrimary,
                            unfocusedBorderColor = ImmersiveBorder,
                            focusedContainerColor = ImmersiveBg,
                            unfocusedContainerColor = ImmersiveBg
                        ),
                        shape = RoundedCornerShape(8.dp),
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
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select Files & Queue", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showShiftDialog = false }, shape = RoundedCornerShape(8.dp)) {
                    Text("Cancel", color = ImmersiveTextSecondary, fontSize = 11.sp)
                }
            }
        )
    }
}
