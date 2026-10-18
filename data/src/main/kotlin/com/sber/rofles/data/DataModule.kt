package com.sber.rofles.data

import com.sber.rofles.data.ble.BleHandshakeTransport
import com.sber.rofles.data.config.PrankConfigRepositoryImpl
import com.sber.rofles.data.local.AlbumRepositoryImpl
import com.sber.rofles.data.local.MyElementRepositoryImpl
import com.sber.rofles.data.local.PrankProgressRepositoryImpl
import com.sber.rofles.data.local.PrankSettingsRepositoryImpl
import com.sber.rofles.data.sensor.ShakeSourceImpl
import com.sber.rofles.data.system.SecureRandom16
import com.sber.rofles.data.system.SystemClock
import com.sber.rofles.domain.repository.AlbumRepository
import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.HandshakeTransport
import com.sber.rofles.domain.repository.MyElementRepository
import com.sber.rofles.domain.repository.PrankConfigRepository
import com.sber.rofles.domain.repository.PrankProgressRepository
import com.sber.rofles.domain.repository.PrankSettingsRepository
import com.sber.rofles.domain.repository.Random16
import com.sber.rofles.domain.repository.ShakeSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindConfig(impl: PrankConfigRepositoryImpl): PrankConfigRepository

    @Binds
    abstract fun bindMyElement(impl: MyElementRepositoryImpl): MyElementRepository

    @Binds
    abstract fun bindAlbum(impl: AlbumRepositoryImpl): AlbumRepository

    @Binds
    abstract fun bindSettings(impl: PrankSettingsRepositoryImpl): PrankSettingsRepository

    @Binds
    abstract fun bindProgress(impl: PrankProgressRepositoryImpl): PrankProgressRepository

    @Binds
    abstract fun bindShakeSource(impl: ShakeSourceImpl): ShakeSource

    @Binds
    abstract fun bindTransport(impl: BleHandshakeTransport): HandshakeTransport

    @Binds
    abstract fun bindClock(impl: SystemClock): Clock

    @Binds
    abstract fun bindRandom(impl: SecureRandom16): Random16
}
