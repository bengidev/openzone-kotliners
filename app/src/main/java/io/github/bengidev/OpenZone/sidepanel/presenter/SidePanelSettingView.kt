package io.github.bengidev.openzone.sidepanel.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bengidev.openzone.home.theme.HomeTheme
import io.github.bengidev.openzone.shared.externals.preference.ExternalAIProviderReasoningModel
import io.github.bengidev.openzone.sidepanel.application.SidePanelSettingComponent

@Composable
fun SidePanelSettingView(
    state: SidePanelSettingComponent.State,
    onApiKeyDraftChanged: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onProviderSelected: (String) -> Unit,
    onReasoningModelSelected: (ExternalAIProviderReasoningModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = HomeTheme.palette
    var providerExpanded by remember { mutableStateOf(false) }
    val providerName = state.selectedProvider?.displayName ?: state.selectedProviderId

    Column(
        modifier =
                modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                    "Provider",
                    color = palette.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
            )
            Text(
                    "Choose which AI provider to use. Each provider has its own API key and model catalog.",
                    color = palette.textSecondary,
                    fontSize = 13.sp
            )
            Box {
                SettingsFieldContainer(
                        modifier = Modifier.clickable { providerExpanded = true }
                ) {
                    Text(
                            providerName,
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                    )
                    Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = palette.textTertiary,
                            modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                        expanded = providerExpanded,
                        onDismissRequest = { providerExpanded = false }
                ) {
                    state.providers.forEach { provider ->
                        DropdownMenuItem(
                                text = { Text(provider.displayName) },
                                onClick = {
                                    providerExpanded = false
                                    onProviderSelected(provider.id)
                                }
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                    "Provider API key",
                    color = palette.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
            )
            Text(
                    if (state.hasStoredKey) {
                        "A key is stored securely in the Keychain. Enter a new value to replace it."
                    } else {
                        "Add your $providerName API key to enable sending. It is stored securely in the Keychain and never leaves this device."
                    },
                    color = palette.textSecondary,
                    fontSize = 13.sp
            )
            SettingsFieldContainer {
                Icon(
                        imageVector =
                                if (state.hasStoredKey) Icons.Filled.Key
                                else Icons.Outlined.Key,
                        contentDescription = null,
                        tint = palette.textTertiary,
                        modifier = Modifier.size(14.dp)
                )
                BasicTextField(
                        value = state.draftApiKey,
                        onValueChange = onApiKeyDraftChanged,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 15.sp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions =
                                KeyboardOptions(
                                        autoCorrectEnabled = false,
                                        imeAction = ImeAction.Done
                                ),
                        cursorBrush = SolidColor(palette.accentPrimary),
                        decorationBox = { innerTextField ->
                            Box {
                                if (state.draftApiKey.isEmpty()) {
                                    Text("sk-...", color = palette.textTertiary, fontSize = 15.sp)
                                }
                                innerTextField()
                            }
                        }
                )
            }
            if (state.hasStoredKey) {
                Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = palette.textSecondary,
                            modifier = Modifier.size(12.dp)
                    )
                    Text("Key stored", color = palette.textSecondary, fontSize = 12.sp)
                }
            }
        }

        state.errorMessage?.let { error ->
            Text(error, color = palette.accentPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                            if (state.canSave) palette.controlStrong
                                            else palette.surfaceSubtle.copy(alpha = 0.9f)
                                    )
                                    .clickable(enabled = state.canSave, onClick = onSaveApiKey),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        if (state.hasStoredKey) "Update key" else "Save key",
                        color =
                                if (state.canSave) palette.controlStrongText
                                else palette.textTertiary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                )
            }
            if (state.hasStoredKey) {
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(44.dp)
                                        .clickable(onClick = onClearApiKey),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            "Remove stored key",
                            color = palette.accentPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (state.modelSupportsReasoning) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                        "Reasoning",
                        color = palette.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                )
                Text(
                        "Choose how much effort the model spends reasoning before it answers. Off sends no reasoning request.",
                        color = palette.textSecondary,
                        fontSize = 13.sp
                )
                ReasoningSegmentedControl(
                        selected = state.reasoningModel,
                        onSelected = onReasoningModelSelected
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsFieldContainer(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val palette = HomeTheme.palette
    val fillAlpha = if (palette.isDark) 0.5f else 0.85f
    val strokeAlpha = if (palette.isDark) 0.45f else 0.6f
    Row(
            modifier =
                    modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.surfaceRaised.copy(alpha = fillAlpha))
                            .border(
                                    1.dp,
                                    palette.lineSoft.copy(alpha = strokeAlpha),
                                    RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
    )
}

@Composable
private fun ReasoningSegmentedControl(
    selected: ExternalAIProviderReasoningModel,
    onSelected: (ExternalAIProviderReasoningModel) -> Unit
) {
    val palette = HomeTheme.palette
    val levels = ExternalAIProviderReasoningModel.entries
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.surfaceSubtle)
                            .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        levels.forEach { level ->
            val isSelected = selected == level
            Box(
                    modifier =
                            Modifier.weight(1f)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(
                                            if (isSelected) palette.surfaceRaised
                                            else palette.surfaceSubtle
                                    )
                                    .clickable { onSelected(level) }
                                    .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        level.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) palette.textPrimary else palette.textSecondary
                )
            }
        }
    }
}
