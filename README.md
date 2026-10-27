# Fast Download Manager (FDM) — Jetpack Compose Redesign

A modern, streamlined redesign of the Android Download Manager & Media Browser, rebuilt with **Jetpack Compose**, **Material 3 (Material You)**, **Kotlin Coroutines & Flow**, and clean architecture.

---

## 1. Project Overview

The original APK ([IDM-18.2.apk](../IDM-18.2.apk)) contained over 5,700 classes and 2,600 resources. The original codebase suffered from extreme legacy Android View bloat:
- Monolithic God Activity (`MainActivity.java` with 21,775 lines) combining raw SQLite queries, broadcast receivers, and ad-blockers.
- Overwhelming UI with 30+ tiny icon buttons across top and bottom toolbars.
- Complex dialogs (`Downloader.java`) exposing dozens of confusing checkboxes and fields on a single screen.

### Redesign Goals
- **Simplify the UX**: Focus on the 3 core tasks: **Downloads**, **Browser & Media Sniffing**, and **Settings**.
- **Material 3 & Edge-to-Edge**: Modern dynamic color palette, smooth animated progress indicators, and rounded surface cards.
- **Modern Jetpack Architecture**: Unidirectional Data Flow (UDF), `ViewModel`, `StateFlow`, and coroutine-based chunked streaming.

---

## 2. Directory Structure

```
rea/
├── IDM-18.2.apk               # Original APK
├── idm-manifest.txt           # Extracted package analysis manifest
├── decompiled_src/            # Complete decompiled output from JADX
│   ├── sources/               # 5,762 decompiled Java classes (idm.*, acr.*, etc.)
│   └── resources/             # AndroidManifest.xml, assets, res/ drawables & layouts
└── idm_compose/               # Modern Jetpack Compose Redesign Codebase
    ├── build.gradle.kts
    ├── settings.gradle.kts
    └── app/
        ├── build.gradle.kts
        └── src/main/
            ├── AndroidManifest.xml
            └── java/com/legozia/fdm/
                ├── MainActivity.kt          # Edge-to-edge entrypoint & Intent handling
                ├── theme/                   # Material 3 colors, typography & dynamic theme
                ├── model/                   # Domain models (DownloadItem, DownloadCategory, SniffedMedia)
                ├── engine/                  # Coroutine-based Range chunk downloader & Media sniffer
                ├── viewmodel/               # DownloadsViewModel, BrowserViewModel, SettingsViewModel
                └── ui/
                    ├── navigation/          # Bottom Navigation Suite & AppNavHost
                    ├── components/          # DownloadCard, AddDownloadBottomSheet, FilterChips, etc.
                    └── screens/             # DownloadsScreen, BrowserScreen, SettingsScreen
```

---

## 3. Core Components & Enhancements

### 1. Unified Downloads Dashboard (`DownloadsScreen.kt`)
- **Category Filter Chips**: Instant filtering by *All*, *Active*, *Videos*, *Music*, *Documents*, *Archives*, *Apps*, and *Completed*.
- **Real-Time Speed & ETA**: Live aggregated speed pill (e.g. `⚡ 14.2 MB/s`) and dynamic ETA calculation.
- **Card Actions**: Direct pause, resume, cancel, and retry controls with animated linear progress indicators.

### 2. Streamlined Download Modal (`AddDownloadBottomSheet.kt`)
- Replaces the 1,000+ line legacy dialog with a clean Material 3 modal.
- Automatically parses filename and clipboard URL.
- Advanced settings (connection threads: 1 to 32 parts, headers) are cleanly tucked into a collapsible accordion.

### 3. Integrated Media Sniffer Browser (`BrowserScreen.kt`)
- Clean in-app browser with integrated address bar, back/forward navigation, and reload.
- **Non-intrusive Sniffer FAB**: Automatically intercepts network requests for media streams (`.m3u8`, `.mpd`, `.mp4`, `.mp3`). When media is detected, a floating pill appears (`"3 Media Detected"`).
- **1-Tap Download Sheet (`SniffedMediaBottomSheet.kt`)**: Displays captured streams with resolution tags (1080p, 720p, Adaptive) ready to download with one tap.

### 4. Grouped Preferences (`SettingsScreen.kt`)
- Simplifies dozens of fragmented legacy settings into clean Material 3 cards:
  - Thread concurrency slider (1–32 connections).
  - Wi-Fi only restriction toggle.
  - Built-in ad-blocker toggle.
  - Material You dark mode.
