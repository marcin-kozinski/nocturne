package dev.kozinski.nocturne

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsViewModelTest {
    @Test
    fun `allows enabling calendar when permissions granted`() {
        // Permissions have already been granted:
        val viewModel = PlainSettingsViewModel()
        assertEquals(false, viewModel.calendarEnabled)

        // Enabling the calendar should work:
        viewModel.setCalendarEnabled(true, calendarPermissionsGranted = true)
        assertEquals(true, viewModel.calendarEnabled)
    }

    @Test
    fun `doesn't allow enabling calendar when permissions denied`() {
        // Permissions haven't been granted:
        val viewModel = PlainSettingsViewModel()
        assertEquals(false, viewModel.calendarEnabled)

        // Enabling the calendar should NOT work:
        viewModel.setCalendarEnabled(true, calendarPermissionsGranted = false)
        assertEquals(false, viewModel.calendarEnabled)
    }

    @Test
    fun `doesn't allow disabling calendar if permissions were revoked`() {
        val viewModel = PlainSettingsViewModel()
        assertEquals(false, viewModel.calendarEnabled)

        // Enabling the calendar should work (permissions granted):
        viewModel.setCalendarEnabled(true, calendarPermissionsGranted = true)
        assertEquals(true, viewModel.calendarEnabled)

        // Disabling the calendar should NOT work (permissions revoked):
        viewModel.setCalendarEnabled(false, calendarPermissionsGranted = false)
        assertEquals(true, viewModel.calendarEnabled)
    }
}
