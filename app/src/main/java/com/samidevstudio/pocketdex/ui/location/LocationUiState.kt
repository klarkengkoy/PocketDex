package com.samidevstudio.pocketdex.ui.location

import androidx.compose.runtime.Immutable

@Immutable
data class LocationListModel(
    val id: String,
    val name: String
)

@Immutable
data class LocationDetailModel(
    val id: String,
    val name: String,
    val regionName: String,
    val areas: List<String>,
    val gameIndices: List<String>
)

sealed interface LocationListUiState {
    data object Loading : LocationListUiState
    data class Success(
        val locations: List<LocationListModel>,
        val errorMessage: String? = null
    ) : LocationListUiState
    data class Error(val message: String) : LocationListUiState
}

sealed interface LocationDetailUiState {
    data object Loading : LocationDetailUiState
    data class Success(val location: LocationDetailModel) : LocationDetailUiState
    data class Error(val message: String) : LocationDetailUiState
}
