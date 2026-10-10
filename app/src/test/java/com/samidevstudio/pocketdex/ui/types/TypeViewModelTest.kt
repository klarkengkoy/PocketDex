package com.samidevstudio.pocketdex.ui.types

import app.cash.turbine.test
import com.samidevstudio.pocketdex.data.TypeRepository
import com.samidevstudio.pocketdex.domain.TypeRelations
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
class TypeViewModelTest {
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
    fun initialSyncFailureCanBeRetried() = runTest {
        val repository = mock<TypeRepository>()
        val types = MutableStateFlow(emptyList<TypeRelations>())
        var syncCalls = 0
        whenever(repository.getAllTypesFlow()).thenReturn(types)
        whenever(repository.syncAllTypes()).thenAnswer {
            if (syncCalls++ == 0) throw IllegalStateException("offline")
            types.value = listOf(
                TypeRelations(
                    name = "fire",
                    doubleDamageFrom = emptySet(),
                    doubleDamageTo = emptySet(),
                    halfDamageFrom = emptySet(),
                    halfDamageTo = emptySet(),
                    noDamageFrom = emptySet(),
                    noDamageTo = emptySet()
                )
            )
        }

        val viewModel = TypeViewModel(repository)

        viewModel.listUiState.test {
            assertEquals(TypeListUiState.Error("offline"), awaitItem())
            viewModel.retrySync()
            assertEquals(TypeListUiState.Loading, awaitItem())
            assertEquals(
                TypeListUiState.Success(listOf(TypeListModel("fire"))),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
