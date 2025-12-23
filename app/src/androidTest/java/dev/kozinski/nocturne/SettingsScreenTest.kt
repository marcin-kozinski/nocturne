package dev.kozinski.nocturne

import android.Manifest
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import dev.kozinski.nocturne.ui.theme.NocturneTheme
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule

private class FakeViewModel : SettingsViewModel {
    private val calendarEnabledState = mutableStateOf(false)

    private var setCalendarEnabledCount = 0
    private var updatesAllowed = true

    fun disableUpdates() {
        updatesAllowed = false
    }

    override val calendarEnabled: Boolean
        get() = calendarEnabledState.value

    override fun setCalendarEnabled(value: Boolean, calendarPermissionsGranted: Boolean) {
        setCalendarEnabledCount++
        if (updatesAllowed) {
            calendarEnabledState.value = value
        }
    }

    fun assertSetterUnused() {
        assertEquals(0, setCalendarEnabledCount)
    }
}

class SettingsScreenTest {
    // Skip the system permissions popup.
    @get:Rule(order = 0)
    val grantPermissions: GrantPermissionRule =
        GrantPermissionRule.grant(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
        )

    @get:Rule(order = 1) val compose = createComposeRule()

    private val viewModel = FakeViewModel()

    @BeforeTest
    fun before() {
        compose.setContent { NocturneTheme { SettingsScreen(viewModel) } }
    }

    @Test
    fun `calendar enabled label is displayed`() {
        compose.onCalendarEnabledLabel().assertIsDisplayed()
        viewModel.assertSetterUnused()
    }

    @Test
    fun `calendar enabled toggle is displayed`() {
        compose.onCalendarEnabledToggle().assertIsDisplayed()
        viewModel.assertSetterUnused()
    }

    @Test
    fun `calendar enabled toggle turns on when clicked`() {
        compose.onCalendarEnabledToggle().assertIsOff().performClick().assertIsOn()
    }

    @Test
    fun `calendar enabled toggle stays off if view model does not update`() {
        viewModel.disableUpdates()

        compose.onCalendarEnabledToggle().assertIsOff().performClick().assertIsOff()
    }

    private fun SemanticsNodeInteractionsProvider.onCalendarEnabledLabel():
        SemanticsNodeInteraction {
        return onNodeWithText("Enable Calendar")
    }

    private fun SemanticsNodeInteractionsProvider.onCalendarEnabledToggle():
        SemanticsNodeInteraction {
        return onCalendarEnabledLabel().onSiblings().filterToOne(isToggleable())
    }
}
