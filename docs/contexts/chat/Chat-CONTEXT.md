# Chat Context

| | |
|---|---|
| **Context** | Chat feature - conversation and streaming |
| **Code** | `chat/` |
| **Parent** | Home |
| **Children** | None |

The Chat feature manages conversation threads, message streaming via SSE, and message persistence.

## Language

- **ChatScreen**: Root composable for the chat feature
- **ChatComponent**: Decompose component managing chat state and streaming
- **ChatState**: State container for conversation and streaming status
- **Streaming**: Server-sent events (SSE) for real-time message chunks
- **Persistence**: Room database for message and conversation storage
- **Wire models**: Data classes matching OpenAI API schema

## Architecture

### State Management

`ChatComponent` owns `MutableValue<ChatState>`:

```kotlin
data class ChatState(
    val conversationId: String,
    val messages: List<ChatMessage>,
    val streamingStatus: StreamingStatus,
    val currentStream: String = "",
    val isScrollToBottomEnabled: Boolean = true
)

enum class StreamingStatus {
    Idle, Streaming, Complete, Error
}
```

### Streaming Flow

The Chat feature uses `OpenAiCompatibleStreamingClient` from `shared/externals/`:

```kotlin
fun streamMessage(request: ChatRequest) {
    componentScope.launch {
        streamingClient
            .streamMessages(request)
            .collect { chunk ->
                when (chunk) {
                    is Chunk.Delta -> state.value = state.value.copy(
                        currentStream = state.value.currentStream + chunk.content
                    )
                    is Chunk.Done -> {
                        // Persist complete message
                        chatRepository.saveMessage(...)
                        state.value = state.value.copy(
                            messages = state.value.messages + completeMessage,
                            streamingStatus = StreamingStatus.Complete
                        )
                    }
                    is Chunk.Error -> {
                        state.value = state.value.copy(
                            streamingStatus = StreamingStatus.Error
                        )
                    }
                }
            }
    }
}
```

### Persistence Strategy

- **Messages**: Persisted via `ChatRepository` (Room)
- **Conversations**: Auto-created when first message sent
- **Migrations**: `MIGRATION_1_2` adds `isPinned` and `lastUpdated` columns

## Dependencies

- **Upstream**: `shared.externals` (streaming client, credential store)
- **Downstream**: None (leaf feature)
- **Domain**: `ChatMessage`, `ChatRequest`, `Chunk` (pure Kotlin)
- **Infrastructure**: `ChatRepository`, `ChatDatabase` (Room)

## Constraints

- Streaming must not persist incomplete messages to avoid data corruption
- `componentScope` tied to component lifecycle for automatic cancellation
- Wire models separate from domain models for clean persistence

## Key Decisions

- **SSE over WebSocket**: Simpler protocol, matches OpenAI API
- **Room over SQLite direct**: Type-safe queries, migration support
- **Factory pattern**: `ChatComponent.Factory` for dependency injection and testing
