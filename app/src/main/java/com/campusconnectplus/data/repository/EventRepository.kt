package com.campusconnectplus.data.repository

import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeEvents(): Flow<List<Event>>
    fun observeEvent(eventId: String): Flow<Event?>
    suspend fun upsert(event: Event)
    suspend fun delete(eventId: String)
}
