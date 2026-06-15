package io.github.bengidev.openzone.home.application

import io.github.bengidev.openzone.home.domain.ComposerContextUsage
import io.github.bengidev.openzone.home.domain.ComposerSpeedMode
import io.github.bengidev.openzone.home.domain.displayTitleForModelId
import io.github.bengidev.openzone.chat.domain.ChatStreamingStatus
import io.github.bengidev.openzone.shared.externals.networking.ChatModel
import io.github.bengidev.openzone.shared.externals.preference.ExternalAIProviderReasoningModel

/**
 * UI state for the home composer shell.
 *
 * Settings presentation is owned by
 * [io.github.bengidev.openzone.sidepanel.application.SidePanelComponent].
 */
data class HomeState(
        val draftMessage: String = "",
        val isSending: Boolean = false,
        val availableModels: List<ChatModel> = emptyList(),
        val selectedModelId: String? = null,
        val selectedProviderId: String = "openrouter",
        val reasoningLevel: ExternalAIProviderReasoningModel = ExternalAIProviderReasoningModel.High,
        val speedMode: ComposerSpeedMode = ComposerSpeedMode.Standard,
        val contextUsage: ComposerContextUsage =
                ComposerContextUsage(usedTokens = 107_000, tokenLimit = 258_000),
        val isContextUsagePresented: Boolean = false,
        val isModelPopupPresented: Boolean = false,
        val modelSearchQuery: String = "",
        val debouncedModelQuery: String = "",
        val modelFilterFreeOnly: Boolean = false,
        val isChatConfigured: Boolean = false,
        val hasApiKey: Boolean = false,
        val hasLoadedPreference: Boolean = false,
        val streamErrorMessage: String? = null,
        val chatStreamingStatus: ChatStreamingStatus = ChatStreamingStatus.IDLE
) {
 val canSend: Boolean
  get() =
          draftMessage.trim().isNotEmpty() &&
                  !isSending &&
                  hasApiKey &&
                  hasSelectedModel

 val hasSelectedModel: Boolean
  get() = !selectedModelId.isNullOrBlank()

 val showMissingApiKeyHint: Boolean
  get() = hasLoadedPreference && !hasApiKey

 /** Context ring is shown only once chat is configured (key + model), like the speed chip. */
 val showComposerContextUsage: Boolean
  get() = hasApiKey && hasSelectedModel

 val showChatErrorBanner: Boolean
  get() = chatStreamingStatus == ChatStreamingStatus.FAILED && streamErrorMessage != null

 val selectedModel: ChatModel?
  get() = availableModels.firstOrNull { it.id == selectedModelId }

 val selectedModelTitle: String
  get() =
          selectedModel?.displayName
                  ?: selectedModelId?.let(::displayTitleForModelId)
                  ?: "Select model"

 val selectedModelSupportsReasoning: Boolean
  get() = selectedModel?.supportsReasoning == true

 val filteredModels: List<ChatModel>
  get() =
          availableModels.filter { model ->
           val matchesFree = !modelFilterFreeOnly || model.isFree
           val q = debouncedModelQuery.trim()
           val matchesQuery =
                   q.isBlank() ||
                           model.displayName.contains(q, ignoreCase = true) ||
                           model.id.contains(q, ignoreCase = true)
           matchesFree && matchesQuery
          }
}
