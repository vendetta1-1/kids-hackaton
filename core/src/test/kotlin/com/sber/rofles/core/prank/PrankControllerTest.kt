package com.sber.rofles.core.prank

import com.sber.rofles.domain.model.BleCapability
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.ComboStyle
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle
import com.sber.rofles.domain.model.HandshakeParams
import com.sber.rofles.domain.model.MyElement
import com.sber.rofles.domain.model.PrankConfig
import com.sber.rofles.domain.model.PrankEvent
import com.sber.rofles.domain.model.PrankSettings
import com.sber.rofles.domain.model.Season
import com.sber.rofles.domain.model.ShakeParams
import com.sber.rofles.domain.model.Skin
import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.MyElementRepository
import com.sber.rofles.domain.repository.PrankConfigRepository
import com.sber.rofles.domain.repository.PrankSettingsRepository
import com.sber.rofles.domain.repository.ShakeSource
import com.sber.rofles.domain.usecase.HighFiveUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.awaitCancellation
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PrankControllerTest {

    private val today = LocalDate.of(2026, 10, 18)

    private val configRepository = mockk<PrankConfigRepository>()
    private val settingsRepository = mockk<PrankSettingsRepository>()
    private val myElementRepository = mockk<MyElementRepository>()
    private val shakeSource = mockk<ShakeSource>()
    private val highFive = mockk<HighFiveUseCase>()
    private val haptics = mockk<Haptics>()
    private val clock = mockk<Clock>()

    private lateinit var controller: PrankController

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { clock.today() } returns today
        every { configRepository.config } returns MutableStateFlow(null)
        every { myElementRepository.observe() } returns flowOf(null)
        controller = PrankController(
            configRepository = configRepository,
            settingsRepository = settingsRepository,
            myElementRepository = myElementRepository,
            shakeSource = shakeSource,
            highFive = highFive,
            haptics = haptics,
            clock = clock,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun skin(id: String) = Skin(
        id = id,
        elements = Element.entries.associateWith { ElementStyle(it, "$id ${it.name}", 0xFF000000) },
        combos = ComboKey.ALL.associateWith { ComboStyle(it, "$id ${it.id}") },
    )

    private fun config(
        skins: Map<String, Skin> = mapOf("base" to skin("base")),
        defaultSkin: String = "base",
    ) = PrankConfig(
        version = 1,
        enabled = true,
        shake = ShakeParams(2.3f, 2, 600, 5_000),
        handshake = HandshakeParams(3_000, 600, -55, 3, 300),
        skins = skins,
        defaultSkin = defaultSkin,
        seasons = emptyList(),
        fallbackTitle = "Дружба",
        texts = emptyMap(),
    )

    // ---- onShake ----

    @Test
    fun `onShake collects high five events`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { settingsRepository.settings } returns flowOf(PrankSettings(vibrationEnabled = false))
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today))

        val solo = PrankEvent.Solo(skin("base").element(Element.FIRE))
        every { highFive.invoke(any()) } returns flowOf(solo)

        controller.onShake(1_000L)
        assertEquals(solo, controller.effect.value?.event)
    }

    @Test
    fun `onShake ignores when sheet is open`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        controller.openElementPicker()
        controller.onShake(1_000L)
        verify(exactly = 0) { highFive.invoke(any()) }
    }

    @Test
    fun `onShake ignores when shake job is active`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { settingsRepository.settings } returns flowOf(PrankSettings(vibrationEnabled = false))
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today))
        every { highFive.invoke(any()) } returns flow { awaitCancellation() }

        controller.onShake(1_000L)
        controller.onShake(1_100L)
        verify(exactly = 1) { highFive.invoke(any()) }
    }

    // ---- handle events ----

    @Test
    fun `high five event shows effect, toast and vibrates`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { settingsRepository.settings } returns flowOf(PrankSettings(vibrationEnabled = true))
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today))
        every { haptics.highFive() } returns Unit

        val skin = skin("base")
        val event = PrankEvent.HighFive(
            combo = ComboStyle(ComboKey.of(Element.FIRE, Element.WATER), "Дружба"),
            mine = skin.element(Element.FIRE),
            friend = skin.element(Element.WATER),
            isNew = true,
            count = 1,
        )
        every { highFive.invoke(any()) } returns flowOf(event)

        controller.onShake(1_000L)
        assertEquals(event, controller.effect.value?.event)
        val toast = controller.toast.value
        assertEquals("Новая карточка!", toast?.caption)
        assertEquals("Дружба", toast?.title)
        assertTrue(toast?.isNew == true)
        verify { haptics.highFive() }
    }

    @Test
    fun `bluetooth unavailable opens bluetooth sheet once`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { settingsRepository.settings } returns flowOf(PrankSettings(bluetoothOnboardingShown = false))
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today))

        val event = PrankEvent.BluetoothUnavailable(BleCapability.OFF)
        every { highFive.invoke(any()) } returns flowOf(event)

        controller.onShake(1_000L)
        assertEquals(SheetUi.Bluetooth(BleCapability.OFF), controller.sheet.value)
    }

    @Test
    fun `bluetooth unavailable ignored when onboarding already shown`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { settingsRepository.settings } returns flowOf(PrankSettings(bluetoothOnboardingShown = true))
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today))

        val event = PrankEvent.BluetoothUnavailable(BleCapability.OFF)
        every { highFive.invoke(any()) } returns flowOf(event)

        controller.onShake(1_000L)
        assertNull(controller.sheet.value)
    }

    @Test
    fun `need element opens element picker in first time mode`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { settingsRepository.settings } returns flowOf(PrankSettings())
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today))
        every { highFive.invoke(any()) } returns flowOf(PrankEvent.NeedElement)

        controller.onShake(1_000L)
        assertEquals(SheetUi.ElementPicker(firstTime = true), controller.sheet.value)
    }

    // ---- selectElement ----

    @Test
    fun `selectElement saves and closes picker when changed`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today.minusDays(1)))
        coEvery { myElementRepository.save(any()) } returns Unit

        controller.openElementPicker()
        controller.selectElement(Element.WATER)

        coVerify { myElementRepository.save(MyElement(Element.WATER, today)) }
        assertNull(controller.sheet.value)
    }

    @Test
    fun `selectElement same element just closes`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today))

        controller.openElementPicker()
        controller.selectElement(Element.FIRE)

        coVerify(exactly = 0) { myElementRepository.save(any()) }
        assertNull(controller.sheet.value)
    }

    @Test
    fun `selectElement too early does nothing`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today))

        controller.openElementPicker()
        controller.selectElement(Element.WATER)

        coVerify(exactly = 0) { myElementRepository.save(any()) }
        assertEquals(SheetUi.ElementPicker(firstTime = false), controller.sheet.value)
    }

    // ---- dismiss ----

    @Test
    fun `dismissEffect removes only matching id`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { settingsRepository.settings } returns flowOf(PrankSettings(vibrationEnabled = false))
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today))
        val solo = PrankEvent.Solo(skin("base").element(Element.FIRE))
        every { highFive.invoke(any()) } returns flowOf(solo)

        controller.onShake(1_000L)
        val id = controller.effect.value?.id!!
        controller.dismissEffect(id)
        assertNull(controller.effect.value)
    }

    @Test
    fun `dismissBluetoothSheet marks onboarding shown`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { settingsRepository.settings } returns flowOf(PrankSettings(bluetoothOnboardingShown = false))
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today))
        coEvery { settingsRepository.setBluetoothOnboardingShown() } returns Unit
        every { highFive.invoke(any()) } returns flowOf(PrankEvent.BluetoothUnavailable(BleCapability.NO_PERMISSION))

        controller.onShake(1_000L)
        assertEquals(SheetUi.Bluetooth(BleCapability.NO_PERMISSION), controller.sheet.value)

        controller.dismissSheet()
        assertNull(controller.sheet.value)
        coVerify { settingsRepository.setBluetoothOnboardingShown() }
    }

    // ---- picker state ----

    @Test
    fun `picker reflects current element and changeability`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        every { myElementRepository.observe() } returns flowOf(MyElement(Element.FIRE, today.minusDays(1)))

        val picker = controller.picker.value!!
        assertEquals(Element.FIRE, picker.current)
        assertTrue(picker.canChange)
        assertEquals(6, picker.elements.size)
        assertEquals("Выбери свою стихию", picker.title)
    }

    @Test
    fun `picker is null when config is missing`() = runTest {
        every { configRepository.config } returns MutableStateFlow(null)
        every { myElementRepository.observe() } returns flowOf(null)
        assertNull(controller.picker.value)
    }

    @Test
    fun `bluetooth texts fall back to defaults`() = runTest {
        every { configRepository.config } returns MutableStateFlow(config())
        assertEquals("Давай дадим пять!", controller.bluetoothTexts.value.title)
        assertEquals("Включить Bluetooth", controller.bluetoothTexts.value.enable)
    }
}