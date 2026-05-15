package com.campusconnectplus.data.remote.repository

import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.ReactionType
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
    reactionCounts = reaction_counts?.mapKeys { 
        try { com.campusconnectplus.data.repository.ReactionType.valueOf(it.key.uppercase()) } 
        catch (e: Exception) { com.campusconnectplus.data.repository.ReactionType.LIKE }
    } ?: emptyMap(),
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
        val initialEvents = postgrest["events"].select().decodeList<RemoteEvent>()
        emit(initialEvents.map { it.toModel() })

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
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    override fun observeEvent(eventId: String): Flow<Event?> = flow {
        val initialEvent = postgrest["events"].select {
            filter { eq("id", eventId) }
        }.decodeSingleOrNull<RemoteEvent>()
        emit(initialEvent?.toModel())

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

    override suspend fun reactToEvent(eventId: String, reactionType: ReactionType?) {
        withContext(Dispatchers.IO) {
            try {
                // Get current event to update counts
                val current = postgrest["events"].select {
                    filter { eq("id", eventId) }
                }.decodeSingleOrNull<RemoteEvent>() ?: return@withContext

                val counts = current.reaction_counts?.toMutableMap() ?: mutableMapOf()
                
                // Note: Simplified logic. Real implementation would track user-specific reactions
                // in a separate table and use a DB trigger/RPC to update counts.
                // Here we just increment for demo/local feel if we were updating counts directly.
                // Since 'reaction_counts' is likely a summary field:
                
                reactionType?.let {
                    val key = it.name.lowercase()
                    counts[key] = (counts[key] ?: 0) + 1
                }

                postgrest["events"].update({
                    set("reaction_counts", counts)
                    set("updated_at", System.currentTimeMillis())
                }) {
                    filter { eq("id", eventId) }
                }
            } catch (e: Exception) {
                println("REACT ERROR: ${e.message}")
            }
        }
    }

    override suspend fun sync() {}
}
