package com.campusconnectplus.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.campusconnectplus.data.repository.AnnouncementRepository
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.MediaRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val eventRepo: EventRepository,
    private val mediaRepo: MediaRepository,
    private val announcementRepo: AnnouncementRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            coroutineScope {
                val eventSync = async { syncEvents() }
                val mediaSync = async { syncMedia() }
                val annSync = async { syncAnnouncements() }

                eventSync.await()
                mediaSync.await()
                annSync.await()
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun syncEvents() {
        eventRepo.sync()
    }

    private suspend fun syncMedia() {
        mediaRepo.sync()
    }

    private suspend fun syncAnnouncements() {
        announcementRepo.sync()
    }

    companion object {
        const val WORK_NAME = "com.campusconnectplus.SyncWorker"

        fun startPeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, java.util.concurrent.TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
