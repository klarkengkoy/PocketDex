package com.samidevstudio.pocketdex.ui.team

import app.cash.turbine.test
import com.samidevstudio.pocketdex.data.TeamRepository
import com.samidevstudio.pocketdex.data.TypeRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class TeamViewModelTest {
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
    fun typeSyncFailureIsShownAndCanBeRetried() = runTest {
        val teamRepository = mock<TeamRepository>()
        val typeRepository = mock<TypeRepository>()
        whenever(teamRepository.getTeamFlow()).thenReturn(MutableStateFlow(emptyList()))
        whenever(typeRepository.getAllTypesFlow()).thenReturn(MutableStateFlow(emptyList()))
        var syncCalls = 0
        whenever(typeRepository.syncAllTypes()).thenAnswer {
            if (syncCalls++ == 0) throw IllegalStateException("offline")
            Unit
        }

        val viewModel = TeamViewModel(teamRepository, typeRepository)

        viewModel.teamUiState.test {
            assertEquals("offline", awaitItem().typeSyncError)
            viewModel.retryTypeSync()
            assertEquals(null, awaitItem().typeSyncError)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
