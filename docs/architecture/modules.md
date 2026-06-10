# Module Layout

Part of this repo's multi-context documentation. See [CONTEXT-MAP.md](../../CONTEXT-MAP.md) for per-feature glossaries and [docs/agents/domain.md](../agents/domain.md) for how agents consume domain docs.

OpenZone uses feature-oriented packages inside the app target. Each feature package hosts its own domain / application / infrastructure / presenter layering, plus an optional theme sub-package.

State management uses Decompose components with `MutableValue` state and intent methods, mirroring iOS TCA reducers.

## Module map

```text
App
├── Shared                 # UI/Theme + Externals (cross-cutting)
├── Onboarding
├── Home
├── Chat
└── SidePanel
    ├── Session             # SidePanelSession… (saved-conversation browsing)
    └── Setting             # SidePanelSetting… (app preferences)
```

## Current layout

```text
io.github.bengidev.openzone/
├── MainActivity.kt
├── onboarding/             # OnboardingComponent, pages, visuals, persistence
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   ├── presenter/
│   └── theme/
├── home/                   # HomeComponent, composer, model catalog, particle orb
│   ├── domain/
│   ├── application/
│   ├── presenter/
│   └── theme/
├── chat/                   # ChatComponent, streaming, history persistence
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   │   └── persistence/
│   ├── presenter/
│   └── theme/
├── sidepanel/              # SidePanel session + setting sub-scopes
│   ├── domain/
│   ├── application/
│   └── presenter/
├── settings/               # DEPRECATED — migrating to sidepanel/setting scope
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   ├── presenter/
│   └── theme/
├── shared/
│   ├── externals/          # Feature-neutral adapters (external integrations)
│   │   ├── networking/     # ChatProvider, AuthScheme, SseLineDecoder, ChatModel, ModelCatalogStore
│   │   ├── security/       # CredentialStore, MutableCredentialStore, EncryptedCredentialStore
│   │   └── preference/     # ComposerReasoningLevel, ProviderPreference, ProviderPreferenceStore
│   └── ui/                 # Shared UI primitives (button styles, badges, patterns)
└── ui/
    └── theme/              # Palette, Typography, AppTheme
```

## File naming

One type per file; the file name matches its primary type. Scope prefixes convey ownership:

- `SidePanelSession…` — session scope (saved-conversation browsing)
- `SidePanelSetting…` — setting scope (app preferences)
- `Home…` — home feature
- `Chat…` — chat feature
- `Onboarding…` — onboarding feature

## Ownership rules

### Feature packages

A feature owns one product workflow. Its package holds domain models, application components, infrastructure adapters, and Compose presenters.

Feature code may depend on `shared/externals`, `shared/ui`, `ui/theme`, standard libraries, and its own feature packages. Feature code must not depend on another feature directly unless a clear integration boundary is introduced.

### `shared/externals/`

Externals contains feature-neutral adapters for systems outside the app: networking primitives, persisted preferences, and secure credential storage. Mirrors iOS `OpenZone/Externals/`.

- Externals code must not import or reference feature UI or component code.
- Feature domain types belong in feature packages.
- Feature orchestration clients belong in the owning feature's package.

### `shared/ui/`

Shared UI contains app-wide Compose primitives that are safe for multiple features to reuse: button styles, badges, card chrome, patterns, backgrounds.

- Shared UI must not import or reference feature code.
- Feature-specific UI remains inside each feature's `presenter/` package.

## State management rules

- The app root (`MainActivity`) creates Decompose components and passes dependencies via constructor injection.
- Each feature component owns its state in a `MutableValue<State>` with intent methods.
- Views observe state via `subscribeAsState()` or `collectAsState()` and send intents to the component.
- Infrastructure dependencies are injected as constructor parameters, never resolved statically.

## Future internal-library path

If module boundaries need compiler enforcement, feature packages can be promoted to internal Gradle modules in this order:

1. Promote `shared/externals/` to an internal Gradle module.
2. Promote `shared/ui/` and `ui/theme/` to an internal Gradle module.
3. Promote feature packages to internal modules as needed.

Keep those modules private to this repo. Remote packages add versioning and CI overhead.
