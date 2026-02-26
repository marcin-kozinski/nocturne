package dev.kozinski.nocturne

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.drewhamilton.skylight.Coordinates
import dev.drewhamilton.skylight.calculator.CalculatorSkylight
import java.time.LocalDate

class RefreshCalendarEventsWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = ContentResolverCalendarRepository(applicationContext.contentResolver)
        if (!repository.calendarExists.value) return Result.success()

        val coordinates = Coordinates(latitude = 53.13528, longitude = 23.14556)
        val events = calculateNighttimeEvents(CalculatorSkylight(), coordinates, LocalDate.now(), 2)

        repository.clearEvents()
        events.forEach { repository.addEvent(it) }

        return Result.success()
    }
}
