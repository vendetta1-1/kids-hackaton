package com.sber.rofles.feature.highfive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sber.rofles.core.particles.ParticleField
import com.sber.rofles.core.particles.ParticleKind
import com.sber.rofles.core.particles.ParticleLayer
import com.sber.rofles.core.particles.particleKind
import com.sber.rofles.core.particles.rememberEffectProgress
import com.sber.rofles.core.prank.PrankArt
import com.sber.rofles.core.prank.PrankPalette
import com.sber.rofles.core.prank.PrankType
import com.sber.rofles.core.prank.toColor
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.ElementStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private const val IMPACT = 0.16f
private const val CARD_IN = 0.30f
private const val FLIP_END = 0.42f
private const val REVEAL = 0.36f
private const val EXIT = 0.86f
private const val CARD_Y = 0.45f

private val Gold = Color(0xFFFFD54A)
private val Scrim = Color(0xFF0E2233)
private val FriendshipColor = Color(0xFFFF7AC6)
private val CardWidth = 248.dp
private val CardHeight = 344.dp

@Composable
fun HighFiveEffect(
    mine: ElementStyle,
    friend: ElementStyle?,
    title: String,
    combo: ComboKey?,
    isNew: Boolean,
    effectId: Long,
    modifier: Modifier = Modifier,
    count: Int = 1,
    durationMs: Int = 3_000,
    onCardClick: (() -> Unit)? = null,
) {
    val myColor = mine.color.toColor()
    val friendColor = friend?.color?.toColor() ?: FriendshipColor
    val confetti = remember(mine, friend) {
        listOf(
            ParticleLayer(ParticleKind.CONFETTI, Color.Unspecified, count = 70),
            ParticleLayer(mine.element.particleKind, myColor, count = 26),
            ParticleLayer(friend?.element?.particleKind ?: ParticleKind.SPARK, friendColor, count = 26),
            ParticleLayer(ParticleKind.SPARK, Gold, count = 20),
        )
    }
    val progress = rememberEffectProgress(durationMs, key = effectId)
    val t = { progress.value }
    val envelope = { min(1f, min(t() / 0.08f, (1f - t()) / 0.1f)).coerceIn(0f, 1f) }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val cardOffset = maxHeight * (CARD_Y - 0.5f)
        val toastOffset = -maxHeight * 0.5f + 70.dp
        val travel = maxWidth.value / 2f

        Canvas(Modifier.fillMaxSize()) {
            val alpha = envelope()
            drawRect(Scrim.copy(alpha = 0.66f * alpha))
            val center = Offset(size.width / 2f, size.height * CARD_Y)
            val glow = size.minDimension * 0.7f
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(lerp(myColor, friendColor, 0.5f).copy(alpha = 0.55f * alpha), Color.Transparent),
                    center = center,
                    radius = glow,
                ),
                radius = glow,
                center = center,
            )
            val rays = ((t() - CARD_IN) / 0.12f).coerceIn(0f, 1f) * alpha
            if (rays > 0f) drawRays(center, size.maxDimension, t() * 70f, myColor, friendColor, rays)
        }

        ParticleField(
            layers = confetti,
            progress = { ((t() - REVEAL) / (1f - REVEAL)).coerceIn(0f, 1f) },
            burst = true,
            seed = effectId.toInt(),
            modifier = Modifier.fillMaxSize(),
        )

        FlyingCharacter(PrankArt.element(mine.element), fromLeft = true, travel = travel, progress = t, offsetY = cardOffset)
        FlyingCharacter(
            friend?.let { PrankArt.element(it.element) } ?: PrankArt.highFive,
            fromLeft = false,
            travel = travel,
            progress = t,
            offsetY = cardOffset,
        )

        Sparkles(progress = t, offsetY = cardOffset, color = Gold)

        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = cardOffset)
                .size(CardWidth, CardHeight)
                .graphicsLayer {
                    val p = t()
                    val enter = ((p - IMPACT) / (CARD_IN - IMPACT)).coerceIn(0f, 1f)
                    val enterEased = 1f - (1f - enter) * (1f - enter) * (1f - enter)
                    val flip = ((p - CARD_IN) / (FLIP_END - CARD_IN)).coerceIn(0f, 1f)
                    val flipEased = flip * flip * (3f - 2f * flip)
                    val exit = ((p - EXIT) / (1f - EXIT)).coerceIn(0f, 1f)
                    val exitEased = exit * exit
                    val hold = ((p - FLIP_END) / (EXIT - FLIP_END)).coerceIn(0f, 1f)
                    val bob = sin(hold * PI.toFloat() * 3f) * 6f

                    rotationY = 180f * (1f - flipEased)
                    rotationZ = -24f * (1f - enterEased) + sin(hold * PI.toFloat() * 2f) * 2f
                    cameraDistance = 16f * density
                    val pulse = 1f + 0.08f * sin(flip * PI.toFloat())
                    val scale = (0.25f + 0.75f * enterEased) * pulse * (1f - 0.8f * exitEased)
                    scaleX = scale
                    scaleY = scale
                    translationY = (bob + (toastOffset - cardOffset).value * exitEased) * density
                    alpha = if (p < IMPACT) 0f else 1f - exitEased
                }
                .clickable(
                    enabled = onCardClick != null,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onCardClick?.invoke() },
        ) {
            val showFront = { t() >= (CARD_IN + FLIP_END) / 2f }
            CardBack(myColor, friendColor, Modifier.graphicsLayer { alpha = if (showFront()) 0f else 1f })
            CardFront(
                mine = mine,
                friend = friend,
                title = title,
                combo = combo,
                isNew = isNew,
                count = count,
                myColor = myColor,
                friendColor = friendColor,
                shine = { ((t() - 0.46f) / 0.16f).coerceIn(0f, 1f) },
                float = { sin(t() * PI.toFloat() * 6f) * 5f },
                modifier = Modifier.graphicsLayer { alpha = if (showFront()) 1f else 0f },
            )
        }
    }
}

