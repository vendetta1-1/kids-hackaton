package com.sber.rofles.di

import com.sber.rofles.domain.repository.AvatarProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val SHELL_AVATAR_INDEX = 1

    @Provides
    fun provideAvatar(): AvatarProvider = AvatarProvider { SHELL_AVATAR_INDEX }
}
