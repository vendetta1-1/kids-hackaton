package com.sber.rofles.ui.shell.content

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sber.rofles.R
import com.sber.rofles.ui.shell.components.SkGutter
import com.sber.rofles.ui.theme.SberKids
import kotlin.math.PI
import kotlin.math.sin

private val ImageShape = RoundedCornerShape(12.dp)
private val ProgressColor = Color(0xFFB385FF)
private val CardTitleStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)

// ---------- Карусель-баннер ----------

/** Бесконечная лента: стартуем с середины огромного диапазона, страницу берём по модулю. */
private const val LOOP_PAGES = 10_000

@Composable
internal fun TopCarousel(items: List<TopBannerItem>, modifier: Modifier = Modifier) {
    if (items.isEmpty()) return
    val cardWidth = 296.dp
    // Стартуем с «Луаны» (индекс 1), чтобы по краям торчали соседи.
    val startPage = LOOP_PAGES / 2 - (LOOP_PAGES / 2) % items.size + 1
    val pagerState = rememberPagerState(initialPage = startPage) { LOOP_PAGES }
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val side = ((maxWidth - cardWidth) / 2).coerceAtLeast(0.dp)
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(cardWidth),
            contentPadding = PaddingValues(horizontal = side),
            pageSpacing = 16.dp,
            // Соседи уже собраны и картинки декодированы — без подтормаживания на первом свайпе.
            beyondViewportPageCount = 2,
            // Один свайп — ровно одна карточка, даже при сильном броске.
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(1),
            ),
            key = { it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
        ) { page ->
            TopBannerCard(items[page % items.size])
        }
    }
}

@Composable
private fun TopBannerCard(item: TopBannerItem) {
    Box(
        Modifier
            .size(width = 296.dp, height = 256.dp)
            .clip(SberKids.shapes.card)
            .clickable { },
    ) {
        Image(
            painterResource(item.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Затемнения сверху (под тег) и снизу (под заголовок), как в макете.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.4f),
                        0.23f to Color.Transparent,
                    ),
                ),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.6f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.55f),
                    ),
                ),
        )
        Row(
            Modifier.padding(start = 20.dp, top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.content_ic_play), null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(2.dp))
            Text("видео", style = SberKids.typography.caption, color = Color.White)
        }
        Text(
            item.title,
            style = TextStyle(fontSize = 24.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
        )
    }
}

// ---------- Истории ----------

@Composable
internal fun StoriesRow(items: List<StoryItem>, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = SkGutter),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(items) { _, story -> Story(story) }
    }
}

@Composable
private fun Story(story: StoryItem) {
    Box(Modifier.size(width = 80.dp, height = 112.dp)) {
        Box(
            Modifier
                .size(width = 80.dp, height = 100.dp)
                .clip(SberKids.shapes.card)
                .background(story.background ?: Color.Transparent)
                .clickable { },
        ) {
            Image(
                painterResource(story.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (story.viewed) {
                Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.6f)))
            }
        }
        Image(
            painterResource(story.tag),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(24.dp),
        )
    }
}

// ---------- Плеер «Эфир Звука» ----------

@Composable
internal fun StreamPlayerCard(modifier: Modifier = Modifier) {
    val colors = SberKids.colors
    var playing by remember { mutableStateOf(true) }
    Row(
        modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(SberKids.shapes.card)
            .background(colors.surface)
            .padding(20.dp),
    ) {
        Image(
            painterResource(R.drawable.content_player_cover),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f).fillMaxSize()) {
            Text("Эфир Звука", style = SberKids.typography.footnote, color = colors.textTertiary)
            Spacer(Modifier.height(4.dp))
            Text(
                "Три кота три хвоста",
                style = CardTitleStyle,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SoundWave(playing = playing, modifier = Modifier.weight(1f).height(16.dp))
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE4EBF0))
                        .clickable { playing = !playing },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(if (playing) R.drawable.content_ic_pause else R.drawable.content_ic_play),
                        contentDescription = if (playing) "Пауза" else "Играть",
                        tint = Color.Black,
                        modifier = Modifier.size(if (playing) 24.dp else 20.dp),
                    )
                }
            }
        }
    }
}

