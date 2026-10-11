package com.samidevstudio.pocketdex.ui.region

import androidx.compose.runtime.Immutable

@Immutable
data class RegionListModel(
    val id: String,
    val name: String
)

@Immutable
data class RegionDetailModel(
    val id: String,
    val name: String,
    val mainGeneration: String,
    val pokedexes: List<String>,
    val versionGroups: List<String>,
    val locationNames: List<String>
)

sealed interface RegionListUiState {
    data object Loading : RegionListUiState
    data class Success(
        val regions: List<RegionListModel>,
        val errorMessage: String? = null
    ) : RegionListUiState
    data class Error(val message: String) : RegionListUiState
}

sealed interface RegionDetailUiState {
    data object Loading : RegionDetailUiState
    data class Success(val region: RegionDetailModel) : RegionDetailUiState
    data class Error(val message: String) : RegionDetailUiState
}
