package io.github.bengidev.openzone.sidepanel.presenter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.sidepanel.application.SidePanelSessionComponent
import io.github.bengidev.openzone.sidepanel.domain.SidePanelSessionSection
import io.github.bengidev.openzone.sidepanel.theme.SidePanelTheme

/**
 * Saved-conversation sidebar drawer. Mirrors iOS `SidePanelSessionSidebarView` 1 — header (title +
 * settings gear + close), capsule search field with clear button, divider, empty / no-results
 * centered states, and a long-press context menu (rename / pin-toggle / delete) per row. Colors are
 * sourced from [SidePanelTheme.palette].
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SidePanelSessionSidebarView(
        component: SidePanelSessionComponent,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
) {
   val state by component.state.subscribeAsState()
   val palette = SidePanelTheme.palette

   var renameTarget by remember { mutableStateOf<ChatConversation?>(null) }
   var renameText by remember { mutableStateOf("") }
   var deleteTarget by remember { mutableStateOf<ChatConversation?>(null) }

   Box(
           modifier =
                   modifier.fillMaxSize()
                           .background(palette.inverseSurface.copy(alpha = 0.32f))
                           .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) }
   ) {
      Column(
              modifier =
                      Modifier.fillMaxHeight()
                              .fillMaxWidth(0.82f)
                              .widthIn(max = 360.dp)
                              .background(palette.surfacePaper)
                              .pointerInput(Unit) {}
      ) {
         SidebarHeader(
                 onSettingsTapped = { component.onSettingsButtonTapped() },
                 onCloseTapped = onDismiss
         )

         SearchField(
                 query = state.historySearchQuery,
                 onQueryChanged = component::onSearchQueryChanged
         )

         HorizontalDivider(color = palette.textTertiary.copy(alpha = 0.25f), thickness = 1.dp)
         when {
            state.conversations.isEmpty() -> {
               SidebarEmptyState(modifier = Modifier.weight(1f))
            }
            state.filteredConversations.isEmpty() -> {
               SidebarNoResultsState(modifier = Modifier.weight(1f))
            }
            else ->
                    ConversationSectionsList(
                            sections = state.sections,
                            activeConversationId = state.activeConversationId,
                            onConversationSelected = component::onConversationSelected,
                            onPinTapped = component::onPinConversation,
                            onRenameTapped = { conversation ->
                               renameText = conversation.title
                               renameTarget = conversation
                            },
                            onDeleteTapped = { deleteTarget = it },
                            modifier = Modifier.weight(1f)
                    )
         }
      }

      if (renameTarget != null) {
         RenameConversationDialog(
                 initialTitle = renameText,
                 onTitleChange = { renameText = it },
                 onConfirm = {
                    val target = renameTarget
                    if (target != null) {
                       component.onRenameConversation(target, renameText)
                    }
                    renameTarget = null
                 },
                 onDismiss = { renameTarget = null }
         )
      }

      val pendingDelete = deleteTarget
      if (pendingDelete != null) {
         DeleteConversationDialog(
                 conversationTitle = pendingDelete.title,
                 onConfirm = {
                    component.onDeleteConversation(pendingDelete)
                    deleteTarget = null
                 },
                 onDismiss = { deleteTarget = null }
         )
      }
   }
}

@Composable
private fun SidebarHeader(onSettingsTapped: () -> Unit, onCloseTapped: () -> Unit) {
   val palette = SidePanelTheme.palette
   Row(
           modifier =
                   Modifier.fillMaxWidth()
                           .padding(horizontal = 20.dp)
                           .padding(top = 24.dp, bottom = 12.dp),
           verticalAlignment = Alignment.CenterVertically
   ) {
      Text(
              text = "History",
              color = palette.textPrimary,
              fontWeight = FontWeight.SemiBold,
              fontSize = 20.sp,
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.weight(1f)
      )

      IconButton(onClick = onSettingsTapped) {
         Icon(
                 imageVector = Icons.Filled.Settings,
                 contentDescription = "Settings",
                 tint = palette.textPrimary,
                 modifier = Modifier.size(18.dp)
         )
      }

      IconButton(onClick = onCloseTapped) {
         Icon(
                 imageVector = Icons.Filled.Close,
                 contentDescription = "Close history",
                 tint = palette.textSecondary,
                 modifier = Modifier.size(16.dp)
         )
      }
   }
}

@Composable
private fun SearchField(query: String, onQueryChanged: (String) -> Unit) {
   val palette = SidePanelTheme.palette
   Row(
           modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
           verticalAlignment = Alignment.CenterVertically
   ) {
      Row(
              modifier =
                      Modifier.fillMaxWidth()
                              .background(palette.surfaceSubtle, CircleShape)
                              .padding(horizontal = 12.dp, vertical = 9.dp),
              verticalAlignment = Alignment.CenterVertically
      ) {
         Icon(
                 imageVector = Icons.Filled.Search,
                 contentDescription = null,
                 tint = palette.textTertiary,
                 modifier = Modifier.size(14.dp)
         )

         Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            TextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                       Text(
                               text = "Search conversations",
                               color = palette.textTertiary,
                               fontSize = 15.sp
                       )
                    },
                    textStyle =
                            MaterialTheme.typography.bodyMedium.copy(
                                    color = palette.textPrimary,
                                    fontSize = 15.sp
                            ),
                    colors =
                            androidx.compose.material3.TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = palette.accentPrimary
                            )
            )
         }

         if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChanged("") }, modifier = Modifier.size(24.dp)) {
               Icon(
                       imageVector = Icons.Filled.Clear,
                       contentDescription = "Clear search",
                       tint = palette.textTertiary,
                       modifier = Modifier.size(15.dp)
               )
            }
         }
      }
   }
}

@Composable
private fun SidebarEmptyState(modifier: Modifier = Modifier) {
   CenteredSidebarState(
           icon = Icons.AutoMirrored.Filled.Chat,
           title = "No conversations yet",
           subtitle = "Your chats will appear here.",
           modifier = modifier
   )
}

@Composable
private fun SidebarNoResultsState(modifier: Modifier = Modifier) {
   CenteredSidebarState(
           icon = Icons.Filled.Search,
           title = "No matches",
           subtitle = "No conversations match your search.",
           modifier = modifier
   )
}

@Composable
private fun CenteredSidebarState(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        title: String,
        subtitle: String,
        modifier: Modifier = Modifier
) {
   val palette = SidePanelTheme.palette
   Box(
           modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
           contentAlignment = Alignment.Center
   ) {
      Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         Icon(
                 imageVector = icon,
                 contentDescription = null,
                 tint = palette.textTertiary,
                 modifier = Modifier.size(28.dp)
         )
         Text(
                 text = title,
                 color = palette.textSecondary,
                 fontWeight = FontWeight.Medium,
                 fontSize = 15.sp,
                 style = MaterialTheme.typography.bodyMedium
         )
         Text(
                 text = subtitle,
                 color = palette.textTertiary,
                 fontSize = 13.sp,
                 style = MaterialTheme.typography.bodySmall
         )
      }
   }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationSectionsList(
        sections: List<SidePanelSessionSection>,
        activeConversationId: String?,
        onConversationSelected: (ChatConversation) -> Unit,
        onPinTapped: (ChatConversation) -> Unit,
        onRenameTapped: (ChatConversation) -> Unit,
        onDeleteTapped: (ChatConversation) -> Unit,
        modifier: Modifier = Modifier
) {
   LazyColumn(
           modifier = modifier.fillMaxWidth(),
           contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
           verticalArrangement = Arrangement.spacedBy(4.dp)
   ) {
      for (section in sections) {
         item(key = "section-${section.id}") { SectionHeader(title = section.title) }
         items(
                 items = section.conversations,
                 key = { conversation: ChatConversation -> conversation.id }
         ) { conversation ->
            ConversationRow(
                    conversation = conversation,
                    isActive = conversation.id == activeConversationId,
                    onClick = { onConversationSelected(conversation) },
                    onPin = { onPinTapped(conversation) },
                    onRename = { onRenameTapped(conversation) },
                    onDelete = { onDeleteTapped(conversation) }
            )
         }
      }
   }
}

@Composable
private fun SectionHeader(title: String) {
   val palette = SidePanelTheme.palette
   Box(
           modifier =
                   Modifier.fillMaxWidth()
                           .background(palette.surfacePaper)
                           .padding(horizontal = 12.dp)
                           .padding(top = 12.dp, bottom = 4.dp)
   ) {
      Text(
              text = title.uppercase(),
              color = palette.textTertiary,
              fontWeight = FontWeight.SemiBold,
              fontSize = 11.sp,
              style = MaterialTheme.typography.labelMedium
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
   val palette = SidePanelTheme.palette
   val relativeTime = SidePanelSessionSection.relativeLabel(conversation.updatedAt)
   var menuExpanded by remember { mutableStateOf(false) }

   val rowBg = if (isActive) palette.surfaceSubtle else palette.surfacePaper

   Box(modifier = modifier.fillMaxWidth()) {
      Row(
              modifier =
                      Modifier.fillMaxWidth()
                              .background(rowBg, RoundedCornerShape(10.dp))
                              .combinedClickable(
                                      onClick = onClick,
                                      onLongClick = { menuExpanded = true }
                              )
                              .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
         if (conversation.isPinned) {
            Icon(
                    imageVector = Icons.Filled.PushPin,
                    contentDescription = "Pinned",
                    tint = palette.textTertiary,
                    modifier = Modifier.size(11.dp)
            )
         }

         Text(
                 text = conversation.title,
                 color = palette.textPrimary,
                 fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                 fontSize = 15.sp,
                 maxLines = 1,
                 overflow = TextOverflow.Ellipsis,
                 style = MaterialTheme.typography.bodyMedium,
                 modifier = Modifier.weight(1f)
         )

         Text(
                 text = relativeTime,
                 color = palette.textTertiary,
                 fontSize = 12.sp,
                 style = MaterialTheme.typography.labelSmall,
                 maxLines = 1
         )
      }

      DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
         DropdownMenuItem(
                 text = { Text("Rename") },
                 onClick = {
                    menuExpanded = false
                    onRename()
                 }
         )
         DropdownMenuItem(
                 text = { Text(if (conversation.isPinned) "Unpin" else "Pin") },
                 onClick = {
                    menuExpanded = false
                    onPin()
                 }
         )
         DropdownMenuItem(
                 text = { Text(text = "Delete", color = palette.danger) },
                 onClick = {
                    menuExpanded = false
                    onDelete()
                 }
         )
      }
   }
}

@Composable
private fun RenameConversationDialog(
        initialTitle: String,
        onTitleChange: (String) -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
) {
   AlertDialog(
           onDismissRequest = onDismiss,
           title = { Text("Rename conversation") },
           text = {
              TextField(
                      value = initialTitle,
                      onValueChange = onTitleChange,
                      modifier = Modifier.fillMaxWidth(),
                      singleLine = true,
                      placeholder = { Text("Title") }
              )
           },
           confirmButton = {
              TextButton(onClick = onConfirm, enabled = initialTitle.trim().isNotEmpty()) {
                 Text("Save")
              }
           },
           dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
   )
}

@Composable
private fun DeleteConversationDialog(
        conversationTitle: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
) {
   val palette = SidePanelTheme.palette
   val label = conversationTitle.ifBlank { "Untitled chat" }
   AlertDialog(
           onDismissRequest = onDismiss,
           title = { Text("Delete conversation?") },
           text = { Text("\"$label\" will be permanently deleted.") },
           confirmButton = {
              TextButton(onClick = onConfirm) { Text("Delete", color = palette.danger) }
           },
           dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
   )
}