/** Волна эфира: синусоида с градиентом, «бежит», пока играет. */
@Composable
private fun SoundWave(playing: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phaseAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    val phase = if (playing) phaseAnim else 0f
    val brush = Brush.horizontalGradient(listOf(Color(0xFFB385FF), Color(0xFF8B5CF6)))
    Canvas(modifier) {
        val amp = if (playing) size.height * 0.18f else size.height * 0.06f
        val mid = size.height / 2
        val waveLen = 14.dp.toPx()
        val path = Path()
        var x = 0f
        path.moveTo(0f, mid)
        while (x <= size.width) {
            val y = mid + amp * sin(x / waveLen * 2 * PI.toFloat() - phase)
            path.lineTo(x, y)
            x += 1f
        }
        drawPath(path, brush, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
    }
}

// ---------- Чипы-фильтры ----------

@Composable
internal fun CategoryChips(
    items: List<ContentCategory>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = SkGutter, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(items) { index, item ->
            CategoryChip(item, selected = index == selected, onClick = { onSelect(index) })
        }
    }
}

@Composable
private fun CategoryChip(item: ContentCategory, selected: Boolean, onClick: () -> Unit) {
    val border by animateColorAsState(if (selected) Color.Black else Color.Transparent, label = "chipBorder")
    Row(
        Modifier
            .height(44.dp)
            .shadow(8.dp, CircleShape, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(CircleShape)
            .background(SberKids.colors.surface)
            .border(2.dp, border, CircleShape)
            .clickable(onClick = onClick)
            .padding(start = if (item.iconColor != null) 12.dp else 18.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.iconColor != null) {
            Box(
                Modifier
                    .size(20.dp)
                    .rotate(45f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(listOf(item.iconColor.copy(alpha = 0.55f), item.iconColor)),
                    ),
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(item.title, style = SberKids.typography.body, color = SberKids.colors.textPrimary)
    }
}

// ---------- Карточки контента ----------

@Composable
private fun KindLabel(kind: ContentKind) {
    val c = SberKids.colors.textSecondary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(kind.icon), null, tint = c, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(2.dp))
        Text(kind.label, style = SberKids.typography.footnote, color = c)
    }
}

@Composable
private fun LikesRow(item: ContentItem, modifier: Modifier = Modifier) {
    val c = SberKids.colors.textSecondary
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.content_ic_heart), null, tint = c, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(6.dp))
        Text(item.likes, style = SberKids.typography.footnote, color = c)
        Spacer(Modifier.weight(1f))
        if (item.progress != null) {
            Box(
                Modifier
                    .size(width = 74.dp, height = 6.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.1f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(item.progress)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(ProgressColor),
                )
            }
        }
    }
}

/** Большая карточка ленты 343: картинка 164, тип, заголовок, лайки и прогресс. */
@Composable
internal fun FeedCard(item: ContentItem, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(SberKids.shapes.card)
            .background(SberKids.colors.surface)
            .clickable { }
            .padding(16.dp),
    ) {
        Image(
            painterResource(item.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp)
                .clip(ImageShape),
        )
        Spacer(Modifier.height(8.dp))
        KindLabel(item.kind)
        Spacer(Modifier.height(10.dp))
        Text(
            item.title,
            style = CardTitleStyle,
            color = SberKids.colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(16.dp))
        LikesRow(item)
    }
}

