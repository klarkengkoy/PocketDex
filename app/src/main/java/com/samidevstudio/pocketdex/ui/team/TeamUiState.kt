package com.samidevstudio.pocketdex.ui.team

import androidx.compose.runtime.Immutable
import com.samidevstudio.pocketdex.domain.TeamAnalysis
import com.samidevstudio.pocketdex.domain.TypeRelations
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailModel

const val MAX_TEAM_SIZE = 6

@Immutable
data class TeamMemberModel(
    val position: Int,
    val pokemonId: String,
    val pokemonName: String,
    val imageUrl: String,
    val types: List<String>
)

sealed interface CompareSlotState {
    data object Empty : CompareSlotState
    data object Loading : CompareSlotState
    data class Success(val pokemon: PokemonDetailModel) : CompareSlotState
    data class Error(val message: String) : CompareSlotState
}

@Immutable
data class TeamUiState(
    val members: List<TeamMemberModel> = emptyList(),
    val analysis: TeamAnalysis? = null,
    val isFull: Boolean = false,
    val typeSyncError: String? = null,
    val compareSlot1: CompareSlotState = CompareSlotState.Empty,
    val compareSlot2: CompareSlotState = CompareSlotState.Empty,
    val typeRelations: Map<String, TypeRelations> = emptyMap()
)
