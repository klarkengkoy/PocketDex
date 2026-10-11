package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.LocationDao
import com.samidevstudio.pocketdex.data.database.LocationDetailEntity
import com.samidevstudio.pocketdex.data.database.LocationListEntity
import com.samidevstudio.pocketdex.ui.location.LocationDetailModel
import com.samidevstudio.pocketdex.ui.location.LocationListModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface LocationRepository {
    fun getLocationListFlow(limit: Int, offset: Int): Flow<List<LocationListModel>>
    suspend fun fetchLocationList(offset: Int, limit: Int = 60)

    fun getLocationDetailFlow(id: String): Flow<LocationDetailModel?>
    suspend fun syncLocationDetail(id: String)
}

class DefaultLocationRepository @Inject constructor(
    private val apiService: PokeApiService,
    private val locationDao: LocationDao
) : LocationRepository {

    private val inFlightListFetches = mutableSetOf<Pair<Int, Int>>()
    private val inFlightDetailSyncs = mutableSetOf<String>()

    override fun getLocationListFlow(limit: Int, offset: Int): Flow<List<LocationListModel>> =
        locationDao.getLocationListPage(limit, offset).map { list ->
            list.map { LocationListModel(id = it.id, name = it.name) }
        }

    override suspend fun fetchLocationList(offset: Int, limit: Int) {
        val fetchKey = offset to limit
        if (inFlightListFetches.contains(fetchKey)) return
        inFlightListFetches.add(fetchKey)

        try {
            val existingCount = locationDao.getLocationListCount()
            if (existingCount >= offset + limit) return

            val response = apiService.getLocationList(offset = offset, limit = limit)
            if (response.results.isEmpty()) return

            locationDao.insertLocationList(
                response.results.map { LocationListEntity(id = it.id, name = it.name) }
            )
        } finally {
            inFlightListFetches.remove(fetchKey)
        }
    }

    override fun getLocationDetailFlow(id: String): Flow<LocationDetailModel?> =
        locationDao.getLocationDetailFlow(id).map { entity ->
            entity?.let {
                LocationDetailModel(
                    id = it.id,
                    name = it.name,
                    regionName = it.regionName,
                    areas = it.areas,
                    gameIndices = it.gameIndices
                )
            }
        }

    override suspend fun syncLocationDetail(id: String) {
        if (inFlightDetailSyncs.contains(id)) return
        inFlightDetailSyncs.add(id)

        try {
            val existing = locationDao.getLocationDetail(id)
            if (existing != null) return

            val detail = apiService.getLocationDetail(id)

            locationDao.insertLocationDetail(
                LocationDetailEntity(
                    id = detail.id.toString(),
                    name = detail.name,
                    regionName = detail.region?.name ?: "unknown",
                    areas = detail.areas.map { it.name },
                    gameIndices = detail.gameIndices.map { "${it.generation.name.uppercase()} (Index: ${it.gameIndex})" }
                )
            )
        } finally {
            inFlightDetailSyncs.remove(id)
        }
    }
}
