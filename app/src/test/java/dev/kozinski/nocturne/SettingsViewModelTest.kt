package dev.kozinski.nocturne

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SettingsViewModelTest {
    private val viewModel = PlainSettingsViewModel(FakeCalendarRepository())

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

class FakeCalendarRepository : CalendarRepository {
    private val calendarId = mutableStateOf<Long?>(null)
    override val calendarExists: State<Boolean> = derivedStateOf { calendarId.value != null }

    override fun createCalendar(): Boolean {
        if (calendarId.value == null) {
            val newId = System.currentTimeMillis()
            calendarId.value = newId
        }
        return true
    }

    override fun deleteCalendar(): Boolean {
        return if (calendarId.value != null) {
            calendarId.value = null
            true
        } else {
            false
        }
    }
}
