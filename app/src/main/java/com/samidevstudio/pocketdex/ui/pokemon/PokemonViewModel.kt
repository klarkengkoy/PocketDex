package com.samidevstudio.pocketdex.ui.pokemon

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.DefaultPokemonRepository
import com.samidevstudio.pocketdex.data.PokemonRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 60

/**
 * ViewModel that manages the state for both the list and detail screens.
 */
class PokemonViewModel(
    private val repository: PokemonRepository = DefaultPokemonRepository()
) : ViewModel() {

    private val allPokemon = mutableListOf<PokemonUiModel>()
    private var currentOffset = 0
    private var isFetching = false

    // Tracks in-progress detail fetches to prevent redundant network calls
    private val activeDetailJobs = mutableMapOf<String, Deferred<PokemonDetailModel?>>()

    var listUiState: PokemonListUiState by mutableStateOf(PokemonListUiState.Loading)
        private set

    var detailUiState: PokemonDetailUiState by mutableStateOf(PokemonDetailUiState.Loading)
        private set

    init {
        viewModelScope.launch {
            fetchNamesBatch()
            backfillTypes()
        }
    }

    /**
     * Loads detail for a specific Pokémon. 
     * Uses the cache first, then checks for any active backfilling jobs before firing a new request.
     */
    fun loadPokemonDetail(id: String) {
        val cached = repository.getCachedPokemonDetail(id)
        if (cached != null) {
            detailUiState = PokemonDetailUiState.Success(cached)
            return
        }

        viewModelScope.launch {
            detailUiState = PokemonDetailUiState.Loading
            detailUiState = try {
                // If a backfill job is already fetching this ID, wait for it instead of starting a new one
                val detailModel = activeDetailJobs[id]?.await() ?: repository.getPokemonDetail(id)
                PokemonDetailUiState.Success(detailModel)
            } catch (e: Exception) {
                PokemonDetailUiState.Error(e.message ?: "Failed to load details")
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
            allPokemon.addAll(newItems)
            listUiState = PokemonListUiState.Success(allPokemon.toList())
            currentOffset += PAGE_SIZE
        } catch (e: Exception) {
            if (allPokemon.isEmpty()) {
                listUiState = PokemonListUiState.Error(e.message ?: "Network error")
            }
        } finally {
            isFetching = false
        }
    }

    private suspend fun backfillTypes() {
        val itemsToUpdate = allPokemon.mapIndexedNotNull { index, model ->
            if (model.types.isEmpty()) index else null
        }
        
        itemsToUpdate.chunked(20).forEach { batchIndices ->
            val jobs = batchIndices.map { index ->
                val id = allPokemon[index].id
                val deferred = viewModelScope.async {
                    try {
                        repository.getPokemonDetail(id)
                    } catch (_: Exception) {
                        null
                    }
                }
                // Store the job so loadPokemonDetail() can "hitch a ride" on it
                activeDetailJobs[id] = deferred
                index to deferred
            }

            val results = jobs.map { (index, deferred) ->
                val detail = deferred.await()
                // Clean up job map once done
                activeDetailJobs.remove(allPokemon[index].id)
                index to (detail?.types ?: emptyList())
            }

            results.forEach { (index, types) ->
                allPokemon[index] = allPokemon[index].copy(types = types)
            }
            listUiState = PokemonListUiState.Success(allPokemon.toList())
        }
    }
}
