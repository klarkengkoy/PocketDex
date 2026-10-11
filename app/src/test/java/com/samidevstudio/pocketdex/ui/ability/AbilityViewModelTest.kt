package com.samidevstudio.pocketdex.ui.ability

import app.cash.turbine.test
import com.samidevstudio.pocketdex.data.AbilityRepository
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
class AbilityViewModelTest {
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
        val repository = mock<AbilityRepository>()
        val listFlow = MutableStateFlow(emptyList<AbilityListModel>())
        var fetchCalls = 0

        whenever(repository.getAbilityListFlow(2000, 0)).thenReturn(listFlow)
        whenever(repository.fetchAbilityList(0, 60)).thenAnswer {
            if (fetchCalls++ == 0) throw IOException("offline")
            listFlow.value = listOf(AbilityListModel("65", "overgrow"))
        }

        val viewModel = AbilityViewModel(repository)

        viewModel.listUiState.test {
            assertEquals(AbilityListUiState.Error("offline"), awaitItem())
            viewModel.retryLoad()
            assertEquals(AbilityListUiState.Loading, awaitItem())
            assertEquals(
                AbilityListUiState.Success(listOf(AbilityListModel("65", "overgrow"))),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
