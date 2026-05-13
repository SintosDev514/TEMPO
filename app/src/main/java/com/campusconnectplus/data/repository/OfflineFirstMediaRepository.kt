package com.campusconnectplus.data.repository

import com.campusconnectplus.data.local.dao.MediaDao
import com.campusconnectplus.data.local.repository.toEntity
import com.campusconnectplus.data.local.repository.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class OfflineFirstMediaRepository @Inject constructor(
    private val remote: MediaRepository,
    private val local: MediaDao
) : MediaRepository {

    override fun observeMedia(): Flow<List<Media>> = channelFlow {
        val localJob = launch {
            local.observeAll()
                .map { list -> list.map { it.toModel() } }
                .collect { send(it) }
        }

        val syncJob = launch(Dispatchers.IO) {
            remote.observeMedia()
                .retryWhen { _, attempt ->
                    val delayTime = (attempt * 1000).coerceAtMost(5000)
                    kotlinx.coroutines.delay(delayTime)
                    true
                }
                .catch { println("Sync Error (Media): ${it.message}") }
                .collectLatest { remoteList ->
                    local.sync(remoteList.map { it.toEntity() })
                }
        }

        awaitClose { 
            localJob.cancel()
            syncJob.cancel() 
        }
    }

    override fun ofEvent(eventId: String): Flow<List<Media>> = channelFlow {
        val localJob = launch {
            local.observeForEvent(eventId)
                .map { list -> list.map { it.toModel() } }
                .collect { send(it) }
        }

        val syncJob = launch(Dispatchers.IO) {
            remote.ofEvent(eventId)
                .retryWhen { _, attempt ->
                    val delayTime = (attempt * 1000).coerceAtMost(5000)
                    kotlinx.coroutines.delay(delayTime)
                    true
                }
                .catch { println("Sync Error (Media for event $eventId): ${it.message}") }
                .collectLatest { remoteList ->
                    local.syncForEvent(eventId, remoteList.map { it.toEntity() })
                }
        }

        awaitClose { 
            localJob.cancel()
            syncJob.cancel() 
        }
    }

    override suspend fun upsert(media: Media) {
        // Save to local first for immediate UI update
        local.upsert(media.toEntity())
        
        // Then attempt remote sync
        try {
            remote.upsert(media)
        } catch (e: Exception) {
            println("Offline: Media sync will happen later. ${e.message}")
        }
    }

    override suspend fun delete(mediaId: String) {
        local.delete(mediaId)
        try {
            remote.delete(mediaId)
        } catch (e: Exception) {
            println("Offline: Delete sync will happen later.")
        }
    }

    override suspend fun uploadFile(bucket: String, path: String, byteArray: ByteArray): String {
        return remote.uploadFile(bucket, path, byteArray)
    }

    override suspend fun sync() {
        remote.observeMedia().take(1).collectLatest { remoteList ->
            local.sync(remoteList.map { it.toEntity() })
        }
    }
}
