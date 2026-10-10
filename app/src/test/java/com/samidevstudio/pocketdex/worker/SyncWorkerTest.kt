package com.samidevstudio.pocketdex.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.samidevstudio.pocketdex.data.PokemonRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response

class SyncWorkerTest {

    @Test
    fun successfulBackfillReturnsSuccess() = runBlocking {
        val repository = mock<PokemonRepository>()

        assertEquals(ListenableWorker.Result.success(), worker(repository, attempt = 0).doWork())
    }

    @Test
    fun transientBackfillFailureReturnsRetry() = runBlocking {
        val repository = mock<PokemonRepository>()
        whenever(repository.backfillMissingTypes()).thenAnswer { throw IOException("offline") }

        assertEquals(ListenableWorker.Result.retry(), worker(repository, attempt = 0).doWork())
    }

    @Test
    fun serverFailureReturnsRetry() = runBlocking {
        val repository = mock<PokemonRepository>()
        whenever(repository.backfillMissingTypes()).thenAnswer { throw httpFailure(503) }

        assertEquals(ListenableWorker.Result.retry(), worker(repository, attempt = 0).doWork())
    }

    @Test
    fun clientFailureIsNotRetried() = runBlocking {
        val repository = mock<PokemonRepository>()
        whenever(repository.backfillMissingTypes()).thenAnswer { throw httpFailure(404) }

        assertEquals(ListenableWorker.Result.failure(), worker(repository, attempt = 0).doWork())
    }

    @Test
    fun repeatedFailureReturnsFailureAtAttemptLimit() = runBlocking {
        val repository = mock<PokemonRepository>()
        whenever(repository.backfillMissingTypes()).thenAnswer { throw IOException("offline") }

        assertEquals(ListenableWorker.Result.failure(), worker(repository, attempt = 2).doWork())
    }

    @Test
    fun nonNetworkFailureIsNotRetried() = runBlocking {
        val repository = mock<PokemonRepository>()
        whenever(repository.backfillMissingTypes()).thenThrow(IllegalStateException("invalid data"))

        assertEquals(ListenableWorker.Result.failure(), worker(repository, attempt = 0).doWork())
    }

    @Test(expected = CancellationException::class)
    fun cancellationPropagates() {
        runBlocking {
            val repository = mock<PokemonRepository>()
            whenever(repository.backfillMissingTypes()).thenAnswer {
                throw CancellationException("cancelled")
            }

            worker(repository, attempt = 0).doWork()
        }
    }

    private fun httpFailure(code: Int) =
        HttpException(Response.error<Any>(code, ByteArray(0).toResponseBody()))

    private fun worker(repository: PokemonRepository, attempt: Int): SyncWorker {
        val params = mock<WorkerParameters>()
        whenever(params.runAttemptCount).thenReturn(attempt)
        return SyncWorker(mock<Context>(), params, repository)
    }
}
