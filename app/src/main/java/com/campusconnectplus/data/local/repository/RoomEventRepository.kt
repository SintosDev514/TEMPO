package com.campusconnectplus.data.local.repository

import com.campusconnectplus.data.local.dao.EventDao
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
}
