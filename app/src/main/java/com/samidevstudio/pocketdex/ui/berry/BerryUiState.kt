package com.samidevstudio.pocketdex.ui.berry

import androidx.compose.runtime.Immutable

@Immutable
data class BerryListModel(
    val id: String,
    val name: String
)

@Immutable
data class BerryDetailModel(
    val id: String,
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

sealed interface BerryListUiState {
    data object Loading : BerryListUiState
    data class Success(
        val berries: List<BerryListModel>,
        val errorMessage: String? = null
    ) : BerryListUiState
    data class Error(val message: String) : BerryListUiState
}

sealed interface BerryDetailUiState {
    data object Loading : BerryDetailUiState
    data class Success(val berry: BerryDetailModel) : BerryDetailUiState
    data class Error(val message: String) : BerryDetailUiState
}
