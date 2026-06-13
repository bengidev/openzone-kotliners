# Module Layout

Part of this repo's multi-context documentation. See [CONTEXT-MAP.md](../../CONTEXT-MAP.md) for per-feature glossaries and [docs/agents/domain.md](../agents/domain.md) for how agents consume domain docs.

OpenZone uses feature-oriented packages inside the app target. Each feature package hosts its own domain / application / infrastructure / presenter / theme sub-packages. State management uses Decompose components with `MutableValue` state and intent methods.

## Module map

The app is a single `:app` module that composes feature packages as siblings, plus cross-cutting shared and externals packages. Side panel is one module hosting two sub-scopes — **session** (saved-conversation browsing) and **setting** (app preferences):

```text
App
├── Shared                # UI primitives + theme
├── Externals             # Feature-neutral adapters (Networking, Preference, Security)
├── Onboarding
├── Home
├── Chat
└── SidePanel
    ├── Session           # SidePanelSession… (saved-conversation browsing, was "history chat")
    └── Setting           # SidePanelSetting… (app preferences, deprecated settings/ package in transition)
```

## Current layout

```text
io.github.bengidev.openzone/
├── MainActivity.kt                    # Entry point, routing, AppTheme + OpenZoneTheme
├── onboarding/                        # Onboarding feature
│   ├── domain/                        # OnboardingPage, OnboardingPageType, OnboardingPromptOption, OnboardingQueueItem, OnboardingFeatureHighlight
│   ├── application/                   # OnboardingState, OnboardingComponent (Decompose: MutableValue + intents)
│   ├── infrastructure/                # DataStoreOnboardingRepository (OnboardingRepository interface)
│   ├── presenter/                     # OnboardingScreen, FeaturePageView, PageVisualFactory, pages 1-5 UI
│   └── theme/                         # OnboardingColorScheme, OnboardingTypography, OnboardingTheme
├── home/                              # Home feature
│   ├── domain/                        # ComposerSpeedMode, ComposerContextUsage
│   ├── application/                   # HomeState, HomeComponent (owns ChatComponent + SidePanelSessionComponent children)
│   ├── presenter/                     # HomeScreen, HomeWelcomeView, HomeComposerView, HomeTopBar, particle orb
│   └── theme/                         # HomeColorScheme, HomeTypography, HomeTheme
├── chat/                              # Chat feature
│   ├── domain/                        # ChatConversation, ChatMessage, ChatMessageRole, ChatRequest, ChatStreamingEvent, ChatStreamError
│   ├── application/                   # ChatState, ChatComponent (SSE streaming, merges deltas by message id)
│   ├── infrastructure/                # ChatAPIClient interface, OpenAiCompatibleStreamingClient, ChatHistoryStore, persistence/ (Room)
│   │   ├── persistence/
│   │   │   ├── ChatDatabase.kt        # Room database, MIGRATION_1_2
│   │   │   ├── ChatHistoryDao.kt      # DAO with CRUD + search/pin/rename
│   │   │   ├── ConversationEntity.kt
│   │   │   ├── MessageEntity.kt
│   │   │   └── ChatMessageMapper.kt   # Entity ↔ domain mapper
│   │   └── wire/
│   │       └── OpenAiWireModels.kt    # ChatCompletionRequest/Chunk/Message/Reasoning
│   ├── presenter/                     # ChatThreadView, ChatMessageRowView, ChatReasoningCardView
│   └── theme/                         # ChatColorScheme, ChatTypography, ChatTheme
├── sidepanel/                         # Side panel (session + setting sub-scopes)
│   ├── domain/                        # SidePanelSessionSection (Pinned/Today/Yesterday/7Days/30Days/Older)
│   ├── application/                   # SidePanelSessionComponent, SidePanelSettingComponent
│   └── presenter/                     # SidePanelScreen, SidePanelSessionSidebarView
├── settings/                          # DEPRECATED: migrating to sidepanel/setting/
│   ├── domain/                        # ModelCatalog (curated fallback list + defaultModelId)
│   ├── application/                   # SettingsState, SettingsComponent (currently active in HomeComponent)
│   ├── presenter/                     # SettingsView, ApiKeyField/ModelPicker/ProviderPicker/ReasoningLevelSelector
│   └── theme/                         # SettingsColorScheme/Theme
├── shared/
│   ├── externals/                     # Feature-neutral adapters (Networking, Preference, Security)
│   │   ├── networking/                # ChatProvider, AuthScheme, SseLineDecoder, ChatModel, ModelCatalogStore, OpenRouterModelFetcher
│   │   ├── preference/                # ComposerReasoningLevel, ProviderPreference, ProviderPreferenceStore, DataStoreProviderPreferenceStore
│   │   └── security/                  # CredentialStore, MutableCredentialStore, EncryptedCredentialStore (AES256-GCM)
│   └── ui/                            # Shared UI primitives (button styles, badges, patterns, backgrounds)
└── ui/
    └── theme/                         # OpenZonePalette, Typography, AppTheme (System/Light/Dark), OpenZoneTheme wrapper
```

