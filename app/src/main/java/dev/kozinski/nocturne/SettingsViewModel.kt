package dev.kozinski.nocturne

import dev.drewhamilton.skylight.Coordinates
import dev.drewhamilton.skylight.calculator.CalculatorSkylight
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface SettingsViewModel {
    val calendarEnabled: Boolean
    val events: SharedFlow<SettingsEvent>

    suspend fun onEnableCalendarClicked(calendarPermissionsGranted: Boolean)

    suspend fun onDisableCalendarClicked(calendarPermissionsGranted: Boolean)
}

sealed interface SettingsEvent {
    data object RequestCalendarPermissions : SettingsEvent
}

class PlainSettingsViewModel(
    private val calendarRepository: CalendarRepository,
    private val scheduleRefresh: () -> Unit,
    private val cancelRefresh: () -> Unit,
) : SettingsViewModel {
    override val calendarEnabled
        get() = calendarRepository.calendarExists.value

    private val _events = MutableSharedFlow<SettingsEvent>()
    override val events: SharedFlow<SettingsEvent>
        get() = _events

    override suspend fun onEnableCalendarClicked(calendarPermissionsGranted: Boolean) {
        if (calendarPermissionsGranted) {
            if (calendarRepository.createCalendar()) {
                val coordinates = Coordinates(latitude = 53.13528, longitude = 23.14556)
                val events =
                    calculateNighttimeEvents(CalculatorSkylight(), coordinates, LocalDate.now(), 1)
                events.forEach { calendarRepository.addEvent(it) }
                scheduleRefresh()
            }
        } else {
            _events.emit(SettingsEvent.RequestCalendarPermissions)
        }
    }

    override suspend fun onDisableCalendarClicked(calendarPermissionsGranted: Boolean) {
        if (calendarPermissionsGranted) {
            calendarRepository.deleteCalendar()
            cancelRefresh()
        } else {
            _events.emit(SettingsEvent.RequestCalendarPermissions)
        }
    }
}
