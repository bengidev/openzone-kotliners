# OpenZone Kotliners

Native mobile AI assistant for Android — bring AI models to your pocket to help get work done.

[![Android CI](https://github.com/bengidev/openzone-kotliners/actions/workflows/android-ci.yml/badge.svg)](https://github.com/bengidev/openzone-kotliners/actions/workflows/android-ci.yml)
[![Platform](https://img.shields.io/badge/platform-Android%2011%2B-blue.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-green.svg)](https://developer.android.com/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

OpenZone Kotliners is a **native Android AI assistant** built with Kotlin and Jetpack Compose. It integrates mobile-first UX with AI models so users can complete real tasks — drafting, summarizing, planning, Q&A, and more — directly from their device.

## Features

- 🤖 **AI model integration** — connect on-device or remote AI models to power assistant workflows
- 📱 **Native Android UI** — fluid, platform-native interface with Jetpack Compose
- 💾 **Local-first persistence** — SwiftData-inspired local storage for chats, history, and context
- 🔒 **Privacy-aware** — designed to keep user data on-device where possible
- ⚡ **Task-oriented** — focused on helping users finish work, not just chat
- 🛡️ **Decompose architecture** — structured state management with unidirectional data flow

## Tech Stack

| Layer         | Technology                        |
|---------------|-----------------------------------|
| **Language**  | Kotlin 2.2.10                     |
| **UI**        | Jetpack Compose BOM 2026.02.01    |
| **Architecture** | Decompose 3.5.0 + Coroutines   |
| **Persistence** | Room 2.7.1, DataStore           |
| **Networking** | OkHttp 4.12.0, SSE streaming     |
| **Security**  | EncryptedSharedPreferences        |
| **Min SDK**   | 30 (Android 11)                   |
| **Target SDK** | 36                                |
| **App Category** | Productivity                   |

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 21
- Android SDK with API 36

### Build & Run

1. Clone the repository: `git clone https://github.com/bengidev/openzone-kotliners.git`
2. Open in Android Studio
3. Sync Gradle and run on an emulator or physical device (API 30+)

**First run**: complete the onboarding product tour. Configure your AI provider/model and API key from Settings/Home before sending messages. The app supports OpenRouter as the initial provider.

## Project Structure

The app ships as a single Gradle module (`:app`) with feature-oriented internal packages:

```
app/src/main/java/io/github/bengidev/openzone/
├── MainActivity.kt              # Composition root + routing
├── onboarding/                  # First-run flow (5 pages)
│   ├── domain/                  # Page models, types, prompts
│   ├── application/             # OnboardingComponent + state
│   ├── infrastructure/          # DataStore repository
│   ├── presenter/               # UI screens + visuals
│   └── theme/                   # Feature theming
├── home/                        # Landing screen + composer
│   ├── domain/                  # Composer models
│   ├── application/             # HomeComponent (owns Chat + SidePanel children)
│   ├── presenter/               # Welcome view, composer, top bar
│   └── theme/                   # Feature theming
├── chat/                        # Live conversation + streaming
│   ├── domain/                  # Message types, streaming events
│   ├── application/             # ChatComponent (SSE streaming, persistence)
│   ├── infrastructure/          # Room database, OkHttp streaming client
│   │   ├── persistence/         # ChatHistoryDao, entities, mappers
│   │   └── wire/                # OpenAI wire protocol models
│   ├── presenter/               # Thread view, message rows, reasoning cards
│   └── theme/                   # Feature theming
├── sidepanel/                   # Slide-in panel (session + setting scopes)
│   ├── domain/                  # SessionSection enum
│   ├── application/             # SessionComponent + SettingComponent
│   └── presenter/               # Sidebar view
├── settings/                    # DEPRECATED: migrating to sidepanel/
├── shared/
│   ├── externals/               # Feature-neutral adapters
│   │   ├── networking/          # ChatProvider, AuthScheme, SseLineDecoder, ChatModel
│   │   ├── preference/          # ComposerReasoningLevel, ProviderPreferenceStore
│   │   └── security/            # CredentialStore, EncryptedCredentialStore
│   └── [ui/]                    # Future reusable UI primitives; currently absent
└── ui/theme/                    # App theme (OpenZonePalette, Typography, AppTheme)
```

### Architecture

**Decompose + MutableValue** for state management, mirroring iOS TCA reducer pattern:
- Each feature owns a `Component` with `MutableValue<State>` as single source of truth
- Intent methods are the only way to mutate state
- Compose views observe state via `subscribeAsState()` / `collectAsState()`
- Dependencies injected via constructor from `MainActivity` composition root

**Layered feature packages** enforce separation of concerns:
- `domain/` — pure Kotlin models (no Android imports)
- `application/` — Decompose components + state
- `infrastructure/` — Room, DataStore, EncryptedSharedPreferences
- `presenter/` — Compose UI
- `theme/` — feature-specific theming

See [docs/architecture/modules.md](docs/architecture/modules.md) for ownership rules and naming conventions.

### Onboarding Layering

Persistence and navigation stay behind abstractions so UI and storage can evolve independently:

| Layer          | Responsibility                                      |
|----------------|-----------------------------------------------------|
| **domain**     | Page models, types, prompts                         |
| **application** | OnboardingComponent + state                        |
| **infrastructure** | DataStore-backed repository                      |
| **presenter**  | UI screens + visual factory                         |

Wire the repository from `MainActivity` (or a future DI graph); do not call DataStore directly from composables.

## CI/CD

- **Android CI** runs on pushes and pull requests to `main`
- Checks: `./gradlew :app:assembleDebug` + `:app:testDebugUnitTest`
- See [.github/workflows/android-ci.yml](.github/workflows/android-ci.yml)

## Documentation

| Topic | Location |
|-------|----------|
| Doc index | [docs/README.md](docs/README.md) |
| Contributing & issues | [CONTRIBUTING.md](CONTRIBUTING.md) |
| Agent / skill configuration | [AGENTS.md](AGENTS.md) → [docs/agents/](docs/agents/) |
| Domain glossaries (multi-context) | [CONTEXT-MAP.md](CONTEXT-MAP.md) → `docs/contexts/<scope>/<Scope>-CONTEXT.md` |
| Architecture decisions (ADRs) | [docs/adr/](docs/adr/) |
| Module & package layout | [docs/architecture/modules.md](docs/architecture/modules.md) |
| Coroutine & concurrency | [docs/architecture/coroutine-concurrency.md](docs/architecture/coroutine-concurrency.md) |

Issues and PRDs are tracked on [GitHub Issues](https://github.com/bengidev/openzone-kotliners/issues). See [docs/agents/issue-tracker.md](docs/agents/issue-tracker.md) for CLI conventions.

## Contributing

Contributions welcome. Open an issue or PR on `bengidev/openzone-kotliners`. See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[MIT](LICENSE) © [bengidev](https://github.com/bengidev)
