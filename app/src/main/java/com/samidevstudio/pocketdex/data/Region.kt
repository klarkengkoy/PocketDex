package com.samidevstudio.pocketdex.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegionListResponse(
    val results: List<RegionListItem>
)

@Serializable
data class RegionListItem(
    val name: String,
    val url: String
) {
    val id: String
        get() = url.trimEnd('/').split('/').last()
}

@Serializable
data class RegionDetail(
    val id: Int,
    val name: String,
    @SerialName("main_generation")
    val mainGeneration: NamedRegionResource? = null,
    val pokedexes: List<NamedRegionResource> = emptyList(),
    @SerialName("version_groups")
    val versionGroups: List<NamedRegionResource> = emptyList(),
    val locations: List<NamedRegionResource> = emptyList()
)

@Serializable
data class NamedRegionResource(
    val name: String,
    val url: String = ""
)
