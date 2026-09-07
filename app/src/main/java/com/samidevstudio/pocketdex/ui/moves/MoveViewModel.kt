package com.samidevstudio.pocketdex.ui.moves

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samidevstudio.pocketdex.PocketDexApplication
import com.samidevstudio.pocketdex.data.MoveRepository
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
private const val MAX_CACHED = 2000

class MoveViewModel(
    private val repository: MoveRepository
) : ViewModel() {

    private var currentOffset = 0
    private var isFetching = false
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val listUiState: StateFlow<MoveListUiState> = combine(
        repository.getMoveListFlow(MAX_CACHED, 0),
        _searchQuery
    ) { list, query ->
        if (list.isNotEmpty() && !isFetching) currentOffset = list.size

        val normalizedQuery = query.trim().lowercase()
        val filtered = if (normalizedQuery.isEmpty()) {
            list
        } else {
            list.filter { it.name.lowercase().contains(normalizedQuery) }
        }

        if (list.isEmpty()) {
            if (currentOffset == 0) fetchNextBatch()
            MoveListUiState.Loading
        } else {
            MoveListUiState.Success(filtered)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MoveListUiState.Loading)

    private val _currentMoveId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailUiState: StateFlow<MoveDetailUiState> = _currentMoveId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest flowOf(MoveDetailUiState.Loading)
            repository.getMoveDetailFlow(id).map { detail ->
                if (detail == null) {
                    viewModelScope.launch { repository.syncMoveDetail(id) }
                    MoveDetailUiState.Loading
                } else {
                    MoveDetailUiState.Success(detail)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MoveDetailUiState.Loading)

    fun loadMoveDetail(id: String?) {
        _currentMoveId.value = id
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadMore() {
        fetchNextBatch()
    }

    private fun fetchNextBatch() {
        if (isFetching) return
        isFetching = true
        viewModelScope.launch {
            try {
                repository.fetchMoveList(offset = currentOffset, limit = PAGE_SIZE)
                currentOffset += PAGE_SIZE
            } catch (_: Exception) {
                // Handled by leaving the list as-is; user can retry via loadMore.
            } finally {
                isFetching = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PocketDexApplication)
                MoveViewModel(repository = application.container.moveRepository)
            }
        }
    }
}
