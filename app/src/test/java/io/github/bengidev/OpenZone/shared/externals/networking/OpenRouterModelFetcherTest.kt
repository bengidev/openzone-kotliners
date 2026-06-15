package io.github.bengidev.openzone.shared.externals.networking

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OpenRouterModelFetcherTest {

    private lateinit var server: MockWebServer
    private lateinit var fetcher: OpenRouterModelFetcher

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        fetcher = OpenRouterModelFetcher(
            httpClient = OkHttpClient(),
            modelsUrl = server.url("/models").toString()
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `model is free only when both prompt and completion pricing are zero`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {"data": [
                        {"id": "test/free-model", "name": "Free Model",
                         "pricing": {"prompt": "0", "completion": "0"}},
                        {"id": "test/partially-free", "name": "Partially Free",
                         "pricing": {"prompt": "0", "completion": "0.000001"}},
                        {"id": "test/paid-model", "name": "Paid Model",
                         "pricing": {"prompt": "0.000005", "completion": "0.00001"}}
                    ]}
                    """.trimIndent()
                )
        )

        val models = fetcher.fetchSync("openrouter")!!

        val free = models.first { it.id == "test/free-model" }
        val partial = models.first { it.id == "test/partially-free" }
        val paid = models.first { it.id == "test/paid-model" }

        assertTrue(free.isFree)
        assertFalse(partial.isFree)
        assertFalse(paid.isFree)
    }
}
