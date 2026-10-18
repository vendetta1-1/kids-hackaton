package com.sber.rofles.domain.usecase

import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.ComboStyle
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.HandshakeParams
import com.sber.rofles.domain.model.PrankConfig
import com.sber.rofles.domain.model.ShakeParams
import com.sber.rofles.domain.model.Skin

object ConfigValidator {

    fun validate(config: PrankConfig): PrankConfig? {
        if (config.fallbackTitle.isBlank()) return null
        val skins = config.skins.mapValues { (_, skin) -> fixSkin(skin, config.fallbackTitle) ?: return null }
        if (config.defaultSkin !in skins) return null
        return config.copy(
            shake = clamp(config.shake),
            handshake = clamp(config.handshake),
            skins = skins,
            seasons = config.seasons.filter { it.skinId in skins },
        )
    }

    private fun fixSkin(skin: Skin, fallbackTitle: String): Skin? {
        if (skin.elements.keys != Element.entries.toSet()) return null
        if (skin.elements.values.any { it.title.isBlank() }) return null
        val combos = ComboKey.ALL.associateWith { key ->
            skin.combos[key]?.takeIf { it.title.isNotBlank() } ?: ComboStyle(key, fallbackTitle)
        }
        return skin.copy(combos = combos)
    }

    private fun clamp(p: ShakeParams) = ShakeParams(
        thresholdG = p.thresholdG.coerceIn(1.3f, 6f),
        peaks = p.peaks.coerceIn(1, 6),
        windowMs = p.windowMs.coerceIn(200, 2_000),
        cooldownMs = p.cooldownMs.coerceIn(3_000, 30_000),
    )

    private fun clamp(p: HandshakeParams) = HandshakeParams(
        durationMs = p.durationMs.coerceIn(1_000, 8_000),
        settleMs = p.settleMs.coerceIn(200, 2_000),
        rssiMin = p.rssiMin.coerceIn(-100, -20),
        rssiSamples = p.rssiSamples.coerceIn(1, 10),
        echoRotateMs = p.echoRotateMs.coerceIn(100, 1_000),
    )
}
