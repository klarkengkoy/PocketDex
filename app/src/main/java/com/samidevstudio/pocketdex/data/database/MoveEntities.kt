package com.samidevstudio.pocketdex.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lightweight cache of the Move browse list (name + id only), fetched page by page.
 */
@Entity(tableName = "move_list")
data class MoveListEntity(
    @PrimaryKey val id: String,
    val name: String
)

/**
 * Full Move detail, fetched lazily when the user opens a Move.
 */
@Entity(tableName = "move_detail")
data class MoveDetailEntity(
    @PrimaryKey val id: String,
    val name: String,
    val typeName: String,
    val damageClass: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    val priority: Int,
    val effect: String,
    val effectChance: Int?
)
