package com.sber.rofles.domain.model

/** Стиль одной стихии (название, цвет, анимация). */
data class ElementStyle(
    val element: Element,
    val displayName: String,
    val colorHex: String,
    val lottieUrl: String,
)

/** Сезонный скин с периодом действия. */
data class Season(
    val name: String,
    val startDate: String, // ISO-дата
    val endDate: String, // ISO-дата
    val skinUrl: String,
)

/** Конфиг прикола из CMS (JSON по ключу, анимации на CDN). */
data class Config(
    val elements: List<ElementStyle>,
    val combinations: List<Combination>,
    val seasons: List<Season>,
    val shakeThreshold: Float,
    val effectDurationMs: Long,
    val enabled: Boolean,
    val texts: Map<String, String>,
) {
    companion object {
        /** Дефолтный конфиг на случай битого/пустого — прикол выключен. */
        val Default = Config(
            elements = emptyList(),
            combinations = Combination.ALL,
            seasons = emptyList(),
            shakeThreshold = 25f,
            effectDurationMs = 3000L,
            enabled = false,
            texts = emptyMap(),
        )
    }
}