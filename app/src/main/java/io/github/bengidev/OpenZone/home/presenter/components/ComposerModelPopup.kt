package io.github.bengidev.openzone.home.presenter.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.github.bengidev.openzone.home.application.HomeState
import io.github.bengidev.openzone.home.theme.HomeTheme
import io.github.bengidev.openzone.shared.networking.ChatModel

/**
 * Composer model picker popup. Shown when the user taps the model chip in the
 * composer rail. Offers debounced client-side search and a free-tier toggle;
 * both filter [HomeState.filteredModels] which is already computed by
 * [HomeState] from [HomeState.availableModels].
 *
 * Stateless: all state flows down from [HomeState]; intents flow up via lambdas.
 * Mirrors the iOS composer model popup pattern.
 */
@Composable
fun ComposerModelPopup(
    state: HomeState,
    onSearchQueryChanged: (String) -> Unit,
    onFilterFreeOnlyToggled: () -> Unit,
    onModelSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        ComposerModelPopupContent(
            state = state,
            onSearchQueryChanged = onSearchQueryChanged,
            onFilterFreeOnlyToggled = onFilterFreeOnlyToggled,
            onModelSelected = onModelSelected
        )
    }
}

@Composable
private fun ComposerModelPopupContent(
    state: HomeState,
    onSearchQueryChanged: (String) -> Unit,
    onFilterFreeOnlyToggled: () -> Unit,
    onModelSelected: (String) -> Unit
) {
    val palette = HomeTheme.palette
    val shape = RoundedCornerShape(16.dp)
    val bg = if (palette.isDark) palette.elevatedSurface else palette.surface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 24.dp, shape = shape, clip = false,
                ambientColor = Color.Black.copy(alpha = 0.22f),
                spotColor = Color.Black.copy(alpha = 0.22f))
            .clip(shape)
            .background(bg)
            .border(width = 1.dp,
                color = palette.border.copy(alpha = if (palette.isDark) 0.30f else 0.50f),
                shape = shape)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Select Model",
            color = palette.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        // Search field
        TextField(
            value = state.modelSearchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text("Search models…", color = palette.textMuted, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = palette.textMuted
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = TextFieldDefaults.colors(
                focusedTextColor = palette.textPrimary,
                unfocusedTextColor = palette.textPrimary,
                focusedContainerColor = palette.surface,
                unfocusedContainerColor = palette.surface,
                focusedIndicatorColor = palette.accent,
                unfocusedIndicatorColor = palette.border.copy(alpha = 0.35f),
                cursorColor = palette.accent
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )

        Spacer(Modifier.height(10.dp))

        // Free-tier filter toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Free models only",
                color = palette.textSecondary,
                fontSize = 13.sp
            )
            Switch(
                checked = state.modelFilterFreeOnly,
                onCheckedChange = { onFilterFreeOnlyToggled() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = palette.primaryActionText,
                    checkedTrackColor = palette.accent,
                    uncheckedThumbColor = palette.textMuted,
                    uncheckedTrackColor = palette.border
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        val filtered = state.filteredModels
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No models match.",
                    color = palette.textMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                items(filtered, key = { it.id }) { model ->
                    ModelPickerRow(
                        model = model,
                        selected = model.id == state.selectedModelId,
                        onClick = { onModelSelected(model.id) }
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun ModelPickerRow(
    model: ChatModel,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = HomeTheme.palette
    val borderColor = if (selected) palette.accent else palette.border.copy(alpha = 0.30f)
    val borderWidth = if (selected) 2.dp else 1.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surface, RoundedCornerShape(10.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Full model name on its own line — wraps freely, never truncates.
            Text(
                text = model.displayName,
                color = palette.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            // Subtitle row — context length + badges flow inline. Order is
            // ctx, REASONING, FREE; matches the Settings model row layout.
            val ctxLabel = model.contextLength?.let(::formatContextLength)
            val badges = buildList {
                if (model.supportsReasoning) add("REASONING" to palette.accent)
                if (model.isFree) add("FREE" to palette.textMuted)
            }
            if (ctxLabel != null || badges.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (ctxLabel != null) {
                        Text(
                            text = ctxLabel,
                            color = palette.textMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    badges.forEachIndexed { index, (label, color) ->
                        if (ctxLabel != null || index > 0) {
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            text = label,
                            color = color,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        if (selected) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = palette.accent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatContextLength(tokens: Int): String = when {
    tokens >= 1_000_000 -> "${tokens / 1_000_000}M ctx"
    tokens >= 1_000 -> "${tokens / 1_000}K ctx"
    else -> "$tokens ctx"
}
