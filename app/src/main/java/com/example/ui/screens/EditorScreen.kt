package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubtitleFormat
import com.example.model.SubtitleStyle
import com.example.ui.components.EditorCueInspector
import com.example.ui.components.EditorMediaHeader
import com.example.ui.components.EditorToolbeltStrip
import com.example.ui.components.ExportDialog
import com.example.ui.components.FullScreenVideoPlayerView
import com.example.ui.components.SubtitleListSheet
import com.example.ui.components.SubtitlePlacementDialog
import com.example.ui.components.SubtitleStyleDialog
import com.example.ui.components.TimelineScrubberView
import com.example.ui.components.VideoPlayerView
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun EditorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoMetadata by viewModel.videoMetadata.collectAsState()
    val subtitleTrack by viewModel.subtitleTrack.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val selectedCue by viewModel.selectedCue.collectAsState()
    val activeCue by viewModel.activeCue.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()

    val showPlacementDialog by viewModel.showPlacementDialog.collectAsState()
    val showStyleDialog by viewModel.showStyleDialog.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val showSubtitleListSheet by viewModel.showSubtitleListSheet.collectAsState()
    val isFullscreenVideo by viewModel.isFullscreenVideo.collectAsState()

    var pendingExportFormat by remember { mutableStateOf(SubtitleFormat.SRT) }

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadVideoFromUri(it) }
    }

    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadSubtitleFromUri(it) }
    }

    val saveSubtitleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportSubtitleToFile(it, pendingExportFormat) }
    }

    if (isFullscreenVideo && videoMetadata.uriString.isNotEmpty()) {
        FullScreenVideoPlayerView(
            playerController = viewModel.playerController,
            playerState = playerState,
            videoMetadata = videoMetadata,
            subtitleTrack = subtitleTrack,
            activeCue = activeCue ?: selectedCue,
            onSubtitlePositionChanged = { newX, newY ->
                viewModel.updateSubtitlePosition(newX, newY)
            },
            onOpenStyleDialog = { viewModel.setShowStyleDialog(true) },
            onOpenPlacementDialog = { viewModel.setShowPlacementDialog(true) },
            onOpenSubtitleList = { viewModel.setShowSubtitleListSheet(true) },
            onExitFullscreen = { viewModel.setFullscreenVideo(false) }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ImmersiveBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Modular Studio Header with Quick Media Actions
            EditorMediaHeader(
                videoFileName = videoMetadata.fileName,
                hasVideoLoaded = videoMetadata.uriString.isNotEmpty(),
                subtitleTrack = subtitleTrack,
                onLoadVideoClick = { videoPickerLauncher.launch("video/*") },
                onUnloadVideoClick = { viewModel.unloadVideo() },
                onLoadSubtitleClick = { subtitlePickerLauncher.launch("*/*") },
                onCreateNewSubtitleTrack = { viewModel.createNewSubtitleTrack() },
                onSaveSubtitleTrack = { format ->
                    pendingExportFormat = format
                    val ext = format.extension
                    saveSubtitleLauncher.launch("${subtitleTrack.title.ifEmpty { "subtitles" }}.$ext")
                },
                onUnloadSubtitleTrack = { viewModel.unloadSubtitleTrack() },
                onBackClick = { viewModel.setTab(com.example.model.AppTab.HOME) }
            )

            // 2. Video Viewport Canvas OR Empty Video Placeholder Card
            if (videoMetadata.uriString.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ImmersiveActionBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(StudioIcons.Video, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(24.dp))
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Add your video file to start editing",
                            color = ImmersiveTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Supports MP4, MKV, WebM, MOV, and AVI formats",
                            color = ImmersiveTextSecondary,
                            fontSize = 10.sp
                        )

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(StudioIcons.Import, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add Video File", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Live Video Player Canvas with Interactive Drag Overlay
                VideoPlayerView(
                    playerController = viewModel.playerController,
                    playerState = playerState,
                    activeCue = activeCue ?: selectedCue,
                    isDraggingSubtitle = false,
                    onSubtitlePositionChanged = { newX, newY ->
                        viewModel.updateSubtitlePosition(newX, newY)
                    },
                    onSubtitleTapped = {
                        viewModel.setShowPlacementDialog(true)
                    },
                    onToggleFullscreen = {
                        viewModel.setFullscreenVideo(true)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            // 3. Pro Multi-Track Waveform & Timeline Scrubber
            TimelineScrubberView(
                currentPositionMs = playerState.currentPositionMs,
                durationMs = playerState.durationMs,
                subtitleTrack = subtitleTrack,
                selectedCue = selectedCue,
                onSeek = { ms -> viewModel.playerController.seekTo(ms) },
                onSelectCue = { cue -> viewModel.selectCue(cue) },
                onAddCueAtCurrentPosition = { viewModel.addCueAtCurrentPosition() },
                onSplitCueAtCurrentPosition = { viewModel.splitCueAtCurrentPosition() },
                onDeleteSelectedCue = { viewModel.deleteSelectedCue() },
                onNudgeTiming = { dStart, dEnd -> viewModel.nudgeTiming(dStart, dEnd) },
                onSetCueStartTime = { cue, newStart -> viewModel.setCueStartTime(cue, newStart) },
                onSetCueEndTime = { cue, newEnd -> viewModel.setCueEndTime(cue, newEnd) },
                onShiftCueTiming = { cue, delta -> viewModel.shiftCueTiming(cue, delta) }
            )

            // 4. Scrollable bottom sheet section for inspector & action belt
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 4. Live Selected Cue Inspector Component
                val currentSelected = selectedCue ?: activeCue
                if (currentSelected != null) {
                    val currentIdx = subtitleTrack.cues.indexOfFirst { it.id == currentSelected.id }
                    EditorCueInspector(
                        selectedCue = currentSelected,
                        totalCuesCount = subtitleTrack.cues.size,
                        currentCueIndex = currentIdx,
                        onSelectPreviousCue = { viewModel.selectPreviousCue() },
                        onSelectNextCue = { viewModel.selectNextCue() },
                        onDuplicateCue = { viewModel.duplicateSelectedCue() },
                        onUpdateCueText = { cue, text -> viewModel.updateCueText(cue, text) }
                    )
                }

                // 5. Studio Action Strip / Toolbelt Component
                EditorToolbeltStrip(
                    onPlacementClick = { viewModel.setShowPlacementDialog(true) },
                    onStyleClick = { viewModel.setShowStyleDialog(true) },
                    onSubtitlesListClick = { viewModel.setShowSubtitleListSheet(true) },
                    onExportClick = { viewModel.setShowExportDialog(true) },
                    onFullscreenClick = if (videoMetadata.uriString.isNotEmpty()) {
                        { viewModel.setFullscreenVideo(true) }
                    } else null
                )

                // Bottom spacing for navigation bar
                Spacer(Modifier.height(60.dp))
            }
        }
    }

    // Modal Dialogs
    if (showPlacementDialog) {
        SubtitlePlacementDialog(
            initialCue = selectedCue ?: activeCue,
            onDismiss = { viewModel.setShowPlacementDialog(false) },
            onApply = { posX, posY, alignment, applyToAll ->
                viewModel.updateSubtitlePosition(posX, posY, alignment, applyToAll)
            }
        )
    }

    if (showStyleDialog) {
        val currentStyle = (selectedCue ?: activeCue)?.style ?: SubtitleStyle()
        SubtitleStyleDialog(
            currentStyle = currentStyle,
            onDismiss = { viewModel.setShowStyleDialog(false) },
            onApplyStyle = { style, applyToAll ->
                viewModel.updateSubtitleStyle(style, applyToAll)
            }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            exportState = exportState,
            onDismiss = { viewModel.setShowExportDialog(false) },
            onStartExport = { config -> viewModel.startExport(config) },
            onResetState = { viewModel.resetExportState() }
        )
    }

    if (showSubtitleListSheet) {
        SubtitleListSheet(
            subtitleTrack = subtitleTrack,
            currentPositionMs = playerState.currentPositionMs,
            selectedCue = selectedCue,
            onDismiss = { viewModel.setShowSubtitleListSheet(false) },
            onSelectCue = { cue -> viewModel.selectCue(cue) },
            onJumpToCue = { cue -> viewModel.jumpToCue(cue) },
            onUpdateCueText = { cue, newText -> viewModel.updateCueText(cue, newText) },
            onDeleteCue = { cue -> viewModel.deleteCue(cue) },
            onAddNewCue = { viewModel.addCueAtCurrentPosition() },
            onBatchShiftTiming = { delta -> viewModel.batchShiftTiming(delta) }
        )
    }
}
