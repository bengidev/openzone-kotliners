package io.github.bengidev.openzone.home.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.home.theme.HomeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sidebar conversation-history drawer (issue #8).
 *
 * Hosts the persisted conversation list only — Settings deliberately stays a
 * separate top-bar sheet and is never relocated here. Tapping a row reopens that
 * conversation in the chat thread via [onConversationSelected]. A scrim behind
 * the panel dismisses the drawer on tap.
 *
 * Left-anchored panel overlaid by [HomeScreen] when `state.isSidebarPresented`
 * is true. All colors are sourced from the authoritative `OpenZonePalette`
 * (via `HomeTheme.palette`); no hardcoded color literals.
 */
@Composable
fun HomeSidebarView(
    conversations: List<ChatConversation>,
    onConversationSelected: (ChatConversation) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = HomeTheme.palette

    Box(
        modifier = modifier
            .fillMaxSize()
            // Scrim: tap anywhere outside the panel dismisses the drawer.
            .background(palette.inverseSurface.copy(alpha = 0.32f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.82f)
                .widthIn(max = 320.dp)
                .background(palette.surface)
                // Consume taps on the panel so they don't reach the scrim.
                .pointerInput(Unit) {}
        ) {
            SidebarHeader()

            if (conversations.isEmpty()) {
                SidebarEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    items(conversations, key = { it.id }) { conversation ->
                        ConversationRow(
                            conversation = conversation,
                            onClick = { onConversationSelected(conversation) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarHeader() {
    val palette = HomeTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Text(
            text = "Conversations",
            color = palette.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SidebarEmptyState() {
    val palette = HomeTheme.palette
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No conversations yet.\nStart chatting to see your history here.",
            color = palette.textSecondary
        )
    }
}

@Composable
private fun ConversationRow(
    conversation: ChatConversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = HomeTheme.palette
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = conversation.title.ifBlank { "Untitled chat" },
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatTimestamp(conversation.updatedAt),
                color = palette.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val DateFormatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

private fun formatTimestamp(epochMillis: Long): String =
    DateFormatter.format(Date(epochMillis))
