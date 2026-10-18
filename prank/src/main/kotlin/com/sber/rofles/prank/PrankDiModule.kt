package com.sber.rofles.prank

import com.sber.rofles.domain.repository.AvatarProvider
import com.sber.rofles.domain.setup.PrankOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PrankDiModule {

    @Provides
    fun provideAvatar(): AvatarProvider = AvatarProvider { PrankOptions.avatarIndex() and 0xFF }
}
