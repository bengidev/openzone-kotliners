package io.github.bengidev.openzone.onboarding.presenter.visuals

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bengidev.openzone.onboarding.theme.OnboardingTheme

/** iOS OnboardingEncryptedPairingVisualView — devices at edges, center lock + CTA overlay. */
@Composable
fun EncryptedPairingVisualView(
    pairingConfirmed: Boolean,
    onTogglePairing: () -> Unit,
    onActionButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = OnboardingTheme.palette
    val radius = OnboardingTheme.radius
    val accent = if (pairingConfirmed) palette.accentPrimary else palette.warning
    val dotOffsetDp by animateFloatAsState(
        targetValue = if (pairingConfirmed) 56f else -56f,
        animationSpec = spring(),
        label = "pairing_dot_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "End-to-end encrypted local-to-OpenZone pairing diagram" }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeviceNode(
                icon = Icons.Filled.PhoneAndroid,
                title = "LOCAL",
                subtitle = "Local key",
                active = pairingConfirmed
            )
            Spacer(modifier = Modifier.weight(1f))
            DeviceNode(
                icon = Icons.Filled.Laptop,
                title = "OPENZONE",
                subtitle = "AI chat lane",
                active = true
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(horizontal = 82.dp)
                ) {
                    val centerY = size.height / 2f
                    drawLine(
                        color = palette.lineSoft.copy(alpha = 0.8f),
                        start = Offset(0f, centerY),
                        end = Offset(size.width, centerY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 7.dp.toPx()))
                    )
                    drawCircle(
                        color = palette.accentPrimary,
                        radius = 4.5.dp.toPx(),
                        center = Offset(size.width / 2f + dotOffsetDp.dp.toPx(), centerY)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(RoundedCornerShape(radius.sm))
                        .background(palette.surfaceRaised)
                        .border(1.dp, palette.lineSoft, RoundedCornerShape(radius.sm))
                        .clickable(onClick = onTogglePairing),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            PairingActionButton(
                pairingConfirmed = pairingConfirmed,
                accent = accent,
                onClick = onActionButtonClick
            )
        }
    }
}

@Composable
private fun DeviceNode(
    icon: ImageVector,
    title: String,
    subtitle: String,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = OnboardingTheme.palette
    val radius = OnboardingTheme.radius
    val border = if (active) palette.accentPrimary.copy(alpha = 0.52f) else palette.lineSoft

    Column(
        modifier = modifier.width(108.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 76.dp, height = 92.dp)
                .clip(RoundedCornerShape(radius.sm))
                .background(palette.surfaceSubtle.copy(alpha = 0.5f))
                .border(1.dp, border, RoundedCornerShape(radius.sm)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) palette.textPrimary else palette.textTertiary,
                modifier = Modifier.size(32.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = OnboardingTheme.typography.monoXs,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = OnboardingTheme.typography.monoXs.copy(
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.sp
                ),
                color = palette.textTertiary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PairingActionButton(
    pairingConfirmed: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.32f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(
            imageVector = if (pairingConfirmed) Icons.Filled.Refresh else Icons.Filled.Link,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = if (pairingConfirmed) "ROTATE KEY" else "PAIR DEVICE",
            style = OnboardingTheme.typography.monoSm,
            color = accent
        )
    }
}
