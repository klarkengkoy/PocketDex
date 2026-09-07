package com.samidevstudio.pocketdex.ui.types

import androidx.compose.runtime.Immutable
import com.samidevstudio.pocketdex.domain.TypeMatchupSummary

@Immutable
data class TypeListModel(
    val name: String
)

@Immutable
data class TypeDetailModel(
    val name: String,
    val matchups: TypeMatchupSummary,
    val pokemonNames: List<String>
)

sealed interface TypeListUiState {
    data object Loading : TypeListUiState
    data class Success(val types: List<TypeListModel>) : TypeListUiState
    data class Error(val message: String) : TypeListUiState
}

sealed interface TypeDetailUiState {
    data object Loading : TypeDetailUiState
    data class Success(val type: TypeDetailModel) : TypeDetailUiState
    data class Error(val message: String) : TypeDetailUiState
}
