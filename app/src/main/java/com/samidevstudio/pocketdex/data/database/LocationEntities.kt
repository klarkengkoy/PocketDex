package com.samidevstudio.pocketdex.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lightweight cache of the Location browse list (id + name).
 */
@Entity(tableName = "location_list")
data class LocationListEntity(
    @PrimaryKey val id: String,
    val name: String
)

/**
 * Detailed information for a single Location.
 */
@Entity(tableName = "location_detail")
data class LocationDetailEntity(
    @PrimaryKey val id: String,
    val name: String,
    val regionName: String,
    val areas: List<String>,
    val gameIndices: List<String>
)
