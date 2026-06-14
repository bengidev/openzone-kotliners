package io.github.bengidev.openzone.sidepanel.domain

import io.github.bengidev.openzone.chat.domain.ChatConversation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SidePanelSessionSectionTest {

    @Test
    fun grouped_emits_collapsible_group_sections() {
        val now = 1_700_000_000_000L
        val conversations = listOf(
            ChatConversation(id = "1", title = "In group", updatedAt = now, groupName = "Work"),
            ChatConversation(id = "2", title = "Ungrouped", updatedAt = now - 86_400_000)
        )

        val collapsed = SidePanelSessionSection.grouped(conversations, now = now)
        val workSection = collapsed.first { it.id == "group:Work" }
        assertTrue(workSection.isGroupSection)
        assertEquals(emptyList<ChatConversation>(), workSection.conversations)

        val expanded = SidePanelSessionSection.grouped(conversations, now = now, expandedGroups = setOf("Work"))
        assertEquals(1, expanded.first { it.id == "group:Work" }.conversations.size)
    }
}
