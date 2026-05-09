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

@Serializable
data class RemoteEvent(
    val id: String? = null,
    val title: String,
    val date: String,
    val venue: String,
    val description: String,
    val category: String,
    val updated_at: String? = null,
    val created_at: String? = null
)

fun RemoteEvent.toModel() = Event(
    id = id ?: "",
    title = title,
    date = date,
    venue = venue,
    description = description,
    category = try { com.campusconnectplus.data.repository.EventCategory.valueOf(category) } catch (e: Exception) { com.campusconnectplus.data.repository.EventCategory.ACADEMIC },
    updatedAt = try { updated_at?.toLong() ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
)

fun Event.toRemote() = RemoteEvent(
    id = id.ifEmpty { null },
    title = title,
    date = date,
    venue = venue,
    description = description,
    category = category.name,
    updated_at = null // Let Supabase handle the timestamp
)

class SupabaseEventRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime
) : EventRepository {

    override fun observeEvents(): Flow<List<Event>> = flow {
        val channel = realtime.channel("events_channel")
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "events"
            }
            channel.subscribe()

            // Emit initial data
            val initialEvents = postgrest["events"].select().decodeList<RemoteEvent>()
            emit(initialEvents.map { it.toModel() })

            // Listen for any change and re-fetch the whole list to ensure UI is in sync
            changeFlow.collect { action ->
                println("Realtime action received: $action")
                val updatedEvents = postgrest["events"].select().decodeList<RemoteEvent>()
                emit(updatedEvents.map { it.toModel() })
            }
        } catch (e: Exception) {
            println("Realtime error in observeEvents: ${e.message}")
            e.printStackTrace()
            // Fallback: Just fetch once if Realtime fails
            try {
                val events = postgrest["events"].select().decodeList<RemoteEvent>()
                emit(events.map { it.toModel() })
            } catch (e2: Exception) {
                emit(emptyList())
            }
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    override fun observeEvent(eventId: String): Flow<Event?> = flow {
        val channel = realtime.channel("event_$eventId")
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "events"
            }
            channel.subscribe()

            val initialEvent = postgrest["events"].select {
                filter {
                    eq("id", eventId)
                }
            }.decodeSingleOrNull<RemoteEvent>()
            emit(initialEvent?.toModel())

            changeFlow.collect {
                val updatedEvent = postgrest["events"].select {
                    filter {
                        eq("id", eventId)
                    }
                }.decodeSingleOrNull<RemoteEvent>()
                emit(updatedEvent?.toModel())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun upsert(event: Event) {
        withContext(Dispatchers.IO) {
            try {
                val remote = event.toRemote()
                postgrest["events"].upsert(remote)
            } catch (e: Exception) {
                // Re-throw so the ViewModel can catch it and show the error
                throw e
            }
        }
    }

    override suspend fun delete(eventId: String) {
        withContext(Dispatchers.IO) {
            try {
                postgrest["events"].delete {
                    filter {
                        eq("id", eventId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
