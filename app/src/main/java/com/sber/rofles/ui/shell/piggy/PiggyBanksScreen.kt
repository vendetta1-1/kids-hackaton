package com.sber.rofles.ui.shell.piggy

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sber.rofles.R
import com.sber.rofles.ui.shell.components.SkCell
import com.sber.rofles.ui.shell.components.SkGutter
import com.sber.rofles.ui.shell.components.SkIconButton
import com.sber.rofles.ui.shell.components.SkPrimaryButton
import com.sber.rofles.ui.shell.components.SkProgressBar
import com.sber.rofles.ui.shell.components.SkScreenBackground
import com.sber.rofles.ui.shell.components.SkTabBarInset
import com.sber.rofles.ui.shell.components.SkTabTopBar
import com.sber.rofles.ui.shell.components.skGroupShape
import com.sber.rofles.ui.theme.SberKids

private val CarouselCardBg = Color(0xFFE4EBF0)
private val DoneGreen = Color(0xFF19D13B)
private val PlusBlue = Color(0xFF4DB0FF)

private val SumStyle = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
private val CellTitleStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)

@Composable
fun PiggyBanksScreen(
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SkScreenBackground(modifier) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            SkTabTopBar(onAvatarClick = onAvatarClick) {
                SkIconButton(
                    icon = R.drawable.piggy_ic_plus,
                    tint = null,
                    onClick = {},
                    contentDescription = "Новая копилка",
                )
            }
            TotalBlock()
            Spacer(Modifier.height(40.dp))
            Column(
                Modifier.padding(horizontal = SkGutter),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PiggyBanksTop.forEach { PiggyBankCell(it) }
                DreamDoneCell()
                PiggyBanksBottom.forEach { PiggyBankCell(it) }
                Spacer(Modifier.height(16.dp))
                SkPrimaryButton(
                    text = "Создать копилку",
                    icon = R.drawable.piggy_ic_plus,
                    onClick = {},
                )
                Spacer(Modifier.height(16.dp))
            }
            OthersCarousel(Modifier.padding(horizontal = SkGutter))
            Spacer(Modifier.height(32.dp))
            SkCell(
                title = "Архив",
                icon = R.drawable.piggy_ic_archive,
                iconTint = null,
                modifier = Modifier.padding(horizontal = SkGutter),
                trailing = {
                    Text(PiggyArchiveCount, style = SberKids.typography.footnote, color = SberKids.colors.textSecondary)
                },
                onClick = {},
            )
            Spacer(Modifier.height(SkTabBarInset))
        }
    }
}

@Composable
private fun TotalBlock() {
    val colors = SberKids.colors
    val type = SberKids.typography
    Box(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .padding(top = 32.dp)
                .padding(horizontal = SkGutter),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(skGroupShape(0, 2))
                    .background(colors.surface)
                    .clickable {}
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    buildAnnotatedString {
                        append(PiggyTotalRubles)
                        pushStyle(SpanStyle(color = colors.textSecondary))
                        append(" ₽")
                        pop()
                    },
                    style = type.balance,
                    color = colors.textPrimary,
                )
                Text("Всего на копилках", style = type.footnote, color = colors.textSecondary)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(skGroupShape(1, 2))
                    .background(colors.surface)
                    .clickable {}
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Годовой процент", style = type.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
                Text(PiggyYearRate, style = SumStyle, color = colors.textPrimary)
            }
        }
        PiggyIllustration(Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun PiggyIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(width = 140.dp, height = 176.dp)
            .clipToBounds(),
    ) {
        Layer(R.drawable.piggy_coin_a, 45.8.dp, 44.dp, 20.34.dp, 20.88.dp)
        Layer(R.drawable.piggy_money_box, (-19).dp, 19.dp, 172.dp, 172.dp)
        Layer(R.drawable.piggy_coin_b, 84.06.dp, 18.89.dp, 35.73.dp, 33.75.dp)
        Layer(R.drawable.piggy_coin_c, 46.81.dp, 6.dp, 18.9.dp, 18.61.dp)
    }
}

