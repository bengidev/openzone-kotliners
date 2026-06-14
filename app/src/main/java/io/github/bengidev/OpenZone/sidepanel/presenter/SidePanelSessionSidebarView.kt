package io.github.bengidev.openzone.sidepanel.presenter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Chat
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.home.theme.HomeTheme
import io.github.bengidev.openzone.sidepanel.application.SidePanelSessionComponent
import io.github.bengidev.openzone.sidepanel.domain.SidePanelSessionSection

private const val DrawerWidthRatio = 0.82f
private val MaxDrawerWidth = 360.dp

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

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
                visible = state.isSidebarVisible,
                enter = slideInHorizontally(animationSpec = tween(280)) { -it },
                exit = slideOutHorizontally(animationSpec = tween(280)) { -it }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(palette.textPrimary.copy(alpha = 0.32f))
                                        .semantics { contentDescription = "Dismiss sidebar" }
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                    onTap = { component.onDismissSidebar() }
                                            )
                                        }
                )

                Column(
                        modifier =
                                Modifier.align(Alignment.CenterStart)
                                        .fillMaxHeight()
                                        .fillMaxWidth(DrawerWidthRatio)
                                        .widthIn(max = MaxDrawerWidth)
                                        .statusBarsPadding()
                                        .background(palette.surfacePaper)
                                        .pointerInput(Unit) {}
                ) {
                    SidebarHeader(
                            onSettingsTapped = component::onSettingsButtonTapped,
                            onDismissTapped = component::onDismissSidebar
                    )

                    SidebarSearchField(
                            query = state.historySearchQuery,
                            onQueryChanged = component::onSearchQueryChanged
                    )

                    HorizontalDivider(color = palette.textTertiary.copy(alpha = 0.25f))

                    when {
                        state.conversations.isEmpty() ->
                                EmptyState(
                                        icon = Icons.Outlined.Chat,
                                        title = "No conversations yet",
                                        subtitle = "Your chats will appear here."
                                )
                        !state.hasSearchResults ->
                                EmptyState(
                                        icon = Icons.Default.Search,
                                        title = "No matches",
                                        subtitle = "No conversations match your search."
                                )
                        else ->
                                LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        contentPadding =
                                                PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    state.sections.forEach { section ->
                                        stickyHeader(key = "header-${section.id}") {
                                            SectionHeader(
                                                    section = section,
                                                    isExpanded = section.isGroupExpanded,
                                                    onGroupToggled = component::onGroupHeaderToggled
                                            )
                                        }
                                        items(section.conversations, key = { it.id }) { conversation
                                            ->
                                            ConversationRow(
                                                    conversation = conversation,
                                                    isActive =
                                                            conversation.id ==
                                                                    state.activeConversationId,
                                                    isInGroup = section.isGroupSection,
                                                    onClick = {
                                                        component.onConversationSelected(
                                                                conversation
                                                        )
                                                    },
                                                    onLongClick = { menuTarget = conversation }
                                            )
                                        }
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
                                renameTarget?.let {
                                    component.onRenameConversation(it.id, renameText)
                                }
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
                dismissButton = {
                    TextButton(onClick = { newGroupTarget = null }) { Text("Cancel") }
                }
        )
    }
}

@Composable
private fun SidebarHeader(onSettingsTapped: () -> Unit, onDismissTapped: () -> Unit) {
    val palette = HomeTheme.palette
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
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onSettingsTapped, modifier = Modifier.size(40.dp)) {
            Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(18.dp)
            )
        }
        IconButton(onClick = onDismissTapped, modifier = Modifier.size(40.dp)) {
            Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close history",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SidebarSearchField(query: String, onQueryChanged: (String) -> Unit) {
    val palette = HomeTheme.palette
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                            .background(palette.surfaceSubtle, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = palette.textTertiary,
                modifier = Modifier.size(14.dp)
        )
        BasicTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = palette.textPrimary, fontSize = 15.sp),
                singleLine = true,
                cursorBrush = SolidColor(palette.accentPrimary),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                    text = "Search conversations",
                                    color = palette.textTertiary,
                                    fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                }
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChanged("") }, modifier = Modifier.size(24.dp)) {
                Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = palette.textTertiary,
                        modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
        section: SidePanelSessionSection,
        isExpanded: Boolean,
        onGroupToggled: (String) -> Unit
) {
    val palette = HomeTheme.palette
    if (section.isGroupSection) {
        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .background(palette.surfacePaper)
                                .clickable { onGroupToggled(section.title) }
                                .padding(horizontal = 12.dp)
                                .padding(top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                    imageVector = Icons.Filled.Folder,
                    contentDescription = null,
                    tint = palette.accentSoft,
                    modifier = Modifier.size(11.dp)
            )
            Text(
                    text = section.title.uppercase(),
                    color = palette.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
            )
            Icon(
                    imageVector =
                            if (isExpanded) Icons.Default.KeyboardArrowDown
                            else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = palette.textTertiary,
                    modifier = Modifier.size(12.dp)
            )
        }
    } else {
        Text(
                text = section.title.uppercase(),
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier =
                        Modifier.fillMaxWidth()
                                .background(palette.surfacePaper)
                                .padding(horizontal = 12.dp)
                                .padding(top = 12.dp, bottom = 4.dp)
        )
    }
}

@Composable
private fun ColumnScope.EmptyState(icon: ImageVector, title: String, subtitle: String) {
    val palette = HomeTheme.palette
    Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.textTertiary,
                modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
                text = title,
                color = palette.textSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Text(text = subtitle, color = palette.textTertiary, fontSize = 13.sp)
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
    val bg = if (isActive) palette.surfaceSubtle else Color.Transparent
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (conversation.isPinned) {
            Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = palette.textTertiary,
                    modifier = Modifier.size(11.dp)
            )
        }
        if (!isInGroup && conversation.groupName != null) {
            Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = palette.accentSoft,
                    modifier = Modifier.size(11.dp)
            )
        }
        Text(
                text = conversation.title,
                color = palette.textPrimary,
                fontSize = 15.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
        )
        Text(
                text = SidePanelSessionSection.relativeLabel(conversation.updatedAt),
                color = palette.textTertiary,
                fontSize = 12.sp
        )
    }
}
