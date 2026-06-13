# Context Map

This repository uses a domain-driven documentation structure where each bounded context has its own `*-CONTEXT.md` file documenting that context's domain, responsibilities, and relationships with other contexts.

## Contexts

| Context | Location | Description |
|---------|----------|-------------|
| [App Shell](docs/contexts/app/App-CONTEXT.md) | `MainActivity.kt` | Application entry point, Decompose composition, root routing |
| [Home Feature](docs/contexts/home/Home-CONTEXT.md) | `home/` | Main landing surface, composer, model catalog |
| [Chat Feature](docs/contexts/chat/Chat-CONTEXT.md) | `chat/` | Message streaming, persistence, and history entities |
| [Onboarding Feature](docs/contexts/onboarding/Onboarding-CONTEXT.md) | `onboarding/` | First-time user setup and welcome flow |
| [SidePanel Feature](docs/contexts/sidepanel/SidePanel-CONTEXT.md) | `sidepanel/` | Session browser + setting scope (slide-in panel) |
| [Externals](docs/contexts/externals/Externals-CONTEXT.md) | `shared/externals/` | External system adapters (APIs, storage, preferences) |
| [Shared](docs/contexts/shared/Shared-CONTEXT.md) | `ui/theme/` | App-wide theme primitives |

## Architecture Overview

### Feature-Oriented Package Layout

Each feature follows a layered structure mirroring iOS OpenZone:

```text
io.github.bengidev.openzone/
├── MainActivity.kt                # Composition root, routing
├── onboarding/                    # Onboarding feature
│   ├── domain/                    # Models (pure Kotlin)
│   ├── application/               # Decompose components, state
│   ├── infrastructure/            # Room, DataStore, EncryptedSharedPreferences
│   ├── presenter/                 # Compose UI
│   └── theme/                     # Feature design tokens
├── home/                          # Home feature (same layering)
├── chat/                          # Chat feature (same layering)
├── sidepanel/                     # SidePanel session + setting sub-scopes
├── settings/                      # DEPRECATED — migrating to sidepanel/
├── shared/
│   ├── externals/                 # Feature-neutral adapters
│   │   ├── networking/            # ChatProvider, AuthScheme, SseLineDecoder, ChatModel, ModelCatalogStore
│   │   ├── security/              # CredentialStore, MutableCredentialStore, EncryptedCredentialStore
│   │   └── preference/            # ComposerReasoningLevel, ProviderPreference, ProviderPreferenceStore
│   └── [ui/]                       # Future shared UI primitives; currently absent
└── ui/
    └── theme/                     # Palette, Typography, AppTheme
```

### Scope Prefixes

Types use prefixes to indicate ownership and scope:

- **`Home…`** — Home feature types (e.g., `HomeComposerView`, `HomeParticleOrbView`)
- **`Chat…`** — Chat feature types (e.g., `ChatMessage`, `ChatThreadView`)
- **`Onboarding…`** — Onboarding feature types (e.g., `OnboardingPage`, `OnboardingView`)
- **`SidePanel…`** — SidePanel feature types (e.g., `SidePanelSessionComponent`, `SidePanelSettingView`)
- **`Shared…`** — Reserved for future cross-feature reusable components; currently no `shared/ui/` types exist

### Type Naming Conventions

#### Persistence Types

Suffix: `Entity` (Room), `Store` (repository interfaces)

**Room entities** (`chat/infrastructure/persistence/*.kt`):
- Use `Entity` suffix for Room `@Entity` types
- Prefix with sub-scope name if the type belongs to a sub-domain
- Example: `ConversationEntity`, `MessageEntity` (Chat feature)

```kotlin
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lastModified: Long
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val content: String,
    val role: String
)
```

**Repository interfaces** (application layer):
- Use `Store` suffix for persistence abstractions
- Example: `ChatHistoryStore`, `OnboardingRepository`, `ModelCatalogStore`

#### Component Types

Decompose components use `Component` suffix:

```kotlin
class HomeComponent(
    componentContext: ComponentContext,
    private val chatComponentFactory: ChatComponent.Factory,
    private val sessionComponentFactory: SidePanelSessionComponent.Factory
) : ComponentContext by componentContext {
    // State and intent
}
```

**Pattern:** `<FeatureName>Component` (e.g., `HomeComponent`, `ChatComponent`, `SidePanelSessionComponent`)

#### View Types

Compose views use `View` or `Screen` suffix:

```kotlin
@Composable
fun HomeView(component: HomeComponent) {
    // UI from component state
}

@Composable
fun ChatThreadView(component: ChatComponent) {
    // Render conversation
}
```

**Pattern:** Descriptive name + `View` suffix for components, `Screen` suffix for top-level navigation destinations.

### Access Control

All types default to `internal`. Use `public` only when promoting a package to an internal Gradle module boundary. This keeps the API surface implicit until you deliberately expose it across a module boundary.

### Coroutine Concurrency

All code should follow Kotlin best practices for structured concurrency:

```kotlin
// Launch work in component's CoroutineScope
componentScope.launch {
    repository.fetchData()
}

// Use Flow for streaming data
fun streamMessages(): Flow<ChatStreamingEvent> = flow {
    emit(/* ... */)
}

// Keep UI state on Main dispatcher
viewModelScope.launch(Dispatchers.Main) {
    state.value = state.value.copy(isLoading = true)
}
```

## Dependency Flow

