package com.sber.rofles.feature.elementpicker

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sber.rofles.core.prank.PickerUi
import com.sber.rofles.core.prank.PrankArt
import com.sber.rofles.core.prank.PrankInfoRow
import com.sber.rofles.core.prank.PrankPalette
import com.sber.rofles.core.prank.PrankPrimaryButton
import com.sber.rofles.core.prank.PrankType
import com.sber.rofles.core.prank.toColor
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementPickerSheet(
    state: PickerUi,
    onSelect: (Element) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf(state.current) }
    val locked = !state.canChange && state.current != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PrankPalette.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(state.title, style = PrankType.headline)
                Text(state.subtitle, style = PrankType.secondary)
            }
            state.elements.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { style ->
                        ElementTile(
                            style = style,
                            selected = style.element == selected,
                            enabled = !locked || style.element == state.current,
                            onClick = { selected = style.element },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            if (locked) PrankInfoRow(state.tomorrow)
            val choice = selected
            PrankPrimaryButton(
                text = state.done,
                enabled = choice != null,
                onClick = {
                    if (choice != null) {
                        if (locked) onDismiss() else onSelect(choice)
                    }
                },
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ElementTile(
    style: ElementStyle,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = style.color.toColor()
    val border by animateColorAsState(if (selected) PrankPalette.green else Color.Transparent, label = "border")
    val scale by animateFloatAsState(if (selected) 1.08f else 1f, spring(dampingRatio = 0.45f), label = "scale")
    val shape = RoundedCornerShape(20.dp)
    Box(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(PrankPalette.surface)
                .border(2.dp, border, shape)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(top = 12.dp, bottom = 14.dp, start = 6.dp, end = 6.dp)
                .graphicsLayer { alpha = if (enabled) 1f else 0.4f },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                Modifier
                    .size(84.dp)
                    .background(
                        Brush.radialGradient(listOf(color.copy(alpha = if (selected) 0.32f else 0.16f), Color.Transparent)),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(PrankArt.element(style.element)),
                    contentDescription = style.title,
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                )
            }
            Text(style.title, style = PrankType.body, textAlign = TextAlign.Center, maxLines = 2)
        }
        if (selected) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(22.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(PrankArt.check), contentDescription = null, tint = PrankPalette.green, modifier = Modifier.size(22.dp))
            }
        }
    }
}
