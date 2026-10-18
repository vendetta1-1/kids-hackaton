package com.sber.rofles.feature.album

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sber.rofles.core.particles.ParticleField
import com.sber.rofles.core.particles.ParticleKind
import com.sber.rofles.core.particles.ParticleLayer
import com.sber.rofles.core.particles.particleKind
import com.sber.rofles.core.particles.rememberEffectProgress
import com.sber.rofles.core.prank.ElementChip
import com.sber.rofles.core.prank.PrankArt
import com.sber.rofles.core.prank.PrankBackTopBar
import com.sber.rofles.core.prank.PrankInfoRow
import com.sber.rofles.core.prank.PrankPalette
import com.sber.rofles.core.prank.PrankPrimaryButton
import com.sber.rofles.core.prank.PrankType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun ComboCardScreen(
    card: AlbumCard,
    texts: AlbumTexts,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sharing by remember { mutableStateOf(false) }
    val colorA = argbColor(card.colorA)
    val colorB = argbColor(card.colorB)
    val layers = remember(card.key) {
        listOf(
            ParticleLayer(card.elementA?.element?.particleKind ?: ParticleKind.SPARK, colorA, count = 18),
            ParticleLayer(card.elementB?.element?.particleKind ?: ParticleKind.SPARK, colorB, count = 18),
        )
    }
    val loop = rememberEffectProgress(4_000, loop = true)
    val float by rememberInfiniteTransition(label = "float").animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(1_800), RepeatMode.Reverse),
        label = "float",
    )

    Column(
        modifier
            .fillMaxSize()
            .background(PrankPalette.background)
            .navigationBarsPadding(),
    ) {
        PrankBackTopBar(title = texts.title, onBack = onBack)
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(PrankPalette.surface)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .widthIn(max = 400.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(colorA.copy(alpha = 0.22f), Color.White, colorB.copy(alpha = 0.22f)))),
                    contentAlignment = Alignment.Center,
                ) {
                    ParticleField(layers, progress = { loop.value }, fade = false, seed = card.key.id.hashCode(), modifier = Modifier.fillMaxSize())
                    Image(
                        painterResource(PrankArt.combo(card.key)),
                        contentDescription = card.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp)
                            .graphicsLayer { translationY = float * density },
                    )
                }
                Text(card.title, style = PrankType.headline.copy(fontSize = 28.sp, lineHeight = 32.sp), textAlign = TextAlign.Center)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    card.elementA?.let { ElementChip(it) }
                    Text("+", style = PrankType.title.copy(color = PrankPalette.secondary), modifier = Modifier.align(Alignment.CenterVertically))
                    card.elementB?.let { ElementChip(it) }
                }
            }
            PrankInfoRow(meetingText(card))
        }
        PrankPrimaryButton(
            text = texts.shareButton,
            enabled = !sharing,
            onClick = {
                sharing = true
                scope.launch {
                    runCatching {
                        val file = withContext(Dispatchers.IO) {
                            ShareCard.createFile(
                                context = context,
                                content = ShareContent(
                                    lead = texts.shareText,
                                    title = card.title,
                                    brand = texts.brand,
                                    colorA = card.colorA.toInt(),
                                    colorB = card.colorB.toInt(),
                                    art = PrankArt.combo(card.key),
                                    fileName = "combo_${card.key.id}.png",
                                ),
                            )
                        }
                        ShareCard.launch(context, file, texts.shareButton)
                    }
                    sharing = false
                }
            },
            modifier = Modifier.padding(16.dp),
        )
    }
}

private val dateFormat = DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru"))

private fun meetingText(card: AlbumCard): String {
    val times = highFiveCountText(card.count)
    if (card.firstAt <= 0) return times
    val date = Instant.ofEpochMilli(card.firstAt).atZone(ZoneId.systemDefault()).format(dateFormat)
    return "$times · впервые $date"
}

internal fun highFiveCountText(count: Int): String = "Вы дали пять $count ${timesWord(count)}"

internal fun timesWord(count: Int): String {
    val lastTwo = count % 100
    val last = count % 10
    return if (lastTwo !in 12..14 && last in 2..4) "раза" else "раз"
}
