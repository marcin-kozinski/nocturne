package dev.kozinski.nocturne

import org.junit.Assert.*
import org.junit.Test

class SettingsViewModelTest {
    @Test
    fun `keeps calendar enabled value`() {
        val viewModel = SettingsViewModel()
        assertEquals(false, viewModel.calendarEnabled)

        viewModel.calendarEnabled = true
        assertEquals(true, viewModel.calendarEnabled)
    }
}
