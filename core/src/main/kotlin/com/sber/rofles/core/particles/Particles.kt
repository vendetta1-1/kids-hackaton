package com.sber.rofles.core.particles

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.sber.rofles.domain.model.Element
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

enum class ParticleKind { EMBER, DROP, LEAF, WIND, SPARK, SNOW, CONFETTI }

val Element.particleKind: ParticleKind
    get() = when (this) {
        Element.FIRE -> ParticleKind.EMBER
        Element.WATER -> ParticleKind.DROP
        Element.EARTH -> ParticleKind.LEAF
        Element.AIR -> ParticleKind.WIND
        Element.LIGHTNING -> ParticleKind.SPARK
        Element.ICE -> ParticleKind.SNOW
    }

data class ParticleLayer(val kind: ParticleKind, val color: Color, val count: Int = 60)

private class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val phase: Float,
    val delay: Float,
    val spin: Float,
    val angle: Float,
)

@Composable
fun rememberEffectProgress(durationMs: Int, loop: Boolean = false, key: Any? = Unit): State<Float> {
    if (loop) {
        val transition = rememberInfiniteTransition(label = "particles")
        return transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMs, easing = LinearEasing), RepeatMode.Restart),
            label = "progress",
        )
    }
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(key) { progress.animateTo(1f, tween(durationMs, easing = LinearEasing)) }
    return progress.asState()
}

@Composable
fun ParticleField(
    layers: List<ParticleLayer>,
    progress: () -> Float,
    modifier: Modifier = Modifier,
    burst: Boolean = false,
    seed: Int = 0,
    fade: Boolean = true,
) {
    val particles = remember(layers, seed) {
        val random = Random(seed)
        layers.map { layer ->
            layer to List(layer.count) {
                Particle(
                    x = random.nextFloat(),
                    y = random.nextFloat(),
                    size = 0.5f + random.nextFloat(),
                    speed = 0.6f + random.nextFloat() * 0.8f,
                    phase = random.nextFloat() * 2f * PI.toFloat(),
                    delay = random.nextFloat() * if (burst) 0.15f else 0.45f,
                    spin = (random.nextFloat() - 0.5f) * 4f,
                    angle = random.nextFloat() * 2f * PI.toFloat(),
                )
            }
        }
    }
    Canvas(modifier) {
        val t = progress()
        val alpha = if (fade) min(1f, min(t * 6f, (1f - t) * 4f)).coerceIn(0f, 1f) else 1f
        if (alpha <= 0f) return@Canvas
        particles.forEach { (layer, list) ->
            list.forEach { p ->
                val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
                if (local <= 0f) return@forEach
                if (burst) drawBurst(layer, p, local, alpha) else drawAmbient(layer, p, local, alpha)
            }
        }
    }
}

private fun DrawScope.unit(): Float = min(size.width, size.height) / 100f

