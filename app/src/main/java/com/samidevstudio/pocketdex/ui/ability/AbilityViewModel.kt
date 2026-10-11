package com.samidevstudio.pocketdex.ui.ability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.AbilityRepository
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
class AbilityViewModel @Inject constructor(
    private val repository: AbilityRepository
) : ViewModel() {

    private var currentOffset = 0
    private var isFetching = false
    private val _listError = MutableStateFlow<String?>(null)
    private val _detailError = MutableStateFlow<Pair<String, String>?>(null)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val listUiState: StateFlow<AbilityListUiState> = combine(
        repository.getAbilityListFlow(MAX_CACHED, 0),
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
            if (errorMessage != null) AbilityListUiState.Error(errorMessage)
            else {
                if (currentOffset == 0) fetchNextBatch()
                AbilityListUiState.Loading
            }
        } else {
            AbilityListUiState.Success(filtered, errorMessage)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AbilityListUiState.Loading)

    private val _currentAbilityId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailUiState: StateFlow<AbilityDetailUiState> = _currentAbilityId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest flowOf(AbilityDetailUiState.Loading)
            combine(repository.getAbilityDetailFlow(id), _detailError) { detail, failure ->
                when {
                    detail != null -> AbilityDetailUiState.Success(detail)
                    failure?.first == id -> AbilityDetailUiState.Error(failure.second)
                    else -> AbilityDetailUiState.Loading
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AbilityDetailUiState.Loading)

    fun loadAbilityDetail(id: String?) {
        _currentAbilityId.value = id
        _detailError.value = null
        id?.let(::syncAbilityDetail)
    }

    fun retryAbilityDetail() {
        _currentAbilityId.value?.let { id ->
            _detailError.value = null
            syncAbilityDetail(id)
        }
    }

    private fun syncAbilityDetail(id: String) {
        viewModelScope.launch {
            try {
                repository.syncAbilityDetail(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_currentAbilityId.value == id) {
                    _detailError.value = id to (e.message ?: "Could not load ability details")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadMore() {
        val state = listUiState.value
        if (state is AbilityListUiState.Error ||
            (state is AbilityListUiState.Success && state.errorMessage != null)
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
                repository.fetchAbilityList(offset = currentOffset, limit = PAGE_SIZE)
                currentOffset += PAGE_SIZE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _listError.value = e.message ?: "Could not load abilities"
            } finally {
                isFetching = false
            }
        }
    }
}
