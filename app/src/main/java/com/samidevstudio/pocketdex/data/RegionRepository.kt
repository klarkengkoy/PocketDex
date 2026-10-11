package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.RegionDao
import com.samidevstudio.pocketdex.data.database.RegionDetailEntity
import com.samidevstudio.pocketdex.data.database.RegionListEntity
import com.samidevstudio.pocketdex.ui.region.RegionDetailModel
import com.samidevstudio.pocketdex.ui.region.RegionListModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface RegionRepository {
    fun getRegionListFlow(limit: Int, offset: Int): Flow<List<RegionListModel>>
    suspend fun fetchRegionList(offset: Int, limit: Int = 60)

    fun getRegionDetailFlow(id: String): Flow<RegionDetailModel?>
    suspend fun syncRegionDetail(id: String)
}

class DefaultRegionRepository @Inject constructor(
    private val apiService: PokeApiService,
    private val regionDao: RegionDao
) : RegionRepository {

    private val inFlightListFetches = mutableSetOf<Pair<Int, Int>>()
    private val inFlightDetailSyncs = mutableSetOf<String>()

    override fun getRegionListFlow(limit: Int, offset: Int): Flow<List<RegionListModel>> =
        regionDao.getRegionListPage(limit, offset).map { list ->
            list.map { RegionListModel(id = it.id, name = it.name) }
        }

    override suspend fun fetchRegionList(offset: Int, limit: Int) {
        val fetchKey = offset to limit
        if (inFlightListFetches.contains(fetchKey)) return
        inFlightListFetches.add(fetchKey)

        try {
            val existingCount = regionDao.getRegionListCount()
            if (existingCount >= offset + limit) return

            val response = apiService.getRegionList(offset = offset, limit = limit)
            if (response.results.isEmpty()) return

            regionDao.insertRegionList(
                response.results.map { RegionListEntity(id = it.id, name = it.name) }
            )
        } finally {
            inFlightListFetches.remove(fetchKey)
        }
    }

    override fun getRegionDetailFlow(id: String): Flow<RegionDetailModel?> =
        regionDao.getRegionDetailFlow(id).map { entity ->
            entity?.let {
                RegionDetailModel(
                    id = it.id,
                    name = it.name,
                    mainGeneration = it.mainGeneration,
                    pokedexes = it.pokedexes,
                    versionGroups = it.versionGroups,
                    locationNames = it.locationNames
                )
            }
        }

    override suspend fun syncRegionDetail(id: String) {
        if (inFlightDetailSyncs.contains(id)) return
        inFlightDetailSyncs.add(id)

        try {
            val existing = regionDao.getRegionDetail(id)
            if (existing != null) return

            val detail = apiService.getRegionDetail(id)

            regionDao.insertRegionDetail(
                RegionDetailEntity(
                    id = detail.id.toString(),
                    name = detail.name,
                    mainGeneration = detail.mainGeneration?.name ?: "unknown",
                    pokedexes = detail.pokedexes.map { it.name },
                    versionGroups = detail.versionGroups.map { it.name },
                    locationNames = detail.locations.map { it.name }
                )
            )
        } finally {
            inFlightDetailSyncs.remove(id)
        }
    }
}
