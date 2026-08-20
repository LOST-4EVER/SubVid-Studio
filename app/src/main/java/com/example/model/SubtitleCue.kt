package com.example.model

import java.util.UUID

enum class SubtitleAlignment {
    TOP_START,
    TOP_CENTER,
    TOP_END,
    CENTER_START,
    CENTER,
    CENTER_END,
    BOTTOM_START,
    BOTTOM_CENTER,
    BOTTOM_END,
    CUSTOM;

    fun displayName(): String = when (this) {
        TOP_START -> "Top Left"
        TOP_CENTER -> "Top Center"
        TOP_END -> "Top Right"
        CENTER_START -> "Middle Left"
        CENTER -> "Center"
        CENTER_END -> "Middle Right"
        BOTTOM_START -> "Bottom Left"
        BOTTOM_CENTER -> "Bottom Center"
        BOTTOM_END -> "Bottom Right"
        CUSTOM -> "Custom (X/Y)"
    }
}

enum class SubtitleFormat(val extension: String, val displayName: String) {
    SRT("srt", "SubRip (.srt)"),
    VTT("vtt", "WebVTT (.vtt)"),
    ASS("ass", "Advanced SubStation (.ass)"),
    SSA("ssa", "SubStation Alpha (.ssa)");

    companion object {
        fun fromExtension(ext: String?): SubtitleFormat {
            return when (ext?.lowercase()) {
                "vtt" -> VTT
                "ass" -> ASS
                "ssa" -> SSA
                else -> SRT
            }
        }
    }
}

data class SubtitleStyle(
    val fontSizeSp: Float = 22f,
    val textColorArgb: Long = 0xFFFFFFFF,
    val strokeColorArgb: Long = 0xFF000000,
    val strokeWidthDp: Float = 2.5f,
    val backgroundColorArgb: Long = 0x00000000, // Default transparent (clean look), toggleable to semi-transparent black
    val cornerRadiusDp: Float = 6f,
    val paddingHorizontalDp: Float = 10f,
    val paddingVerticalDp: Float = 4f,
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val shadowRadiusDp: Float = 4f,
    val shadowColorArgb: Long = 0xCC000000
) {
    val hasBackground: Boolean get() = ((backgroundColorArgb ushr 24) and 0xFF) > 15
}

data class SubtitleCue(
    val id: String = UUID.randomUUID().toString(),
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String,
    val posX: Float = 0.5f, // Center horizontally (0.0 = left, 1.0 = right)
    val posY: Float = 0.88f, // Near bottom (0.0 = top, 1.0 = bottom)
    val alignment: SubtitleAlignment = SubtitleAlignment.BOTTOM_CENTER,
    val style: SubtitleStyle = SubtitleStyle()
) {
    val durationMs: Long get() = (endTimeMs - startTimeMs).coerceAtLeast(0)

    fun isActiveAt(positionMs: Long): Boolean {
        return positionMs in startTimeMs..endTimeMs
    }

    fun formattedStartTime(): String = formatTimestamp(startTimeMs)
    fun formattedEndTime(): String = formatTimestamp(endTimeMs)

    companion object {
        fun formatTimestamp(ms: Long): String {
            val totalSeconds = ms / 1000
            val millis = ms % 1000
            val seconds = totalSeconds % 60
            val minutes = (totalSeconds / 60) % 60
            val hours = totalSeconds / 3600
            return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
        }

        fun formatTimestampShort(ms: Long): String {
            val totalSeconds = ms / 1000
            val millis = ms % 1000
            val seconds = totalSeconds % 60
            val minutes = (totalSeconds / 60) % 60
            return String.format("%02d:%02d.%03d", minutes, seconds, millis)
        }
    }
}

data class SubtitleTrack(
    val title: String = "Track 1",
    val format: SubtitleFormat = SubtitleFormat.SRT,
    val language: String = "en",
    val cues: List<SubtitleCue> = emptyList(),
    val defaultStyle: SubtitleStyle = SubtitleStyle()
)

data class VideoMetadata(
    val uriString: String,
    val fileName: String,
    val durationMs: Long,
    val width: Int = 1920,
    val height: Int = 1080,
    val mimeType: String = "video/mp4",
    val isMkv: Boolean = fileName.endsWith(".mkv", ignoreCase = true),
    val fileSizeFormatted: String = "HD Video"
) {
    val aspectRatio: Float get() = if (height > 0) width.toFloat() / height.toFloat() else 16f / 9f
    val formattedDuration: String get() = SubtitleCue.formatTimestamp(durationMs)
}
