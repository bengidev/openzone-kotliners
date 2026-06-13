package io.github.bengidev.openzone.chat.domain

import io.github.bengidev.openzone.shared.externals.preference.ComposerReasoningLevel
import io.github.bengidev.openzone.shared.externals.networking.ChatProvider

/**
 * Request payload sent to a chat API client.
 *
 * Carries the [provider] descriptor alongside the [modelId] so the API client
 * is provider-agnostic: it resolves the endpoint, auth scheme, and attribution
 * headers from [provider] and looks up the matching secret from the credential
 * store at call time. The secret itself never enters this value — only the
 * non-sensitive descriptor does.
 *
 * [reasoningLevel] controls whether a `reasoning.effort` parameter is included
 * in the wire request. [ComposerReasoningLevel.Off] (the default) means no
 * reasoning parameter is sent. Only non-null [ComposerReasoningLevel.wireEffort]
 * values reach the provider.
 *
 * Mirrors iOS `ChatRequest`.
 */
data class ChatRequest(
    val conversationId: String,
    val messages: List<ChatMessage>,
    val modelId: String,
    val provider: ChatProvider,
    val reasoningLevel: ComposerReasoningLevel = ComposerReasoningLevel.Off
) {
    val latestUserText: String
        get() = latestUserTextIn(messages)

    companion object {
        fun latestUserTextIn(messages: List<ChatMessage>): String =
            messages.reversed()
                .firstOrNull { it is ChatMessage.Text && it.message.role == ChatMessageRole.USER }
                ?.let { (it as ChatMessage.Text).message.content }
                .orEmpty()
    }
}
