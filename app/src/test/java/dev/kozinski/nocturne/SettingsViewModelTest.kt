package dev.kozinski.nocturne

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SettingsViewModelTest {
    private val calendarRepository = FakeCalendarRepository()
    private val viewModel = PlainSettingsViewModel(calendarRepository)

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
    fun `adds an event when enabling calendar`() = runTest {
        viewModel.onEnableCalendarClicked(calendarPermissionsGranted = true)
        calendarRepository.assertAtLeastOneEventAdded()
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
    private val events = mutableListOf<Event>()
    final override val calendarExists: State<Boolean>
        field = mutableStateOf(false)

    override fun createCalendar(): Boolean {
        if (!calendarExists.value) {
            calendarExists.value = true
        }
        return true
    }

    override fun addEvent(event: Event) {
        events += event
    }

    override fun deleteCalendar(): Boolean {
        return if (calendarExists.value) {
            calendarExists.value = false
            true
        } else {
            false
        }
    }

    fun assertAtLeastOneEventAdded() = assertTrue(events.isNotEmpty())
}
