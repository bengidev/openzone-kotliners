package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.chat.domain.ChatMessageRole
import io.github.bengidev.openzone.chat.domain.ChatMessages
import io.github.bengidev.openzone.chat.domain.ChatRequest
import io.github.bengidev.openzone.chat.domain.ChatStreamingEvent
import io.github.bengidev.openzone.shared.externals.security.CredentialStore
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OpenAiCompatibleStreamingClientTest {

    private lateinit var server: MockWebServer

    /** In-memory credential store; key read at call time. */
    private class FakeCredentialStore(var secret: String?) : CredentialStore {
        override fun secretFor(providerId: String): String? = secret
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun provider() =
        ChatProviders.openRouter.copy(baseUrl = server.url("/v1").toString())

    private fun client(secret: String? = "sk-test"): OpenAiCompatibleStreamingClient {
        return OpenAiCompatibleStreamingClient(
            credentialStore = FakeCredentialStore(secret)
        )
    }

    private fun request() = ChatRequest(
        conversationId = "c1",
        messages = listOf(ChatMessages.text("m1", ChatMessageRole.USER, "Hi")),
        modelId = "deepseek/deepseek-r1:free",
        provider = provider()
    )

    private fun sse(body: String) = MockResponse()
        .setHeader("Content-Type", "text/event-stream")
        .setBody(body)

    private fun texts(events: List<ChatStreamingEvent>) =
        events.filterIsInstance<ChatStreamingEvent.TextDelta>().joinToString("") { it.delta }

    private fun thinking(events: List<ChatStreamingEvent>) =
        events.filterIsInstance<ChatStreamingEvent.ThinkingDelta>().joinToString("") { it.delta }

    @Test
    fun `maps content deltas to text events and terminates on done`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"choices":[{"delta":{"content":"Hel"}}]}
                data: {"choices":[{"delta":{"content":"lo"}}]}
                data: [DONE]
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()

        assertEquals("Hello", texts(events))
        assertTrue(events.last() is ChatStreamingEvent.Done)
    }

    @Test
    fun `maps reasoning deltas to thinking events`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"choices":[{"delta":{"reasoning":"Let me think"}}]}
                data: {"choices":[{"delta":{"content":"Answer"}}]}
                data: [DONE]
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()

        assertEquals("Let me think", thinking(events))
        assertEquals("Answer", texts(events))
    }

    @Test
    fun `maps reasoning_content field to thinking events`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"choices":[{"delta":{"reasoning_content":"hmm"}}]}
                data: [DONE]
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()
        assertEquals("hmm", thinking(events))
    }

    @Test
    fun `skips comment keep-alive lines`() = runTest {
        server.enqueue(
            sse(
                ": OPENROUTER PROCESSING\n" +
                    "data: {\"choices\":[{\"delta\":{\"content\":\"ok\"}}]}\n" +
                    "data: [DONE]\n"
            )
        )

        val events = client().stream(request()).toList()
        assertEquals("ok", texts(events))
    }

    @Test
    fun `maps http 401 to a single error event`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("Unauthorized"))

        val events = client().stream(request()).toList()

        assertEquals(1, events.size)
        val err = events.single() as ChatStreamingEvent.Error
        assertTrue(err.error.message.contains("401"))
        assertTrue(err.error.message.contains("API key"))
    }

    @Test
    fun `maps http 403 with provider body to upgrade message`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(403)
                .setBody(
                    """{"error":{"message":"Your Go plan doesn't include API access. Upgrade to Provider or higher.","code":"upgrade_required"}}"""
                )
        )

        val events = client().stream(request()).toList()
        val err = events.single() as ChatStreamingEvent.Error
        assertTrue(err.error.message.contains("403"))
        assertTrue(err.error.message.contains("Go plan"))
    }

    @Test
    fun `maps mid-stream error object to error event`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"choices":[{"delta":{"content":"partial"}}]}
                data: {"error":{"message":"upstream exploded"}}
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()

        assertEquals("partial", texts(events))
        val err = events.last() as ChatStreamingEvent.Error
        assertTrue(err.error.message.contains("upstream exploded"))
    }

    @Test
    fun `maps provider error envelope to error event message`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"error":{"message":"Invalid model","type":"invalid_request_error"}}""")
        )

        val events = client().stream(request()).toList()
        val err = events.single() as ChatStreamingEvent.Error
        assertTrue(err.error.message.contains("Invalid model"))
    }

    @Test
    fun `missing credential short-circuits with error and no network call`() = runTest {
        val events = client(secret = null).stream(request()).toList()

        val err = events.single() as ChatStreamingEvent.Error
        assertTrue(err.error.message.contains("API key"))
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `sends bearer auth and attribution headers`() = runTest {
        server.enqueue(sse("data: [DONE]\n"))

        client().stream(request()).toList()

        val recorded: RecordedRequest = server.takeRequest()
        assertEquals("Bearer sk-test", recorded.getHeader("Authorization"))
        assertEquals("OpenZone", recorded.getHeader("X-Title"))
        assertNull(null) // sanity
    }

    @Test
    fun `unparseable data lines are skipped without crashing`() = runTest {
        server.enqueue(
            sse(
                "data: not-json\n" +
                    "data: {\"choices\":[{\"delta\":{\"content\":\"safe\"}}]}\n" +
                    "data: [DONE]\n"
            )
        )

        val events = client().stream(request()).toList()
        assertEquals("safe", texts(events))
    }

    @Test
    fun `maps reasoning_details deltas to thinking events`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"choices":[{"delta":{"reasoning_details":[{"type":"reasoning.text","text":"Let me think"}]}}]}
                data: {"choices":[{"delta":{"reasoning_details":[{"type":"reasoning.text","text":" step by step"}]}}]}
                data: {"choices":[{"delta":{"content":"Hello!"}}]}
                data: [DONE]
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()

        assertEquals("Let me think step by step", thinking(events))
        assertEquals("Hello!", texts(events))
    }

    @Test
    fun `maps reasoning_details summary type to thinking events`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"choices":[{"delta":{"reasoning_details":[{"type":"reasoning.summary","summary":"Planning reply"}]}}]}
                data: {"choices":[{"delta":{"content":"Hi there"}}]}
                data: [DONE]
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()

        assertEquals("Planning reply", thinking(events))
        assertEquals("Hi there", texts(events))
    }

    @Test
    fun `maps message content fallback on final chunk`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"choices":[{"message":{"role":"assistant","content":"Hello from message field"}}]}
                data: [DONE]
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()
        assertEquals("Hello from message field", texts(events))
    }

    @Test
    fun `maps openrouter qwen3 reasoning_details stream to thinking and text`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"id":"gen-1","choices":[{"index":0,"delta":{"reasoning_details":[{"type":"reasoning.text","text":"Thinking"}]},"finish_reason":null}]}
                data: {"id":"gen-1","choices":[{"index":0,"delta":{"role":"assistant","content":"Hello!","reasoning":null,"reasoning_details":[]},"finish_reason":null}]}
                data: [DONE]
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()

        assertEquals("Thinking", thinking(events))
        assertEquals("Hello!", texts(events))
        assertTrue(events.last() is ChatStreamingEvent.Done)
    }

    @Test
    fun `maps reasoning_text field to thinking events`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"choices":[{"delta":{"reasoning_text":"hmm"}}]}
                data: {"choices":[{"delta":{"content":"Answer"}}]}
                data: [DONE]
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()
        assertEquals("hmm", thinking(events))
        assertEquals("Answer", texts(events))
    }

    @Test
    fun `skips null delta chunks without aborting stream`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"choices":[{"index":0,"delta":{"content":"Hi"}}]}
                data: {"choices":[{"index":0,"delta":null,"finish_reason":"stop"}]}
                data: [DONE]
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()
        assertEquals("Hi", texts(events))
    }

    @Test
    fun `maps ndjson lines without data prefix`() = runTest {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "text/event-stream")
                .setBody(
                    """
                    {"choices":[{"delta":{"content":"Hello"}}]}
                    {"choices":[{"delta":{"content":"!"}}]}
                    data: [DONE]
                    """.trimIndent() + "\n"
                )
        )

        val events = client().stream(request()).toList()
        assertEquals("Hello!", texts(events))
    }

    @Test
    fun `maps stream finish_reason error to error event`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"choices":[{"finish_reason":"error","native_finish_reason":"Provider overloaded"}]}
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()
        val err = events.last() as ChatStreamingEvent.Error
        assertTrue(err.error.message.contains("Provider overloaded"))
    }

    @Test
    fun `whitespace-only reasoning deltas are filtered out`() = runTest {
        server.enqueue(
            sse(
                """
                data: {"choices":[{"delta":{"reasoning":"   "}}]}
                data: {"choices":[{"delta":{"content":"Answer"}}]}
                data: [DONE]
                """.trimIndent() + "\n"
            )
        )

        val events = client().stream(request()).toList()

        assertTrue(events.filterIsInstance<ChatStreamingEvent.ThinkingDelta>().isEmpty())
        assertEquals("Answer", texts(events))
    }
}
