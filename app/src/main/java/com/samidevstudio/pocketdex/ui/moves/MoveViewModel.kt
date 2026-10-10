package com.samidevstudio.pocketdex.ui.moves

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.MoveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 60
private const val MAX_CACHED = 2000

@HiltViewModel
class MoveViewModel @Inject constructor(
    private val repository: MoveRepository
) : ViewModel() {

    private var currentOffset = 0
    private var isFetching = false
    private val _listError = MutableStateFlow<String?>(null)
    private val _detailError = MutableStateFlow<Pair<String, String>?>(null)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val listUiState: StateFlow<MoveListUiState> = combine(
        repository.getMoveListFlow(MAX_CACHED, 0),
        _searchQuery,
        _listError
    ) { list, query, errorMessage ->
        if (list.isNotEmpty() && !isFetching) currentOffset = list.size

        val normalizedQuery = query.trim().lowercase()
        val filtered = if (normalizedQuery.isEmpty()) {
            list
        } else {
            list.filter { it.name.lowercase().contains(normalizedQuery) }
        }

        if (list.isEmpty()) {
            if (errorMessage != null) MoveListUiState.Error(errorMessage)
            else {
                if (currentOffset == 0) fetchNextBatch()
                MoveListUiState.Loading
            }
        } else {
            MoveListUiState.Success(filtered, errorMessage)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MoveListUiState.Loading)

    private val _currentMoveId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailUiState: StateFlow<MoveDetailUiState> = _currentMoveId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest flowOf(MoveDetailUiState.Loading)
            combine(repository.getMoveDetailFlow(id), _detailError) { detail, failure ->
                when {
                    detail != null -> MoveDetailUiState.Success(detail)
                    failure?.first == id -> MoveDetailUiState.Error(failure.second)
                    else -> MoveDetailUiState.Loading
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MoveDetailUiState.Loading)

    fun loadMoveDetail(id: String?) {
        _currentMoveId.value = id
        _detailError.value = null
        id?.let(::syncMoveDetail)
    }

    fun retryMoveDetail() {
        _currentMoveId.value?.let { id ->
            _detailError.value = null
            syncMoveDetail(id)
        }
    }

    private fun syncMoveDetail(id: String) {
        viewModelScope.launch {
            try {
                repository.syncMoveDetail(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_currentMoveId.value == id) {
                    _detailError.value = id to (e.message ?: "Could not load move details")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadMore() {
        val state = listUiState.value
        if (state is MoveListUiState.Error ||
            (state is MoveListUiState.Success && state.errorMessage != null)
        ) return
        fetchNextBatch()
    }

    fun retryLoad() {
        _listError.value = null
        fetchNextBatch()
    }

    private fun fetchNextBatch() {
        if (isFetching) return
        isFetching = true
        _listError.value = null
        viewModelScope.launch {
            try {
                repository.fetchMoveList(offset = currentOffset, limit = PAGE_SIZE)
                currentOffset += PAGE_SIZE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _listError.value = e.message ?: "Could not load moves"
            } finally {
                isFetching = false
            }
        }
    }
}