@Composable
private fun CardBack(myColor: Color, friendColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = myColor, spotColor = friendColor)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(myColor, lerp(myColor, friendColor, 0.5f), friendColor)))
            .drawWithContent {
                drawContent()
                val step = size.width / 5f
                for (row in 0..8) {
                    for (col in 0..5) {
                        val x = col * step + if (row % 2 == 0) 0f else step / 2f
                        val y = row * step * 0.9f
                        drawStar(Offset(x, y), step * 0.12f, Color.White.copy(alpha = 0.22f))
                    }
                }
            }
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.16f)),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(132.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(PrankArt.highFive), contentDescription = null, modifier = Modifier.size(104.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Дай пять!", style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
        }
    }
}

@Composable
private fun CardFront(
    mine: ElementStyle,
    friend: ElementStyle?,
    title: String,
    combo: ComboKey?,
    isNew: Boolean,
    count: Int,
    myColor: Color,
    friendColor: Color,
    shine: () -> Float,
    float: () -> Float,
    modifier: Modifier = Modifier,
) {
    val outer = RoundedCornerShape(28.dp)
    val inner = RoundedCornerShape(23.dp)
    Box(
        modifier
            .fillMaxSize()
            .shadow(28.dp, outer, ambientColor = myColor, spotColor = friendColor)
            .clip(outer)
            .background(Brush.linearGradient(listOf(myColor, Gold, friendColor)))
            .padding(5.dp)
            .clip(inner)
            .background(
                Brush.verticalGradient(
                    listOf(Color.White, lerp(Color.White, lerp(myColor, friendColor, 0.5f), 0.22f)),
                ),
            )
            .drawWithContent {
                drawContent()
                val s = shine()
                if (s > 0f && s < 1f) {
                    val x = -size.width + s * size.width * 3f
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = 0.75f), Color.Transparent),
                            start = Offset(x - size.width * 0.35f, 0f),
                            end = Offset(x, size.height),
                        ),
                    )
                }
            },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Badge(isNew, count)
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .size(184.dp)
                    .background(
                        Brush.radialGradient(listOf(lerp(myColor, friendColor, 0.5f).copy(alpha = 0.28f), Color.Transparent)),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painterResource(combo?.let(PrankArt::combo) ?: PrankArt.highFive),
                    contentDescription = title,
                    modifier = Modifier
                        .size(164.dp)
                        .graphicsLayer { translationY = float() * density },
                )
            }
            Text(
                title,
                style = PrankType.headline.copy(fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Spacer(Modifier.weight(1f))
            if (friend != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ElementBubble(mine)
                    Text("+", style = PrankType.title.copy(color = PrankPalette.secondary))
                    ElementBubble(friend)
                }
            } else {
                Text("Вы дали пять друг другу", style = PrankType.footnote)
            }
        }
    }
}

