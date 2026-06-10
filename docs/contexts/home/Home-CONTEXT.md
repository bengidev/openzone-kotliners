# Home Context

| | |
| --- | --- |
| **Context** | Home feature |
| **Code** | `home/` |
| **Map** | [CONTEXT-MAP.md](../../../CONTEXT-MAP.md) |
| **Layout rules** | [docs/architecture/modules.md](../../architecture/modules.md) |

The home feature owns the main landing surface: the welcome state, the composer that starts a conversation, model selection, and the entry point into the side panel.

## Language

- **Composer** — the input surface (`HomeComposerView`) for starting a message, with speed mode, reasoning level, and context-usage indicators.
- **Speed mode** — `ComposerSpeedMode`, the latency/quality tradeoff selected for a request.
- **Reasoning level** — `ComposerReasoningLevel`, the reasoning tier surfaced in the composer.
- **Context usage** — `ComposerContextUsage`, the live token/context budget indicator.
- **Welcome** — the empty/first-load state (`HomeWelcomeView`) shown before a conversation begins.

## Architecture

- State lives in `HomeState`; intents are methods on `HomeComponent`.
- Value types: `ComposerSpeedMode`, `ComposerReasoningLevel`, `ComposerContextUsage`.
- Views: `HomeView` (via `HomeScreen`), `HomeWelcomeView`, `HomeComposerView`, `ComposerModelPopup`, `HomeParticleOrbView`.

## Boundaries

- Home owns the landing and composer workflow only. Live streaming belongs to [Chat](../chat/Chat-CONTEXT.md).
- Reuse theme and UI primitives from `shared/ui` and `ui/theme`; reuse provider/credential adapters from `shared/externals/`.
- Do not depend on other feature components directly; integrate through the app shell.

## Relation to the side panel

Saved-conversation browsing has moved out of Home into the side panel module. Home composes `SidePanelSessionComponent` as a child and renders `SidePanelSessionSidebarView` scoped to the panel's state. Settings will migrate to `SidePanelSettingComponent`.

Home no longer owns that state. It only:
- forwards the toggle/settings-button intents into the panel,
- handles the panel's delegate outputs — opening the chosen conversation in Chat,
- syncs the active-conversation id into the session scope so the sidebar highlights the open thread.

The session list and its grouping live under [SidePanel context](../sidepanel/SidePanel-CONTEXT.md).
