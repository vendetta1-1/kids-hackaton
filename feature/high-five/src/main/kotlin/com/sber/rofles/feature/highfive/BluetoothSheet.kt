package com.sber.rofles.feature.highfive

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sber.rofles.core.prank.BluetoothTexts
import com.sber.rofles.core.prank.PrankArt
import com.sber.rofles.core.prank.PrankPalette
import com.sber.rofles.core.prank.PrankPrimaryButton
import com.sber.rofles.core.prank.PrankTextButton
import com.sber.rofles.core.prank.PrankType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothSheet(
    texts: BluetoothTexts,
    onEnable: () -> Unit,
    onLater: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onLater,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PrankPalette.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painterResource(PrankArt.bluetooth),
                contentDescription = null,
                modifier = Modifier.size(180.dp),
            )
            Text(texts.title, style = PrankType.headline, textAlign = TextAlign.Center)
            Text(texts.text, style = PrankType.secondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            PrankPrimaryButton(texts.enable, onClick = onEnable)
            PrankTextButton(texts.later, onClick = onLater)
        }
    }
}
