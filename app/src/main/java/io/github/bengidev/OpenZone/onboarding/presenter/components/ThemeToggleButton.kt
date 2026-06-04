package io.github.bengidev.openzone.onboarding.presenter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import io.github.bengidev.openzone.onboarding.theme.OnboardingTheme

/** Theme toggle track — matches iOS ThemeToggleButton (32×28, sliding accent thumb). */
@Composable
fun ThemeToggleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = OnboardingTheme.palette.isDark
) {
    val palette = OnboardingTheme.palette

    Box(
        modifier = modifier
            .width(32.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(palette.surfaceSubtle.copy(alpha = 0.5f))
            .border(0.5.dp, palette.lineSoft, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = if (isDark) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 3.dp, vertical = 3.dp)
                .width(11.dp)
                .fillMaxHeight()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(3.dp),
                    ambientColor = palette.accentPrimary.copy(alpha = 0.35f),
                    spotColor = palette.accentPrimary.copy(alpha = 0.35f)
                )
                .clip(RoundedCornerShape(3.dp))
                .background(palette.accentPrimary)
                .border(0.5.dp, palette.textPrimary.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
        )
    }
}
