package dev.kozinski.nocturne

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import java.util.TimeZone
import kotlin.time.Instant

interface CalendarRepository {
    val calendarExists: State<Boolean>

    fun createCalendar(): Boolean

    fun addEvent(event: Event)

    fun deleteCalendar(): Boolean
}

data class Event(
    val title: String,
    val start: Instant,
    val end: Instant,
    val timeZone: TimeZone = TimeZone.getDefault(),
)

class ContentResolverCalendarRepository(private val contentResolver: ContentResolver) :
    CalendarRepository {

    private val calendarId = mutableStateOf<Long?>(null)
    override val calendarExists: State<Boolean> = derivedStateOf { calendarId.value != null }

    init {
        calendarId.value = findCalendarIdByName(CALENDAR_NAME)
    }

    override fun createCalendar(): Boolean {
        if (calendarId.value != null) return true

        val values =
            ContentValues().apply {
                put(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                put(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
                put(CalendarContract.Calendars.NAME, CALENDAR_NAME)
                put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_NAME)
                put(
                    CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                    CalendarContract.Calendars.CAL_ACCESS_OWNER,
                )
                put(CalendarContract.Calendars.OWNER_ACCOUNT, ACCOUNT_NAME)
                put(CalendarContract.Calendars.VISIBLE, 1)
                put(CalendarContract.Calendars.SYNC_EVENTS, 1)
                put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
            }

        return try {
            val uri =
                contentResolver.insert(
                    CalendarContract.Calendars.CONTENT_URI.asSyncAdapter(
                        ACCOUNT_NAME,
                        ACCOUNT_TYPE,
                    ),
                    values,
                )

            val newId = uri?.lastPathSegment?.toLongOrNull()
            if (newId != null) {
                calendarId.value = newId
                true
            } else {
                false
            }
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    override fun addEvent(event: Event) {
        val id =
            calendarId.value
                ?: throw IllegalStateException("Cannot add event: calendar does not exist")

        val eventValues =
            ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, id)
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DTSTART, event.start.toEpochMilliseconds())
                put(CalendarContract.Events.DTEND, event.end.toEpochMilliseconds())
                put(CalendarContract.Events.EVENT_TIMEZONE, event.timeZone.id)
            }

        contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues)
    }

    override fun deleteCalendar(): Boolean {
        val id = calendarId.value ?: return false

        return try {
            val deletedRows =
                contentResolver.delete(
                    ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, id)
                        .asSyncAdapter(ACCOUNT_NAME, ACCOUNT_TYPE),
                    null,
                    null,
                )

            if (deletedRows > 0) {
                calendarId.value = null
                true
            } else {
                false
            }
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun findCalendarIdByName(calendarName: String): Long? {
        val projection =
            arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE,
            )

        val selection =
            "${CalendarContract.Calendars.NAME} = ? AND " +
                "${CalendarContract.Calendars.ACCOUNT_TYPE} = ?"
        val selectionArgs = arrayOf(calendarName, ACCOUNT_TYPE)

        return try {
            contentResolver
                .query(
                    CalendarContract.Calendars.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                )
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                    } else {
                        null
                    }
                }
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun Uri.asSyncAdapter(account: String, accountType: String): Uri {
        return buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, accountType)
            .build()
    }

    companion object {
        private const val CALENDAR_NAME = "Nocturne"
        private const val ACCOUNT_NAME = "Nocturne"
        private const val ACCOUNT_TYPE = "dev.kozinski.nocturne"
    }
}
