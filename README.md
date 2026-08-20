# SubVid Studio 🎬⚡

**SubVid Studio** is an advanced, high-performance Android subtitle editor, timeline synchronizer, batch processor, and video hardcoder built natively with Kotlin and Jetpack Compose. Designed for content creators, subtitlers, video editors, and translators who demand frame-accurate precision, interactive canvas drag-and-drop subtitle positioning, and blazing-fast hardware-accelerated video rendering.

---

## ✨ Key Features & Capabilities

- 📺 **Fullscreen Live Video Playback & Editing**:
  - Full-screen edge-to-edge video preview with cinema-grade controls.
  - Interactive on-video subtitle dragging with real-time safe-area bounding boxes and coordinate crosshairs directly in fullscreen.
  - Frame-stepping (±1 frame at ~33ms precision), 5-second fast seek, multi-speed playback (0.5x – 2.0x), and loop toggling.
  - Aspect ratio toggle (Fit / Zoom) and quick-access styling sheets directly inside the fullscreen theater view.
- ⏱️ **Multitrack Canvas Timeline Scrubber**:
  - Hardware-accelerated 60 FPS timeline scrubber rendering cue intervals on a custom Canvas without Compose recomposition bottlenecks.
  - Visual cue blocks with real-time playhead tracking, drag-to-seek, cue splitting, and micro-nudging (±0.1s / ±0.5s).
- 🔤 **Universal Subtitle Format Support**:
  - **SRT** (SubRip Subtitle format with HTML markup formatting `<b>`, `<i>`, `<u>`)
  - **WebVTT** (`.vtt` format with position, line, and alignment metadata)
  - **ASS / SSA** (Advanced SubStation Alpha with `\pos(x,y)`, `\an`, `\c&HBBGGRR&`, font overrides, and custom margins)
- 🎨 **Interactive Drag-and-Drop Caption Layout Engine**:
  - Freely drag subtitle overlays directly across video viewports.
  - 9-point anchor alignment presets (Top-Left, Center, Bottom-Center, etc.) with custom coordinate overrides.
  - Real-time styling: Text colors, stroke outline width/color, semi-transparent background box, corner radius, and font scaling.
- ⚡ **Hardware-Accelerated Video Exporter (`MediaCodec` + `MediaMuxer`)**:
  - **Lossless Soft-Muxing**: Remux video, audio, and subtitle streams in seconds without re-encoding quality loss.
  - **Hardcoded Burn-In**: High-bitrate AVC/H.264 video hardware encoder with pixel-perfect subtitle rasterization directly onto video frames.
  - Resolution presets: Native Source, 1080p Full HD, and 720p HD.
- 🧩 **Multi-Threaded CPU/GPU Batch Hub**:
  - Bulk subtitle format conversion (e.g., SRT ↔ VTT ↔ ASS).
  - Mass timecode synchronization & shifting.
  - Configurable thread pools (1–8 CPU cores) and batch queue management.
- 📱 **Universal Android 16 & Modern Device Compatibility**:
  - Optimized for Android 16 (API 36) through Android 7.0 (API 24) on standard phones, foldable devices, and tablets.
  - ProGuard/R8 minification and resource shrinking for a compact APK footprint.

---

## 🛠️ Architecture & Tech Stack

| Module | Tech Stack / Library |
| :--- | :--- |
| **Language** | Kotlin 1.9+ |
| **UI Framework** | Jetpack Compose + Material Design 3 |
| **Video Player** | AndroidX Media3 ExoPlayer (`1.2.1`) |
| **Data Storage** | SharedPreferences JSON + Local Cache |
| **Code Processing** | Coroutines Flow + `Executors.newFixedThreadPool` |
| **Hardware Video** | Android `MediaCodec`, `MediaMuxer`, `MediaExtractor` |
| **Build System** | Gradle (Kotlin DSL) + KSP |

---

## 🚀 Building the Project

### Prerequisites

- Android Studio Jellyfish (2023.3.1) or higher
- JDK 17
- Android SDK 34

### Building via Command Line

```bash
# Clone the repository
git clone https://github.com/your-username/subvid-studio.git
cd subvid-studio

# Build Debug APK
./gradlew assembleDebug

# Build Signed Release APK
./gradlew assembleRelease
```

The compiled APKs will be located at:
- **Debug**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release**: `app/build/outputs/apk/release/app-release.apk`

---

## 🤖 GitHub Actions Automated CI/CD Workflow

This repository includes a pre-configured GitHub Actions workflow in `.github/workflows/build-release.yml`.

When code is pushed to `main` / `master` or a new version tag (`v*`) is created:
1. The project automatically builds both **Debug** and **Signed Release** APKs.
2. The APKs are automatically uploaded to the **GitHub Releases** tab attached to the git release.

### Signing Key Setup in GitHub Secrets

To sign release builds with your custom keystore in GitHub Actions, add the following secrets to your GitHub repository (`Settings -> Secrets and variables -> Actions`):

- `KEYSTORE_BASE64`: Base64 encoded string of your release `.keystore` or `.jks` file.
- `STORE_PASSWORD`: Password for the keystore.
- `KEY_ALIAS`: Alias of your release key.
- `KEY_PASSWORD`: Password for your release key.

*Note: If no secrets are set, the workflow automatically generates a temporary signing key in CI so builds complete and publish release APKs seamlessly.*

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
