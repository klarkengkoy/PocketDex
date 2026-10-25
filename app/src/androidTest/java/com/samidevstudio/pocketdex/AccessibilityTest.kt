package com.samidevstudio.pocketdex

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.samidevstudio.pocketdex.di.RepositoryModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class AccessibilityTest {

    @Module(includes = [UiTestRepositories::class])
    @InstallIn(SingletonComponent::class)
    object TestModule

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
        composeTestRule.waitForIdle()
        listOf("Explore", "Moves", "Items", "Team Lab", "Pokedex List").forEach { description ->
            composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
        }
    }

    @Test
    fun verifyPokemonCardAccessibility() {
        val card = hasText("BULBASAUR") and hasClickAction()
        composeTestRule.waitUntilExactlyOneExists(card, 5_000L)
        // Merged card semantics expose its label, action and sprite description together.
        composeTestRule.onNode(card)
            .assertIsDisplayed()
            .assert(hasContentDescription("bulbasaur"))
    }
}