@Composable
private fun Badge(isNew: Boolean, count: Int) {
    val text = if (isNew) "Новая карточка!" else "Снова вместе · ×$count"
    Text(
        text,
        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White),
        modifier = Modifier
            .shadow(8.dp, CircleShape, ambientColor = PrankPalette.green, spotColor = PrankPalette.green)
            .background(
                if (isNew) {
                    Brush.horizontalGradient(listOf(Color(0xFF19D13B), Color(0xFF6BE04A)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF7D868C), Color(0xFFA8B4BD)))
                },
                CircleShape,
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

@Composable
private fun ElementBubble(style: ElementStyle) {
    Row(
        Modifier
            .background(style.color.toColor().copy(alpha = 0.14f), CircleShape)
            .padding(start = 4.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(PrankArt.element(style.element)), contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.size(4.dp))
        Text(style.title, style = PrankType.footnote.copy(color = PrankPalette.text, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun FlyingCharacter(
    image: Int,
    fromLeft: Boolean,
    travel: Float,
    progress: () -> Float,
    offsetY: Dp,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .offset(y = offsetY)
                .size(128.dp)
                .graphicsLayer {
                    val t = progress()
                    val approach = (t / IMPACT).coerceIn(0f, 1f)
                    val eased = 1f - (1f - approach) * (1f - approach)
                    val direction = if (fromLeft) -1f else 1f
                    translationX = direction * (travel * (1f - eased) + 40f) * density
                    rotationZ = direction * -20f * (1f - eased)
                    val shrink = ((t - IMPACT) / 0.06f).coerceIn(0f, 1f)
                    scaleX = 1f - shrink
                    scaleY = 1f - shrink
                    alpha = if (t <= 0f) 0f else 1f - shrink
                },
        )
    }
}

@Composable
private fun Sparkles(progress: () -> Float, offsetY: Dp, color: Color) {
    val spots = remember {
        listOf(
            Offset(-0.62f, -0.55f), Offset(0.64f, -0.42f), Offset(-0.7f, 0.18f), Offset(0.7f, 0.3f),
            Offset(-0.45f, 0.66f), Offset(0.5f, 0.7f), Offset(0.05f, -0.72f), Offset(-0.05f, 0.8f),
        )
    }
    Canvas(Modifier.fillMaxSize()) {
        val t = progress()
        val appear = ((t - FLIP_END) / 0.08f).coerceIn(0f, 1f) * (1f - ((t - EXIT) / (1f - EXIT)).coerceIn(0f, 1f))
        if (appear <= 0f) return@Canvas
        val center = Offset(size.width / 2f, size.height / 2f + offsetY.toPx())
        val w = CardWidth.toPx()
        val h = CardHeight.toPx()
        spots.forEachIndexed { i, s ->
            val twinkle = 0.5f + 0.5f * sin(t * 30f + i * 1.7f)
            val r = 7.dp.toPx() + 6.dp.toPx() * twinkle
            drawStar(Offset(center.x + s.x * w, center.y + s.y * h), r, color.copy(alpha = appear * (0.5f + 0.5f * twinkle)))
        }
    }
}

private fun DrawScope.drawRays(center: Offset, length: Float, angle: Float, a: Color, b: Color, alpha: Float) {
    val count = 14
    val sweep = 360f / count / 2f
    rotate(angle, center) {
        repeat(count) { i ->
            val start = i * 360f / count
            val path = Path().apply {
                moveTo(center.x, center.y)
                val a1 = Math.toRadians(start.toDouble())
                val a2 = Math.toRadians((start + sweep).toDouble())
                lineTo(center.x + (cos(a1) * length).toFloat(), center.y + (sin(a1) * length).toFloat())
                lineTo(center.x + (cos(a2) * length).toFloat(), center.y + (sin(a2) * length).toFloat())
                close()
            }
            drawPath(
                path,
                brush = Brush.radialGradient(
                    listOf((if (i % 2 == 0) a else b).copy(alpha = 0.32f * alpha), Gold.copy(alpha = 0.12f * alpha), Color.Transparent),
                    center = center,
                    radius = max(length * 0.55f, 1f),
                ),
            )
        }
    }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color)
}
