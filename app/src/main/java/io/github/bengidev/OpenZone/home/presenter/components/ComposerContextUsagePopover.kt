package io.github.bengidev.openzone.home.presenter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bengidev.openzone.home.domain.ComposerContextUsage
import io.github.bengidev.openzone.home.theme.HomeTheme

/** Context usage popover — iOS `HomeComposerContextUsagePopover`. */
@Composable
fun ComposerContextUsagePopover(
    usage: ComposerContextUsage,
    modifier: Modifier = Modifier
) {
    val palette = HomeTheme.palette
    val shape = RoundedCornerShape(28.dp)
    val fillColor = if (palette.isDark) {
        palette.elevatedSurface.copy(alpha = 0.78f)
    } else {
        palette.surface.copy(alpha = 0.82f)
    }
    val borderColor = palette.border.copy(alpha = if (palette.isDark) 0.45f else 0.65f)
    val badgeFill = palette.accentSoft.copy(alpha = if (palette.isDark) 0.35f else 1f)

    Column(
        modifier = modifier
            .width(202.dp)
            .shadow(
                elevation = 14.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(shape)
            .background(fillColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Context window",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${usage.usedPercent}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = palette.accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(HomeTheme.radius.pill))
                    .background(badgeFill)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        LinearProgressIndicator(
            progress = { usage.usedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleX = 1f, scaleY = 0.72f)
                .height(4.dp)
                .clip(RoundedCornerShape(HomeTheme.radius.pill)),
            color = palette.accent,
            trackColor = palette.accent.copy(alpha = 0.14f)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${usage.usedPercent}% used",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = palette.textPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${usage.remainingPercent}% left",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = palette.textMuted
            )
        }

        Text(
            text = "${usage.usedTokensLabel} / ${usage.tokenLimitLabel} tokens",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = palette.textSecondary
        )
    }
}
