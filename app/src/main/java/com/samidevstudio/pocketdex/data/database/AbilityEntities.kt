package com.samidevstudio.pocketdex.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lightweight cache of the Ability browse list (id + name).
 */
@Entity(tableName = "ability_list")
data class AbilityListEntity(
    @PrimaryKey val id: String,
    val name: String
)

/**
 * Detailed information for a single Ability.
 */
@Entity(tableName = "ability_detail")
data class AbilityDetailEntity(
    @PrimaryKey val id: String,
    val name: String,
    val effect: String,
    val shortEffect: String,
    val flavorText: String,
    val pokemonNames: List<String>
)
