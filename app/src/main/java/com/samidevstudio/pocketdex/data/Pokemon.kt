package com.samidevstudio.pocketdex.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonResponse(
    val results: List<PokemonListItem>
)

@Serializable
data class PokemonListItem(
    val name: String,
    val url: String
) {
    val id: String
        get() = url.trimEnd('/').split('/').last()

    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
}

@Serializable
data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<PokemonTypeSlot>,
    val stats: List<PokemonStatSlot>,
    val sprites: PokemonSprites
)

@Serializable
data class PokemonSprites(
    @SerialName("front_default")
    val frontDefault: String,
    @SerialName("front_shiny")
    val frontShiny: String,
    val other: OtherSprites
)

@Serializable
data class OtherSprites(
    @SerialName("official-artwork")
    val officialArtwork: OfficialArtwork
)

@Serializable
data class OfficialArtwork(
    @SerialName("front_default")
    val frontDefault: String
)

@Serializable
data class PokemonStatSlot(
    @SerialName("base_stat")
    val baseStat: Int,
    val stat: PokemonStat
)

@Serializable
data class PokemonStat(
    val name: String
)

@Serializable
data class PokemonTypeSlot(
    val type: PokemonType
)

@Serializable
data class PokemonType(
    val name: String
)

// --- NEW MODELS FOR MODULE 3 ---

@Serializable
data class PokemonSpeciesResponse(
    @SerialName("flavor_text_entries")
    val flavorTextEntries: List<FlavorTextEntry>,
    @SerialName("evolution_chain")
    val evolutionChain: EvolutionChainLink
)

@Serializable
data class FlavorTextEntry(
    @SerialName("flavor_text")
    val flavorText: String,
    val language: LanguageReference
)

@Serializable
data class LanguageReference(val name: String)

@Serializable
data class EvolutionChainLink(val url: String)

@Serializable
data class EvolutionChainResponse(
    val chain: ChainLink
)

@Serializable
data class ChainLink(
    val species: SpeciesReference,
    @SerialName("evolves_to")
    val evolvesTo: List<ChainLink>
)

@Serializable
data class SpeciesReference(
    val name: String,
    val url: String
) {
    val id: String
        get() = url.trimEnd('/').split('/').last()
}
