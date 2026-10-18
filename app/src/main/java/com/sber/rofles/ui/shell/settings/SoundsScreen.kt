package com.sber.rofles.ui.shell.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sber.rofles.R
import com.sber.rofles.ui.shell.components.SkBackTopBar
import com.sber.rofles.ui.shell.components.SkCell
import com.sber.rofles.ui.shell.components.SkCellGroup
import com.sber.rofles.ui.shell.components.SkGutter
import com.sber.rofles.ui.shell.components.SkScreenBackground
import com.sber.rofles.ui.shell.components.SkSwitch

@Composable
fun SoundsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SoundsViewModel = viewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    var sounds by rememberSaveable { mutableStateOf(true) }

    SkScreenBackground(modifier, blobs = false) {
        Column(Modifier.fillMaxSize()) {
            SkBackTopBar(title = "Звуки и вибрации", onBack = onBack)
            SkCellGroup(
                modifier = Modifier.padding(horizontal = SkGutter, vertical = 8.dp),
                items = listOf(
                    { shape ->
                        SkCell(
                            title = "Звуки",
                            shape = shape,
                            icon = R.drawable.sk_ic_vol,
                            onClick = { sounds = !sounds },
                            trailing = { SkSwitch(sounds, { sounds = it }) },
                        )
                    },
                    { shape ->
                        SkCell(
                            title = "Вибрации",
                            shape = shape,
                            icon = R.drawable.sk_ic_vib,
                            onClick = { viewModel.setVibration(!settings.vibrationEnabled) },
                            trailing = { SkSwitch(settings.vibrationEnabled, viewModel::setVibration) },
                        )
                    },
                    { shape ->
                        SkCell(
                            title = "Дай пять",
                            subtitle = "Тряхни телефон рядом с другом",
                            shape = shape,
                            icon = R.drawable.sk_ic_star,
                            onClick = { viewModel.setHighFive(!settings.highFiveEnabled) },
                            trailing = { SkSwitch(settings.highFiveEnabled, viewModel::setHighFive) },
                        )
                    },
                ),
            )
        }
    }
}
