package com.samidevstudio.pocketdex.ui.ability

import androidx.compose.runtime.Immutable
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel

@Immutable
data class AbilityListModel(
    val id: String,
    val name: String
)

@Immutable
data class AbilityDetailModel(
    val id: String,
    val name: String,
    val effect: String,
    val shortEffect: String,
    val flavorText: String,
    val pokemon: List<PokemonUiModel>
)

sealed interface AbilityListUiState {
    data object Loading : AbilityListUiState
    data class Success(
        val abilities: List<AbilityListModel>,
        val errorMessage: String? = null
    ) : AbilityListUiState
    data class Error(val message: String) : AbilityListUiState
}

sealed interface AbilityDetailUiState {
    data object Loading : AbilityDetailUiState
    data class Success(val ability: AbilityDetailModel) : AbilityDetailUiState
    data class Error(val message: String) : AbilityDetailUiState
}
