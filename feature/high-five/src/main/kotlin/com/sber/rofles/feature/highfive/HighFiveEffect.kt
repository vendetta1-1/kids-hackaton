package com.sber.rofles.feature.highfive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sber.rofles.core.particles.ParticleField
import com.sber.rofles.core.particles.ParticleKind
import com.sber.rofles.core.particles.ParticleLayer
import com.sber.rofles.core.particles.drawEdgeGlow
import com.sber.rofles.core.particles.particleKind
import com.sber.rofles.core.particles.rememberEffectProgress
import com.sber.rofles.core.prank.PrankArt
import com.sber.rofles.core.prank.PrankPalette
import com.sber.rofles.core.prank.PrankType
import com.sber.rofles.core.prank.toColor
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.ElementStyle
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

private const val IMPACT = 0.24f
private val FriendshipColor = Color(0xFFFF7AC6)

@Composable
fun HighFiveEffect(
    mine: ElementStyle,
    friend: ElementStyle?,
    title: String,
    combo: ComboKey?,
    isNew: Boolean,
    effectId: Long,
    modifier: Modifier = Modifier,
    durationMs: Int = 3_000,
) {
    val myColor = mine.color.toColor()
    val friendColor = friend?.color?.toColor() ?: FriendshipColor
    val layers = remember(mine, friend) {
        listOf(
            ParticleLayer(mine.element.particleKind, myColor, count = 50),
            ParticleLayer(friend?.element?.particleKind ?: ParticleKind.SPARK, friendColor, count = 50),
            ParticleLayer(ParticleKind.SPARK, Color(0xFFFFD54A), count = 24),
        )
    }
    val progress = rememberEffectProgress(durationMs, key = effectId)
    val burst = { ((progress.value - IMPACT) / (1f - IMPACT)).coerceIn(0f, 1f) }
    val fadeOut = { ((1f - progress.value) * 4f).coerceIn(0f, 1f) }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val travel = maxWidth.value / 2f
        Canvas(Modifier.fillMaxSize()) {
            val t = progress.value
            val fade = min(1f, min(t * 5f, (1f - t) * 3f)).coerceAtLeast(0f)
            drawEdgeGlow(myColor, fade)
            drawEdgeGlow(friendColor, fade * 0.8f)
            if (t >= IMPACT) {
                val flash = ((t - IMPACT) / 0.3f).coerceIn(0f, 1f)
                val center = Offset(size.width / 2f, size.height * 0.42f)
                val radius = size.minDimension * (0.15f + 0.6f * flash)
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color.White.copy(alpha = 0.95f * (1f - flash)),
                            lerp(myColor, friendColor, 0.5f).copy(alpha = 0.45f * (1f - flash)),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = radius,
                    ),
                    radius = radius,
                    center = center,
                )
            }
        }
        ParticleField(
            layers = layers,
            progress = burst,
            burst = true,
            seed = effectId.toInt(),
            modifier = Modifier.fillMaxSize(),
        )

        FlyingCharacter(
            image = PrankArt.element(mine.element),
            fromLeft = true,
            travel = travel,
            progress = { progress.value },
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-60).dp),
        )
        FlyingCharacter(
            image = friend?.let { PrankArt.element(it.element) } ?: PrankArt.highFive,
            fromLeft = false,
            travel = travel,
            progress = { progress.value },
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-60).dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-40).dp)
                .padding(horizontal = 40.dp)
                .widthIn(max = 320.dp)
                .graphicsLayer {
                    val appear = ((progress.value - IMPACT - 0.04f) / 0.12f).coerceIn(0f, 1f)
                    val pop = 1f + 0.12f * sin(appear * PI.toFloat())
                    scaleX = appear * pop
                    scaleY = appear * pop
                    alpha = min(appear, fadeOut())
                }
                .shadow(28.dp, RoundedCornerShape(28.dp), ambientColor = myColor, spotColor = friendColor)
                .background(PrankPalette.surface, RoundedCornerShape(28.dp))
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                if (isNew) "Новая карточка!" else "Дай пять!",
                style = PrankType.footnote.copy(
                    color = if (isNew) PrankPalette.green else PrankPalette.tertiary,
                    fontSize = 13.sp,
                ),
                modifier = Modifier
                    .background(if (isNew) PrankPalette.greenSoft else PrankPalette.background, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
            Image(
                painter = painterResource(combo?.let(PrankArt::combo) ?: PrankArt.highFive),
                contentDescription = title,
                modifier = Modifier.size(150.dp),
            )
            Text(title, style = PrankType.headline.copy(fontSize = 24.sp, lineHeight = 28.sp), textAlign = TextAlign.Center)
            if (friend != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Image(painterResource(PrankArt.element(mine.element)), null, Modifier.size(26.dp))
                    Text("${mine.title} + ${friend.title}", style = PrankType.footnote)
                    Image(painterResource(PrankArt.element(friend.element)), null, Modifier.size(26.dp))
                }
            }
        }
    }
}

@Composable
private fun FlyingCharacter(
    image: Int,
    fromLeft: Boolean,
    travel: Float,
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(128.dp)
            .graphicsLayer {
                val t = progress()
                val approach = (t / IMPACT).coerceIn(0f, 1f)
                val eased = 1f - (1f - approach) * (1f - approach)
                val direction = if (fromLeft) -1f else 1f
                translationX = direction * (travel * (1f - eased) + 36f) * density
                rotationZ = direction * -18f * (1f - eased)
                val shrink = ((t - IMPACT) / 0.08f).coerceIn(0f, 1f)
                scaleX = 1f - shrink
                scaleY = 1f - shrink
                alpha = 1f - shrink
            },
    ) {
        Image(painterResource(image), contentDescription = null, modifier = Modifier.fillMaxSize())
    }
}
