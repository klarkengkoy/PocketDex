package com.samidevstudio.pocketdex.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samidevstudio.pocketdex.PocketDexApplication
import com.samidevstudio.pocketdex.data.TeamRepository
import com.samidevstudio.pocketdex.data.TypeRepository
import com.samidevstudio.pocketdex.domain.TeamAnalyzer
import com.samidevstudio.pocketdex.domain.TeamMemberTypes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeamViewModel(
    private val teamRepository: TeamRepository,
    private val typeRepository: TypeRepository
) : ViewModel() {

    val teamUiState: StateFlow<TeamUiState> = combine(
        teamRepository.getTeamFlow(),
        typeRepository.getAllTypesFlow()
    ) { members, types ->
        val typeMap = types.associateBy { it.name }
        val analysis = TeamAnalyzer.analyze(
            members = members.map { TeamMemberTypes(pokemonId = it.pokemonId, types = it.types) },
            allTypes = typeMap
        )
        TeamUiState(
            members = members,
            analysis = analysis,
            isFull = members.size >= MAX_TEAM_SIZE
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TeamUiState())

    init {
        viewModelScope.launch { typeRepository.syncAllTypes() }
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PocketDexApplication)
                TeamViewModel(
                    teamRepository = application.container.teamRepository,
                    typeRepository = application.container.typeRepository
                )
            }
        }
    }
}
