package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.ItemDao
import com.samidevstudio.pocketdex.data.database.MoveDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

class DefaultMoveAndItemRepositoryTest {

    @Test
    fun moveListFetchPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<MoveDao>()
        whenever(dao.getMoveListCount()).thenReturn(0)
        whenever(api.getMoveList(limit = 60, offset = 0)).thenAnswer { throw IOException("offline") }

        assertIOException { DefaultMoveRepository(api, dao).fetchMoveList(offset = 0) }
    }

    @Test
    fun moveDetailSyncPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<MoveDao>()
        whenever(dao.getMoveDetail("1")).thenReturn(null)
        whenever(api.getMoveDetail("1")).thenAnswer { throw IOException("offline") }

        assertIOException { DefaultMoveRepository(api, dao).syncMoveDetail("1") }
    }

    @Test
    fun itemListFetchPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<ItemDao>()
        whenever(dao.getItemListCount()).thenReturn(0)
        whenever(api.getItemList(limit = 60, offset = 0)).thenAnswer { throw IOException("offline") }

        assertIOException { DefaultItemRepository(api, dao).fetchItemList(offset = 0) }
    }

    @Test
    fun itemDetailSyncPropagatesNetworkFailure() = runBlocking {
        val api = mock<PokeApiService>()
        val dao = mock<ItemDao>()
        whenever(dao.getItemDetail("potion")).thenReturn(null)
        whenever(api.getItemDetail("potion")).thenAnswer { throw IOException("offline") }

        assertIOException { DefaultItemRepository(api, dao).syncItemDetail("potion") }
    }

    private suspend fun assertIOException(block: suspend () -> Unit) {
        try {
            block()
            fail("Expected network failure to propagate")
        } catch (e: IOException) {
            assertEquals("offline", e.message)
        }
    }
}
