package io.github.bengidev.openzone.chat.domain

/**
 * Sealed message union — every row in the chat thread is one of these variants.
 * Mirrors iOS `ChatMessage` enum.
 */
sealed interface ChatMessage {
    val id: String
    val role: ChatMessageRole
    val timestamp: Long

    data class Text(val message: ChatTextMessage) : ChatMessage {
        override val id: String get() = message.id
        override val role: ChatMessageRole get() = message.role
        override val timestamp: Long get() = message.timestamp
    }

    data class Thinking(val message: ChatThinkingMessage) : ChatMessage {
        override val id: String get() = message.id
        override val role: ChatMessageRole get() = message.role
        override val timestamp: Long get() = message.timestamp
    }

    data class System(val message: ChatSystemMessage) : ChatMessage {
        override val id: String get() = message.id
        override val role: ChatMessageRole get() = message.role
        override val timestamp: Long get() = message.timestamp
    }
}

/** Convenience factories matching the iOS API. */
object ChatMessages {
    fun text(
        id: String,
        role: ChatMessageRole,
        content: String,
        isComplete: Boolean = true,
        timestamp: Long = System.currentTimeMillis()
    ): ChatMessage = ChatMessage.Text(
        ChatTextMessage(id = id, role = role, content = content, isComplete = isComplete, timestamp = timestamp)
    )

    fun thinking(
        id: String,
        content: String,
        isComplete: Boolean = true,
        timestamp: Long = System.currentTimeMillis()
    ): ChatMessage = ChatMessage.Thinking(
        ChatThinkingMessage(id = id, content = content, isComplete = isComplete, timestamp = timestamp)
    )

    fun system(
        id: String,
        content: String,
        timestamp: Long = System.currentTimeMillis()
    ): ChatMessage = ChatMessage.System(
        ChatSystemMessage(id = id, content = content, timestamp = timestamp)
    )
}
