package com.samidevstudio.pocketdex.ui.pokemon.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonSharedElementEligibilityTest {

    @Test
    fun forwardTransitionKeepsOnlyTheOriginEligibleAfterCarouselMoves() {
        assertTrue(
            isSharedPokemonElementEligible(
                elementId = "133",
                originatingPokemonId = "133",
                currentPokemonId = "134",
                isBackingOut = false
            )
        )
        assertFalse(
            isSharedPokemonElementEligible(
                elementId = "134",
                originatingPokemonId = "133",
                currentPokemonId = "134",
                isBackingOut = false
            )
        )
    }

    @Test
    fun backTransitionTracksOnlyTheCurrentlySelectedPokemon() {
        assertTrue(
            isSharedPokemonElementEligible(
                elementId = "134",
                originatingPokemonId = "133",
                currentPokemonId = "134",
                isBackingOut = true
            )
        )
        assertFalse(
            isSharedPokemonElementEligible(
                elementId = "133",
                originatingPokemonId = "133",
                currentPokemonId = "134",
                isBackingOut = true
            )
        )
    }
}
