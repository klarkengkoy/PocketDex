package com.samidevstudio.pocketdex.ui.berry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.BerryRepository
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
class BerryViewModel @Inject constructor(
    private val repository: BerryRepository
) : ViewModel() {

    private var currentOffset = 0
    private var isFetching = false
    private val _listError = MutableStateFlow<String?>(null)
    private val _detailError = MutableStateFlow<Pair<String, String>?>(null)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val listUiState: StateFlow<BerryListUiState> = combine(
        repository.getBerryListFlow(MAX_CACHED, 0),
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
            if (errorMessage != null) BerryListUiState.Error(errorMessage)
            else {
                if (currentOffset == 0) fetchNextBatch()
                BerryListUiState.Loading
            }
        } else {
            BerryListUiState.Success(filtered, errorMessage)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BerryListUiState.Loading)

    private val _currentBerryId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailUiState: StateFlow<BerryDetailUiState> = _currentBerryId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest flowOf(BerryDetailUiState.Loading)
            combine(repository.getBerryDetailFlow(id), _detailError) { detail, failure ->
                when {
                    detail != null -> BerryDetailUiState.Success(detail)
                    failure?.first == id -> BerryDetailUiState.Error(failure.second)
                    else -> BerryDetailUiState.Loading
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BerryDetailUiState.Loading)

    fun loadBerryDetail(id: String?) {
        _currentBerryId.value = id
        _detailError.value = null
        id?.let(::syncBerryDetail)
    }

    fun retryBerryDetail() {
        _currentBerryId.value?.let { id ->
            _detailError.value = null
            syncBerryDetail(id)
        }
    }

    private fun syncBerryDetail(id: String) {
        viewModelScope.launch {
            try {
                repository.syncBerryDetail(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_currentBerryId.value == id) {
                    _detailError.value = id to (e.message ?: "Could not load berry details")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadMore() {
        val state = listUiState.value
        if (state is BerryListUiState.Error ||
            (state is BerryListUiState.Success && state.errorMessage != null)
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
                repository.fetchBerryList(offset = currentOffset, limit = PAGE_SIZE)
                currentOffset += PAGE_SIZE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _listError.value = e.message ?: "Could not load berries"
            } finally {
                isFetching = false
            }
        }
    }
}
