package com.sber.rofles.prank

import com.sber.rofles.domain.setup.PrankOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PrankScreen {
    data class Album(val focusCardId: String? = null) : PrankScreen

    data object Service : PrankScreen
}

class PrankSetup internal constructor() {
    var configUrl: String = PrankOptions.DEFAULT_CONFIG_URL
    var avatarIndex: () -> Int = { 0 }
}

object Prank {

    private val screen = MutableStateFlow<PrankScreen?>(null)
    private val pickerRequests = MutableStateFlow(0L)

    internal val openScreen: StateFlow<PrankScreen?> = screen.asStateFlow()
    internal val elementPickerRequests: StateFlow<Long> = pickerRequests.asStateFlow()

    fun configure(block: PrankSetup.() -> Unit) {
        val setup = PrankSetup().apply(block)
        PrankOptions.configUrl = setup.configUrl
        PrankOptions.avatarIndex = setup.avatarIndex
    }

    fun openAlbum(cardId: String? = null) {
        screen.value = PrankScreen.Album(cardId)
    }

    fun openElementPicker() {
        pickerRequests.value += 1
    }

    fun openServiceScreen() {
        screen.value = PrankScreen.Service
    }

    fun close() {
        screen.value = null
    }

    internal fun consumeAlbumFocus() {
        val current = screen.value
        if (current is PrankScreen.Album && current.focusCardId != null) screen.value = PrankScreen.Album()
    }
}
