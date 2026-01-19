package dev.kozinski.nocturne

import dev.drewhamilton.skylight.Coordinates
import dev.drewhamilton.skylight.SkylightDay
import dev.drewhamilton.skylight.calculator.CalculatorSkylight
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
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
                var currentDay = LocalDate.now()
                val lastDay = currentDay.plusDays(180)

                while (currentDay <= lastDay) {
                    // Search for sunset as start time
                    var startTime: Instant? = null
                    while (startTime == null && currentDay <= lastDay) {
                        when (val skylightDay = skylight.getSkylightDay(coordinates, currentDay)) {
                            is SkylightDay.AlwaysDaytime -> {
                                // Continue searching.
                                currentDay += Period.ofDays(1)
                            }

                            is SkylightDay.NeverLight -> {
                                startTime =
                                    currentDay.atStartOfDay(ZoneId.systemDefault()).toInstant()
                            }

                            is SkylightDay.Eventful -> {
                                val sunset = skylightDay.sunset
                                if (sunset != null) {
                                    startTime = sunset
                                } else {
                                    // Continue searching.
                                    currentDay += Period.ofDays(1)
                                }
                            }
                        }
                    }

                    // Search for next sunrise, starting from the date of the start time.
                    var endTime: Instant? = null
                    if (startTime != null) {
                        while (endTime == null && currentDay <= lastDay) {
                            when (
                                val skylightDay = skylight.getSkylightDay(coordinates, currentDay)
                            ) {
                                is SkylightDay.AlwaysDaytime,
                                is SkylightDay.NeverLight -> {
                                    // Continue searching.
                                    currentDay += Period.ofDays(1)
                                }

                                is SkylightDay.Eventful -> {
                                    val sunrise = skylightDay.sunrise
                                    if (sunrise == null) {
                                        // Continue searching.
                                        currentDay += Period.ofDays(1)
                                    } else if (sunrise <= startTime) {
                                        // Continue searching.
                                        currentDay += Period.ofDays(1)
                                    } else {
                                        endTime = sunrise
                                    }
                                }
                            }
                        }

                        // Fallback: if no sunrise found, use end of last searched day
                        if (endTime == null) {
                            endTime = currentDay.atStartOfDay(ZoneId.systemDefault()).toInstant()
                        }

                        calendarRepository.addEvent(
                            Event(title = "Dark", start = startTime, end = endTime)
                        )
                    }
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
