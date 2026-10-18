package com.sber.rofles.ui.shell.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sber.rofles.R
import com.sber.rofles.ui.shell.components.SkBackTopBar
import com.sber.rofles.ui.shell.components.SkCell
import com.sber.rofles.ui.shell.components.SkCellGroup
import com.sber.rofles.ui.shell.components.SkGutter
import com.sber.rofles.ui.shell.components.SkScreenBackground
import com.sber.rofles.ui.shell.components.SkSwitch

/**
 * «Профиль → Звуки и вибрации» по макету + строка «Дай пять».
 * Пока переключатели держат только локальное состояние: связь с настройками прикола
 * подключим, когда будет готов сам прикол.
 */
@Composable
fun SoundsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sounds by rememberSaveable { mutableStateOf(true) }
    var vibration by rememberSaveable { mutableStateOf(true) }
    var highFive by rememberSaveable { mutableStateOf(true) }

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
                            onClick = { vibration = !vibration },
                            trailing = { SkSwitch(vibration, { vibration = it }) },
                        )
                    },
                    { shape ->
                        SkCell(
                            title = "Дай пять",
                            subtitle = "Тряхни телефон рядом с другом",
                            shape = shape,
                            icon = R.drawable.sk_ic_star,
                            onClick = { highFive = !highFive },
                            trailing = { SkSwitch(highFive, { highFive = it }) },
                        )
                    },
                ),
            )
        }
    }
}
