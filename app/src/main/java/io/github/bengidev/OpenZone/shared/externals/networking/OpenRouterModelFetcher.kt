package io.github.bengidev.openzone.shared.externals.networking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Fetches the live model list from the OpenRouter `/models` endpoint and maps
 * it to [ChatModel] instances.
 *
 * Must be called from a background thread. Returns `null` on any network or
 * parse failure so the caller can fall back to the curated catalog gracefully.
 * No credentials are needed for the public models endpoint.
 *
 * Mirrors the iOS live-catalog fetch seam.
 */
class OpenRouterModelFetcher(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val json: Json = defaultJson
) : ModelCatalogFetcher {

    override fun fetchSync(providerId: String): List<ChatModel>? {
        if (providerId != PROVIDER_ID) return null
        val request = Request.Builder()
            .url(MODELS_URL)
            .header("Accept", "application/json")
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val envelope = json.decodeFromString<ModelsEnvelope>(body)
                envelope.data.mapNotNull { it.toChatModel() }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun WireModel.toChatModel(): ChatModel? {
        val modelId = id.takeIf { it.isNotBlank() } ?: return null
        val free = pricing?.prompt?.let { it == "0" || it == "0.0" } ?: false
        val reasoning = supportedParameters.any { it == "reasoning" || it == "thinking" }
        return ChatModel(
            id = modelId,
            displayName = name.ifBlank { modelId },
            providerId = PROVIDER_ID,
            isFree = free,
            contextLength = contextLength,
            supportsReasoning = reasoning,
            description = description.orEmpty().normalizeDescription()
        )
    }

    /** Collapses newlines/whitespace into single spaces; no length cap so
     *  [ChatModel.description] stays intact for future detail surfaces. */
    private fun String.normalizeDescription(): String =
        trim().replace(Regex("\\s+"), " ")

    // ---- Wire models -------------------------------------------------------

    @Serializable
    private data class ModelsEnvelope(val data: List<WireModel> = emptyList())

    @Serializable
    private data class WireModel(
        val id: String = "",
        val name: String = "",
        val description: String? = null,
        val pricing: WirePricing? = null,
        @SerialName("context_length") val contextLength: Int? = null,
        @SerialName("supported_parameters") val supportedParameters: List<String> = emptyList()
    )

    @Serializable
    private data class WirePricing(val prompt: String? = null)

    private companion object {
        const val PROVIDER_ID = "openrouter"
        const val MODELS_URL = "https://openrouter.ai/api/v1/models"

        val defaultJson = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}
