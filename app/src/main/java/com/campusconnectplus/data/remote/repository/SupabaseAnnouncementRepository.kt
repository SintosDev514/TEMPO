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
import javax.inject.Inject

@Serializable
data class RemoteAnnouncement(
    val id: String? = null,
    val title: String,
    val content: String,
    val priority: Int,
    val status: String,
    val created_at: Long? = null,
    val updated_at: Long? = null
)

fun RemoteAnnouncement.toModel() = Announcement(
    id = id ?: "",
    title = title,
    content = content,
    priority = priority,
    status = try { AnnouncementStatus.valueOf(status) } catch (e: Exception) { AnnouncementStatus.ACTIVE },
    createdAt = created_at ?: System.currentTimeMillis(),
    updatedAt = updated_at ?: System.currentTimeMillis()
)

fun Announcement.toRemote() = RemoteAnnouncement(
    id = id.ifEmpty { null },
    title = title,
    content = content,
    priority = priority,
    status = status.name,
    created_at = createdAt,
    updated_at = updatedAt
)

class SupabaseAnnouncementRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime
) : AnnouncementRepository {

    override fun observeAnnouncements(): Flow<List<Announcement>> = flow {
        val channel = realtime.channel("announcements_channel")
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "announcements"
            }
            channel.subscribe()

            val initial = postgrest["announcements"].select().decodeList<RemoteAnnouncement>()
            emit(initial.map { it.toModel() })

            changeFlow.collect { action ->
                println("Realtime action received (ann): $action")
                val updated = postgrest["announcements"].select().decodeList<RemoteAnnouncement>()
                emit(updated.map { it.toModel() })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val list = postgrest["announcements"].select().decodeList<RemoteAnnouncement>()
                emit(list.map { it.toModel() })
            } catch (e2: Exception) {
                emit(emptyList())
            }
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
