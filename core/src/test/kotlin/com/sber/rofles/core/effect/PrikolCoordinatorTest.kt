package com.sber.rofles.core.effect

import com.sber.rofles.core.bluetooth.HighFiveCoordinator
import com.sber.rofles.core.bluetooth.PeerHandshake
import com.sber.rofles.domain.model.Combination
import com.sber.rofles.domain.model.Config
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle
import com.sber.rofles.domain.repository.ConfigRepository
import com.sber.rofles.domain.repository.HighFiveRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrikolCoordinatorTest {

    private val shakeController = mockk<ShakeController>()
    private val repository = mockk<HighFiveRepository>()
    private val configRepository = mockk<ConfigRepository>()
    private val highFive = mockk<HighFiveCoordinator>()

    private val enabledConfig = Config(
        elements = Element.entries.map {
            ElementStyle(element = it, displayName = it.name, colorHex = "#000", lottieUrl = "lottie/${it.name}.json")
        },
        combinations = Combination.ALL,
        seasons = emptyList(),
        shakeThreshold = 25f,
        effectDurationMs = 3000L,
        enabled = true,
        texts = emptyMap(),
    )

    private fun coordinator(): PrikolCoordinator =
        PrikolCoordinator(shakeController, repository, configRepository, highFive)

    @Test
    fun `solo effect when no peer nearby`() = runTest {
        every { configRepository.config() } returns flowOf(enabledConfig)
        every { repository.isEnabled() } returns flowOf(true)
        every { repository.currentElement() } returns flowOf(Element.FIRE)
        coEvery { highFive.shakeHands(any(), any()) } returns null

        val effect = coordinator()
            .also { it.currentScreen = PrikolScreen.MAIN }
            .processShake()

        assertEquals(EffectKind.SOLO, effect?.kind)
        assertEquals(Element.FIRE, effect?.element)
    }

    @Test
    fun `high five when peer present adds a card`() = runTest {
        every { configRepository.config() } returns flowOf(enabledConfig)
        every { repository.isEnabled() } returns flowOf(true)
        every { repository.currentElement() } returns flowOf(Element.FIRE)
        coEvery { highFive.shakeHands(any(), any()) } returns PeerHandshake(Element.WATER, 1, 42)
        coEvery { repository.addCard(any()) } returns Result.success(Unit)

        val effect = coordinator()
            .also { it.currentScreen = PrikolScreen.MAIN }
            .processShake()

        assertEquals(EffectKind.HIGH_FIVE, effect?.kind)
        assertEquals(Combination(Element.FIRE, Element.WATER), effect?.combination)
        coVerify { repository.addCard(Combination(Element.FIRE, Element.WATER)) }
    }

    @Test
    fun `no effect on a money screen`() = runTest {
        every { configRepository.config() } returns flowOf(enabledConfig)
        every { repository.isEnabled() } returns flowOf(true)
        every { repository.currentElement() } returns flowOf(Element.FIRE)

        val effect = coordinator()
            .also { it.currentScreen = PrikolScreen.MONEY_TRANSFER }
            .processShake()

        assertNull(effect)
    }

    @Test
    fun `no effect when prikol is disabled`() = runTest {
        every { configRepository.config() } returns flowOf(enabledConfig.copy(enabled = false))
        every { repository.isEnabled() } returns flowOf(true)
        every { repository.currentElement() } returns flowOf(Element.FIRE)

        val effect = coordinator()
            .also { it.currentScreen = PrikolScreen.MAIN }
            .processShake()

        assertNull(effect)
    }

    @Test
    fun `no effect when element is not chosen`() = runTest {
        every { configRepository.config() } returns flowOf(enabledConfig)
        every { repository.isEnabled() } returns flowOf(true)
        every { repository.currentElement() } returns flowOf(null)

        val effect = coordinator()
            .also { it.currentScreen = PrikolScreen.MAIN }
            .processShake()

        assertNull(effect)
    }
}