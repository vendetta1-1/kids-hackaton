package com.sber.rofles.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CombinationTest {

    @Test
    fun `all contains exactly 21 combinations`() {
        assertEquals(21, Combination.ALL.size)
    }

    @Test
    fun `all ids are distinct and cover the full range`() {
        val ids = Combination.ALL.map { it.id }.sorted()
        assertEquals((0..20).toList(), ids)
    }

    @Test
    fun `order of elements does not matter for the id`() {
        val fireThenWater = Combination(Element.FIRE, Element.WATER)
        val waterThenFire = Combination(Element.WATER, Element.FIRE)
        assertEquals(fireThenWater.id, waterThenFire.id)
    }

    @Test
    fun `same pair in different order is a distinct data instance`() {
        val a = Combination(Element.FIRE, Element.WATER)
        val b = Combination(Element.WATER, Element.FIRE)
        assertNotEquals(a, b)
    }

    @Test
    fun `no combination contains a duplicated element`() {
        Combination.ALL.forEach { combo ->
            assertNotEquals(combo.first, combo.second)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `two identical elements are rejected`() {
        Combination(Element.FIRE, Element.FIRE)
    }

    @Test
    fun `every pair from ALL is unique`() {
        assertEquals(21, Combination.ALL.toSet().size)
    }
}