package io.github.bengidev.openzone.home.presenter.visuals

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import io.github.bengidev.openzone.home.theme.HomeTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val CanvasWidth = 360f
private const val CanvasHeight = 240f
private val GlyphRamp = listOf("░", "▒", "▓", "█")

/**
 * Compose particle orb — visual parity with iOS HomeAsciiParticleOrbView.
 */
@Composable
fun HomeParticleOrbView(
    modifier: Modifier = Modifier
) {
    val palette = HomeTheme.palette
    val textMeasurer = rememberTextMeasurer()
    val outerDots = remember { ParticleOrbLayout.makeOuterDots(seedOffset = 0, count = 138, radiusBias = 0.72) }
    val outerDots2 = remember { ParticleOrbLayout.makeOuterDots(seedOffset = 1200, count = 126, radiusBias = 0.66) }
    val pulseDots = remember { ParticleOrbLayout.makePulseDots(seedOffset = 1800, count = 86) }
    val coreBlocks = remember {
        ParticleOrbLayout.makeCoreBlocks(seedOffset = 2400, count = 236, prominence = 0.96)
    }
    val coreBlocks2 = remember {
        ParticleOrbLayout.makeCoreBlocks(seedOffset = 4800, count = 220, prominence = 0.78)
    }
    val coreBlocks3 = remember {
        ParticleOrbLayout.makeCoreBlocks(seedOffset = 7200, count = 158, prominence = 0.58)
    }
    val orbDust = remember { ParticleOrbLayout.makeOrbDust(seedOffset = 9600, count = 132) }

    val transition = rememberInfiniteTransition(label = "orb")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        val scale = minOf(size.width / CanvasWidth, size.height / CanvasHeight)
        val offsetX = (size.width - CanvasWidth * scale) / 2f
        val offsetY = (size.height - CanvasHeight * scale) / 2f

        rotate(degrees = drift * 8f, pivot = Offset(size.width / 2f, size.height / 2f)) {
            drawParticleLayer(
                dots = outerDots,
                tint = palette.orbTint,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                alphaScale = 0.36f + sin(pulse * PI.toFloat() * 2f) * 0.04f
            )
            drawParticleLayer(
                dots = outerDots2,
                tint = palette.orbTint,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                alphaScale = 0.28f
            )
            drawParticleLayer(
                dots = pulseDots,
                tint = palette.orbTint,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                alphaScale = 0.20f + sin(pulse * PI.toFloat() * 2f) * 0.10f
            )
            drawBlockLayer(
                blocks = coreBlocks,
                tint = palette.accent,
                textMeasurer = textMeasurer,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                alphaScale = 0.78f + sin(pulse * PI.toFloat() * 2f) * 0.08f
            )
            drawBlockLayer(
                blocks = coreBlocks2,
                tint = palette.accent,
                textMeasurer = textMeasurer,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                alphaScale = 0.52f
            )
            drawBlockLayer(
                blocks = coreBlocks3,
                tint = palette.accent,
                textMeasurer = textMeasurer,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                alphaScale = 0.30f
            )
            drawParticleLayer(
                dots = orbDust,
                tint = palette.orbTint,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                alphaScale = 0.30f
            )
        }
    }
}

private fun DrawScope.drawParticleLayer(
    dots: List<ParticleDot>,
    tint: Color,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    alphaScale: Float
) {
    dots.forEach { dot ->
        val x = offsetX + dot.point.x * scale
        val y = offsetY + dot.point.y * scale
        drawCircle(
            color = tint.copy(alpha = dot.opacity * alphaScale),
            radius = dot.size * scale * 0.5f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawBlockLayer(
    blocks: List<ParticleBlock>,
    tint: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    alphaScale: Float
) {
    blocks.forEach { block ->
        val style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = (block.size * scale).coerceAtLeast(4f).spValue,
            color = tint.copy(alpha = block.opacity * alphaScale)
        )
        val layout = textMeasurer.measure(block.glyph, style)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                offsetX + block.point.x * scale - layout.size.width / 2f,
                offsetY + block.point.y * scale - layout.size.height / 2f
            )
        )
    }
}

private val Float.spValue: androidx.compose.ui.unit.TextUnit
    get() = androidx.compose.ui.unit.TextUnit(this, androidx.compose.ui.unit.TextUnitType.Sp)

private data class ParticleDot(
    val point: Offset,
    val size: Float,
    val opacity: Float
)

private data class ParticleBlock(
    val point: Offset,
    val glyph: String,
    val size: Float,
    val opacity: Float
)

private object ParticleOrbLayout {
    private val center = Offset(CanvasWidth * 0.5f, CanvasHeight * 0.5f)
    private val outerField = androidx.compose.ui.geometry.Size(324f, 204f)
    private val coreField = androidx.compose.ui.geometry.Size(156f, 146f)
    private const val SnapGrid = 3f

    fun makePulseDots(seedOffset: Int, count: Int): List<ParticleDot> {
        return buildList {
            repeat(count) { index ->
                val seed = (seedOffset + index).toDouble()
                val orbit = 0.18 + ParticleOrbMath.noise(seed, 3.0) * 0.42
                val angle = ParticleOrbMath.noise(seed, 11.0) * PI * 2
                val point = Offset(
                    x = center.x + cos(angle).toFloat() * outerField.width * orbit.toFloat() * 0.5f,
                    y = center.y + sin(angle).toFloat() * outerField.height * orbit.toFloat() * 0.5f
                )
                val size = (1.0 + ParticleOrbMath.noise(seed, 47.0) * 1.8).toFloat()
                val opacity = (0.12 + ParticleOrbMath.noise(seed, 59.0) * 0.22).toFloat()
                add(ParticleDot(point, size, opacity))
            }
        }
    }

