package com.campusconnectplus

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI/navigation test: launches MainActivity and verifies the initial
 * student home screen is displayed (start destination).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivity_launchesAndShowsStudentHome() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("CampusConnect+").assertExists()
    }

    @Test
    fun mainActivity_showsWelcomeText() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Welcome back, Student!").assertExists()
    }
}
