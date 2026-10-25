package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.TypeDao
import com.samidevstudio.pocketdex.data.database.TypeEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypeRepositoryTest {

    @Test
    fun emptyCacheFetchesAndStoresCompleteRoster() = runBlocking {
        val api = FakeTypeApi()
        val dao = FakeTypeDao()
        DefaultTypeRepository(api, dao).syncAllTypes()

        assertEquals(listOf("fire", "water"), dao.types.map { it.name })
        assertEquals(listOf("4|charmander"), dao.types.single { it.name == "fire" }.pokemonNames)
        assertEquals(2, api.detailCalls)
    }

    @Test
    fun partialCacheIsReplacedByCompleteRoster() = runBlocking {
        val api = FakeTypeApi()
        val dao = FakeTypeDao(TypeEntity("fire", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))
        DefaultTypeRepository(api, dao).syncAllTypes()

        assertEquals(listOf("fire", "water"), dao.types.map { it.name }.sorted())
        assertEquals(2, api.detailCalls)
    }

    @Test
    fun legacyCacheRefreshesPokemonReferencesWithIds() = runBlocking {
        val api = FakeTypeApi()
        val dao = FakeTypeDao(
            TypeEntity("fire", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), listOf("charmander")),
            TypeEntity("water", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        )
        DefaultTypeRepository(api, dao).syncAllTypes()

        assertEquals(2, api.detailCalls)
        assertEquals(listOf("4|charmander"), dao.types.single { it.name == "fire" }.pokemonNames)
    }

    @Test
    fun completeCacheSkipsDetailRequests() = runBlocking {
        val api = FakeTypeApi()
        val dao = FakeTypeDao(
            TypeEntity("fire", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
            TypeEntity("water", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        )
        DefaultTypeRepository(api, dao).syncAllTypes()

        assertEquals(0, api.detailCalls)
    }

    @Test
    fun failedTypeRequestDoesNotPersistPartialRosterAndCanRecover() = runBlocking {
        val api = FakeTypeApi().apply { failDetailId = "2" }
        val dao = FakeTypeDao()
        val repository = DefaultTypeRepository(api, dao)

        try {
            repository.syncAllTypes()
            throw AssertionError("Expected type sync to fail")
        } catch (e: IllegalStateException) {
            assertEquals("offline", e.message)
        }
        assertTrue(dao.types.isEmpty())

        api.failDetailId = null
        repository.syncAllTypes()

        assertEquals(listOf("fire", "water"), dao.types.map { it.name })
        assertEquals(4, api.detailCalls)
    }

    @Test
    fun cancellationPropagatesWithoutReplacingCache() = runBlocking {
        val api = FakeTypeApi().apply { failDetailId = "1"; failure = CancellationException("cancelled") }
        val old = TypeEntity("old", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        val dao = FakeTypeDao(old)

        try {
            DefaultTypeRepository(api, dao).syncAllTypes()
            throw AssertionError("Expected cancellation")
        } catch (e: CancellationException) {
            assertEquals("cancelled", e.message)
        }
        assertEquals(listOf("old"), dao.types.map { it.name })
    }

    private class FakeTypeDao(vararg initial: TypeEntity) : TypeDao {
        var types = initial.toList()

        override fun getAllTypesFlow(): Flow<List<TypeEntity>> = emptyFlow()
        override fun getTypeFlow(name: String): Flow<TypeEntity?> = emptyFlow()
        override suspend fun getType(name: String): TypeEntity? = types.firstOrNull { it.name == name }
        override suspend fun getAllTypes(): List<TypeEntity> = types
        override suspend fun insertTypes(types: List<TypeEntity>) { this.types = types }
        override suspend fun replaceTypes(types: List<TypeEntity>) { this.types = types }
        override suspend fun clearTypes() { types = emptyList() }
    }

    private class FakeTypeApi : PokeApiService {
        var detailCalls = 0
        var failDetailId: String? = null
        var failure: Exception = IllegalStateException("offline")

        override suspend fun getTypeList() = TypeListResponse(
            listOf(
                TypeListItem("fire", "https://example.com/type/1/"),
                TypeListItem("water", "https://example.com/type/2/")
            )
        )

        override suspend fun getTypeDetail(id: String): TypeDetail {
            detailCalls++
            if (id == failDetailId) throw failure
            return TypeDetail(
                id = id.toInt(),
                name = if (id == "1") "fire" else "water",
                damageRelations = TypeDamageRelations(),
                pokemon = if (id == "1") {
                    listOf(
                        TypePokemonSlot(
                            NamedApiResource("charmander", "https://example.com/pokemon/4/")
                        )
                    )
                } else {
                    emptyList()
                }
            )
        }

        override suspend fun getPokemonList(limit: Int, offset: Int): PokemonResponse = error("unused")
        override suspend fun getPokemonDetail(id: String): PokemonDetail = error("unused")
        override suspend fun getPokemonSpecies(id: String): PokemonSpeciesResponse = error("unused")
        override suspend fun getEvolutionChain(url: String): EvolutionChainResponse = error("unused")
        override suspend fun getMoveList(limit: Int, offset: Int): MoveListResponse = error("unused")
        override suspend fun getMoveDetail(id: String): MoveDetail = error("unused")
        override suspend fun getItemList(limit: Int, offset: Int): ItemListResponse = error("unused")
        override suspend fun getItemDetail(id: String): ItemDetail = error("unused")
        override suspend fun getAbilityList(limit: Int, offset: Int): AbilityListResponse = error("unused")
        override suspend fun getAbilityDetail(id: String): AbilityDetail = error("unused")
        override suspend fun getBerryList(limit: Int, offset: Int): BerryListResponse = error("unused")
        override suspend fun getBerryDetail(id: String): BerryDetail = error("unused")
        override suspend fun getRegionList(limit: Int, offset: Int): RegionListResponse = error("unused")
        override suspend fun getRegionDetail(id: String): RegionDetail = error("unused")
        override suspend fun getLocationList(limit: Int, offset: Int): LocationListResponse = error("unused")
        override suspend fun getLocationDetail(id: String): LocationDetail = error("unused")
    }
}
