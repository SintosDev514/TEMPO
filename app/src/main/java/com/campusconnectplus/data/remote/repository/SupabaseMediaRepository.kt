package com.campusconnectplus.data.remote.repository

import com.campusconnectplus.data.repository.Media
import com.campusconnectplus.data.repository.MediaRepository
import com.campusconnectplus.data.repository.MediaType
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

fun RemoteMedia.toModel() = Media(
    id = id ?: "",
    eventId = event_id ?: "",
    url = url ?: "",
    type = try { type?.let { MediaType.valueOf(it.uppercase()) } ?: MediaType.IMAGE } catch (e: Exception) { MediaType.IMAGE },
    title = title ?: "Untitled",
    fileName = file_name ?: "",
    date = date ?: "",
    sizeMb = size_mb ?: 0,
    duration = duration ?: "",
    saves = saves ?: 0,
    coverUrl = cover_url ?: "",
    updatedAt = updated_at ?: System.currentTimeMillis()
)


fun Media.toRemote() = RemoteMedia(
    id = if (id.isBlank()) null else id,
    event_id = if (eventId.isBlank()) null else eventId,
    url = url.ifBlank { null },
    type = type.name.lowercase(),
    title = title.ifBlank { "Untitled" },
    file_name = fileName.ifBlank { "file" },
    date = date.ifBlank { null },
    size_mb = sizeMb,
    duration = duration.ifBlank { null },
    saves = saves,
    cover_url = coverUrl.ifBlank { null },
    updated_at = System.currentTimeMillis()
)

class SupabaseMediaRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime,
    private val storage: Storage,
    private val auth: Auth
) : MediaRepository {

    override fun observeMedia(): Flow<List<Media>> = flow {
        // 1. Initial Fetch
        try {
            val initial = postgrest["media"].select().decodeList<RemoteMedia>()
            emit(initial.map { it.toModel() })
        } catch (e: Exception) {
            println("Initial fetch error (media): ${e.message}")
            emit(emptyList())
        }

        // 2. Realtime
        val channelId = "media_realtime_${UUID.randomUUID()}"
        val channel = realtime.channel(channelId)
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "media"
            }
            channel.subscribe()

            changeFlow.collect {
                val updated = postgrest["media"].select().decodeList<RemoteMedia>()
                emit(updated.map { it.toModel() })
            }
        } catch (e: Exception) {
            println("Realtime error (media): ${e.message}")
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    override fun ofEvent(eventId: String): Flow<List<Media>> = flow {
        // 1. Initial Fetch
        try {
            val initial = postgrest["media"].select {
                filter { eq("event_id", eventId) }
            }.decodeList<RemoteMedia>()
            emit(initial.map { it.toModel() })
        } catch (e: Exception) {
            println("Initial fetch error (media of event): ${e.message}")
            emit(emptyList())
        }

        // 2. Realtime
        val channelId = "media_event_realtime_${eventId}_${UUID.randomUUID()}"
        val channel = realtime.channel(channelId)
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "media"
            }
            channel.subscribe()

            changeFlow.collect {
                val updated = postgrest["media"].select {
                    filter { eq("event_id", eventId) }
                }.decodeList<RemoteMedia>()
                emit(updated.map { it.toModel() })
            }
        } catch (e: Exception) {
            println("Realtime error (media of event): ${e.message}")
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)


    override suspend fun uploadFile(bucket: String, path: String, byteArray: ByteArray): String {
        return try {
            println("DEBUG: Starting Storage Upload to '$bucket'...")
            val bucketRef = storage.from(bucket)
            // Note: upsert = true requires both INSERT and UPDATE policies
            bucketRef.upload(path, byteArray) {
                upsert = true
            }
            val url = bucketRef.publicUrl(path)
            println("DEBUG: Storage Upload Success. URL: $url")
            url
        } catch (e: Exception) {
            println("DEBUG: Storage Error: ${e.message}")
            val isRls = e.message?.contains("security policy", ignoreCase = true) == true
            throw Exception(if (isRls) 
                "STEP 1 FAILED: Storage Policy Violation. Please go to Supabase Dashboard -> Storage -> Policies and add 'INSERT' and 'UPDATE' policies for the '$bucket' bucket." 
                else "Storage Error: ${e.message}")
        }
    }

    override suspend fun upsert(media: Media) {
        withContext(Dispatchers.IO) {
            try {
                val session = auth.currentSessionOrNull()
                val currentUserId = session?.user?.id
                
                println("DEBUG: Authenticated User ID: $currentUserId")
                
                // We send the record without user_id first to avoid "column does not exist" errors
                // because RemoteMedia marks user_id as @Transient
                val remote = media.toRemote()
                
                println("DEBUG: Starting Database Upsert for file: ${remote.file_name}")
                try {
                    postgrest["media"].upsert(remote)
                    println("DEBUG: Database Upsert Success.")
                } catch (e: Exception) {
                    println("DEBUG: Inner Database Error: ${e.message}")
                    throw e
                }
            } catch (e: Exception) {
                println("DEBUG: Final Database Error: ${e.message}")
                val msg = e.message ?: ""
                if (msg.contains("security policy", ignoreCase = true) || msg.contains("RLS", ignoreCase = true)) {
                    throw Exception("STEP 2 FAILED (Database): The record could not be saved. Run 'ALTER TABLE public.media DISABLE ROW LEVEL SECURITY;' in your Supabase SQL Editor.")
                } else {
                    throw Exception("Database Error: $msg")
                }
            }
        }
    }

    override suspend fun delete(mediaId: String) {
        withContext(Dispatchers.IO) {
            try {
                postgrest["media"].delete {
                    filter {
                        eq("id", mediaId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
