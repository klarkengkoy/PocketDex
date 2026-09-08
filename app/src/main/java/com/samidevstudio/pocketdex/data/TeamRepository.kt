package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.TeamDao
import com.samidevstudio.pocketdex.data.database.TeamMemberEntity
import com.samidevstudio.pocketdex.ui.team.MAX_TEAM_SIZE
import com.samidevstudio.pocketdex.ui.team.TeamMemberModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface TeamRepository {
    fun getTeamFlow(): Flow<List<TeamMemberModel>>

    /** Adds to the first free slot, or replaces [replacePosition] if given. Returns false if the team is full and no replacement position was given. */
    suspend fun addOrReplace(
        pokemonId: String,
        pokemonName: String,
        imageUrl: String,
        types: List<String>,
        replacePosition: Int? = null
    ): Boolean

    suspend fun remove(position: Int)
}

class DefaultTeamRepository @Inject constructor(
    private val teamDao: TeamDao
) : TeamRepository {

    override fun getTeamFlow(): Flow<List<TeamMemberModel>> =
        teamDao.getTeamFlow().map { list ->
            list.map {
                TeamMemberModel(
                    position = it.position,
                    pokemonId = it.pokemonId,
                    pokemonName = it.pokemonName,
                    imageUrl = it.imageUrl,
                    types = it.types
                )
            }
        }

    override suspend fun addOrReplace(
        pokemonId: String,
        pokemonName: String,
        imageUrl: String,
        types: List<String>,
        replacePosition: Int?
    ): Boolean {
        val targetPosition = replacePosition ?: run {
            val currentSize = teamDao.getTeamSize()
            if (currentSize >= MAX_TEAM_SIZE) return false
            currentSize
        }

        teamDao.upsertTeamMember(
            TeamMemberEntity(
                position = targetPosition,
                pokemonId = pokemonId,
                pokemonName = pokemonName,
                imageUrl = imageUrl,
                types = types
            )
        )
        return true
    }

    override suspend fun remove(position: Int) {
        teamDao.removeTeamMember(position)
    }
}
