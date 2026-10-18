package com.sber.rofles.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.sber.rofles.core.effect.PrikolScreen
import com.sber.rofles.core.prank.PrankViewModel
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.feature.album.AlbumScreen
import com.sber.rofles.feature.demo.DemoScreen
import com.sber.rofles.feature.settings.SettingsScreen
import com.sber.rofles.ui.prank.PrankOverlayHost
import com.sber.rofles.ui.shell.components.SkMenuItem
import com.sber.rofles.ui.shell.components.SkTab
import com.sber.rofles.ui.shell.components.SkTabBar
import com.sber.rofles.ui.shell.content.ContentScreen
import com.sber.rofles.ui.shell.home.HomeScreen
import com.sber.rofles.ui.shell.piggy.PiggyBanksScreen
import com.sber.rofles.ui.shell.settings.SoundsScreen
import com.sber.rofles.ui.shell.tasks.TasksScreen
import com.sber.rofles.ui.shell.transfer.TransferScreen
import com.sber.rofles.ui.theme.SberKidsTheme

@Composable
fun RoflesAppContent(prank: PrankViewModel = viewModel()) {
    SberKidsTheme {
        val backStack = remember { mutableStateListOf(Route.MAIN) }
        val navigate: (Route) -> Unit = { backStack.add(it) }
        val back: () -> Unit = { if (backStack.size > 1) backStack.removeLastOrNull() }

        val selectTab: (SkTab) -> Unit = { tab ->
            backStack.clear()
            backStack.add(Route.MAIN)
            if (tab != SkTab.HOME) backStack.add(tab.route)
        }

        val avatarMenu = listOf(
            SkMenuItem("Мой альбом") { navigate(Route.ALBUM) },
            SkMenuItem("Моя стихия") { prank.controller.openElementPicker() },
        )

        val currentRoute = backStack.lastOrNull() ?: Route.MAIN
        var albumFocus by remember { mutableStateOf<String?>(null) }
        val openCard: (ComboKey?) -> Unit = { key ->
            albumFocus = key?.id
            if (backStack.lastOrNull() != Route.ALBUM) navigate(Route.ALBUM)
        }

        val logoTaps = remember { mutableListOf<Long>() }
        val onLogoTap: () -> Unit = {
            val now = System.currentTimeMillis()
            logoTaps.removeAll { now - it > SECRET_TAPS_WINDOW_MS }
            logoTaps += now
            if (logoTaps.size >= SECRET_TAPS) {
                logoTaps.clear()
                navigate(Route.DEMO)
            }
        }

        PrankOverlayHost(
            controller = prank.controller,
            screen = currentRoute.toPrikolScreen(),
            shakeEnabled = currentRoute != Route.DEMO,
            onOpenCard = openCard,
        ) {
            Box(Modifier.fillMaxSize()) {
                NavDisplay(
                    backStack = backStack,
                    onBack = back,
                    entryProvider = { key ->
                        when (key) {
                            Route.MAIN -> NavEntry(key) {
                                HomeScreen(
                                    onAvatarClick = { navigate(Route.SOUNDS) },
                                    onTransferClick = { navigate(Route.TRANSFER) },
                                    avatarMenu = avatarMenu,
                                )
                            }
                            Route.PIGGY_BANKS -> NavEntry(key) { PiggyBanksScreen(onAvatarClick = { navigate(Route.SOUNDS) }) }
                            Route.TASKS -> NavEntry(key) { TasksScreen(onAvatarClick = { navigate(Route.SOUNDS) }) }
                            Route.CONTENT -> NavEntry(key) { ContentScreen(onAvatarClick = { navigate(Route.SOUNDS) }) }
                            Route.TRANSFER -> NavEntry(key) { TransferScreen(onBack = back) }
                            Route.SOUNDS -> NavEntry(key) { SoundsScreen(onBack = back) }
                            Route.ALBUM -> NavEntry(key) {
                                AlbumScreen(
                                    onBack = back,
                                    focusCardId = albumFocus,
                                    onFocusConsumed = { albumFocus = null },
                                )
                            }
                            Route.SETTINGS -> NavEntry(key) { SettingsScreen() }
                            Route.DEMO -> NavEntry(key) { DemoScreen(onBack = back) }
                        }
                    },
                )

                val tab = currentRoute.tab
                AnimatedVisibility(
                    visible = tab != null,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    SkTabBar(selected = tab ?: SkTab.HOME, onSelect = selectTab, onAssistantClick = onLogoTap)
                }
            }
        }
    }
}

private const val SECRET_TAPS = 5
private const val SECRET_TAPS_WINDOW_MS = 2_000L

private fun Route.toPrikolScreen(): PrikolScreen = when (this) {
    Route.MAIN -> PrikolScreen.MAIN
    Route.PIGGY_BANKS -> PrikolScreen.PIGGYBANK
    Route.TASKS -> PrikolScreen.TASKS
    Route.CONTENT -> PrikolScreen.CONTENT
    Route.SOUNDS, Route.ALBUM, Route.SETTINGS -> PrikolScreen.PROFILE
    Route.TRANSFER -> PrikolScreen.MONEY_TRANSFER
    Route.DEMO -> PrikolScreen.MAIN
}
