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
import com.example.model.AppTab
import com.example.model.ProcessingEngine
import com.example.model.StudioProject
import com.example.ui.components.dialogs.QuickPresetStyleSheet
import com.example.ui.components.home.HomeHeroBanner
import com.example.ui.components.home.HomeProjectCard
import com.example.ui.components.home.HomeQuickToolsRow
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
    var showStylePacksSheet by remember { mutableStateOf(false) }

    val videoMetadata by viewModel.videoMetadata.collectAsState()
    val subtitleTrack by viewModel.subtitleTrack.collectAsState()
    val hasActiveSession = videoMetadata.uriString.isNotEmpty() || subtitleTrack.cues.isNotEmpty()

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.loadVideoFromUri(it)
            onNavigateTab(AppTab.EDITOR)
        }
    }

    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.loadSubtitleFromUri(it)
            onNavigateTab(AppTab.EDITOR)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ImmersivePrimary, RoundedCornerShape(8.dp)),
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
                        shape = RoundedCornerShape(6.dp),
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

        // Hero Studio Banner Card
        item {
            HomeHeroBanner(
                onNewProject = {
                    newProjectName = "Project ${projects.size + 1}"
                    showNewProjectDialog = true
                },
                onImportVideo = { videoPickerLauncher.launch("video/*") },
                onImportSubtitle = { subtitlePickerLauncher.launch("*/*") }
            )
        }

        // Active Session Resume Banner (if media or subtitles loaded)
        if (hasActiveSession) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersivePrimary.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, ImmersivePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateTab(AppTab.EDITOR) }
                        .testTag("home_resume_session_banner")
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(ImmersivePrimary, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = StudioIcons.Play,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Active Session in Progress",
                                    color = ImmersivePrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (videoMetadata.fileName.isNotEmpty()) {
                                        "${videoMetadata.fileName} • ${subtitleTrack.cues.size} Cues"
                                    } else {
                                        "${subtitleTrack.cues.size} Cues • ${subtitleTrack.format.extension.uppercase()}"
                                    },
                                    color = ImmersiveTextPrimary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = { onNavigateTab(AppTab.EDITOR) },
                            colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Resume", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Quick Action Cards (Add Video, Import Subtitle, Batch Hub, Style Packs)
        item {
            Text(
                text = "QUICK ACTIONS & TOOLS",
                color = ImmersiveTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))

            HomeQuickToolsRow(
                onImportSubtitle = { subtitlePickerLauncher.launch("*/*") },
                onOpenBatchHub = { onNavigateTab(AppTab.BATCH) },
                onOpenStylePacks = { showStylePacksSheet = true }
            )
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
                        shape = RoundedCornerShape(4.dp),
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
                    shape = RoundedCornerShape(6.dp),
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
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
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
                                .size(48.dp)
                                .background(ImmersiveActionBg, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = StudioIcons.FolderOpen,
                                contentDescription = null,
                                tint = ImmersivePrimary,
                                modifier = Modifier.size(26.dp)
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
                                    onNavigateTab(AppTab.EDITOR)
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
                HomeProjectCard(
                    project = project,
                    onOpen = {
                        viewModel.openProject(project)
                        onNavigateTab(AppTab.EDITOR)
                    },
                    onDelete = { projectToDelete = project }
                )
            }
        }

        // Space at bottom for navigation bar
        item {
            Spacer(Modifier.height(60.dp))
        }
    }

    // Quick Preset Style Sheet
    if (showStylePacksSheet) {
        QuickPresetStyleSheet(
            onDismiss = { showStylePacksSheet = false },
            onSelectPreset = { presetId ->
                viewModel.applyPresetStyle(presetId)
                onNavigateTab(AppTab.EDITOR)
            }
        )
    }

    // New Project Name Dialog
    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            containerColor = ImmersiveSurface,
            shape = RoundedCornerShape(16.dp),
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
                            onNavigateTab(AppTab.EDITOR)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Create & Open", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }, shape = RoundedCornerShape(8.dp)) {
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
            shape = RoundedCornerShape(16.dp),
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
                TextButton(onClick = { projectToDelete = null }, shape = RoundedCornerShape(8.dp)) {
                    Text("Cancel", color = ImmersiveTextSecondary, fontSize = 12.sp)
                }
            }
        )
    }
}
