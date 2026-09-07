package com.samidevstudio.pocketdex.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemListResponse(
    val results: List<ItemListItem>
)

@Serializable
data class ItemListItem(
    val name: String,
    val url: String
) {
    val id: String
        get() = url.trimEnd('/').split('/').last()
}

@Serializable
data class ItemDetail(
    val id: Int,
    val name: String,
    val cost: Int = 0,
    val category: ItemCategory,
    val sprites: ItemSprites,
    @SerialName("effect_entries")
    val effectEntries: List<MoveEffectEntry> = emptyList(),
    @SerialName("flavor_text_entries")
    val flavorTextEntries: List<ItemFlavorTextEntry> = emptyList()
)

@Serializable
data class ItemCategory(val name: String)

@Serializable
data class ItemSprites(
    val default: String? = null
)

@Serializable
data class ItemFlavorTextEntry(
    val text: String,
    val language: LanguageReference
)
