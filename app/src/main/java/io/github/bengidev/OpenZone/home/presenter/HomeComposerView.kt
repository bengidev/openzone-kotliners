package io.github.bengidev.openzone.home.presenter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.bengidev.openzone.home.application.HomeState
import io.github.bengidev.openzone.home.domain.ComposerContextUsage
import io.github.bengidev.openzone.home.domain.ComposerReasoningLevel
import io.github.bengidev.openzone.home.domain.ComposerSpeedMode
import io.github.bengidev.openzone.home.presenter.components.ComposerContextUsagePopover
import io.github.bengidev.openzone.home.presenter.components.ComposerModelPopup
import io.github.bengidev.openzone.home.theme.HomeTheme
import io.github.bengidev.openzone.home.theme.homeComposerTextFieldColors

/** Composer prompt panel + context rail — Material3 components. */
@Composable
fun HomeComposerView(
    state: HomeState,
    onDraftMessageChanged: (String) -> Unit,
    onSendTapped: () -> Unit,
    onAttachmentTapped: () -> Unit,
    onMicrophoneTapped: () -> Unit,
    onConfigureApiKeyTapped: () -> Unit,
    onModelPopupOpen: () -> Unit,
    onModelPopupDismiss: () -> Unit,
    onModelSearchQueryChanged: (String) -> Unit,
    onModelFilterFreeOnlyToggled: () -> Unit,
    onModelSelected: (String) -> Unit,
    onReasoningLevelSelected: (ComposerReasoningLevel) -> Unit,
    onSpeedModeSelected: (ComposerSpeedMode) -> Unit,
    onContextUsageTapped: () -> Unit,
    onContextUsageDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearFocusOnTapOutside()
            .padding(horizontal = 8.dp)
            .padding(top = 8.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ComposerPromptPanel(
            draftMessage = state.draftMessage,
            canSend = state.canSend,
            hasApiKey = state.hasApiKey,
            onConfigureApiKeyTapped = onConfigureApiKeyTapped,
            onDraftMessageChanged = onDraftMessageChanged,
            onSendTapped = onSendTapped,
            onAttachmentTapped = onAttachmentTapped,
            onMicrophoneTapped = onMicrophoneTapped
        )
        ComposerContextRail(
            state = state,
            onModelPopupOpen = onModelPopupOpen,
            onModelPopupDismiss = onModelPopupDismiss,
            onModelSearchQueryChanged = onModelSearchQueryChanged,
            onModelFilterFreeOnlyToggled = onModelFilterFreeOnlyToggled,
            onModelSelected = onModelSelected,
            onReasoningLevelSelected = onReasoningLevelSelected,
            onSpeedModeSelected = onSpeedModeSelected,
            onContextUsageTapped = onContextUsageTapped,
            onContextUsageDismissed = onContextUsageDismissed
        )
    }
}

