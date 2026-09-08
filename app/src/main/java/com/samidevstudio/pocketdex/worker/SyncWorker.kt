package com.samidevstudio.pocketdex.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.samidevstudio.pocketdex.data.PokemonRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: PokemonRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.backfillMissingTypes()
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error during backfill sync", e)
            Result.retry()
        }
    }
}
