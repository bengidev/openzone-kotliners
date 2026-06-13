# Shared Context

| | |
| --- | --- |
| **Context** | App-wide theme |
| **Code** | `ui/theme/` |
| **Map** | [CONTEXT-MAP.md](../../../CONTEXT-MAP.md) |
| **Layout rules** | [docs/architecture/modules.md](../../architecture/modules.md) |

`ui/theme/` contains the app-wide theme primitives used by feature themes. There is no `shared/ui/` package yet; `shared/` currently hosts `shared/externals/` only.

```text
ui/
└── theme/      # OpenZonePalette, Typography, AppTheme, Theme.kt (OpenZoneTheme wrapper)
```

External integrations live in `shared/externals/` — see [Externals context](../externals/Externals-CONTEXT.md).

## Dependencies

None. Shared is a leaf dependency — it must not import any feature code.

## Language

- **Theme** — app-wide color scheme preference, palette, typography.
- **Feature theme** — feature-local aliases/wrappers that reuse app-wide palette and typography.
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

No `shared/ui/` primitives exist in the current codebase. Feature-specific UI remains inside each feature's `presenter/` package until a reusable primitive is extracted.

## Architecture

- Theme code must not import or reference feature code.
- Feature-specific UI remains inside each feature's `presenter/` package.
- Reusable UI primitives should be introduced under `shared/ui/` only when at least two features consume them.
- Palette and typography are defined once in `ui/theme/Palette.kt`. Feature themes re-export or extend from it via `typealias` — never duplicate the palette.

## Constraints

- **No feature imports** — Shared theme code must never import `home/`, `chat/`, `onboarding/`, `sidepanel/`, or legacy `settings/`.
- **No business logic** — only theme definitions until `shared/ui/` exists.
- **No state management** — no Room, no Decompose components, no persistence code.

## Usage pattern

```kotlin
@Composable
fun HomeComposerView(component: HomeComponent) {
    val palette = HomeTheme.palette
    val appTheme = LocalAppTheme.current

    Column(
        modifier = Modifier
            .background(palette.surface)
            .padding(16.dp)
    ) {
        // Feature presenter content stays in the feature package.
    }
}
```
