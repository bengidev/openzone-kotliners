package io.github.bengidev.openzone.sidepanel.presenter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.home.theme.HomeTheme
import io.github.bengidev.openzone.sidepanel.application.SidePanelSessionComponent
import io.github.bengidev.openzone.sidepanel.domain.SidePanelSessionSection

/** Saved-conversation sidebar drawer. Mirrors iOS `SidePanelSessionSidebarView`. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SidePanelSessionSidebarView(
        component: SidePanelSessionComponent,
        modifier: Modifier = Modifier
) {
 val state by component.state.subscribeAsState()
 val palette = HomeTheme.palette

 var renameTarget by remember { mutableStateOf<ChatConversation?>(null) }
 var renameText by remember { mutableStateOf("") }
 var menuTarget by remember { mutableStateOf<ChatConversation?>(null) }
 var newGroupTarget by remember { mutableStateOf<ChatConversation?>(null) }
 var newGroupText by remember { mutableStateOf("") }

 Box(
         modifier =
                 modifier.fillMaxSize()
                         .background(palette.textPrimary.copy(alpha = 0.32f))
                         .pointerInput(Unit) {
                          detectTapGestures(onTap = { component.onDismissSidebar() })
                         }
 ) {
  Column(
          modifier =
                  Modifier.fillMaxHeight()
                          .fillMaxWidth(0.82f)
                          .widthIn(max = 360.dp)
                          .background(palette.surface)
                          .pointerInput(Unit) {}
  ) {
   Row(
           modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
           verticalAlignment = Alignment.CenterVertically
   ) {
    Text(
            "History",
            color = palette.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
    )
    IconButton(onClick = component::onSettingsButtonTapped) {
     Icon(Icons.Default.Settings, contentDescription = "Settings", tint = palette.textPrimary)
    }
    IconButton(onClick = component::onDismissSidebar) {
     Icon(Icons.Default.Close, contentDescription = "Close history", tint = palette.textSecondary)
    }
   }

   OutlinedTextField(
           value = state.historySearchQuery,
           onValueChange = component::onSearchQueryChanged,
           modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
           placeholder = { Text("Search conversations", color = palette.textTertiary) },
           leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = palette.textTertiary)
           },
           singleLine = true,
           shape = CircleShape,
           colors =
                   TextFieldDefaults.colors(
                           focusedContainerColor = palette.surfaceSubtle,
                           unfocusedContainerColor = palette.surfaceSubtle,
                           focusedTextColor = palette.textPrimary,
                           unfocusedTextColor = palette.textPrimary
                   )
   )

   Spacer(Modifier.height(12.dp))
   HorizontalDivider(color = palette.textTertiary.copy(alpha = 0.25f))

   when {
    state.conversations.isEmpty() ->
            EmptyState("No conversations yet", "Your chats will appear here.")
    !state.hasSearchResults -> EmptyState("No matches", "No conversations match your search.")
    else -> {
     LazyColumn(
             modifier = Modifier.fillMaxWidth().weight(1f),
             contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
             verticalArrangement = Arrangement.spacedBy(4.dp)
     ) {
      state.sections.forEach { section ->
       item(key = "header-${section.id}") {
        if (section.isGroupSection) {
         Row(
                 modifier =
                         Modifier.fillMaxWidth()
                                 .clickable { component.onGroupHeaderToggled(section.title) }
                                 .padding(horizontal = 12.dp, vertical = 8.dp),
                 verticalAlignment = Alignment.CenterVertically
         ) {
          Icon(
                  Icons.Filled.Folder,
                  contentDescription = null,
                  tint = palette.accentSoft,
                  modifier = Modifier.padding(end = 6.dp)
          )
          Text(
                  section.title.uppercase(),
                  color = palette.textSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
          )
         }
        } else {
         Text(
                 section.title.uppercase(),
                 color = palette.textTertiary,
                 fontSize = 11.sp,
                 fontWeight = FontWeight.SemiBold,
                 modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
         )
        }
       }
       items(section.conversations, key = { it.id }) { conversation ->
        ConversationRow(
                conversation = conversation,
                isActive = conversation.id == state.activeConversationId,
                isInGroup = section.isGroupSection,
                onClick = { component.onConversationSelected(conversation) },
                onLongClick = { menuTarget = conversation }
        )
       }
      }
     }
    }
   }
  }
 }

 menuTarget?.let { target ->
  DropdownMenu(expanded = true, onDismissRequest = { menuTarget = null }) {
   DropdownMenuItem(
           text = { Text("Rename") },
           onClick = {
            renameTarget = target
            renameText = target.title
            menuTarget = null
           }
   )
   DropdownMenuItem(
           text = { Text(if (target.isPinned) "Unpin" else "Pin") },
           onClick = {
            component.onPinConversation(target)
            menuTarget = null
           }
   )
   state.availableGroups.forEach { group ->
    DropdownMenuItem(
            text = { Text(group) },
            onClick = {
             val next = if (target.groupName == group) null else group
             component.onConversationGroupChanged(target.id, next)
             menuTarget = null
            }
    )
   }
   if (target.groupName != null) {
    DropdownMenuItem(
            text = { Text("Remove from ${target.groupName}") },
            onClick = {
             component.onConversationGroupChanged(target.id, null)
             menuTarget = null
            }
    )
   }
   DropdownMenuItem(
           text = { Text("New Group...") },
           onClick = {
            newGroupTarget = target
            newGroupText = ""
            menuTarget = null
           }
   )
   DropdownMenuItem(
           text = { Text("Delete") },
           onClick = {
            component.onDeleteConversation(target)
            menuTarget = null
           }
   )
  }
 }

 if (renameTarget != null) {
  AlertDialog(
          onDismissRequest = { renameTarget = null },
          title = { Text("Rename conversation") },
          text = {
           OutlinedTextField(
                   value = renameText,
                   onValueChange = { renameText = it },
                   singleLine = true
           )
          },
          confirmButton = {
           TextButton(
                   onClick = {
                    renameTarget?.let { component.onRenameConversation(it.id, renameText) }
                    renameTarget = null
                   }
           ) { Text("Save") }
          },
          dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Cancel") } }
  )
 }

 if (newGroupTarget != null) {
  AlertDialog(
          onDismissRequest = { newGroupTarget = null },
          title = { Text("Create Group") },
          text = {
           OutlinedTextField(
                   value = newGroupText,
                   onValueChange = { newGroupText = it },
                   singleLine = true,
                   placeholder = { Text("Group name") }
           )
          },
          confirmButton = {
           TextButton(
                   onClick = {
                    newGroupTarget?.let {
                     component.onConversationGroupChanged(it.id, newGroupText)
                    }
                    newGroupTarget = null
                   }
           ) { Text("Create") }
          },
          dismissButton = { TextButton(onClick = { newGroupTarget = null }) { Text("Cancel") } }
  )
 }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
 val palette = HomeTheme.palette
 Column(
         modifier = Modifier.fillMaxWidth().padding(32.dp),
         horizontalAlignment = Alignment.CenterHorizontally,
         verticalArrangement = Arrangement.Center
 ) {
  Spacer(Modifier.height(48.dp))
  Text(title, color = palette.textSecondary, fontWeight = FontWeight.Medium)
  Text(subtitle, color = palette.textTertiary, fontSize = 13.sp)
 }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationRow(
        conversation: ChatConversation,
        isActive: Boolean,
        isInGroup: Boolean,
        onClick: () -> Unit,
        onLongClick: () -> Unit
) {
 val palette = HomeTheme.palette
 val bg = if (isActive) palette.surfaceSubtle else palette.surface
 Row(
         modifier =
                 Modifier.fillMaxWidth()
                         .background(bg, RoundedCornerShape(10.dp))
                         .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                         .padding(
                                 start = if (isInGroup) 18.dp else 12.dp,
                                 end = 12.dp,
                                 top = 10.dp,
                                 bottom = 10.dp
                         ),
         verticalAlignment = Alignment.CenterVertically
 ) {
  if (conversation.isPinned) {
   Icon(
           Icons.Default.PushPin,
           contentDescription = null,
           tint = palette.textTertiary,
           modifier = Modifier.padding(end = 6.dp)
   )
  }
  if (!isInGroup && conversation.groupName != null) {
   Icon(
           Icons.Outlined.Folder,
           contentDescription = null,
           tint = palette.accentSoft,
           modifier = Modifier.padding(end = 6.dp)
   )
  }
  Text(
          conversation.title,
          color = palette.textPrimary,
          fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
  )
  Text(
          SidePanelSessionSection.relativeLabel(conversation.updatedAt),
          color = palette.textTertiary,
          fontSize = 12.sp
  )
 }
}
