package com.samidevstudio.pocketdex.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

private const val PAGE_SIZE = 60
private const val MAX_CACHED = 3000

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private var currentOffset = 0
    private var isFetching = false
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val listUiState: StateFlow<ItemListUiState> = combine(
        repository.getItemListFlow(MAX_CACHED, 0),
        _searchQuery,
        _categoryFilter
    ) { list, query, category ->
        if (list.isNotEmpty() && !isFetching) currentOffset = list.size

        val normalizedQuery = query.trim().lowercase()
        val filtered = list.filter { item ->
            val matchesQuery = normalizedQuery.isEmpty() || item.name.lowercase().contains(normalizedQuery)
            val matchesCategory = category.isNullOrEmpty() || item.category == category
            matchesQuery && matchesCategory
        }

        if (list.isEmpty()) {
            if (currentOffset == 0) fetchNextBatch()
            ItemListUiState.Loading
        } else {
            ItemListUiState.Success(filtered)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ItemListUiState.Loading)

    private val _currentItemId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailUiState: StateFlow<ItemDetailUiState> = _currentItemId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest flowOf(ItemDetailUiState.Loading)
            repository.getItemDetailFlow(id).map { detail ->
                if (detail == null) {
                    viewModelScope.launch { repository.syncItemDetail(id) }
                    ItemDetailUiState.Loading
                } else {
                    ItemDetailUiState.Success(detail)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ItemDetailUiState.Loading)

    fun loadItemDetail(id: String?) {
        _currentItemId.value = id
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    fun loadMore() {
        fetchNextBatch()
    }

    private fun fetchNextBatch() {
        if (isFetching) return
        isFetching = true
        viewModelScope.launch {
            try {
                repository.fetchItemList(offset = currentOffset, limit = PAGE_SIZE)
                currentOffset += PAGE_SIZE
            } catch (_: Exception) {
                // Handled by leaving the list as-is; user can retry via loadMore.
            } finally {
                isFetching = false
            }
        }
    }
}
