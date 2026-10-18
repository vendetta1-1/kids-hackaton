package com.sber.rofles.domain

import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.usecase.ConfigValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ConfigValidatorTest {

    @Test
    fun `valid config passes`() {
        assertNotNull(ConfigValidator.validate(TestConfigs.config()))
    }

    @Test
    fun `missing element rejects config`() {
        val skin = TestConfigs.skin("base").let { it.copy(elements = it.elements - Element.ICE) }
        assertNull(ConfigValidator.validate(TestConfigs.config(skins = mapOf("base" to skin))))
    }

    @Test
    fun `missing default skin rejects config`() {
        assertNull(ConfigValidator.validate(TestConfigs.config(defaultSkin = "summer")))
    }

    @Test
    fun `missing combos fall back to friendship`() {
        val skin = TestConfigs.skin("base", combos = emptyMap())
        val validated = ConfigValidator.validate(TestConfigs.config(skins = mapOf("base" to skin)))!!
        val combos = validated.skins.getValue("base").combos
        assertEquals(21, combos.size)
        assertEquals("Дружба", combos.getValue(ComboKey.of(Element.FIRE, Element.WATER)).title)
    }

    @Test
    fun `seasons pointing to unknown skins are dropped`() {
        val validated = ConfigValidator.validate(TestConfigs.config(skins = mapOf("base" to TestConfigs.skin("base"))))!!
        assertEquals(emptyList<Any>(), validated.seasons)
    }

    @Test
    fun `thresholds out of range are clamped`() {
        val config = TestConfigs.config().let {
            it.copy(
                shake = it.shake.copy(thresholdG = 100f, cooldownMs = 0),
                handshake = it.handshake.copy(rssiMin = 10, rssiSamples = 0),
            )
        }
        val validated = ConfigValidator.validate(config)!!
        assertEquals(6f, validated.shake.thresholdG)
        assertEquals(3_000L, validated.shake.cooldownMs)
        assertEquals(-20, validated.handshake.rssiMin)
        assertEquals(1, validated.handshake.rssiSamples)
    }
}
