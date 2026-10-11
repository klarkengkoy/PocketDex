package com.samidevstudio.pocketdex.ui.location

import app.cash.turbine.test
import com.samidevstudio.pocketdex.data.LocationRepository
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
class LocationViewModelTest {
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
    fun selectedLocationIdIsUsedForDetailFlowAndSync() = runTest {
        val repository = mock<LocationRepository>()
        val locationId = "unova-mystery-zone"
        whenever(repository.getLocationDetailFlow(locationId)).thenReturn(
            MutableStateFlow(
                LocationDetailModel(
                    id = "999",
                    name = locationId,
                    regionName = "",
                    areas = emptyList(),
                    gameIndices = emptyList()
                )
            )
        )
        val viewModel = LocationViewModel(repository)

        viewModel.detailUiState.test {
            assertEquals(LocationDetailUiState.Loading, awaitItem())
            viewModel.loadLocationDetail(locationId)
            assertEquals(
                LocationDetailUiState.Success(
                    LocationDetailModel(
                        id = "999",
                        name = locationId,
                        regionName = "",
                        areas = emptyList(),
                        gameIndices = emptyList()
                    )
                ),
                awaitItem()
            )
            org.mockito.kotlin.verify(repository).syncLocationDetail(locationId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fetchNextBatchErrorEmitsErrorStateAndCanBeRetried() = runTest {
        val repository = mock<LocationRepository>()
        val listFlow = MutableStateFlow(emptyList<LocationListModel>())
        var fetchCalls = 0

        whenever(repository.getLocationListFlow(2000, 0)).thenReturn(listFlow)
        whenever(repository.fetchLocationList(0, 60)).thenAnswer {
            if (fetchCalls++ == 0) throw IOException("offline")
            listFlow.value = listOf(LocationListModel("1", "celadon-city"))
        }

        val viewModel = LocationViewModel(repository)

        viewModel.listUiState.test {
            assertEquals(LocationListUiState.Error("offline"), awaitItem())
            viewModel.retryLoad()
            assertEquals(LocationListUiState.Loading, awaitItem())
            assertEquals(
                LocationListUiState.Success(listOf(LocationListModel("1", "celadon-city"))),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
