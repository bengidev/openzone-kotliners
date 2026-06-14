package io.github.bengidev.openzone.chat.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatMessageRole
import io.github.bengidev.openzone.chat.domain.ChatStreamingStatus
import io.github.bengidev.openzone.chat.domain.ChatTextMessage
import io.github.bengidev.openzone.chat.theme.ChatTheme
import io.github.bengidev.openzone.chat.theme.LocalChatTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val OPPOSITE_SIDE_MIN_WIDTH_DP = 60
private val UserBubbleCorner = RoundedCornerShape(20.dp)
private val AssistantBubbleCorner = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)

/**
 * One row in the chat thread. Renders user bubbles right-aligned, assistant text left-aligned,
 * system messages centered, and reasoning cards in a collapsible container. Mirrors iOS
 * `ChatMessageRowView`.
 */
@Composable
fun ChatMessageRowView(
        message: ChatMessage,
        isLastAssistantMessage: Boolean,
        streamingStatus: ChatStreamingStatus,
        modifier: Modifier = Modifier
) {
 when (message) {
  is ChatMessage.Text ->
          TextRow(
                  message = message.message,
                  isLastAssistantMessage = isLastAssistantMessage,
                  streamingStatus = streamingStatus,
                  modifier = modifier
          )
  // Hide empty completed thinking cards — non-reasoning models never
  // emit content, so the card would show just "Thinking" with nothing
  // inside. Mirrors iOS: the row is never created in that case.
  is ChatMessage.Thinking -> {
   val thinking = message.message
   if (thinking.content.isBlank() && thinking.isComplete) return@ChatMessageRowView
   AssistantSurround(modifier) {
    ChatReasoningCardView(message = thinking, isStreaming = !thinking.isComplete)
   }
  }
  is ChatMessage.System -> SystemRow(message.message.content, modifier)
 }
}

@Composable
private fun TextRow(
        message: ChatTextMessage,
        isLastAssistantMessage: Boolean,
        streamingStatus: ChatStreamingStatus,
        modifier: Modifier = Modifier
) {
 if (message.role == ChatMessageRole.USER) {
  UserRow(message, modifier)
 } else {
  AssistantRow(
          message = message,
          isLastAssistantMessage = isLastAssistantMessage,
          streamingStatus = streamingStatus,
          modifier = modifier
  )
 }
}

@Composable
private fun UserRow(message: ChatTextMessage, modifier: Modifier = Modifier) {
 val palette = ChatTheme.palette
 val typography = LocalChatTypography.current

 Row(
         modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
         horizontalArrangement = Arrangement.End
 ) {
  Spacer(modifier = Modifier.widthIn(min = OPPOSITE_SIDE_MIN_WIDTH_DP.dp))
  Column(horizontalAlignment = Alignment.End) {
   Text(
           text = message.content,
           style = typography.messageBody,
           color = palette.userBubbleText,
           modifier =
                   Modifier.clip(UserBubbleCorner)
                           .background(palette.userBubble)
                           .padding(horizontal = 16.dp, vertical = 12.dp)
                           .widthIn(max = 320.dp)
   )
   Text(
           text = formatTime(message.timestamp),
           style = typography.messageMeta,
           color = palette.messageMetaText,
           modifier = Modifier.padding(top = 4.dp)
   )
  }
 }
}

@Composable
private fun AssistantRow(
        message: ChatTextMessage,
        isLastAssistantMessage: Boolean,
        streamingStatus: ChatStreamingStatus,
        modifier: Modifier = Modifier
) {
 val palette = ChatTheme.palette
 val typography = LocalChatTypography.current

 // While the model is mid-stream with no tokens yet, render nothing — the
 // reasoning card (or whatever is just above) is already signalling load.
 // A "Streaming…" caption under an empty bubble stacked under the thinking
 // card and made the screen look piled up. Errors and finished states still
 // render normally.
 val isEmptyMidStream =
         message.content.isEmpty() &&
                 streamingStatus == ChatStreamingStatus.RUNNING &&
                 !message.isComplete
 if (isEmptyMidStream) return

 Column(
         modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
         verticalArrangement = Arrangement.spacedBy(6.dp)
 ) {
  if (message.content.isNotEmpty()) {
   Text(
           text = message.content,
           style = typography.messageBody,
           color = palette.assistantBubbleText,
           modifier = Modifier.widthIn(max = 540.dp).align(Alignment.Start)
   )
  }
  if (isLastAssistantMessage) {
   AssistantMetaRow(
           streamingStatus = streamingStatus,
           isComplete = message.isComplete,
           timestamp = message.timestamp
   )
  }
 }
}

@Composable
private fun AssistantMetaRow(
        streamingStatus: ChatStreamingStatus,
        isComplete: Boolean,
        timestamp: Long
) {
 val palette = ChatTheme.palette
 val typography = LocalChatTypography.current

 when (streamingStatus) {
  ChatStreamingStatus.FAILED -> {
   Text(
           text = "Streaming failed — try again.",
           style = typography.messageMeta,
           color = palette.systemMessageText
   )
  }
  ChatStreamingStatus.RUNNING ->
          if (!isComplete) {
           // Subtle subtitle while streaming — mirrors iOS `Text("Streaming…")`.
           // The earlier three-dot pulse stacked visually with the reasoning
           // card and the empty assistant bubble; a single muted caption reads
           // as one continuous loading state instead.
           Text(
                   text = "Streaming…",
                   style = typography.messageMeta,
                   color = palette.messageMetaText
           )
          } else {
           Text(
                   text = formatTime(timestamp),
                   style = typography.messageMeta,
                   color = palette.messageMetaText
           )
          }
  ChatStreamingStatus.DONE, ChatStreamingStatus.IDLE -> {
   Text(
           text = formatTime(timestamp),
           style = typography.messageMeta,
           color = palette.messageMetaText
   )
  }
 }
}

@Composable
private fun AssistantSurround(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
 Row(
         modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
         horizontalArrangement = Arrangement.Start
 ) {
  Box(modifier = Modifier.widthIn(max = 540.dp)) { content() }
  Spacer(modifier = Modifier.widthIn(min = OPPOSITE_SIDE_MIN_WIDTH_DP.dp))
 }
}

@Composable
private fun SystemRow(content: String, modifier: Modifier = Modifier) {
 val palette = ChatTheme.palette
 val typography = LocalChatTypography.current
 Text(
         text = content,
         style = typography.systemMessage,
         color = palette.systemMessageText,
         textAlign = TextAlign.Center,
         modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
 )
}

private val TimeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

private fun formatTime(timestamp: Long): String = TimeFormatter.format(Date(timestamp))
