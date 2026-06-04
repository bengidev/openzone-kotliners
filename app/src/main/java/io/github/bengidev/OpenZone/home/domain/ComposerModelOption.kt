package io.github.bengidev.openzone.home.domain

enum class ComposerModelOption(val title: String) {
    Gpt54("GPT-5.4"),
    Gpt55("GPT-5.5"),
    Local("Local");

    val availableSpeedModes: List<ComposerSpeedMode>
        get() = when (this) {
            Gpt54, Gpt55 -> ComposerSpeedMode.entries
            Local -> emptyList()
        }
}
