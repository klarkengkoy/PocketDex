package com.samidevstudio.pocketdex.ui.pokemon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samidevstudio.pocketdex.PocketDexApplication
import com.samidevstudio.pocketdex.data.PokemonRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 60

class PokemonViewModel(
    private val repository: PokemonRepository,
) : ViewModel() {

    private var currentOffset = 0
    private var isFetching = false

    val listUiState: StateFlow<PokemonListUiState> = repository.getPokemonListFlow()
        .map { list ->
            // Keep offset in sync with what's actually in the DB
            if (list.isNotEmpty() && !isFetching) {
                currentOffset = list.size
            }
            
            if (list.isEmpty()) {
                if (currentOffset == 0) fetchNamesBatch()
                PokemonListUiState.Loading
            } else {
                PokemonListUiState.Success(list)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PokemonListUiState.Loading
        )

    private val _currentPokemonId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailUiState: StateFlow<PokemonDetailUiState> = _currentPokemonId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest flowOf(PokemonDetailUiState.Loading)
            
            // Observe DB and combine with evolution chain
            repository.getPokemonDetailFlow(id).flatMapLatest { detail ->
                if (detail == null) {
                    // Trigger sync if not in DB
                    viewModelScope.launch { repository.syncPokemonDetail(id) }
                    flowOf(PokemonDetailUiState.Loading)
                } else {
                    repository.getEvolutionChainFlow(detail.chainId).map { evolutions ->
                        PokemonDetailUiState.Success(detail.copy(evolutions = evolutions))
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PokemonDetailUiState.Loading
        )

    init {
        viewModelScope.launch {
            repository.backfillMissingTypes()
        }
    }

    fun loadPokemonDetail(id: String?) {
        _currentPokemonId.value = id
    }

    fun loadMore() {
        fetchNamesBatch()
    }

    private fun fetchNamesBatch() {
        if (isFetching) return
        isFetching = true
        viewModelScope.launch {
            try {
                repository.fetchPokemonList(offset = currentOffset, limit = PAGE_SIZE)
                currentOffset += PAGE_SIZE
                // Trigger backfill for the newly added batch
                repository.backfillMissingTypes()
            } catch (_: Exception) {
                // Handle error
            } finally {
                isFetching = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PocketDexApplication)
                val pokemonRepository = application.container.pokemonRepository
                PokemonViewModel(repository = pokemonRepository)
            }
        }
    }
}
