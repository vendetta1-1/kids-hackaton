package com.sber.rofles.ui.shell.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sber.rofles.R
import com.sber.rofles.ui.theme.SberKids

/** Вкладки нижнего меню СберKids. */
enum class SkTab(val title: String, @DrawableRes val icon: Int) {
    HOME("Главный", R.drawable.sk_ic_home),
    PIGGY_BANKS("Копилки", R.drawable.sk_ic_piggy),
    TASKS("Дела", R.drawable.sk_ic_check),
    CONTENT("Контент", R.drawable.sk_ic_star),
}

/**
 * Плавающее нижнее меню: белая плашка 88dp, радиус 24, четыре вкладки и лапка-ассистент по центру.
 * Активная вкладка — зелёная, с розовыми «штрихами» как в макете.
 */
@Composable
fun SkTabBar(
    selected: SkTab,
    onSelect: (SkTab) -> Unit,
    modifier: Modifier = Modifier,
    onAssistantClick: () -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .shadow(24.dp, SberKids.shapes.tabBar, ambientColor = Color(0x1F2B4A63), spotColor = Color(0x1F2B4A63))
            .clip(SberKids.shapes.tabBar)
            .background(SberKids.colors.surface)
            .height(88.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabItem(SkTab.HOME, selected, onSelect)
        TabItem(SkTab.PIGGY_BANKS, selected, onSelect)
        Image(
            painter = painterResource(R.drawable.sk_assistant_paw),
            contentDescription = "Ассистент",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(onClick = onAssistantClick),
        )
        TabItem(SkTab.TASKS, selected, onSelect)
        TabItem(SkTab.CONTENT, selected, onSelect)
    }
}

@Composable
private fun TabItem(tab: SkTab, selected: SkTab, onSelect: (SkTab) -> Unit) {
    val colors = SberKids.colors
    val active = tab == selected
    val tint = if (active) colors.green else colors.iconMono
    Column(
        Modifier
            .width(60.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onSelect(tab) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(width = 44.dp, height = 28.dp), contentAlignment = Alignment.Center) {
            if (active) {
                Icon(
                    painterResource(R.drawable.sk_tab_doodle),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(width = 44.dp, height = 26.dp)
                        .offset(y = (-2).dp),
                )
            }
            Icon(painterResource(tab.icon), contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(tab.title, style = SberKids.typography.caption, color = tint, maxLines = 1)
    }
}
