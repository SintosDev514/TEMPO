package com.campusconnectplus.data.remote.repository

import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.ReactionType
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.util.UUID

fun RemoteEvent.toModel(userReaction: ReactionType? = null) = Event(
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
    imageUrl = image_url,
    reactionCounts = reaction_counts?.let { json ->
        json.keys.associate { key ->
            val type = try { 
                ReactionType.valueOf(key.uppercase()) 
            } catch (e: Exception) { 
                ReactionType.LIKE 
            }
            val count = json[key]?.let { 
                if (it is kotlinx.serialization.json.JsonPrimitive) it.content.toIntOrNull() ?: 0 else 0 
            } ?: 0
            type to count
        }
    } ?: emptyMap(),
    userReaction = userReaction,
    updatedAt = updated_at ?: System.currentTimeMillis()
)

fun Event.toRemote() = RemoteEvent(
    id = id.ifEmpty { null },
    title = title,
    date = date,
    venue = venue,
    description = description,
    category = category.name,
    image_url = imageUrl,
    updated_at = System.currentTimeMillis()
)

class SupabaseEventRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime,
    private val auth: Auth
) : EventRepository {

    private suspend fun getMyReactions(): Map<String, ReactionType> {
        val userId = auth.currentSessionOrNull()?.user?.id ?: return emptyMap()
        return try {
            postgrest["event_reactions"].select {
                filter { eq("user_id", userId) }
            }.decodeList<RemoteReaction>().associate { 
                it.event_id to try { ReactionType.valueOf(it.reaction_type.uppercase()) } catch(e: Exception) { ReactionType.LIKE }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    override fun observeEvents(): Flow<List<Event>> = flow {
        val myReactions = getMyReactions()
        val initialEvents = postgrest["events"].select().decodeList<RemoteEvent>()
        emit(initialEvents.map { it.toModel(myReactions[it.id]) })

        val channelId = "events_${UUID.randomUUID()}"
        val channel = realtime.channel(channelId)
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "events"
            }
            val reactionChangeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "event_reactions"
            }
            channel.subscribe()
            
            kotlinx.coroutines.flow.merge(changeFlow, reactionChangeFlow).collect {
                val currentMyReactions = getMyReactions()
                val updatedEvents = postgrest["events"].select().decodeList<RemoteEvent>()
                emit(updatedEvents.map { it.toModel(currentMyReactions[it.id]) })
            }
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    override fun observeEvent(eventId: String): Flow<Event?> = flow {
        val userId = auth.currentSessionOrNull()?.user?.id
        val myReaction = userId?.let { uid ->
            postgrest["event_reactions"].select {
                filter { 
                    eq("event_id", eventId)
                    eq("user_id", uid)
                }
            }.decodeSingleOrNull<RemoteReaction>()?.let { 
                try { ReactionType.valueOf(it.reaction_type.uppercase()) } catch(e: Exception) { null }
            }
        }

        val initialEvent = postgrest["events"].select {
            filter { eq("id", eventId) }
        }.decodeSingleOrNull<RemoteEvent>()
        emit(initialEvent?.toModel(myReaction))

        val channelId = "event_${eventId}_${UUID.randomUUID()}"
        val channel = realtime.channel(channelId)
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "events"
            }
            val reactionChangeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "event_reactions"
            }
            channel.subscribe()
            
            // Collect both event changes and user's own reaction changes
            kotlinx.coroutines.flow.merge(changeFlow, reactionChangeFlow).collect {
                val currentMyReaction = auth.currentSessionOrNull()?.user?.id?.let { uid ->
                    postgrest["event_reactions"].select {
                        filter { 
                            eq("event_id", eventId)
                            eq("user_id", uid)
                        }
                    }.decodeSingleOrNull<RemoteReaction>()?.let { 
                        try { ReactionType.valueOf(it.reaction_type.uppercase()) } catch(e: Exception) { null }
                    }
                }
                val updatedEvent = postgrest["events"].select {
                    filter { eq("id", eventId) }
                }.decodeSingleOrNull<RemoteEvent>()
                emit(updatedEvent?.toModel(currentMyReaction))
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
        val userId = auth.currentSessionOrNull()?.user?.id ?: throw Exception("You must be logged in to react")
        withContext(Dispatchers.IO) {
            try {
                if (reactionType == null) {
                    postgrest["event_reactions"].delete {
                        filter {
                            eq("event_id", eventId)
                            eq("user_id", userId)
                        }
                    }
                } else {
                    postgrest["event_reactions"].upsert(
                        RemoteReaction(
                            event_id = eventId,
                            user_id = userId,
                            reaction_type = reactionType.name
                        )
                    )
                }
            } catch (e: Exception) {
                println("REACT ERROR: ${e.message}")
                throw e
            }
        }
    }

    override suspend fun sync() {
        // No-op for remote repo as it's already real-time.
        // The offline-first repo will call observeEvents().take(1) to sync local DB.
    }
}
