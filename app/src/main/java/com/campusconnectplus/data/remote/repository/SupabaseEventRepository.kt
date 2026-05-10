package com.campusconnectplus.data.remote.repository

import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import javax.inject.Inject
import java.util.UUID

fun RemoteEvent.toModel() = Event(
    id = id ?: "",
    title = title ?: "Untitled Event",
    date = date ?: "",
    venue = venue ?: "TBD",
    description = description ?: "",
    category = try { 
        category?.let { com.campusconnectplus.data.repository.EventCategory.valueOf(it.uppercase()) } 
            ?: com.campusconnectplus.data.repository.EventCategory.ACADEMIC 
    } catch (e: Exception) { 
        com.campusconnectplus.data.repository.EventCategory.ACADEMIC 
    },
    updatedAt = updated_at ?: System.currentTimeMillis()
)

fun Event.toRemote() = RemoteEvent(
    id = id.ifEmpty { null },
    title = title,
    date = date,
    venue = venue,
    description = description,
    category = category.name,
    updated_at = System.currentTimeMillis()
)

class SupabaseEventRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime
) : EventRepository {

    override fun observeEvents(): Flow<List<Event>> = flow {
        try {
            val initialEvents = postgrest["events"].select().decodeList<RemoteEvent>()
            emit(initialEvents.map { it.toModel() })
        } catch (e: Exception) {
            println("FETCH ERROR (events): ${e.message}")
            emit(emptyList())
        }

        val channelId = "events_${UUID.randomUUID()}"
        val channel = realtime.channel(channelId)
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "events"
            }
            channel.subscribe()
            changeFlow.collect {
                val updatedEvents = postgrest["events"].select().decodeList<RemoteEvent>()
                emit(updatedEvents.map { it.toModel() })
            }
        } catch (e: Exception) {
            println("REALTIME ERROR (events): ${e.message}")
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    override fun observeEvent(eventId: String): Flow<Event?> = flow {
        try {
            val initialEvent = postgrest["events"].select {
                filter { eq("id", eventId) }
            }.decodeSingleOrNull<RemoteEvent>()
            emit(initialEvent?.toModel())
        } catch (e: Exception) {
            println("FETCH ERROR (event $eventId): ${e.message}")
        }

        val channelId = "event_${eventId}_${UUID.randomUUID()}"
        val channel = realtime.channel(channelId)
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "events"
            }
            channel.subscribe()
            changeFlow.collect {
                val updatedEvent = postgrest["events"].select {
                    filter { eq("id", eventId) }
                }.decodeSingleOrNull<RemoteEvent>()
                emit(updatedEvent?.toModel())
            }
        } catch (e: Exception) {
            println("REALTIME ERROR (event $eventId): ${e.message}")
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun upsert(event: Event) {
        withContext(Dispatchers.IO) {
            try {
                postgrest["events"].upsert(event.toRemote())
            } catch (e: Exception) {
                println("UPSERT ERROR (event): ${e.message}")
                throw e
            }
        }
    }

    override suspend fun delete(eventId: String) {
        withContext(Dispatchers.IO) {
            try {
                postgrest["events"].delete {
                    filter { eq("id", eventId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
