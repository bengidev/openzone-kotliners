# AGENTS.md

Conventions for AI-assisted development on this project.

## Agent skill usage

- **/tdd**: run unit tests via `./gradlew :app:testDebugUnitTest`; instrumented via `./gradlew connectedDebugAndroidTest`
- **/grill-me**: use with domain vocabulary from CONTEXT files; verify against [docs/architecture/modules.md](docs/architecture/modules.md)
- **/diagnose**: reproduce via Compose previews, emulator logs, Room Inspector; prefer minimal repro
- **/improve-codebase-architecture**: preserve feature-neutral externals (see ADR-0001). Do not merge feature packages without explicit plan

## Architecture rules

Follow the layered package structure defined in [docs/architecture/modules.md](docs/architecture/modules.md):

1. **Feature packages** use `domain/`, `application/`, `infrastructure/`, `presenter/`, `theme/` layering
2. **`domain/` must remain pure Kotlin** — no Android SDK, no Compose, no Decompose imports
3. **`application/` uses Decompose + MutableValue** for state management, not ViewModels
4. **`shared/externals/` is feature-neutral** — never import feature packages
5. **Dependency direction**: features → shared, never the reverse

## State management

- Use Decompose `MutableValue<State>` in components, mirrored after iOS TCA reducer pattern
- Intent methods are the only way to mutate state via `state.value = state.value.copy(...)`
- Child components are created by parent via factory pattern
- Compose views observe state with `subscribeAsState()` or `collectAsState()`

## Coroutines

See [docs/architecture/coroutine-concurrency.md](docs/architecture/coroutine-concurrency.md) for dispatcher choice, flow rules, SSE streaming, and persistence rules.

## iOS parity goals

- Preserve iOS-faithful palette (`OpenZonePalette` — graphite monochrome, no blue accents)
- Do not introduce HStack/VStack-style Compose idioms that diverge from the iOS layout language
- Port visual treatments 1:1 where possible; document divergences in the relevant context file

## Issue tracker

GitHub Issues on `bengidev/openzone-kotliners`. See [docs/agents/issue-tracker.md](docs/agents/issue-tracker.md) for `gh` CLI conventions.

## Triage labels

Five canonical roles mapped 1:1 to label strings of the same name. See [docs/agents/triage-labels.md](docs/agents/triage-labels.md).

## Domain docs

Multi-context: [CONTEXT-MAP.md](CONTEXT-MAP.md) points to per-context `CONTEXT.md` files in `docs/contexts/`; system-wide ADRs in [docs/adr/](docs/adr/); module layout rules in [docs/architecture/modules.md](docs/architecture/modules.md). See [docs/agents/domain.md](docs/agents/domain.md).

## Deprecated packages

The `settings/` package is **deprecated** — superseded by `sidepanel/` (`SidePanelComponent` hosts session + setting scopes, mirroring iOS `SidePanelFeature`). Legacy `SettingsComponent` remains for tests only.
