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
            // Types are a small, fixed roster (~20) - skip the network round trip if already cached.
            if (typeDao.getTypeCount() > 0) return

            val listResponse = apiService.getTypeList()
            val entities = listResponse.results.mapNotNull { item ->
                try {
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
                } catch (_: Exception) {
                    null
                }
            }

            if (entities.isNotEmpty()) {
                typeDao.insertTypes(entities)
            }
        } catch (_: Exception) {
            // Leaves the type table empty; caller retries by observing an empty flow.
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
