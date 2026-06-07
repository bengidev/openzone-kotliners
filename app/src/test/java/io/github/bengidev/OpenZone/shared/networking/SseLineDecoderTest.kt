package io.github.bengidev.openzone.shared.networking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SseLineDecoderTest {

    private fun data(events: List<SseLineDecoder.SseEvent>): List<String> =
        events.filterIsInstance<SseLineDecoder.SseEvent.Data>().map { it.payload }

    @Test
    fun `parses a single complete data line`() {
        val decoder = SseLineDecoder()
        val events = decoder.decode("data: hello\n")
        assertEquals(listOf("hello"), data(events))
    }

    @Test
    fun `buffers a line split across two chunks`() {
        val decoder = SseLineDecoder()
        val first = decoder.decode("data: hel")
        assertTrue("no event until newline arrives", first.isEmpty())

        val second = decoder.decode("lo world\n")
        assertEquals(listOf("hello world"), data(second))
    }

    @Test
    fun `reassembles payload split mid-token across three chunks`() {
        val decoder = SseLineDecoder()
        assertTrue(decoder.decode("da").isEmpty())
        assertTrue(decoder.decode("ta: par").isEmpty())
        val events = decoder.decode("tial\n")
        assertEquals(listOf("partial"), data(events))
    }

    @Test
    fun `skips comment and keep-alive lines`() {
        val decoder = SseLineDecoder()
        val events = decoder.decode(": OPENROUTER PROCESSING\ndata: real\n")
        assertEquals(listOf("real"), data(events))
    }

    @Test
    fun `handles CRLF line endings`() {
        val decoder = SseLineDecoder()
        val events = decoder.decode("data: windows\r\n")
        assertEquals(listOf("windows"), data(events))
    }

    @Test
    fun `terminates on done sentinel and emits Done`() {
        val decoder = SseLineDecoder()
        val events = decoder.decode("data: one\ndata: [DONE]\ndata: ignored\n")
        assertEquals(listOf("one"), data(events))
        assertTrue(events.any { it is SseLineDecoder.SseEvent.Done })
        // The data line after [DONE] in the same chunk must not be emitted.
        assertEquals(1, data(events).size)
    }

    @Test
    fun `multiple events in one chunk preserve order`() {
        val decoder = SseLineDecoder()
        val events = decoder.decode("data: a\ndata: b\ndata: c\n")
        assertEquals(listOf("a", "b", "c"), data(events))
    }

    @Test
    fun `blank lines between events are ignored`() {
        val decoder = SseLineDecoder()
        val events = decoder.decode("data: a\n\ndata: b\n\n")
        assertEquals(listOf("a", "b"), data(events))
    }

    @Test
    fun `flush parses a trailing line without newline`() {
        val decoder = SseLineDecoder()
        assertTrue(decoder.decode("data: tail").isEmpty())
        val flushed = decoder.flush()
        assertEquals(listOf("tail"), data(flushed))
    }

    @Test
    fun `preserves JSON payloads containing colons and spaces`() {
        val decoder = SseLineDecoder()
        val payload = """{"choices":[{"delta":{"content":"hi: there"}}]}"""
        val events = decoder.decode("data: $payload\n")
        assertEquals(listOf(payload), data(events))
    }
}
