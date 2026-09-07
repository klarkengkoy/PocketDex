package com.samidevstudio.pocketdex.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TypeListResponse(
    val results: List<TypeListItem>
)

@Serializable
data class TypeListItem(
    val name: String,
    val url: String
) {
    val id: String
        get() = url.trimEnd('/').split('/').last()
}

@Serializable
data class TypeDetail(
    val id: Int,
    val name: String,
    @SerialName("damage_relations")
    val damageRelations: TypeDamageRelations,
    val pokemon: List<TypePokemonSlot> = emptyList()
)

@Serializable
data class TypeDamageRelations(
    @SerialName("double_damage_from")
    val doubleDamageFrom: List<NamedApiResource> = emptyList(),
    @SerialName("double_damage_to")
    val doubleDamageTo: List<NamedApiResource> = emptyList(),
    @SerialName("half_damage_from")
    val halfDamageFrom: List<NamedApiResource> = emptyList(),
    @SerialName("half_damage_to")
    val halfDamageTo: List<NamedApiResource> = emptyList(),
    @SerialName("no_damage_from")
    val noDamageFrom: List<NamedApiResource> = emptyList(),
    @SerialName("no_damage_to")
    val noDamageTo: List<NamedApiResource> = emptyList()
)

@Serializable
data class NamedApiResource(
    val name: String,
    val url: String
)

@Serializable
data class TypePokemonSlot(
    val pokemon: NamedApiResource
)
