package com.sber.rofles.domain.usecase

import com.sber.rofles.domain.handshake.Peer
import com.sber.rofles.domain.model.AlbumEntry
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.ComboStyle
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle
import com.sber.rofles.domain.model.MyElement
import com.sber.rofles.domain.model.PrankConfig
import com.sber.rofles.domain.model.Skin
import java.time.LocalDate
import java.time.MonthDay

object ResolveSkin {
    operator fun invoke(config: PrankConfig, today: LocalDate): Skin {
        val day = MonthDay.from(today)
        val season = config.seasons.firstOrNull { season ->
            if (season.from <= season.to) {
                day >= season.from && day <= season.to
            } else {
                day >= season.from || day <= season.to
            }
        }
        return season?.let { config.skins[it.skinId] } ?: config.skins.getValue(config.defaultSkin)
    }
}

sealed interface Resolved {
    data class Known(val combo: ComboStyle, val friend: ElementStyle) : Resolved

    data class Unknown(val title: String) : Resolved
}

object ResolveCombo {
    operator fun invoke(mine: Element, peers: List<Peer>, skin: Skin, fallbackTitle: String): Resolved {
        val friend = peers.firstNotNullOfOrNull { Element.fromId(it.element) }
            ?: return Resolved.Unknown(fallbackTitle)
        val key = ComboKey.of(mine, friend)
        val combo = skin.combos[key] ?: ComboStyle(key, fallbackTitle)
        return Resolved.Known(combo, skin.element(friend))
    }
}

sealed interface SelectResult {
    data class Ok(val element: MyElement) : SelectResult

    data object Same : SelectResult

    data class TooEarly(val nextOn: LocalDate) : SelectResult
}

object SelectElement {
    operator fun invoke(current: MyElement?, wanted: Element, today: LocalDate): SelectResult = when {
        current == null -> SelectResult.Ok(MyElement(wanted, today))
        current.element == wanted -> SelectResult.Same
        current.changedOn >= today -> SelectResult.TooEarly(current.changedOn.plusDays(1))
        else -> SelectResult.Ok(MyElement(wanted, today))
    }

    fun canChange(current: MyElement?, today: LocalDate): Boolean =
        current == null || current.changedOn < today
}

object AlbumBook {
    fun record(entries: Map<ComboKey, AlbumEntry>, key: ComboKey, now: Long): Map<ComboKey, AlbumEntry> {
        val existing = entries[key]
        val updated = existing?.copy(count = existing.count + 1) ?: AlbumEntry(key, now, 1)
        return entries + (key to updated)
    }
}

class Cooldown {
    private var lastAt: Long? = null

    @Synchronized
    fun tryPass(at: Long, cooldownMs: Long): Boolean {
        val last = lastAt
        if (last != null && at - last < cooldownMs) return false
        lastAt = at
        return true
    }
}