@Composable
private fun Layer(res: Int, x: Dp, y: Dp, w: Dp, h: Dp) {
    Image(
        painterResource(res),
        contentDescription = null,
        modifier = Modifier
            .offset(x, y)
            .size(w, h),
    )
}

@Composable
private fun PiggyBankCell(bank: PiggyBank) {
    val colors = SberKids.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(SberKids.shapes.card)
            .background(colors.surface)
            .clickable {}
            .padding(start = 12.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PiggyImageModule(bank.image)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    buildAnnotatedString {
                        append(bank.rubles)
                        pushStyle(SpanStyle(color = colors.textSecondary))
                        if (bank.kopecks != null) append(",${bank.kopecks}")
                        append(" ₽")
                        pop()
                    },
                    style = SumStyle,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                if (bank.goal != null) {
                    Text(bank.goal, style = SberKids.typography.footnote, color = colors.textSecondary)
                }
            }
            if (bank.goal != null) {
                Spacer(Modifier.height(8.dp))
                SkProgressBar(bank.progress)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                bank.title,
                style = CellTitleStyle,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PiggyImageModule(image: PiggyImage) {
    when (image) {
        is PiggyImage.Icon -> Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(image.background)),
                contentAlignment = Alignment.Center,
            ) {
                if (image.tint != null) {
                    Icon(painterResource(image.icon), null, tint = Color(image.tint), modifier = Modifier.size(24.dp))
                } else {
                    Image(painterResource(image.icon), null)
                }
            }
        }
        is PiggyImage.Photo -> Image(
            painterResource(image.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(104.dp)
                .clip(RoundedCornerShape(20.dp)),
        )
    }
}

@Composable
private fun DreamDoneCell() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(SberKids.shapes.card)
            .background(DoneGreen)
            .clickable {},
    ) {
        Image(
            painterResource(R.drawable.piggy_doodle_sparks),
            contentDescription = null,
            modifier = Modifier
                .offset(16.dp, 15.dp)
                .size(100.dp),
        )
        Image(
            painterResource(R.drawable.piggy_doodle_rocket),
            contentDescription = null,
            modifier = Modifier
                .offset(284.dp, 19.dp)
                .size(48.dp, 97.dp),
        )
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painterResource(R.drawable.piggy_img_robot),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp)),
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Ура! Ты накопил на мечту",
                    style = SberKids.typography.title,
                    color = Color.White,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    PiggyDoneWithdraw,
                    style = SberKids.typography.footnote,
                    color = SberKids.colors.textPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SberKids.gradients.primaryButton)
                        .clickable {}
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun OthersCarousel(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(SberKids.shapes.card)
            .background(SberKids.colors.surface)
            .padding(vertical = 24.dp),
    ) {
        Text(
            "На что копят другие",
            style = SberKids.typography.headline,
            color = SberKids.colors.textPrimary,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(19.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(OthersGoals) { OthersGoalCard(it) }
        }
        Spacer(Modifier.height(24.dp))
        SkPrimaryButton(
            text = "Посмотреть все",
            onClick = {},
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun OthersGoalCard(goal: OthersGoal) {
    val colors = SberKids.colors
    Column(
        Modifier
            .size(width = 224.dp, height = 324.dp)
            .clip(SberKids.shapes.card)
            .background(CarouselCardBg)
            .clickable {}
            .padding(24.dp),
    ) {
        Image(
            painterResource(goal.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(176.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            goal.title,
            style = CellTitleStyle,
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .heightIn(min = 40.dp)
                .weight(1f),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                buildAnnotatedString {
                    append(goal.price)
                    pushStyle(SpanStyle(color = colors.textSecondary))
                    append(" ₽")
                    pop()
                },
                style = SberKids.typography.body.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PlusBlue)
                    .clickable {},
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.piggy_ic_plus),
                    contentDescription = "Добавить",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
