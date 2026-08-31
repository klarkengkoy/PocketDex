package com.samidevstudio.pocketdex.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel
import com.samidevstudio.pocketdex.ui.pokemon.StatInfo

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val types: List<String>
)

fun PokemonEntity.toUiModel() = PokemonUiModel(
    id = id,
    name = name,
    imageUrl = imageUrl,
    types = types
)

fun PokemonUiModel.toEntity() = PokemonEntity(
    id = id,
    name = name,
    imageUrl = imageUrl,
    types = types
)

@Entity(tableName = "pokemon_detail")
data class PokemonDetailEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val height: Int,
    val weight: Int,
    val stats: List<StatInfo>,
    val flavorText: String,
    val speciesId: String,
    val chainId: String
)

@Entity(tableName = "evolution_chains")
data class EvolutionChainEntity(
    @PrimaryKey val id: String,
    val evolutions: List<EvolutionNode>
)

fun PokemonDetailEntity.toDetailModel(evolutions: List<EvolutionNode>) = PokemonDetailModel(
    id = id,
    name = name,
    imageUrl = imageUrl,
    types = types,
    height = height,
    weight = weight,
    stats = stats,
    flavorText = flavorText,
    evolutions = evolutions,
    chainId = chainId
)

fun PokemonDetailModel.toEntity(speciesId: String, chainId: String) = PokemonDetailEntity(
    id = id,
    name = name,
    imageUrl = imageUrl,
    types = types,
    height = height,
    weight = weight,
    stats = stats,
    flavorText = flavorText,
    speciesId = speciesId,
    chainId = chainId
)
