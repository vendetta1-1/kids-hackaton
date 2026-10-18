package com.sber.rofles.core.prank

import com.sber.rofles.domain.repository.AlbumRepository
import com.sber.rofles.domain.repository.AvatarProvider
import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.HandshakeTransport
import com.sber.rofles.domain.repository.MyElementRepository
import com.sber.rofles.domain.repository.PrankConfigRepository
import com.sber.rofles.domain.repository.PrankSettingsRepository
import com.sber.rofles.domain.repository.Random16
import com.sber.rofles.domain.usecase.Cooldown
import com.sber.rofles.domain.usecase.HighFiveUseCase
import com.sber.rofles.domain.usecase.RunHandshake
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PrankModule {

    @Provides
    @Singleton
    fun provideHighFive(
        config: PrankConfigRepository,
        settings: PrankSettingsRepository,
        myElement: MyElementRepository,
        album: AlbumRepository,
        transport: HandshakeTransport,
        avatar: AvatarProvider,
        clock: Clock,
        random: Random16,
    ): HighFiveUseCase = HighFiveUseCase(
        configRepository = config,
        settingsRepository = settings,
        myElementRepository = myElement,
        albumRepository = album,
        transport = transport,
        runHandshake = RunHandshake(transport, clock),
        avatar = avatar,
        clock = clock,
        random = random,
        cooldown = Cooldown(),
    )
}
