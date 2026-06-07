package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatMessageRole
import io.github.bengidev.openzone.chat.domain.ChatRequest
import io.github.bengidev.openzone.chat.domain.ChatStreamError
import io.github.bengidev.openzone.chat.domain.ChatStreamingEvent
import io.github.bengidev.openzone.chat.infrastructure.wire.ChatCompletionChunk
import io.github.bengidev.openzone.chat.infrastructure.wire.ChatCompletionRequest
import io.github.bengidev.openzone.chat.infrastructure.wire.WireErrorEnvelope
import io.github.bengidev.openzone.chat.infrastructure.wire.WireMessage
import io.github.bengidev.openzone.shared.networking.AuthScheme
import io.github.bengidev.openzone.shared.networking.ChatProvider
import io.github.bengidev.openzone.shared.networking.CredentialStore
import io.github.bengidev.openzone.shared.networking.SseLineDecoder
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.coroutineContext

/**
 * OpenAI-compatible streaming chat client. Implements the existing
 * [ChatAPIClient] seam by issuing a `stream:true` chat-completions request to a
 * [ChatProvider] and translating SSE frames into [ChatStreamingEvent]s:
 *
 * - `delta.content`             → [ChatStreamingEvent.TextDelta]
 * - `delta.reasoning(_content)` → [ChatStreamingEvent.ThinkingDelta]
 * - `[DONE]` sentinel           → [ChatStreamingEvent.Done]
 * - HTTP / network / parse fail → [ChatStreamingEvent.Error]
 *
 * Secrets are read from [credentialStore] at call time (never captured at
 * construction), so a key entered after the client is built is honored on the
 * next request. The target provider is read from each [ChatRequest.provider],
 * making a single instance provider-agnostic. Mirrors the iOS OpenAI-compatible
 * client.
 */
class OpenAiCompatibleStreamingClient(
    private val credentialStore: CredentialStore,
    private val callFactory: Call.Factory = OkHttpClient(),
    private val json: Json = defaultJson
) : ChatAPIClient {

    override fun stream(request: ChatRequest): Flow<ChatStreamingEvent> = flow {
        val provider = request.provider

        // Resolve credential at call time.
        val secret = credentialStore.secretFor(provider.id)
        if (provider.authScheme is AuthScheme.Bearer && secret.isNullOrBlank()) {
            emit(
                ChatStreamingEvent.Error(
                    ChatStreamError("No API key configured for ${provider.displayName}.")
                )
            )
            return@flow
        }

        val httpRequest = buildRequest(request, secret)
        val call = callFactory.newCall(httpRequest)

        val response = try {
            call.execute()
        } catch (io: IOException) {
            emit(ChatStreamingEvent.Error(ChatStreamError(io.message ?: "Network error.")))
            return@flow
        }

        response.use { resp ->
            val body = resp.body
            if (!resp.isSuccessful) {
                val raw = body?.string().orEmpty()
                emit(ChatStreamingEvent.Error(ChatStreamError(httpErrorMessage(resp.code, raw))))
                return@flow
            }
            if (body == null) {
                emit(ChatStreamingEvent.Error(ChatStreamError("Empty response body.")))
                return@flow
            }

            val decoder = SseLineDecoder()
            val source = body.source()
            try {
                while (!source.exhausted()) {
                    coroutineContext.ensureActive()
                    val line = source.readUtf8Line() ?: break
                    // Re-add newline so the decoder's line-buffering contract holds
                    // even though okio already split on the newline for us.
                    for (event in decoder.decode(line + "\n")) {
                        when (event) {
                            is SseLineDecoder.SseEvent.Data ->
                                emitDelta(event.payload)
                            SseLineDecoder.SseEvent.Done -> {
                                emit(ChatStreamingEvent.Done)
                                return@use
                            }
                        }
                    }
                }
                // Stream ended without an explicit [DONE]; treat as a clean finish.
                emit(ChatStreamingEvent.Done)
            } catch (io: IOException) {
                emit(ChatStreamingEvent.Error(ChatStreamError(io.message ?: "Stream read error.")))
            }
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<ChatStreamingEvent>.emitDelta(
        payload: String
    ) {
        val chunk = runCatching { json.decodeFromString<ChatCompletionChunk>(payload) }.getOrNull()
            ?: return
        val delta = chunk.choices.firstOrNull()?.delta ?: return

        delta.reasoningText?.takeIf { it.isNotEmpty() }?.let {
            emit(ChatStreamingEvent.ThinkingDelta(it))
        }
        delta.content?.takeIf { it.isNotEmpty() }?.let {
            emit(ChatStreamingEvent.TextDelta(it))
        }
    }

    private fun buildRequest(request: ChatRequest, secret: String?): Request {
        val provider = request.provider
        val payload = ChatCompletionRequest(
            model = request.modelId,
            messages = request.messages.toWireMessages(),
            stream = true
        )
        val bodyJson = json.encodeToString(ChatCompletionRequest.serializer(), payload)
        val builder = Request.Builder()
            .url(provider.chatCompletionsUrl)
            .post(bodyJson.toRequestBody(JSON_MEDIA_TYPE))
            .header("Accept", "text/event-stream")

        provider.defaultHeaders.forEach { (k, v) -> builder.header(k, v) }

        if (provider.authScheme is AuthScheme.Bearer && !secret.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $secret")
        }
        return builder.build()
    }

    private fun httpErrorMessage(code: Int, rawBody: String): String {
        val parsed = runCatching {
            json.decodeFromString<WireErrorEnvelope>(rawBody).error?.message
        }.getOrNull()
        val detail = parsed?.takeIf { it.isNotBlank() }
        return when {
            detail != null -> "HTTP $code: $detail"
            code == 401 -> "HTTP 401: Unauthorized — check your API key."
            else -> "HTTP $code: request failed."
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        val defaultJson = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}

/** Maps domain messages onto the OpenAI wire role/content shape. */
private fun List<ChatMessage>.toWireMessages(): List<WireMessage> =
    mapNotNull { message ->
        when (message) {
            is ChatMessage.Text -> WireMessage(
                role = message.message.role.wireRole,
                content = message.message.content
            )
            is ChatMessage.System -> WireMessage(
                role = "system",
                content = message.message.content
            )
            // Reasoning rows are local UI state, not part of the request context.
            is ChatMessage.Thinking -> null
        }
    }

private val ChatMessageRole.wireRole: String
    get() = when (this) {
        ChatMessageRole.USER -> "user"
        ChatMessageRole.ASSISTANT -> "assistant"
        ChatMessageRole.SYSTEM -> "system"
    }
