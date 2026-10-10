package com.samidevstudio.pocketdex.ui

import app.cash.turbine.test
import com.samidevstudio.pocketdex.data.ItemRepository
import com.samidevstudio.pocketdex.data.MoveRepository
import com.samidevstudio.pocketdex.data.PokemonRepository
import com.samidevstudio.pocketdex.ui.items.ItemDetailUiState
import com.samidevstudio.pocketdex.ui.items.ItemListModel
import com.samidevstudio.pocketdex.ui.items.ItemListUiState
import com.samidevstudio.pocketdex.ui.items.ItemViewModel
import com.samidevstudio.pocketdex.ui.moves.MoveDetailUiState
import com.samidevstudio.pocketdex.ui.moves.MoveListModel
import com.samidevstudio.pocketdex.ui.moves.MoveListUiState
import com.samidevstudio.pocketdex.ui.moves.MoveViewModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailUiState
import com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonListUiState
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DataLoadingViewModelTest {
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
    fun pokemonPaginationFailureRetriesSameOffsetThenAdvancesAfterSuccess() = runTest {
        val repository = mock<PokemonRepository>()
        val cached = List(60) { PokemonUiModel("$it", "pokemon$it", "", emptyList()) }
        var attempts = 0
        whenever(repository.getPokemonListFlow()).thenReturn(MutableStateFlow(cached))
        whenever(repository.fetchPokemonList(any(), any())).thenAnswer {
            if (attempts++ == 0) throw IllegalStateException("offline")
            Unit
        }
        val viewModel = PokemonViewModel(repository)

        viewModel.listUiState.test {
            assertEquals(PokemonListUiState.Success(cached), awaitItem())
            viewModel.loadMore()
            assertEquals(PokemonListUiState.Success(cached, "offline"), awaitItem())
            viewModel.loadMore()
            advanceUntilIdle()
            verify(repository).fetchPokemonList(eq(60), eq(60))
            viewModel.retryLoad()
            advanceUntilIdle()
            assertEquals(PokemonListUiState.Success(cached), viewModel.listUiState.value)
            viewModel.loadMore()
            advanceUntilIdle()
            verify(repository).fetchPokemonList(eq(120), eq(60))
            cancelAndIgnoreRemainingEvents()
        }
        verify(repository, times(2)).fetchPokemonList(eq(60), eq(60))
    }

    @Test
    fun pokemonDetailFailureCanRetry() = runTest {
        val repository = mock<PokemonRepository>()
        whenever(repository.getPokemonListFlow()).thenReturn(MutableStateFlow(emptyList()))
        whenever(repository.getPokemonDetailFlow("25")).thenReturn(MutableStateFlow(null))
        whenever(repository.syncPokemonDetail("25")).thenThrow(IllegalStateException("offline"))
        val viewModel = PokemonViewModel(repository)

        viewModel.detailUiState.test {
            assertEquals(PokemonDetailUiState.Loading, awaitItem())
            viewModel.loadPokemonDetail("25")
            assertEquals(PokemonDetailUiState.Error("offline"), awaitItem())
            viewModel.retryPokemonDetail()
            assertEquals(PokemonDetailUiState.Loading, awaitItem())
            assertEquals(PokemonDetailUiState.Error("offline"), awaitItem())
        }
        verify(repository, times(2)).syncPokemonDetail("25")
    }

    @Test
    fun cachedPokemonDetailKeepsDataAndExposesEvolutionRetryFailure() = runTest {
        val repository = mock<PokemonRepository>()
        whenever(repository.getPokemonListFlow()).thenReturn(MutableStateFlow(emptyList()))
        val detail = PokemonDetailModel(
            id = "25", name = "Pikachu", imageUrl = "", types = listOf("Electric"),
            height = 4, weight = 60, stats = emptyList(), chainId = "1"
        )
        whenever(repository.getPokemonDetailFlow("25")).thenReturn(MutableStateFlow(detail))
        whenever(repository.getEvolutionChainFlow("1")).thenReturn(MutableStateFlow(emptyList<EvolutionNode>()))
        whenever(repository.syncPokemonDetail("25")).thenThrow(IllegalStateException("offline"))
        val viewModel = PokemonViewModel(repository)

        viewModel.detailUiState.test {
            assertEquals(PokemonDetailUiState.Loading, awaitItem())
            viewModel.loadPokemonDetail("25")
            val state = awaitItem() as PokemonDetailUiState.Success
            assertEquals(detail, state.pokemon)
            if (state.errorMessage == null) {
                assertEquals("offline", (awaitItem() as PokemonDetailUiState.Success).errorMessage)
            } else {
                assertEquals("offline", state.errorMessage)
            }
        }
        verify(repository).syncPokemonDetail("25")
    }

    @Test
    fun movePaginationFailureCanRetrySameOffsetWithoutAutomaticRetryLoop() = runTest {
        val repository = mock<MoveRepository>()
        val cached = List(60) { MoveListModel("$it", "move$it") }
        whenever(repository.getMoveListFlow(any(), any())).thenReturn(MutableStateFlow(cached))
        whenever(repository.fetchMoveList(any(), any())).thenThrow(IllegalStateException("offline"))
        val viewModel = MoveViewModel(repository)

        viewModel.listUiState.test {
            assertEquals(MoveListUiState.Success(cached), awaitItem())
            viewModel.loadMore()
            assertEquals(MoveListUiState.Success(cached, "offline"), awaitItem())
            viewModel.loadMore()
            advanceUntilIdle()
            verify(repository).fetchMoveList(eq(60), eq(60))
            viewModel.retryLoad()
            advanceUntilIdle()
            assertEquals(MoveListUiState.Success(cached, "offline"), viewModel.listUiState.value)
            cancelAndIgnoreRemainingEvents()
        }
        verify(repository, times(2)).fetchMoveList(eq(60), eq(60))
    }

    @Test
    fun moveDetailFailureCanRetry() = runTest {
        val repository = mock<MoveRepository>()
        whenever(repository.getMoveListFlow(any(), any())).thenReturn(MutableStateFlow(emptyList()))
        whenever(repository.getMoveDetailFlow("1")).thenReturn(MutableStateFlow(null))
        whenever(repository.syncMoveDetail("1")).thenThrow(IllegalStateException("offline"))
        val viewModel = MoveViewModel(repository)

        viewModel.detailUiState.test {
            assertEquals(MoveDetailUiState.Loading, awaitItem())
            viewModel.loadMoveDetail("1")
            assertEquals(MoveDetailUiState.Error("offline"), awaitItem())
            viewModel.retryMoveDetail()
            assertEquals(MoveDetailUiState.Loading, awaitItem())
            assertEquals(MoveDetailUiState.Error("offline"), awaitItem())
        }
        verify(repository, times(2)).syncMoveDetail("1")
    }

    @Test
    fun itemPaginationFailureCanRetrySameOffsetWithoutAutomaticRetryLoop() = runTest {
        val repository = mock<ItemRepository>()
        val cached = List(60) { ItemListModel("$it", "item$it", "") }
        whenever(repository.getItemListFlow(any(), any())).thenReturn(MutableStateFlow(cached))
        whenever(repository.fetchItemList(any(), any())).thenThrow(IllegalStateException("offline"))
        val viewModel = ItemViewModel(repository)

        viewModel.listUiState.test {
            assertEquals(ItemListUiState.Success(cached), awaitItem())
            viewModel.loadMore()
            assertEquals(ItemListUiState.Success(cached, "offline"), awaitItem())
            viewModel.loadMore()
            advanceUntilIdle()
            verify(repository).fetchItemList(eq(60), eq(60))
            viewModel.retryLoad()
            advanceUntilIdle()
            assertEquals(ItemListUiState.Success(cached, "offline"), viewModel.listUiState.value)
            cancelAndIgnoreRemainingEvents()
        }
        verify(repository, times(2)).fetchItemList(eq(60), eq(60))
    }

    @Test
    fun itemDetailFailureCanRetry() = runTest {
        val repository = mock<ItemRepository>()
        whenever(repository.getItemListFlow(any(), any())).thenReturn(MutableStateFlow(emptyList()))
        whenever(repository.getItemDetailFlow("potion")).thenReturn(MutableStateFlow(null))
        whenever(repository.syncItemDetail("potion")).thenThrow(IllegalStateException("offline"))
        val viewModel = ItemViewModel(repository)

        viewModel.detailUiState.test {
            assertEquals(ItemDetailUiState.Loading, awaitItem())
            viewModel.loadItemDetail("potion")
            assertEquals(ItemDetailUiState.Error("offline"), awaitItem())
            viewModel.retryItemDetail()
            assertEquals(ItemDetailUiState.Loading, awaitItem())
            assertEquals(ItemDetailUiState.Error("offline"), awaitItem())
        }
        verify(repository, times(2)).syncItemDetail("potion")
    }
}
