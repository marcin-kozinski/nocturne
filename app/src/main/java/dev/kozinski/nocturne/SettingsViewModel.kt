package dev.kozinski.nocturne

import dev.drewhamilton.skylight.Coordinates
import dev.drewhamilton.skylight.SkylightDay
import dev.drewhamilton.skylight.calculator.CalculatorSkylight
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toKotlinInstant
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
                // Calculate sunset for Bialystok
                val skylight = CalculatorSkylight()
                val coordinates = Coordinates(latitude = 53.13528, longitude = 23.14556)
                val today = LocalDate.now()

                val sunsetTime =
                    when (val skylightDay = skylight.getSkylightDay(coordinates, today)) {
                        is SkylightDay.Eventful -> {
                            skylightDay.sunset?.toKotlinInstant()
                        }
                        else -> null
                    }

                if (sunsetTime != null) {

                    calendarRepository.addEvent(
                        Event(title = "Sunset", start = sunsetTime, end = sunsetTime + 1.minutes)
                    )
                }
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
