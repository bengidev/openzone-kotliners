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

```text
app/
├── src/main/java/io/github/bengidev/OpenZone/MainActivity.kt  # App entry point
├── src/main/java/io/github/bengidev/OpenZone/ui/theme/        # Compose theme
├── src/main/res/                                              # Android resources
app/src/test/                                                  # Unit tests
app/src/androidTest/                                           # Instrumented tests
```

## ✅ CI/CD

- **Android CI** runs on pushes and pull requests to `main`.
- **Android Release** runs on version tags like `v1.0.0` and manual dispatch.
- Debug and release APKs are uploaded as GitHub Actions artifacts.

## 🤝 Contributing

Contributions are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

## 📄 License

[MIT](LICENSE) © [bengidev](https://github.com/bengidev)
