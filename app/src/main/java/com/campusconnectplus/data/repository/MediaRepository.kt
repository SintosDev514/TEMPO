package com.campusconnectplus.data.repository

import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun observeMedia(): Flow<List<Media>>
    fun ofEvent(eventId: String): Flow<List<Media>>
    suspend fun upsert(media: Media)
    suspend fun delete(mediaId: String)
}
