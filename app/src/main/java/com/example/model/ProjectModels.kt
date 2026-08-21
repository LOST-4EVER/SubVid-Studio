package com.example.model

import java.util.UUID

data class StudioProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val videoUriString: String = "",
    val videoFileName: String = "No Video",
    val videoDurationMs: Long = 0L,
    val currentPositionMs: Long = 0L,
    val subtitleFileName: String = "No Subtitles",
    val subtitleFormat: SubtitleFormat = SubtitleFormat.SRT,
    val cueCount: Int = 0,
    val subtitleTrack: SubtitleTrack = SubtitleTrack(title = name, format = subtitleFormat, cues = emptyList()),
    val lastModifiedMs: Long = System.currentTimeMillis()
) {
    val hasVideo: Boolean get() = videoUriString.isNotEmpty()
    val formattedDuration: String get() = SubtitleCue.formatTimestampShort(videoDurationMs)
    val formattedDate: String
        get() {
            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(lastModifiedMs))
        }
}

