package com.samidevstudio.pocketdex.ui.region

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.RegionRepository
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
class RegionViewModel @Inject constructor(
    private val repository: RegionRepository
) : ViewModel() {

    private var currentOffset = 0
    private var isFetching = false
    private val _listError = MutableStateFlow<String?>(null)
    private val _detailError = MutableStateFlow<Pair<String, String>?>(null)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val listUiState: StateFlow<RegionListUiState> = combine(
        repository.getRegionListFlow(MAX_CACHED, 0),
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
            if (errorMessage != null) RegionListUiState.Error(errorMessage)
            else {
                if (currentOffset == 0) fetchNextBatch()
                RegionListUiState.Loading
            }
        } else {
            RegionListUiState.Success(filtered, errorMessage)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RegionListUiState.Loading)

    private val _currentRegionId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailUiState: StateFlow<RegionDetailUiState> = _currentRegionId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest flowOf(RegionDetailUiState.Loading)
            combine(repository.getRegionDetailFlow(id), _detailError) { detail, failure ->
                when {
                    detail != null -> RegionDetailUiState.Success(detail)
                    failure?.first == id -> RegionDetailUiState.Error(failure.second)
                    else -> RegionDetailUiState.Loading
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RegionDetailUiState.Loading)

    fun loadRegionDetail(id: String?) {
        _currentRegionId.value = id
        _detailError.value = null
        id?.let(::syncRegionDetail)
    }

    fun retryRegionDetail() {
        _currentRegionId.value?.let { id ->
            _detailError.value = null
            syncRegionDetail(id)
        }
    }

    private fun syncRegionDetail(id: String) {
        viewModelScope.launch {
            try {
                repository.syncRegionDetail(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_currentRegionId.value == id) {
                    _detailError.value = id to (e.message ?: "Could not load region details")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadMore() {
        val state = listUiState.value
        if (state is RegionListUiState.Error ||
            (state is RegionListUiState.Success && state.errorMessage != null)
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
                repository.fetchRegionList(offset = currentOffset, limit = PAGE_SIZE)
                currentOffset += PAGE_SIZE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _listError.value = e.message ?: "Could not load regions"
            } finally {
                isFetching = false
            }
        }
    }
}
