package com.sber.rofles.feature.album

import com.sber.rofles.domain.model.AlbumEntry
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle
import com.sber.rofles.domain.model.HandshakeParams
import com.sber.rofles.domain.model.PrankConfig
import com.sber.rofles.domain.model.ShakeParams
import com.sber.rofles.domain.model.Skin
import com.sber.rofles.domain.repository.AlbumRepository
import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.PrankConfigRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelTest {

    private val albumRepository = mockk<AlbumRepository>()
    private val configRepository = mockk<PrankConfigRepository>()
    private val clock = mockk<Clock>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { clock.today() } returns LocalDate.of(2026, 10, 18)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun skin() = Skin(
        id = "base",
        elements = Element.entries.associateWith { ElementStyle(it, it.name, 0xFF000000) },
        combos = ComboKey.ALL.associateWith { key -> com.sber.rofles.domain.model.ComboStyle(key, "title-${key.id}") },
    )

    private fun config() = PrankConfig(
        version = 1,
        enabled = true,
        shake = ShakeParams(2.3f, 2, 600, 5_000),
        handshake = HandshakeParams(3_000, 600, -55, 3, 300),
        skins = mapOf("base" to skin()),
        defaultSkin = "base",
        seasons = emptyList(),
        fallbackTitle = "Дружба",
        texts = emptyMap(),
    )

    private fun viewModel() = AlbumViewModel(albumRepository, configRepository, clock)

    private fun TestScope.current(vm: AlbumViewModel): AlbumUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        return vm.state.value
    }

    @Test
    fun `unavailable when config missing`() = runTest {
        every { configRepository.config } returns MutableStateFlow(null)
        every { albumRepository.observe() } returns flowOf(emptyList())
        val state = current(viewModel())
        assertTrue(state is AlbumUiState.Unavailable)
        assertEquals("Мой альбом", state.texts.title)
    }

    @Test
    fun `ready builds 21 cards`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { albumRepository.observe() } returns flowOf(emptyList())
        val state = current(viewModel()) as AlbumUiState.Ready
        assertEquals(21, state.cards.size)
        assertEquals(0, state.opened)
        assertEquals(21, state.total)
        assertTrue(state.cards.all { !it.isOpen })
    }

    @Test
    fun `unlocked combo shows opened card`() = runTest {
        val key = ComboKey.of(Element.FIRE, Element.WATER)
        every { configRepository.config } returns MutableStateFlow(config())
        every { albumRepository.observe() } returns flowOf(
            listOf(AlbumEntry(key, firstAt = 1000L, count = 2)),
        )
        val state = current(viewModel()) as AlbumUiState.Ready
        val card = state.cards.single { it.key == key }
        assertTrue(card.isOpen)
        assertEquals(2, card.count)
        assertEquals(1, state.opened)
    }

    @Test
    fun `card carries element styles and colors`() = runTest {
        val key = ComboKey.of(Element.FIRE, Element.WATER)
        every { configRepository.config } returns MutableStateFlow(config())
        every { albumRepository.observe() } returns flowOf(listOf(AlbumEntry(key, firstAt = 1L, count = 1)))
        val state = current(viewModel()) as AlbumUiState.Ready
        val card = state.cards.single { it.key == key }
        assertEquals(Element.FIRE, card.elementA?.element)
        assertEquals(Element.WATER, card.elementB?.element)
        assertEquals(0xFF000000, card.colorA)
    }

    @Test
    fun `missing combo falls back to friendship title`() = runTest {
        val key = ComboKey.of(Element.FIRE, Element.ICE)
        val skin = skin().copy(combos = skin().combos - key)
        val cfg = config().copy(skins = mapOf("base" to skin))
        every { configRepository.config } returns MutableStateFlow(cfg)
        every { albumRepository.observe() } returns flowOf(emptyList())
        val state = current(viewModel()) as AlbumUiState.Ready
        val card = state.cards.single { it.key == key }
        assertEquals("Дружба", card.title)
    }

    @Test
    fun `ready applies custom texts`() = runTest {
        val cfg = config().copy(
            texts = mapOf(
                "albumTitle" to "Альбом",
                "shareText" to "Делимся",
                "shareButton" to "Поделиться",
                "shareBrand" to "Дай пять!",
                "albumProgress" to "собрано",
            ),
        )
        every { configRepository.config } returns MutableStateFlow(cfg)
        every { albumRepository.observe() } returns flowOf(emptyList())
        val state = current(viewModel()) as AlbumUiState.Ready
        assertEquals("Альбом", state.texts.title)
        assertEquals("Делимся", state.texts.shareText)
        assertEquals("собрано", state.texts.progress)
    }
}