package com.samidevstudio.pocketdex.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute.EvolutionDetail
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute.Evolutions
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute.Explore
import org.junit.Assert.assertEquals
import org.junit.Test

class EvolutionNavigationTest {
    @Test
    fun repeatedPokemonSelectionReplacesEvolutionDetailAndBackReturnsToFamilyList() {
        val backStack = mutableListOf<NavKey>(Explore, Evolutions)

        backStack.selectEvolutionPokemon("133", "eevee")
        backStack.selectEvolutionPokemon("134", "vaporeon")

        assertEquals(
            listOf(Explore, Evolutions, EvolutionDetail("134", "vaporeon")),
            backStack
        )

        backStack.removeAt(backStack.lastIndex)
        assertEquals(Evolutions, backStack.last())
    }
}