@Composable
private fun ComposerPromptPanel(
    draftMessage: String,
    canSend: Boolean,
    hasApiKey: Boolean,
    onConfigureApiKeyTapped: () -> Unit,
    onDraftMessageChanged: (String) -> Unit,
    onSendTapped: () -> Unit,
    onAttachmentTapped: () -> Unit,
    onMicrophoneTapped: () -> Unit
) {
    val palette = HomeTheme.palette
    val shape = RoundedCornerShape(HomeTheme.radius.composer)
    val glassFill = if (palette.isDark) {
        palette.elevatedSurface.copy(alpha = 0.7f)
    } else {
        palette.surface.copy(alpha = 0.72f)
    }
    val glassBorder = palette.border.copy(alpha = if (palette.isDark) 0.35f else 0.55f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = shape,
                clip = false,
                ambientColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.16f),
                spotColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.16f)
            )
            .clip(shape)
            .background(glassFill)
            .border(width = 1.dp, color = glassBorder, shape = shape)
            .clearFocusOnTapOutside()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!hasApiKey) {
            MissingApiKeyHint(onClick = onConfigureApiKeyTapped)
        }

        TextField(
            value = draftMessage,
            onValueChange = onDraftMessageChanged,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 50.dp),
            placeholder = {
                Text(
                    text = "Ask anything... @files, \$skills, /commands",
                    style = HomeTheme.typography.composerInput
                )
            },
            textStyle = HomeTheme.typography.composerInput,
            colors = homeComposerTextFieldColors(),
            singleLine = false,
            maxLines = 5
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ComposerPromptIconButton(
                onClick = onAttachmentTapped,
                contentDescription = "Add attachment"
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = palette.textMuted
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            ComposerPromptIconButton(
                onClick = onMicrophoneTapped,
                contentDescription = "Start voice input"
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = palette.textMuted
                )
            }

            FilledIconButton(
                onClick = onSendTapped,
                enabled = canSend,
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (canSend) {
                        palette.primaryActionFill
                    } else {
                        palette.inverseSurface.copy(alpha = 0.08f)
                    },
                    contentColor = if (canSend) {
                        palette.primaryActionText
                    } else {
                        palette.textMuted
                    },
                    disabledContainerColor = palette.inverseSurface.copy(alpha = 0.08f),
                    disabledContentColor = palette.textMuted
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Send message",
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun ComposerPromptIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(30.dp)
    ) {
        content()
    }
}

/**
 * Tappable notice shown above the composer input when no API key is stored.
 * Routes to Settings. Mirrors iOS `MissingAPIKeyHint`: slashed-key icon,
 * two-line label, trailing chevron, soft rounded surfaceSubtle background.
 */
@Composable
private fun MissingApiKeyHint(onClick: () -> Unit) {
    val palette = HomeTheme.palette
    val shape = RoundedCornerShape(14.dp)
    val fill = palette.surfaceSubtle.copy(alpha = if (palette.isDark) 0.5f else 0.8f)
    val border = palette.border.copy(alpha = if (palette.isDark) 0.45f else 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(fill)
            .border(width = 1.dp, color = border, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.KeyOff,
            contentDescription = null,
            tint = palette.textSecondary,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = "Add an API key in Settings to start sending",
            style = HomeTheme.typography.chipLabel,
            color = palette.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = palette.textSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ComposerContextRail(
    state: HomeState,
    onModelPopupOpen: () -> Unit,
    onModelPopupDismiss: () -> Unit,
    onModelSearchQueryChanged: (String) -> Unit,
    onModelFilterFreeOnlyToggled: () -> Unit,
    onModelSelected: (String) -> Unit,
    onReasoningLevelSelected: (ComposerReasoningLevel) -> Unit,
    onSpeedModeSelected: (ComposerSpeedMode) -> Unit,
    onContextUsageTapped: () -> Unit,
    onContextUsageDismissed: () -> Unit
) {
    val clearFocus = rememberClearTextInputFocus()
    val density = LocalDensity.current
    val popoverOffset = remember(density) {
        IntOffset(
            x = with(density) { (-2).dp.roundToPx() },
            y = with(density) { (-46).dp.roundToPx() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Model chip — opens the dynamic catalog popup
                ComposerModelChip(
                    selectedModelTitle = state.selectedModelTitle,
                    onClick = {
                        clearFocus()
                        onModelPopupOpen()
                    }
                )
                // Reasoning chip — only shown when the selected model supports reasoning.
                if (state.selectedModelSupportsReasoning) {
                    ComposerReasoningChip(
                        selectedLevel = state.reasoningLevel,
                        onLevelSelected = onReasoningLevelSelected
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // SpeedMode is a cosmetic composer affordance only — not sent to the provider.
                // Show speed chip whenever a model is selected.
                val showSpeedChip = state.selectedModel != null
                if (showSpeedChip) {
                    ComposerSpeedChip(
                        speedMode = state.speedMode,
                        availableModes = ComposerSpeedMode.entries,
                        onSpeedModeSelected = onSpeedModeSelected
                    )
                }
                ComposerContextUsageButton(
                    usage = state.contextUsage,
                    onClick = {
                        clearFocus()
                        onContextUsageTapped()
                    }
                )
            }
        }

        if (state.isContextUsagePresented) {
            Popup(
                alignment = Alignment.BottomEnd,
                offset = popoverOffset,
                onDismissRequest = {
                    clearFocus()
                    onContextUsageDismissed()
                }
            ) {
                ComposerContextUsagePopover(usage = state.contextUsage)
            }
        }

        if (state.isModelPopupPresented) {
            ComposerModelPopup(
                state = state,
                onSearchQueryChanged = onModelSearchQueryChanged,
                onFilterFreeOnlyToggled = onModelFilterFreeOnlyToggled,
                onModelSelected = onModelSelected,
                onDismiss = onModelPopupDismiss
            )
        }
    }
}

@Composable
private fun ComposerModelChip(
    selectedModelTitle: String,
    onClick: () -> Unit
) {
    ComposerMenuChip(
        title = selectedModelTitle,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        },
        onClick = onClick
    )
}

@Composable
private fun ComposerReasoningChip(
    selectedLevel: ComposerReasoningLevel,
    onLevelSelected: (ComposerReasoningLevel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        ComposerMenuChip(
            title = selectedLevel.title,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.GridView,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            },
            onClick = { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ComposerReasoningLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level.title) },
                    onClick = {
                        expanded = false
                        onLevelSelected(level)
                    }
                )
            }
        }
    }
}

