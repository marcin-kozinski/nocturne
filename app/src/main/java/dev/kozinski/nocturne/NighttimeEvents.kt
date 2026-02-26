package dev.kozinski.nocturne

import dev.drewhamilton.skylight.Coordinates
import dev.drewhamilton.skylight.Skylight
import dev.drewhamilton.skylight.SkylightDay
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

suspend fun calculateNighttimeEvents(
    skylight: Skylight,
    coordinates: Coordinates,
    from: LocalDate,
    days: Int,
): List<Event> {
    val events = mutableListOf<Event>()
    var currentDay = from
    val lastDay = from.plusDays((days - 1).toLong())

    while (currentDay < lastDay) {
        // Search for sunset as start time
        var startTime: Instant? = null
        while (startTime == null && currentDay < lastDay) {
            when (val skylightDay = skylight.getSkylightDay(coordinates, currentDay)) {
                is SkylightDay.AlwaysDaytime -> {
                    // Continue searching.
                    currentDay += Period.ofDays(1)
                }

                is SkylightDay.NeverLight -> {
                    startTime = currentDay.atStartOfDay(ZoneId.systemDefault()).toInstant()
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
                when (val skylightDay = skylight.getSkylightDay(coordinates, currentDay)) {
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

            events += Event(title = "Dark", start = startTime, end = endTime)
        }
    }

    return events
}
