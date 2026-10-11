package com.samidevstudio.pocketdex.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lightweight cache of the Berry browse list (id + name).
 */
@Entity(tableName = "berry_list")
data class BerryListEntity(
    @PrimaryKey val id: String,
    val name: String
)

/**
 * Detailed information for a single Berry.
 */
@Entity(tableName = "berry_detail")
data class BerryDetailEntity(
    @PrimaryKey val id: String,
    val name: String,
    val growthTime: Int,
    val maxHarvest: Int,
    val naturalGiftPower: Int,
    val size: Int,
    val smoothness: Int,
    val soilDryness: Int,
    val firmness: String,
    val naturalGiftType: String,
    val flavors: List<String>
)
