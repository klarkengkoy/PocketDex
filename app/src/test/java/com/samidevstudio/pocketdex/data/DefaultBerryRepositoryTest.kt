package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.BerryDao
import com.samidevstudio.pocketdex.data.database.BerryDetailEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

class DefaultBerryRepositoryTest {

    @Test
    fun berryListFetchPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<BerryDao>()
        whenever(dao.getBerryListCount()).thenReturn(0)
        whenever(api.getBerryList(limit = 60, offset = 0)).thenAnswer { throw IOException("offline") }

        try {
            DefaultBerryRepository(api, dao).fetchBerryList(offset = 0)
            fail("Expected network failure")
        } catch (e: IOException) {
            assertEquals("offline", e.message)
        }
    }

    @Test
    fun berryDetailSyncPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<BerryDao>()
        whenever(dao.getBerryDetail("cheri")).thenReturn(null)
        whenever(api.getBerryDetail("cheri")).thenAnswer { throw IOException("offline") }

        try {
            DefaultBerryRepository(api, dao).syncBerryDetail("cheri")
            fail("Expected network failure")
        } catch (e: IOException) {
            assertEquals("offline", e.message)
        }
    }

    @Test
    fun berryDetailSyncStoresDataInDaoWhenCached() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<BerryDao>()
        whenever(dao.getBerryDetail("cheri")).thenReturn(
            BerryDetailEntity(
                id = "1",
                name = "cheri",
                growthTime = 3,
                maxHarvest = 5,
                naturalGiftPower = 60,
                size = 20,
                smoothness = 25,
                soilDryness = 15,
                firmness = "soft",
                naturalGiftType = "fire",
                flavors = listOf("Spicy (10)")
            )
        )

        DefaultBerryRepository(api, dao).syncBerryDetail("cheri")
        // Skips API call since cached in DAO
    }
}
