package io.github.bengidev.openzone.settings.domain

/**
 * Curated fallback catalog of free models, used when a live model list is
 * unavailable (offline, no key yet, or provider list fetch out of scope for
 * this slice). Mirrors the iOS curated free-model fallback.
 *
 * All entries are OpenRouter free-tier model identifiers (the `:free` suffix is
 * part of OpenRouter's id scheme). Kept deliberately small and stable; a live
 * catalog fetch can layer on top in a later slice without changing this seam.
 */
object ModelCatalog {

    /** OpenRouter free-tier fallback models (provider id `"openrouter"`). */
    val openRouterFree: List<CatalogModel> = listOf(
        CatalogModel(
            id = "deepseek/deepseek-chat-v3-0324:free",
            displayName = "DeepSeek V3 0324",
            providerId = "openrouter",
            description = "General-purpose chat, strong reasoning"
        ),
        CatalogModel(
            id = "deepseek/deepseek-r1:free",
            displayName = "DeepSeek R1",
            providerId = "openrouter",
            description = "Reasoning-tuned, emits thinking traces"
        ),
        CatalogModel(
            id = "meta-llama/llama-3.3-70b-instruct:free",
            displayName = "Llama 3.3 70B Instruct",
            providerId = "openrouter",
            description = "Meta instruct model, broad knowledge"
        ),
        CatalogModel(
            id = "google/gemini-2.0-flash-exp:free",
            displayName = "Gemini 2.0 Flash (exp)",
            providerId = "openrouter",
            description = "Fast, multimodal-capable"
        ),
        CatalogModel(
            id = "qwen/qwen-2.5-72b-instruct:free",
            displayName = "Qwen 2.5 72B Instruct",
            providerId = "openrouter",
            description = "Multilingual, strong coding"
        ),
        CatalogModel(
            id = "mistralai/mistral-small-3.1-24b-instruct:free",
            displayName = "Mistral Small 3.1 24B",
            providerId = "openrouter",
            description = "Lightweight, low-latency"
        )
    )

    /** All curated models across providers, keyed lookups derive from this. */
    val all: List<CatalogModel> = openRouterFree

    /** Curated models for [providerId], in display order. */
    fun forProvider(providerId: String): List<CatalogModel> =
        all.filter { it.providerId == providerId }

    /** The default model id for [providerId], or `null` if none is curated. */
    fun defaultModelId(providerId: String): String? =
        forProvider(providerId).firstOrNull()?.id

    /** Look up a single catalog model by its [modelId]. */
    fun modelById(modelId: String): CatalogModel? =
        all.firstOrNull { it.id == modelId }
}
