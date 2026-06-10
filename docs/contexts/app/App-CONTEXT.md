# App Shell Context

| | |
| --- | --- |
| **Context** | OpenZone app shell |
| **Code** | `io.github.bengidev.openzone` (root app package sources outside feature packages) |
| **Map** | [CONTEXT-MAP.md](../../../CONTEXT-MAP.md) |
| **Layout rules** | [docs/architecture/modules.md](../../architecture/modules.md) |

The app shell owns entry-point wiring, global routing, and coordination between features.

## Language

- **App shell** — `MainActivity`, root Decompose `ComponentContext`, and routing that decides which top-level screen is shown.
- **Root component** — root `HomeComponent` that composes child feature components and handles cross-feature routing.

The shell does not have a dedicated component; routing decisions live in `MainActivity`.

## Architecture

- The shell creates dependencies (Room database, DataStore, EncryptedSharedPreferences) and passes them into feature components via constructor injection.
- Onboarding completion is observed by the shell to switch from onboarding to main content.
- Feature-specific logic stays in feature packages; only cross-cutting routing belongs here.

## Boundaries

- Do not put feature-specific domain language or components in the shell.
- Shared theme and UI come from `shared/ui` and `ui/theme`; external adapters from `shared/externals/`; feature UI from each feature's package.