## State management rules

- The app root (`MainActivity`) creates Decompose components and passes dependencies via constructor injection.
- Each feature owns a Decompose component named `<Feature>Component` with `MutableValue<State>` as single source of truth.
- Feature views observe state via `subscribeAsState()` or `collectAsState()` and send intents to the component.
- Do not add separate ViewModels for Decompose-backed features. State belongs in `MutableValue`; mutations happen via intent methods on the component.
- Infrastructure dependencies (Room DB, DataStore, credential store) are injected as constructor parameters, never resolved statically.
- Tests should verify component state transitions for user intents (send message, toggle sidebar, change provider, etc.).
- Child components (e.g., ChatComponent inside HomeComponent) are created via factory pattern: parent component creates child and manages its lifecycle.

## File naming

One type per file; the file name matches its primary type. The suffix conveys the type's **role**, not the module — the module is already conveyed by the scope prefix.

- `…Component` — a Decompose component (`class …Component : ComponentContext`). There is normally exactly one per module. The `Component` suffix is reserved for Decompose components; do not append it to non-component files.
- `…Screen` — a top-level Compose screen (`OnboardingScreen`, `HomeScreen`). Use only for navigation destinations.
- `…View` — a Compose view (`HomeWelcomeView`, `ChatThreadView`, `SidePanelSessionSidebarView`).
- `…Entity` — a Room database entity (`ConversationEntity`, `MessageEntity`). Use in `infrastructure/persistence/`.
- `…Store` — a repository/data store interface or implementation (`ChatHistoryStore`, `OnboardingRepository`, `ModelCatalogStore`).
- Everything else — named after the value type / enum it defines (`ChatMessage`, `ComposerSpeedMode`, `OnboardingPage`).

So within a module only the single Decompose component file carries `Component`; every other file is named by its role. This is why most files have no `Component` suffix — they aren't components.

## Scope prefixes

Boundary prefixes clarify ownership and help agents find related files:

- `SidePanelSession…` — session scope (saved-conversation browsing)
- `SidePanelSetting…` — setting scope (app preferences)
- `Home…` — home feature (`HomeComposerView`, `HomeState`, `HomeWelcomeView`)
- `Chat…` — chat feature (`ChatMessage`, `ChatThreadView`, `ChatAPIClient`)
- `Onboarding…` — onboarding feature (`OnboardingPage`, `OnboardingComponent`, `OnboardingScreen`)
- Use feature prefix for feature-owned types, parent scope prefix for sub-scopes.

## Ownership rules

### Feature packages (`<feature>/`)

A feature owns one product workflow. Its package organizes files by architectural layer — domain models in `domain/`, Decompose components in `application/`, infrastructure adapters in `infrastructure/`, Compose UI in `presenter/`, theme tokens in `theme/`. Use scope-prefixed type names (e.g., `HomeComposerView`, `ChatAPIClient`, `SidePanelSessionSection`).

Feature code may depend on `shared/externals`, `shared/ui`, `ui/theme`, Kotlin coroutines, and its own feature packages. Feature code must not depend on another feature directly unless a clear integration boundary is introduced.

#### `sidepanel/`

