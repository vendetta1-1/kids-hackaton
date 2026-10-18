package com.sber.rofles.data

import com.sber.rofles.domain.model.Config
import com.sber.rofles.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Заглушка конфига. Позже заменяется на загрузку JSON с CMS и анимаций с CDN.
 * Сейчас возвращает конфиг с выключенным приколом (как битый конфиг).
 */
@Singleton
class ConfigRepositoryImpl @Inject constructor() : ConfigRepository {

    private val _config = MutableStateFlow(Config.Default)

    override fun config(): Flow<Config> = _config

    override suspend fun refresh(): Result<Unit> = runCatching {
        // TODO: загрузить JSON с CMS, распарсить, положить в _config.
    }
}