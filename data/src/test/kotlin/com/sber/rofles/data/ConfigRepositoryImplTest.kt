package com.sber.rofles.data

import com.sber.rofles.domain.model.Element
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigRepositoryImplTest {

    @Test
    fun `default config is enabled and describes all seven elements`() = runTest {
        val repo = ConfigRepositoryImpl()
        val config = repo.config().first()

        assertTrue(config.enabled)
        assertEquals(7, config.elements.size)
        assertEquals(Element.entries, config.elements.map { it.element })
    }

    @Test
    fun `refresh keeps the config available`() = runTest {
        val repo = ConfigRepositoryImpl()
        repo.refresh()
        assertTrue(repo.config().first().enabled)
    }
}