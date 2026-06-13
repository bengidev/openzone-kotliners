# Shared Context

| | |
| --- | --- |
| **Context** | Cross-feature reusable UI and theme |
| **Code** | `shared/` (excluding `shared/externals/`), `ui/theme/` |
| **Map** | [CONTEXT-MAP.md](../../../CONTEXT-MAP.md) |
| **Layout rules** | [docs/architecture/modules.md](../../architecture/modules.md) |

`shared/` and `ui/theme/` contain app-wide theme and UI primitives that are safe for multiple features to reuse.

```text
shared/
├── ui/         # Shared UI primitives (button styles, badges, patterns, backgrounds)
└── [externals/]  # See Externals context

ui/
└── theme/      # OpenZonePalette, Typography, AppTheme, Theme.kt (OpenZoneTheme wrapper)
```

External integrations live in `shared/externals/` — see [Externals context](../externals/Externals-CONTEXT.md).

## Dependencies

None. Shared is a leaf dependency — it must not import any feature code.

## Language

- **Shared primitive** — reusable, feature-neutral UI or theme code.
- **Theme** — app-wide color scheme preference, palette, typography.
- **UI primitive** — reusable visual building block such as a button style, badge, card chrome, or background pattern.
- **OpenZonePalette** — the iOS-faithful graphite monochrome palette. Authoritative definition in `ui/theme/Palette.kt`.
- **AppTheme** — light/dark/system theme preference (enum with `CompositionLocal`).
- **OpenZoneTheme** — the Compose wrapper that applies theme colors, typography, and shapes over `MaterialTheme`.

## Type inventory

### `ui/theme/`

- `OpenZonePalette` — iOS-faithful graphite color palette (no blue accents)
- `Typography` — default text styles
- `AppTheme` — `System` / `Light` / `Dark` enum + `CompositionLocal`
- `Theme.kt` — `OpenZoneTheme` composable wrapper over `MaterialTheme`
- `Color.kt` — legacy color constants (deprecated: use `OpenZonePalette`)
- `Type.kt` — default typography definitions

### `shared/ui/`

Feature-neutral Compose primitives consumed by any feature presenter.

## Architecture

- Shared code must not import or reference feature code.
- Shared code must not contain feature-specific copy, workflow state, or components.
- Feature-specific UI remains inside each feature's `presenter/` package.
- Shared UI can be used by feature views, but Shared should remain state-management agnostic unless a reusable component explicitly requires a binding or action callback.
- Palette and typography are defined once in `ui/theme/Palette.kt`. Feature themes re-export or extend from it via `typealias` — never duplicate the palette.

## Constraints

- **No feature imports** — Shared must never import `home/`, `chat/`, `onboarding/`, `sidepanel/`, or legacy `settings/`.
- **No business logic** — only UI primitives and theme definitions.
- **No state management** — no Room, no Decompose components, no persistence code.

## Usage pattern

```kotlin
@Composable
fun HomeComposerView(component: HomeComponent) {
    val palette = LocalOpenZonePalette.current
    val theme = LocalAppTheme.current
    
    Column(
        modifier = Modifier
            .background(palette.surface)
            .padding(16.dp)
    ) {
        SharedBadge("Status", palette.accent)
        SharedCardChrome { /* content */ }
    }
}
```
