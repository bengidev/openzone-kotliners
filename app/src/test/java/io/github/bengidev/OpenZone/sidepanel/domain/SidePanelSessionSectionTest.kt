package io.github.bengidev.openzone.sidepanel.domain

import io.github.bengidev.openzone.chat.domain.ChatConversation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SidePanelSessionSectionTest {

    @Test
    fun `grouped puts pinned conversations in a leading section`() {
        val now = 1_000_000L
        val day = TimeUnit.DAYS.toMillis(1)
        val conversations = listOf(
            ChatConversation(id = "recent", title = "Recent", updatedAt = now, isPinned = false),
            ChatConversation(id = "pinned", title = "Pinned", updatedAt = now - day, isPinned = true)
        )

        val sections = SidePanelSessionSection.grouped(conversations, now = now)

        assertEquals(listOf("pinned", "today"), sections.map { it.id })
        assertEquals(listOf("pinned"), sections.first().conversations.map { it.id })
    }

    @Test
    fun `grouped assigns recency buckets by updatedAt`() {
        val now = 10_000_000L
        val day = TimeUnit.DAYS.toMillis(1)
        val calendar = Calendar.getInstance()
        val conversations = listOf(
            ChatConversation(id = "today", title = "Today", updatedAt = now),
            ChatConversation(id = "yesterday", title = "Yesterday", updatedAt = now - day),
            ChatConversation(id = "week", title = "Week", updatedAt = now - (3 * day)),
            ChatConversation(id = "month", title = "Month", updatedAt = now - (10 * day)),
            ChatConversation(id = "older", title = "Older", updatedAt = now - (40 * day))
        )

        val sections = SidePanelSessionSection.grouped(conversations, now = now, calendar = calendar)

        assertEquals(
            listOf("today", "yesterday", "previous7Days", "previous30Days", "older"),
            sections.map { it.id }
        )
        assertEquals("today", sections.first().conversations.single().id)
    }

    @Test
    fun `relativeLabel uses compact units`() {
        val now = 1_000_000L
        val minute = TimeUnit.MINUTES.toMillis(1)
        val hour = TimeUnit.HOURS.toMillis(1)
        val day = TimeUnit.DAYS.toMillis(1)

        assertEquals("now", SidePanelSessionSection.relativeLabel(now, now))
        assertEquals("5m", SidePanelSessionSection.relativeLabel(now - 5 * minute, now))
        assertEquals("3h", SidePanelSessionSection.relativeLabel(now - 3 * hour, now))
        assertEquals("2d", SidePanelSessionSection.relativeLabel(now - 2 * day, now))
    }

    @Test
    fun `grouped drops empty buckets`() {
        val now = 1_000_000L
        val sections = SidePanelSessionSection.grouped(
            listOf(ChatConversation(id = "only", title = "Only", updatedAt = now)),
            now = now
        )
        assertTrue(sections.all { it.conversations.isNotEmpty() })
        assertEquals(1, sections.size)
    }
}
