package io.github.bengidev.openzone.chat.presenter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bengidev.openzone.chat.application.ChatState
import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatMessageRole

/**
 * Scrollable list of chat messages with auto-scroll to bottom on
 * content / streaming changes. Mirrors iOS `ChatThreadView` (which
 * uses SwiftUI's `List` with a `ScrollViewReader`).
 */
@Composable
fun ChatThreadView(
    state: ChatState,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val messages = state.messages
    val lastMessageId = messages.lastOrNull()?.id
    val lastAssistantText = remember(messages) {
        messages.lastOrNull { it is ChatMessage.Text && it.message.role == ChatMessageRole.ASSISTANT }
    }
    val assistantContentLength = (lastAssistantText as? ChatMessage.Text)?.message?.content?.length ?: 0
    val thinkingContentLength = (messages.lastOrNull { it is ChatMessage.Thinking } as? ChatMessage.Thinking)
        ?.message?.content?.length ?: 0
    val status = state.status

    // Auto-scroll on new message id (animated).
    LaunchedEffect(lastMessageId) {
        if (lastMessageId != null) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    // Auto-scroll on streaming text growth (no animation, every chunk).
    LaunchedEffect(assistantContentLength, thinkingContentLength) {
        if (lastMessageId != null && (assistantContentLength > 0 || thinkingContentLength > 0)) {
            // jumpTo avoids jittering while typing; animateScroll only for discrete changes.
            listState.scrollToItem(messages.lastIndex)
        }
    }
    // Snap on status transitions.
    LaunchedEffect(status) {
        if (lastMessageId != null) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.Start
        ) {
            items(
                items = messages,
                key = { it.id }
            ) { message ->
                ChatMessageRowView(
                    message = message,
                    isLastAssistantMessage = isLastAssistantMessage(message, messages),
                    streamingStatus = state.status
                )
            }
        }
    }
}

private fun isLastAssistantMessage(message: ChatMessage, messages: List<ChatMessage>): Boolean {
    if (message !is ChatMessage.Text) return false
    if (message.message.role != ChatMessageRole.ASSISTANT) return false
    val lastAssistant = messages.lastOrNull {
        it is ChatMessage.Text && it.message.role == ChatMessageRole.ASSISTANT
    } ?: return false
    return (lastAssistant as ChatMessage.Text).id == message.id
}
