package com.campusconnectplus.data.repository

import kotlinx.coroutines.flow.Flow

interface AnnouncementRepository {
    fun observeAnnouncements(): Flow<List<Announcement>>
    suspend fun upsert(announcement: Announcement)
    suspend fun delete(id: String)
}
