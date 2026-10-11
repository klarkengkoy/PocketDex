package com.samidevstudio.pocketdex.ui.types

import androidx.compose.runtime.Immutable
import com.samidevstudio.pocketdex.domain.TypeMatchupSummary
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel

@Immutable
data class TypeListModel(
    val name: String
)

@Immutable
data class TypeDetailModel(
    val name: String,
    val matchups: TypeMatchupSummary,
    val pokemon: List<PokemonUiModel>
)

sealed interface TypeListUiState {
    data object Loading : TypeListUiState
    data class Success(
        val types: List<TypeListModel>,
        val errorMessage: String? = null
    ) : TypeListUiState
    data class Error(val message: String) : TypeListUiState
}

sealed interface TypeDetailUiState {
    data object Loading : TypeDetailUiState
    data class Success(val type: TypeDetailModel) : TypeDetailUiState
    data class Error(val message: String) : TypeDetailUiState
}
