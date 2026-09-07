package com.samidevstudio.pocketdex.ui.moves

import androidx.compose.runtime.Immutable

@Immutable
data class MoveListModel(
    val id: String,
    val name: String
)

@Immutable
data class MoveDetailModel(
    val id: String,
    val name: String,
    val typeName: String,
    val damageClass: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    val priority: Int,
    val effect: String,
    val effectChance: Int?
)

sealed interface MoveListUiState {
    data object Loading : MoveListUiState
    data class Success(val moves: List<MoveListModel>) : MoveListUiState
    data class Error(val message: String) : MoveListUiState
}

sealed interface MoveDetailUiState {
    data object Loading : MoveDetailUiState
    data class Success(val move: MoveDetailModel) : MoveDetailUiState
    data class Error(val message: String) : MoveDetailUiState
}
