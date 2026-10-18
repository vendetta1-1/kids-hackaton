package com.sber.rofles.prank

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sber.rofles.core.prank.PrankArt
import com.sber.rofles.core.prank.PrankPalette
import com.sber.rofles.core.prank.PrankType
import com.sber.rofles.core.prank.toColor
import com.sber.rofles.domain.model.ElementStyle

private val FrameGold = Color(0xFFFFD54A)
private val FrameLime = Color(0xFFA6FF2D)

@Composable
fun PrankAvatarFrame(
    modifier: Modifier = Modifier,
    avatarSize: Dp = 56.dp,
    avatar: @Composable () -> Unit,
) {
    val viewModel: PrankRewardsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    ElementFrame(style = state.frame?.style, avatarSize = avatarSize, modifier = modifier, content = avatar)
}

@Composable
fun PrankShakeHint(modifier: Modifier = Modifier) {
    val viewModel: PrankRewardsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    AnimatedVisibility(visible = state.showShakeHint, modifier = modifier) {
        ShakeHintCard(onDismiss = viewModel::dismissShakeHint)
    }
}

@Composable
internal fun ElementFrame(
    style: ElementStyle?,
    avatarSize: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        if (style != null) {
            val color = style.color.toColor()
            val spin by rememberInfiniteTransition(label = "frame").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(6_000, easing = LinearEasing)),
                label = "spin",
            )
            Canvas(Modifier.size(avatarSize + avatarSize / 4)) {
                val stroke = avatarSize.toPx() / 14f
                drawCircle(
                    brush = Brush.radialGradient(listOf(color.copy(alpha = 0.45f), Color.Transparent)),
                    radius = size.minDimension / 2f,
                )
                rotate(spin) {
                    drawCircle(
                        brush = Brush.sweepGradient(listOf(color, FrameGold, Color.White, color, FrameLime, color)),
                        radius = size.minDimension / 2f - stroke,
                        style = Stroke(width = stroke),
                    )
                }
            }
        }
        Box(Modifier.size(avatarSize).clip(CircleShape), contentAlignment = Alignment.Center) {
            content()
        }
        if (style != null) {
            Image(
                painter = painterResource(PrankArt.element(style.element)),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = avatarSize / 14, y = avatarSize / 14)
                    .size(avatarSize / 2),
            )
        }
    }
}

@Composable
private fun ShakeHintCard(onDismiss: () -> Unit) {
    val wiggle by rememberInfiniteTransition(label = "wiggle").animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(260), RepeatMode.Reverse),
        label = "angle",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .clip(PrankPalette.card)
            .background(PrankPalette.primaryButton)
            .padding(2.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PrankPalette.surface),
    ) {
        Row(
            Modifier.padding(start = 12.dp, end = 40.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painterResource(PrankArt.shakePhone),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer { rotationZ = wiggle },
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Тряхни телефон!", style = PrankType.title)
                Spacer(Modifier.height(2.dp))
                Text("Встряхни рядом с другом, и вы получите общую карточку", style = PrankType.footnote)
            }
        }
        Icon(
            painterResource(PrankArt.cross),
            contentDescription = "Закрыть",
            tint = PrankPalette.iconMono,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onDismiss)
                .padding(6.dp),
        )
    }
}
