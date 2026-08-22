package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.model.SubtitleFormat
import com.example.model.SubtitleStyle
import com.example.parser.SubtitleWriter
import com.example.ui.components.EditorCueInspector
import com.example.ui.components.EditorMediaHeader
import com.example.ui.components.EditorToolbeltStrip
import com.example.ui.components.ExportDialog
import com.example.ui.components.FullScreenVideoPlayerView
import com.example.ui.components.OptimizationBottomSheet
import com.example.ui.components.SubtitleListSheet
import com.example.ui.components.SubtitlePlacementDialog
import com.example.ui.components.SubtitleStyleDialog
import com.example.ui.components.TimelineScrubberView
import com.example.ui.components.VideoPlayerView
import com.example.ui.components.dialogs.FindReplaceDialog
import com.example.ui.components.dialogs.QuickPresetStyleSheet
import com.example.ui.components.dialogs.TimingSanitizerDialog
import com.example.ui.theme.ImmersiveBg
import com.example.viewmodel.MainViewModel

@Composable
fun EditorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val playerState by viewModel.playerState.collectAsState()
    val videoMetadata by viewModel.videoMetadata.collectAsState()
    val subtitleTrack by viewModel.subtitleTrack.collectAsState()
    val selectedCue by viewModel.selectedCue.collectAsState()
    val activeCue by viewModel.activeCue.collectAsState()
    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val processingSettings by viewModel.processingSettings.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val isFullscreenVideo by viewModel.isFullscreenVideo.collectAsState()

    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    val showOptimizationSheet by viewModel.showOptimizationSheet.collectAsState()
    val showPlacementDialog by viewModel.showPlacementDialog.collectAsState()
    val showStyleDialog by viewModel.showStyleDialog.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val showSubtitleListSheet by viewModel.showSubtitleListSheet.collectAsState()
    val showFindReplaceDialog by viewModel.showFindReplaceDialog.collectAsState()
    val showSanitizerDialog by viewModel.showSanitizerDialog.collectAsState()

    var showPresetStylesSheet by remember { mutableStateOf(false) }

    var pendingExportFormat by remember { mutableStateOf<SubtitleFormat?>(null) }

    // Media and Subtitle file pickers
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.loadVideoFromUri(it)
            Toast.makeText(context, "Loaded video: ${it.lastPathSegment ?: "media"}", Toast.LENGTH_SHORT).show()
        }
    }

    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.loadSubtitleFromUri(it)
            Toast.makeText(context, "Loaded subtitle file", Toast.LENGTH_SHORT).show()
        }
    }

    // Save Subtitle file creation launcher
    val saveSubtitleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        uri?.let {
            val format = pendingExportFormat ?: SubtitleFormat.SRT
            val textContent = SubtitleWriter.generate(subtitleTrack, format)
            try {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(textContent.toByteArray())
                }
                Toast.makeText(context, "Exported subtitles successfully", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Full-Screen Immersive Video Player Overlay Mode
    if (isFullscreenVideo && videoMetadata.uriString.isNotEmpty()) {
        FullScreenVideoPlayerView(
            playerController = viewModel.playerController,
            playerState = playerState,
            videoMetadata = videoMetadata,
            subtitleTrack = subtitleTrack,
            activeCue = selectedCue ?: activeCue,
            onSubtitlePositionChanged = { newX, newY -> viewModel.updateSubtitlePosition(newX, newY) },
            onOpenStyleDialog = { viewModel.setShowStyleDialog(true) },
            onOpenPlacementDialog = { viewModel.setShowPlacementDialog(true) },
            onOpenSubtitleList = { viewModel.setShowSubtitleListSheet(true) },
            onExitFullscreen = { viewModel.setFullscreenVideo(false) }
        )
        return
    }

    // Standard Multi-Pane Studio Editor Layout
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Studio Media & Project Header
            EditorMediaHeader(
                videoFileName = videoMetadata.fileName,
                hasVideoLoaded = videoMetadata.uriString.isNotEmpty(),
                subtitleTrack = subtitleTrack,
                aspectRatio = aspectRatio,
                canUndo = canUndo,
                canRedo = canRedo,
                onSelectAspectRatio = { viewModel.setAspectRatio(it) },
                onUndoClick = { viewModel.undo() },
                onRedoClick = { viewModel.redo() },
                onSaveProjectClick = {
                    viewModel.saveCurrentProject()
                    Toast.makeText(context, "Project & subtitles saved", Toast.LENGTH_SHORT).show()
                },
                onExportClick = { viewModel.setShowExportDialog(true) },
                onOptimizeClick = { viewModel.setShowOptimizationSheet(true) },
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

            // 2. Video Viewport Canvas with Aspect Ratio Framing & Interactive Drag/Pinch/Corner Handles
            val currentSelected = selectedCue ?: activeCue
            VideoPlayerView(
                playerController = viewModel.playerController,
                playerState = playerState,
                activeCue = currentSelected,
                aspectRatioOption = aspectRatio,
                hasVideoLoaded = videoMetadata.uriString.isNotEmpty(),
                isMediaLost = videoMetadata.uriString.isNotEmpty() && playerState.durationMs == 0L && !playerState.isPlaying && videoMetadata.fileName.contains("lost", ignoreCase = true),
                onReplaceMediaClick = { videoPickerLauncher.launch("video/*") },
                onSubtitlePositionChanged = { newX, newY ->
                    viewModel.updateSubtitlePosition(newX, newY)
                },
                onSubtitleFontSizeChanged = { newSize ->
                    currentSelected?.let { cue ->
                        viewModel.updateCueFontSize(cue, newSize)
                    }
                },
                onSubtitleTapped = {
                    viewModel.setShowPlacementDialog(true)
                },
                onDeleteSelectedCue = { viewModel.deleteSelectedCue() },
                onDuplicateSelectedCue = { viewModel.duplicateSelectedCue() },
                onAlignSelectedCueCenter = {
                    currentSelected?.let {
                        viewModel.updateSubtitlePosition(0.50f, it.posY)
                    }
                },
                onToggleFullscreen = if (videoMetadata.uriString.isNotEmpty()) {
                    { viewModel.setFullscreenVideo(true) }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            )

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

            // 4. Live Selected Cue Inspector Component (Handles Single-Cue Editing, Font Resizing & Stepper)
            val currentIdx = if (currentSelected != null) subtitleTrack.cues.indexOfFirst { it.id == currentSelected.id } else -1

            EditorCueInspector(
                selectedCue = currentSelected,
                totalCuesCount = subtitleTrack.cues.size,
                currentCueIndex = currentIdx,
                onSelectPreviousCue = { viewModel.selectPreviousCue() },
                onSelectNextCue = { viewModel.selectNextCue() },
                onDuplicateCue = { viewModel.duplicateSelectedCue() },
                onDeleteCue = { viewModel.deleteSelectedCue() },
                onUpdateCueText = { cue, text -> viewModel.updateCueText(cue, text) },
                onUpdateCueStyle = { cue, style -> viewModel.updateSubtitleStyle(style, applyToAll = false) },
                onUpdateCueFontSize = { cue, newSize -> viewModel.updateCueFontSize(cue, newSize, applyToAll = false) },
                onJumpToCue = { cue -> viewModel.jumpToCue(cue) },
                onAddFirstCue = { viewModel.addCueAtCurrentPosition() }
            )

            // 5. Studio Action Strip / Toolbelt Component
            EditorToolbeltStrip(
                hasSelectedCue = currentSelected != null,
                onAddSubtitleClick = { viewModel.addCueAtCurrentPosition() },
                onSplitClick = { viewModel.splitCueAtCurrentPosition() },
                onPlacementClick = { viewModel.setShowPlacementDialog(true) },
                onStyleClick = { viewModel.setShowStyleDialog(true) },
                onPresetPacksClick = { showPresetStylesSheet = true },
                onFindReplaceClick = { viewModel.setShowFindReplaceDialog(true) },
                onSanitizeTimingClick = { viewModel.setShowSanitizerDialog(true) },
                onResizeTextClick = { viewModel.nudgeCueFontSize(2f) },
                onSubtitlesListClick = { viewModel.setShowSubtitleListSheet(true) },
                onOptimizationClick = { viewModel.setShowOptimizationSheet(true) },
                onDeleteClick = { viewModel.deleteSelectedCue() },
                onDuplicateClick = { viewModel.duplicateSelectedCue() }
            )

            // Bottom spacing for navigation bar
            Spacer(Modifier.height(80.dp))
        }
    }

    // Modal Dialogs & Sheets
    if (showFindReplaceDialog) {
        FindReplaceDialog(
            subtitleTrack = subtitleTrack,
            onDismiss = { viewModel.setShowFindReplaceDialog(false) },
            onReplaceAll = { findText, replaceText, matchCase, useRegex ->
                viewModel.findAndReplace(findText, replaceText, matchCase, useRegex)
            }
        )
    }

    if (showSanitizerDialog) {
        TimingSanitizerDialog(
            subtitleTrack = subtitleTrack,
            onDismiss = { viewModel.setShowSanitizerDialog(false) },
            onSanitize = { minGapMs ->
                viewModel.autoRepairOverlaps(minGapMs)
            },
            onSortChronologically = {
                viewModel.reorderCuesChronologically()
            }
        )
    }

    if (showPresetStylesSheet) {
        QuickPresetStyleSheet(
            onDismiss = { showPresetStylesSheet = false },
            onSelectPreset = { presetId ->
                viewModel.applyPresetStyle(presetId)
                Toast.makeText(context, "Applied style preset", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showOptimizationSheet) {
        OptimizationBottomSheet(
            settings = processingSettings,
            onDismiss = { viewModel.setShowOptimizationSheet(false) },
            onApplySettings = { newSettings -> viewModel.updateProcessingSettings(newSettings) },
            onClearCache = {
                viewModel.clearTemporaryCache()
                Toast.makeText(context, "Temporary cache cleared", Toast.LENGTH_SHORT).show()
            }
        )
    }

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
