package io.github.bengidev.openzone.chat.presenter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.bengidev.openzone.chat.domain.ChatThinkingMessage
import io.github.bengidev.openzone.chat.theme.ChatTheme
import io.github.bengidev.openzone.chat.theme.LocalChatTypography

/**
 * Collapsible reasoning card — streams monospace text while the model
 * thinks. Auto-expands while streaming, auto-collapses once finished,
 * user can manually toggle. Mirrors iOS `ChatReasoningCardView`.
 */
@Composable
fun ChatReasoningCardView(
    message: ChatThinkingMessage,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = ChatTheme.palette
    val typography = LocalChatTypography.current
    val content = message.content
    val isComplete = message.isComplete
    val hasBody = isStreaming || content.isNotEmpty()

    var isExpanded by remember(isStreaming) { mutableStateOf(isStreaming) }
    var didAutoCollapse by remember { mutableStateOf(false) }

    // Auto-collapse once thinking finishes (only once).
    androidx.compose.runtime.LaunchedEffect(isStreaming) {
        if (!isStreaming && !didAutoCollapse) {
            didAutoCollapse = true
            isExpanded = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.reasoningCard)
            .border(0.5.dp, palette.reasoningBorder, RoundedCornerShape(14.dp))
            .let { if (hasBody) it.clickable { isExpanded = !isExpanded } else it }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        ReasoningHeader(
            isComplete = isComplete,
            isStreaming = isStreaming,
            hasBody = hasBody,
            isExpanded = isExpanded
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (hasBody) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = palette.reasoningBorder.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                StreamingReasoningText(
                    // No "…" placeholder — the blinking caret alone signals
                    // streaming. The placeholder visually competed with the
                    // pulse dot in the header and read as a stacked indicator.
                    content = content,
                    isStreaming = isStreaming,
                    textColor = palette.reasoningText,
                    cursorColor = palette.streamingDot
                )
            }
        }
    }
}

@Composable
private fun ReasoningHeader(
    isComplete: Boolean,
    isStreaming: Boolean,
    hasBody: Boolean,
    isExpanded: Boolean
) {
    val palette = ChatTheme.palette
    val typography = LocalChatTypography.current
    val title = if (isComplete) "Thought" else "Thinking"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReasoningIcon()
        Text(
            text = title,
            style = typography.reasoningHeader,
            color = palette.reasoningText,
            fontWeight = FontWeight.Medium
        )
        if (isStreaming) {
            ReasoningPulseDot(color = palette.streamingDot)
        }
        Spacer(modifier = Modifier.weight(1f))
        if (hasBody) {
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = if (isExpanded) "Collapse reasoning" else "Expand reasoning",
                tint = palette.reasoningChevron,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(if (isExpanded) 180f else 0f)
            )
        }
    }
}

@Composable
private fun ReasoningIcon() {
    // Lightweight "thinking" marker — small accent square. Avoids an extra icon asset.
    val palette = ChatTheme.palette
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(palette.streamingDot)
    )
}

@Composable
private fun ReasoningPulseDot(color: Color) {
    val transition = rememberInfiniteTransition(label = "reasoning-pulse")
    val opacity by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-opacity"
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .alpha(opacity)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun StreamingReasoningText(
    content: String,
    isStreaming: Boolean,
    textColor: Color,
    cursorColor: Color
) {
    val typography = LocalChatTypography.current

    val cursorAlpha by if (isStreaming) {
        val transition = rememberInfiniteTransition(label = "reasoning-cursor")
        transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(550, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cursor-opacity"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Render the body text and the blinking caret as a single inline string so
    // the caret always trails the last character (and wraps with it) instead of
    // being pushed to the far edge of the row. Mirrors iOS lastTextBaseline HStack.
    val text = buildAnnotatedString {
        append(content)
        if (isStreaming) {
            withStyle(SpanStyle(color = cursorColor.copy(alpha = cursorAlpha))) {
                append("▍")
            }
        }
    }

    Text(
        text = text,
        style = typography.reasoningBody,
        color = textColor,
        modifier = Modifier.fillMaxWidth()
    )
}