```text
┌─────────────────────────────────────────────────────────┐
│                        App Layer                         │
│  MainActivity → Root Components → Feature Stores         │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                     Feature Layer                         │
│  Home ↔ Chat ↔ Onboarding ↔ SidePanel                    │
│  (communicate via parent-child composition, callbacks)   │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                    Externals Layer                        │
│  ChatProvider • CredentialStore • ProviderPreferenceStore │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                     Shared Layer                          │
│  Theme • UI Components • Extensions                      │
└─────────────────────────────────────────────────────────┘
```

## Cross-Feature Communication

Features communicate through **parent-child composition and callbacks** rather than direct dependencies:

```kotlin
// HomeComponent creates ChatComponent as child
class HomeComponent(
    componentContext: ComponentContext,
    private val chatComponentFactory: ChatComponent.Factory
) : ComponentContext by componentContext {
    
    private val childSlot = childSlot(
        source = navigation,
        serializer = Navigation.serializer(),
        handleBackButton = true,
        childFactory = { config, childComponentContext ->
            when (config) {
                is Navigation.Chat -> chatComponentFactory.create(
                    conversationId = config.conversationId,
                    onBack = { navigation.navigate(Navigation.Welcome) }
                )
            }
        }
    )
}
```

## Navigation Guide

### Adding a New Feature

1. Create feature package: `io.github.bengidev.openzone.yourfeature/`
2. Add layered subdirectories: `domain/`, `application/`, `infrastructure/`, `presenter/`, `theme/`
3. Create Decompose component in `application/YourFeatureComponent.kt`
4. Add context file: `docs/contexts/yourfeature/YourFeature-CONTEXT.md`
5. Update this Context Map with new feature entry
6. Wire up in `MainActivity` composition root

### Modifying Existing Features

1. Read the feature's `*-CONTEXT.md` file first
2. Follow the layered structure (domain models, application logic, infrastructure adapters, presenter UI)
3. Use appropriate prefixes for new types
4. Keep types `internal` unless cross-module sharing is required
5. Update context file if adding new responsibilities

### Working with Persistence

1. Room entities go in `infrastructure/persistence/` with `Entity` suffix
2. Use Room `@Entity` annotation and `@PrimaryKey` for IDs
3. Keep entities as pure data holders (no business logic)
4. Create repository interface in `application/` with `Store` suffix
5. Implement repository in `infrastructure/` using Room DAOs

### Integrating External APIs

1. API adapters go in `shared/externals/` with appropriate scope prefix
2. Expose via interfaces in `shared/externals/` (e.g., `CredentialStore`, `ProviderPreferenceStore`)
3. Features wire concrete implementations in `MainActivity` composition root
4. Document in `Externals-CONTEXT.md`

## Recent Changes

1. **Feature-oriented package layout** — Features organized into domain/application/infrastructure/presenter/theme layers instead of flat packages
2. **Scope prefixes** — Added `Home…`, `Chat…`, `Onboarding…`, `SidePanel…` prefixes for clarity
3. **Persistence entity naming** — Used `Entity` suffix for Room types, `Store` suffix for repository abstractions
4. **Composition root pattern** — `MainActivity` creates Decompose components and injects dependencies via constructors
5. **SidePanel migration** — Consolidated session and setting scopes into `sidepanel/` package, deprecated old `settings/` and `HomeSidebarView`

## Documentation Conventions

- **Location:** All docs in `docs/` directory
- **Naming:** `<Context>-CONTEXT.md` for context documentation
- **Content:** Each context file includes Purpose, Responsibilities, Dependencies, State Management, and External Integrations sections
- **Code examples:** Show actual patterns from codebase, not theoretical examples
- **Tables:** Use for summarizing type relationships and responsibilities

## File Index

```text
docs/
├── CONTEXT-MAP.md                    # This file
├── architecture/
│   ├── modules.md                    # Organization rules
│   └── coroutine-concurrency.md      # Coroutine best practices
└── contexts/
    ├── app/
    │   └── App-CONTEXT.md
    ├── chat/
    │   └── Chat-CONTEXT.md
    ├── externals/
    │   └── Externals-CONTEXT.md
    ├── home/
    │   └── Home-CONTEXT.md
    ├── onboarding/
    │   └── Onboarding-CONTEXT.md
    ├── shared/
    │   └── Shared-CONTEXT.md
    └── sidepanel/
        ├── SidePanel-CONTEXT.md
        ├── SidePanelSession-CONTEXT.md
        └── SidePanelSetting-CONTEXT.md
```

## System-wide ADRs

Architectural decisions live in [`docs/adr/`](docs/adr/). When a decision affects multiple contexts (e.g., persistence strategy, external API integration), document it there rather than duplicating across context files.

## Architecture Layout Rules

Module and package ownership rules are documented in [`docs/architecture/modules.md`](docs/architecture/modules.md). This includes:

- Feature package boundaries
- Shared UI and externals ownership
- Dependency direction rules
- Future internal library promotion path

## Agent and Workflow Configuration

Automated agents and workflows read these files for conventions:

- [`AGENTS.md`](AGENTS.md) — Agent skill index and coding rules
- [`docs/agents/issue-tracker.md`](docs/agents/issue-tracker.md) — GitHub Issues CLI conventions
- [`docs/agents/triage-labels.md`](docs/agents/triage-labels.md) — Issue triage label vocabulary
- [`docs/agents/domain.md`](docs/agents/domain.md) — How agents consume context and ADR documentation

## Product Documentation

- [`README.md`](README.md) — Build, project structure, CI/CD
- [`ABOUT.md`](ABOUT.md) — Product vision and principles
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — How to contribute and report issues
- [`SECURITY.md`](SECURITY.md) — Private vulnerability reporting
