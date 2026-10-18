package com.sber.rofles.core.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Заглушка Bluetooth-рукопожатия. Позже заменяется на реальный BLE-обмен
 * (advertising + scan в окне ~3 с). Пока сосед всегда «не найден».
 */
@Singleton
class BluetoothHighFiveCoordinator @Inject constructor() : HighFiveCoordinator {

    private val _peerPresent = MutableStateFlow(false)
    override val peerPresent: Flow<Boolean> = _peerPresent

    override fun start() = Unit
    override fun stop() = Unit
}