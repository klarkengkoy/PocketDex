package com.samidevstudio.pocketdex.ui.types

import app.cash.turbine.test
import com.samidevstudio.pocketdex.data.TypeRepository
import com.samidevstudio.pocketdex.domain.TypePokemonReference
import com.samidevstudio.pocketdex.domain.TypeRelations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TypeViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectedTypeDetailTracksTheRequestedTypeAndIgnoresOldCleanup() = runTest {
        val repository = mock<TypeRepository>()
        fun typeRelations(name: String, pokemonId: String, pokemonName: String) = TypeRelations(
            name = name,
            doubleDamageFrom = emptySet(),
            doubleDamageTo = if (name == "fire") setOf("water") else emptySet(),
            halfDamageFrom = emptySet(),
            halfDamageTo = emptySet(),
            noDamageFrom = emptySet(),
            noDamageTo = emptySet(),
            pokemonReferences = listOf(TypePokemonReference(pokemonId, pokemonName))
        )

        whenever(repository.getAllTypesFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.getTypeFlow("fire")).thenReturn(flowOf(typeRelations("fire", "4", "charmander")))
        whenever(repository.getTypeFlow("water")).thenReturn(flowOf(typeRelations("water", "7", "squirtle")))

        val viewModel = TypeViewModel(repository)

        viewModel.detailUiState.test {
            assertEquals(TypeDetailUiState.Loading, awaitItem())
            viewModel.loadTypeDetail("fire")
            val fire = (awaitItem() as TypeDetailUiState.Success).type
            assertEquals("fire", fire.name)
            assertEquals(listOf("water"), fire.matchups.superEffectiveAgainst)
            assertEquals("4", fire.pokemon.single().id)
            assertEquals("charmander", fire.pokemon.single().name)
            assertEquals(
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png",
                fire.pokemon.single().imageUrl
            )

            viewModel.loadTypeDetail("water")
            val water = (awaitItem() as TypeDetailUiState.Success).type
            assertEquals("water", water.name)
            assertEquals("7", water.pokemon.single().id)
            assertEquals("squirtle", water.pokemon.single().name)

            viewModel.clearTypeDetail("fire")
            expectNoEvents()
        }
    }

    @Test
    fun browseableTypeListExcludesOnlyUnknownAndStellar() = runTest {
        val repository = mock<TypeRepository>()
        val types = listOf("fire", "unknown", "stellar", "shadow").map { name ->
            TypeRelations(
                name = name,
                doubleDamageFrom = emptySet(),
                doubleDamageTo = emptySet(),
                halfDamageFrom = emptySet(),
                halfDamageTo = emptySet(),
                noDamageFrom = emptySet(),
                noDamageTo = emptySet()
            )
        }
        whenever(repository.getAllTypesFlow()).thenReturn(flowOf(types))

        val viewModel = TypeViewModel(repository)
        val result = viewModel.listUiState.first { it is TypeListUiState.Success } as TypeListUiState.Success

        assertEquals(
            listOf(TypeListModel("fire"), TypeListModel("shadow")),
            result.types
        )
    }

    @Test
    fun detailWaitsForCachedPokemonReferencesAndReportsSyncFailure() = runTest {
        val repository = mock<TypeRepository>()
        val incomplete = TypeRelations(
            name = "fire",
            doubleDamageFrom = emptySet(),
            doubleDamageTo = emptySet(),
            halfDamageFrom = emptySet(),
            halfDamageTo = emptySet(),
            noDamageFrom = emptySet(),
            noDamageTo = emptySet(),
            hasIncompletePokemonReferences = true
        )
        whenever(repository.getAllTypesFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.getTypeFlow("fire")).thenReturn(MutableStateFlow(incomplete))
        whenever(repository.syncAllTypes()).thenAnswer { throw IllegalStateException("offline") }

        val viewModel = TypeViewModel(repository)
        viewModel.detailUiState.test {
            assertEquals(TypeDetailUiState.Loading, awaitItem())
            viewModel.loadTypeDetail("fire")
            assertEquals(TypeDetailUiState.Error("offline"), awaitItem())
        }
    }

    @Test
    fun initialSyncFailureCanBeRetried() = runTest {
        val repository = mock<TypeRepository>()
        val types = MutableStateFlow(emptyList<TypeRelations>())
        var syncCalls = 0
        whenever(repository.getAllTypesFlow()).thenReturn(types)
        whenever(repository.syncAllTypes()).thenAnswer {
            if (syncCalls++ == 0) throw IllegalStateException("offline")
            types.value = listOf(
                TypeRelations(
                    name = "fire",
                    doubleDamageFrom = emptySet(),
                    doubleDamageTo = emptySet(),
                    halfDamageFrom = emptySet(),
                    halfDamageTo = emptySet(),
                    noDamageFrom = emptySet(),
                    noDamageTo = emptySet()
                )
            )
        }

        val viewModel = TypeViewModel(repository)

        viewModel.listUiState.test {
            assertEquals(TypeListUiState.Error("offline"), awaitItem())
            viewModel.retrySync()
            assertEquals(TypeListUiState.Loading, awaitItem())
            assertEquals(
                TypeListUiState.Success(listOf(TypeListModel("fire"))),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
