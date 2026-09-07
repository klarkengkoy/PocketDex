package com.samidevstudio.pocketdex.ui.items

import androidx.compose.runtime.Immutable

@Immutable
data class ItemListModel(
    val id: String,
    val name: String,
    val category: String
)

@Immutable
data class ItemDetailModel(
    val id: String,
    val name: String,
    val cost: Int,
    val category: String,
    val spriteUrl: String?,
    val effect: String,
    val flavorText: String
)

sealed interface ItemListUiState {
    data object Loading : ItemListUiState
    data class Success(val items: List<ItemListModel>) : ItemListUiState
    data class Error(val message: String) : ItemListUiState
}

sealed interface ItemDetailUiState {
    data object Loading : ItemDetailUiState
    data class Success(val item: ItemDetailModel) : ItemDetailUiState
    data class Error(val message: String) : ItemDetailUiState
}
