# AI Phone Agent 🤖📱

> **Control your Android phone with natural language. One command, infinite possibilities.**

[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-orange.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

---

## Table of Contents

1. [What it does](#what-it-does)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [Prerequisites](#prerequisites)
5. [Building the App](#building-the-app)
6. [Configuration](#configuration)
7. [Changing AI Providers](#changing-ai-providers)
8. [Changing UI Theme / Colors](#changing-ui-theme--colors)
9. [Adding a New Tool](#adding-a-new-tool)
10. [Permissions Deep Dive](#permissions-deep-dive)
11. [Roadmap](#roadmap)

---

## What it does

AI Phone Agent turns your Android device into a fully AI-controlled assistant:

| Capability | Example Command |
|---|---|
| **File Management** | "Find all PDFs in Downloads and move them to a new folder called Documents" |
| **App Control** | "Open WhatsApp, click on Contacts, search for John" |
| **Image OCR** | "Read the text in the screenshot at /sdcard/screen.png" |
| **Multi-step Tasks** | "Find all invoice images, extract their totals, write a summary to invoices.txt" |
| **Device Info** | "What apps are installed that I haven't used in 30 days?" |

---

## Architecture

```
┌────────────────────────────────────────────┐
│              Presentation Layer             │
│  Jetpack Compose UI  •  MVVM ViewModels     │
└──────────────┬─────────────────────────────┘
               │
┌──────────────▼─────────────────────────────┐
│           Orchestration Layer               │
│  Router → Planner → Executor               │
│  (Plan-and-Execute: 1 AI call per task)     │
└──────────────┬─────────────────────────────┘
               │
┌──────────────▼─────────────────────────────┐
│              Tool Layer                     │
│  FileManager │ AppControl │ OCR │ Macros    │
└──────────────┬─────────────────────────────┘
               │
┌──────────────▼─────────────────────────────┐
│           Android System Layer              │
│  SAF │ AccessibilityService │ ML Kit        │
└────────────────────────────────────────────┘
```

**Key design decisions:**
- **Plan-and-Execute**: The AI generates one JSON plan per task, then the Executor runs it step-by-step locally — minimising API calls by 50–70%.
- **CacheManager**: File listings, OCR results, and app lists are cached with TTL to avoid repeated work.
- **Sandbox Mode**: All file operations can be restricted to a user-chosen folder — prevents accidents.
- **No root required**: Uses Android Accessibility Service for UI interaction, SAF for storage.

---

## Project Structure

```
app/src/main/java/com/aiphone/agent/
├── core/
│   ├── accessibility/     AIAccessibilityService.kt
│   ├── ai/
│   │   ├── AIProviderManager.kt
│   │   └── providers/     OpenAI / Anthropic / Gemini providers
│   ├── cache/             CacheManager.kt
│   ├── macro/             MacroEngine.kt
│   ├── orchestration/     Router → Planner → Executor
│   └── tools/             All tool implementations
├── data/
│   ├── local/
│   │   ├── database/      Room DB (conversations, messages, macros)
│   │   └── preferences/   SecurePreferences (encrypted API keys)
│   ├── remote/models/     API request/response models
│   └── repository/        Repository implementations
├── di/                    Hilt DI modules
├── domain/
│   ├── model/             Core domain models
│   ├── repository/        Repository interfaces
│   └── usecase/           Use cases
└── presentation/
    ├── chat/              Chat screen + ViewModel
    ├── macros/            Macros screen + ViewModel
    ├── navigation/        AppNavigation + Screen routes
    ├── onboarding/        Onboarding screen + ViewModel
    ├── settings/          Settings screen + ViewModel
    └── theme/             Colors, Typography, Theme
```

---

## Prerequisites

| Tool | Version |
|---|---|
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 17 |
| Android SDK | API 35 (compile), API 26 (min) |
| Gradle | 8.9 (via wrapper — no install needed) |

---

## Building the App

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_ORG/AIPhoneAgent.git
cd AIPhoneAgent

# 2. Open in Android Studio OR build from CLI:
./gradlew assembleDebug

# 3. Install on connected device:
./gradlew installDebug
```

The first build will download ~500 MB of Gradle dependencies. Subsequent builds are cached.

### Release Build
```bash
./gradlew assembleRelease
# APK output: app/build/outputs/apk/release/app-release-unsigned.apk
# You must sign with your keystore before distribution.
```

---

## Configuration

### Adding Your API Key (In-App)
1. Launch the app
2. Tap **Settings** (⚙️ icon)
3. Select your **AI Provider** (OpenAI, Anthropic, Gemini, OpenRouter)
4. Paste your **API Key** — it is immediately encrypted with AES-256-GCM via Android Keystore and never leaves your device.
5. Optionally change the **Model**.

### Sandbox Mode
Enable in Settings to restrict all file operations to a chosen directory. Recommended for safety on first use.

### Accessibility Service
Required for app control (clicking, typing, scrolling). Enable via:
1. Android Settings → Accessibility → Installed Services → **AI Phone Agent Control**

---

## Changing AI Providers

### Adding a New Provider

1. Add the new `ProviderType` entry in `domain/model/AIProvider.kt`:
```kotlin
MY_PROVIDER(
    displayName = "My Provider",
    baseUrl = "https://api.myprovider.com/v1/",
    defaultModel = "my-model-1",
    availableModels = listOf("my-model-1", "my-model-2")
)
```

2. Create `core/ai/providers/MyProvider.kt` extending `BaseAIProvider`. Implement the `chat()` function — format the request/response for your API.

3. Register in `AIProviderManager.kt` `buildProvider()` when block:
```kotlin
ProviderType.MY_PROVIDER -> MyProvider(apiKey, httpClient, moshi)
```

4. Done! The Settings screen will automatically show it.

### Changing the Default Model
Edit `ProviderType.defaultModel` in `domain/model/AIProvider.kt`.

---

## Changing UI Theme / Colors

All theme values are in `presentation/theme/`:

### Colors (`Color.kt`)
```kotlin
val BrandPrimary     = Color(0xFF6C63FF)  // ← Main accent colour
val BrandSecondary   = Color(0xFF03DAC6)  // ← Secondary accent
val Background       = Color(0xFF0F0F1A)  // ← App background
val Surface          = Color(0xFF1A1A2E)  // ← Card/surface colour
```
Change any hex values here and the entire app updates automatically.

### Typography (`Type.kt`)
Adjust `fontSize`, `fontWeight`, or `lineHeight` in the `AppTypography` object.

### Dark-only mode
The app is intentionally dark-only. To add a light theme, add `lightColorScheme(...)` in `Theme.kt` and use `isSystemInDarkTheme()` to switch.

---

## Adding a New Tool

1. Create a new class in `core/tools/` extending `BaseTool`:
```kotlin
class MyNewTool @Inject constructor(/* dependencies */) : BaseTool() {
    override val name = "my_new_tool"
    override val description = "What this tool does (shown to the AI)"
    override val parameters = mapOf(
        "input" to ToolParameter("Description of the input")
    )
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val input = param(params, "input")
        // ... do the work
        return ToolExecutionResult(success = true, output = "result here")
    }
}
```

2. Register it in `ToolRegistry.kt`:
```kotlin
class ToolRegistry @Inject constructor(
    // ... existing tools ...
    myNewTool: MyNewTool   // ← add here
) {
    private val tools = listOf(
        // ... existing tools ...
        myNewTool            // ← and here
    ).associateBy { it.name }
}
```

The AI will automatically discover and use it — its `name` and `description` are injected into the system prompt.

---

## Permissions Deep Dive

| Permission | Why it's needed | How to avoid |
|---|---|---|
| `INTERNET` | AI API calls | Cannot be removed |
| `READ_EXTERNAL_STORAGE` | File listing/reading (API < 33) | Use SAF DocumentPicker instead |
| `READ_MEDIA_IMAGES` | File listing (API 33+) | Use SAF DocumentPicker instead |
| `BIND_ACCESSIBILITY_SERVICE` | App control (clicking, typing) | Don't use app-control tools |
| `QUERY_ALL_PACKAGES` | Finding installed apps | Can remove if only using file tools |

### Google Play Concerns
If targeting Google Play, consider:
- Remove `QUERY_ALL_PACKAGES` (use `<queries>` elements instead)
- Avoid `MANAGE_EXTERNAL_STORAGE` — use SAF exclusively
- Accessibility Service requires thorough Play Console justification

---

## Roadmap

- [ ] v1.1 — On-device intent classifier (TFLite) for offline simple commands
- [ ] v1.2 — Parallel step execution for independent plan steps
- [ ] v1.3 — Macro recorder (record AI actions for replay)
- [ ] v2.0 — Scheduled tasks (cron-like automation)
- [ ] v2.1 — Cross-device sync via encrypted cloud backup
- [ ] v3.0 — Plugin system for third-party tools

---

## License

Proprietary. All rights reserved. See `LICENSE` file.
