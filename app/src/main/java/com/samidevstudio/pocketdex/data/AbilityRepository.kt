package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.AbilityDao
import com.samidevstudio.pocketdex.data.database.AbilityDetailEntity
import com.samidevstudio.pocketdex.data.database.AbilityListEntity
import com.samidevstudio.pocketdex.ui.ability.AbilityDetailModel
import com.samidevstudio.pocketdex.ui.ability.AbilityListModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface AbilityRepository {
    fun getAbilityListFlow(limit: Int, offset: Int): Flow<List<AbilityListModel>>
    suspend fun fetchAbilityList(offset: Int, limit: Int = 60)

    fun getAbilityDetailFlow(id: String): Flow<AbilityDetailModel?>
    suspend fun syncAbilityDetail(id: String)
}

class DefaultAbilityRepository @Inject constructor(
    private val apiService: PokeApiService,
    private val abilityDao: AbilityDao
) : AbilityRepository {

    private val inFlightListFetches = mutableSetOf<Pair<Int, Int>>()
    private val inFlightDetailSyncs = mutableSetOf<String>()

    override fun getAbilityListFlow(limit: Int, offset: Int): Flow<List<AbilityListModel>> =
        abilityDao.getAbilityListPage(limit, offset).map { list ->
            list.map { AbilityListModel(id = it.id, name = it.name) }
        }

    override suspend fun fetchAbilityList(offset: Int, limit: Int) {
        val fetchKey = offset to limit
        if (inFlightListFetches.contains(fetchKey)) return
        inFlightListFetches.add(fetchKey)

        try {
            val existingCount = abilityDao.getAbilityListCount()
            if (existingCount >= offset + limit) return

            val response = apiService.getAbilityList(offset = offset, limit = limit)
            if (response.results.isEmpty()) return

            abilityDao.insertAbilityList(
                response.results.map { AbilityListEntity(id = it.id, name = it.name) }
            )
        } finally {
            inFlightListFetches.remove(fetchKey)
        }
    }

    override fun getAbilityDetailFlow(id: String): Flow<AbilityDetailModel?> =
        abilityDao.getAbilityDetailFlow(id).map { entity ->
            entity?.let {
                AbilityDetailModel(
                    id = it.id,
                    name = it.name,
                    effect = it.effect,
                    shortEffect = it.shortEffect,
                    flavorText = it.flavorText,
                    pokemon = it.pokemonNames.map(::decodeAbilityPokemonReference)
                )
            }
        }

    override suspend fun syncAbilityDetail(id: String) {
        if (inFlightDetailSyncs.contains(id)) return
        inFlightDetailSyncs.add(id)

        try {
            val existing = abilityDao.getAbilityDetail(id)
            val hasLegacyPokemonNames = existing?.pokemonNames?.any { '|' !in it } == true
            if (existing != null && !hasLegacyPokemonNames) return

            val detail = apiService.getAbilityDetail(id)
            val effectEntry = detail.effectEntries.firstOrNull { it.language.name == "en" }
            val flavorEntry = detail.flavorTextEntries.firstOrNull { it.language.name == "en" }

            val effectText = (effectEntry?.effect ?: "").replace("\n", " ")
            val shortEffectText = (effectEntry?.shortEffect ?: "").replace("\n", " ")
            val flavorText = (flavorEntry?.flavorText ?: "").replace("\n", " ").replace("\u000c", " ")
            val pokemonNames = detail.pokemon.map { "${it.pokemon.id}|${it.pokemon.name}" }

            abilityDao.insertAbilityDetail(
                AbilityDetailEntity(
                    id = detail.id.toString(),
                    name = detail.name,
                    effect = effectText,
                    shortEffect = shortEffectText,
                    flavorText = flavorText,
                    pokemonNames = pokemonNames
                )
            )
        } finally {
            inFlightDetailSyncs.remove(id)
        }
    }
}

private fun decodeAbilityPokemonReference(reference: String): PokemonUiModel {
    val separatorIndex = reference.indexOf('|')
    val id = if (separatorIndex >= 0) reference.substring(0, separatorIndex) else reference
    val name = if (separatorIndex >= 0) reference.substring(separatorIndex + 1) else reference

    return PokemonUiModel(
        id = id,
        name = name,
        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
        types = emptyList()
    )
}
