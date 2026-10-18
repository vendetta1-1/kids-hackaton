package com.sber.rofles.data.config

import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.ComboStyle
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle
import com.sber.rofles.domain.model.HandshakeParams
import com.sber.rofles.domain.model.PrankConfig
import com.sber.rofles.domain.model.Season
import com.sber.rofles.domain.model.ShakeParams
import com.sber.rofles.domain.model.Skin
import com.sber.rofles.domain.usecase.ConfigValidator
import org.json.JSONArray
import org.json.JSONObject
import java.time.MonthDay
import java.time.format.DateTimeFormatter

object ConfigParser {

    private val monthDay = DateTimeFormatter.ofPattern("MM-dd")

    fun parse(json: String): PrankConfig? = runCatching {
        val root = JSONObject(json)
        val shake = root.getJSONObject("shake")
        val handshake = root.getJSONObject("handshake")
        val skins = root.getJSONObject("skins").let { obj ->
            obj.keys().asSequence().associateWith { id -> parseSkin(id, obj.getJSONObject(id)) }
        }
        val config = PrankConfig(
            version = root.getInt("version"),
            enabled = root.optBoolean("enabled", false),
            shake = ShakeParams(
                thresholdG = shake.getDouble("thresholdG").toFloat(),
                peaks = shake.getInt("peaks"),
                windowMs = shake.getLong("windowMs"),
                cooldownMs = shake.getLong("cooldownMs"),
            ),
            handshake = HandshakeParams(
                durationMs = handshake.getLong("durationMs"),
                settleMs = handshake.getLong("settleMs"),
                rssiMin = handshake.getInt("rssiMin"),
                rssiSamples = handshake.getInt("rssiSamples"),
                echoRotateMs = handshake.optLong("echoRotateMs", 300),
            ),
            skins = skins,
            defaultSkin = root.getString("defaultSkin"),
            seasons = root.optJSONArray("seasons").objects().map { season ->
                Season(
                    from = MonthDay.parse(season.getString("from"), monthDay),
                    to = MonthDay.parse(season.getString("to"), monthDay),
                    skinId = season.getString("skin"),
                )
            },
            fallbackTitle = root.getJSONObject("fallback").getString("title"),
            texts = root.optJSONObject("texts")?.let { texts ->
                texts.keys().asSequence().associateWith { texts.getString(it) }
            } ?: emptyMap(),
        )
        ConfigValidator.validate(config)
    }.getOrNull()

    private fun parseSkin(id: String, obj: JSONObject): Skin {
        val elements = obj.getJSONArray("elements").objects().mapNotNull { e ->
            val element = Element.fromId(e.getInt("id")) ?: return@mapNotNull null
            element to ElementStyle(element, e.getString("title"), parseColor(e.getString("color")))
        }.toMap()
        val combos = obj.optJSONArray("combos").objects().mapNotNull { c ->
            val a = Element.fromId(c.getInt("a")) ?: return@mapNotNull null
            val b = Element.fromId(c.getInt("b")) ?: return@mapNotNull null
            val key = ComboKey.of(a, b)
            key to ComboStyle(key, c.getString("title"))
        }.toMap()
        return Skin(id, elements, combos)
    }

    private fun parseColor(value: String): Long {
        val hex = value.removePrefix("#")
        val argb = when (hex.length) {
            6 -> "FF$hex"
            8 -> hex
            else -> error("bad color $value")
        }
        return argb.toLong(16)
    }

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }
}
