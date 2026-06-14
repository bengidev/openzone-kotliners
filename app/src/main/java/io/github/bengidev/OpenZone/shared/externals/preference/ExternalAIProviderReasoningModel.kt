package io.github.bengidev.openzone.shared.externals.preference

/**
 * Persisted reasoning effort for provider requests.
 * Mirrors iOS `ExternalAIProviderReasoningModel`.
 *
 * [Off] means no reasoning parameter is sent to the provider.
 */
enum class ExternalAIProviderReasoningModel(val title: String) {
    Off("Off"),
    Low("Low"),
    Medium("Medium"),
    High("High");

    /** The OpenRouter/OpenAI `reasoning.effort` wire string, or null when [Off]. */
    val wireEffort: String?
        get() = when (this) {
            Off -> null
            Low -> "low"
            Medium -> "medium"
            High -> "high"
        }
}

/** @deprecated Use [ExternalAIProviderReasoningModel]. */
typealias ComposerReasoningLevel = ExternalAIProviderReasoningModel
