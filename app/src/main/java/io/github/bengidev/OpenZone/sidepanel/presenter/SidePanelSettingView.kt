package io.github.bengidev.openzone.sidepanel.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bengidev.openzone.home.theme.HomeTheme
import io.github.bengidev.openzone.shared.externals.preference.ExternalAIProviderReasoningModel
import io.github.bengidev.openzone.sidepanel.application.SidePanelSettingComponent

@OptIn(ExperimentalMaterial3Api::class)
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Provider", color = palette.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Choose which AI provider to use. Each provider has its own API key.",
                color = palette.textSecondary,
                fontSize = 13.sp
            )
            ExposedDropdownMenuBox(
                expanded = providerExpanded,
                onExpandedChange = { providerExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.selectedProvider?.displayName ?: state.selectedProviderId,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = palette.surfaceRaised,
                        unfocusedContainerColor = palette.surfaceRaised,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary
                    )
                )
                ExposedDropdownMenu(
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
            Text("Provider API key", color = palette.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(
                if (state.hasStoredKey) {
                    "A key is stored securely. Enter a new value to replace it."
                } else {
                    "Add your API key to enable sending. It is stored securely and never leaves this device."
                },
                color = palette.textSecondary,
                fontSize = 13.sp
            )
            OutlinedTextField(
                value = state.draftApiKey,
                onValueChange = onApiKeyDraftChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("sk-...", color = palette.textTertiary) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = palette.surfaceRaised,
                    unfocusedContainerColor = palette.surfaceRaised,
                    focusedTextColor = palette.textPrimary,
                    unfocusedTextColor = palette.textPrimary
                )
            )
            if (state.hasStoredKey) {
                Text("Key stored", color = palette.textSecondary, fontSize = 12.sp)
            }
            state.errorMessage?.let {
                Text(it, color = palette.accentPrimary, fontSize = 13.sp)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(
                onClick = onSaveApiKey,
                enabled = state.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (state.canSave) palette.controlStrong else palette.surfaceSubtle,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    if (state.hasStoredKey) "Update key" else "Save key",
                    color = if (state.canSave) palette.controlStrongText else palette.textTertiary
                )
            }
            if (state.hasStoredKey) {
                TextButton(onClick = onClearApiKey, modifier = Modifier.fillMaxWidth()) {
                    Text("Remove stored key", color = palette.accentPrimary)
                }
            }
        }

        if (state.modelSupportsReasoning) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Reasoning", color = palette.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Choose how much effort the model spends reasoning before it answers.",
                    color = palette.textSecondary,
                    fontSize = 13.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    ExternalAIProviderReasoningModel.entries.forEach { level ->
                        val selected = state.reasoningModel == level
                        TextButton(
                            onClick = { onReasoningModelSelected(level) },
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selected) palette.controlStrong else palette.surfaceSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                        ) {
                            Text(
                                level.title,
                                color = if (selected) palette.controlStrongText else palette.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
