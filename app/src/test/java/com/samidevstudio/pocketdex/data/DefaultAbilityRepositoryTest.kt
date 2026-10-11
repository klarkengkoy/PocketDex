package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.AbilityDao
import com.samidevstudio.pocketdex.data.database.AbilityDetailEntity
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

class DefaultAbilityRepositoryTest {

    @Test
    fun abilityListFetchPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<AbilityDao>()
        whenever(dao.getAbilityListCount()).thenReturn(0)
        whenever(api.getAbilityList(limit = 60, offset = 0)).thenAnswer { throw IOException("offline") }

        try {
            DefaultAbilityRepository(api, dao).fetchAbilityList(offset = 0)
            fail("Expected network failure")
        } catch (e: IOException) {
            assertEquals("offline", e.message)
        }
    }

    @Test
    fun abilityDetailSyncPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<AbilityDao>()
        whenever(dao.getAbilityDetail("overgrow")).thenReturn(null)
        whenever(api.getAbilityDetail("overgrow")).thenAnswer { throw IOException("offline") }

        try {
            DefaultAbilityRepository(api, dao).syncAbilityDetail("overgrow")
            fail("Expected network failure")
        } catch (e: IOException) {
            assertEquals("offline", e.message)
        }
    }

    @Test
    fun abilityDetailSyncStoresDataInDaoWhenCached() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<AbilityDao>()
        whenever(dao.getAbilityDetail("overgrow")).thenReturn(
            AbilityDetailEntity(
                id = "65",
                name = "overgrow",
                effect = "Powers up Grass moves",
                shortEffect = "Powers up Grass moves",
                flavorText = "Powers up Grass-type moves in a pinch.",
                pokemonNames = listOf("1|bulbasaur")
            )
        )

        DefaultAbilityRepository(api, dao).syncAbilityDetail("overgrow")
        // Skips API call since the cached reference includes its ID.
    }

    @Test
    fun abilityDetailSyncUpgradesCachedNamesToPokemonIds() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<AbilityDao>()
        whenever(dao.getAbilityDetail("overgrow")).thenReturn(
            AbilityDetailEntity(
                id = "65",
                name = "overgrow",
                effect = "",
                shortEffect = "",
                flavorText = "",
                pokemonNames = listOf("bulbasaur")
            )
        )
        whenever(api.getAbilityDetail("overgrow")).thenReturn(
            AbilityDetail(
                id = 65,
                name = "overgrow",
                pokemon = listOf(
                    AbilityPokemonEntry(
                        pokemon = AbilityPokemonRef(
                            name = "bulbasaur",
                            url = "https://pokeapi.co/api/v2/pokemon/1/"
                        )
                    )
                )
            )
        )

        DefaultAbilityRepository(api, dao).syncAbilityDetail("overgrow")

        val inserted = argumentCaptor<AbilityDetailEntity>()
        verify(dao).insertAbilityDetail(inserted.capture())
        assertEquals(listOf("1|bulbasaur"), inserted.firstValue.pokemonNames)
    }

    @Test
    fun abilityDetailFlowMapsStoredReferencesToPokemonRows() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<AbilityDao>()
        whenever(dao.getAbilityDetailFlow("overgrow")).thenReturn(
            flowOf(
                AbilityDetailEntity(
                    id = "65",
                    name = "overgrow",
                    effect = "",
                    shortEffect = "",
                    flavorText = "",
                    pokemonNames = listOf("1|bulbasaur")
                )
            )
        )

        val ability = DefaultAbilityRepository(api, dao).getAbilityDetailFlow("overgrow").first()!!

        assertEquals(
            PokemonUiModel(
                id = "1",
                name = "bulbasaur",
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
                types = emptyList()
            ),
            ability.pokemon.single()
        )
    }
}
