package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.EvolutionChainEntity
import com.samidevstudio.pocketdex.data.database.PokemonDao
import com.samidevstudio.pocketdex.data.database.PokemonDetailEntity
import com.samidevstudio.pocketdex.data.database.PokemonEntity
import com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class DefaultPokemonRepositoryTest {

    @Test
    fun syncEvolutionChain_preservesLinearRelationships() = runBlocking {
        val dao = FakePokemonDao()
        val service = FakePokeApiService().apply {
            evolutionChain = ChainLink(
                SpeciesReference("1", "https://example.com/pokemon/1/"),
                listOf(ChainLink(
                    SpeciesReference("2", "https://example.com/pokemon/2/"),
                    listOf(ChainLink(SpeciesReference("3", "https://example.com/pokemon/3/"), emptyList()))
                ))
            )
        }

        DefaultPokemonRepository(service, dao).syncEvolutionChain("1")

        assertEquals(listOf("1"), dao.savedEvolutionChain?.evolutions?.map { it.id })
        assertEquals("2", dao.savedEvolutionChain?.evolutions?.single()?.evolvesTo?.single()?.id)
        assertEquals("3", dao.savedEvolutionChain?.evolutions?.single()?.evolvesTo?.single()?.evolvesTo?.single()?.id)
    }

    @Test
    fun syncEvolutionChain_preservesSplitBranches() = runBlocking {
        val dao = FakePokemonDao()
        val service = FakePokeApiService().apply {
            evolutionChain = ChainLink(
                SpeciesReference("1", "https://example.com/pokemon/1/"),
                listOf(
                    ChainLink(SpeciesReference("2", "https://example.com/pokemon/2/"), emptyList()),
                    ChainLink(SpeciesReference("3", "https://example.com/pokemon/3/"), emptyList())
                )
            )
        }

        DefaultPokemonRepository(service, dao).syncEvolutionChain("1")

        assertEquals(listOf("2", "3"), dao.savedEvolutionChain?.evolutions?.single()?.evolvesTo?.map { it.id })
    }

    @Test
    fun syncEvolutionChain_preservesBranchFollowedByEvolution() = runBlocking {
        val dao = FakePokemonDao()
        val service = FakePokeApiService().apply {
            evolutionChain = ChainLink(
                SpeciesReference("1", "https://example.com/pokemon/1/"),
                listOf(
                    ChainLink(
                        SpeciesReference("2", "https://example.com/pokemon/2/"),
                        listOf(ChainLink(SpeciesReference("4", "https://example.com/pokemon/4/"), emptyList()))
                    ),
                    ChainLink(SpeciesReference("3", "https://example.com/pokemon/3/"), emptyList())
                )
            )
        }

        DefaultPokemonRepository(service, dao).syncEvolutionChain("1")

        val roots = dao.savedEvolutionChain!!.evolutions
        assertEquals("4", roots.single().evolvesTo.first().evolvesTo.single().id)
        assertEquals("3", roots.single().evolvesTo.last().id)
    }

    @Test
    fun syncPokemonDetail_refreshesLegacyFlatEvolutionCache() = runBlocking {
        val dao = FakePokemonDao().apply {
            existingDetail = PokemonDetailEntity(
                id = "25", name = "pikachu", imageUrl = "", types = listOf("electric"),
                height = 4, weight = 60, stats = emptyList(), flavorText = "cached",
                speciesId = "25", chainId = "1"
            )
            pokemonTypes = listOf("electric")
            cachedEvolutionChain = EvolutionChainEntity(
                id = "1",
                evolutions = listOf(
                    EvolutionNode("1", "one", ""),
                    EvolutionNode("2", "two", "")
                )
            )
        }

        DefaultPokemonRepository(FakePokeApiService(), dao).syncPokemonDetail("25")

        assertEquals(1, dao.evolutionChainFetches)
        assertEquals(1, dao.savedEvolutionChain?.evolutions?.size)
    }

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
    fun fetchPokemonList_propagatesFailuresAndReleasesInFlightKey() = runBlocking {
        val service = FakePokeApiService().apply { listFailure = IOException("offline") }
        val repository = DefaultPokemonRepository(service, FakePokemonDao())

        repeat(2) {
            try {
                repository.fetchPokemonList(offset = 0, limit = 2)
                fail("Expected list fetch to fail")
            } catch (e: IOException) {
                assertEquals("offline", e.message)
            }
        }
        assertEquals(2, service.listCallCount)
    }

    @Test
    fun fetchPokemonList_recoversOnRetryAtSameOffset() = runBlocking {
        val service = FakePokeApiService().apply { listFailure = IOException("offline") }
        val repository = DefaultPokemonRepository(service, FakePokemonDao())

        try {
            repository.fetchPokemonList(offset = 0, limit = 2)
            fail("Expected first list fetch to fail")
        } catch (_: IOException) {
            // Retry below should use the same page after the failed request.
        }

        service.listFailure = null
        repository.fetchPokemonList(offset = 0, limit = 2)

        assertEquals(2, service.listCallCount)
    }

    @Test
    fun syncPokemonDetail_recoversAfterDetailFailure() = runBlocking {
        val service = FakePokeApiService().apply { detailFailure = IOException("offline") }
        val repository = DefaultPokemonRepository(service, FakePokemonDao())

        try {
            repository.syncPokemonDetail("25")
            fail("Expected first detail sync to fail")
        } catch (_: IOException) {
            // Retry below verifies the in-flight key was released.
        }

        service.detailFailure = null
        repository.syncPokemonDetail("25")

        assertEquals(2, service.detailCallCount)
    }

    @Test
    fun syncPokemonDetail_repairsMissingListTypesFromCachedDetail() = runBlocking {
        val service = FakePokeApiService()
        val dao = FakePokemonDao().apply {
            pokemonTypes = emptyList()
            existingDetail = PokemonDetailEntity(
                id = "25",
                name = "pikachu",
                imageUrl = "",
                types = listOf("electric"),
                height = 4,
                weight = 60,
                stats = emptyList(),
                flavorText = "cached",
                speciesId = "25",
                chainId = "1"
            )
        }
        val repository = DefaultPokemonRepository(service, dao)

        repository.syncPokemonDetail("25")

        assertEquals(0, service.detailCallCount)
        assertEquals(listOf("25"), dao.updatedTypeIds)
    }

    @Test
    fun syncPokemonDetail_propagatesCancellation() = runBlocking {
        val service = FakePokeApiService().apply { detailFailure = CancellationException("cancelled") }
        val repository = DefaultPokemonRepository(service, FakePokemonDao())

        try {
            repository.syncPokemonDetail("25")
            fail("Expected cancellation to propagate")
        } catch (e: CancellationException) {
            assertEquals("cancelled", e.message)
        }
    }

    @Test
    fun backfillMissingTypes_propagatesFailureForWorkerRetry() = runBlocking {
        val service = FakePokeApiService().apply { detailFailure = IOException("offline") }
        val dao = FakePokemonDao().apply { missingTypeIds = listOf("25", "1") }
        val repository = DefaultPokemonRepository(service, dao)

        try {
            repository.backfillMissingTypes()
            fail("Expected backfill to fail")
        } catch (e: IOException) {
            assertEquals("offline", e.message)
        }
        assertEquals(1, service.detailCallCount)
    }

    @Test
    fun backfillKeepsEarlierProgressWhenLaterDetailFails() = runBlocking {
        val service = FakePokeApiService().apply { detailFailureId = "1" }
        val dao = FakePokemonDao().apply { missingTypeIds = listOf("25", "1") }
        val repository = DefaultPokemonRepository(service, dao)

        try {
            repository.backfillMissingTypes()
            fail("Expected second detail fetch to fail")
        } catch (_: IOException) {
            // The first row was synced before the second request failed.
        }

        assertEquals(listOf("25"), dao.updatedTypeIds)
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

    private class FakePokeApiService : PokeApiService {
        var listCallCount = 0
        var detailCallCount = 0
        var listFailure: Exception? = null
        var detailFailure: Exception? = null
        var detailFailureId: String? = null
        var detailTypes: List<String> = emptyList()
        var evolutionChain = ChainLink(
            species = SpeciesReference(name = "pikachu", url = "https://example.com/pokemon/25/"),
            evolvesTo = emptyList()
        )

        override suspend fun getPokemonList(limit: Int, offset: Int): PokemonResponse {
            listCallCount += 1
            listFailure?.let { throw it }
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
            if (id == detailFailureId) throw IOException("offline")
            detailFailure?.let { throw it }
            delay(50)
            return PokemonDetail(
                id = id.toInt(),
                name = "pikachu",
                height = 4,
                weight = 60,
                types = detailTypes.map { PokemonTypeSlot(PokemonType(it)) },
                stats = emptyList(),
                sprites = PokemonSprites(
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
            return EvolutionChainResponse(chain = evolutionChain)
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
        override suspend fun getPokemonTypes(id: String): List<String>? = null
        override suspend fun getPokemonIdsMissingTypes(): List<String> = emptyList()
        override suspend fun getEvolutionChain(id: String): EvolutionChainEntity? = null
        override fun getEvolutionChainFlow(id: String): Flow<EvolutionChainEntity?> = flowOf(null)
        override suspend fun insertEvolutionChain(chain: EvolutionChainEntity) = Unit
    }

    private class FakePokemonDao : PokemonDao {
        var missingTypeIds: List<String> = emptyList()
        var existingDetail: PokemonDetailEntity? = null
        var pokemonTypes: List<String>? = null
        var cachedEvolutionChain: EvolutionChainEntity? = null
        var savedEvolutionChain: EvolutionChainEntity? = null
        var evolutionChainFetches = 0
        val updatedTypeIds = mutableListOf<String>()

        override fun getPokemonList(limit: Int, offset: Int): Flow<List<PokemonEntity>> = flowOf(emptyList())

        override suspend fun getPokemonCountInRange(offset: Int, limit: Int): Int = 0

        override suspend fun insertPokemonList(pokemon: List<PokemonEntity>) = Unit

        override suspend fun getPokemonDetail(id: String): PokemonDetailEntity? = existingDetail

        override fun getPokemonDetailFlow(id: String): Flow<PokemonDetailEntity?> = flowOf(null)

        override suspend fun insertPokemonDetail(pokemonDetail: PokemonDetailEntity) = Unit

        override suspend fun updatePokemonTypes(id: String, types: List<String>) {
            updatedTypeIds += id
        }

        override suspend fun getPokemonTypes(id: String): List<String>? = pokemonTypes

        override suspend fun getPokemonIdsMissingTypes(): List<String> = missingTypeIds

        override suspend fun getEvolutionChain(id: String): EvolutionChainEntity? = cachedEvolutionChain

        override fun getEvolutionChainFlow(id: String): Flow<EvolutionChainEntity?> = flowOf(cachedEvolutionChain)

        override suspend fun insertEvolutionChain(chain: EvolutionChainEntity) {
            evolutionChainFetches += 1
            savedEvolutionChain = chain
            cachedEvolutionChain = chain
        }
    }
}
