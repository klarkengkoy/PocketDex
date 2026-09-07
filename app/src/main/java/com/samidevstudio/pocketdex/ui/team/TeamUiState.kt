package com.samidevstudio.pocketdex.ui.team

import androidx.compose.runtime.Immutable
import com.samidevstudio.pocketdex.domain.TeamAnalysis

const val MAX_TEAM_SIZE = 6

@Immutable
data class TeamMemberModel(
    val position: Int,
    val pokemonId: String,
    val pokemonName: String,
    val imageUrl: String,
    val types: List<String>
)

@Immutable
data class TeamUiState(
    val members: List<TeamMemberModel> = emptyList(),
    val analysis: TeamAnalysis? = null,
    val isFull: Boolean = false
)
