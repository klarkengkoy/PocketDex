package com.samidevstudio.pocketdex.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.TeamRepository
import com.samidevstudio.pocketdex.data.TypeRepository
import com.samidevstudio.pocketdex.domain.TeamAnalyzer
import com.samidevstudio.pocketdex.domain.TeamMemberTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val typeRepository: TypeRepository
) : ViewModel() {

    private val typeSyncError = MutableStateFlow<String?>(null)

    val teamUiState: StateFlow<TeamUiState> = combine(
        teamRepository.getTeamFlow(),
        typeRepository.getAllTypesFlow(),
        typeSyncError
    ) { members, types, error ->
        val typeMap = types.associateBy { it.name }
        val analysis = TeamAnalyzer.analyze(
            members = members.map { TeamMemberTypes(pokemonId = it.pokemonId, types = it.types) },
            allTypes = typeMap
        )
        TeamUiState(
            members = members,
            analysis = analysis,
            isFull = members.size >= MAX_TEAM_SIZE,
            typeSyncError = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TeamUiState())

    init {
        syncTypes()
    }

    fun retryTypeSync() = syncTypes()

    private fun syncTypes() {
        typeSyncError.value = null
        viewModelScope.launch {
            try {
                typeRepository.syncAllTypes()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                typeSyncError.value = e.message ?: "Could not load types"
            }
        }
    }

    fun addOrReplace(
        pokemonId: String,
        pokemonName: String,
        imageUrl: String,
        types: List<String>,
        replacePosition: Int?
    ) {
        viewModelScope.launch {
            teamRepository.addOrReplace(pokemonId, pokemonName, imageUrl, types, replacePosition)
        }
    }

    fun remove(position: Int) {
        viewModelScope.launch { teamRepository.remove(position) }
    }
}
