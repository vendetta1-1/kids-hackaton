package com.sber.rofles.core

import com.sber.rofles.core.bluetooth.BluetoothHighFiveCoordinator
import com.sber.rofles.core.bluetooth.HighFiveCoordinator
import com.sber.rofles.core.effect.ShakeController
import com.sber.rofles.core.effect.ShakeControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt-привязки модуля :core. */
@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    @Binds
    @Singleton
    abstract fun bindShakeController(impl: ShakeControllerImpl): ShakeController

    @Binds
    @Singleton
    abstract fun bindHighFiveCoordinator(impl: BluetoothHighFiveCoordinator): HighFiveCoordinator
}