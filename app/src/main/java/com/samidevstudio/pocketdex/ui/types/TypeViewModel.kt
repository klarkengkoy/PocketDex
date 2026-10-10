package com.samidevstudio.pocketdex.ui.types

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.TypeRepository
import com.samidevstudio.pocketdex.domain.TypeEffectivenessCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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

@HiltViewModel
class TypeViewModel @Inject constructor(
    private val repository: TypeRepository
) : ViewModel() {

    private val _syncError = MutableStateFlow<String?>(null)

    val listUiState: StateFlow<TypeListUiState> = combine(
        repository.getAllTypesFlow(),
        _syncError
    ) { types, errorMessage ->
        when {
            types.isNotEmpty() -> TypeListUiState.Success(
                types = types.map { TypeListModel(name = it.name) },
                errorMessage = errorMessage
            )
            errorMessage != null -> TypeListUiState.Error(errorMessage)
            else -> TypeListUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TypeListUiState.Loading)

    private val _currentTypeName = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailUiState: StateFlow<TypeDetailUiState> = _currentTypeName
        .flatMapLatest { name ->
            if (name == null) return@flatMapLatest flowOf(TypeDetailUiState.Loading)
            repository.getTypeFlow(name).map { relations ->
                if (relations == null) {
                    TypeDetailUiState.Loading
                } else {
                    TypeDetailUiState.Success(
                        TypeDetailModel(
                            name = relations.name,
                            matchups = TypeEffectivenessCalculator.singleTypeMatchups(relations),
                            pokemonNames = relations.pokemonNames
                        )
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TypeDetailUiState.Loading)

    init {
        syncTypes()
    }

    fun retrySync() {
        syncTypes()
    }

    private fun syncTypes() {
        _syncError.value = null
        viewModelScope.launch {
            try {
                repository.syncAllTypes()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _syncError.value = e.message ?: "Could not load types"
            }
        }
    }

    fun loadTypeDetail(name: String?) {
        _currentTypeName.value = name
    }
}
