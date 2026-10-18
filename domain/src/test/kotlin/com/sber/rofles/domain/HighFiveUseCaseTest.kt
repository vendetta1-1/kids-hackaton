package com.sber.rofles.domain

import com.sber.rofles.domain.handshake.HandshakeResult
import com.sber.rofles.domain.handshake.Peer
import com.sber.rofles.domain.model.AlbumEntry
import com.sber.rofles.domain.model.BleCapability
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.MyElement
import com.sber.rofles.domain.model.PrankEvent
import com.sber.rofles.domain.model.PrankSettings
import com.sber.rofles.domain.repository.AlbumRepository
import com.sber.rofles.domain.repository.AvatarProvider
import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.HandshakeTransport
import com.sber.rofles.domain.repository.MyElementRepository
import com.sber.rofles.domain.repository.PrankConfigRepository
import com.sber.rofles.domain.repository.PrankSettingsRepository
import com.sber.rofles.domain.repository.Random16
import com.sber.rofles.domain.usecase.Cooldown
import com.sber.rofles.domain.usecase.HighFiveUseCase
import com.sber.rofles.domain.usecase.RunHandshake
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HighFiveUseCaseTest {

    private val config = TestConfigs.config()
    private val configRepository = mockk<PrankConfigRepository>()
    private val settingsRepository = mockk<PrankSettingsRepository>()
    private val myElementRepository = mockk<MyElementRepository>()
    private val albumRepository = mockk<AlbumRepository>()
    private val transport = mockk<HandshakeTransport>()
    private val runHandshake = mockk<RunHandshake>()
    private val avatar = AvatarProvider { 3 }
    private val clock = mockk<Clock>()

    private val today = LocalDate.of(2026, 10, 18)

    private fun useCase(random: Random16 = Random16 { 7 }) = HighFiveUseCase(
        configRepository = configRepository,
        settingsRepository = settingsRepository,
        myElementRepository = myElementRepository,
        albumRepository = albumRepository,
        transport = transport,
        runHandshake = runHandshake,
        avatar = avatar,
        clock = clock,
        random = random,
        cooldown = Cooldown(),
    )

    private fun happyPath(peers: List<Peer> = listOf(Peer(200, Element.WATER.id, 4))) {
        every { configRepository.config } returns MutableStateFlow(config)
        every { settingsRepository.settings } returns flowOf(PrankSettings())
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today))
        every { clock.today() } returns today
        every { clock.now() } returns 10_000L
        every { transport.capability() } returns BleCapability.FULL
        coEvery { runHandshake.invoke(any(), any()) } returns HandshakeResult.Matched(peers)
    }

    @Test
    fun `no config produces no events`() = runTest {
        every { configRepository.config } returns MutableStateFlow(null)
        val events = useCase().invoke(shakeAt = 0).toList()
        assertTrue(events.isEmpty())
    }

    @Test
    fun `disabled config produces no events`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config.copy(enabled = false))
        val events = useCase().invoke(shakeAt = 0).toList()
        assertTrue(events.isEmpty())
    }

    @Test
    fun `high five disabled in settings produces no events`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config)
        every { settingsRepository.settings } returns flowOf(PrankSettings(highFiveEnabled = false))
        val events = useCase().invoke(shakeAt = 0).toList()
        assertTrue(events.isEmpty())
    }

    @Test
    fun `cooldown blocks a second shake`() = runTest {
        happyPath(peers = emptyList())
        val case = useCase()
        assertEquals(1, case.invoke(shakeAt = 0).toList().size)
        val second = case.invoke(shakeAt = 1).toList()
        assertTrue(second.isEmpty())
    }

    @Test
    fun `no selected element requires element picker`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config)
        every { settingsRepository.settings } returns flowOf(PrankSettings())
        every { myElementRepository.observe() } returns flowOf(null)
        val events = useCase().invoke(shakeAt = 0).toList()
        assertEquals(listOf<PrankEvent>(PrankEvent.NeedElement), events)
    }

    @Test
    fun `bluetooth unavailable emits solo then unavailable`() = runTest {
        happyPath()
        every { transport.capability() } returns BleCapability.OFF
        val events = useCase().invoke(shakeAt = 0).toList()
        assertEquals(2, events.size)
        assertTrue(events[0] is PrankEvent.Solo)
        assertEquals(PrankEvent.BluetoothUnavailable(BleCapability.OFF), events[1])
    }

    @Test
    fun `no matched peers stops after solo`() = runTest {
        happyPath(peers = emptyList())
        val events = useCase().invoke(shakeAt = 0).toList()
        assertEquals(1, events.size)
        assertTrue(events[0] is PrankEvent.Solo)
    }

    @Test
    fun `known combo emits high five and records album`() = runTest {
        happyPath()
        coEvery { albumRepository.record(any(), any()) } returns AlbumEntry(
            ComboKey.of(Element.FIRE, Element.WATER),
            firstAt = 10_000,
            count = 1,
        )
        val events = useCase().invoke(shakeAt = 0).toList()
        assertEquals(2, events.size)
        assertTrue(events[0] is PrankEvent.Solo)
        val highFive = events[1] as PrankEvent.HighFive
        assertEquals(ComboKey.of(Element.FIRE, Element.WATER), highFive.combo.key)
        assertTrue(highFive.isNew)
        assertEquals(1, highFive.count)
        coVerify { albumRepository.record(ComboKey.of(Element.FIRE, Element.WATER), 10_000L) }
    }

    @Test
    fun `repeated combo increments album count`() = runTest {
        happyPath()
        coEvery { albumRepository.record(any(), any()) } returns AlbumEntry(
            ComboKey.of(Element.FIRE, Element.WATER),
            firstAt = 5_000,
            count = 3,
        )
        val events = useCase().invoke(shakeAt = 0).toList()
        val highFive = events[1] as PrankEvent.HighFive
        assertTrue(!highFive.isNew)
        assertEquals(3, highFive.count)
    }

    @Test
    fun `unknown peer element emits friendship`() = runTest {
        happyPath(peers = listOf(Peer(200, 99, 4)))
        val events = useCase().invoke(shakeAt = 0).toList()
        val friendship = events[1] as PrankEvent.Friendship
        assertEquals("Дружба", friendship.title)
    }
}