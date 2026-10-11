package com.samidevstudio.pocketdex.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BerryListResponse(
    val results: List<BerryListItem>
)

@Serializable
data class BerryListItem(
    val name: String,
    val url: String
) {
    val id: String
        get() = url.trimEnd('/').split('/').last()
}

@Serializable
data class BerryDetail(
    val id: Int,
    val name: String,
    @SerialName("growth_time")
    val growthTime: Int = 0,
    @SerialName("max_harvest")
    val maxHarvest: Int = 0,
    @SerialName("natural_gift_power")
    val naturalGiftPower: Int = 0,
    val size: Int = 0,
    val smoothness: Int = 0,
    @SerialName("soil_dryness")
    val soilDryness: Int = 0,
    val firmness: BerryFirmness? = null,
    @SerialName("natural_gift_type")
    val naturalGiftType: BerryTypeRef? = null,
    val flavors: List<BerryFlavorMap> = emptyList()
)

@Serializable
data class BerryFirmness(
    val name: String
)

@Serializable
data class BerryTypeRef(
    val name: String
)

@Serializable
data class BerryFlavorMap(
    val potency: Int = 0,
    val flavor: BerryFlavorRef
)

@Serializable
data class BerryFlavorRef(
    val name: String
)
