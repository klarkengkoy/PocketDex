package com.samidevstudio.pocketdex.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lightweight cache of the Item browse list, fetched page by page.
 * Category is included so the list screen can filter/group without a detail fetch.
 */
@Entity(tableName = "item_list")
data class ItemListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String = ""
)

/**
 * Full Item detail, fetched lazily when the user opens an Item.
 */
@Entity(tableName = "item_detail")
data class ItemDetailEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cost: Int,
    val category: String,
    val spriteUrl: String?,
    val effect: String,
    val flavorText: String
)
