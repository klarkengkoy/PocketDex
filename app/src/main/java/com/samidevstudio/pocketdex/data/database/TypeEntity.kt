package com.samidevstudio.pocketdex.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached Pokémon type with damage relations, keyed by type name so it can be looked up
 * directly against the type strings already used on Pokémon (e.g. "fire", "water").
 * The full type roster is small (~20), so it is cached whole rather than paginated.
 */
@Entity(tableName = "type_detail")
data class TypeEntity(
    @PrimaryKey val name: String,
    val doubleDamageFrom: List<String>,
    val doubleDamageTo: List<String>,
    val halfDamageFrom: List<String>,
    val halfDamageTo: List<String>,
    val noDamageFrom: List<String>,
    val noDamageTo: List<String>,
    val pokemonNames: List<String>
)
