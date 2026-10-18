package com.sber.rofles.ui.shell.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sber.rofles.ui.shell.components.SkGutter
import com.sber.rofles.ui.shell.components.SkScreenBackground
import com.sber.rofles.ui.shell.components.SkTabBarInset
import com.sber.rofles.ui.shell.components.SkTabTopBar

/** Вкладка «Контент» — лента: карусель, истории, эфир, фильтры, карточки и подборки. */
@Composable
fun ContentScreen(
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by rememberSaveable { mutableIntStateOf(0) }
    var bannerVisible by rememberSaveable { mutableStateOf(true) }
    val cardGap = 24.dp

    SkScreenBackground(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = SkTabBarInset),
        ) {
            item(key = "top") { SkTabTopBar(onAvatarClick = onAvatarClick) }
            item(key = "carousel") { TopCarousel(topBanners) }
            item(key = "stories") { StoriesRow(stories) }
            item(key = "player") {
                StreamPlayerCard(Modifier.padding(start = SkGutter, end = SkGutter, top = 16.dp))
            }
            item(key = "chips") {
                CategoryChips(
                    items = categories,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it },
                    modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
                )
            }
            items(feedTop) { card ->
                FeedCard(card, Modifier.padding(start = SkGutter, end = SkGutter, bottom = cardGap))
            }
            item(key = "banner") {
                AnimatedVisibility(visible = bannerVisible) {
                    DreamBanner(
                        onClose = { bannerVisible = false },
                        modifier = Modifier.padding(start = SkGutter, end = SkGutter, top = 18.dp, bottom = 16.dp),
                    )
                }
            }
            item(key = "top5") { Top5Block(top5) }
            item { Spacer(Modifier.height(cardGap)) }
            items(feedMiddle) { card ->
                FeedCard(card, Modifier.padding(start = SkGutter, end = SkGutter, bottom = 8.dp))
            }
            item(key = "algo") { AlgorithmicsBlock(algorithmics, Modifier.padding(bottom = 32.dp)) }
            items(feedBottom) { card ->
                FeedCard(card, Modifier.padding(start = SkGutter, end = SkGutter, bottom = cardGap))
            }
        }
    }
}
