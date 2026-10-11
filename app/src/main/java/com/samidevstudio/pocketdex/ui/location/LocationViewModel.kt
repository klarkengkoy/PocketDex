package com.samidevstudio.pocketdex.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.LocationRepository
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
class LocationViewModel @Inject constructor(
    private val repository: LocationRepository
) : ViewModel() {

    private var currentOffset = 0
    private var isFetching = false
    private val _listError = MutableStateFlow<String?>(null)
    private val _detailError = MutableStateFlow<Pair<String, String>?>(null)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val listUiState: StateFlow<LocationListUiState> = combine(
        repository.getLocationListFlow(MAX_CACHED, 0),
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
            if (errorMessage != null) LocationListUiState.Error(errorMessage)
            else {
                if (currentOffset == 0) fetchNextBatch()
                LocationListUiState.Loading
            }
        } else {
            LocationListUiState.Success(filtered, errorMessage)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocationListUiState.Loading)

    private val _currentLocationId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailUiState: StateFlow<LocationDetailUiState> = _currentLocationId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest flowOf(LocationDetailUiState.Loading)
            combine(repository.getLocationDetailFlow(id), _detailError) { detail, failure ->
                when {
                    detail != null -> LocationDetailUiState.Success(detail)
                    failure?.first == id -> LocationDetailUiState.Error(failure.second)
                    else -> LocationDetailUiState.Loading
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocationDetailUiState.Loading)

    fun loadLocationDetail(id: String?) {
        _currentLocationId.value = id
        _detailError.value = null
        id?.let(::syncLocationDetail)
    }

    fun retryLocationDetail() {
        _currentLocationId.value?.let { id ->
            _detailError.value = null
            syncLocationDetail(id)
        }
    }

    private fun syncLocationDetail(id: String) {
        viewModelScope.launch {
            try {
                repository.syncLocationDetail(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_currentLocationId.value == id) {
                    _detailError.value = id to (e.message ?: "Could not load location details")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadMore() {
        val state = listUiState.value
        if (state is LocationListUiState.Error ||
            (state is LocationListUiState.Success && state.errorMessage != null)
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
                repository.fetchLocationList(offset = currentOffset, limit = PAGE_SIZE)
                currentOffset += PAGE_SIZE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _listError.value = e.message ?: "Could not load locations"
            } finally {
                isFetching = false
            }
        }
    }
}
