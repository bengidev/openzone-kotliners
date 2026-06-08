package io.github.bengidev.openzone.settings.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bengidev.openzone.home.domain.ComposerReasoningLevel
import io.github.bengidev.openzone.shared.networking.formatContextLength
import io.github.bengidev.openzone.settings.application.SettingsState
import io.github.bengidev.openzone.shared.networking.ChatModel
import io.github.bengidev.openzone.settings.theme.SettingsTheme
import io.github.bengidev.openzone.shared.networking.ChatProvider

/**
 * Settings content surface. Lets the user paste an API key (stored encrypted,
 * never rendered back), choose a provider, and pick a model from the curated
 * fallback catalog. All colors come from [SettingsTheme.palette] — no hardcoded
 * tokens. Stateless: state in, intents out, owned by `SettingsComponent`.
 */
@Composable
fun SettingsView(
    state: SettingsState,
    onApiKeyDraftChanged: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onProviderSelected: (String) -> Unit,
    onModelSelected: (String) -> Unit,
    onReasoningLevelSelected: (ComposerReasoningLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = SettingsTheme.palette

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        SectionLabel("API KEY")
        Spacer(Modifier.height(8.dp))
        ApiKeyField(
            draft = state.apiKeyDraft,
            hasApiKey = state.hasApiKey,
            canSave = state.canSaveKey,
            onDraftChanged = onApiKeyDraftChanged,
            onSave = onSaveApiKey,
            onClear = onClearApiKey
        )

        Spacer(Modifier.height(24.dp))
        SectionLabel("PROVIDER")
        Spacer(Modifier.height(8.dp))
        state.providers.forEach { provider ->
            ProviderRow(
                provider = provider,
                selected = provider.id == state.selectedProviderId,
                onClick = { onProviderSelected(provider.id) }
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))
        SectionLabel("MODEL")
        Spacer(Modifier.height(8.dp))
        if (state.models.isEmpty()) {
            Text(
                text = "No models available for this provider.",
                color = palette.textSecondary,
                fontSize = 14.sp
            )
        } else {
            state.models.forEach { model ->
                ModelRow(
                    model = model,
                    selected = model.id == state.selectedModelId,
                    onClick = { onModelSelected(model.id) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        // Reasoning effort — only shown when the selected model supports reasoning.
        if (state.selectedModelSupportsReasoning) {
            Spacer(Modifier.height(24.dp))
            SectionLabel("REASONING EFFORT")
            Spacer(Modifier.height(8.dp))
            ReasoningLevelPicker(
                selectedLevel = state.reasoningLevel,
                onLevelSelected = onReasoningLevelSelected
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = SettingsTheme.palette.textSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun ApiKeyField(
    draft: String,
    hasApiKey: Boolean,
    canSave: Boolean,
    onDraftChanged: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit
) {
    val palette = SettingsTheme.palette

    OutlinedTextField(
        value = draft,
        onValueChange = onDraftChanged,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        placeholder = {
            Text(
                text = if (hasApiKey) "Key stored — enter a new key to replace" else "Paste API key",
                color = palette.textTertiary
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = palette.textPrimary,
            unfocusedTextColor = palette.textPrimary,
            focusedContainerColor = palette.surface,
            unfocusedContainerColor = palette.surface,
            focusedIndicatorColor = palette.accentPrimary,
            unfocusedIndicatorColor = palette.lineSoft,
            cursorColor = palette.accentPrimary
        )
    )

    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            label = "Save key",
            enabled = canSave,
            fill = palette.controlStrong,
            content = palette.controlStrongText,
            onClick = onSave,
            modifier = Modifier.width(140.dp)
        )
        if (hasApiKey) {
            ActionButton(
                label = "Clear",
                enabled = true,
                fill = palette.surfaceSubtle,
                content = palette.textPrimary,
                onClick = onClear,
                modifier = Modifier.width(100.dp)
            )
        }
    }
    if (hasApiKey) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = palette.success,
                modifier = Modifier.width(16.dp).height(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "A key is securely stored on this device.",
                color = palette.textSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    enabled: Boolean,
    fill: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = SettingsTheme.palette
    val bg = if (enabled) fill else palette.surfaceSubtle
    val fg = if (enabled) content else palette.textTertiary
    Box(
        modifier = modifier
            .height(44.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .clickableWhen(enabled, onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = fg, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProviderRow(
    provider: ChatProvider,
    selected: Boolean,
    onClick: () -> Unit
) {
    SelectableRow(
        title = provider.displayName,
        subtitle = provider.baseUrl,
        selected = selected,
        onClick = onClick
    )
}

@Composable
private fun ModelRow(
    model: ChatModel,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = SettingsTheme.palette
    // No description — matches the home picker after the iOS-faithful pass.
    // Subtitle now carries the context length so the row still has secondary
    // info, but never renders OpenRouter's marketing prose.
    SelectableRow(
        title = model.displayName,
        subtitle = model.contextLength?.let(::formatContextLength),
        subtitleBadges = buildList {
            if (model.supportsReasoning) add("REASONING" to palette.accent)
            if (model.isFree) add("FREE" to palette.textMuted)
        },
        selected = selected,
        onClick = onClick
    )
}

@Composable
private fun SelectableRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
    trailing: String? = null,
    /**
     * Optional badges rendered inline after the subtitle (e.g. `128K ctx
     * REASONING FREE`). Order is preserved — pass [REASONING, FREE] to render
     * REASONING first.
     */
    subtitleBadges: List<Pair<String, Color>> = emptyList()
) {
    val palette = SettingsTheme.palette
    val borderColor = if (selected) palette.accentPrimary else palette.lineSoft
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surface, RoundedCornerShape(12.dp))
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickableWhen(true, onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = palette.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            val hasSubtitle = !subtitle.isNullOrBlank()
            val hasBadges = subtitleBadges.isNotEmpty()
            if (hasSubtitle || hasBadges) {
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasSubtitle) {
                        Text(
                            text = subtitle!!,
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    subtitleBadges.forEachIndexed { index, (label, color) ->
                        if (hasSubtitle || index > 0) {
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = label,
                            color = color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = trailing,
                color = palette.textTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = palette.accentPrimary,
                modifier = Modifier.width(20.dp).height(20.dp)
            )
        }
    }
}

/** Segmented-style row of reasoning level buttons. */
@Composable
private fun ReasoningLevelPicker(
    selectedLevel: ComposerReasoningLevel,
    onLevelSelected: (ComposerReasoningLevel) -> Unit
) {
    val palette = SettingsTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ComposerReasoningLevel.entries.forEach { level ->
            val selected = level == selectedLevel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        if (selected) palette.controlStrong else palette.surface,
                        RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) palette.accentPrimary else palette.lineSoft,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickableWhen(true) { onLevelSelected(level) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level.title,
                    color = if (selected) palette.controlStrongText else palette.textSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

/** Applies a click handler only when [enabled]; otherwise leaves the modifier untouched. */
private fun Modifier.clickableWhen(enabled: Boolean, onClick: () -> Unit): Modifier =
    if (enabled) this.clickable(onClick = onClick) else this
