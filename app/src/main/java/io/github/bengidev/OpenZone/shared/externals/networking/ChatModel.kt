package io.github.bengidev.openzone.shared.externals.networking

import kotlinx.serialization.Serializable

/**
 * Value type for a selectable chat model. The canonical wire identity is [id]
 * — the string sent as `ChatRequest.modelId` and persisted in the preference
 * store. All other fields are presentation / routing metadata only.
 *
 * Replaces the old `ComposerModelOption` enum as model identity. Replaces the
 * Slice-3 `CatalogModel` as the settings-catalog element type — both the Home
 * and Settings features now share this single type via the `shared/networking`
 * seam, so a model selected in one surface is the same identity in the other.
 *
 * `@Serializable` so the live catalog can be cached as JSON in the preference
 * store (see [ModelCatalogStore]).
 *
 * Mirrors iOS `ChatModelOption`.
 *
 * @property id provider model identifier, e.g. `"meta-llama/llama-3.3-70b-instruct:free"`.
 * @property displayName human-facing label shown in the composer chip and popup.
 * @property providerId stable [ChatProvider.id] this model belongs to.
 * @property isFree whether the model is on the provider's free tier.
 * @property contextLength maximum token context window, or `null` if unknown.
 * @property supportsReasoning whether the model emits reasoning/thinking traces.
 * @property description short capability hint shown in the model picker.
 */
@Serializable
data class ChatModel(
    val id: String,
    val displayName: String,
    val providerId: String,
    val isFree: Boolean = false,
    val contextLength: Int? = null,
    val supportsReasoning: Boolean = false,
    val description: String = ""
)

/** Formats a token context window for display, e.g. `128K ctx`. */
fun formatContextLength(tokens: Int): String = when {
    tokens >= 1_000_000 -> "${tokens / 1_000_000}M ctx"
    tokens >= 1_000 -> "${tokens / 1_000}K ctx"
    else -> "$tokens ctx"
}
