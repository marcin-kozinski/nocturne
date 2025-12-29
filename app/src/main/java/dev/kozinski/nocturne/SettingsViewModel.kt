package dev.kozinski.nocturne

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface SettingsViewModel {
    val calendarEnabled: Boolean
    val events: SharedFlow<SettingsEvent>

    suspend fun setCalendarEnabled(value: Boolean, calendarPermissionsGranted: Boolean)
}

sealed interface SettingsEvent {
    data object RequestCalendarPermissions : SettingsEvent
}

class PlainSettingsViewModel : SettingsViewModel {
    private val calendarEnabledState = mutableStateOf(false)
    override val calendarEnabled
        get() = calendarEnabledState.value

    private val _events = MutableSharedFlow<SettingsEvent>()
    override val events: SharedFlow<SettingsEvent>
        get() = _events

    override suspend fun setCalendarEnabled(value: Boolean, calendarPermissionsGranted: Boolean) {
        if (calendarPermissionsGranted) {
            calendarEnabledState.value = value
        } else {
            _events.emit(SettingsEvent.RequestCalendarPermissions)
        }
    }
}
