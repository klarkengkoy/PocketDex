package com.samidevstudio.pocketdex.ui.theme

import androidx.compose.ui.graphics.Color

// Defining some primary Pokémon colors
val PokedexRed = Color(0xFFE3350D)
val PokedexBlue = Color(0xFF31A7D7)
val PokedexGold = Color(0xFFE6BC2F)

// Light Palette
val LightPrimary = PokedexRed
val LightOnPrimary = Color.White
val LightBackground = Color(0xFFF5F5F5)

// Dark Palette
val DarkPrimary = Color(0xFFFF5F3D)
val DarkOnPrimary = Color.Black
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)

/**
 * Official colors for each Pokémon type.
 * Used for the type badges on the cards.
 */
object PokemonTypeColors {
    val map = mapOf(
        "grass" to Color(0xFF9BCC50),
        "fire" to Color(0xFFFD7D24),
        "water" to Color(0xFF4592C4),
        "bug" to Color(0xFF729F3F),
        "normal" to Color(0xFFA4ACAF),
        "poison" to Color(0xFFB97FC9),
        "electric" to Color(0xFFEED535),
        "ground" to Color(0xFFAB9842),
        "fairy" to Color(0xFFFDB9E9),
        "fighting" to Color(0xFFD56723),
        "psychic" to Color(0xFFF366B9),
        "rock" to Color(0xFFA38C21),
        "ghost" to Color(0xFF7B62A3),
        "ice" to Color(0xFF51C4E7),
        "dragon" to Color(0xFFF16E57),
        "dark" to Color(0xFF707070),
        "steel" to Color(0xFF9EB7B8),
        "flying" to Color(0xFF3DC7EF)
    )
}