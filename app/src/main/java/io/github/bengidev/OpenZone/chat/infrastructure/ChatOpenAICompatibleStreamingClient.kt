package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatMessageRole
import io.github.bengidev.openzone.chat.domain.ChatRequest
import io.github.bengidev.openzone.chat.domain.ChatStreamError
import io.github.bengidev.openzone.chat.domain.ChatStreamingEvent
import io.github.bengidev.openzone.chat.infrastructure.wire.ChatCompletionRequest
import io.github.bengidev.openzone.chat.infrastructure.wire.OpenRouterStreamPayloadParser
import io.github.bengidev.openzone.chat.infrastructure.wire.WireErrorEnvelope
import io.github.bengidev.openzone.chat.infrastructure.wire.WireMessage
import io.github.bengidev.openzone.chat.infrastructure.wire.WireReasoning
import io.github.bengidev.openzone.shared.externals.networking.AuthScheme
import io.github.bengidev.openzone.shared.externals.networking.ChatProvider
import io.github.bengidev.openzone.shared.externals.networking.SseLineDecoder
import io.github.bengidev.openzone.shared.externals.security.CredentialStore
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
import java.util.concurrent.TimeUnit
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
 * making a single instance provider-agnostic. Mirrors iOS
 * `ChatOpenAICompatibleStreamingClient`.
 */
class ChatOpenAICompatibleStreamingClient(
    private val credentialStore: CredentialStore,
    private val callFactory: Call.Factory = defaultStreamingClient,
    private val json: Json = defaultJson
) : ChatAPIClient {

    override fun stream(request: ChatRequest): Flow<ChatStreamingEvent> = flow {
        val provider = request.provider

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
                    val trimmed = line.removePrefix("\uFEFF").trim()
                    if (trimmed.isEmpty() || trimmed.startsWith(":")) continue

                    for (event in decoder.decode(line + "\n")) {
                        when (event) {
                            is SseLineDecoder.SseEvent.Data ->
                                if (emitDelta(event.payload)) return@use
                            SseLineDecoder.SseEvent.Done -> {
                                emit(ChatStreamingEvent.Done)
                                return@use
                            }
                        }
                    }

                    // Some proxies / providers emit NDJSON lines without the SSE `data:` prefix.
                    if (trimmed.startsWith("{") && !trimmed.startsWith("data:")) {
                        if (emitDelta(trimmed)) return@use
                    }
                }
                for (event in decoder.flush()) {
                    when (event) {
                        is SseLineDecoder.SseEvent.Data ->
                            if (emitDelta(event.payload)) return@use
                        SseLineDecoder.SseEvent.Done -> {
                            emit(ChatStreamingEvent.Done)
                            return@use
                        }
                    }
                }
                emit(ChatStreamingEvent.Done)
            } catch (io: IOException) {
                emit(ChatStreamingEvent.Error(ChatStreamError(io.message ?: "Stream read error.")))
            }
        }
    }

    /** @return `true` when the stream should terminate after an error payload. */
    private suspend fun kotlinx.coroutines.flow.FlowCollector<ChatStreamingEvent>.emitDelta(
        payload: String
    ): Boolean {
        val parsed = OpenRouterStreamPayloadParser.parse(payload, json)

        parsed.errorMessage?.let { message ->
            emit(ChatStreamingEvent.Error(ChatStreamError(message)))
            return true
        }

        for (thinking in parsed.thinkingDeltas) {
            emit(ChatStreamingEvent.ThinkingDelta(thinking))
        }
        for (text in parsed.textDeltas) {
            emit(ChatStreamingEvent.TextDelta(text))
        }
        return false
    }

    private fun buildRequest(request: ChatRequest, secret: String?): Request {
        val provider = request.provider
        val reasoning = request.reasoningLevel.wireEffort?.let { WireReasoning(effort = it) }
        val payload = ChatCompletionRequest(
            model = request.modelId,
            messages = request.messages.toWireMessages(),
            stream = true,
            reasoning = reasoning
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
        val detail =
                OpenRouterStreamPayloadParser.parse(rawBody, json).errorMessage
                        ?: runCatching {
                            json.decodeFromString<WireErrorEnvelope>(rawBody).error?.message
                        }.getOrNull()
        val message = detail?.takeIf { it.isNotBlank() }
        return when (code) {
            401 -> "Unauthorized (401). Check that your API key is valid."
            403 ->
                if (message != null) {
                    "Forbidden (403): $message"
                } else {
                    "Forbidden (403). Your plan may not include API access. Upgrade your provider plan to use these endpoints."
                }
            else ->
                if (message != null) {
                    "Request failed ($code): $message"
                } else {
                    "Request failed with status $code."
                }
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        val defaultStreamingClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .callTimeout(0, TimeUnit.SECONDS)
            .build()

        val defaultJson = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
            isLenient = true
        }
    }
}

@Deprecated(
    message = "Renamed to ChatOpenAICompatibleStreamingClient for iOS parity.",
    replaceWith = ReplaceWith("ChatOpenAICompatibleStreamingClient")
)
typealias OpenAiCompatibleStreamingClient = ChatOpenAICompatibleStreamingClient

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
            is ChatMessage.Thinking -> null
        }
    }

private val ChatMessageRole.wireRole: String
    get() = when (this) {
        ChatMessageRole.USER -> "user"
        ChatMessageRole.ASSISTANT -> "assistant"
        ChatMessageRole.SYSTEM -> "system"
    }
