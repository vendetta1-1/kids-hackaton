package com.sber.rofles.data

import com.sber.rofles.data.config.ConfigParser
import com.sber.rofles.domain.model.Element
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigParserTest {

    private fun baseJson(
        version: Int = 1,
        defaultSkin: String = "base",
        skinId: String = "base",
        elements: String = elementsJson(),
        combos: String = allCombosJson(),
    ) = """
        {
          "enabled": true,
          "version": $version,
          "shake": { "thresholdG": 2.3, "peaks": 2, "windowMs": 600, "cooldownMs": 5000 },
          "handshake": { "durationMs": 3000, "settleMs": 600, "rssiMin": -55, "rssiSamples": 3 },
          "defaultSkin": "$defaultSkin",
          "skins": {
            "$skinId": {
              "elements": $elements,
              "combos": $combos
            }
          },
          "seasons": [ { "from": "12-20", "to": "01-10", "skin": "$skinId" } ],
          "fallback": { "title": "Дружба" },
          "texts": { "pickerTitle": "Выбери свою стихию" }
        }
    """.trimIndent()

    private fun elementsJson() = """
        [
          { "id": 1, "title": "Огонь", "color": "#FF7A45" },
          { "id": 2, "title": "Вода", "color": "#3EA8FF" },
          { "id": 3, "title": "Земля", "color": "#5FBF4A" },
          { "id": 4, "title": "Воздух", "color": "#9AD8FF" },
          { "id": 5, "title": "Молния", "color": "#FFC93C" },
          { "id": 6, "title": "Лёд", "color": "#6FE3EA" }
        ]
    """.trimIndent()

    private fun allCombosJson(): String {
        val combos = mutableListOf<String>()
        Element.entries.forEach { a ->
            Element.entries.filter { it.id >= a.id }.forEach { b ->
                combos += """{ "a": ${a.id}, "b": ${b.id}, "title": "${a.name}-${b.name}" }"""
            }
        }
        return "[ ${combos.joinToString(", ")} ]"
    }

    @Test
    fun `valid config parses`() {
        val config = ConfigParser.parse(baseJson())
        assertNotNull(config)
        assertEquals(1, config!!.version)
        assertEquals(2.3f, config.shake.thresholdG)
        assertEquals(-55, config.handshake.rssiMin)
        assertEquals(1, config.seasons.size)
        assertEquals("Дружба", config.fallbackTitle)
        assertEquals("Выбери свою стихию", config.text("pickerTitle", "x"))
    }

    @Test
    fun `parses 8-digit hex colors`() {
        val json = baseJson(elements = """
            [
              { "id": 1, "title": "Огонь", "color": "#80FF7A45" },
              { "id": 2, "title": "Вода", "color": "#3EA8FF" },
              { "id": 3, "title": "Земля", "color": "#5FBF4A" },
              { "id": 4, "title": "Воздух", "color": "#9AD8FF" },
              { "id": 5, "title": "Молния", "color": "#FFC93C" },
              { "id": 6, "title": "Лёд", "color": "#6FE3EA" }
            ]
        """.trimIndent())
        val config = ConfigParser.parse(json)!!
        val fire = config.skins.getValue("base").elements.getValue(Element.FIRE)
        assertEquals(0x80FF7A45L, fire.color)
    }

    @Test
    fun `enabled defaults to false when absent`() {
        val json = baseJson().replace("\"enabled\": true,", "")
        val config = ConfigParser.parse(json)!!
        assertTrue(!config.enabled)
    }

    @Test
    fun `missing element rejects config`() {
        val json = baseJson(elements = """
            [
              { "id": 1, "title": "Огонь", "color": "#FF7A45" },
              { "id": 2, "title": "Вода", "color": "#3EA8FF" }
            ]
        """.trimIndent())
        assertNull(ConfigParser.parse(json))
    }

    @Test
    fun `invalid default skin rejects config`() {
        assertNull(ConfigParser.parse(baseJson(defaultSkin = "missing")))
    }

    @Test
    fun `garbage json returns null`() {
        assertNull(ConfigParser.parse("not json at all"))
        assertNull(ConfigParser.parse("{}"))
        assertNull(ConfigParser.parse(""))
    }

    @Test
    fun `values out of range are clamped`() {
        val json = baseJson().replace("\"thresholdG\": 2.3", "\"thresholdG\": 100")
            .replace("\"cooldownMs\": 5000", "\"cooldownMs\": 0")
            .replace("\"rssiMin\": -55", "\"rssiMin\": 10")
        val config = ConfigParser.parse(json)!!
        assertEquals(6f, config.shake.thresholdG)
        assertEquals(3_000L, config.shake.cooldownMs)
        assertEquals(-20, config.handshake.rssiMin)
    }
}