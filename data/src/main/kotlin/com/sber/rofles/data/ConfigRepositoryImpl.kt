package com.sber.rofles.data

import com.sber.rofles.domain.model.Combination
import com.sber.rofles.domain.model.Config
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle
import com.sber.rofles.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий конфига. Пока отдаёт дефолтный (включённый) конфиг.
 * Позже заменяется на загрузку JSON с CMS и анимаций с CDN.
 */
@Singleton
class ConfigRepositoryImpl @Inject constructor() : ConfigRepository {

    private val _config = MutableStateFlow(defaultConfig())

    override fun config(): Flow<Config> = _config

    override suspend fun refresh(): Result<Unit> = runCatching {
        // TODO: загрузить JSON с CMS, распарсить, положить в _config.
        _config.value = defaultConfig()
    }

    private fun defaultConfig(): Config = Config(
        elements = Element.entries.map { element ->
            ElementStyle(
                element = element,
                displayName = element.displayName,
                colorHex = elementColor(element),
                lottieUrl = "lottie/${element.name.lowercase()}.json",
            )
        },
        combinations = Combination.ALL,
        seasons = emptyList(),
        shakeThreshold = 25f,
        effectDurationMs = 3000L,
        enabled = true,
        texts = mapOf(
            "bluetooth_sheet" to "Чтобы телефоны узнали друг друга, нужен Bluetooth",
            "new_card" to "Новая карточка!",
        ),
    )

    private fun elementColor(element: Element): String = when (element) {
        Element.FIRE -> "#FF5722"
        Element.WATER -> "#2196F3"
        Element.ICE -> "#00BCD4"
        Element.AIR -> "#9E9E9E"
        Element.EARTH -> "#795548"
        Element.LIGHTNING -> "#FFEB3B"
        Element.MAGIC -> "#9C27B0"
    }
}