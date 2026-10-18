package com.sber.rofles.feature.highfive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sber.rofles.core.prank.PrankArt
import com.sber.rofles.core.prank.PrankPalette
import com.sber.rofles.core.prank.PrankType
import com.sber.rofles.core.prank.ToastKind
import com.sber.rofles.core.prank.ToastUi
import kotlinx.coroutines.delay

@Composable
fun PrankToast(
    toast: ToastUi,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    visibleMs: Long = 3_500,
) {
    var visible by remember(toast.id) { mutableStateOf(false) }
    LaunchedEffect(toast.id) {
        visible = true
        delay(visibleMs)
        visible = false
        delay(300)
        onDismiss()
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(20.dp, PrankPalette.card, ambientColor = Color(0x332B4A63), spotColor = Color(0x332B4A63))
                .clip(PrankPalette.card)
                .background(PrankPalette.surface)
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .background(
                        if (toast.isNew) PrankPalette.greenSoft else PrankPalette.background,
                        RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painterResource(
                        if (toast.kind == ToastKind.BLUETOOTH) {
                            PrankArt.bluetooth
                        } else {
                            toast.combo?.let(PrankArt::combo) ?: PrankArt.highFive
                        },
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    toast.caption,
                    style = PrankType.footnote.copy(color = if (toast.isNew) PrankPalette.green else PrankPalette.tertiary),
                )
                Text(toast.title, style = PrankType.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (onClick != null) {
                Text(
                    if (toast.kind == ToastKind.BLUETOOTH) "Включить" else "Открыть",
                    style = PrankType.footnote.copy(color = PrankPalette.green),
                    modifier = Modifier
                        .background(PrankPalette.greenSoft, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}
