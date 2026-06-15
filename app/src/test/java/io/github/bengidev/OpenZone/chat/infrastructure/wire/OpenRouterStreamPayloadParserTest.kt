package io.github.bengidev.openzone.chat.infrastructure.wire

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenRouterStreamPayloadParserTest {

    private val json =
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                coerceInputValues = true
                isLenient = true
            }

    @Test
    fun `parses qwen3 reasoning_details chunks`() {
        val payload =
                """
                {
                  "id": "gen-1",
                  "choices": [{
                    "index": 0,
                    "delta": {
                      "reasoning_details": [
                        { "type": "reasoning.text", "text": "I need to think." },
                        { "type": "reasoning.text", "text": " Let me analyze." }
                      ]
                    },
                    "logprobs": null,
                    "finish_reason": null
                  }]
                }
                """
                    .trimIndent()

        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)

        assertEquals(listOf("I need to think. Let me analyze."), parsed.thinkingDeltas)
        assertTrue(parsed.textDeltas.isEmpty())
    }

    @Test
    fun `parses null reasoning and empty reasoning_details with content`() {
        val payload =
                """
                {
                  "choices": [{
                    "index": 0,
                    "delta": {
                      "role": "assistant",
                      "content": "Hello!",
                      "reasoning": null,
                      "reasoning_details": []
                    }
                  }]
                }
                """
                    .trimIndent()

        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)

        assertEquals(listOf("Hello!"), parsed.textDeltas)
        assertTrue(parsed.thinkingDeltas.isEmpty())
    }

    @Test
    fun `survives null delta and null choices on other chunks`() {
        val payload =
                """
                {
                  "choices": [{
                    "index": 0,
                    "delta": null,
                    "finish_reason": "stop"
                  }]
                }
                """
                    .trimIndent()

        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)
        assertTrue(parsed.isEmpty)
    }

    @Test
    fun `parses reasoning_text field`() {
        val payload =
                """
                {
                  "choices": [{
                    "delta": { "reasoning_text": "thinking aloud" }
                  }]
                }
                """
                    .trimIndent()

        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)
        assertEquals(listOf("thinking aloud"), parsed.thinkingDeltas)
    }

    @Test
    fun `parses structured content array`() {
        val payload =
                """
                {
                  "choices": [{
                    "delta": {
                      "content": [
                        { "type": "text", "text": "Hello" },
                        { "type": "text", "text": " there" }
                      ]
                    }
                  }]
                }
                """
                    .trimIndent()

        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)
        assertEquals(listOf("Hello there"), parsed.textDeltas)
    }

    @Test
    fun `parses metadata-only reasoning detail without text`() {
        val payload =
                """
                {
                  "choices": [{
                    "delta": {
                      "reasoning_details": [
                        { "type": "reasoning.text", "format": "unknown", "index": 0 }
                      ]
                    }
                  }]
                }
                """
                    .trimIndent()

        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)
        assertTrue(parsed.thinkingDeltas.isEmpty())
    }

    @Test
    fun `extracts top-level stream error`() {
        val payload =
                """{"error":{"message":"Provider disconnected unexpectedly"},"choices":[{"delta":{"content":""},"finish_reason":"error"}]}"""

        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)
        assertEquals("Provider disconnected unexpectedly", parsed.errorMessage)
    }

    @Test
    fun `parses structured content object`() {
        val payload =
                """
                {
                  "choices": [{
                    "delta": {
                      "content": { "type": "text", "text": "Hello object" }
                    }
                  }]
                }
                """
                    .trimIndent()

        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)
        assertEquals(listOf("Hello object"), parsed.textDeltas)
    }

    @Test
    fun `parses single-object choices envelope`() {
        val payload =
                """
                {
                  "choices": {
                    "delta": { "content": "Hello single choice" }
                  }
                }
                """
                    .trimIndent()

        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)
        assertEquals(listOf("Hello single choice"), parsed.textDeltas)
    }

    @Test
    fun `dedupes string reasoning when reasoning_details already present`() {
        val payload =
                """
                {
                  "choices": [{
                    "delta": {
                      "reasoning_details": [
                        { "type": "reasoning.text", "text": "Thinking" }
                      ],
                      "reasoning": "duplicate"
                    }
                  }]
                }
                """
                    .trimIndent()

        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)
        assertEquals(listOf("Thinking"), parsed.thinkingDeltas)
    }

    @Test
    fun `extracts choice finish_reason error`() {
        val payload =
                """
                {
                  "choices": [{
                    "finish_reason": "error",
                    "native_finish_reason": "Provider overloaded"
                  }]
                }
                """
                    .trimIndent()

        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)
        assertEquals("Provider overloaded", parsed.errorMessage)
    }

    @Test
    fun `ignores invalid json without throwing`() {
        val parsed = OpenRouterStreamPayloadParser.parse("not-json", json)
        assertTrue(parsed.isEmpty)
        assertNull(parsed.errorMessage)
    }
}
