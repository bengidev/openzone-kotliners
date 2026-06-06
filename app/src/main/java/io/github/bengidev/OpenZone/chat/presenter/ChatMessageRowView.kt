package io.github.bengidev.openzone.chat.presenter

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
 * One row in the chat thread. Renders user bubbles right-aligned, assistant
 * text left-aligned, system messages centered, and reasoning cards in a
 * collapsible container. Mirrors iOS `ChatMessageRowView`.
 */
@Composable
fun ChatMessageRowView(
    message: ChatMessage,
    isLastAssistantMessage: Boolean,
    streamingStatus: ChatStreamingStatus,
    modifier: Modifier = Modifier
) {
    when (message) {
        is ChatMessage.Text -> TextRow(
            message = message.message,
            isLastAssistantMessage = isLastAssistantMessage,
            streamingStatus = streamingStatus,
            modifier = modifier
        )
        is ChatMessage.Thinking -> AssistantSurround(modifier) {
            ChatReasoningCardView(
                message = message.message,
                isStreaming = !message.message.isComplete
            )
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Spacer(modifier = Modifier.widthIn(min = OPPOSITE_SIDE_MIN_WIDTH_DP.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = message.content,
                style = typography.messageBody,
                color = palette.userBubbleText,
                modifier = Modifier
                    .clip(UserBubbleCorner)
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = message.content.ifEmpty {
                if (streamingStatus == ChatStreamingStatus.RUNNING && !message.isComplete) "…" else ""
            },
            style = typography.messageBody,
            color = palette.assistantBubbleText,
            modifier = Modifier
                .widthIn(max = 540.dp)
                .align(Alignment.Start)
        )
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
        ChatStreamingStatus.RUNNING -> if (!isComplete) {
            StreamingDots(color = palette.streamingDot)
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
private fun AssistantSurround(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(modifier = Modifier.widthIn(max = 540.dp)) {
            content()
        }
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun StreamingDots(color: androidx.compose.ui.graphics.Color) {
    val transition = rememberInfiniteTransition(label = "streaming-dots")
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            val opacity by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, delayMillis = i * 120, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot-$i"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .alpha(opacity)
                    .clip(RoundedCornerShape(50))
                    .background(color)
                    .padding(3.dp)
            )
        }
    }
}

private val TimeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
private fun formatTime(timestamp: Long): String = TimeFormatter.format(Date(timestamp))
