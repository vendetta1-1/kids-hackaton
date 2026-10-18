package com.sber.rofles.core.bluetooth

import kotlinx.coroutines.flow.Flow

/**
 * Bluetooth-рукопожатие «Дай пять»: два телефона трясутся ~3 с рядом,
 * обмениваются 8 байтами (стихия, номер аватарки, случайные числа).
 */
interface HighFiveCoordinator {
    /** Рядом ли телефон-друг, готовый к рукопожатию. */
    val peerPresent: Flow<Boolean>

    fun start()
    fun stop()
}