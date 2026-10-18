package com.sber.rofles.feature.album

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sber.rofles.core.prank.PrankArt
import com.sber.rofles.core.prank.PrankBackTopBar
import com.sber.rofles.core.prank.PrankPalette
import com.sber.rofles.core.prank.PrankType

@Composable
fun AlbumScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AlbumViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = (state as? AlbumUiState.Ready)
        ?.cards
        ?.firstOrNull { it.key.id == selectedId && it.isOpen }

    BackHandler(enabled = selected != null) { selectedId = null }

    AnimatedContent(
        targetState = selected,
        contentKey = { it?.key?.id },
        transitionSpec = {
            (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.96f)) togetherWith fadeOut(tween(160))
        },
        modifier = modifier
            .fillMaxSize()
            .background(PrankPalette.background),
        label = "album-content",
    ) { card ->
        if (card != null) {
            ComboCardScreen(card = card, texts = state.texts, onBack = { selectedId = null })
        } else {
            AlbumGridScreen(state = state, onBack = onBack, onCardClick = { selectedId = it.key.id })
        }
    }
}

@Composable
private fun AlbumGridScreen(
    state: AlbumUiState,
    onBack: () -> Unit,
    onCardClick: (AlbumCard) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PrankPalette.background),
    ) {
        PrankBackTopBar(title = state.texts.title, onBack = onBack)
        when (state) {
            is AlbumUiState.Loading -> Box(Modifier.fillMaxSize())
            is AlbumUiState.Unavailable -> AlbumUnavailable()
            is AlbumUiState.Ready -> AlbumGrid(state = state, onCardClick = onCardClick)
        }
    }
}

@Composable
private fun AlbumGrid(state: AlbumUiState.Ready, onCardClick: (AlbumCard) -> Unit) {
    val navigationBars = WindowInsets.navigationBars.asPaddingValues()
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 24.dp + navigationBars.calculateBottomPadding(),
        ),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            AlbumProgress(opened = state.opened, total = state.total, caption = state.texts.progress)
        }
        items(state.cards, key = { it.key.id }) { card ->
            if (card.isOpen) OpenedCard(card = card, onClick = { onCardClick(card) }) else LockedCard(card)
        }
    }
}

@Composable
private fun AlbumProgress(opened: Int, total: Int, caption: String) {
    val fraction = if (total == 0) 0f else opened.toFloat() / total
    val animated by animateFloatAsState(fraction, tween(700), label = "album-progress")
    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(PrankPalette.card)
                .background(PrankPalette.surface)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$opened", style = PrankType.headline.copy(fontSize = 32.sp, lineHeight = 36.sp))
                Text(" из $total", style = PrankType.headline.copy(fontSize = 22.sp, color = PrankPalette.secondary))
            }
            Text(caption, style = PrankType.footnote.copy(color = PrankPalette.secondary))
            Box(
                Modifier
                    .padding(top = 16.dp, end = 120.dp)
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(PrankPalette.outline),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animated)
                        .clip(CircleShape)
                        .background(PrankPalette.green),
                )
            }
        }
        Image(
            painterResource(PrankArt.album),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp)
                .size(128.dp),
        )
    }
}

@Composable
private fun OpenedCard(card: AlbumCard, onClick: () -> Unit) {
    val colorA = argbColor(card.colorA)
    val colorB = argbColor(card.colorB)
    Column(
        Modifier
            .clip(PrankPalette.card)
            .background(PrankPalette.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(colorA.copy(alpha = 0.18f), colorB.copy(alpha = 0.18f)))),
        ) {
            Image(
                painterResource(PrankArt.combo(card.key)),
                contentDescription = card.title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
            )
            if (card.count > 1) {
                Text(
                    "×${card.count}",
                    style = PrankType.footnote.copy(color = PrankPalette.green, fontSize = 12.sp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(PrankPalette.surface, CircleShape)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
        }
        Text(
            card.title.replace("-", "-​"),
            style = PrankType.footnote.copy(color = PrankPalette.text, fontSize = 12.sp, lineHeight = 15.sp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
        )
    }
}

@Composable
private fun LockedCard(card: AlbumCard) {
    Column(
        Modifier
            .clip(PrankPalette.card)
            .background(PrankPalette.surface.copy(alpha = 0.6f))
            .padding(8.dp)
            .semantics { contentDescription = "Карточка ещё не открыта" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(PrankPalette.background),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(PrankArt.combo(card.key)),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color(0xFFDCE4EA)),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
            )
            Text("?", style = PrankType.headline.copy(color = PrankPalette.surface, fontSize = 30.sp))
        }
        Text(
            "Не найдена",
            style = PrankType.footnote.copy(color = PrankPalette.secondary, fontSize = 12.sp, lineHeight = 15.sp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
        )
    }
}

@Composable
private fun AlbumUnavailable() {
    Box(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(PrankPalette.card)
                .background(PrankPalette.surface)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(painterResource(PrankArt.album), contentDescription = null, modifier = Modifier.size(120.dp))
            Text("Альбом пока недоступен", style = PrankType.headline, textAlign = TextAlign.Center)
            Text("Загляни чуть позже, карточки уже в пути", style = PrankType.secondary, textAlign = TextAlign.Center)
        }
    }
}
