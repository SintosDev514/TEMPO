package com.campusconnectplus.data.local.repository

import com.campusconnectplus.data.local.dao.AnnouncementDao
import com.campusconnectplus.data.repository.Announcement
import com.campusconnectplus.data.repository.AnnouncementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local Room implementation of [AnnouncementRepository].
 * This handles the persistence of announcements in th
    override fun observeAnnouncements(): Flow<List<Announcement>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun upsert(announcement: Announcement) {
        dao.upsert(announcement.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }

    override suspend fun sync() {
        // Sync implementation for local repository if needed.
        // Usually handled by the OfflineFirst wrapper which coordinates 
        // between this local repository and a remote source.
    }
}
