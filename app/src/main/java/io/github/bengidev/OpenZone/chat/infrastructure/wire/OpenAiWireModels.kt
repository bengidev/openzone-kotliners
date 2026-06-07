package io.github.bengidev.openzone.chat.infrastructure.wire

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenAI-compatible wire models for the chat-completions streaming endpoint.
 * Internal to chat infrastructure — not exposed to domain/application layers.
 *
 * Only the fields OpenZone consumes are modeled; unknown fields are ignored by
 * the configured `Json { ignoreUnknownKeys = true }` instance.
 */

@Serializable
internal data class ChatCompletionRequest(
    val model: String,
    val messages: List<WireMessage>,
    val stream: Boolean = true
)

@Serializable
internal data class WireMessage(
    val role: String,
    val content: String
)

/** One streamed chunk: `data: {"choices":[{"delta":{...}}]}`. */
@Serializable
internal data class ChatCompletionChunk(
    val choices: List<WireChoice> = emptyList()
)

@Serializable
internal data class WireChoice(
    val delta: WireDelta = WireDelta(),
    @SerialName("finish_reason") val finishReason: String? = null
)

/**
 * Delta payload. `content` is the answer text; `reasoning` (OpenRouter) and
 * `reasoning_content` (some OpenAI-compatible providers) carry chain-of-thought.
 */
@Serializable
internal data class WireDelta(
    val content: String? = null,
    val reasoning: String? = null,
    @SerialName("reasoning_content") val reasoningContent: String? = null
) {
    /** Reasoning text under whichever field the provider used. */
    val reasoningText: String?
        get() = reasoning ?: reasoningContent
}

/** Error envelope: `{"error":{"message":"...","type":"...","code":"..."}}`. */
@Serializable
internal data class WireErrorEnvelope(
    val error: WireError? = null
)

@Serializable
internal data class WireError(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null
)
