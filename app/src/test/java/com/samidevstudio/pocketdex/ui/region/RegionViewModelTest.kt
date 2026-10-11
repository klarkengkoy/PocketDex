package com.samidevstudio.pocketdex.ui.region

import app.cash.turbine.test
import com.samidevstudio.pocketdex.data.RegionRepository
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
class RegionViewModelTest {
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
        val repository = mock<RegionRepository>()
        val listFlow = MutableStateFlow(emptyList<RegionListModel>())
        var fetchCalls = 0

        whenever(repository.getRegionListFlow(2000, 0)).thenReturn(listFlow)
        whenever(repository.fetchRegionList(0, 60)).thenAnswer {
            if (fetchCalls++ == 0) throw IOException("offline")
            listFlow.value = listOf(RegionListModel("1", "kanto"))
        }

        val viewModel = RegionViewModel(repository)

        viewModel.listUiState.test {
            assertEquals(RegionListUiState.Error("offline"), awaitItem())
            viewModel.retryLoad()
            assertEquals(RegionListUiState.Loading, awaitItem())
            assertEquals(
                RegionListUiState.Success(listOf(RegionListModel("1", "kanto"))),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
