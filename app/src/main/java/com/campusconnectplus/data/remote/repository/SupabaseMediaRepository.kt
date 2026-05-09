package com.campusconnectplus.data.remote.repository

import com.campusconnectplus.data.repository.Media
import com.campusconnectplus.data.repository.MediaRepository
import com.campusconnectplus.data.repository.MediaType
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
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@Serializable
data class RemoteMedia(
    val id: String? = null,
    val event_id: String,
    val url: String,
    val type: String,
    val title: String,
    val file_name: String,
    val date: String,
    val size_mb: Int,
    val duration: String,
    val saves: Int,
    val cover_url: String,
    val updated_at: Long? = null
)

fun RemoteMedia.toModel() = Media(
    id = id ?: "",
    eventId = event_id,
    url = url,
    type = try { MediaType.valueOf(type) } catch (e: Exception) { MediaType.IMAGE },
    title = title,
    fileName = file_name,
    date = date,
    sizeMb = size_mb,
    duration = duration,
    saves = saves,
    coverUrl = cover_url,
    updatedAt = updated_at ?: System.currentTimeMillis()
)

fun Media.toRemote() = RemoteMedia(
    id = id.ifEmpty { null },
    event_id = eventId,
    url = url,
    type = type.name,
    title = title,
    file_name = fileName,
    date = date,
    size_mb = sizeMb,
    duration = duration,
    saves = saves,
    cover_url = coverUrl,
    updated_at = updatedAt
)

class SupabaseMediaRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime,
    private val storage: Storage
) : MediaRepository {

    override fun observeMedia(): Flow<List<Media>> = flow {
        val channel = realtime.channel("media_channel")
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "media"
            }
            channel.subscribe()

            val initial = postgrest["media"].select().decodeList<RemoteMedia>()
            emit(initial.map { it.toModel() })

            changeFlow.collect {
                val updated = postgrest["media"].select().decodeList<RemoteMedia>()
                emit(updated.map { it.toModel() })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    override fun ofEvent(eventId: String): Flow<List<Media>> = flow {
        val channel = realtime.channel("media_event_$eventId")
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "media"
            }
            channel.subscribe()

            val initial = postgrest["media"].select {
                filter {
                    eq("event_id", eventId)
                }
            }.decodeList<RemoteMedia>()
            emit(initial.map { it.toModel() })

            changeFlow.collect {
                val updated = postgrest["media"].select {
                    filter {
                        eq("event_id", eventId)
                    }
                }.decodeList<RemoteMedia>()
                emit(updated.map { it.toModel() })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun uploadFile(bucket: String, path: String, byteArray: ByteArray): String {
        return try {
            println("STORAGE: Uploading to $bucket / $path")
            val bucketRef = storage.from(bucket)
            bucketRef.upload(path, byteArray) {
                upsert = true
            }
            val url = bucketRef.publicUrl(path)
            println("STORAGE: Upload successful. Public URL: $url")
            url
        } catch (e: Exception) {
            println("STORAGE ERROR: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun upsert(media: Media) {
        withContext(Dispatchers.IO) {
            try {
                val remote = media.toRemote()
                println("DATABASE: Upserting media record: $remote")
                postgrest["media"].upsert(remote)
                println("DATABASE: Media record saved successfully")
            } catch (e: Exception) {
                println("DATABASE ERROR: ${e.message}")
                e.printStackTrace()
                throw e
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
