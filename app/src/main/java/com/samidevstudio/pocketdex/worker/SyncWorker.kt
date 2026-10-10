package com.samidevstudio.pocketdex.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.samidevstudio.pocketdex.data.PokemonRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import java.io.IOException
import retrofit2.HttpException

private const val MAX_RETRY_ATTEMPTS = 3

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: PokemonRepository
) : CoroutineWorker(context, params) {

    private fun retryOrFail(): Result =
        if (runAttemptCount < MAX_RETRY_ATTEMPTS - 1) Result.retry() else Result.failure()

    override suspend fun doWork(): Result {
        return try {
            repository.backfillMissingTypes()
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (_: IOException) {
            retryOrFail()
        } catch (e: HttpException) {
            if (e.code() == 429 || e.code() in 500..599) retryOrFail() else Result.failure()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}