private fun DrawScope.drawAmbient(layer: ParticleLayer, p: Particle, t: Float, alpha: Float) {
    val w = size.width
    val h = size.height
    val u = unit()
    when (layer.kind) {
        ParticleKind.EMBER -> {
            val y = h * (1.05f - t * p.speed * 1.2f)
            val x = w * p.x + sin(t * 8f + p.phase) * u * 4f
            val flicker = 0.6f + 0.4f * sin(t * 40f + p.phase)
            val r = u * 1.4f * p.size * (1f - t * 0.5f)
            drawGlow(layer.color, Offset(x, y), r, alpha * flicker)
        }
        ParticleKind.DROP -> {
            val y = h * (-0.1f + t * p.speed * 1.4f + p.y * 0.2f)
            val x = w * p.x
            val len = u * 3.5f * p.size
            drawLine(layer.color.copy(alpha = alpha * 0.85f), Offset(x, y - len), Offset(x, y), u * 0.9f * p.size, StrokeCap.Round)
            drawCircle(layer.color.copy(alpha = alpha), u * 0.9f * p.size, Offset(x, y))
        }
        ParticleKind.LEAF -> {
            val y = h * (-0.05f + t * p.speed * 1.1f)
            val x = w * p.x + sin(t * 6f + p.phase) * u * 8f
            drawLeaf(layer.color, Offset(x, y), u * 2.6f * p.size, p.angle + t * p.spin * 6f, alpha)
        }
        ParticleKind.WIND -> {
            val x = w * (-0.3f + t * p.speed * 1.6f)
            val y = h * p.y + sin(t * 5f + p.phase) * u * 6f
            val len = u * 14f * p.size
            val path = Path().apply {
                moveTo(x - len, y)
                cubicTo(x - len * 0.6f, y - u * 3f, x - len * 0.3f, y + u * 3f, x, y)
            }
            drawPath(path, layer.color.copy(alpha = alpha * 0.8f), style = Stroke(u * 0.8f * p.size, cap = StrokeCap.Round))
        }
        ParticleKind.SPARK -> {
            val cycle = (t * 3f + p.phase) % 1f
            if (p.size > 1.25f && cycle < 0.18f) {
                drawBolt(layer.color, Offset(w * p.x, 0f), h * (0.35f + p.y * 0.5f), u, alpha * (1f - cycle / 0.18f), p.phase)
            }
            val x = w * p.x + cos(p.angle) * u * 20f * t
            val y = h * p.y + sin(p.angle) * u * 20f * t
            drawGlow(layer.color, Offset(x, y), u * 0.8f * p.size, alpha * (0.5f + 0.5f * sin(t * 50f + p.phase)))
        }
        ParticleKind.SNOW -> {
            val y = h * (-0.05f + t * p.speed * 0.9f)
            val x = w * p.x + sin(t * 4f + p.phase) * u * 5f
            drawSnowflake(layer.color, Offset(x, y), u * 2.2f * p.size, p.angle + t * p.spin, alpha)
        }
        ParticleKind.CONFETTI -> {
            val y = h * (-0.05f + t * p.speed * 1.2f)
            val x = w * p.x + sin(t * 7f + p.phase) * u * 6f
            drawConfetti(confettiColor(layer.color, p), Offset(x, y), u * 1.6f * p.size, p.angle + t * p.spin * 8f, t * 9f + p.phase, alpha)
        }
    }
}

private fun DrawScope.drawBurst(layer: ParticleLayer, p: Particle, t: Float, alpha: Float) {
    val u = unit()
    val center = Offset(size.width / 2f, size.height * 0.45f)
    val eased = 1f - (1f - t) * (1f - t)
    val distance = u * 70f * p.speed * eased
    val pos = Offset(
        center.x + cos(p.angle) * distance,
        center.y + sin(p.angle) * distance + u * 30f * t * t,
    )
    val fade = alpha * (1f - t * 0.7f)
    when (layer.kind) {
        ParticleKind.EMBER, ParticleKind.SPARK -> drawGlow(layer.color, pos, u * 1.3f * p.size, fade)
        ParticleKind.DROP -> drawCircle(layer.color.copy(alpha = fade), u * 1.2f * p.size, pos)
        ParticleKind.LEAF -> drawLeaf(layer.color, pos, u * 2.4f * p.size, p.angle + t * p.spin * 5f, fade)
        ParticleKind.WIND -> drawLine(
            layer.color.copy(alpha = fade),
            pos,
            Offset(pos.x - cos(p.angle) * u * 6f, pos.y - sin(p.angle) * u * 6f),
            u * 0.8f,
            StrokeCap.Round,
        )
        ParticleKind.SNOW -> drawSnowflake(layer.color, pos, u * 2f * p.size, p.angle + t * p.spin, fade)
        ParticleKind.CONFETTI -> drawConfetti(
            confettiColor(layer.color, p),
            pos,
            u * 1.7f * p.size,
            p.angle + t * p.spin * 8f,
            t * 12f + p.phase,
            fade,
        )
    }
}

private val ConfettiPalette = listOf(
    Color(0xFF19D13B),
    Color(0xFF3ED9FF),
    Color(0xFFFFC93C),
    Color(0xFFFF6FA8),
    Color(0xFFA06BFF),
    Color(0xFFA6FF2D),
)

