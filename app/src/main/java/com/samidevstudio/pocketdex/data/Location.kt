package com.samidevstudio.pocketdex.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationListResponse(
    val results: List<LocationListItem>
)

@Serializable
data class LocationListItem(
    val name: String,
    val url: String
) {
    val id: String
        get() = url.trimEnd('/').split('/').last()
}

@Serializable
data class LocationDetail(
    val id: Int,
    val name: String,
    val region: NamedLocationResource? = null,
    val areas: List<NamedLocationResource> = emptyList(),
    @SerialName("game_indices")
    val gameIndices: List<LocationGameIndex> = emptyList()
)

@Serializable
data class NamedLocationResource(
    val name: String,
    val url: String = ""
) {
    val id: String
        get() = url.trimEnd('/').split('/').last()
}

@Serializable
data class LocationGameIndex(
    @SerialName("game_index")
    val gameIndex: Int,
    val generation: NamedLocationResource
)
