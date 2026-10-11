package com.samidevstudio.pocketdex.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AbilityListResponse(
    val results: List<AbilityListItem>
)

@Serializable
data class AbilityListItem(
    val name: String,
    val url: String
) {
    val id: String
        get() = url.trimEnd('/').split('/').last()
}

@Serializable
data class AbilityDetail(
    val id: Int,
    val name: String,
    @SerialName("is_main_series")
    val isMainSeries: Boolean = true,
    @SerialName("effect_entries")
    val effectEntries: List<AbilityEffectEntry> = emptyList(),
    @SerialName("flavor_text_entries")
    val flavorTextEntries: List<AbilityFlavorTextEntry> = emptyList(),
    val pokemon: List<AbilityPokemonEntry> = emptyList()
)

@Serializable
data class AbilityEffectEntry(
    val effect: String,
    @SerialName("short_effect")
    val shortEffect: String,
    val language: LanguageReference
)

@Serializable
data class AbilityFlavorTextEntry(
    @SerialName("flavor_text")
    val flavorText: String,
    val language: LanguageReference
)

@Serializable
data class AbilityPokemonEntry(
    @SerialName("is_hidden")
    val isHidden: Boolean = false,
    val slot: Int = 1,
    val pokemon: AbilityPokemonRef
)

@Serializable
data class AbilityPokemonRef(
    val name: String,
    val url: String
) {
    val id: String
        get() = url.trimEnd('/').substringAfterLast('/')
}
