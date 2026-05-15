package com.campusconnectplus.data.local.repository

import com.campusconnectplus.data.local.dao.AnnouncementDao
import com.campusconnectplus.data.repository.Announcement
import com.campusconnectplus.data.repository.AnnouncementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Local Room implementation of [AnnouncementRepository].
 * This handles the persistence of announcements in the local database.
 */
class RoomAnnouncementRepository @Inject constructor(
    private val dao: AnnouncementDao
) : AnnouncementRepository {

    override fun observeAnnouncements(): Flow<List<Announcement>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun upsert(announcement: Announcement) {
        dao.upsert(announcement.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }

    override suspend fun sync() {
        // Local sync is usually a no-op as coordination is handled by OfflineFirstAnnouncementRepository
    }
}
