package com.example.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object StudioIcons {
    // Custom Vector SVG: Subtitles / Dialogue Caption
    val Subtitles: ImageVector = ImageVector.Builder(
        name = "StudioSubtitles",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = SolidColor(Color.White),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(20f, 4f)
        lineTo(4f, 4f)
        curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
        lineTo(2f, 18f)
        curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
        lineTo(20f, 20f)
        curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
        lineTo(22f, 6f)
        curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
        close()
        moveTo(11f, 11f)
        lineTo(6f, 11f)
        lineTo(6f, 9f)
        lineTo(11f, 9f)
        lineTo(11f, 11f)
        close()
        moveTo(18f, 11f)
        lineTo(13f, 11f)
        lineTo(13f, 9f)
        lineTo(18f, 9f)
        lineTo(18f, 11f)
        close()
        moveTo(8f, 15f)
        lineTo(6f, 15f)
        lineTo(6f, 13f)
        lineTo(8f, 13f)
        lineTo(8f, 15f)
        close()
        moveTo(18f, 15f)
        lineTo(10f, 15f)
        lineTo(10f, 13f)
        lineTo(18f, 13f)
        lineTo(18f, 15f)
        close()
    }.build()

    // Custom Vector SVG: Video Reel
    val Video: ImageVector = ImageVector.Builder(
        name = "StudioVideo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = SolidColor(Color.White)
    ) {
        moveTo(18f, 4f)
        lineTo(6f, 4f)
        curveTo(4.9f, 4f, 4f, 4.9f, 4f, 6f)
        lineTo(4f, 18f)
        curveTo(4f, 19.1f, 4.9f, 20f, 6f, 20f)
        lineTo(18f, 20f)
        curveTo(19.1f, 20f, 20f, 19.1f, 20f, 18f)
        lineTo(20f, 6f)
        curveTo(20f, 4.9f, 19.1f, 4f, 18f, 4f)
        close()
        moveTo(8f, 17f)
        lineTo(6f, 17f)
        lineTo(6f, 15f)
        lineTo(8f, 15f)
        lineTo(8f, 17f)
        close()
        moveTo(8f, 13f)
        lineTo(6f, 13f)
        lineTo(6f, 11f)
        lineTo(8f, 11f)
        lineTo(8f, 13f)
        close()
        moveTo(8f, 9f)
        lineTo(6f, 9f)
        lineTo(6f, 7f)
        lineTo(8f, 7f)
        lineTo(8f, 9f)
        close()
        moveTo(18f, 17f)
        lineTo(16f, 17f)
        lineTo(16f, 15f)
        lineTo(18f, 15f)
        lineTo(18f, 17f)
        close()
        moveTo(18f, 13f)
        lineTo(16f, 13f)
        lineTo(16f, 11f)
        lineTo(18f, 11f)
        lineTo(18f, 13f)
        close()
        moveTo(18f, 9f)
        lineTo(16f, 9f)
        lineTo(16f, 7f)
        lineTo(18f, 7f)
        lineTo(18f, 9f)
        close()
    }.build()

    // Custom Vector SVG: Hardware GPU Accelerator
    val Gpu: ImageVector = ImageVector.Builder(
        name = "StudioGpu",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = SolidColor(Color.White)
    ) {
        moveTo(11f, 21f)
        lineTo(9f, 21f)
        lineTo(9f, 19f)
        lineTo(7f, 19f)
        curveTo(5.9f, 19f, 5f, 18.1f, 5f, 17f)
        lineTo(5f, 15f)
        lineTo(3f, 15f)
        lineTo(3f, 13f)
        lineTo(5f, 13f)
        lineTo(5f, 11f)
        lineTo(3f, 11f)
        lineTo(3f, 9f)
        lineTo(5f, 9f)
        lineTo(5f, 7f)
        curveTo(5f, 5.9f, 5.9f, 5f, 7f, 5f)
        lineTo(9f, 5f)
        lineTo(9f, 3f)
        lineTo(11f, 3f)
        lineTo(11f, 5f)
        lineTo(13f, 5f)
        lineTo(13f, 3f)
        lineTo(15f, 3f)
        lineTo(15f, 5f)
        lineTo(17f, 5f)
        curveTo(18.1f, 5f, 19f, 5.9f, 19f, 7f)
        lineTo(19f, 9f)
        lineTo(21f, 9f)
        lineTo(21f, 11f)
        lineTo(19f, 11f)
        lineTo(19f, 13f)
        lineTo(21f, 13f)
        lineTo(21f, 15f)
        lineTo(19f, 15f)
        lineTo(19f, 17f)
        curveTo(19f, 18.1f, 18.1f, 19f, 17f, 19f)
        lineTo(15f, 19f)
        lineTo(15f, 21f)
        lineTo(13f, 21f)
        lineTo(13f, 19f)
        lineTo(11f, 19f)
        lineTo(11f, 21f)
        close()
        moveTo(7f, 7f)
        lineTo(7f, 17f)
        lineTo(17f, 17f)
        lineTo(17f, 7f)
        lineTo(7f, 7f)
        close()
        moveTo(9f, 9f)
        lineTo(15f, 9f)
        lineTo(15f, 15f)
        lineTo(9f, 15f)
        lineTo(9f, 9f)
        close()
    }.build()

    // Custom Vector SVG: SubVid Studio Brand App Badge Logo
    val StudioLogo: ImageVector = ImageVector.Builder(
        name = "StudioLogo",
        defaultWidth = 32.dp,
        defaultHeight = 32.dp,
        viewportWidth = 32f,
        viewportHeight = 32f
    ).path(
        fill = SolidColor(Color(0xFF00E5FF)),
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(16f, 2f)
        curveTo(8.27f, 2f, 2f, 8.27f, 2f, 16f)
        curveTo(2f, 23.73f, 8.27f, 30f, 16f, 30f)
        curveTo(23.73f, 30f, 30f, 23.73f, 30f, 16f)
        curveTo(30f, 8.27f, 23.73f, 2f, 16f, 2f)
        close()
        moveTo(12f, 22f)
        lineTo(12f, 10f)
        lineTo(22f, 16f)
        lineTo(12f, 22f)
        close()
    }.path(
        fill = SolidColor(Color(0xFFD0BCFF))
    ) {
        moveTo(8f, 24f)
        lineTo(24f, 24f)
        lineTo(24f, 26f)
        lineTo(8f, 26f)
        close()
    }.build()

    // Custom Vector SVG: Hardware Video Burn-In / Hardcode
    val BurnIn: ImageVector = ImageVector.Builder(
        name = "StudioBurnIn",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = SolidColor(Color.White)
    ) {
        moveTo(13.5f, 2f)
        curveTo(13.5f, 2f, 11f, 5f, 11f, 7.5f)
        curveTo(11f, 8.88f, 12.12f, 10f, 13.5f, 10f)
        curveTo(14.88f, 10f, 16f, 8.88f, 16f, 7.5f)
        curveTo(16f, 5f, 13.5f, 2f, 13.5f, 2f)
        close()
        moveTo(12f, 12f)
        curveTo(8.13f, 12f, 5f, 15.13f, 5f, 19f)
        curveTo(5f, 20.66f, 5.58f, 22.18f, 6.57f, 23.38f)
        lineTo(8.06f, 21.89f)
        curveTo(7.39f, 21.08f, 7f, 20.08f, 7f, 19f)
        curveTo(7f, 16.24f, 9.24f, 14f, 12f, 14f)
        curveTo(14.76f, 14f, 17f, 16.24f, 17f, 19f)
        curveTo(17f, 20.08f, 16.61f, 21.08f, 15.94f, 21.89f)
        lineTo(17.43f, 23.38f)
        curveTo(18.42f, 22.18f, 19f, 20.66f, 19f, 19f)
        curveTo(19f, 15.13f, 15.87f, 12f, 12f, 12f)
        close()
    }.build()

    // Standard mappings
    val Home: ImageVector = Icons.Filled.Home
    val Import: ImageVector = Icons.Filled.FileUpload
    val Export: ImageVector = Icons.Filled.FileDownload
    val Play: ImageVector = Icons.Filled.PlayArrow
    val Pause: ImageVector = Icons.Filled.Pause
    val Rewind: ImageVector = Icons.Filled.FastRewind
    val Forward: ImageVector = Icons.Filled.FastForward
    val Cut: ImageVector = Icons.Filled.ContentCut
    val Copy: ImageVector = Icons.Filled.Layers
    val Edit: ImageVector = Icons.Filled.Edit
    val Delete: ImageVector = Icons.Filled.Delete
    val Add: ImageVector = Icons.Filled.Add
    val Position: ImageVector = Icons.Filled.OpenWith
    val Style: ImageVector = Icons.Filled.FormatPaint
    val SyncAudio: ImageVector = Icons.Filled.Sync
    val Settings: ImageVector = Icons.Filled.Settings
    val Back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val Close: ImageVector = Icons.Filled.Close
    val Check: ImageVector = Icons.Filled.Check
    val CheckCircle: ImageVector = Icons.Filled.CheckCircle
    val Grid: ImageVector = Icons.Filled.GridView
    val Layers: ImageVector = Icons.Filled.Layers
    val Bold: ImageVector = Icons.Filled.FormatBold
    val Italic: ImageVector = Icons.Filled.FormatItalic
    val TextColor: ImageVector = Icons.Filled.FormatColorText
    val FontSize: ImageVector = Icons.Filled.FormatSize
    val Tune: ImageVector = Icons.Filled.Tune
    val Replay: ImageVector = Icons.Filled.Replay
    val BatchQueue: ImageVector = Icons.Filled.Layers
    val Folder: ImageVector = Icons.Filled.Folder
    val FolderOpen: ImageVector = Icons.Filled.FolderOpen
    val Cpu: ImageVector = Icons.Filled.Memory
    val Speed: ImageVector = Icons.Filled.Speed
    val Timer: ImageVector = Icons.Filled.Timer
    val Share: ImageVector = Icons.Filled.Share
    val Save: ImageVector = Icons.Filled.Save
    val Refresh: ImageVector = Icons.Filled.Refresh
    val Info: ImageVector = Icons.Filled.Info
    val Error: ImageVector = Icons.Filled.ErrorOutline
    val VolumeUp: ImageVector = Icons.AutoMirrored.Filled.VolumeUp
    val VolumeOff: ImageVector = Icons.AutoMirrored.Filled.VolumeOff
    val AudioTrack: ImageVector = Icons.Filled.Audiotrack

    // Custom Vector SVG: Fullscreen Enter
    val Fullscreen: ImageVector = ImageVector.Builder(
        name = "StudioFullscreen",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = SolidColor(Color.White)
    ) {
        moveTo(7f, 14f)
        lineTo(5f, 14f)
        lineTo(5f, 19f)
        lineTo(10f, 19f)
        lineTo(10f, 17f)
        lineTo(7f, 17f)
        lineTo(7f, 14f)
        close()
        moveTo(5f, 10f)
        lineTo(7f, 10f)
        lineTo(7f, 7f)
        lineTo(10f, 7f)
        lineTo(10f, 5f)
        lineTo(5f, 5f)
        lineTo(5f, 10f)
        close()
        moveTo(17f, 17f)
        lineTo(14f, 17f)
        lineTo(14f, 19f)
        lineTo(19f, 19f)
        lineTo(19f, 14f)
        lineTo(17f, 14f)
        lineTo(17f, 17f)
        close()
        moveTo(14f, 5f)
        lineTo(14f, 7f)
        lineTo(17f, 7f)
        lineTo(17f, 10f)
        lineTo(19f, 10f)
        lineTo(19f, 5f)
        lineTo(14f, 5f)
        close()
    }.build()

    // Custom Vector SVG: Fullscreen Exit
    val FullscreenExit: ImageVector = ImageVector.Builder(
        name = "StudioFullscreenExit",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = SolidColor(Color.White)
    ) {
        moveTo(5f, 16f)
        lineTo(8f, 16f)
        lineTo(8f, 19f)
        lineTo(10f, 19f)
        lineTo(10f, 14f)
        lineTo(5f, 14f)
        lineTo(5f, 16f)
        close()
        moveTo(8f, 8f)
        lineTo(5f, 8f)
        lineTo(5f, 10f)
        lineTo(10f, 10f)
        lineTo(10f, 5f)
        lineTo(8f, 5f)
        lineTo(8f, 8f)
        close()
        moveTo(14f, 19f)
        lineTo(16f, 19f)
        lineTo(16f, 16f)
        lineTo(19f, 16f)
        lineTo(19f, 14f)
        lineTo(14f, 14f)
        lineTo(14f, 19f)
        close()
        moveTo(16f, 8f)
        lineTo(14f, 8f)
        lineTo(14f, 10f)
        lineTo(19f, 10f)
        lineTo(19f, 5f)
        lineTo(16f, 5f)
        lineTo(16f, 8f)
        close()
    }.build()

    // Custom Vector SVG: Aspect Ratio Mode
    val AspectRatio: ImageVector = ImageVector.Builder(
        name = "StudioAspectRatio",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = SolidColor(Color.White)
    ) {
        moveTo(19f, 12f)
        lineTo(17f, 12f)
        lineTo(17f, 15f)
        lineTo(14f, 15f)
        lineTo(14f, 17f)
        lineTo(19f, 17f)
        lineTo(19f, 12f)
        close()
        moveTo(7f, 9f)
        lineTo(10f, 9f)
        lineTo(10f, 7f)
        lineTo(5f, 7f)
        lineTo(5f, 12f)
        lineTo(7f, 12f)
        lineTo(7f, 9f)
        close()
        moveTo(21f, 3f)
        lineTo(3f, 3f)
        curveTo(1.9f, 3f, 1f, 3.9f, 1f, 5f)
        lineTo(1f, 19f)
        curveTo(1f, 20.1f, 1.9f, 21f, 3f, 21f)
        lineTo(21f, 21f)
        curveTo(22.1f, 21f, 23f, 20.1f, 23f, 19f)
        lineTo(23f, 5f)
        curveTo(23f, 3.9f, 22.1f, 3f, 21f, 3f)
        close()
        moveTo(21f, 19f)
        lineTo(3f, 19f)
        lineTo(3f, 5f)
        lineTo(21f, 5f)
        lineTo(21f, 19f)
        close()
    }.build()

    // Custom Vector SVG: Undo
    val Undo: ImageVector = ImageVector.Builder(
        name = "StudioUndo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(12.5f, 8f)
        curveTo(9.46f, 8f, 6.77f, 9.17f, 4.79f, 11.08f)
        lineTo(1f, 7.29f)
        lineTo(1f, 16.71f)
        lineTo(10.42f, 16.71f)
        lineTo(6.63f, 12.92f)
        curveTo(8.17f, 11.44f, 10.22f, 10.5f, 12.5f, 10.5f)
        curveTo(16.98f, 10.5f, 20.65f, 13.68f, 21.47f, 17.84f)
        lineTo(23.95f, 17.03f)
        curveTo(22.86f, 11.83f, 18.13f, 8f, 12.5f, 8f)
        close()
    }.build()

    // Custom Vector SVG: Redo
    val Redo: ImageVector = ImageVector.Builder(
        name = "StudioRedo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(18.41f, 11.08f)
        curveTo(16.43f, 9.17f, 13.74f, 8f, 10.7f, 8f)
        curveTo(5.07f, 8f, 0.34f, 11.83f, -0.75f, 17.03f)
        lineTo(1.73f, 17.84f)
        curveTo(2.55f, 13.68f, 6.22f, 10.5f, 10.7f, 10.5f)
        curveTo(12.98f, 10.5f, 15.03f, 11.44f, 16.57f, 12.92f)
        lineTo(12.78f, 16.71f)
        lineTo(22.2f, 16.71f)
        lineTo(22.2f, 7.29f)
        lineTo(18.41f, 11.08f)
        close()
    }.build()

    // Custom Vector SVG: MoreVert
    val MoreVert: ImageVector = ImageVector.Builder(
        name = "StudioMoreVert",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(12f, 8f)
        curveTo(13.1f, 8f, 14f, 7.1f, 14f, 6f)
        curveTo(14f, 4.9f, 13.1f, 4f, 12f, 4f)
        curveTo(10.9f, 4f, 10f, 4.9f, 10f, 6f)
        curveTo(10f, 7.1f, 10.9f, 8f, 12f, 8f)
        close()
        moveTo(12f, 10f)
        curveTo(10.9f, 10f, 10f, 10.9f, 10f, 12f)
        curveTo(10f, 13.1f, 10.9f, 14f, 12f, 14f)
        curveTo(13.1f, 14f, 14f, 13.1f, 14f, 12f)
        curveTo(14f, 10.9f, 13.1f, 10f, 12f, 10f)
        close()
        moveTo(12f, 16f)
        curveTo(10.9f, 16f, 10f, 16.9f, 10f, 18f)
        curveTo(10f, 19.1f, 10.9f, 20f, 12f, 20f)
        curveTo(13.1f, 20f, 14f, 19.1f, 14f, 18f)
        curveTo(14f, 16.9f, 13.1f, 16f, 12f, 16f)
        close()
    }.build()

    val Crop: ImageVector = AspectRatio
    val Split: ImageVector = Cut
    val Placement: ImageVector = Position
    val Warning: ImageVector = Icons.Filled.ErrorOutline
    val ArrowBack = Icons.AutoMirrored.Filled.ArrowBack
    val Search = Icons.Filled.FolderOpen
    val FindReplace: ImageVector = SyncAudio
    val Sparkles: ImageVector = Style
    val Waves: ImageVector = Subtitles
    val Diagnostics: ImageVector = Gpu
    val Broom: ImageVector = Delete
    val Movie: ImageVector = Video
    val List: ImageVector = Subtitles
}

