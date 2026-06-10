# Shared App Primitives Context

| | |
| --- | --- |
| **Context** | Shared app primitives |
| **Code** | `shared/` (excluding `shared/externals/`) |
| **Map** | [CONTEXT-MAP.md](../../../CONTEXT-MAP.md) |
| **Layout rules** | [docs/architecture/modules.md](../../architecture/modules.md) |

`shared/` contains app-wide theme and UI primitives that are safe for multiple features to reuse.

```text
shared/
├── ui/         # button styles, badges, patterns, backgrounds
└── ui/theme/   # palette, typography, color helpers (note: also at top-level ui/theme/)
```

External integrations live in `shared/externals/` — see [Externals context](../externals/Externals-CONTEXT.md).

## Language

- **Shared primitive** — reusable, feature-neutral UI or theme code.
- **Theme** — app-wide color scheme preference, palette, typography.
- **UI primitive** — reusable visual building block such as a button style, badge, card chrome, or background pattern.

## Architecture

- Shared code must not import or reference feature code.
- Shared code must not contain feature-specific copy, workflow state, or components.
- Feature-specific UI remains inside each feature's `presenter/` package.
- Shared UI can be used by feature views, but Shared should remain state-management agnostic unless a reusable component explicitly requires a binding or action callback.
