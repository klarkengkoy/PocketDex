package com.samidevstudio.pocketdex.ui.pokemon

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class PokemonUiModel(
    val id: String,
    val name: String,
    val imageUrl: String,
    val types: List<String>
)

@Immutable
@Serializable
data class PokemonDetailModel(
    val id: String,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val height: Int,
    val weight: Int,
    val stats: List<StatInfo>,
    val flavorText: String = "",
    val evolutions: List<EvolutionNode> = emptyList(),
    val chainId: String = ""
)

@Immutable
@Serializable
data class EvolutionNode(
    val id: String,
    val name: String,
    val imageUrl: String,
    val evolvesTo: List<EvolutionNode> = emptyList()
)

@Immutable
@Serializable
data class StatInfo(
    val name: String,
    val value: Int
)

sealed interface PokemonListUiState {
    data object Loading : PokemonListUiState
    data class Success(val pokemonList: List<PokemonUiModel>, val errorMessage: String? = null) : PokemonListUiState
    data class Error(val message: String) : PokemonListUiState
}

sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState
    data class Success(val pokemon: PokemonDetailModel, val errorMessage: String? = null) : PokemonDetailUiState
    data class Error(val message: String) : PokemonDetailUiState
}
