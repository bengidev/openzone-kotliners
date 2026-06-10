# AGENTS.md

## Agent skills

Project agents should preserve the native Android direction and keep AI provider implementation separated from UI code.

### Package structure

The project follows a feature-oriented internal-package layout mirroring iOS OpenZone:

```
io.github.bengidev.openzone/
├── MainActivity.kt
├── onboarding/          # OnboardingComponent, pages, visuals, persistence
├── home/                # HomeComponent, composer, model popup, particle orb
├── chat/                # ChatComponent, streaming, history persistence
├── sidepanel/           # SidePanel session + setting sub-scopes
├── settings/            # DEPRECATED — migrating to sidepanel/
├── shared/
│   ├── externals/       # Feature-neutral adapters (Networking, Preference, Security)
│   └── ui/              # Shared UI primitives
└── ui/theme/            # Palette, Typography, AppTheme
```

### Feature layering

Each feature package uses domain/application/infrastructure/presenter/theme sub-packages:

- **domain** — models only; no Android or Compose imports
- **application** — Decompose components and state; depends on interfaces, not concrete storage
- **infrastructure** — concrete storage implementations (Room, DataStore, EncryptedSharedPreferences)
- **presenter** — Compose UI; talks to components, not persistence
- **theme** — feature design tokens

### `shared/externals/` package

Feature-neutral adapters for external systems. Mirrors iOS `OpenZone/Externals/`:

- `networking/` — `ChatProvider`, `AuthScheme`, `SseLineDecoder`, `ChatModel`, `ModelCatalogStore`
- `security/` — `CredentialStore`, `MutableCredentialStore`, `EncryptedCredentialStore`
- `preference/` — `ComposerReasoningLevel`, `ProviderPreference`, `ProviderPreferenceStore`

Externals code MUST be feature-neutral — no imports from `chat/`, `home/`, `onboarding/`, `sidepanel/`. MUST stay free of Compose types.

### Side panel architecture

The side panel is one module (`sidepanel/`) hosting two sub-scopes:

- **Session** (`SidePanelSession*`) — saved-conversation browser, supersedes old `HomeSidebarView`
- **Setting** (`SidePanelSetting*`) — app preferences, supersedes old `SettingsComponent`

Old `settings/` and `home/presenter/HomeSidebarView.kt` are deprecated and targeted for removal once the migration is complete.

### Onboarding (internal module)

Onboarding lives under `onboarding/` as an internal feature package, not a standalone Gradle module.

Preserve the existing layering when changing onboarding. Do not reintroduce a separate `:onboarding` library module unless the team explicitly decides to extract and publish it.

### Issue tracker

GitHub Issues on `bengidev/openzone-kotliners`. See `docs/agents/issue-tracker.md`.

### Triage labels

Five canonical roles mapped 1:1 to label strings of the same name. See `docs/agents/triage-labels.md`.

### Domain docs

Multi-context: `CONTEXT-MAP.md` at the repo root points to per-context `CONTEXT.md` files in `docs/contexts/`; system-wide ADRs in `docs/adr/`; module layout rules in `docs/architecture/modules.md`. See `docs/agents/domain.md`.
