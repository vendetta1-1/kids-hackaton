package com.sber.rofles.core.prank

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class PrankViewModelTest {

    @Test
    fun `exposes its controller`() {
        val controller = mockk<PrankController>()
        val vm = PrankViewModel(controller)
        assertEquals(controller, vm.controller)
    }
}