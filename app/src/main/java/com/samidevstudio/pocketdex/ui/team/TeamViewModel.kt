package com.samidevstudio.pocketdex.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samidevstudio.pocketdex.data.PokemonRepository
import com.samidevstudio.pocketdex.data.TeamRepository
import com.samidevstudio.pocketdex.data.TypeRepository
import com.samidevstudio.pocketdex.domain.TeamAnalyzer
import com.samidevstudio.pocketdex.domain.TeamMemberTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val typeRepository: TypeRepository,
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val typeSyncError = MutableStateFlow<String?>(null)

    private val compareId1 = MutableStateFlow<String?>(null)
    private val compareId2 = MutableStateFlow<String?>(null)
    private val compareError1 = MutableStateFlow<String?>(null)
    private val compareError2 = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val compareDetail1: Flow<CompareSlotState> = compareId1.flatMapLatest { id ->
        if (id == null) flowOf(CompareSlotState.Empty)
        else combine(pokemonRepository.getPokemonDetailFlow(id), compareError1) { detail, error ->
            when {
                detail != null -> CompareSlotState.Success(detail)
                error != null -> CompareSlotState.Error(error)
                else -> CompareSlotState.Loading
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val compareDetail2: Flow<CompareSlotState> = compareId2.flatMapLatest { id ->
        if (id == null) flowOf(CompareSlotState.Empty)
        else combine(pokemonRepository.getPokemonDetailFlow(id), compareError2) { detail, error ->
            when {
                detail != null -> CompareSlotState.Success(detail)
                error != null -> CompareSlotState.Error(error)
                else -> CompareSlotState.Loading
            }
        }
    }

    val teamUiState: StateFlow<TeamUiState> = combine(
        teamRepository.getTeamFlow(),
        typeRepository.getAllTypesFlow(),
        typeSyncError,
        compareDetail1,
        compareDetail2
    ) { members, types, error, slot1, slot2 ->
        val typeMap = types.associateBy { it.name }
        val analysis = TeamAnalyzer.analyze(
            members = members.map { TeamMemberTypes(pokemonId = it.pokemonId, types = it.types) },
            allTypes = typeMap
        )
        TeamUiState(
            members = members,
            analysis = analysis,
            isFull = members.size >= MAX_TEAM_SIZE,
            typeSyncError = error,
            compareSlot1 = slot1,
            compareSlot2 = slot2,
            typeRelations = typeMap
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

    fun selectComparePokemon(slot: Int, pokemonId: String) {
        if (slot == 1) {
            compareId1.value = pokemonId
            compareError1.value = null
            syncCompareDetail(1, pokemonId)
        } else if (slot == 2) {
            compareId2.value = pokemonId
            compareError2.value = null
            syncCompareDetail(2, pokemonId)
        }
    }

    fun retryCompare(slot: Int) {
        if (slot == 1) {
            compareId1.value?.let { id ->
                compareError1.value = null
                syncCompareDetail(1, id)
            }
        } else if (slot == 2) {
            compareId2.value?.let { id ->
                compareError2.value = null
                syncCompareDetail(2, id)
            }
        }
    }

    fun clearComparison() {
        compareId1.value = null
        compareId2.value = null
        compareError1.value = null
        compareError2.value = null
    }

    private fun syncCompareDetail(slot: Int, id: String) {
        viewModelScope.launch {
            try {
                pokemonRepository.syncPokemonDetail(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Could not load Pokémon details"
                if (slot == 1 && compareId1.value == id) compareError1.value = errorMsg
                if (slot == 2 && compareId2.value == id) compareError2.value = errorMsg
            }
        }
    }
}
