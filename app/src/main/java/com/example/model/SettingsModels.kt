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

data class ProcessingSettings(
    val engine: ProcessingEngine = ProcessingEngine.GPU_HARDWARE,
    val cpuThreads: Int = 4,
    val enableHardwareSurface: Boolean = true,
    val targetFps: Int = 60,
    val defaultQuality: QualityPreset = QualityPreset.NATIVE_ULTRA,
    val defaultExportContainer: String = "mp4",
    val autoSaveProject: Boolean = true
)

enum class AppTab(val title: String) {
    HOME("Projects"),
    EDITOR("Editor"),
    BATCH("Batch Hub"),
    SETTINGS("Settings")
}
