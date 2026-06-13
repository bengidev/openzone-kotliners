# Chat Context

| | |
|---|---|
| **Context** | Chat feature - conversation and streaming |
| **Code** | `chat/` |
| **Parent** | Home |
| **Children** | None |

The Chat feature manages conversation threads, message streaming via SSE, and message persistence.

## Language

- **ChatThreadView**: Root thread composable rendered inside Home
- **ChatComponent**: State holder managing chat state, streaming, and turn-boundary persistence
- **ChatState**: StateFlow-backed state for active conversation, messages, draft, and streaming status
- **Streaming**: `Flow<ChatStreamingEvent>` from the OpenAI-compatible SSE client
- **Persistence**: `ChatHistoryStore` boundary backed by Room
- **Wire models**: Data classes matching OpenAI-compatible API schema

## Architecture

### State Management

`ChatComponent` owns `MutableStateFlow<ChatState>`:

```kotlin
data class ChatState(
    val conversation: ChatConversation = ChatState.defaultConversation(),
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val status: ChatStreamingStatus = ChatStreamingStatus.IDLE,
    val isReasoningExpanded: Boolean = false,
    val canSend: Boolean = false
)

enum class ChatStreamingStatus {
    IDLE, RUNNING, DONE, FAILED
}
```

### Streaming Flow

The Chat feature uses `ChatAPIClient.stream(request): Flow<ChatStreamingEvent>`:

```kotlin
apiClient.stream(request).collect { event ->
    when (event) {
        is ChatStreamingEvent.TextDelta -> appendTextDelta(event.delta)
        is ChatStreamingEvent.ThinkingDelta -> appendThinkingDelta(event.delta)
        ChatStreamingEvent.Done -> {
            markAssistantTurnsComplete()
            persistCompletedAssistant()
        }
        is ChatStreamingEvent.Error -> {
            markAssistantTurnsComplete()
            appendSystemError(event.error.message)
        }
    }
}
```

### Persistence Strategy

- **Messages**: Persisted via `ChatHistoryStore` and Room `messages` rows
- **Conversations**: Upserted at turn boundaries before persisting messages
- **Turn-boundary writes**: User message persists on send; assistant message persists once on stream completion
- **Migrations**: `MIGRATION_1_2` adds `isPinned` to `conversations`

## Dependencies

- **Upstream**: `shared.externals` credential/provider/model preference seams
- **Downstream**: None (leaf feature)
- **Domain**: `ChatMessage`, `ChatConversation`, `ChatRequest`, `ChatStreamingEvent`
- **Infrastructure**: `ChatAPIClient`, `ChatHistoryStore`, `RoomChatHistoryStore`, `ChatDatabase`

## Constraints

- Streaming deltas must not be persisted per chunk.
- User turns persist immediately so aborted streams do not lose prompts.
- Completed assistant text persists once after `ChatStreamingEvent.Done`.
- Wire models stay separate from domain models for clean persistence and API isolation.

## Key Decisions

- **SSE over WebSocket**: Simpler protocol, matches OpenAI-compatible APIs.
- **Room over SQLite direct**: Type-safe queries, migration support.
- **Store boundary**: `ChatComponent` depends on `ChatHistoryStore`, not Room entities.
