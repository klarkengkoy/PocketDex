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
import kotlinx.coroutines.flow.combine
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
    private val _searchQuery = MutableStateFlow("")
    private val _typeFilter = MutableStateFlow<Set<String>>(emptySet())

    val searchQuery: StateFlow<String> = _searchQuery
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ""
        )

    val typeFilter: StateFlow<Set<String>> = _typeFilter
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    val listUiState: StateFlow<PokemonListUiState> = combine(
        repository.getPokemonListFlow(),
        _searchQuery,
        _typeFilter
    ) { list, query, typeFilter ->
        // Keep offset in sync with what's actually in the DB
        if (list.isNotEmpty() && !isFetching) {
            currentOffset = list.size
        }

        val filteredList = applyPokemonSearch(query, list, typeFilter)

        if (list.isEmpty()) {
            if (currentOffset == 0) fetchNamesBatch()
            PokemonListUiState.Loading
        } else {
            PokemonListUiState.Success(filteredList)
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PokemonListUiState.Loading
        )

    private val _currentPokemonId = MutableStateFlow<String?>(null)

    /**
     * The ID of the Pokémon currently being focused for shared element transitions.
     * This is updated when a card is clicked in the list, and as the user swipes
     * through the evolution family in the detail screen.
     */
    val activePokemonId = MutableStateFlow<String?>(null)

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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query.trim()
    }

    fun toggleTypeFilter(type: String) {
        val normalizedType = type.trim().lowercase()
        if (normalizedType.isEmpty()) return

        _typeFilter.value = _typeFilter.value.toMutableSet().apply {
            if (contains(normalizedType)) remove(normalizedType) else add(normalizedType)
        }
    }

    fun clearTypeFilters() {
        _typeFilter.value = emptySet()
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
        val TYPE_FILTER_OPTIONS = listOf(
            "fire",
            "water",
            "grass",
            "electric",
            "psychic",
            "ice",
            "fighting",
            "poison",
            "bug",
            "rock",
            "ground",
            "flying",
            "normal",
            "ghost",
            "dragon",
            "fairy",
            "dark",
            "steel"
        )

        fun applyPokemonSearch(
            query: String,
            list: List<PokemonUiModel>,
            typeFilter: Set<String> = emptySet()
        ): List<PokemonUiModel> {
            val normalizedQuery = query.trim().lowercase()
            val normalizedTypeFilter = typeFilter.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()

            return list.filter { pokemon ->
                val matchesText = normalizedQuery.isEmpty() ||
                    pokemon.name.lowercase().contains(normalizedQuery) ||
                    pokemon.id.lowercase().contains(normalizedQuery) ||
                    pokemon.types.any { it.lowercase().contains(normalizedQuery) }

                val matchesType = normalizedTypeFilter.isEmpty() ||
                    pokemon.types.any { it.lowercase() in normalizedTypeFilter }

                matchesText && matchesType
            }
        }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PocketDexApplication)
                val pokemonRepository = application.container.pokemonRepository
                PokemonViewModel(repository = pokemonRepository)
            }
        }
    }
}
