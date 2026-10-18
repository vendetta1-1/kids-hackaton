package com.sber.rofles.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.sber.rofles.core.effect.EffectOverlayHost
import com.sber.rofles.core.effect.PrikolScreen
import com.sber.rofles.feature.album.AlbumScreen
import com.sber.rofles.feature.demo.DemoScreen
import com.sber.rofles.feature.elementpicker.ElementPickerScreen
import com.sber.rofles.feature.highfive.HighFiveScreen
import com.sber.rofles.feature.settings.SettingsScreen
import com.sber.rofles.feature.solo.SoloEffectScreen
import com.sber.rofles.ui.theme.RoflesTheme
import kotlinx.coroutines.delay

/** Корневой UI: тема, навигация Nav3 и глобальный оверлей эффекта. */
@Composable
fun RoflesAppContent(viewModel: RoflesViewModel = viewModel()) {
    RoflesTheme {
        val effect by viewModel.effect.collectAsState()

        // Эффект живёт ~3 с, потом гасим.
        LaunchedEffect(effect) {
            if (effect != null) {
                delay(3000)
                viewModel.dismissEffect()
            }
        }

        val backStack = remember { mutableStateListOf<Route>(Route.MAIN) }

        // Сообщаем координатору текущий экран — чтобы ScreenGuard решал, где прикол работает.
        val currentRoute = backStack.lastOrNull() ?: Route.MAIN
        LaunchedEffect(currentRoute) {
            viewModel.setScreen(currentRoute.toPrikolScreen())
        }

        Box(Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = { key ->
                    when (key) {
                        Route.MAIN -> NavEntry(key) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Rofles — главный экран (заглушка)")
                            }
                        }

                        Route.SOLO -> NavEntry(key) { SoloEffectScreen() }
                        Route.HIGH_FIVE -> NavEntry(key) { HighFiveScreen() }
                        Route.ELEMENT_PICKER -> NavEntry(key) { ElementPickerScreen() }
                        Route.ALBUM -> NavEntry(key) { AlbumScreen() }
                        Route.SETTINGS -> NavEntry(key) { SettingsScreen() }
                        Route.DEMO -> NavEntry(key) { DemoScreen() }
                    }
                },
            )

            // Глобальный оверлей-эффект поверх навигации.
            effect?.let { current ->
                EffectOverlayHost(
                    animation = current.animationUrl,
                    onDone = { viewModel.dismissEffect() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/**
 * Сейчас все экраны прикола безопасны. Когда появятся денежные
 * (перевод/QR/деталка карты) — замапим их на PrikolScreen.MONEY_TRANSFER.
 */
private fun Route.toPrikolScreen(): PrikolScreen = when (this) {
    Route.MAIN, Route.SOLO, Route.HIGH_FIVE, Route.ELEMENT_PICKER,
    Route.ALBUM, Route.SETTINGS, Route.DEMO -> PrikolScreen.MAIN
}