private fun confettiColor(base: Color, p: Particle): Color =
    if (base == Color.Unspecified) ConfettiPalette[((p.phase * 100).toInt()) % ConfettiPalette.size] else base

private fun DrawScope.drawConfetti(color: Color, center: Offset, size: Float, angle: Float, flip: Float, alpha: Float) {
    val squash = abs(cos(flip))
    withTransform({
        translate(center.x, center.y)
        rotate(angle * 180f / PI.toFloat(), Offset.Zero)
        scale(1f, 0.25f + 0.75f * squash, Offset.Zero)
    }) {
        drawRect(color.copy(alpha = alpha), topLeft = Offset(-size, -size * 0.55f), size = Size(size * 2f, size * 1.1f))
    }
}

private fun DrawScope.drawGlow(color: Color, center: Offset, radius: Float, alpha: Float) {
    if (radius <= 0f) return
    drawCircle(color.copy(alpha = alpha * 0.25f), radius * 2.6f, center)
    drawCircle(color.copy(alpha = alpha), radius, center)
    drawCircle(Color.White.copy(alpha = alpha * 0.7f), radius * 0.4f, center)
}

private fun DrawScope.drawLeaf(color: Color, center: Offset, length: Float, angle: Float, alpha: Float) {
    withTransform({
        translate(center.x, center.y)
        rotate(angle * 180f / PI.toFloat(), Offset.Zero)
    }) {
        val path = Path().apply {
            moveTo(-length, 0f)
            quadraticTo(0f, -length * 0.7f, length, 0f)
            quadraticTo(0f, length * 0.7f, -length, 0f)
            close()
        }
        drawPath(path, color.copy(alpha = alpha))
        drawLine(Color.White.copy(alpha = alpha * 0.5f), Offset(-length * 0.8f, 0f), Offset(length * 0.8f, 0f), length * 0.08f)
    }
}

private fun DrawScope.drawSnowflake(color: Color, center: Offset, radius: Float, angle: Float, alpha: Float) {
    rotate(angle * 180f / PI.toFloat(), center) {
        repeat(3) { i ->
            val a = i * PI.toFloat() / 3f
            val dx = cos(a) * radius
            val dy = sin(a) * radius
            drawLine(
                color.copy(alpha = alpha),
                Offset(center.x - dx, center.y - dy),
                Offset(center.x + dx, center.y + dy),
                radius * 0.18f,
                StrokeCap.Round,
            )
        }
        drawCircle(Color.White.copy(alpha = alpha * 0.8f), radius * 0.2f, center)
    }
}

private fun DrawScope.drawBolt(color: Color, top: Offset, length: Float, u: Float, alpha: Float, phase: Float) {
    val random = Random((phase * 1000).toInt())
    val path = Path().apply {
        moveTo(top.x, top.y)
        var x = top.x
        var y = top.y
        val steps = 6
        repeat(steps) {
            x += (random.nextFloat() - 0.5f) * u * 14f
            y += length / steps
            lineTo(x, y)
        }
    }
    drawPath(path, color.copy(alpha = alpha * 0.35f), style = Stroke(u * 3f, cap = StrokeCap.Round))
    drawPath(path, color.copy(alpha = alpha), style = Stroke(u * 1.1f, cap = StrokeCap.Round))
    drawPath(path, Color.White.copy(alpha = alpha), style = Stroke(u * 0.4f, cap = StrokeCap.Round))
}

fun DrawScope.drawEdgeGlow(color: Color, alpha: Float) {
    val radius = size.maxDimension * 0.5f
    listOf(Offset(0f, 0f), Offset(size.width, size.height), Offset(size.width, 0f), Offset(0f, size.height)).forEach { corner ->
        drawCircle(
            brush = Brush.radialGradient(
                listOf(color.copy(alpha = 0.22f * alpha), Color.Transparent),
                center = corner,
                radius = radius,
            ),
            radius = radius,
            center = corner,
        )
    }
}
