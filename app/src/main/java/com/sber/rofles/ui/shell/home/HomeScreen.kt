package com.sber.rofles.ui.shell.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sber.rofles.R
import com.sber.rofles.ui.shell.components.SkAvatar
import com.sber.rofles.ui.shell.components.SkCard
import com.sber.rofles.ui.shell.components.SkCell
import com.sber.rofles.ui.shell.components.SkCellGap
import com.sber.rofles.ui.shell.components.SkCellGroup
import com.sber.rofles.ui.shell.components.SkGutter
import com.sber.rofles.ui.shell.components.SkIconButton
import com.sber.rofles.ui.shell.components.SkMenuItem
import com.sber.rofles.ui.shell.components.SkMoneyText
import com.sber.rofles.ui.shell.components.SkScreenBackground
import com.sber.rofles.ui.shell.components.SkTabBarInset
import com.sber.rofles.ui.shell.components.SkTabTopBar
import com.sber.rofles.ui.shell.components.skGroupShape
import com.sber.rofles.ui.theme.SberKids

private const val BALANCE_RUBLES = "800"
private const val BALANCE_KOPECKS = "55"
private const val SPASIBO_BONUSES = "123"

@Composable
fun HomeScreen(
    onAvatarClick: () -> Unit,
    onTransferClick: () -> Unit,
    avatarMenu: List<SkMenuItem>,
    modifier: Modifier = Modifier,
) {
    SkScreenBackground(modifier) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            SkTabTopBar(onAvatarClick = onAvatarClick, avatarMenu = avatarMenu) {
                SkIconButton(icon = R.drawable.sk_ic_qr, onClick = {}, tint = SberKids.colors.blue, contentDescription = "QR")
            }
            Column(
                Modifier.padding(horizontal = SkGutter),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CardWidget()
                TransferRow(onTransferClick = onTransferClick)
                PromoBanner()
                SkCellGroup(
                    items = listOf(
                        { shape -> SkCell("Пополнить мобилку", shape = shape, icon = R.drawable.sk_ic_mobile, onClick = {}) },
                        { shape -> SkCell("История", shape = shape, icon = R.drawable.sk_ic_clock, onClick = {}) },
                    ),
                )
            }
            Spacer(Modifier.height(SkTabBarInset))
        }
    }
}

@Composable
private fun CardWidget() {
    val colors = SberKids.colors
    val type = SberKids.typography
    Column(verticalArrangement = Arrangement.spacedBy(SkCellGap)) {
        SkCard(
            modifier = Modifier.fillMaxWidth(),
            shape = skGroupShape(0, 2),
            onClick = {},
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Детская карта", style = type.body.copy(fontSize = 17.sp), color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    SkMoneyText(BALANCE_RUBLES, BALANCE_KOPECKS)
                }
                Image(
                    painter = painterResource(R.drawable.sk_card_cat),
                    contentDescription = "Дизайн карты",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 88.dp, height = 72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }
        }
        SkCell(
            title = "Бонусы Спасибо",
            shape = skGroupShape(1, 2),
            onClick = {},
            trailing = {
                Text(SPASIBO_BONUSES, style = type.title, color = colors.textPrimary)
                Spacer(Modifier.width(8.dp))
                Icon(
                    painterResource(R.drawable.sk_ic_spasibo),
                    contentDescription = null,
                    tint = colors.green,
                    modifier = Modifier.size(20.dp),
                )
            },
        )
    }
}

@Composable
private fun TransferRow(onTransferClick: () -> Unit) {
    val colors = SberKids.colors
    val type = SberKids.typography
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SkCard(
            modifier = Modifier
                .weight(1f)
                .height(112.dp),
            onClick = onTransferClick,
        ) {
            Text(
                "Скинуть\nденег",
                style = type.body.copy(fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                modifier = Modifier.padding(start = 24.dp, top = 20.dp),
            )
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 16.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.blue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.sk_ic_arrow_right), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        SkCard(
            modifier = Modifier.size(width = 80.dp, height = 112.dp),
            onClick = onTransferClick,
        ) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                SkAvatar(size = 48.dp)
                Spacer(Modifier.height(8.dp))
                Text("Себе", style = type.footnote.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
            }
        }
    }
}

@Composable
private fun PromoBanner() {
    var visible by rememberSaveable { mutableStateOf(true) }
    AnimatedVisibility(visible) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(SberKids.shapes.card)
                .background(SberKids.gradients.banner)
                .clickable {},
        ) {
            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp, end = 130.dp),
            ) {
                Text(
                    "Узнай свои сильные стороны",
                    style = SberKids.typography.body.copy(fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Spacer(Modifier.height(2.dp))
                Text("с тестом по профориентации", style = SberKids.typography.footnote, color = Color.White.copy(alpha = 0.8f))
            }
            Image(
                painter = painterResource(R.drawable.sk_banner_kid),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp)
                    .size(width = 114.dp, height = 104.dp),
            )
            Text(
                "Реклама · sberbank.ru",
                style = SberKids.typography.caption.copy(fontSize = 9.sp),
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(topStart = 8.dp))
                    .background(Color(0x66000000))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
            Icon(
                painterResource(R.drawable.sk_ic_cross),
                contentDescription = "Закрыть",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { visible = false }
                    .padding(5.dp),
            )
        }
    }
}