The side panel is one feature package that hosts two sub-scopes, each scope-prefixed:

- **Session** (`SidePanelSession…`) — saved-conversation browsing, formerly "history chat". Lists and groups persisted conversations and hands off to Chat to open a thread. Consumes Chat's history persistence (`ChatHistoryStore`); does not own the live stream.
- **Setting** (`SidePanelSetting…`) — app preferences. Reads/writes through `externals` stores and theme preference. **Migration target**: `SidePanelSettingComponent` exists but settings/ package is still active in HomeComponent.

### `shared/externals/`

Externals contains feature-neutral adapters for systems outside the app:

- `networking/` — `ChatProvider`, `AuthScheme`, `SseLineDecoder`, `ChatModel`, `ModelCatalogStore`, `OpenRouterModelFetcher`.
- `preference/` — `ComposerReasoningLevel`, `ProviderPreference`, `ProviderPreferenceStore`, `DataStoreProviderPreferenceStore`.
- `security/` — `CredentialStore`, `MutableCredentialStore`, `EncryptedCredentialStore` (AES256-GCM via EncryptedSharedPreferences).

Externals must not reference feature UI or components. Chat domain types (e.g., `ChatMessage`) belong in `chat/domain/`. Feature orchestration clients (e.g., `ChatAPIClient`, `OpenRouterModelFetcher`) belong in the owning feature's package or `shared/externals/` if truly cross-cutting.

### `shared/ui/` and `ui/theme/`

Shared contains app-wide UI primitives that are safe for multiple features to reuse:

- `ui/theme/` — `OpenZonePalette` (iOS-faithful graphite), `Typography`, `AppTheme` (System/Light/Dark), `OpenZoneTheme` wrapper.
- `shared/ui/` — reusable Compose primitives, patterns, button styles, badges.

Shared code must not import or reference feature code. If a component contains feature-specific copy, state, or workflow behavior, keep it in the feature's `presenter/` package instead.

## Type visibility

All types default to `internal` unless they are:
- In `shared/externals/` or `shared/ui/` — these are cross-cutting and reusable across features
- In `ui/theme/` — app-wide theme

This keeps the API surface implicit. Public types should be documented in their context's `CONTEXT.md`.

## Dependency injection

Currently uses manual constructor injection (composition root pattern) in `MainActivity`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    val componentContext = DefaultComponentContext(lifecycle = lifecycle.referenced())
    
    // Create infrastructure
    val database = Room.databaseBuilder(application, ChatDatabase::class.java, "chat.db").build()
    val dataStore = applicationContext.dataStore
    val credentialStore = EncryptedCredentialStore(applicationContext)
    
    // Create feature components with constructor injection
    val onboardingComponent = OnboardingComponent(componentContext, DataStoreOnboardingRepository(dataStore))
    val chatComponent = ChatComponent(
        componentContext = componentContext,
        apiClient = OpenAiCompatibleStreamingClient(credentialStore),
        historyStore = RoomChatHistoryStore(database.chatHistoryDao()),
        providerId = "openrouter"
    )
    val sessionComponent = SidePanelSessionComponent(componentContext, chatComponent)
    val homeComponent = HomeComponent(componentContext, chatComponent, sessionComponent)
    
    setContent {
        OpenZoneTheme {
            if (showHome) {
                HomeScreen(homeComponent)
            } else {
                OnboardingScreen(onboardingComponent)
            }
        }
    }
}
```

This pattern is explicit and testable. Consider migrating to Hilt when:
- Feature dependencies become complex
- Multiple implementations of repositories needed
- Testing requires extensive mocking

## Future internal library path

If module boundaries need compiler enforcement, promote these packages to internal Gradle modules in this order:

1. Promote `shared/externals/` to an internal module (`:externals`).
2. Promote `shared/ui/` and `ui/theme/` to an internal module (`:shared-ui`).
3. Promote feature packages to internal modules as needed (`:feature-chat`, `:feature-home`, etc.).
4. Keep domain models in pure Kotlin modules (no Android dependencies).

Keep those modules private to this repo. Remote packages add versioning, CI, and cross-repo coordination overhead.
