package dev.kozinski.nocturne

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SettingsViewModelTest {
    private val viewModel = PlainSettingsViewModel()

    @Test
    fun `by default calendar is disabled`() {
        assertFalse(viewModel.calendarEnabled)
    }

    @Test
    fun `allows enabling calendar when permissions granted`() = runTest {
        viewModel.onEnableCalendarClicked(calendarPermissionsGranted = true)
        assertTrue(viewModel.calendarEnabled)
    }

    @Test
    fun `allows disabling calendar when permissions granted`() = runTest {
        viewModel.onEnableCalendarClicked(calendarPermissionsGranted = true)
        viewModel.onDisableCalendarClicked(calendarPermissionsGranted = true)
        assertFalse(viewModel.calendarEnabled)
    }

    @Test
    fun `doesn't allow enabling calendar when permissions denied`() = runTest {
        viewModel.onEnableCalendarClicked(calendarPermissionsGranted = false)
        assertFalse(viewModel.calendarEnabled)
    }

    @Test
    fun `doesn't allow disabling calendar if permissions were revoked`() = runTest {
        viewModel.onEnableCalendarClicked(calendarPermissionsGranted = true)
        viewModel.onDisableCalendarClicked(calendarPermissionsGranted = false)
        assertTrue(viewModel.calendarEnabled)
    }

    @Test
    fun `emits permission request event when permissions not granted`() = runTest {
        viewModel.events.test {
            viewModel.onEnableCalendarClicked(calendarPermissionsGranted = false)
            assertEquals(SettingsEvent.RequestCalendarPermissions, awaitItem())
        }
    }
}
