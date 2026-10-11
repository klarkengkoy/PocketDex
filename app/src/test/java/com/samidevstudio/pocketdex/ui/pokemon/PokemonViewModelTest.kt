package com.samidevstudio.pocketdex.ui.pokemon

import app.cash.turbine.test
import com.samidevstudio.pocketdex.data.PokemonRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonViewModelTest {

    private lateinit var viewModel: PokemonViewModel
    private lateinit var repository: PokemonRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    private val pokemonListFlow = MutableStateFlow<List<PokemonUiModel>>(emptyList())
    private val pokemonDetailFlow = MutableStateFlow<PokemonDetailModel?>(null)
    private val evolutionChainFlow = MutableStateFlow<List<EvolutionNode>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        whenever(repository.getPokemonListFlow()).thenReturn(pokemonListFlow)
        whenever(repository.getPokemonDetailFlow(any())).thenReturn(pokemonDetailFlow)
        whenever(repository.getEvolutionChainFlow(any())).thenReturn(evolutionChainFlow)
        
        // Reset flows
        pokemonListFlow.value = emptyList()
        pokemonDetailFlow.value = null
        evolutionChainFlow.value = emptyList()

        viewModel = PokemonViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial listUiState is Loading`() = runTest {
        viewModel.listUiState.test {
            assertEquals(PokemonListUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `successful loading of pokemon list updates listUiState to Success`() = runTest {
        val pokemonList = listOf(
            PokemonUiModel("1", "Bulbasaur", "", listOf("Grass", "Poison")),
            PokemonUiModel("25", "Pikachu", "", listOf("Electric"))
        )
        
        viewModel.listUiState.test {
            assertEquals(PokemonListUiState.Loading, awaitItem())
            
            pokemonListFlow.value = pokemonList
            
            val state = awaitItem()
            assertTrue(state is PokemonListUiState.Success)
            assertEquals(pokemonList, (state as PokemonListUiState.Success).pokemonList)
        }
    }

    @Test
    fun `updateSearchQuery triggers filtering and updates searchQuery flow`() = runTest {
        val pokemonList = listOf(
            PokemonUiModel("1", "Bulbasaur", "", listOf("Grass", "Poison")),
            PokemonUiModel("25", "Pikachu", "", listOf("Electric"))
        )
        pokemonListFlow.value = pokemonList

        viewModel.searchQuery.test {
            assertEquals("", awaitItem())
            
            viewModel.updateSearchQuery("Pika")
            assertEquals("Pika", awaitItem())
        }

        viewModel.listUiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonListUiState.Success)
            val filteredList = (state as PokemonListUiState.Success).pokemonList
            assertEquals(1, filteredList.size)
            assertEquals("Pikachu", filteredList[0].name)
        }
    }

    @Test
    fun `toggleTypeFilter updates typeFilter flow and triggers filtering`() = runTest {
        val pokemonList = listOf(
            PokemonUiModel("1", "Bulbasaur", "", listOf("Grass", "Poison")),
            PokemonUiModel("4", "Charmander", "", listOf("Fire")),
            PokemonUiModel("25", "Pikachu", "", listOf("Electric"))
        )
        pokemonListFlow.value = pokemonList

        viewModel.typeFilter.test {
            assertEquals(emptySet<String>(), awaitItem())
            
            viewModel.toggleTypeFilter("Fire")
            assertEquals(setOf("fire"), awaitItem())
            
            viewModel.toggleTypeFilter("Fire")
            assertEquals(emptySet<String>(), awaitItem())
        }

        viewModel.updateSearchQuery("") // Clear search
        viewModel.toggleTypeFilter("Electric")
        
        viewModel.listUiState.test {
            val state = awaitItem()
            assertTrue(state is PokemonListUiState.Success)
            val filteredList = (state as PokemonListUiState.Success).pokemonList
            assertEquals(1, filteredList.size)
            assertEquals("Pikachu", filteredList[0].name)
        }
    }

    @Test
    fun `loadMore calls repository fetchPokemonList with correct offset`() = runTest {
        // Initial call if list is empty when subscribed
        viewModel.listUiState.test {
            awaitItem() // Loading
            verify(repository, atLeastOnce()).fetchPokemonList(eq(0), any())
        }
        
        // Test loadMore with existing items
        val pokemonList = List(60) { index -> 
            PokemonUiModel(index.toString(), "Pokemon $index", "", emptyList()) 
        }
        pokemonListFlow.value = pokemonList
        
        viewModel.listUiState.test {
            awaitItem() // Success
            viewModel.loadMore()
            verify(repository).fetchPokemonList(eq(60), eq(60))
        }
    }

    @Test
    fun `loadPokemonDetail triggers repository getPokemonDetailFlow and updates detailUiState`() = runTest {
        val pokemonId = "25"
        val pokemonDetail = PokemonDetailModel(
            id = pokemonId,
            name = "Pikachu",
            imageUrl = "",
            types = listOf("Electric"),
            height = 4,
            weight = 60,
            stats = emptyList(),
            chainId = "1"
        )
        val evolutions = listOf(
            EvolutionNode("25", "Pikachu", ""),
            EvolutionNode("26", "Raichu", "")
        )

        // Set up repo state BEFORE triggering the flow
        pokemonDetailFlow.value = pokemonDetail
        evolutionChainFlow.value = evolutions

        viewModel.detailUiState.test {
            // First item is initial Loading
            assertEquals(PokemonDetailUiState.Loading, awaitItem())
            
            viewModel.loadPokemonDetail(pokemonId)
            
            val successState = awaitItem()
            assertTrue(successState is PokemonDetailUiState.Success)
            val detail = (successState as PokemonDetailUiState.Success).pokemon
            assertEquals(pokemonId, detail.id)
            assertEquals(evolutions, detail.evolutions)
        }
    }

    @Test
    fun `loadPokemonDetail triggers sync when detail is null`() = runTest {
        val pokemonId = "150"
        
        viewModel.detailUiState.test {
            assertEquals(PokemonDetailUiState.Loading, awaitItem())
            
            viewModel.loadPokemonDetail(pokemonId)
            
            // Should still be Loading since repo returned null (default)
            // But verify sync was called
            verify(repository).syncPokemonDetail(pokemonId)
        }
    }

    @Test
    fun `rapid selection ignores late detail emissions from the previous pokemon`() = runTest {
        val olderId = "133"
        val selectedId = "134"
        val olderOwner = Any()
        val selectedOwner = Any()
        val olderFlow = MutableStateFlow<PokemonDetailModel?>(null)
        val selectedFlow = MutableStateFlow<PokemonDetailModel?>(null)
        val olderDetail = PokemonDetailModel(
            id = olderId,
            name = "eevee",
            imageUrl = "",
            types = listOf("Normal"),
            height = 3,
            weight = 65,
            stats = emptyList(),
            chainId = "1"
        )
        val selectedDetail = olderDetail.copy(id = selectedId, name = "vaporeon", chainId = "2")

        whenever(repository.getPokemonDetailFlow(olderId)).thenReturn(olderFlow)
        whenever(repository.getPokemonDetailFlow(selectedId)).thenReturn(selectedFlow)

        viewModel.detailUiState.test {
            assertEquals(PokemonDetailUiState.Loading, awaitItem())
            viewModel.loadPokemonDetail(olderId, olderOwner)
            viewModel.loadPokemonDetail(selectedId, selectedOwner)

            selectedFlow.value = selectedDetail
            assertEquals(selectedId, (awaitItem() as PokemonDetailUiState.Success).pokemon.id)

            olderFlow.value = olderDetail
            viewModel.clearPokemonDetail(olderOwner)
            expectNoEvents()
        }
    }

    @Test
    fun `rapid same family reselection cancels stale sync and loads the last pokemon`() = runTest {
        val firstId = "133"
        val middleId = "134"
        val firstFlow = MutableStateFlow<PokemonDetailModel?>(null)
        val middleFlow = MutableStateFlow<PokemonDetailModel?>(null)
        val firstDetail = PokemonDetailModel(
            id = firstId,
            name = "eevee",
            imageUrl = "",
            types = listOf("Normal"),
            height = 3,
            weight = 65,
            stats = emptyList(),
            chainId = "1"
        )
        val middleDetail = firstDetail.copy(id = middleId, name = "vaporeon")
        val firstSyncStarted = CompletableDeferred<Unit>()
        val inFlightIds = mutableSetOf<String>()
        var firstSyncPending = true
        var firstSyncCancelled = false

        repository = object : PokemonRepository {
            override fun getPokemonListFlow() = pokemonListFlow
            override suspend fun fetchPokemonList(offset: Int, limit: Int) = Unit
            override fun getPokemonDetailFlow(id: String) = when (id) {
                firstId -> firstFlow
                middleId -> middleFlow
                else -> MutableStateFlow(null)
            }
            override fun getEvolutionChainFlow(chainId: String) = evolutionChainFlow
            override suspend fun syncPokemonDetail(id: String) {
                if (!inFlightIds.add(id)) return
                try {
                    if (id == firstId && firstSyncPending) {
                        firstSyncPending = false
                        firstSyncStarted.complete(Unit)
                        try {
                            awaitCancellation()
                        } finally {
                            firstSyncCancelled = true
                        }
                    }
                    when (id) {
                        firstId -> firstFlow.value = firstDetail
                        middleId -> middleFlow.value = middleDetail
                    }
                } finally {
                    inFlightIds.remove(id)
                }
            }
            override suspend fun syncEvolutionChain(chainId: String) = Unit
            override suspend fun backfillMissingTypes() = Unit
        }
        viewModel = PokemonViewModel(repository)

        viewModel.detailUiState.test {
            assertEquals(PokemonDetailUiState.Loading, awaitItem())
            val owner = Any()
            viewModel.loadPokemonDetail(firstId, owner)
            assertTrue(firstSyncStarted.isCompleted)

            viewModel.loadPokemonDetail(middleId, owner)
            viewModel.loadPokemonDetail(firstId, owner)

            assertTrue(firstSyncCancelled)
            val finalState = viewModel.detailUiState.value as? PokemonDetailUiState.Success
            assertEquals(firstId, finalState?.pokemon?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `disposing outgoing detail owner does not clear incoming pokemon`() = runTest {
        val outgoingId = "133"
        val incomingId = "134"
        val outgoingOwner = Any()
        val incomingOwner = Any()
        val outgoingDetail = PokemonDetailModel(
            id = outgoingId,
            name = "eevee",
            imageUrl = "",
            types = listOf("Normal"),
            height = 3,
            weight = 65,
            stats = emptyList(),
            chainId = "1"
        )
        val incomingDetail = outgoingDetail.copy(id = incomingId, name = "vaporeon")
        whenever(repository.getPokemonDetailFlow(outgoingId)).thenReturn(kotlinx.coroutines.flow.flowOf(outgoingDetail))
        whenever(repository.getPokemonDetailFlow(incomingId)).thenReturn(kotlinx.coroutines.flow.flowOf(incomingDetail))

        viewModel.detailUiState.test {
            assertEquals(PokemonDetailUiState.Loading, awaitItem())
            viewModel.loadPokemonDetail(outgoingId, outgoingOwner)
            assertEquals(outgoingId, (awaitItem() as PokemonDetailUiState.Success).pokemon.id)

            viewModel.loadPokemonDetail(incomingId, incomingOwner)
            assertEquals(incomingId, (awaitItem() as PokemonDetailUiState.Success).pokemon.id)

            viewModel.updatePokemonDetail("135", outgoingOwner)
            viewModel.clearPokemonDetail(outgoingOwner)
            expectNoEvents()
        }
    }
}
