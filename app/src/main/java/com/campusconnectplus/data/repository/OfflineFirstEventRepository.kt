package com.campusconnectplus.data.repository

import com.campusconnectplus.data.local.dao.EventDao
import com.campusconnectplus.data.local.repository.toEntity
import com.campusconnectplus.data.local.repository.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class OfflineFirstEventRepository @Inject constructor(
    private val remote: EventRepository,
    private val local: EventDao
) : EventRepository {

    override fun observeEvents(): Flow<List<Event>> = channelFlow {
        // Collect local data first and immediately
        val localJob = launch {
            local.observeAll()
                .map { list -> list.map { it.toModel() } }
                .collect { send(it) }
        }

        // Then try to sync from remote
        val syncJob = launch(Dispatchers.IO) {
            remote.observeEvents()
                .retryWhen { _, attempt ->
                    val delayTime = (attempt * 1000).coerceAtMost(5000)
                    kotlinx.coroutines.delay(delayTime)
                    true
                }
                .catch { println("Sync Error (Events): ${it.message}") }
                .collectLatest { remoteList ->
                    local.sync(remoteList.map { it.toEntity() })
                }
        }

        awaitClose { 
            localJob.cancel()
            syncJob.cancel() 
        }
    }

    override fun observeEvent(eventId: String): Flow<Event?> = channelFlow {
        val localJob = launch {
            local.observeById(eventId)
                .map { it?.toModel() }
                .collect { send(it) }
        }

        val syncJob = launch(Dispatchers.IO) {
            remote.observeEvent(eventId)
                .retryWhen { _, attempt ->
                    val delayTime = (attempt * 1000).coerceAtMost(5000)
                    kotlinx.coroutines.delay(delayTime)
                    true
                }
                .catch { println("Sync Error (Event $eventId): ${it.message}") }
                .collectLatest { event ->
                    event?.let { local.upsert(it.toEntity()) }
                }
        }

        awaitClose { 
            localJob.cancel()
            syncJob.cancel() 
        }
    }

    override suspend fun upsert(event: Event) {
        // Save locally first
        local.upsert(event.toEntity())
        
        // Then attempt remote
        try {
            remote.upsert(event)
        } catch (e: Exception) {
            println("Offline: Event will be synced when online.")
        }
    }

    override suspend fun delete(eventId: String) {
        local.delete(eventId)
        try {
            remote.delete(eventId)
        } catch (e: Exception) {
            println("Offline: Event deletion will be synced when online.")
        }
    }

    override suspend fun reactToEvent(eventId: String, reactionType: ReactionType?) {
        // Update local first for snappy UI
        val current = local.observeById(eventId).take(1).firstOrNull()?.toModel()
        current?.let {
            val newCounts = it.reactionCounts.toMutableMap()
            
            // If there was a previous reaction, decrement it
            it.userReaction?.let { prev ->
                val prevCount = newCounts[prev] ?: 0
                if (prevCount > 0) newCounts[prev] = prevCount - 1
            }

            // If there's a new reaction, increment it
            reactionType?.let { r -> 
                newCounts[r] = (newCounts[r] ?: 0) + 1 
            }

            val updated = it.copy(
                userReaction = reactionType,
                reactionCounts = newCounts,
                updatedAt = System.currentTimeMillis()
            )
            local.upsert(updated.toEntity())
        }

        // Then remote
        try {
            remote.reactToEvent(eventId, reactionType)
        } catch (e: Exception) {
            println("Remote Reaction Error: ${e.message}")
        }
    }

    override suspend fun sync() {
        remote.observeEvents().take(1).collectLatest { remoteList ->
            local.sync(remoteList.map { it.toEntity() })
        }
    }
}