    fun makeOrbDust(seedOffset: Int, count: Int): List<ParticleDot> {
        return buildList {
            repeat(count) { index ->
                val seed = (seedOffset + index).toDouble()
                val orbit = 0.35 + ParticleOrbMath.noise(seed, 3.0) * 0.55
                val angle = ParticleOrbMath.noise(seed, 11.0) * PI * 2
                val point = Offset(
                    x = center.x + cos(angle).toFloat() * outerField.width * orbit.toFloat() * 0.5f,
                    y = center.y + sin(angle).toFloat() * outerField.height * orbit.toFloat() * 0.5f
                )
                val size = (0.8 + ParticleOrbMath.noise(seed, 47.0) * 1.6).toFloat()
                val opacity = (0.10 + ParticleOrbMath.noise(seed, 59.0) * 0.20).toFloat()
                add(ParticleDot(point, size, opacity))
            }
        }
    }

    fun makeOuterDots(seedOffset: Int, count: Int, radiusBias: Double): List<ParticleDot> {
        return buildList {
            repeat(count) { index ->
                val seed = (seedOffset + index).toDouble()
                val orbit = 0.28 + ParticleOrbMath.noise(seed, 3.0).pow(0.82) * radiusBias
                val angle = ParticleOrbMath.noise(seed, 11.0) * PI * 2
                val jitterX = (ParticleOrbMath.noise(seed, 29.0) - 0.5) * 14
                val jitterY = (ParticleOrbMath.noise(seed, 37.0) - 0.5) * 11
                val point = Offset(
                    x = center.x + cos(angle).toFloat() * outerField.width * orbit.toFloat() * 0.5f + jitterX.toFloat(),
                    y = center.y + sin(angle).toFloat() * outerField.height * orbit.toFloat() * 0.5f + jitterY.toFloat()
                )
                val size = (1.2 + ParticleOrbMath.noise(seed, 47.0) * 2.4).toFloat()
                val opacity = (0.16 + ParticleOrbMath.noise(seed, 59.0) * 0.28).toFloat()
                add(ParticleDot(point, size, opacity))
            }
        }
    }

    fun makeCoreBlocks(seedOffset: Int, count: Int, prominence: Double): List<ParticleBlock> {
        val blocks = mutableListOf<ParticleBlock>()
        val maxAttempts = count * 12
        var attempts = 0

        while (blocks.size < count && attempts < maxAttempts) {
            val seed = (seedOffset + attempts).toDouble()
            val x = ParticleOrbMath.noise(seed, 3.0) * 2 - 1
            val y = ParticleOrbMath.noise(seed, 9.0) * 2 - 1
            val density = coreDensity(x, y, seed) * prominence

            if (ParticleOrbMath.noise(seed, 15.0) < density) {
                val snappedX = snap(center.x + x.toFloat() * coreField.width * 0.5f)
                val snappedY = snap(center.y + y.toFloat() * coreField.height * 0.5f)
                val energy = (density + ParticleOrbMath.noise(seed, 25.0) * 0.12).coerceIn(0.0, 1.0)
                val size = (4.1 + energy * 5.8 + ParticleOrbMath.noise(seed, 21.0) * 1.1).toFloat()
                val glyphIndex = ((energy * GlyphRamp.size).toInt().coerceIn(0, GlyphRamp.size - 1))
                val opacity = (0.18 + energy * 0.82 + ParticleOrbMath.noise(seed, 33.0) * 0.08).toFloat().coerceAtMost(1f)
                blocks += ParticleBlock(
                    point = Offset(snappedX, snappedY),
                    glyph = GlyphRamp[glyphIndex],
                    size = size,
                    opacity = opacity
                )
            }
            attempts++
        }
        return blocks
    }

    private fun snap(value: Float): Float {
        return (value / SnapGrid).toInt() * SnapGrid
    }

    private fun coreDensity(x: Double, y: Double, seed: Double): Double {
        val radius = sqrt(x * x + y * y)
        val angle = kotlin.math.atan2(y, x)
        val shell = maxOf(0.0, 1 - (radius / 1.05).pow(2)) * 0.36
        val ring = exp(-((radius - 0.56) / 0.24).pow(2)) * 0.34
        val centerMass = ParticleOrbMath.gaussian2D(x + 0.03, y + 0.02, 0.34, 0.30) * 0.38
        val upperLeftMass = ParticleOrbMath.gaussian2D(x + 0.24, y + 0.15, 0.28, 0.18) * 0.28
        val lowerRightMass = ParticleOrbMath.gaussian2D(x - 0.22, y - 0.20, 0.22, 0.20) * 0.24
        val spiral = (0.5 + 0.5 * sin(angle * 3.2 + radius * 8.4)) * 0.16
        val centerCut = ParticleOrbMath.gaussian2D(x - 0.02, y - 0.02, 0.18, 0.16) * 0.20
        val bite = ParticleOrbMath.gaussian2D(x + 0.34, y - 0.23, 0.18, 0.14) * 0.18
        val noise = (ParticleOrbMath.noise(seed, 41.0) - 0.5) * 0.20
        return (shell + ring + centerMass + upperLeftMass + lowerRightMass + spiral - centerCut - bite + noise)
            .coerceIn(0.0, 1.0)
    }
}

private object ParticleOrbMath {
    fun noise(value: Double, seed: Double): Double {
        val mixed = sin(value * 12.9898 + seed * 78.233) * 43_758.5453
        return mixed - kotlin.math.floor(mixed)
    }

    fun gaussian2D(x: Double, y: Double, sigmaX: Double, sigmaY: Double): Double {
        return exp(-0.5 * ((x / sigmaX).pow(2) + (y / sigmaY).pow(2)))
    }
}
