package io.github.bengidev.openzone.onboarding.presenter.visuals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.bengidev.openzone.onboarding.domain.OnboardingQueueItem
import io.github.bengidev.openzone.onboarding.theme.OnboardingTheme

/** iOS-matched queue rows: timeline dot, inline status + title, detail below. */
@Composable
fun PromptQueueVisualView(
    queuedPromptCount: Int,
    modifier: Modifier = Modifier,
    appeared: Boolean = true
) {
    val spacing = OnboardingTheme.spacing
    val items = OnboardingQueueItem.samples.take(queuedPromptCount.coerceAtLeast(1))

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEachIndexed { index, item ->
            QueueRow(
                item = item,
                index = index,
                isLast = index == items.lastIndex,
                appeared = appeared
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QueueRow(
    item: OnboardingQueueItem,
    index: Int,
    isLast: Boolean,
    appeared: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = OnboardingTheme.palette
    val radius = OnboardingTheme.radius
    val spacing = OnboardingTheme.spacing

    val statusColor = when (item.status) {
        OnboardingQueueItem.Status.RUNNING -> palette.accentPrimary
        OnboardingQueueItem.Status.NEXT -> palette.warning
        OnboardingQueueItem.Status.QUEUED -> palette.textSecondary
        OnboardingQueueItem.Status.READY -> palette.success
    }

    val rowAlpha = if (appeared) 1f else 0f
    val rowOffset = if (appeared) 0f else 10f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = rowAlpha
                translationY = rowOffset
            }
            .clip(RoundedCornerShape(radius.sm))
            .background(
                palette.surfaceSubtle.copy(alpha = if (index == 0) 0.5f else 0.3f)
            )
            .border(
                width = 1.dp,
                color = if (index == 0) palette.accentPrimary.copy(alpha = 0.34f) else palette.lineSoft,
                shape = RoundedCornerShape(radius.sm)
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.width(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(palette.lineSoft.copy(alpha = 0.72f))
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.status.name,
                    style = OnboardingTheme.typography.monoXs,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
                Text(
                    text = item.title,
                    style = OnboardingTheme.typography.monoSm,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            Text(
                text = item.detail,
                style = OnboardingTheme.typography.monoXs,
                color = palette.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = if (index == 0) Icons.Filled.HourglassTop else Icons.AutoMirrored.Filled.List,
            contentDescription = item.status.name,
            tint = if (index == 0) palette.accentPrimary else palette.textTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}
