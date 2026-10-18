package com.sber.rofles.core.prank

import com.sber.rofles.domain.model.BleCapability
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle
import com.sber.rofles.domain.model.PrankEvent

data class EffectUi(val id: Long, val event: PrankEvent)

enum class ToastKind { CARD, BLUETOOTH }

data class ToastUi(
    val id: Long,
    val caption: String,
    val title: String,
    val combo: ComboKey?,
    val isNew: Boolean,
    val kind: ToastKind = ToastKind.CARD,
    val capability: BleCapability? = null,
)

sealed interface SheetUi {
    data class ElementPicker(val firstTime: Boolean) : SheetUi

    data class Bluetooth(val capability: BleCapability) : SheetUi
}

data class PickerUi(
    val elements: List<ElementStyle>,
    val current: Element?,
    val canChange: Boolean,
    val title: String,
    val subtitle: String,
    val done: String,
    val tomorrow: String,
)

data class BluetoothTexts(
    val title: String,
    val text: String,
    val enable: String,
    val later: String,
)
