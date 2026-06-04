# OpenZone Kotliners

> Native mobile AI assistant for Android — bring AI models to your pocket to help get work done.

[![Android CI](https://github.com/bengidev/openzone-kotliners/actions/workflows/android-ci.yml/badge.svg)](https://github.com/bengidev/openzone-kotliners/actions/workflows/android-ci.yml)
[![Platform](https://img.shields.io/badge/platform-Android%2011%2B-blue.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-green.svg)](https://developer.android.com/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

OpenZone Kotliners is a **native Android AI assistant** built with Kotlin and Jetpack Compose. It integrates mobile-first UX with AI models so users can complete real tasks — drafting, summarizing, planning, Q&A, and more — directly from their device.

## ✨ Features

- 🤖 **AI model integration** — connect on-device or remote AI models to power assistant workflows
- 📱 **Native Jetpack Compose** — fluid, platform-native Android interface
- 💾 **Local-first persistence ready** — designed for chats, history, and context storage
- 🔒 **Privacy-aware** — designed to keep user data on-device where possible
- ⚡ **Task-oriented** — focused on helping users finish work, not just chat
- 🛡️ **Modern Kotlin** — built with Kotlin, Compose, and Android architecture best practices

## 🧱 Tech Stack

| Layer        | Technology            |
|--------------|-----------------------|
| Language     | Kotlin 2.2.10         |
| UI           | Jetpack Compose       |
| Design       | Material 3            |
| Min Target   | Android 11 / API 30+  |
| App Category | Productivity          |
| Build        | Android Gradle Plugin |

## 🚀 Getting Started

### Prerequisites

- Android Studio
- JDK 21
- Android SDK 36

### Build & Run

```bash
git clone https://github.com/bengidev/openzone-kotliners.git
cd openzone-kotliners
./gradlew assembleDebug
```

Open the project in Android Studio, select an emulator/device, and run the **app** configuration.

## 📂 Project Structure

The app ships as a single Gradle module (`:app`). Feature areas such as onboarding live as **internal packages** inside the app module (not separate Gradle library modules).

```text
app/
├── src/main/java/io/github/bengidev/openzone/
│   ├── MainActivity.kt                    # App entry point
│   ├── ui/theme/                          # App-wide Compose theme
│   └── onboarding/                        # Onboarding feature (internal module)
│       ├── OnboardingScreen.kt            # Root composable
│       ├── domain/                        # Page models and enums
│       ├── application/                   # OnboardingComponent, OnboardingState
│       ├── infrastructure/                # OnboardingRepository + DataStore impl
│       ├── presenter/                     # Compose UI and page visuals
│       └── theme/                         # Onboarding design tokens
├── src/main/res/                          # Android resources
├── src/test/                              # Unit tests
└── src/androidTest/                       # Instrumented tests
```

### Onboarding layering

Persistence and navigation stay behind abstractions so UI and storage can evolve independently:

| Layer | Responsibility |
|-------|----------------|
| `domain` | Onboarding pages, types, demo models |
| `application` | Flow state and actions (`OnboardingComponent`) |
| `infrastructure` | `OnboardingRepository` interface; `DataStoreOnboardingRepository` in the app |
| `presenter` | Jetpack Compose screens and per-page visuals |
| `theme` | Onboarding-specific colors, typography, spacing |

Wire the repository from `MainActivity` (or a future DI graph); do not call DataStore directly from composables.

## ✅ CI/CD

- **Android CI** runs on pushes and pull requests to `main`.
- **Android Release** runs on version tags like `v1.0.0` and manual dispatch.
- Debug and release APKs are uploaded as GitHub Actions artifacts.

## 🤝 Contributing

Contributions are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

## 📄 License

[MIT](LICENSE) © [bengidev](https://github.com/bengidev)
