package com.sber.rofles.core.effect

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenGuardTest {

    @Test
    fun `money screens are never allowed`() {
        assertFalse(ScreenGuard.allows(PrikolScreen.MONEY_TRANSFER))
        assertFalse(ScreenGuard.allows(PrikolScreen.QR_PAYMENT))
        assertFalse(ScreenGuard.allows(PrikolScreen.CARD_DETAILS))
    }

    @Test
    fun `safe screens are allowed`() {
        assertTrue(ScreenGuard.allows(PrikolScreen.MAIN))
        assertTrue(ScreenGuard.allows(PrikolScreen.PROFILE))
        assertTrue(ScreenGuard.allows(PrikolScreen.PIGGYBANK))
        assertTrue(ScreenGuard.allows(PrikolScreen.TASKS))
    }
}