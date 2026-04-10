package com.campusconnectplus.data.local.repository

import com.campusconnectplus.data.local.dao.MediaDao
import com.campusconnectplus.data.repository.Media
import com.campusconnectplus.data.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMediaRepository(
    private val dao: MediaDao
) : MediaRepository {

    override fun observeMedia(): Flow<List<Media>> =
        dao.observeAll().map { it.map { e -> e.toModel() } }

    override fun ofEvent(eventId: String): Flow<List<Media>> =
        dao.observeForEvent(eventId).map { it.map { e -> e.toModel() } }

    override suspend fun upsert(media: Media) {
        dao.upsert(media.toEntity())
    }

    override suspend fun delete(mediaId: String) {
        dao.delete(mediaId)
    }
}
