package com.onlystack.starterapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit4.runners.AndroidJUnit4
import com.onlystack.starterapp.features.auth.WelcomeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Compose UI tests — run on device or emulator via ./gradlew connectedDevDebugAndroidTest
// These tests render composables in isolation without a real Activity or network.

@RunWith(AndroidJUnit4::class)
class WelcomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun welcomeScreen_showsAppName() {
        composeTestRule.setContent {
            WelcomeScreen(onGetStarted = {})
        }
        composeTestRule.onNodeWithText("StarterApp").assertIsDisplayed()
    }

    @Test
    fun welcomeScreen_showsGetStartedButton() {
        composeTestRule.setContent {
            WelcomeScreen(onGetStarted = {})
        }
        composeTestRule.onNodeWithText("Get Started").assertIsDisplayed()
    }

    @Test
    fun welcomeScreen_getStartedButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            WelcomeScreen(onGetStarted = { clicked = true })
        }
        composeTestRule.onNodeWithText("Get Started").performClick()
        assert(clicked) { "onGetStarted callback should have been called" }
    }
}
