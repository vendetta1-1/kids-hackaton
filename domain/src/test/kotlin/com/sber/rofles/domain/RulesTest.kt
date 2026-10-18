package com.sber.rofles.domain

import com.sber.rofles.domain.handshake.Peer
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.MyElement
import com.sber.rofles.domain.usecase.AlbumBook
import com.sber.rofles.domain.usecase.Cooldown
import com.sber.rofles.domain.usecase.ResolveCombo
import com.sber.rofles.domain.usecase.ResolveSkin
import com.sber.rofles.domain.usecase.Resolved
import com.sber.rofles.domain.usecase.SelectElement
import com.sber.rofles.domain.usecase.SelectResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RulesTest {

    @Test
    fun `combo key is symmetric`() {
        Element.entries.forEach { a ->
            Element.entries.forEach { b -> assertEquals(ComboKey.of(a, b), ComboKey.of(b, a)) }
        }
    }

    @Test
    fun `exactly 21 combos including twins`() {
        assertEquals(21, ComboKey.ALL.size)
        assertEquals(21, ComboKey.ALL.toSet().size)
        assertEquals(6, ComboKey.ALL.count { it.isTwins })
    }

    @Test
    fun `combo key id round trip`() {
        ComboKey.ALL.forEach { assertEquals(it, ComboKey.parse(it.id)) }
        assertEquals(null, ComboKey.parse("7-1"))
        assertEquals(null, ComboKey.parse("garbage"))
    }

    @Test
    fun `season crosses new year`() {
        val config = TestConfigs.config()
        assertEquals("newyear", ResolveSkin(config, LocalDate.of(2026, 12, 20)).id)
        assertEquals("newyear", ResolveSkin(config, LocalDate.of(2026, 12, 31)).id)
        assertEquals("newyear", ResolveSkin(config, LocalDate.of(2027, 1, 1)).id)
        assertEquals("newyear", ResolveSkin(config, LocalDate.of(2027, 1, 10)).id)
        assertEquals("base", ResolveSkin(config, LocalDate.of(2027, 1, 11)).id)
        assertEquals("base", ResolveSkin(config, LocalDate.of(2026, 12, 19)).id)
        assertEquals("base", ResolveSkin(config, LocalDate.of(2026, 7, 1)).id)
    }

    @Test
    fun `element can be changed once a day`() {
        val today = LocalDate.of(2026, 10, 18)
        val first = SelectElement(null, Element.FIRE, today)
        assertEquals(SelectResult.Ok(MyElement(Element.FIRE, today)), first)

        val current = MyElement(Element.FIRE, today)
        assertEquals(SelectResult.TooEarly(today.plusDays(1)), SelectElement(current, Element.WATER, today))
        assertEquals(SelectResult.Same, SelectElement(current, Element.FIRE, today))
        assertEquals(
            SelectResult.Ok(MyElement(Element.WATER, today.plusDays(1))),
            SelectElement(current, Element.WATER, today.plusDays(1)),
        )
        assertFalse(SelectElement.canChange(current, today))
        assertTrue(SelectElement.canChange(current, today.plusDays(1)))
    }

    @Test
    fun `album repeat increments count and keeps first date`() {
        val key = ComboKey.of(Element.WATER, Element.FIRE)
        val once = AlbumBook.record(emptyMap(), key, now = 10)
        assertEquals(1, once.getValue(key).count)
        val twice = AlbumBook.record(once, key, now = 20)
        assertEquals(2, twice.getValue(key).count)
        assertEquals(10L, twice.getValue(key).firstAt)
        assertEquals(1, twice.size)
    }

    @Test
    fun `combo resolves known friend and twins`() {
        val skin = TestConfigs.skin("base")
        val known = ResolveCombo(Element.FIRE, listOf(Peer(1, 2, 0)), skin, "Дружба") as Resolved.Known
        assertEquals(ComboKey.of(Element.FIRE, Element.WATER), known.combo.key)
        assertEquals(Element.WATER, known.friend.element)

        val twins = ResolveCombo(Element.ICE, listOf(Peer(1, 6, 0)), skin, "Дружба") as Resolved.Known
        assertTrue(twins.combo.key.isTwins)
    }

    @Test
    fun `unknown friend element gives friendship`() {
        val skin = TestConfigs.skin("base")
        assertEquals(Resolved.Unknown("Дружба"), ResolveCombo(Element.FIRE, listOf(Peer(1, 9, 0)), skin, "Дружба"))
    }

    @Test
    fun `cooldown ignores shakes that come too soon`() {
        val cooldown = Cooldown()
        assertTrue(cooldown.tryPass(0, 5_000))
        assertFalse(cooldown.tryPass(4_999, 5_000))
        assertTrue(cooldown.tryPass(5_000, 5_000))
    }
}
