package com.samidevstudio.pocketdex.ui.pokemon

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.DefaultPokemonRepository
import com.samidevstudio.pocketdex.data.PokemonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the state for both the list and detail screens.
 */
class PokemonViewModel(
    private val repository: PokemonRepository = DefaultPokemonRepository()
) : ViewModel() {

    private val allPokemon = mutableListOf<PokemonUiModel>()
    private var currentOffset = 0
    private var isFetching = false

    var listUiState: PokemonListUiState by mutableStateOf(PokemonListUiState.Loading)
        private set

    var detailUiState: PokemonDetailUiState by mutableStateOf(PokemonDetailUiState.Loading)
        private set

    init {
        viewModelScope.launch {
            fetchNamesBatch()
            fetchNamesBatch()
            backfillTypes()
        }
    }

    fun loadPokemonDetail(id: String) {
        val cached = repository.getCachedPokemonDetail(id)
        if (cached != null) {
            detailUiState = PokemonDetailUiState.Success(cached)
            return
        }

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
