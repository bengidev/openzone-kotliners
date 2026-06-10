package io.github.bengidev.openzone.sidepanel.presenter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.home.theme.HomeTheme
import io.github.bengidev.openzone.sidepanel.application.SidePanelSessionComponent
import io.github.bengidev.openzone.sidepanel.domain.SidePanelSessionSection

/**
 * Saved-conversation sidebar drawer. Mirrors iOS `SidePanelSessionSidebarView`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SidePanelSessionSidebarView(
    component: SidePanelSessionComponent,
    onConversationSelected: (ChatConversation) -> Unit,
    onPinTapped: (ChatConversation) -> Unit,
    onRenameTapped: (ChatConversation) -> Unit,
    onDeleteTapped: (ChatConversation) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()
    val palette = HomeTheme.palette

    Box(
        modifier = modifier
            .fillMaxSize()
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
                .pointerInput(Unit) {}
        ) {
            SidebarSectionHeader("Conversations")

            if (state.conversations.isEmpty()) {
                SidebarEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    for (section in state.sections) {
                        item(key = "section-${section.id}") {
                            SectionHeader(title = section.title)
                        }
                        items(section.conversations, key = { conversation: ChatConversation -> conversation.id }) { conversation ->
                            ConversationRow(
                                conversation = conversation,
                                isActive = conversation.id == state.activeConversationId,
                                onClick = { onConversationSelected(conversation) },
                                onPin = { onPinTapped(conversation) },
                                onRename = { onRenameTapped(conversation) },
                                onDelete = { onDeleteTapped(conversation) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarSectionHeader(title: String) {
    val palette = HomeTheme.palette
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = palette.textPrimary,
            fontWeight = FontWeight.SemiBold,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    val palette = HomeTheme.palette
    Text(
        text = title,
        color = palette.textSecondary,
        fontWeight = FontWeight.Medium,
        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SidebarEmptyState() {
    val palette = HomeTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No conversations yet",
            color = palette.textSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationRow(
    conversation: ChatConversation,
    isActive: Boolean,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = HomeTheme.palette
    val bgColor = if (isActive) palette.inverseSurface.copy(alpha = 0.08f) else palette.surface
    val relativeTime = SidePanelSessionSection.relativeLabel(conversation.updatedAt)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Chat,
            contentDescription = null,
            tint = palette.textSecondary,
            modifier = Modifier.padding(end = 12.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.title,
                color = if (isActive) palette.textPrimary else palette.textPrimary,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            Text(
                text = relativeTime,
                color = palette.textSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall
            )
        }

        if (conversation.isPinned) {
            Icon(
                imageVector = Icons.Filled.PushPin,
                contentDescription = "Pinned",
                tint = palette.textSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
