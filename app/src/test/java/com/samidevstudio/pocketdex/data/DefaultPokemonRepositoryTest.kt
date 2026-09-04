package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.EvolutionChainEntity
import com.samidevstudio.pocketdex.data.database.PokemonDao
import com.samidevstudio.pocketdex.data.database.PokemonDetailEntity
import com.samidevstudio.pocketdex.data.database.PokemonEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultPokemonRepositoryTest {

    @Test
    fun fetchPokemonList_deduplicatesConcurrentRequests() = runBlocking {
        val service = FakePokeApiService()
        val dao = FakePokemonDao()
        val repository = DefaultPokemonRepository(apiService = service, pokemonDao = dao)

        val first = async { repository.fetchPokemonList(offset = 0, limit = 2) }
        val second = async { repository.fetchPokemonList(offset = 0, limit = 2) }

        first.await()
        second.await()

        assertEquals(1, service.listCallCount)
    }

    @Test
    fun syncPokemonDetail_deduplicatesConcurrentRequests() = runBlocking {
        val service = FakePokeApiService()
        val dao = FakePokemonDao()
        val repository = DefaultPokemonRepository(apiService = service, pokemonDao = dao)

        val first = async { repository.syncPokemonDetail("25") }
        val second = async { repository.syncPokemonDetail("25") }

        first.await()
        second.await()

        assertEquals(1, service.detailCallCount)
    }

    private class FakePokeApiService : PokeApiService {
        var listCallCount = 0
        var detailCallCount = 0

        override suspend fun getPokemonList(limit: Int, offset: Int): PokemonResponse {
            listCallCount += 1
            delay(50)
            return PokemonResponse(
                results = listOf(
                    PokemonListItem(name = "pikachu", url = "https://example.com/pokemon/25/"),
                    PokemonListItem(name = "bulbasaur", url = "https://example.com/pokemon/1/")
                )
            )
        }

        override suspend fun getPokemonDetail(id: String): PokemonDetail {
            detailCallCount += 1
            delay(50)
            return PokemonDetail(
                id = id.toInt(),
                name = "pikachu",
                height = 4,
                weight = 60,
                types = emptyList(),
                stats = emptyList(),
                sprites = PokemonSprites(
                    frontDefault = "",
                    frontShiny = "",
                    other = OtherSprites(
                        officialArtwork = OfficialArtwork(frontDefault = "")
                    )
                )
            )
        }

        override suspend fun getPokemonSpecies(id: String): PokemonSpeciesResponse {
            return PokemonSpeciesResponse(
                flavorTextEntries = listOf(
                    FlavorTextEntry(
                        flavorText = "Test flavor text",
                        language = LanguageReference(name = "en")
                    )
                ),
                evolutionChain = EvolutionChainLink("https://example.com/evolution-chain/1/")
            )
        }

        override suspend fun getEvolutionChain(url: String): EvolutionChainResponse {
            return EvolutionChainResponse(
                chain = ChainLink(
                    species = SpeciesReference(name = "pikachu", url = "https://example.com/pokemon/25/"),
                    evolvesTo = emptyList()
                )
            )
        }
    }

    private class FakePokemonDao : PokemonDao {
        override fun getPokemonList(limit: Int, offset: Int): Flow<List<PokemonEntity>> = flowOf(emptyList())

        override suspend fun getPokemonCountInRange(offset: Int, limit: Int): Int = 0

        override suspend fun insertPokemonList(pokemon: List<PokemonEntity>) = Unit

        override suspend fun getPokemonDetail(id: String): PokemonDetailEntity? = null

        override fun getPokemonDetailFlow(id: String): Flow<PokemonDetailEntity?> = flowOf(null)

        override suspend fun insertPokemonDetail(pokemonDetail: PokemonDetailEntity) = Unit

        override suspend fun updatePokemonTypes(id: String, types: List<String>) = Unit

        override suspend fun getPokemonIdsMissingTypes(): List<String> = emptyList()

        override suspend fun getEvolutionChain(id: String): EvolutionChainEntity? = null

        override fun getEvolutionChainFlow(id: String): Flow<EvolutionChainEntity?> = flowOf(null)

        override suspend fun insertEvolutionChain(chain: EvolutionChainEntity) = Unit

        override suspend fun clearPokemonList() = Unit

        override suspend fun clearPokemonDetail() = Unit

        override suspend fun clearEvolutionChains() = Unit
    }
}
