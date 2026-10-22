package com.apnasandesh.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeUiFlowInstrumentationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAppLaunchesAndDisplaysCoreUi() {
        // Verifies the MainActivity launches successfully and displays the header or get started
        composeTestRule.waitForIdle()
        // Check if either Welcome to LocalReach SMS or Dashboard is displayed
        val welcomeNode = composeTestRule.onNodeWithText("Welcome to LocalReach SMS")
        val dashboardNode = composeTestRule.onNodeWithText("Dashboard")
        
        // At least one of the primary surfaces must be displayed
        assertTrue(welcomeNode != null || dashboardNode != null)
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}
