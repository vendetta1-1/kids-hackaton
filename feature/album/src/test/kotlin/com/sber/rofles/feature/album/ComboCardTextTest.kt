package com.sber.rofles.feature.album

import org.junit.Assert.assertEquals
import org.junit.Test

class ComboCardTextTest {

    @Test
    fun `timesWord singular`() {
        assertEquals("раз", timesWord(1))
        assertEquals("раз", timesWord(21))
        assertEquals("раз", timesWord(101))
    }

    @Test
    fun `timesWord plural 2-4`() {
        assertEquals("раза", timesWord(2))
        assertEquals("раза", timesWord(3))
        assertEquals("раза", timesWord(4))
        assertEquals("раза", timesWord(22))
        assertEquals("раза", timesWord(23))
        assertEquals("раза", timesWord(24))
        assertEquals("раза", timesWord(102))
    }

    @Test
    fun `timesWord exceptions 12-14`() {
        assertEquals("раз", timesWord(12))
        assertEquals("раз", timesWord(13))
        assertEquals("раз", timesWord(14))
        assertEquals("раз", timesWord(112))
        assertEquals("раз", timesWord(113))
        assertEquals("раз", timesWord(114))
    }

    @Test
    fun `highFiveCountText builds caption`() {
        assertEquals("Вы дали пять 1 раз", highFiveCountText(1))
        assertEquals("Вы дали пять 5 раз", highFiveCountText(5))
        assertEquals("Вы дали пять 22 раза", highFiveCountText(22))
    }
}