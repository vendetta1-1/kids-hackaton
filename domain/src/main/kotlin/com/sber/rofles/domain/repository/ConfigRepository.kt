package com.sber.rofles.domain.repository

import com.sber.rofles.domain.model.Config
import kotlinx.coroutines.flow.Flow

/** Порт для конфига прикола (CMS-ключ + CDN). Реализуется в :data. */
interface ConfigRepository {
    fun config(): Flow<Config>

    /** Перечитать конфиг (например, по кнопке на служебном экране). */
    suspend fun refresh(): Result<Unit>
}