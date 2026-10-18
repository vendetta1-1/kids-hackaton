package com.sber.rofles.core.bluetooth

import com.sber.rofles.domain.model.Element

/** Данные соседа, полученные при Bluetooth-рукопожатии «Дай пять». */
data class PeerHandshake(
    val peerElement: Element?,
    val peerAvatar: Int,
    val random: Int,
)

/**
 * Bluetooth-рукопожатие «Дай пять»: два телефона трясутся ~3 с рядом,
 * обмениваются 8 байтами (стихия, номер аватарки, случайные числа).
 */
interface HighFiveCoordinator {

    /**
     * Запустить окно рукопожатия: рекламировать себя и искать соседа.
     * Возвращает данные соседа или null, если за окно никто не найден.
     */
    suspend fun shakeHands(myElement: Element?, myAvatar: Int): PeerHandshake?

    fun start()
    fun stop()
}