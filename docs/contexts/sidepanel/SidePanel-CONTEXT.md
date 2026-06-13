# Side Panel Context

| | |
| --- | --- |
| **Context** | Side panel feature |
| **Code** | `sidepanel/` |
| **Map** | [CONTEXT-MAP.md](../../../CONTEXT-MAP.md) |
| **Layout rules** | [docs/architecture/modules.md](../../architecture/modules.md) |

The side panel is the single navigation surface that slides in alongside the main content. It is one module that hosts two sub-scopes: **session** (saved-conversation browsing, formerly "history chat") and **setting** (app preferences).

```text
sidepanel/
├── SidePanelScreen.kt                  # Overlay container
├── domain/
│   └── SidePanelSessionSection.kt      # Recency/pin grouping (Pinned/Today/Yesterday/7Days/30Days/Older)
├── application/
│   ├── SidePanelSessionComponent.kt    # Session scope (list, filter, active id)
│   └── SidePanelSettingComponent.kt    # Setting scope (migration target; settings/ still active)
└── presenter/
    └── SidePanelSessionSidebarView.kt  # Rendered session list
```

## Sub-scopes

- **Session** — list, group, open, pin, and manage saved conversations. Replaces the old "history chat" surface. See [SidePanelSession context](./SidePanelSession-CONTEXT.md).
- **Setting** — app-wide preferences (theme, provider, credentials entry points). See [SidePanelSetting context](./SidePanelSetting-CONTEXT.md).

## Language

- **Side panel** — the slide-in container that presents session and setting scopes.
- **Session scope** — the saved-conversation browser inside the side panel (`SidePanelSession*`).
- **Setting scope** — the preferences surface inside the side panel (`SidePanelSetting*`).
- **Session section** — `SidePanelSessionSection`, a recency- or pin-based group of sessions.

## Architecture

- `SidePanelSessionComponent` owns the session scope: conversation list, search query, sidebar visibility, and the active-conversation id.
- `SidePanelSettingComponent` is the migration target for the legacy `settings/` package. The old `SettingsComponent` + `SettingsView` remain active in `home/`.
- The session scope emits callbacks (`onOpenConversation`, `onRenameConversation`, `onDeleteConversation`); the parent (Home) handles them.
- The panel does not reach into the live chat component directly.

## Naming convention

All symbols and files in this module carry the `SidePanel` scope prefix, and the two sub-scopes extend it:

- `SidePanelSession…` for the session scope — e.g., `SidePanelSessionComponent`, `SidePanelSessionSidebarView`, `SidePanelSessionSection`.
- `SidePanelSetting…` for the setting scope — e.g., `SidePanelSettingComponent`.

## Boundaries

- The side panel owns navigation across saved conversations and app settings; it does not own the live chat stream (Chat) or the landing composer (Home).
- Reuse theme and UI primitives from `shared/ui` and `ui/theme`; reuse provider/credential/preference adapters from `shared/externals/`.
- Do not depend on other feature components directly; integrate through the app shell.
