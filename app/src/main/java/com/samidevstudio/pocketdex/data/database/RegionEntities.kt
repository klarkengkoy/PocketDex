package com.samidevstudio.pocketdex.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lightweight cache of the Region browse list (id + name).
 */
@Entity(tableName = "region_list")
data class RegionListEntity(
    @PrimaryKey val id: String,
    val name: String
)

/**
 * Detailed information for a single Region.
 */
@Entity(tableName = "region_detail")
data class RegionDetailEntity(
    @PrimaryKey val id: String,
    val name: String,
    val mainGeneration: String,
    val pokedexes: List<String>,
    val versionGroups: List<String>,
    val locationNames: List<String>
)
