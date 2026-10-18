package com.sber.rofles.ui

import com.sber.rofles.ui.shell.components.SkTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoutesTest {

    @Test
    fun `tab routes map to their tab`() {
        assertEquals(SkTab.HOME, Route.MAIN.tab)
        assertEquals(SkTab.PIGGY_BANKS, Route.PIGGY_BANKS.tab)
        assertEquals(SkTab.TASKS, Route.TASKS.tab)
        assertEquals(SkTab.CONTENT, Route.CONTENT.tab)
    }

    @Test
    fun `detail routes have no tab`() {
        assertNull(Route.TRANSFER.tab)
        assertNull(Route.SOUNDS.tab)
        assertNull(Route.ALBUM.tab)
        assertNull(Route.SETTINGS.tab)
        assertNull(Route.DEMO.tab)
    }

    @Test
    fun `tabs map back to their route`() {
        assertEquals(Route.MAIN, SkTab.HOME.route)
        assertEquals(Route.PIGGY_BANKS, SkTab.PIGGY_BANKS.route)
        assertEquals(Route.TASKS, SkTab.TASKS.route)
        assertEquals(Route.CONTENT, SkTab.CONTENT.route)
    }

    @Test
    fun `tab to route round trip is identity`() {
        SkTab.entries.forEach { assertEquals(it, it.route.tab) }
    }

    @Test
    fun `every route has a tab or is a detail screen`() {
        val detail = setOf(Route.TRANSFER, Route.SOUNDS, Route.ALBUM, Route.SETTINGS, Route.DEMO)
        Route.entries.forEach { route ->
            if (route in detail) {
                assertNull(route.tab)
            } else {
                assertEquals(route, route.tab!!.route)
            }
        }
    }
}