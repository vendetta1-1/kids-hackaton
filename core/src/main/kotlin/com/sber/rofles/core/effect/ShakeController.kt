package com.sber.rofles.core.effect

import kotlinx.coroutines.flow.Flow

/** Детектор тряски (сенсор). Реализация — в :data или :core. */
interface ShakeController {
    val shakes: Flow<Unit>
    fun start()
    fun stop()
}