/** Маленькая карточка 180×262 для горизонтальных подборок. */
@Composable
private fun SmallCard(item: ContentItem, modifier: Modifier = Modifier) {
    Column(
        modifier
            .size(width = 180.dp, height = 262.dp)
            .clip(SberKids.shapes.card)
            .background(SberKids.colors.surface)
            .clickable { }
            .padding(16.dp),
    ) {
        Image(
            painterResource(item.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(ImageShape),
        )
        Spacer(Modifier.height(8.dp))
        KindLabel(item.kind)
        Spacer(Modifier.height(10.dp))
        Text(
            item.title,
            style = CardTitleStyle,
            color = SberKids.colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        LikesRow(item)
    }
}

// ---------- Баннер «Исполни мечту» ----------

@Composable
internal fun DreamBanner(onClose: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(SberKids.shapes.card)
            .background(
                Brush.linearGradient(
                    0.09f to Color(0xFF0A3199),
                    1f to Color(0xFF010510),
                    start = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    end = Offset(0f, 0f),
                ),
            )
            .clickable { },
    ) {
        Image(
            painterResource(R.drawable.content_banner_planet),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-18).dp)
                .size(width = 114.dp, height = 140.dp),
        )
        Column(Modifier.padding(start = 24.dp, top = 26.dp, end = 130.dp)) {
            Text("Исполни мечту", style = SberKids.typography.navTitle, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(
                "Участвуй в конкурсе юных предпринимателей",
                style = SberKids.typography.footnote.copy(fontWeight = FontWeight.Normal),
                color = Color.White.copy(alpha = 0.8f),
            )
        }
        Icon(
            painterResource(R.drawable.sk_ic_cross),
            contentDescription = "Скрыть",
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(28.dp)
                .clip(CircleShape)
                .clickable(onClick = onClose)
                .padding(6.dp),
        )
        Text(
            "Реклама · sberbank.ru",
            style = SberKids.typography.caption,
            color = SberKids.colors.textTertiary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(topStart = 8.dp))
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

// ---------- Топ 5 за неделю ----------

@Composable
internal fun Top5Block(items: List<ContentItem>, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Text(
            "Топ 5 за неделю",
            style = SberKids.typography.headline,
            color = SberKids.colors.textPrimary,
            modifier = Modifier.padding(horizontal = SkGutter, vertical = 16.dp),
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(end = SkGutter),
        ) {
            itemsIndexed(items) { index, item ->
                // Ширина шага 204: карточка 180 + отступ 24, цифра заезжает на карточку слева.
                Box(Modifier.width(204.dp).height(278.dp)) {
                    SmallCard(item, Modifier.padding(start = 16.dp, top = 16.dp))
                    SprayNumber(index + 1, Modifier.offset(x = (-24).dp))
                }
            }
        }
    }
}

@Composable
private fun SprayNumber(number: Int, modifier: Modifier = Modifier) {
    val res = when (number) {
        1 -> R.drawable.content_spray_1
        2 -> R.drawable.content_spray_2
        else -> null
    }
    Box(modifier.size(width = 88.dp, height = 164.dp), contentAlignment = Alignment.Center) {
        if (res != null) {
            Image(painterResource(res), contentDescription = "$number место", modifier = Modifier.fillMaxSize())
        } else {
            // Для 3–5 в макете нет «граффити» — рисуем похожую цифру текстом.
            Text(
                "$number",
                style = TextStyle(
                    fontSize = 150.sp,
                    lineHeight = 150.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                ),
                color = Color(0xFF7B6CF6),
                modifier = Modifier.alpha(0.85f),
            )
        }
    }
}

// ---------- Алгоритмика ----------

@Composable
internal fun AlgorithmicsBlock(items: List<ContentItem>, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth()) {
        Column(
            Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                .background(Color(0xFFDDE6FF))
                .padding(bottom = 24.dp),
        ) {
            Text(
                "Алгоритмика",
                style = SberKids.typography.headline,
                color = SberKids.colors.textPrimary,
                modifier = Modifier.padding(start = SkGutter, top = 28.dp, bottom = 20.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = SkGutter),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(items) { _, item -> SmallCard(item) }
            }
        }
        Image(
            painterResource(R.drawable.content_algo_mascot),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(width = 148.dp, height = 126.dp),
        )
    }
}
