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
import dev.kozinski.nocturne.ui.theme.NocturneTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule(order = 0)
    val grantPermissions: GrantPermissionRule =
        GrantPermissionRule.grant(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
        )

    @get:Rule(order = 1) val compose = createComposeRule()

    @Before
    fun before() {
        compose.setContent { NocturneTheme { SettingsScreen(SettingsViewModel()) } }
    }

    @Test
    fun `calendar enabled label is displayed`() {
        compose.onCalendarEnabledLabel().assertIsDisplayed()
    }

    @Test
    fun `calendar enabled toggle is displayed`() {
        compose.onCalendarEnabledToggle().assertIsDisplayed()
    }

    @Test
    fun `calendar enabled toggle turns on when clicked`() {
        compose.onCalendarEnabledToggle().assertIsOff().performClick().assertIsOn()
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
