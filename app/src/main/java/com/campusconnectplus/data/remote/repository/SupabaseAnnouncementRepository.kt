package com.campusconnectplus.data.remote.repository

import com.campusconnectplus.data.repository.Announcement
import com.campusconnectplus.data.repository.AnnouncementRepository
import com.campusconnectplus.data.repository.AnnouncementStatus
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
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

fun RemoteAnnouncement.toModel() = Announcement(
    id = id ?: "",
    title = title ?: "Untitled Announcement",
    content = content ?: "",
    priority = priority ?: 0,
    status = try { status?.let { AnnouncementStatus.valueOf(it.uppercase()) } ?: AnnouncementStatus.ACTIVE } catch (e: Exception) { AnnouncementStatus.ACTIVE },
    createdAt = created_at ?: System.currentTimeMillis(),
    updatedAt = updated_at ?: System.currentTimeMillis()
)



fun Announcement.toRemote() = RemoteAnnouncement(
    id = id.ifEmpty { null },
    title = title,
    content = content,
    priority = priority,
    status = status.name,
    created_at = null,
    updated_at = System.currentTimeMillis()
)

class SupabaseAnnouncementRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime
) : AnnouncementRepository {

    override fun observeAnnouncements(): Flow<List<Announcement>> = flow {
        // 1. Initial Fetch
        try {
            val initial = postgrest["announcements"].select().decodeList<RemoteAnnouncement>()
            emit(initial.map { it.toModel() })
        } catch (e: Exception) {
            println("Initial fetch error (announcements): ${e.message}")
            emit(emptyList())
        }

        // 2. Realtime
        val channelId = "announcements_realtime_${UUID.randomUUID()}"
        val channel = realtime.channel(channelId)
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "announcements"
            }
            channel.subscribe()

            changeFlow.collect { action ->
                println("Realtime action received (ann): $action")
                val updated = postgrest["announcements"].select().decodeList<RemoteAnnouncement>()
                emit(updated.map { it.toModel() })
            }
        } catch (e: Exception) {
            println("Realtime error (announcements): ${e.message}")
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)


    override suspend fun upsert(announcement: Announcement) {
        withContext(Dispatchers.IO) {
            try {
                postgrest["announcements"].upsert(announcement.toRemote())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            try {
                postgrest["announcements"].delete {
                    filter {
                        eq("id", id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
