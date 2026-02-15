package com.samidevstudio.pocketdex.ui.pokemon

data class PokemonUiModel(
    val id: String,
    val name: String,
    val imageUrl: String,
    val types: List<String>
)

data class PokemonDetailModel(
    val id: String,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val height: Int,
    val weight: Int,
    val stats: List<StatInfo>,
    val flavorText: String = "",
    val evolutions: List<EvolutionNode> = emptyList()
)

data class EvolutionNode(
    val id: String,
    val name: String,
    val imageUrl: String
)

data class StatInfo(
    val name: String,
    val value: Int
)

sealed interface PokemonListUiState {
    data object Loading : PokemonListUiState
    data class Success(val pokemonList: List<PokemonUiModel>) : PokemonListUiState
    data class Error(val message: String) : PokemonListUiState
}

sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState
    data class Success(val pokemon: PokemonDetailModel) : PokemonDetailUiState
    data class Error(val message: String) : PokemonDetailUiState
}
