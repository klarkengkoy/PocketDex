package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.LocationDao
import com.samidevstudio.pocketdex.data.database.LocationDetailEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

class DefaultLocationRepositoryTest {

    @Test
    fun locationListFetchPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<LocationDao>()
        whenever(dao.getLocationListCount()).thenReturn(0)
        whenever(api.getLocationList(limit = 60, offset = 0)).thenAnswer { throw IOException("offline") }

        try {
            DefaultLocationRepository(api, dao).fetchLocationList(offset = 0)
            fail("Expected network failure")
        } catch (e: IOException) {
            assertEquals("offline", e.message)
        }
    }

    @Test
    fun locationDetailSyncPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<LocationDao>()
        whenever(dao.getLocationDetail("celadon-city")).thenReturn(null)
        whenever(api.getLocationDetail("celadon-city")).thenAnswer { throw IOException("offline") }

        try {
            DefaultLocationRepository(api, dao).syncLocationDetail("celadon-city")
            fail("Expected network failure")
        } catch (e: IOException) {
            assertEquals("offline", e.message)
        }
    }

    @Test
    fun locationDetailSyncStoresDataInDaoWhenCached() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<LocationDao>()
        whenever(dao.getLocationDetail("celadon-city")).thenReturn(
            LocationDetailEntity(
                id = "1",
                name = "celadon-city",
                regionName = "kanto",
                areas = listOf("celadon-city-area"),
                gameIndices = listOf("GENERATION-I (Index: 1)")
            )
        )

        DefaultLocationRepository(api, dao).syncLocationDetail("celadon-city")
        // Skips API call since cached in DAO
    }
}
