package com.example.onboarding.presenter.visuals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onboarding.theme.OnboardingTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/** iOS-matched reasoning control: ring, slider, presets, bar chart. */
@Composable
fun ReasoningControlVisualView(
    reasoningLevel: Float,
    onReasoningLevelChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    appeared: Boolean = true
) {
    val palette = OnboardingTheme.palette
    val spacing = OnboardingTheme.spacing
    val radius = OnboardingTheme.radius

    val levelName = when {
        reasoningLevel < 0.38f -> "FAST ANSWER"
        reasoningLevel < 0.76f -> "BALANCED PLAN"
        else -> "DEEP REASONING"
    }
    val percentage = (reasoningLevel * 100).roundToInt()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReasoningRing(
                value = reasoningLevel,
                label = "$percentage%",
                modifier = Modifier.size(76.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = levelName,
                    style = OnboardingTheme.typography.monoSm,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.accentPrimary
                )
                Text(
                    text = "Set thinking before run.",
                    style = OnboardingTheme.typography.monoXs,
                    color = palette.textTertiary,
                    maxLines = 2
                )
            }
        }

        Slider(
            value = reasoningLevel,
            onValueChange = onReasoningLevelChanged,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = palette.surfaceRaised,
                activeTrackColor = palette.accentPrimary,
                inactiveTrackColor = palette.lineSoft.copy(alpha = 0.75f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            PresetButton("FAST", 0.22f, reasoningLevel, { onReasoningLevelChanged(0.22f) }, Modifier.weight(1f))
            PresetButton("BALANCED", 0.62f, reasoningLevel, { onReasoningLevelChanged(0.62f) }, Modifier.weight(1f))
            PresetButton("DEEP", 0.90f, reasoningLevel, { onReasoningLevelChanged(0.90f) }, Modifier.weight(1f))
        }

        ComputeBudgetChart(
            reasoningLevel = reasoningLevel,
            appeared = appeared,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        )
    }
}

@Composable
private fun ReasoningRing(value: Float, label: String, modifier: Modifier = Modifier) {
    val palette = OnboardingTheme.palette
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(),
        label = "reasoning_ring"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = 5.dp.toPx()
            drawCircle(
                color = palette.lineSoft.copy(alpha = 0.75f),
                radius = size.minDimension / 2f - stroke / 2f,
                style = Stroke(stroke)
            )
            drawArc(
                color = palette.accentPrimary,
                startAngle = -90f,
                sweepAngle = 360f * animatedValue,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = label,
            style = OnboardingTheme.typography.monoSm.copy(fontSize = 17.sp),
            fontWeight = FontWeight.SemiBold,
            color = palette.textPrimary
        )
    }
}

@Composable
private fun PresetButton(
    text: String,
    value: Float,
    currentValue: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = OnboardingTheme.palette
    val isSelected = abs(currentValue - value) < 0.08f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) palette.controlStrong else palette.surfaceSubtle.copy(alpha = 0.4f))
            .border(
                width = 1.dp,
                color = if (isSelected) palette.controlStrong.copy(alpha = 0.3f) else palette.lineSoft,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = OnboardingTheme.typography.monoXs,
            color = if (isSelected) palette.controlStrongText else palette.textSecondary
        )
    }
}

@Composable
private fun ComputeBudgetChart(
    reasoningLevel: Float,
    appeared: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = OnboardingTheme.palette
    val radius = OnboardingTheme.radius
    val barHeights = listOf(12.dp, 16.dp, 20.dp, 24.dp, 28.dp, 32.dp, 36.dp, 40.dp)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        barHeights.forEachIndexed { index, height ->
            if (index > 0) Spacer(modifier = Modifier.width(7.dp))
            val normalizedIndex = (index + 1) / 8f
            val isActive = normalizedIndex <= reasoningLevel
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(height)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isActive) palette.accentPrimary
                        else palette.textTertiary.copy(alpha = 0.22f)
                    )
            )
        }
    }
}
