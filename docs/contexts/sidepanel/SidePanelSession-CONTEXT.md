# Side Panel — Session Scope

| | |
| --- | --- |
| **Context** | Side panel → session scope |
| **Code** | `sidepanel/` (`SidePanelSession…` symbols) |
| **Parent** | [SidePanel context](./SidePanel-CONTEXT.md) |
| **Map** | [CONTEXT-MAP.md](../../../CONTEXT-MAP.md) |
| **Layout rules** | [docs/architecture/modules.md](../../architecture/modules.md) |

The session scope is the saved-conversation browser inside the side panel. It replaces the former "history chat" surface. It lists persisted conversations, groups them by recency, and lets the user open, pin, and manage them.

## Language

- **Session** — a saved conversation as presented for browsing/resuming (was "history chat" entry). The underlying persisted thread is the Chat `ChatConversation`.
- **Session section** — a recency- or pin-based group of sessions (`SidePanelSessionSection`): Pinned, Today, Yesterday, Previous 7 Days, Previous 30 Days, Older.
- **Session list** — the rendered, grouped list of sessions in the side panel (`SidePanelSessionSidebarView`).

## Architecture

- The session scope has its own component, `SidePanelSessionComponent`. Its state owns the loaded conversation list, the search query (with `filteredConversations`), sidebar visibility, and the active-conversation id used to highlight the open thread.
- It reads/writes persisted conversations through `ChatHistoryStore`: loading on open, and persisting pin/rename/delete before reloading the authoritative order.
- Grouping/relative-time labeling logic lives with the session scope (`SidePanelSessionSection`).
- It never touches the live chat component directly. Instead it emits callbacks the parent acts on: `onOpenConversation` (resume in chat), `onRenameConversation`, and `onDeleteConversation`.
- Naming follows the [file-naming rules](../../architecture/modules.md): `SidePanelSessionSidebarView` (view), `SidePanelSessionSection` (value type), `SidePanelSessionComponent` (component).

## Migration note

This scope supersedes the old "history chat" naming. Former Home-scoped browsing (`HomeSidebarView`) is replaced by `SidePanelSessionSidebarView` and moved into `sidepanel/presenter/`. Persistence types owned by Chat (`ChatHistoryStore`, `ConversationEntity`) remain in `chat/infrastructure/`; this scope consumes them.

## Boundaries

- Owns browsing/navigation across saved sessions only — not the live stream (Chat) or the composer (Home).
- Reuse theme and UI primitives from `shared/ui` and `ui/theme`.
