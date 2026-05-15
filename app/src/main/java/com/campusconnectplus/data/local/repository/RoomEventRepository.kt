package com.campusconnectplus.data.local.repository

import com.campusconnectplus.data.local.dao.EventDao
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.ReactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take

class RoomEventRepository(
    private val dao: EventDao
) : EventRepository {

    override fun observeEvents(): Flow<List<Event>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override fun observeEvent(eventId: String): Flow<Event?> =
        dao.observeById(eventId).map { it?.toModel() }

    override suspend fun upsert(event: Event) {
        dao.upsert(event.toEntity())
    }

    override suspend fun delete(eventId: String) {
        dao.delete(eventId)
    }

    override suspend fun reactToEvent(eventId: String, reactionType: ReactionType?) {
        val current = dao.observeById(eventId).take(1).firstOrNull() ?: return
        val model: Event = current.toModel()
        val newCounts = model.reactionCounts.toMutableMap()
        
        // If there was a previous reaction, decrement it
        model.userReaction?.let { prev ->
            val prevCount = newCounts[prev] ?: 0
            if (prevCount > 0) newCounts[prev] = prevCount - 1
        }

        // If there's a new reaction, increment it
        reactionType?.let { r ->
            newCounts[r] = (newCounts[r] ?: 0) + 1
        }
        
        val updated = model.copy(
            userReaction = reactionType,
            reactionCounts = newCounts,
            updatedAt = System.currentTimeMillis()
        )
        dao.upsert(updated.toEntity())
    }

    override suspend fun sync() {
        // Implementation for sync with remote source
    }
}
