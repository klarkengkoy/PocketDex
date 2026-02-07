package com.samidevstudio.pocketdex.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.DefaultPokemonRepository
import com.samidevstudio.pocketdex.data.PokemonRepository
import com.samidevstudio.pocketdex.data.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the state for both the list and detail screens.
 * Uses the Repository pattern to fetch data.
 */
class PokemonViewModel(
    private val repository: PokemonRepository = DefaultPokemonRepository()
) : ViewModel() {

    private val allPokemon = mutableListOf<PokemonUiModel>()
    private var currentOffset = 0
    private var isFetching = false

    // State for the main Pokémon grid
    var listUiState: PokemonListUiState by mutableStateOf(PokemonListUiState.Loading)
        private set

    // State for the detail screen, one Pokémon at a time
    var detailUiState: PokemonDetailUiState by mutableStateOf(PokemonDetailUiState.Loading)
        private set

    init {
        viewModelScope.launch {
            fetchNamesBatch()
            fetchNamesBatch()
            backfillTypes()
        }
    }

    /**
     * Fetches detailed information for a single Pokémon using the repository.
     */
    fun loadPokemonDetail(id: String) {
        viewModelScope.launch {
            detailUiState = PokemonDetailUiState.Loading
            try {
                val detailModel = repository.getPokemonDetail(id)
                detailUiState = PokemonDetailUiState.Success(detailModel)
            } catch (e: Exception) {
                detailUiState = PokemonDetailUiState.Error(e.message ?: "Failed to load details")
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
            val newItems = repository.getPokemonList(offset = currentOffset)
            allPokemon.addAll(newItems)
            listUiState = PokemonListUiState.Success(allPokemon.toList())
            currentOffset += 20
        } catch (e: Exception) {
            if (allPokemon.isEmpty()) {
                listUiState = PokemonListUiState.Error(e.message ?: "Network error")
            }
        } finally {
            isFetching = false
        }
    }

    /**
     * Backfills Pokémon types for the list screen.
     * Keeps the list snappy by showing basic info first.
     */
    private suspend fun backfillTypes() {
        val itemsToUpdate = allPokemon.mapIndexedNotNull { index, model ->
            if (model.types.isEmpty()) index else null
        }
        itemsToUpdate.chunked(20).forEach { batchIndices ->
            val jobs = batchIndices.map { index ->
                viewModelScope.async {
                    try {
                        val detail = repository.getPokemonDetail(allPokemon[index].id)
                        index to detail.types
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            val results = jobs.awaitAll().filterNotNull()
            results.forEach { (index, types) ->
                allPokemon[index] = allPokemon[index].copy(types = types)
            }
            listUiState = PokemonListUiState.Success(allPokemon.toList())
        }
    }
}
