package com.campusconnectplus.data.repository

import com.campusconnectplus.data.local.dao.AnnouncementDao
import com.campusconnectplus.data.local.repository.toEntity
import com.campusconnectplus.data.local.repository.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class OfflineFirstAnnouncementRepository @Inject constructor(
    private val remote: AnnouncementRepository,
    private val local: AnnouncementDao
) : AnnouncementRepository {

    override fun observeAnnouncements(): Flow<List<Announcement>> = channelFlow {
        val localJob = launch {
            local.observeAll()
                .map { list -> list.map { it.toModel() } }
                .collect { send(it) }
        }

        val syncJob = launch(Dispatchers.IO) {
            remote.observeAnnouncements()
                .retryWhen { _, attempt ->
                    val delayTime = (attempt * 1000).coerceAtMost(5000)
                    kotlinx.coroutines.delay(delayTime)
                    true
                }
                .catch { println("Sync Error (Announcements): ${it.message}") }
                .collectLatest { remoteList ->
                    local.sync(remoteList.map { it.toEntity() })
                }
        }

        awaitClose { 
            localJob.cancel()
            syncJob.cancel() 
        }
    }

    override suspend fun upsert(announcement: Announcement) {
        local.upsert(announcement.toEntity())
        try {
            remote.upsert(announcement)
        } catch (e: Exception) {
            println("Offline: Announcement will be synced later.")
        }
    }

    override suspend fun delete(id: String) {
        local.delete(id)
        try {
            remote.delete(id)
        } catch (e: Exception) {
            println("Offline: Deletion will be synced later.")
        }
    }

    override suspend fun sync() {
        remote.observeAnnouncements().take(1).collectLatest { remoteList ->
            local.sync(remoteList.map { it.toEntity() })
        }
    }
}
