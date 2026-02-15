package com.samidevstudio.pocketdex.ui.pokemon

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samidevstudio.pocketdex.PocketDexApplication
import com.samidevstudio.pocketdex.data.PokemonRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 60

class PokemonViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _allPokemon = mutableStateListOf<PokemonUiModel>()

    var listUiState: PokemonListUiState by mutableStateOf(PokemonListUiState.Loading)
        private set

    var detailUiState: PokemonDetailUiState by mutableStateOf(PokemonDetailUiState.Loading)
        private set

    private var currentOffset = 0
    private var isFetching = false

    private val activeDetailJobs = mutableMapOf<String, Deferred<PokemonDetailModel?>>()

    init {
        viewModelScope.launch {
            fetchNamesBatch()
            backfillTypes()
        }
    }

    /**
     * @param isInitialEntry When true, we're coming from the list, so clear old data.
     */
    fun loadPokemonDetail(id: String, isInitialEntry: Boolean = false) {
        if (isInitialEntry) {
            detailUiState = PokemonDetailUiState.Loading
        }

        val cached = repository.getCachedPokemonDetail(id)
        if (cached != null) {
            detailUiState = PokemonDetailUiState.Success(cached)
            return
        }

        viewModelScope.launch {
            try {
                val detailModel = activeDetailJobs[id]?.await() ?: repository.getPokemonDetail(id)
                detailUiState = PokemonDetailUiState.Success(detailModel)
            } catch (e: Exception) {
                if (detailUiState is PokemonDetailUiState.Loading) {
                    detailUiState = PokemonDetailUiState.Error(e.message ?: "Failed to load details")
                }
            }
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            fetchNamesBatch()
            backfillTypes()
        }
    }

    private suspend fun fetchNamesBatch() {
        if (isFetching) return
        isFetching = true
        try {
            val newItems = repository.getPokemonList(offset = currentOffset, limit = PAGE_SIZE)
            _allPokemon.addAll(newItems)
            
            if (listUiState !is PokemonListUiState.Success) {
                listUiState = PokemonListUiState.Success(_allPokemon)
            }
            
            currentOffset += PAGE_SIZE
        } catch (e: Exception) {
            if (_allPokemon.isEmpty()) {
                listUiState = PokemonListUiState.Error(e.message ?: "Network error")
            }
        } finally {
            isFetching = false
        }
    }

    private suspend fun backfillTypes() {
        val itemsToUpdate = _allPokemon.mapIndexedNotNull { index, model ->
            if (model.types.isEmpty()) index else null
        }
        
        itemsToUpdate.chunked(20).forEach { batchIndices ->
            val jobs = batchIndices.map { index ->
                val id = _allPokemon[index].id
                val deferred = viewModelScope.async {
                    try {
                        repository.getPokemonDetail(id)
                    } catch (_: Exception) {
                        null
                    }
                }
                activeDetailJobs[id] = deferred
                index to deferred
            }

            jobs.forEach { (index, deferred) ->
                val detail = deferred.await()
                activeDetailJobs -= _allPokemon[index].id
                
                if (detail != null) {
                    _allPokemon[index] = _allPokemon[index].copy(types = detail.types)
                }
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
