package com.sber.rofles.data

import com.sber.rofles.domain.repository.ConfigRepository
import com.sber.rofles.domain.repository.HighFiveRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt-привязки модуля :data к портам домена. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindHighFiveRepository(impl: HighFiveRepositoryImpl): HighFiveRepository

    @Binds
    @Singleton
    abstract fun bindConfigRepository(impl: ConfigRepositoryImpl): ConfigRepository
}