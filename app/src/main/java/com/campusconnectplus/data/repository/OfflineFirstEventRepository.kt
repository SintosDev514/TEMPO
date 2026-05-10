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

        local.observeAll()
            .map { list -> list.map { it.toModel() } }
            .collect { send(it) }

        awaitClose { syncJob.cancel() }
    }

    override fun observeEvent(eventId: String): Flow<Event?> = channelFlow {
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

        local.observeById(eventId)
            .map { it?.toModel() }
            .collect { send(it) }

        awaitClose { syncJob.cancel() }
    }

    override suspend fun upsert(event: Event) {
        remote.upsert(event)
        local.upsert(event.toEntity())
    }

    override suspend fun delete(eventId: String) {
        remote.delete(eventId)
        local.delete(eventId)
    }
}
