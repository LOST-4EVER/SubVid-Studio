# SubVid Studio — AI Agent Guidelines & Architecture Manual

Welcome to **SubVid Studio**, an advanced, high-performance Android application built with Kotlin and Jetpack Compose for subtitle editing, timeline synchronization, batch processing, and hardware-accelerated video hardcoding.

---

## 1. Project Architecture & Stack Overview

- **UI Framework**: Jetpack Compose (Material Design 3 Immersive Dark Theme).
- **Video Engine**: AndroidX Media3 ExoPlayer (`1.2.1`) for preview and frame-accurate seeking.
- **Subtitle Parsing & Export Engine**: Native Kotlin parsers supporting `SRT`, `VTT`, `ASS` / `SSA` with full tag rendering, position extraction, and styling.
- **Video Export & Hardcoding Engine**: Android `MediaCodec` + `MediaMuxer` hardware acceleration with lossy burn-in and lossless soft-muxing options.
- **Batch Processing**: CPU Multi-threading + Hardware Video Encoder integration managed by `BatchProcessingManager`.
- **State Management**: Clean ViewModel architecture with `StateFlow` and immutable state models.

---

## 2. Directory & Package Structure

```
app/src/main/java/com/example/
├── export/                # Hardware video export, lossless muxing, and batch processing
│   ├── BatchProcessingManager.kt
│   └── VideoExportManager.kt
├── model/                 # Data models (SubtitleTrack, SubtitleCue, ExportConfig, BatchTask, StudioProject)
│   ├── BatchTask.kt
│   ├── ExportConfig.kt
│   ├── ProjectRepository.kt
│   ├── StudioProject.kt
│   ├── SubtitleCue.kt
│   ├── SubtitleFormat.kt
│   ├── SubtitleStyle.kt
│   └── SubtitleTrack.kt
├── parser/                # SRT, WEBVTT, ASS/SSA parsers and string generators
│   ├── SubtitleParser.kt
│   └── SubtitleWriter.kt
├── player/                # ExoPlayer controller and timeline progress synchronization
│   └── VideoPlayerController.kt
├── ui/                    # Jetpack Compose UI layer
│   ├── components/        # Custom components (VideoPlayerView, TimelineScrubberView, SubtitleEditorSheet)
│   ├── icons/             # Custom SVG vector icons (StudioIcons)
│   ├── screens/           # Main application tab screens (HomeScreen, EditorScreen, BatchHubScreen, SettingsScreen)
│   └── theme/             # Immersive design system theme, colors, and typography
└── viewmodel/             # Central ViewModel (MainViewModel) managing app state lifecycle
```

---

## 3. Core Development Rules & Guidelines

1. **SVG Vector Icons Only**: All icons in the application must use vector drawables / `ImageVector` paths from `StudioIcons.kt`. Avoid generated PNG/raster icons or raw image resources for UI elements.
2. **UI Performance**:
   - High-frequency components (e.g. `TimelineScrubberView`) must render cue blocks on a single Compose `Canvas` rather than allocating individual composable nodes.
   - Position updates from `VideoPlayerController` must be checked for changes before state mutation to prevent unnecessary recomposition ticks.
3. **File System & URIs**:
   - Always invoke `takePersistableUriPermission` when reading video or subtitle URIs via `ContentResolver`.
   - Handle missing media URIs gracefully with try-catch blocks and non-blocking UI notifications.
4. **Build & Release Signing**:
   - `app/build.gradle.kts` configures release signing. Environment variables `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` inject release keys during CI/CD.
   - If no release key is provided, the build automatically falls back to `debug.keystore` ensuring zero build failures.

---

## 4. Build Commands

- **Build Debug APK**: `gradle assembleDebug`
- **Build Release APK**: `gradle assembleRelease`
- **Run Unit Tests**: `gradle :app:testDebugUnitTest`
