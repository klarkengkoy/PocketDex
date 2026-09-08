package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.MoveDao
import com.samidevstudio.pocketdex.data.database.MoveDetailEntity
import com.samidevstudio.pocketdex.data.database.MoveListEntity
import com.samidevstudio.pocketdex.ui.moves.MoveDetailModel
import com.samidevstudio.pocketdex.ui.moves.MoveListModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface MoveRepository {
    fun getMoveListFlow(limit: Int, offset: Int): Flow<List<MoveListModel>>
    suspend fun fetchMoveList(offset: Int, limit: Int = 60)

    fun getMoveDetailFlow(id: String): Flow<MoveDetailModel?>
    suspend fun syncMoveDetail(id: String)
}

class DefaultMoveRepository @Inject constructor(
    private val apiService: PokeApiService,
    private val moveDao: MoveDao
) : MoveRepository {

    private val inFlightListFetches = mutableSetOf<Pair<Int, Int>>()
    private val inFlightDetailSyncs = mutableSetOf<String>()

    override fun getMoveListFlow(limit: Int, offset: Int): Flow<List<MoveListModel>> =
        moveDao.getMoveListPage(limit, offset).map { list ->
            list.map { MoveListModel(id = it.id, name = it.name) }
        }

    override suspend fun fetchMoveList(offset: Int, limit: Int) {
        val fetchKey = offset to limit
        if (inFlightListFetches.contains(fetchKey)) return
        inFlightListFetches.add(fetchKey)

        try {
            val existingCount = moveDao.getMoveListCount()
            if (existingCount >= offset + limit) return

            val response = apiService.getMoveList(offset = offset, limit = limit)
            if (response.results.isEmpty()) return

            moveDao.insertMoveList(
                response.results.map { MoveListEntity(id = it.id, name = it.name) }
            )
        } catch (_: Exception) {
            // Swallowed intentionally for now: list stays whatever was cached, UI shows no new page.
        } finally {
            inFlightListFetches.remove(fetchKey)
        }
    }

    override fun getMoveDetailFlow(id: String): Flow<MoveDetailModel?> =
        moveDao.getMoveDetailFlow(id).map { entity ->
            entity?.let {
                MoveDetailModel(
                    id = it.id,
                    name = it.name,
                    typeName = it.typeName,
                    damageClass = it.damageClass,
                    power = it.power,
                    accuracy = it.accuracy,
                    pp = it.pp,
                    priority = it.priority,
                    effect = it.effect,
                    effectChance = it.effectChance
                )
            }
        }

    override suspend fun syncMoveDetail(id: String) {
        if (inFlightDetailSyncs.contains(id)) return
        inFlightDetailSyncs.add(id)

        try {
            val existing = moveDao.getMoveDetail(id)
            if (existing != null) return

            val detail = apiService.getMoveDetail(id)
            val effectEntry = detail.effectEntries.firstOrNull { it.language.name == "en" }
            // PokeAPI effect text sometimes contains a literal "$effect_chance" placeholder
            // that should be substituted with the numeric effect_chance value.
            val effectText = (effectEntry?.shortEffect ?: effectEntry?.effect ?: "")
                .replace("\n", " ")
                .replace("\$effect_chance", detail.effectChance?.toString() ?: "")

            moveDao.insertMoveDetail(
                MoveDetailEntity(
                    id = detail.id.toString(),
                    name = detail.name,
                    typeName = detail.type.name,
                    damageClass = detail.damageClass?.name ?: "unknown",
                    power = detail.power,
                    accuracy = detail.accuracy,
                    pp = detail.pp,
                    priority = detail.priority,
                    effect = effectText,
                    effectChance = detail.effectChance
                )
            )
        } catch (_: Exception) {
            // Handled by leaving the Flow null; the ViewModel keeps showing loading/retry.
        } finally {
            inFlightDetailSyncs.remove(id)
        }
    }
}
