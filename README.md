# SubVid Studio 🎬⚡

**SubVid Studio** is a professional, high-performance Android subtitle editor, timeline synchronizer, batch processor, and video hardcoder designed with Kotlin and Jetpack Compose.

---

## ✨ Features

- **Project Management**: Home screen dashboard for quick project switching, metadata tracking, and history management.
- **Timeline Scrubber**: Hardware-accelerated 60 FPS multitrack timeline scrubber with visual cue blocks, drag-and-drop seeking, split/merge controls, and micro-nudging (±0.1s / ±0.5s).
- **Format Support**:
  - **SRT** (SubRip Subtitle)
  - **VTT** (WebVTT with position and line tags)
  - **ASS / SSA** (Advanced SubStation Alpha with position `\pos(x,y)` and custom color/style overrides)
- **Interactive Drag-and-Drop Editor**: Real-time canvas overlay allowing interactive dragging of subtitles directly over the video player with exact X/Y positioning.
- **Batch Processing Hub**: Process multiple subtitle and video operations in bulk:
  - Format conversions (e.g. SRT to ASS / VTT)
  - Global time shifting
  - Lossless MKV/MP4 soft-muxing
  - Multi-threaded CPU / Hardware-accelerated video burn-in
- **Hardware-Accelerated Video Export**:
  - Lossless container multiplexing
  - High-bitrate hardcoded burn-in using Android `MediaCodec` and `MediaMuxer`
- **Modern Immersive Design**: Material Design 3 theme with pure vector SVG graphics and dynamic touch target ergonomics.

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
