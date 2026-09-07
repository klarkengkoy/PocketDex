package com.samidevstudio.pocketdex.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single Team slot (0..5). Using the slot position as the primary key makes
 * "replace" a plain upsert and "remove" a plain delete-by-position.
 */
@Entity(tableName = "team_members")
data class TeamMemberEntity(
    @PrimaryKey val position: Int,
    val pokemonId: String,
    val pokemonName: String,
    val imageUrl: String,
    val types: List<String>
)
