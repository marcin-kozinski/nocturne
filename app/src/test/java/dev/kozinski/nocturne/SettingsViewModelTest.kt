package dev.kozinski.nocturne

import org.junit.Assert.*
import org.junit.Test

class SettingsViewModelTest {
    @Test
    fun `returns expected name`() {
        val name = "Android"
        val viewModel = SettingsViewModel(name)
        assertEquals(name, viewModel.name)
    }
}
