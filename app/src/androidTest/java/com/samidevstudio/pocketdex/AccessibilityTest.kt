package com.samidevstudio.pocketdex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class AccessibilityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun verifyNavTabContentDescriptions() {
        // Verify all bottom tabs have content descriptions for accessibility
        composeTestRule.onNodeWithContentDescription("Moves").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Types").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Items").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Team").assertIsDisplayed()
        
        // Verify central Pokeball
        composeTestRule.onNodeWithContentDescription("Pokedex List").assertIsDisplayed()
    }

    @Test
    fun verifyPokemonCardAccessibility() {
        // Wait for list
        composeTestRule.waitUntilAtLeastOneExists(hasText("BULBASAUR"), 10000L)
        
        // Find a card and verify it merges descendants and has image description
        val card = composeTestRule.onAllNodes(hasClickAction()).onFirst()
        card.assert(hasAnyDescendant(hasContentDescription("BULBASAUR")))
    }
}
