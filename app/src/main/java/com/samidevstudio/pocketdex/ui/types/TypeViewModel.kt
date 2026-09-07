package com.samidevstudio.pocketdex.ui.types

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samidevstudio.pocketdex.PocketDexApplication
import com.samidevstudio.pocketdex.data.TypeRepository
import com.samidevstudio.pocketdex.domain.TypeEffectivenessCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TypeViewModel(
    private val repository: TypeRepository
) : ViewModel() {

    val listUiState: StateFlow<TypeListUiState> = repository.getAllTypesFlow()
        .map { types ->
            if (types.isEmpty()) {
                TypeListUiState.Loading
            } else {
                TypeListUiState.Success(types.map { TypeListModel(name = it.name) })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TypeListUiState.Loading)

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
        viewModelScope.launch {
            repository.syncAllTypes()
        }
    }

    fun loadTypeDetail(name: String?) {
        _currentTypeName.value = name
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PocketDexApplication)
                TypeViewModel(repository = application.container.typeRepository)
            }
        }
    }
}
