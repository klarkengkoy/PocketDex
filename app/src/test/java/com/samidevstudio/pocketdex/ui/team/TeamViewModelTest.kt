package com.samidevstudio.pocketdex.ui.team

import app.cash.turbine.test
import com.samidevstudio.pocketdex.data.PokemonRepository
import com.samidevstudio.pocketdex.data.TeamRepository
import com.samidevstudio.pocketdex.data.TypeRepository
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailModel
import com.samidevstudio.pocketdex.ui.pokemon.StatInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TeamViewModelTest {
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
    fun typeSyncFailureIsShownAndCanBeRetried() = runTest {
        val teamRepository = mock<TeamRepository>()
        val typeRepository = mock<TypeRepository>()
        val pokemonRepository = mock<PokemonRepository>()
        whenever(teamRepository.getTeamFlow()).thenReturn(MutableStateFlow(emptyList()))
        whenever(typeRepository.getAllTypesFlow()).thenReturn(MutableStateFlow(emptyList()))
        var syncCalls = 0
        whenever(typeRepository.syncAllTypes()).thenAnswer {
            if (syncCalls++ == 0) throw IllegalStateException("offline")
            Unit
        }

        val viewModel = TeamViewModel(teamRepository, typeRepository, pokemonRepository)

        viewModel.teamUiState.test {
            assertEquals("offline", awaitItem().typeSyncError)
            viewModel.retryTypeSync()
            assertEquals(null, awaitItem().typeSyncError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectComparePokemonUpdatesSlotsAndLoadsDetails() = runTest {
        val teamRepository = mock<TeamRepository>()
        val typeRepository = mock<TypeRepository>()
        val pokemonRepository = mock<PokemonRepository>()

        whenever(teamRepository.getTeamFlow()).thenReturn(MutableStateFlow(emptyList()))
        whenever(typeRepository.getAllTypesFlow()).thenReturn(MutableStateFlow(emptyList()))

        val bulbasaur = PokemonDetailModel(
            id = "1", name = "bulbasaur", imageUrl = "", types = listOf("grass", "poison"),
            height = 7, weight = 69, stats = listOf(StatInfo("HP", 45), StatInfo("Attack", 49))
        )
        val charmander = PokemonDetailModel(
            id = "4", name = "charmander", imageUrl = "", types = listOf("fire"),
            height = 6, weight = 85, stats = listOf(StatInfo("HP", 39), StatInfo("Attack", 52))
        )

        whenever(pokemonRepository.getPokemonDetailFlow("1")).thenReturn(MutableStateFlow(bulbasaur))
        whenever(pokemonRepository.getPokemonDetailFlow("4")).thenReturn(MutableStateFlow(charmander))

        val viewModel = TeamViewModel(teamRepository, typeRepository, pokemonRepository)

        viewModel.teamUiState.test {
            assertEquals(CompareSlotState.Empty, awaitItem().compareSlot1)

            viewModel.selectComparePokemon(1, "1")
            assertEquals(CompareSlotState.Success(bulbasaur), awaitItem().compareSlot1)

            viewModel.selectComparePokemon(2, "4")
            val state = awaitItem()
            assertEquals(CompareSlotState.Success(bulbasaur), state.compareSlot1)
            assertEquals(CompareSlotState.Success(charmander), state.compareSlot2)

            viewModel.clearComparison()
            val finalState = expectMostRecentItem()
            assertEquals(CompareSlotState.Empty, finalState.compareSlot1)
            assertEquals(CompareSlotState.Empty, finalState.compareSlot2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun compareSyncFailureCanBeRetried() = runTest {
        val teamRepository = mock<TeamRepository>()
        val typeRepository = mock<TypeRepository>()
        val pokemonRepository = mock<PokemonRepository>()

        whenever(teamRepository.getTeamFlow()).thenReturn(MutableStateFlow(emptyList()))
        whenever(typeRepository.getAllTypesFlow()).thenReturn(MutableStateFlow(emptyList()))

        whenever(pokemonRepository.getPokemonDetailFlow("25")).thenReturn(MutableStateFlow(null))
        whenever(pokemonRepository.syncPokemonDetail("25")).thenAnswer {
            throw IllegalStateException("offline")
        }

        val viewModel = TeamViewModel(teamRepository, typeRepository, pokemonRepository)

        viewModel.teamUiState.test {
            assertEquals(CompareSlotState.Empty, awaitItem().compareSlot1)

            viewModel.selectComparePokemon(1, "25")
            assertEquals(CompareSlotState.Loading, awaitItem().compareSlot1)
            assertEquals(CompareSlotState.Error("offline"), awaitItem().compareSlot1)

            viewModel.retryCompare(1)
            assertEquals(CompareSlotState.Loading, awaitItem().compareSlot1)
            assertEquals(CompareSlotState.Error("offline"), awaitItem().compareSlot1)

            cancelAndIgnoreRemainingEvents()
        }

        verify(pokemonRepository, times(2)).syncPokemonDetail("25")
    }

    @Test
    fun selectComparePokemonDoesNotMutateTeamRoster() = runTest {
        val teamRepository = mock<TeamRepository>()
        val typeRepository = mock<TypeRepository>()
        val pokemonRepository = mock<PokemonRepository>()

        whenever(teamRepository.getTeamFlow()).thenReturn(MutableStateFlow(emptyList()))
        whenever(typeRepository.getAllTypesFlow()).thenReturn(MutableStateFlow(emptyList()))
        whenever(pokemonRepository.getPokemonDetailFlow("1")).thenReturn(MutableStateFlow(null))

        val viewModel = TeamViewModel(teamRepository, typeRepository, pokemonRepository)

        viewModel.selectComparePokemon(1, "1")
        viewModel.selectComparePokemon(2, "4")

        // Proves choosing comparison Pokémon does not mutate or call teamRepository.addOrReplace
        verify(teamRepository, never()).addOrReplace(any(), any(), any(), any(), any())
        verify(teamRepository, never()).remove(any())
    }
}
