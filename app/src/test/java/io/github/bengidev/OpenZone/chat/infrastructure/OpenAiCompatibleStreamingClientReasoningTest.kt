package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.chat.domain.ChatMessageRole
import io.github.bengidev.openzone.chat.domain.ChatMessages
import io.github.bengidev.openzone.chat.domain.ChatRequest
import io.github.bengidev.openzone.shared.externals.preference.ComposerReasoningLevel
import io.github.bengidev.openzone.shared.externals.security.CredentialStore
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Wire-level tests for the reasoning effort parameter (issue #7):
 *
 * - [ComposerReasoningLevel.Off] omits the `reasoning` object from the request body.
 * - Low/Medium/High serialize as `"reasoning":{"effort":"..."}`.
 */
class OpenAiCompatibleStreamingClientReasoningTest {

    private lateinit var server: MockWebServer

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

    private fun client() = OpenAiCompatibleStreamingClient(
        credentialStore = FakeCredentialStore("sk-test")
    )

    private fun request(level: ComposerReasoningLevel) = ChatRequest(
        conversationId = "c1",
        messages = listOf(ChatMessages.text("m1", ChatMessageRole.USER, "Hi")),
        modelId = "nvidia/nemotron-3-ultra-550b-a55b:free",
        provider = provider(),
        reasoningLevel = level
    )

    private fun sseDone() = MockResponse()
        .setHeader("Content-Type", "text/event-stream")
        .setBody("data: [DONE]\n")

    @Test
    fun `Off omits the reasoning parameter from the request body`() = runTest {
        server.enqueue(sseDone())

        client().stream(request(ComposerReasoningLevel.Off)).toList()

        val recorded: RecordedRequest = server.takeRequest()
        val body = recorded.body.readUtf8()
        assertFalse("reasoning must not be present when Off", body.contains("\"reasoning\""))
    }

    @Test
    fun `High sends reasoning effort high on the wire`() = runTest {
        server.enqueue(sseDone())

        client().stream(request(ComposerReasoningLevel.High)).toList()

        val recorded: RecordedRequest = server.takeRequest()
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"reasoning\""))
        assertTrue(body.contains("\"effort\":\"high\""))
    }

    @Test
    fun `Low and Medium map to their effort strings`() = runTest {
        server.enqueue(sseDone())
        client().stream(request(ComposerReasoningLevel.Low)).toList()
        val low = server.takeRequest().body.readUtf8()
        assertTrue(low.contains("\"effort\":\"low\""))

        server.enqueue(sseDone())
        client().stream(request(ComposerReasoningLevel.Medium)).toList()
        val medium = server.takeRequest().body.readUtf8()
        assertTrue(medium.contains("\"effort\":\"medium\""))
    }
}
