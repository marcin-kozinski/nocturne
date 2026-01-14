package dev.kozinski.nocturne

import dev.drewhamilton.skylight.Coordinates
import dev.drewhamilton.skylight.SkylightDay
import dev.drewhamilton.skylight.calculator.CalculatorSkylight
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Instant
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
                // Calculate nighttime period for Bialystok
                val skylight = CalculatorSkylight()
                val coordinates = Coordinates(latitude = 53.13528, longitude = 23.14556)
                val today = LocalDate.now()

                val startTime: Instant? =
                    when (val todaySkylightDay = skylight.getSkylightDay(coordinates, today)) {
                        is SkylightDay.AlwaysDaytime -> {
                            // Don't create event.
                            null
                        }

                        is SkylightDay.NeverLight -> {
                            today.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant()
                        }
                        is SkylightDay.Eventful -> {
                            // Intentionally null if no sunset, doesn't create event.
                            todaySkylightDay.sunset?.toKotlinInstant()
                        }
                    }

                // Search for next sunrise.
                var endTime: Instant? = null
                if (startTime != null) {
                    val maxDaysToSearch = 365

                    for (i in 0..maxDaysToSearch) {
                        val searchDate = today.plusDays(i.toLong())
                        when (val skylightDay = skylight.getSkylightDay(coordinates, searchDate)) {
                            is SkylightDay.AlwaysDaytime,
                            is SkylightDay.NeverLight -> {
                                // Continue searching.
                            }
                            is SkylightDay.Eventful -> {
                                val sunrise = skylightDay.sunrise?.toKotlinInstant()
                                if (sunrise == null) {
                                    // Continue searching.
                                } else if (sunrise <= startTime) {
                                    // Continue searching.
                                } else {
                                    endTime = sunrise
                                    break
                                }
                            }
                        }
                    }
                }

                if (startTime != null && endTime != null) {
                    calendarRepository.addEvent(
                        Event(title = "Dark", start = startTime, end = endTime)
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
