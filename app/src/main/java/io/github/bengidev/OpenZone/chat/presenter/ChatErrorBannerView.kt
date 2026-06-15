package io.github.bengidev.openzone.chat.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.bengidev.openzone.chat.theme.ChatTheme
import io.github.bengidev.openzone.chat.theme.LocalChatTypography

/**
 * Persistent failure banner shown when a chat turn errors before or during
 * streaming. Mirrors iOS `ChatErrorBannerView`.
 */
@Composable
fun ChatErrorBannerView(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = ChatTheme.palette
    val typography = LocalChatTypography.current
    val shape = RoundedCornerShape(14.dp)

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .shadow(elevation = 10.dp, shape = shape, clip = false)
                .semantics { contentDescription = "Chat error banner" },
        shape = shape,
        color = palette.surfaceRaised,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .border(1.dp, palette.lineStrong, shape)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = palette.danger,
                modifier = Modifier.size(16.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Couldn't get a response",
                    style = typography.errorTitle,
                    color = palette.assistantBubbleText
                )
                Text(
                    text = errorMessage,
                    style = typography.errorBody,
                    color = palette.reasoningText
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onRetry,
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .background(palette.userBubble)
                            .semantics { contentDescription = "Retry sending the message" }
                ) {
                    Text(
                        text = "Retry",
                        style = typography.errorAction,
                        color = palette.userBubbleText
                    )
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.semantics { contentDescription = "Dismiss error" }
                ) {
                    Text(
                        text = "Dismiss",
                        style = typography.errorAction,
                        color = palette.reasoningText
                    )
                }
            }
        }
    }
}
