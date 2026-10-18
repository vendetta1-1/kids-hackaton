package com.sber.rofles.ui.shell.settings

import com.sber.rofles.domain.model.PrankSettings
import com.sber.rofles.domain.repository.PrankSettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SoundsViewModelTest {

    private val repository = mockk<PrankSettingsRepository>()
    private val settingsFlow = MutableStateFlow(PrankSettings())

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { repository.settings } returns settingsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default settings exposed`() = runTest {
        val vm = SoundsViewModel(repository)
        assertEquals(PrankSettings(), vm.settings.value)
    }

    @Test
    fun `repository settings are surfaced`() = runTest {
        settingsFlow.value = PrankSettings(vibrationEnabled = false, highFiveEnabled = false, soundEnabled = true)
        val vm = SoundsViewModel(repository)
        assertFalse(vm.settings.value.vibrationEnabled)
        assertFalse(vm.settings.value.highFiveEnabled)
        assertTrue(vm.settings.value.soundEnabled)
    }

    @Test
    fun `setVibration delegates to repository`() = runTest {
        coEvery { repository.setVibrationEnabled(any()) } returns Unit
        val vm = SoundsViewModel(repository)
        vm.setVibration(true)
        coVerify { repository.setVibrationEnabled(true) }
    }

    @Test
    fun `setVibration false delegates to repository`() = runTest {
        coEvery { repository.setVibrationEnabled(any()) } returns Unit
        val vm = SoundsViewModel(repository)
        vm.setVibration(false)
        coVerify { repository.setVibrationEnabled(false) }
    }

    @Test
    fun `setHighFive delegates to repository`() = runTest {
        coEvery { repository.setHighFiveEnabled(any()) } returns Unit
        val vm = SoundsViewModel(repository)
        vm.setHighFive(true)
        coVerify { repository.setHighFiveEnabled(true) }
    }
}