package dev.kozinski.nocturne

import kotlin.time.Duration.Companion.hours
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

class PlainSettingsViewModel(private val calendarRepository: CalendarRepository) :
    SettingsViewModel {
    override val calendarEnabled
        get() = calendarRepository.calendarExists.value

    private val _events = MutableSharedFlow<SettingsEvent>()
    override val events: SharedFlow<SettingsEvent>
        get() = _events

    override suspend fun onEnableCalendarClicked(calendarPermissionsGranted: Boolean) {
        if (calendarPermissionsGranted) {
            if (calendarRepository.createCalendar()) {
                val now = kotlin.time.Clock.System.now()
                calendarRepository.addEvent(
                    Event(
                        title = "Nocturne Calendar Created",
                        start = now,
                        end = now + 1.hours,
                        timeZone = java.util.TimeZone.getDefault(),
                    )
                )
            }
        } else {
            _events.emit(SettingsEvent.RequestCalendarPermissions)
        }
    }

    override suspend fun onDisableCalendarClicked(calendarPermissionsGranted: Boolean) {
        if (calendarPermissionsGranted) {
            calendarRepository.deleteCalendar()
        } else {
            _events.emit(SettingsEvent.RequestCalendarPermissions)
        }
    }
}
