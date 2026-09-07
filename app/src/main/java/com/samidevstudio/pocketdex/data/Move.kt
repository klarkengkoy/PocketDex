package com.samidevstudio.pocketdex.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoveListResponse(
    val results: List<MoveListItem>
)

@Serializable
data class MoveListItem(
    val name: String,
    val url: String
) {
    val id: String
        get() = url.trimEnd('/').split('/').last()
}

@Serializable
data class MoveDetail(
    val id: Int,
    val name: String,
    val accuracy: Int? = null,
    val pp: Int? = null,
    val priority: Int = 0,
    val power: Int? = null,
    @SerialName("damage_class")
    val damageClass: MoveDamageClass? = null,
    val type: MoveType,
    @SerialName("effect_chance")
    val effectChance: Int? = null,
    @SerialName("effect_entries")
    val effectEntries: List<MoveEffectEntry> = emptyList()
)

@Serializable
data class MoveDamageClass(val name: String)

@Serializable
data class MoveType(val name: String)

@Serializable
data class MoveEffectEntry(
    val effect: String,
    @SerialName("short_effect")
    val shortEffect: String,
    val language: LanguageReference
)
