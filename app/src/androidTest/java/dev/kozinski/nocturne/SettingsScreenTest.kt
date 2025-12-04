package dev.kozinski.nocturne

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.kozinski.nocturne.ui.theme.NocturneTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun isDisplayed() {
        val name = "Android"
        compose.setContent { NocturneTheme { SettingsScreen(name) } }

        compose.onNodeWithText("Hello $name!").assertIsDisplayed()
    }
}
