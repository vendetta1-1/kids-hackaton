package com.sber.rofles.domain

import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.ComboStyle
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle
import com.sber.rofles.domain.model.HandshakeParams
import com.sber.rofles.domain.model.PrankConfig
import com.sber.rofles.domain.model.Season
import com.sber.rofles.domain.model.ShakeParams
import com.sber.rofles.domain.model.Skin
import java.time.MonthDay

object TestConfigs {
    val handshake = HandshakeParams(durationMs = 3_000, settleMs = 600, rssiMin = -55, rssiSamples = 3)

    fun skin(id: String, combos: Map<ComboKey, ComboStyle> = ComboKey.ALL.associateWith { ComboStyle(it, "$id ${it.id}") }) = Skin(
        id = id,
        elements = Element.entries.associateWith { ElementStyle(it, "$id ${it.name}", 0xFF000000) },
        combos = combos,
    )

    fun config(
        skins: Map<String, Skin> = mapOf("base" to skin("base"), "newyear" to skin("newyear")),
        defaultSkin: String = "base",
    ) = PrankConfig(
        version = 1,
        enabled = true,
        shake = ShakeParams(thresholdG = 2.3f, peaks = 2, windowMs = 600, cooldownMs = 5_000),
        handshake = handshake,
        skins = skins,
        defaultSkin = defaultSkin,
        seasons = listOf(Season(MonthDay.of(12, 20), MonthDay.of(1, 10), "newyear")),
        fallbackTitle = "Дружба",
        texts = emptyMap(),
    )
}
