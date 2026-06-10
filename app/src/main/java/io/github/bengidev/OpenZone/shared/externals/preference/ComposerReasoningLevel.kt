package io.github.bengidev.openzone.shared.externals.preference

/**
 * Reasoning effort level for the composer. Mirrors iOS `ComposerReasoningLevel`.
 *
 * [Off] means no reasoning parameter is sent to the provider.
 * [Low] / [Medium] / [High] map to the provider `reasoning.effort` values
 * "low" / "medium" / "high" respectively. The mapping is done at the
 * infrastructure layer ([OpenAiCompatibleStreamingClient]) so the domain stays
 * wire-format-agnostic.
 *
 * The indicator and effort control are shown only when the selected model has
 * [ChatModel.supportsReasoning] set to true; [Off] is still a valid level for
 * reasoning-capable models (user may want to disable it).
 */
enum class ComposerReasoningLevel(val title: String) {
    Off("Off"),
    Low("Low"),
    Medium("Medium"),
    High("High");

    /** The OpenRouter/OpenAI `reasoning.effort` wire string, or null when [Off]. */
    val wireEffort: String?
        get() = when (this) {
            Off    -> null
            Low    -> "low"
            Medium -> "medium"
            High   -> "high"
        }
}
