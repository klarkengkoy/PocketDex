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

    @Test
    fun fetchPokemonList_skipsEmptyResults() = runBlocking {
        val service = EmptyPokemonListService()
        val dao = CountingPokemonDao()
        val repository = DefaultPokemonRepository(apiService = service, pokemonDao = dao)

        repository.fetchPokemonList(offset = 0, limit = 2)

        assertEquals(0, dao.insertCount)
    }

    @Test
    fun calculateBackoffDelay_capsAtOneMinute() {
        assertEquals(1000L, DefaultPokemonRepository.calculateBackoffDelay(0))
        assertEquals(2000L, DefaultPokemonRepository.calculateBackoffDelay(1000))
        assertEquals(60000L, DefaultPokemonRepository.calculateBackoffDelay(30000))
        assertEquals(60000L, DefaultPokemonRepository.calculateBackoffDelay(60000))
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

        override suspend fun getMoveList(limit: Int, offset: Int): MoveListResponse = throw NotImplementedError()
        override suspend fun getMoveDetail(id: String): MoveDetail = throw NotImplementedError()
        override suspend fun getTypeList(): TypeListResponse = throw NotImplementedError()
        override suspend fun getTypeDetail(id: String): TypeDetail = throw NotImplementedError()
        override suspend fun getItemList(limit: Int, offset: Int): ItemListResponse = throw NotImplementedError()
        override suspend fun getItemDetail(id: String): ItemDetail = throw NotImplementedError()
    }

    private class EmptyPokemonListService : PokeApiService {
        override suspend fun getPokemonList(limit: Int, offset: Int): PokemonResponse {
            return PokemonResponse(results = emptyList())
        }

        override suspend fun getPokemonDetail(id: String): PokemonDetail = throw NotImplementedError()
        override suspend fun getPokemonSpecies(id: String): PokemonSpeciesResponse = throw NotImplementedError()
        override suspend fun getEvolutionChain(url: String): EvolutionChainResponse = throw NotImplementedError()
        override suspend fun getMoveList(limit: Int, offset: Int): MoveListResponse = throw NotImplementedError()
        override suspend fun getMoveDetail(id: String): MoveDetail = throw NotImplementedError()
        override suspend fun getTypeList(): TypeListResponse = throw NotImplementedError()
        override suspend fun getTypeDetail(id: String): TypeDetail = throw NotImplementedError()
        override suspend fun getItemList(limit: Int, offset: Int): ItemListResponse = throw NotImplementedError()
        override suspend fun getItemDetail(id: String): ItemDetail = throw NotImplementedError()
    }

    private class CountingPokemonDao : PokemonDao {
        var insertCount = 0

        override fun getPokemonList(limit: Int, offset: Int): Flow<List<PokemonEntity>> = flowOf(emptyList())
        override suspend fun getPokemonCountInRange(offset: Int, limit: Int): Int = 0
        override suspend fun insertPokemonList(pokemon: List<PokemonEntity>) { insertCount += 1 }
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
