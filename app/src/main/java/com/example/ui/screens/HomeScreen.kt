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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.model.ProcessingEngine
import com.example.model.StudioProject
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
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val settings by viewModel.processingSettings.collectAsState()
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var projectToDelete by remember { mutableStateOf<StudioProject?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.loadVideoFromUri(it)
        }
    }

    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.loadSubtitleFromUri(it)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Sleek Compact Header & Engine Status
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ImmersiveSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(ImmersivePrimary, AccentCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = StudioIcons.Video,
                                contentDescription = "SubVid Studio",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "SubVid Studio",
                                color = ImmersiveTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Precision Subtitle Editor & Muxer",
                                color = ImmersiveTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Engine Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan.copy(alpha = 0.15f) else ImmersivePrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan.copy(alpha = 0.4f) else ImmersivePrimary.copy(alpha = 0.4f)
                        )
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
                                text = if (settings.engine == ProcessingEngine.GPU_HARDWARE) "GPU Active" else "CPU Multi-Core",
                                color = if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan else ImmersivePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Quick Action Cards (Add Video, Import Subtitle, Batch Hub)
        item {
            Text(
                text = "QUICK ACTIONS",
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
                // Import Video Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { videoPickerLauncher.launch("video/*") }
                        .testTag("home_import_video_btn")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ImmersivePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(StudioIcons.Video, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "Add Video File",
                            color = ImmersiveTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MP4, MKV, WebM",
                            color = ImmersiveTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Import Subtitle Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { subtitlePickerLauncher.launch("*/*") }
                        .testTag("home_import_sub_btn")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(StudioIcons.Subtitles, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "Add Subtitle",
                            color = ImmersiveTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "SRT, VTT, ASS",
                            color = ImmersiveTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Batch Hub Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(AppTab.BATCH) }
                        .testTag("home_batch_hub_btn")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(StudioIcons.BatchQueue, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "Batch Hub",
                            color = ImmersiveTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Multi-file Queue",
                            color = ImmersiveTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // 3. Projects Header & "+ New Project" Button
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
                        text = "PROJECTS",
                        color = ImmersiveTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = CircleShape,
                        color = ImmersiveActionBg
                    ) {
                        Text(
                            text = "${projects.size}",
                            color = ImmersivePrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ImmersivePrimary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersivePrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable {
                        newProjectName = "Project ${projects.size + 1}"
                        showNewProjectDialog = true
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(StudioIcons.Add, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(13.dp))
                        Text("+ New Project", color = ImmersivePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Projects List or Prompt to Add File
        if (projects.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ImmersiveSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(ImmersiveActionBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = StudioIcons.FolderOpen,
                                contentDescription = null,
                                tint = ImmersivePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "Add your file to begin editing",
                            color = ImmersiveTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "No projects yet. Tap below to add your video file (MP4, MKV, WebM) or subtitle track (SRT, VTT, ASS).",
                            color = ImmersiveTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { videoPickerLauncher.launch("video/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(StudioIcons.Video, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Add Video", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ImmersiveActionBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                                modifier = Modifier.clickable {
                                    viewModel.createNewProject("Project 1")
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(StudioIcons.Add, contentDescription = null, tint = ImmersiveTextPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("New Blank Project", color = ImmersiveTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            items(projects, key = { it.id }) { project ->
                ProjectListItemCard(
                    project = project,
                    onOpen = { viewModel.openProject(project) },
                    onDelete = { projectToDelete = project }
                )
            }
        }

        // Space at bottom for navigation bar
        item {
            Spacer(Modifier.height(60.dp))
        }
    }

    // New Project Name Dialog
    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            containerColor = ImmersiveSurface,
            title = {
                Text("Create New Project", color = ImmersiveTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a name for your subtitle project:", color = ImmersiveTextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
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
                        if (newProjectName.isNotBlank()) {
                            viewModel.createNewProject(newProjectName.trim())
                            showNewProjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Create & Open", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel", color = ImmersiveTextSecondary, fontSize = 12.sp)
                }
            }
        )
    }

    // Delete Project Confirmation Dialog
    val toDelete = projectToDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            containerColor = ImmersiveSurface,
            title = {
                Text("Delete Project?", color = ImmersiveTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${toDelete.name}'? This action cannot be undone.",
                    color = ImmersiveTextSecondary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(toDelete.id)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = ImmersiveTextSecondary, fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
fun ProjectListItemCard(
    project: StudioProject,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Project Thumbnail / Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (project.hasVideo) ImmersiveSurfaceCard else ImmersiveActionBg)
                        .border(1.dp, ImmersiveBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (project.hasVideo) StudioIcons.Video else StudioIcons.Folder,
                        contentDescription = null,
                        tint = if (project.hasVideo) ImmersivePrimary else ImmersiveTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Project Details
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = project.name,
                        color = ImmersiveTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AccentCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = project.subtitleFormat.extension.uppercase(),
                                color = AccentCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Text(
                            text = if (project.cueCount > 0) "${project.cueCount} cues" else "0 cues",
                            color = ImmersiveTextSecondary,
                            fontSize = 10.sp
                        )

                        Text(
                            text = "•",
                            color = ImmersiveTextSecondary,
                            fontSize = 10.sp
                        )

                        Text(
                            text = project.formattedDate,
                            color = ImmersiveTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ImmersivePrimary,
                    modifier = Modifier.clickable { onOpen() }
                ) {
                    Text(
                        text = "Open",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = StudioIcons.Delete,
                        contentDescription = "Delete Project",
                        tint = AccentRose.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
