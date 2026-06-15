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
    val stream: Boolean = true,
    /**
     * OpenRouter / OpenAI `reasoning` parameter. `null` means the field is
     * omitted entirely from the serialized JSON (via `explicitNulls = false`
     * on the shared [Json] instance) so non-reasoning models are unaffected.
     */
    val reasoning: WireReasoning? = null
)

/**
 * Reasoning effort hint sent to providers that support chain-of-thought.
 * Serializes as `{"effort": "low"|"medium"|"high"}`.
 * Mirrors the OpenRouter `reasoning` request parameter.
 */
@Serializable
internal data class WireReasoning(
    val effort: String
)

@Serializable
internal data class WireMessage(
    val role: String,
    val content: String
)

/** One streamed chunk: `data: {"choices":[{"delta":{...}}]}`. */
@Serializable
internal data class ChatCompletionChunk(
    val choices: List<WireChoice> = emptyList(),
    val error: WireError? = null
)

@Serializable
internal data class WireChoice(
    val delta: WireDelta = WireDelta(),
    val message: WireMessage? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

/** One item in OpenRouter's `reasoning_details` array (streaming + final). */
@Serializable
internal data class WireReasoningDetail(
    val type: String? = null,
    val text: String? = null,
    val summary: String? = null
) {
    /** Extracts human-readable reasoning text from a detail object. */
    val thinkingText: String?
        get() =
                when (type) {
                    "reasoning.text" -> text
                    "reasoning.summary" -> summary
                    else -> text ?: summary
                }
}

/**
 * Delta payload. `content` is the answer text; `reasoning` (OpenRouter) and
 * `reasoning_content` (some OpenAI-compatible providers) carry chain-of-thought.
 * Newer OpenRouter reasoning models also stream via `reasoning_details`.
 */
@Serializable
internal data class WireDelta(
    val content: String? = null,
    val reasoning: String? = null,
    @SerialName("reasoning_content") val reasoningContent: String? = null,
    @SerialName("reasoning_details") val reasoningDetails: List<WireReasoningDetail>? = null
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
