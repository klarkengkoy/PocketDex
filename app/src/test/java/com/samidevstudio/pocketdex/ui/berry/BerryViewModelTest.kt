package com.samidevstudio.pocketdex.ui.berry

import app.cash.turbine.test
import com.samidevstudio.pocketdex.data.BerryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class BerryViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchNextBatchErrorEmitsErrorStateAndCanBeRetried() = runTest {
        val repository = mock<BerryRepository>()
        val listFlow = MutableStateFlow(emptyList<BerryListModel>())
        var fetchCalls = 0

        whenever(repository.getBerryListFlow(2000, 0)).thenReturn(listFlow)
        whenever(repository.fetchBerryList(0, 60)).thenAnswer {
            if (fetchCalls++ == 0) throw IOException("offline")
            listFlow.value = listOf(BerryListModel("1", "cheri"))
        }

        val viewModel = BerryViewModel(repository)

        viewModel.listUiState.test {
            assertEquals(BerryListUiState.Error("offline"), awaitItem())
            viewModel.retryLoad()
            assertEquals(BerryListUiState.Loading, awaitItem())
            assertEquals(
                BerryListUiState.Success(listOf(BerryListModel("1", "cheri"))),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
