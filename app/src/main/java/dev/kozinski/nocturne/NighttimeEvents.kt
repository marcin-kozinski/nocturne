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
    val lastDay = from.plusDays(days.toLong())

    while (currentDay < lastDay) {
        // Search for dusk as start time
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
                    val dusk = skylightDay.dusk
                    if (dusk != null) {
                        startTime = dusk
                    } else {
                        // Continue searching.
                        currentDay += Period.ofDays(1)
                    }
                }
            }
        }

        // Search for next dawn, starting from the date of the start time.
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
                        val dawn = skylightDay.dawn
                        if (dawn == null) {
                            // Continue searching.
                            currentDay += Period.ofDays(1)
                        } else if (dawn <= startTime) {
                            // Continue searching.
                            currentDay += Period.ofDays(1)
                        } else {
                            endTime = dawn
                        }
                    }
                }
            }

            // Fallback: if no dawn found, use end of last searched day
            if (endTime == null) {
                endTime = currentDay.atStartOfDay(ZoneId.systemDefault()).toInstant()
            }

            events += Event(title = "Dark", start = startTime, end = endTime)
        }
    }

    return events
}
