# Chat Context

| | |
| --- | --- |
| **Context** | Chat feature |
| **Code** | `chat/` |
| **Map** | [CONTEXT-MAP.md](../../../CONTEXT-MAP.md) |
| **Layout rules** | [docs/architecture/modules.md](../../architecture/modules.md) |

The chat feature owns the live conversation workflow: composing a request, streaming the assistant response, rendering messages and reasoning, and surfacing stream errors.

## Language

- **Conversation** — a single chat thread (`ChatConversation`) and its ordered messages.
- **Message** — one turn (`ChatMessage`), carrying a role and content.
- **Model** — the selected AI model descriptor (`ChatModel`) used for a request.
- **Request** — the outbound `ChatRequest` sent to the provider.
- **Streaming event** — an incremental `ChatStreamingEvent` decoded from the wire.
- **Stream error** — a terminal failure surfaced as `ChatStreamError`.

## Architecture

- State lives in `ChatState`; intents are methods on `ChatComponent`.
- Streaming, persistence, and provider wiring run as effects from `ChatComponent`.
- Value types: `ChatConversation`, `ChatMessage`, `ChatMessageRole`, `ChatRequest`, `ChatStreamingEvent`, `ChatStreamError`.
- Infrastructure: `ChatAPIClient`, `OpenAiCompatibleStreamingClient`, `ChatHistoryStore`, `RoomChatHistoryStore`.
- Views: `ChatThreadView`, `ChatMessageRowView`, `ChatReasoningCardView`, `ChatErrorBannerView`.
- Persistence: `ChatDatabase` (Room), `ChatHistoryDao`, `ConversationEntity`, `MessageEntity`, `ChatMessageMapper`.

## Boundaries

- Chat domain types stay in `chat/`; do not move them to `shared/externals/`.
- `OpenAiCompatibleStreamingClient` stays here because it combines provider wire behavior with chat domain types.
- Reuse theme and UI primitives from `shared/ui` and `ui/theme`; reuse provider/credential adapters from `shared/externals/`.
- Do not depend on other feature components directly; integrate through the app shell.

## Relation to the side panel

Persisted conversations produced by this feature are listed and resumed from the side panel's session scope — see [SidePanel context](../sidepanel/SidePanel-CONTEXT.md). Chat owns the active thread; the side panel owns navigation across saved conversations.
