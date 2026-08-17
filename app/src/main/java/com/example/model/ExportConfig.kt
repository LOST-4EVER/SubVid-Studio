package com.example.model

enum class ExportMode(val title: String, val description: String) {
    HARDCODE_BURN_IN(
        "Hardcode Burn-in (Max Native Quality)",
        "Burns styled subtitles directly into the video frames with native resolution and ultra-high bitrate for maximum sharpness."
    ),
    LOSSLESS_SOFT_MUX(
        "Lossless Soft Muxing (MKV / MP4)",
        "Embeds subtitle track directly into container stream without re-encoding video. 100% bit-for-bit lossless native video quality & instantaneous export."
    ),
    SUBTITLE_ONLY(
        "Export Subtitle File (.srt / .vtt / .ass)",
        "Exports the edited subtitle file with all updated timings, positions, and formatting tags."
    )
}

enum class QualityPreset(val label: String, val resolutionName: String, val targetBitrateBps: Int, val targetFps: Int) {
    NATIVE_ULTRA("Native Source / Ultra High", "Native Res", 20_000_000, 60),
    FULL_HD_1080P("1080p Full HD (Crisp)", "1920x1080", 12_000_000, 30),
    HD_720P("720p HD (Balanced)", "1280x720", 6_000_000, 30)
}

data class ExportConfig(
    val exportMode: ExportMode = ExportMode.HARDCODE_BURN_IN,
    val qualityPreset: QualityPreset = QualityPreset.NATIVE_ULTRA,
    val containerFormat: String = "mp4", // "mp4" or "mkv"
    val subtitleExportFormat: SubtitleFormat = SubtitleFormat.SRT,
    val applyCustomPositions: Boolean = true,
    val burnInBoxBackground: Boolean = true
)

sealed class ExportState {
    object Idle : ExportState()
    data class Exporting(val progress: Float, val currentFrame: Long, val totalFrames: Long, val message: String) : ExportState()
    data class Success(val outputFilePath: String, val fileSizeFormatted: String, val mode: ExportMode) : ExportState()
    data class Error(val errorMessage: String) : ExportState()
}
