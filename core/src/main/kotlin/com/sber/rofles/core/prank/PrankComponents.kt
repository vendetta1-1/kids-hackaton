package com.sber.rofles.core.prank

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sber.rofles.domain.model.ElementStyle

object PrankType {
    val headline = TextStyle(fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, color = PrankPalette.text)
    val title = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, color = PrankPalette.text)
    val body = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = PrankPalette.text)
    val secondary = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal, color = PrankPalette.secondary)
    val footnote = TextStyle(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, color = PrankPalette.tertiary)
}

@Composable
fun PrankPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(if (enabled) PrankPalette.primaryButton else Brush.linearGradient(listOf(PrankPalette.disabled, PrankPalette.disabled)))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = PrankType.title.copy(color = if (enabled) PrankPalette.text else PrankPalette.secondary))
    }
}

@Composable
fun PrankTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        style = PrankType.body.copy(color = PrankPalette.tertiary),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
fun PrankBackTopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .size(56.dp)
                .shadow(12.dp, PrankPalette.card, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
                .clip(PrankPalette.card)
                .background(PrankPalette.surface)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(PrankArt.back), contentDescription = "Назад", tint = PrankPalette.iconMono)
        }
        Text(title, style = PrankType.title, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 64.dp))
    }
}

@Composable
fun ElementChip(style: ElementStyle, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(CircleShape)
            .background(style.color.toColor().copy(alpha = 0.14f))
            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(PrankArt.element(style.element)), contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(6.dp))
        Text(style.title, style = PrankType.body)
    }
}

@Composable
fun PrankInfoRow(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(PrankPalette.card)
            .background(PrankPalette.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(PrankArt.clock), contentDescription = null, tint = PrankPalette.iconMono, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Column { Text(text, style = PrankType.body) }
    }
}
