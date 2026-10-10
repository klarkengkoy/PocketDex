package com.samidevstudio.pocketdex.data.database

import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonConvertersTest {
    @Test
    fun decodesEvolutionCacheWrittenBeforeChildLinksWereAdded() {
        val cachedJson = """[{"id":"1","name":"one","imageUrl":"sprite/1.png"},{"id":"2","name":"two","imageUrl":"sprite/2.png"}]"""

        val decoded = PokemonConverters().toEvolutionNodeList(cachedJson)

        assertEquals(listOf("1", "2"), decoded.map { it.id })
        assertEquals(emptyList<com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode>(), decoded.first().evolvesTo)
    }
}
