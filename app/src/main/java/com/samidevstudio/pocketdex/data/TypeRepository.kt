package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.TypeDao
import com.samidevstudio.pocketdex.data.database.TypeEntity
import com.samidevstudio.pocketdex.domain.TypeRelations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface TypeRepository {
    fun getAllTypesFlow(): Flow<List<TypeRelations>>
    fun getTypeFlow(name: String): Flow<TypeRelations?>
    suspend fun syncAllTypes()
}

class DefaultTypeRepository @Inject constructor(
    private val apiService: PokeApiService,
    private val typeDao: TypeDao
) : TypeRepository {

    private var isSyncing = false

    override fun getAllTypesFlow(): Flow<List<TypeRelations>> =
        typeDao.getAllTypesFlow().map { list -> list.map { it.toDomain() } }

    override fun getTypeFlow(name: String): Flow<TypeRelations?> =
        typeDao.getTypeFlow(name).map { it?.toDomain() }

    override suspend fun syncAllTypes() {
        if (isSyncing) return
        isSyncing = true

        try {
            val roster = apiService.getTypeList().results
            if (typeDao.getTypeNames().toSet() == roster.map { it.name }.toSet()) return

            // Replace the cache only after every detail succeeds, so a partial response is never marked complete.
            val entities = roster.map { item ->
                val detail = apiService.getTypeDetail(item.id)
                TypeEntity(
                    name = detail.name,
                    doubleDamageFrom = detail.damageRelations.doubleDamageFrom.map { it.name },
                    doubleDamageTo = detail.damageRelations.doubleDamageTo.map { it.name },
                    halfDamageFrom = detail.damageRelations.halfDamageFrom.map { it.name },
                    halfDamageTo = detail.damageRelations.halfDamageTo.map { it.name },
                    noDamageFrom = detail.damageRelations.noDamageFrom.map { it.name },
                    noDamageTo = detail.damageRelations.noDamageTo.map { it.name },
                    pokemonNames = detail.pokemon.map { it.pokemon.name }
                )
            }
            typeDao.replaceTypes(entities)
        } finally {
            isSyncing = false
        }
    }
}

private fun TypeEntity.toDomain() = TypeRelations(
    name = name,
    doubleDamageFrom = doubleDamageFrom.toSet(),
    doubleDamageTo = doubleDamageTo.toSet(),
    halfDamageFrom = halfDamageFrom.toSet(),
    halfDamageTo = halfDamageTo.toSet(),
    noDamageFrom = noDamageFrom.toSet(),
    noDamageTo = noDamageTo.toSet(),
    pokemonNames = pokemonNames
)
