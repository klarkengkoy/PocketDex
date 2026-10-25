package com.samidevstudio.pocketdex

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
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
class PokemonUiTest {

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
    fun navigateToDetailAndBack() {
        composeTestRule.waitUntilAtLeastOneExists(hasText("POKEMON"), 5_000L)
        composeTestRule.onNodeWithText("POKEMON").assertIsDisplayed()

        val card = hasText("BULBASAUR") and hasClickAction()
        composeTestRule.waitUntilExactlyOneExists(card, 5_000L)
        composeTestRule.onNode(card).performClick()

        // Detail-specific content proves navigation completed, rather than matching the old card.
        composeTestRule.waitUntilExactlyOneExists(hasText("BASE STATS:"), 5_000L)
        composeTestRule.onNodeWithText("BASE STATS:").assertIsDisplayed()

        Espresso.pressBack()
        composeTestRule.waitUntilExactlyOneExists(hasText("POKEMON"), 5_000L)
        composeTestRule.onNodeWithText("POKEMON").assertIsDisplayed()
        composeTestRule.onNode(card).assertIsDisplayed()
        composeTestRule.onNodeWithText("BASE STATS:").assertDoesNotExist()
    }
}
