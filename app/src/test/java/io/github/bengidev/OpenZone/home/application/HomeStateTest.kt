package io.github.bengidev.openzone.home.application

import io.github.bengidev.openzone.chat.domain.ChatStreamingStatus
import io.github.bengidev.openzone.shared.externals.networking.ChatModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeStateTest {

    @Test
    fun `catalog is empty until api key loads live models`() {
        val withoutKey = HomeState(hasApiKey = false, availableModels = emptyList())
        assertTrue(withoutKey.filteredModels.isEmpty())

        val withKeyButNoCatalog =
            HomeState(
                hasApiKey = true,
                availableModels = emptyList(),
                modelFilterFreeOnly = true
            )
        assertTrue(withKeyButNoCatalog.filteredModels.isEmpty())
    }

    @Test
    fun `send stays closed without api key`() {
        val state =
            HomeState(
                draftMessage = "hello",
                hasLoadedPreference = true,
                hasApiKey = false,
                selectedModelId = null
            )

        assertFalse(state.hasSelectedModel)
        assertFalse(state.canSend)
    }

    @Test
    fun `free-only filter excludes paid models`() {
        val models =
            listOf(
                ChatModel(id = "free", displayName = "Free", providerId = "openrouter", isFree = true),
                ChatModel(id = "paid", displayName = "Paid", providerId = "openrouter", isFree = false)
            )
        val filtered =
            HomeState(availableModels = models, modelFilterFreeOnly = true).filteredModels

        assertTrue(filtered.all { it.isFree })
        assertEquals(1, filtered.size)
    }

    @Test
    fun `context usage hidden until api key and model are set`() {
        val unconfigured =
            HomeState(
                hasApiKey = false,
                selectedModelId = null,
                hasLoadedPreference = true
            )
        assertFalse(unconfigured.showComposerContextUsage)

        val configured =
            HomeState(
                hasApiKey = true,
                selectedModelId = "meta-llama/llama-3.3-70b-instruct:free"
            )
        assertTrue(configured.showComposerContextUsage)
    }

    @Test
    fun `error banner shows only when stream failed with message`() {
        val hidden =
            HomeState(
                chatStreamingStatus = ChatStreamingStatus.RUNNING,
                streamErrorMessage = "boom"
            )
        assertFalse(hidden.showChatErrorBanner)

        val visible =
            HomeState(
                chatStreamingStatus = ChatStreamingStatus.FAILED,
                streamErrorMessage = "Forbidden (403): upgrade required"
            )
        assertTrue(visible.showChatErrorBanner)
    }
}
