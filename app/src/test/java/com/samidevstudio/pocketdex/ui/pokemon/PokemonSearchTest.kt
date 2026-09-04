package com.samidevstudio.pocketdex.ui.pokemon

import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonSearchTest {

    @Test
    fun applyPokemonSearch_returnsOriginalListWhenQueryIsBlank() {
        val list = listOf(
            PokemonUiModel("25", "pikachu", "", listOf("electric")),
            PokemonUiModel("1", "bulbasaur", "", listOf("grass", "poison"))
        )

        val filtered = PokemonViewModel.applyPokemonSearch("   ", list)

        assertEquals(list, filtered)
    }

    @Test
    fun applyPokemonSearch_filtersByNameTypeAndId() {
        val list = listOf(
            PokemonUiModel("25", "pikachu", "", listOf("electric")),
            PokemonUiModel("1", "bulbasaur", "", listOf("grass", "poison")),
            PokemonUiModel("4", "charmander", "", listOf("fire"))
        )

        val filteredByName = PokemonViewModel.applyPokemonSearch("pik", list)
        val filteredByType = PokemonViewModel.applyPokemonSearch("fire", list)
        val filteredById = PokemonViewModel.applyPokemonSearch("25", list)

        assertEquals(listOf(list[0]), filteredByName)
        assertEquals(listOf(list[2]), filteredByType)
        assertEquals(listOf(list[0]), filteredById)
    }

    @Test
    fun applyPokemonSearch_filtersBySelectedTypeWhenProvided() {
        val list = listOf(
            PokemonUiModel("25", "pikachu", "", listOf("electric")),
            PokemonUiModel("1", "bulbasaur", "", listOf("grass", "poison")),
            PokemonUiModel("4", "charmander", "", listOf("fire"))
        )

        val filtered = PokemonViewModel.applyPokemonSearch("", list, setOf("fire"))

        assertEquals(listOf(list[2]), filtered)
    }

    @Test
    fun applyPokemonSearch_filtersByMultipleSelectedTypes() {
        val list = listOf(
            PokemonUiModel("25", "pikachu", "", listOf("electric")),
            PokemonUiModel("1", "bulbasaur", "", listOf("grass", "poison")),
            PokemonUiModel("4", "charmander", "", listOf("fire")),
            PokemonUiModel("7", "squirtle", "", listOf("water"))
        )

        val filtered = PokemonViewModel.applyPokemonSearch("", list, setOf("fire", "water"))

        assertEquals(listOf(list[2], list[3]), filtered)
    }
}
