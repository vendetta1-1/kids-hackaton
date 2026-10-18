package com.sber.rofles.domain.model

import java.time.MonthDay

data class ShakeParams(
    val thresholdG: Float,
    val peaks: Int,
    val windowMs: Long,
    val cooldownMs: Long,
)

data class HandshakeParams(
    val durationMs: Long,
    val settleMs: Long,
    val rssiMin: Int,
    val rssiSamples: Int,
    val echoRotateMs: Long = 300,
)

data class ElementStyle(
    val element: Element,
    val title: String,
    val color: Long,
)

data class ComboStyle(
    val key: ComboKey,
    val title: String,
)

data class Skin(
    val id: String,
    val elements: Map<Element, ElementStyle>,
    val combos: Map<ComboKey, ComboStyle>,
) {
    fun element(element: Element): ElementStyle = elements.getValue(element)

    fun combo(key: ComboKey): ComboStyle = combos.getValue(key)
}

data class Season(
    val from: MonthDay,
    val to: MonthDay,
    val skinId: String,
)

data class PrankConfig(
    val version: Int,
    val enabled: Boolean,
    val shake: ShakeParams,
    val handshake: HandshakeParams,
    val skins: Map<String, Skin>,
    val defaultSkin: String,
    val seasons: List<Season>,
    val fallbackTitle: String,
    val texts: Map<String, String>,
) {
    fun text(key: String, default: String): String = texts[key] ?: default
}
