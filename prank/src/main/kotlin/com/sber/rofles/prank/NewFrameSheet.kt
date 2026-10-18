package com.sber.rofles.prank

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sber.rofles.core.prank.PrankArt
import com.sber.rofles.core.prank.PrankPalette
import com.sber.rofles.core.prank.PrankPrimaryButton
import com.sber.rofles.core.prank.PrankType
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewFrameSheet(style: ElementStyle, isMine: Boolean, onDone: () -> Unit) {
    val color = Color(style.color)
    ModalBottomSheet(
        onDismissRequest = onDone,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PrankPalette.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(180.dp)
                    .background(Brush.radialGradient(listOf(color.copy(alpha = 0.35f), Color.Transparent)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                ElementFrame(style = style, avatarSize = 112.dp) {
                    Box(
                        Modifier
                            .size(112.dp)
                            .background(PrankPalette.background, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(painterResource(PrankArt.highFive), contentDescription = null, modifier = Modifier.size(84.dp))
                    }
                }
            }
            Text(
                "Новая рамка!",
                style = PrankType.footnote.copy(color = PrankPalette.green),
                modifier = Modifier
                    .background(PrankPalette.greenSoft, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
            Text("Стихия «${style.title}» собрана", style = PrankType.headline, textAlign = TextAlign.Center)
            Text(
                if (isMine) {
                    "Ты нашёл все 6 карточек с этой стихией. Рамка теперь видна у аватара на Главном"
                } else {
                    "Ты нашёл все 6 карточек с этой стихией. Выбери её своей стихией, и рамка появится у аватара"
                },
                style = PrankType.secondary,
                textAlign = TextAlign.Center,
            )
            ElementRow(style)
            Spacer(Modifier.height(4.dp))
            PrankPrimaryButton("Круто!", onClick = onDone)
        }
    }
}

@Composable
private fun ElementRow(style: ElementStyle) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Element.entries.forEach { other ->
            Image(painterResource(PrankArt.combo(ComboKey.of(style.element, other))), contentDescription = null, modifier = Modifier.size(40.dp))
        }
    }
}
