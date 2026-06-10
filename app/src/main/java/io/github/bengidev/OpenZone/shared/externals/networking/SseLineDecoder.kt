package io.github.bengidev.openzone.shared.externals.networking

/**
 * Generic, transport-agnostic Server-Sent Events (SSE) line decoder.
 *
 * Feeds raw bytes/strings (as they arrive from the network, possibly split
 * mid-line across chunk boundaries) and emits the **data payloads** of complete
 * SSE events. Feature-neutral: it knows nothing about chat, JSON, or providers.
 *
 * Per the SSE spec and OpenAI-style streaming:
 * - lines are separated by `\n` (a trailing `\r` is stripped, so `\r\n` works);
 * - a line beginning with `:` is a comment / keep-alive and is skipped;
 * - a line beginning with `data:` carries a payload (one optional leading
 *   space after the colon is removed);
 * - the sentinel payload `[DONE]` signals end-of-stream and is reported via
 *   [SseEvent.Done] rather than as data;
 * - any other field (`event:`, `id:`, `retry:`) is ignored for this use case.
 *
 * This decoder is **stateful**: partial lines are buffered between [decode]
 * calls so a line split across two network chunks is reassembled correctly.
 * It is not thread-safe; drive it from a single coroutine.
 */
class SseLineDecoder(private val doneSentinel: String = "[DONE]") {

    private val buffer = StringBuilder()

    /** One decoded SSE outcome. */
    sealed interface SseEvent {
        /** A `data:` payload (sentinel already filtered out). */
        data class Data(val payload: String) : SseEvent

        /** The done sentinel was seen; callers should stop reading. */
        data object Done : SseEvent
    }

    /**
     * Feed the next chunk of decoded text. Returns the events completed by this
     * chunk, in order. Bytes after the last newline are retained for the next
     * call. Once [SseEvent.Done] is emitted, further data lines in the same
     * chunk are not returned.
     */
    fun decode(chunk: String): List<SseEvent> {
        if (chunk.isEmpty()) return emptyList()
        buffer.append(chunk)
        val events = mutableListOf<SseEvent>()

        var newlineIndex = buffer.indexOf("\n")
        while (newlineIndex >= 0) {
            val rawLine = buffer.substring(0, newlineIndex)
            buffer.delete(0, newlineIndex + 1)

            val event = parseLine(rawLine)
            if (event != null) {
                events += event
                if (event is SseEvent.Done) {
                    buffer.setLength(0)
                    return events
                }
            }
            newlineIndex = buffer.indexOf("\n")
        }
        return events
    }

    /**
     * Signal end of the byte stream. If the final line arrived without a
     * trailing newline, parse whatever remains in the buffer.
     */
    fun flush(): List<SseEvent> {
        if (buffer.isEmpty()) return emptyList()
        val rawLine = buffer.toString()
        buffer.setLength(0)
        val event = parseLine(rawLine)
        return if (event != null) listOf(event) else emptyList()
    }

    /** Reset all buffered state for reuse. */
    fun reset() {
        buffer.setLength(0)
    }

    private fun parseLine(rawLine: String): SseEvent? {
        // Normalize CRLF and trim trailing CR.
        val line = rawLine.removeSuffix("\r")

        // Blank line = event dispatch boundary; nothing buffered to emit here.
        if (line.isEmpty()) return null

        // Comment / keep-alive.
        if (line.startsWith(":")) return null

        // Only the data field carries a payload for our purposes.
        if (!line.startsWith("data:")) return null

        // Strip "data:" and an optional single leading space.
        val payload = line.removePrefix("data:").let {
            if (it.startsWith(" ")) it.substring(1) else it
        }

        if (payload == doneSentinel) return SseEvent.Done
        return SseEvent.Data(payload)
    }
}
