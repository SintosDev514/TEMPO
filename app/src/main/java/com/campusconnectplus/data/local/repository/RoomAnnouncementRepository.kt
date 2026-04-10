package com.campusconnectplus.data.local.repository

import com.campusconnectplus.data.local.dao.AnnouncementDao
import com.campusconnectplus.data.repository.Announcement
import com.campusconnectplus.data.repository.AnnouncementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomAnnouncementRepository(
    private val dao: AnnouncementDao
) : AnnouncementRepository {

    override fun observeAnnouncements(): Flow<List<Announcement>> =
        dao.observeAll().map { it.map { a -> a.toModel() } }

    override suspend fun upsert(announcement: Announcement) {
        dao.upsert(announcement.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}
