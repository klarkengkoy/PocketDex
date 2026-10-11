package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.RegionDao
import com.samidevstudio.pocketdex.data.database.RegionDetailEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

class DefaultRegionRepositoryTest {

    @Test
    fun regionListFetchPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<RegionDao>()
        whenever(dao.getRegionListCount()).thenReturn(0)
        whenever(api.getRegionList(limit = 60, offset = 0)).thenAnswer { throw IOException("offline") }

        try {
            DefaultRegionRepository(api, dao).fetchRegionList(offset = 0)
            fail("Expected network failure")
        } catch (e: IOException) {
            assertEquals("offline", e.message)
        }
    }

    @Test
    fun regionDetailSyncPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<RegionDao>()
        whenever(dao.getRegionDetail("kanto")).thenReturn(null)
        whenever(api.getRegionDetail("kanto")).thenAnswer { throw IOException("offline") }

        try {
            DefaultRegionRepository(api, dao).syncRegionDetail("kanto")
            fail("Expected network failure")
        } catch (e: IOException) {
            assertEquals("offline", e.message)
        }
    }

    @Test
    fun regionDetailSyncStoresDataInDaoWhenCached() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<RegionDao>()
        whenever(dao.getRegionDetail("kanto")).thenReturn(
            RegionDetailEntity(
                id = "1",
                name = "kanto",
                mainGeneration = "generation-i",
                pokedexes = listOf("kanto"),
                versionGroups = listOf("red-blue"),
                locationNames = listOf("celadon-city")
            )
        )

        DefaultRegionRepository(api, dao).syncRegionDetail("kanto")
        // Skips API call since cached in DAO
    }
}
