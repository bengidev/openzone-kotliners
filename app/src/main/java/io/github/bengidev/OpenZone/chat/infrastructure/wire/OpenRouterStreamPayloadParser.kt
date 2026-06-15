package io.github.bengidev.openzone.chat.infrastructure.wire

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray

/**
 * Lenient extractor for OpenRouter / Qwen3 streaming `data:` payloads.
 *
 * OpenRouter chunks are not always strict OpenAI shapes: `choices` or `delta`
 * may be null, `reasoning` may be null, and Qwen3-family models stream thinking
 * via `reasoning_details` (and sometimes `reasoning_text`) rather than only
 * `reasoning` / `reasoning_content`. Strict kotlinx decoding of a typed model
 * drops the entire chunk when any one field has an unexpected shape.
 */
internal object OpenRouterStreamPayloadParser {

    data class ParsedChunk(
        val thinkingDeltas: List<String> = emptyList(),
        val textDeltas: List<String> = emptyList(),
        val errorMessage: String? = null
    ) {
        val isEmpty: Boolean
            get() = errorMessage == null && thinkingDeltas.isEmpty() && textDeltas.isEmpty()
    }

    fun parse(payload: String, json: Json): ParsedChunk {
        val root =
                runCatching { json.parseToJsonElement(payload) }
                        .getOrNull()
                        ?.takeIf { it !is JsonNull }
                        ?: return ParsedChunk()

        if (root !is JsonObject) return ParsedChunk()

        extractErrorMessage(root)?.let { message ->
            return ParsedChunk(errorMessage = message)
        }

        val thinking = mutableListOf<String>()
        val text = mutableListOf<String>()

        root["delta"]?.let { delta ->
            if (delta is JsonObject) parseDelta(delta, thinking, text)
        }

        when (val choices = root["choices"]) {
            is JsonArray -> {
                for (choiceElement in choices) {
                    if (choiceElement !is JsonObject) continue
                    extractErrorMessage(choiceElement)?.let { message ->
                        return ParsedChunk(errorMessage = message)
                    }
                    parseChoice(choiceElement, thinking, text)
                }
            }
            is JsonObject -> {
                extractErrorMessage(choices)?.let { message ->
                    return ParsedChunk(errorMessage = message)
                }
                parseChoice(choices, thinking, text)
            }
            else -> Unit
        }

        return ParsedChunk(
                thinkingDeltas = thinking.filter { it.trim().isNotBlank() },
                textDeltas = text.filter { it.isNotEmpty() }
        )
    }

    private fun parseChoice(
        choice: JsonObject,
        thinking: MutableList<String>,
        text: MutableList<String>
    ) {
        choice["delta"]?.let { delta ->
            if (delta is JsonObject) parseDelta(delta, thinking, text)
        }
        choice["message"]?.let { message ->
            if (message is JsonObject) parseMessage(message, thinking, text)
        }
    }

    private fun parseDelta(
        delta: JsonObject,
        thinking: MutableList<String>,
        text: MutableList<String>
    ) {
        val fromDetails = extractThinkingFromDetails(delta)
        if (fromDetails != null) {
            thinking += fromDetails
        } else {
            extractThinkingFromStringFields(delta)?.let { thinking += it }
        }
        extractText(delta["content"])?.let { text += it }
        extractText(delta["text"])?.let { text += it }
    }

    private fun parseMessage(
        message: JsonObject,
        thinking: MutableList<String>,
        text: MutableList<String>
    ) {
        val fromDetails = extractThinkingFromDetails(message)
        if (fromDetails != null) {
            thinking += fromDetails
        } else {
            extractThinkingFromStringFields(message)?.let { thinking += it }
        }
        extractText(message["content"])?.let { text += it }
        extractText(message["text"])?.let { text += it }
    }

    private fun extractErrorMessage(container: JsonObject): String? {
        container["error"]?.let { error ->
            if (error is JsonObject) {
                error.stringField("message")?.takeIf { it.isNotBlank() }?.let { return it }
            }
        }

        val finishReason = container.stringField("finish_reason")
        if (finishReason == "error" || finishReason == "content_filter") {
            container.stringField("native_finish_reason")?.takeIf { it.isNotBlank() }?.let {
                return it
            }
            return when (finishReason) {
                "content_filter" -> "The model blocked this response."
                else -> "The model provider returned an error."
            }
        }

        return null
    }

    private fun extractThinkingFromDetails(container: JsonObject): String? {
        val joined =
                container["reasoning_details"]
                        ?.jsonArray
                        ?.mapNotNull { detail ->
                            if (detail !is JsonObject) return@mapNotNull null
                            when (detail.stringField("type")) {
                                "reasoning.text" -> detail.stringField("text")
                                "reasoning.summary" -> detail.stringField("summary")
                                else ->
                                        detail.stringField("text")
                                                ?: detail.stringField("summary")
                            }
                        }
                        ?.filter { it.isNotBlank() }
                        ?.joinToString("")
        return joined?.takeIf { it.isNotBlank() }
    }

    private fun extractThinkingFromStringFields(container: JsonObject): String? {
        for (field in REASONING_FIELDS) {
            container.stringField(field)?.takeIf { it.isNotBlank() }?.let { return it }
        }
        return null
    }

    private fun extractText(element: JsonElement?): String? {
        if (element == null || element is JsonNull) return null
        if (element is JsonPrimitive) {
            return element.contentOrNull?.takeIf { it.isNotEmpty() }
        }
        if (element is JsonObject) {
            return element.stringField("text")
                    ?: element.stringField("content")
                    ?: element.stringField("output_text")
        }
        if (element is JsonArray) {
            val joined =
                    element.mapNotNull { item -> extractText(item) }
                            .filter { it.isNotBlank() }
                            .joinToString("")
            return joined.takeIf { it.isNotEmpty() }
        }
        return null
    }

    private fun JsonObject.stringField(vararg path: String): String? {
        var current: JsonElement = this
        for (key in path) {
            val obj = current as? JsonObject ?: return null
            current = obj[key] ?: return null
        }
        return (current as? JsonPrimitive)?.contentOrNull
    }

    private val REASONING_FIELDS =
            listOf("reasoning_content", "reasoning", "reasoning_text")
}
