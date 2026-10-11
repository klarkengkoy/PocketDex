package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.BerryDao
import com.samidevstudio.pocketdex.data.database.BerryDetailEntity
import com.samidevstudio.pocketdex.data.database.BerryListEntity
import com.samidevstudio.pocketdex.ui.berry.BerryDetailModel
import com.samidevstudio.pocketdex.ui.berry.BerryListModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface BerryRepository {
    fun getBerryListFlow(limit: Int, offset: Int): Flow<List<BerryListModel>>
    suspend fun fetchBerryList(offset: Int, limit: Int = 60)

    fun getBerryDetailFlow(id: String): Flow<BerryDetailModel?>
    suspend fun syncBerryDetail(id: String)
}

class DefaultBerryRepository @Inject constructor(
    private val apiService: PokeApiService,
    private val berryDao: BerryDao
) : BerryRepository {

    private val inFlightListFetches = mutableSetOf<Pair<Int, Int>>()
    private val inFlightDetailSyncs = mutableSetOf<String>()

    override fun getBerryListFlow(limit: Int, offset: Int): Flow<List<BerryListModel>> =
        berryDao.getBerryListPage(limit, offset).map { list ->
            list.map { BerryListModel(id = it.id, name = it.name) }
        }

    override suspend fun fetchBerryList(offset: Int, limit: Int) {
        val fetchKey = offset to limit
        if (inFlightListFetches.contains(fetchKey)) return
        inFlightListFetches.add(fetchKey)

        try {
            val existingCount = berryDao.getBerryListCount()
            if (existingCount >= offset + limit) return

            val response = apiService.getBerryList(offset = offset, limit = limit)
            if (response.results.isEmpty()) return

            berryDao.insertBerryList(
                response.results.map { BerryListEntity(id = it.id, name = it.name) }
            )
        } finally {
            inFlightListFetches.remove(fetchKey)
        }
    }

    override fun getBerryDetailFlow(id: String): Flow<BerryDetailModel?> =
        berryDao.getBerryDetailFlow(id).map { entity ->
            entity?.let {
                BerryDetailModel(
                    id = it.id,
                    name = it.name,
                    growthTime = it.growthTime,
                    maxHarvest = it.maxHarvest,
                    naturalGiftPower = it.naturalGiftPower,
                    size = it.size,
                    smoothness = it.smoothness,
                    soilDryness = it.soilDryness,
                    firmness = it.firmness,
                    naturalGiftType = it.naturalGiftType,
                    flavors = it.flavors
                )
            }
        }

    override suspend fun syncBerryDetail(id: String) {
        if (inFlightDetailSyncs.contains(id)) return
        inFlightDetailSyncs.add(id)

        try {
            val existing = berryDao.getBerryDetail(id)
            if (existing != null) return

            val detail = apiService.getBerryDetail(id)
            val activeFlavors = detail.flavors
                .filter { it.potency > 0 }
                .map { "${it.flavor.name.replaceFirstChar { c -> c.uppercase() }} (${it.potency})" }

            berryDao.insertBerryDetail(
                BerryDetailEntity(
                    id = detail.id.toString(),
                    name = detail.name,
                    growthTime = detail.growthTime,
                    maxHarvest = detail.maxHarvest,
                    naturalGiftPower = detail.naturalGiftPower,
                    size = detail.size,
                    smoothness = detail.smoothness,
                    soilDryness = detail.soilDryness,
                    firmness = detail.firmness?.name ?: "unknown",
                    naturalGiftType = detail.naturalGiftType?.name ?: "unknown",
                    flavors = activeFlavors
                )
            )
        } finally {
            inFlightDetailSyncs.remove(id)
        }
    }
}
