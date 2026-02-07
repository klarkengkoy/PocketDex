package com.samidevstudio.pocketdex.ui

/**
 * A lean model for the Pokémon grid. Contains just enough info for the list item.
 */
data class PokemonUiModel(
    val id: String,
    val name: String,
    val imageUrl: String,
    val types: List<String>
)

/**
 * A rich model for the Pokémon detail screen.
 */
data class PokemonDetailModel(
    val id: String,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val height: Int, // in decimetres
    val weight: Int, // in hectograms
    val stats: List<StatInfo>
)

/**
 * Represents a single stat (e.g., HP, Attack) for the detail screen.
 */
data class StatInfo(
    val name: String,
    val value: Int
)

/**
 * Represents the various states for the main Pokémon grid screen.
 */
sealed interface PokemonListUiState {
    data object Loading : PokemonListUiState
    data class Success(val pokemonList: List<PokemonUiModel>) : PokemonListUiState
    data class Error(val message: String) : PokemonListUiState
}

/**
 * Represents the various states for the Pokémon detail screen.
 */
sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState
    data class Success(val pokemon: PokemonDetailModel) : PokemonDetailUiState
    data class Error(val message: String) : PokemonDetailUiState
}