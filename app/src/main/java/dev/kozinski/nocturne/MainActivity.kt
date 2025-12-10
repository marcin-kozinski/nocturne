package dev.kozinski.nocturne

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.kozinski.nocturne.ui.theme.NocturneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { NocturneTheme { SettingsScreen(SettingsViewModel()) } }
    }
}

class SettingsViewModel {
    var calendarEnabled by mutableStateOf(false)
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {
            // ignore for now
        }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        )
    }

    SettingsScreen(
        calendarEnabled = viewModel.calendarEnabled,
        onCalendarEnabledChange = { viewModel.calendarEnabled = it },
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    calendarEnabled: Boolean,
    onCalendarEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(innerPadding).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(R.string.enable_calendar))
            Switch(checked = calendarEnabled, onCheckedChange = onCalendarEnabledChange)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    NocturneTheme { SettingsScreen(calendarEnabled = false, onCalendarEnabledChange = {}) }
}
