package com.samidevstudio.pocketdex.ui.pokemon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

private const val PAGE_SIZE = 60

@HiltViewModel
class PokemonViewModel @Inject constructor(
    private val repository: PokemonRepository,
) : ViewModel() {

    private var currentOffset = 0
    private var isFetching = false
    private val _listError = MutableStateFlow<String?>(null)
    private val _detailError = MutableStateFlow<Pair<String, String>?>(null)
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
        _typeFilter,
        _listError
    ) { list, query, typeFilter, errorMessage ->
        // Keep offset in sync with what's actually in the DB
        if (list.isNotEmpty() && !isFetching) {
            currentOffset = list.size
        }

        val filteredList = applyPokemonSearch(query, list, typeFilter)

        if (list.isEmpty()) {
            if (errorMessage != null) PokemonListUiState.Error(errorMessage)
            else {
                if (currentOffset == 0) fetchNamesBatch()
                PokemonListUiState.Loading
            }
        } else {
            PokemonListUiState.Success(filteredList, errorMessage)
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PokemonListUiState.Loading
        )

    private val _currentPokemonId = MutableStateFlow<String?>(null)
    private var detailOwner: Any? = null
    private val selectedDetailSyncMutex = Mutex()
    private var selectedDetailSyncJob: Job? = null
    private var selectedDetailSyncId: String? = null

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

            val detailState = repository.getPokemonDetailFlow(id).flatMapLatest { detail ->
                if (detail == null) flowOf(PokemonDetailUiState.Loading)
                else repository.getEvolutionChainFlow(detail.chainId).map { evolutions ->
                    PokemonDetailUiState.Success(detail.copy(evolutions = evolutions))
                }
            }
            combine(detailState, _detailError) { state, failure ->
                if (failure?.first == id) {
                    if (state is PokemonDetailUiState.Success) state.copy(errorMessage = failure.second)
                    else PokemonDetailUiState.Error(failure.second)
                } else state
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PokemonDetailUiState.Loading
        )

    fun loadPokemonDetail(id: String?, owner: Any? = null) {
        detailOwner = owner
        setPokemonDetail(id)
    }

    fun updatePokemonDetail(id: String, owner: Any) {
        if (detailOwner === owner) setPokemonDetail(id)
    }

    private fun setPokemonDetail(id: String?) {
        _currentPokemonId.value = id
        _detailError.value = null
        if (id == null) {
            selectedDetailSyncJob?.cancel()
            selectedDetailSyncJob = null
            selectedDetailSyncId = null
        } else {
            syncSelectedPokemonDetail(id)
        }
    }

    fun clearPokemonDetail(owner: Any) {
        if (detailOwner === owner) {
            detailOwner = null
            setPokemonDetail(null)
        }
    }

    fun retryPokemonDetail() {
        _currentPokemonId.value?.let { id ->
            _detailError.value = null
            syncSelectedPokemonDetail(id, force = true)
        }
    }

    fun refreshPokemon(id: String) {
        syncPokemonDetail(id)
    }

    private fun syncSelectedPokemonDetail(id: String, force: Boolean = false) {
        if (!force && selectedDetailSyncId == id && selectedDetailSyncJob?.isActive == true) return

        selectedDetailSyncJob?.cancel()
        selectedDetailSyncId = id
        selectedDetailSyncJob = viewModelScope.launch {
            selectedDetailSyncMutex.withLock {
                if (_currentPokemonId.value == id) syncPokemonDetailRequest(id)
            }
        }
    }

    private fun syncPokemonDetail(id: String) {
        viewModelScope.launch { syncPokemonDetailRequest(id) }
    }

    private suspend fun syncPokemonDetailRequest(id: String) {
        try {
            repository.syncPokemonDetail(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (_currentPokemonId.value == id) {
                _detailError.value = id to (e.message ?: "Could not load Pokémon details")
            }
        }
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

    fun clearFilters() {
        _searchQuery.value = ""
        _typeFilter.value = emptySet()
    }

    fun loadMore() {
        val state = listUiState.value
        if (state is PokemonListUiState.Error ||
            (state is PokemonListUiState.Success && state.errorMessage != null)
        ) return
        fetchNamesBatch()
    }

    fun retryLoad() {
        _listError.value = null
        fetchNamesBatch()
    }

    private fun fetchNamesBatch() {
        if (isFetching) return
        isFetching = true
        _listError.value = null
        viewModelScope.launch {
            try {
                repository.fetchPokemonList(offset = currentOffset, limit = PAGE_SIZE)
                currentOffset += PAGE_SIZE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _listError.value = e.message ?: "Could not load Pokémon"
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
    }
}
