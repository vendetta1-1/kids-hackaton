package com.sber.rofles.feature.solo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sber.rofles.core.particles.ParticleField
import com.sber.rofles.core.particles.ParticleLayer
import com.sber.rofles.core.particles.drawEdgeGlow
import com.sber.rofles.core.particles.particleKind
import com.sber.rofles.core.particles.rememberEffectProgress
import com.sber.rofles.core.prank.PrankArt
import com.sber.rofles.core.prank.PrankType
import com.sber.rofles.core.prank.toColor
import com.sber.rofles.domain.model.ElementStyle
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

@Composable
fun SoloEffect(
    style: ElementStyle,
    effectId: Long,
    modifier: Modifier = Modifier,
    durationMs: Int = 3_000,
) {
    val color = style.color.toColor()
    val kind = style.element.particleKind
    val layers = remember(style) {
        listOf(
            ParticleLayer(kind, color, count = 60),
            ParticleLayer(kind, lerp(color, Color.White, 0.45f), count = 26),
        )
    }
    val progress = rememberEffectProgress(durationMs, key = effectId)
    val fade = { min(1f, min(progress.value * 6f, (1f - progress.value) * 4f)).coerceIn(0f, 1f) }

    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val alpha = fade()
            drawEdgeGlow(color, alpha)
            val center = Offset(size.width / 2f, size.height * 0.42f)
            val radius = size.minDimension * 0.42f
            drawCircle(
                brush = Brush.radialGradient(listOf(color.copy(alpha = 0.32f * alpha), Color.Transparent), center = center, radius = radius),
                radius = radius,
                center = center,
            )
        }
        ParticleField(
            layers = layers,
            progress = { progress.value },
            seed = effectId.toInt(),
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-48).dp)
                .graphicsLayer {
                    val t = progress.value
                    val appear = (t / 0.14f).coerceIn(0f, 1f)
                    val pop = 1f + 0.18f * sin(appear * PI.toFloat())
                    val float = sin(t * 2f * PI.toFloat() * 1.5f) * 8f
                    scaleX = appear * pop
                    scaleY = appear * pop
                    translationY = float * density
                    alpha = fade()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(PrankArt.element(style.element)),
                contentDescription = style.title,
                modifier = Modifier.size(168.dp),
            )
            Text(
                style.title,
                style = PrankType.title,
                modifier = Modifier
                    .shadow(12.dp, CircleShape, ambientColor = color, spotColor = color)
                    .background(Color.White, CircleShape)
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            )
        }
    }
}
