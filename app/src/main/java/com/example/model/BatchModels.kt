package com.example.model

import java.util.UUID

enum class BatchOperationType(val title: String, val description: String) {
    CONVERT_FORMAT("Convert Subtitle Format", "Batch convert SRT, VTT, or ASS/SSA files into target format."),
    TIME_SHIFT("Shift Timings (Sync Audio)", "Batch offset subtitle timestamps by +/- milliseconds for audio sync."),
    HARD_SUB_BURN_IN("Batch Video Burn-In", "Render high-quality hardcoded subtitles directly into video queue."),
    MKV_SOFT_MUX("Batch MKV/MP4 Soft-Mux", "Lossless remuxing embedding subtitle tracks into containers.")
}

enum class BatchTaskStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

data class BatchTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val sourceSubtitleUri: String? = null,
    val sourceVideoUri: String? = null,
    val operationType: BatchOperationType,
    val targetFormat: SubtitleFormat = SubtitleFormat.SRT,
    val timeShiftMs: Long = 0L,
    val status: BatchTaskStatus = BatchTaskStatus.PENDING,
    val progress: Float = 0f,
    val speedFps: Float = 0f,
    val outputFileName: String = "",
    val errorMessage: String? = null,
    val resultPath: String? = null
)
