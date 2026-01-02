package dev.kozinski.nocturne

import android.Manifest
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
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail
import org.junit.Rule

class SettingsScreenTest {
    // Skip the system permissions popup.
    @get:Rule(order = 0)
    val grantPermissions: GrantPermissionRule =
        GrantPermissionRule.grant(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
        )

    @get:Rule(order = 1) val compose = createComposeRule()

    @Test
    fun `calendar enabled label is displayed`() {
        compose.setContent {
            SettingsScreen(calendarEnabled = false, onCalendarEnabledChange = { fail() })
        }
        compose.onCalendarEnabledLabel().assertIsDisplayed()
    }

    @Test
    fun `calendar enabled toggle is off when calendar is disabled`() {
        compose.setContent {
            SettingsScreen(calendarEnabled = false, onCalendarEnabledChange = { fail() })
        }
        compose.onCalendarEnabledToggle().assertIsDisplayed().assertIsOff()
    }

    @Test
    fun `calendar enabled toggle is on when calendar is enabled`() {
        compose.setContent {
            SettingsScreen(calendarEnabled = true, onCalendarEnabledChange = { fail() })
        }
        compose.onCalendarEnabledToggle().assertIsDisplayed().assertIsOn()
    }

    @Test
    fun `onCalendarEnabledChange is called when calendar enabled toggle is clicked`() {
        var onCalendarEnabledChangeCalled = false
        compose.setContent {
            SettingsScreen(
                calendarEnabled = false,
                onCalendarEnabledChange = { onCalendarEnabledChangeCalled = true },
            )
        }
        compose.onCalendarEnabledToggle().performClick()
        assertTrue(onCalendarEnabledChangeCalled)
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