@Composable
private fun ComposerSpeedChip(
    speedMode: ComposerSpeedMode,
    availableModes: List<ComposerSpeedMode>,
    onSpeedModeSelected: (ComposerSpeedMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val palette = HomeTheme.palette
    val isFast = speedMode == ComposerSpeedMode.Fast

    Box {
        FilledTonalIconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(38.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = palette.surface,
                contentColor = if (isFast) palette.accent else palette.textSecondary
            )
        ) {
            Icon(
                imageVector = if (isFast) Icons.Default.Bolt else Icons.Outlined.Bolt,
                contentDescription = "Speed, ${speedMode.title}",
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            availableModes.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.title) },
                    onClick = {
                        expanded = false
                        onSpeedModeSelected(mode)
                    }
                )
            }
        }
    }
}

@Composable
private fun ComposerMenuChip(
    title: String,
    onClick: () -> Unit,
    leadingIcon: @Composable () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = title,
                style = HomeTheme.typography.chipLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = Modifier.widthIn(min = 92.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    )
}

@Composable
private fun ComposerContextUsageButton(
    usage: ComposerContextUsage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(38.dp)
    ) {
        ComposerContextUsageIndicator(usage = usage)
    }
}

/** Ring indicator — iOS `HomeComposerContextUsageIndicator`. */
@Composable
private fun ComposerContextUsageIndicator(
    usage: ComposerContextUsage,
    modifier: Modifier = Modifier
) {
    val palette = HomeTheme.palette
    val fillAlpha = if (palette.isDark) 0.42f else 0.72f

    Box(
        modifier = modifier
            .size(38.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.06f),
                spotColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.06f)
            )
            .clip(CircleShape)
            .background(palette.elevatedSurface.copy(alpha = fillAlpha))
            .border(
                width = 1.dp,
                color = palette.accent.copy(alpha = if (palette.isDark) 0.18f else 0.12f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(23.dp)) {
            val stroke = 3.dp.toPx()
            drawCircle(
                color = palette.accent.copy(alpha = if (palette.isDark) 0.14f else 0.12f),
                radius = size.minDimension / 2f,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = palette.accent.copy(alpha = if (palette.isDark) 0.92f else 0.82f),
                startAngle = -90f,
                sweepAngle = 360f * usage.usedFraction,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = usage.usedPercent.toString(),
            style = HomeTheme.typography.contextPercent,
            color = palette.accent
        )
    }
}
