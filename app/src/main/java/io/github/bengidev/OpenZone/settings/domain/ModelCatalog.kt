package io.github.bengidev.openzone.settings.domain

import io.github.bengidev.openzone.shared.externals.networking.ChatModel

/**
 * Curated fallback catalog of free models. Used when a live model list is
 * unavailable (offline, no key yet). All slugs verified against the live
 * OpenRouter `/models` endpoint on 2026-06-07.
 *
 * Produces [ChatModel] instances — the single shared model-identity type used
 * by both Home and Settings. `CatalogModel` is retired; callers that previously
 * used `CatalogModel` should switch to `ChatModel` from `shared/networking`.
 *
 * Mirrors iOS `ChatModelCatalog`.
 */
object ModelCatalog {

    /** OpenRouter free-tier fallback models, in display order. */
    val openRouterFree: List<ChatModel> = listOf(
        ChatModel(
            id = "meta-llama/llama-3.3-70b-instruct:free",
            displayName = "Llama 3.3 70B Instruct",
            providerId = "openrouter",
            isFree = true,
            contextLength = 131_072,
            supportsReasoning = false,
            description = "Meta instruct model, broad knowledge"
        ),
        ChatModel(
            id = "nousresearch/hermes-3-llama-3.1-405b:free",
            displayName = "Hermes 3 405B Instruct",
            providerId = "openrouter",
            isFree = true,
            contextLength = 131_072,
            supportsReasoning = false,
            description = "Nous Research, strong instruction following"
        ),
        ChatModel(
            id = "nvidia/nemotron-3-ultra-550b-a55b:free",
            displayName = "Nemotron 3 Ultra 550B",
            providerId = "openrouter",
            isFree = true,
            contextLength = 1_000_000,
            supportsReasoning = true,
            description = "NVIDIA, 1M context, reasoning support"
        ),
        ChatModel(
            id = "google/gemma-4-31b-it:free",
            displayName = "Gemma 4 31B",
            providerId = "openrouter",
            isFree = true,
            contextLength = 262_144,
            supportsReasoning = false,
            description = "Google, 262K context, instruction tuned"
        ),
        ChatModel(
            id = "qwen/qwen3-coder:free",
            displayName = "Qwen3 Coder 480B",
            providerId = "openrouter",
            isFree = true,
            contextLength = 1_048_576,
            supportsReasoning = false,
            description = "Qwen, 1M context, strong at coding"
        ),
        ChatModel(
            id = "moonshotai/kimi-k2.6:free",
            displayName = "Kimi K2.6",
            providerId = "openrouter",
            isFree = true,
            contextLength = 262_144,
            supportsReasoning = false,
            description = "Moonshot AI, 262K context"
        )
    )

    /** All curated models across providers, in display order. */
    val all: List<ChatModel> = openRouterFree

    /** Curated models for [providerId], in display order. */
    fun forProvider(providerId: String): List<ChatModel> =
        all.filter { it.providerId == providerId }

    /** The default model id for [providerId], or `null` if none is curated. */
    fun defaultModelId(providerId: String): String? =
        forProvider(providerId).firstOrNull()?.id

    /** Look up a single model by its [modelId]. */
    fun modelById(modelId: String): ChatModel? =
        all.firstOrNull { it.id == modelId }

    /** Look up a model by [modelId] for the given [providerId]. */
    fun option(modelId: String?, providerId: String?): ChatModel? {
        if (modelId == null) return null
        return forProvider(providerId ?: "openrouter").firstOrNull { it.id == modelId }
    }
}
