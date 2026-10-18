package com.sber.rofles.ui.shell.transfer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sber.rofles.R
import com.sber.rofles.ui.shell.components.SkBackTopBar
import com.sber.rofles.ui.shell.components.SkCard
import com.sber.rofles.ui.shell.components.SkGutter
import com.sber.rofles.ui.shell.components.SkLimeButton
import com.sber.rofles.ui.shell.components.SkMoneyText
import com.sber.rofles.ui.shell.components.SkScreenBackground
import com.sber.rofles.ui.theme.SberKids

private data class Destination(val title: String, val subtitle: String, val badge: String, val color: Color)

private val destinations = listOf(
    Destination("СберКарта", "Основная", "С", Color(0xFF19D13B)),
    Destination("Другой банк", "СБП", "₽", Color(0xFF4DB0FF)),
)

@Composable
fun TransferScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SberKids.colors
    val type = SberKids.typography
    var amount by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableIntStateOf(0) }

    SkScreenBackground(modifier, blobs = false) {
        Column(
            Modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            SkBackTopBar(title = "Скинуть денег", onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SkGutter),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SkCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                        Text("СберКарта", style = type.footnote, color = colors.textSecondary)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painterResource(R.drawable.sk_card_cat),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(width = 32.dp, height = 22.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("•• 0420", style = type.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
                            SkMoneyText("800", "55", style = type.title)
                        }
                    }
                }

                SkCard(Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colors.background),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("М", style = type.body, color = colors.iconMono)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Мама", style = type.body, color = colors.textPrimary)
                                Text("+7 999 777-66-55", style = type.footnote, color = colors.textSecondary)
                            }
                        }
                        HorizontalDivider(thickness = 2.dp, color = colors.background)
                        Row(
                            Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            destinations.forEachIndexed { index, d ->
                                DestinationChip(
                                    d,
                                    selected = index == selected,
                                    onClick = { selected = index },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                SkCard(Modifier.fillMaxWidth()) {
                    BasicTextField(
                        value = amount,
                        onValueChange = { value -> amount = value.filter { it.isDigit() }.take(7) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = type.balance.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.green),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 22.dp),
                        decorationBox = { inner ->
                            if (amount.isEmpty()) {
                                Text("Сколько", style = type.balance, color = colors.textSecondary)
                            }
                            inner()
                        },
                    )
                }

                Row(
                    Modifier
                        .clip(SberKids.shapes.small)
                        .clickable {}
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.blue),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Добавить сообщение", style = type.body.copy(fontWeight = FontWeight.Medium), color = colors.blue)
                }
            }
            SkLimeButton(
                text = "Скинуть",
                onClick = onBack,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = SkGutter, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun DestinationChip(d: Destination, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SberKids.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .height(96.dp)
            .clip(shape)
            .background(colors.background)
            .border(2.dp, if (selected) colors.purple else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(d.color),
            contentAlignment = Alignment.Center,
        ) {
            Text(d.badge, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Column {
            Text(
                d.title,
                style = SberKids.typography.footnote.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
                maxLines = 1,
            )
            Text(d.subtitle, style = SberKids.typography.footnote, color = colors.textSecondary, maxLines = 1)
        }
    }
}
