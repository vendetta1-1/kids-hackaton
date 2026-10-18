package com.sber.rofles.domain.repository

import com.sber.rofles.domain.model.AlbumEntry
import com.sber.rofles.domain.model.BleCapability
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.MyElement
import com.sber.rofles.domain.model.PrankConfig
import com.sber.rofles.domain.model.PrankSettings
import com.sber.rofles.domain.model.ShakeParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.util.UUID

interface PrankConfigRepository {
    val config: StateFlow<PrankConfig?>

    suspend fun refresh()
}

interface MyElementRepository {
    fun observe(): Flow<MyElement?>

    suspend fun save(element: MyElement)
}

interface AlbumRepository {
    fun observe(): Flow<List<AlbumEntry>>

    suspend fun record(key: ComboKey, now: Long): AlbumEntry
}

interface PrankSettingsRepository {
    val settings: Flow<PrankSettings>

    suspend fun setHighFiveEnabled(enabled: Boolean)

    suspend fun setVibrationEnabled(enabled: Boolean)

    suspend fun setSoundEnabled(enabled: Boolean)

    suspend fun setBluetoothOnboardingShown()
}

interface ShakeSource {
    fun shakes(params: ShakeParams): Flow<Long>
}

data class RawAdvert(val uuid: UUID, val rssi: Int)

interface HandshakeTransport {
    fun capability(): BleCapability

    fun scan(): Flow<RawAdvert>

    fun advertise(uuid: UUID)

    fun stop()
}

interface Clock {
    fun now(): Long

    fun today(): LocalDate
}

fun interface Random16 {
    fun next(): Int
}

fun interface AvatarProvider {
    fun index(): Int
}
