package com.example.model

enum class ProcessingEngine(val title: String, val badge: String, val description: String) {
    GPU_HARDWARE(
        "GPU Hardware Accelerated",
        "GPU • MediaCodec",
        "Uses hardware MediaCodec decoders/encoders and Surface buffers for maximum throughput & low power consumption."
    ),
    CPU_MULTITHREAD(
        "CPU Multi-Threaded",
        "CPU • Multi-Core",
        "Utilizes multi-core software worker pool for pixel-exact subtitle rasterization and custom glyph rendering."
    )
}

enum class RenderOptimizationLevel(val label: String, val description: String) {
    HIGH_PERFORMANCE("Max Performance (60/120 FPS)", "Smoothest timeline scrubbing and GPU text rendering with zero lag"),
    BALANCED("Balanced Mode", "Optimal trade-off between battery life and playback responsiveness"),
    BATTERY_SAVER("Battery Saver Mode", "Lower frame polling and throttled preview updates for extended editing sessions")
}

data class ProcessingSettings(
    val engine: ProcessingEngine = ProcessingEngine.GPU_HARDWARE,
    val cpuThreads: Int = 4,
    val enableHardwareSurface: Boolean = true,
    val targetFps: Int = 60,
    val defaultQuality: QualityPreset = QualityPreset.NATIVE_ULTRA,
    val defaultExportContainer: String = "mp4",
    val autoSaveProject: Boolean = true,
    val optimizationLevel: RenderOptimizationLevel = RenderOptimizationLevel.HIGH_PERFORMANCE,
    val fastWaveformRendering: Boolean = true,
    val hardwareTextAntiAliasing: Boolean = true,
    val memoryCacheSizeMb: Int = 512,
    val lowLatencyAudio: Boolean = true,
    // Studio Defaults & Subtitle Preferences
    val defaultSubtitleFormat: SubtitleFormat = SubtitleFormat.SRT,
    val defaultFontSize: Float = 22f,
    val defaultFontFamily: String = "Sans-Serif",
    val defaultTextColorHex: String = "#FFFFFF",
    val defaultOutlineWidth: Float = 2.5f,
    val defaultBackgroundEnabled: Boolean = false,
    val defaultBgColorHex: String = "#000000",
    val defaultBgAlpha: Float = 0.6f,
    // Timeline & Interaction Preferences
    val snapToCues: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val nudgeStepMs: Long = 250L,
    val autoFixOverlapsOnImport: Boolean = true,
    val minimumCueGapMs: Long = 50L,
    val timelineZoomLevel: Float = 1.0f,
    val autoScrollTimeline: Boolean = true
)

enum class AppTab(val title: String) {
    HOME("Projects"),
    EDITOR("Editor"),
    BATCH("Batch Hub"),
    SETTINGS("Settings")
}

