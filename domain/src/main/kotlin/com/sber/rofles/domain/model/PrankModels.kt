package com.sber.rofles.domain.model

import java.time.LocalDate

data class AlbumEntry(
    val key: ComboKey,
    val firstAt: Long,
    val count: Int,
)

data class MyElement(
    val element: Element,
    val changedOn: LocalDate,
)

data class PrankSettings(
    val highFiveEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val bluetoothOnboardingShown: Boolean = false,
)

enum class BleCapability { FULL, NO_PERMISSION, OFF, UNSUPPORTED }

sealed interface PrankEvent {
    data class Solo(val style: ElementStyle) : PrankEvent

    data class HighFive(
        val combo: ComboStyle,
        val mine: ElementStyle,
        val friend: ElementStyle,
        val isNew: Boolean,
        val count: Int,
    ) : PrankEvent

    data class Friendship(val title: String, val mine: ElementStyle) : PrankEvent

    data object NeedElement : PrankEvent

    data class BluetoothUnavailable(val capability: BleCapability) : PrankEvent
}
