# Fast Download Manager (FDM)

Fast Download Manager (FDM) is a modern, high-performance Android download manager and media browser built with Jetpack Compose, Material 3, and Kotlin Coroutines. It features a clean architecture designed for speed, efficiency, and a seamless user experience.

---

## 1. Features & Architecture Highlights

- **Streamlined UX**: Focused on 3 core tasks: **Downloads**, **Browser & Media Sniffing**, and **Settings**.
- **Material 3 & Edge-to-Edge**: Modern dynamic color palette, smooth animated progress indicators, and rounded surface cards.
- **Modern Jetpack Architecture**: Unidirectional Data Flow (UDF), `ViewModel`, `StateFlow`, and coroutine-based chunked multi-threaded streaming.

---

## 2. Directory Structure

```
FDM/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/legozia/fdm/
            ├── MainActivity.kt          # Edge-to-edge entrypoint & Intent handling
            ├── theme/                   # Material 3 colors, typography & dynamic theme
            ├── model/                   # Domain models (DownloadItem, DownloadCategory, SniffedMedia)
            ├── engine/                  # Multi-threaded Chunk Downloader & Media Sniffer engine
            ├── viewmodel/               # DownloadsViewModel, BrowserViewModel, SettingsViewModel
            └── ui/
                ├── navigation/          # Bottom Navigation & AppNavHost
                ├── components/          # DownloadCard, AddDownloadBottomSheet, FilterChips, etc.
                └── screens/             # DownloadsScreen, BrowserScreen, SettingsScreen
```

---

## 3. Core Features & Capabilities

### 1. Unified Downloads Dashboard (`DownloadsScreen.kt`)
- **Category Filter Chips**: Instant filtering by *All*, *Active*, *Videos*, *Music*, *Documents*, *Archives*, *Apps*, and *Completed*.
- **Real-Time Speed & ETA**: Live aggregated speed pill (e.g. `⚡ 14.2 MB/s`) and dynamic ETA calculation.
- **Card Actions**: Direct pause, resume, cancel, and retry controls with animated linear progress indicators.

### 2. Streamlined Download Modal (`AddDownloadBottomSheet.kt`)
- Clean Material 3 modal sheet.
- Automatically parses filename and clipboard URL.
- Advanced settings (connection threads: 1 to 32 parts, custom headers) tucked into a collapsible accordion.

### 3. Integrated Media Sniffer Browser (`BrowserScreen.kt`)
- Clean in-app browser with integrated address bar, back/forward navigation, and reload.
- **Non-intrusive Sniffer FAB**: Automatically intercepts network requests for media streams (`.m3u8`, `.mpd`, `.mp4`, `.mp3`). When media is detected, a floating badge appears (`"3 Media Detected"`).
- **1-Tap Download Sheet (`SniffedMediaBottomSheet.kt`)**: Displays captured streams with resolution tags (1080p, 720p, Adaptive) ready to download with one tap.

### 4. Grouped Preferences (`SettingsScreen.kt`)
- Material 3 settings configuration:
  - Thread concurrency slider (1–32 connections).
  - Wi-Fi only restriction toggle.
  - Built-in ad-blocker toggle.
  - Material You dark mode.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
Developed by **LEGOZIA GROUP**.

