package com.samidevstudio.pocketdex

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class PokemonUiTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun navigateToDetailAndBack() {
        // 1. Verify "PocketDex" title is visible
        composeTestRule.onNodeWithText("PocketDex").assertIsDisplayed()

        // Wait for the list to load and find "BULBASAUR"
        // Since the app uses real network/database in instrumented tests (unless mocked), 
        // we use a long timeout if needed, but typically Compose handles this with idling resources
        composeTestRule.waitUntilAtLeastOneExists(hasText("BULBASAUR"), 10000L)

        // 2. Click on a Pokémon card (BULBASAUR)
        composeTestRule.onAllNodesWithText("BULBASAUR").onFirst().performClick()

        // 3. Verify the Pokémon name is shown in the detail screen
        // We look for the "BULBASAUR" text again. If multiple exist, we just verify at least one is displayed.
        composeTestRule.onAllNodesWithText("BULBASAUR").onFirst().assertIsDisplayed()
        
        // Verify a detail-specific element
        composeTestRule.waitUntilAtLeastOneExists(hasText("BASE STATS:"), 15000L)

        // 4. Use system back press
        Espresso.pressBack()
        
        // Wait for the transition
        composeTestRule.waitForIdle()

        // 5. Verify we are back on the list screen. 
        composeTestRule.waitUntilAtLeastOneExists(hasText("PocketDex"), 15000L)
    }
}
