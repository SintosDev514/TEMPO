package com.campusconnectplus.data.local.repository

import com.campusconnectplus.data.local.dao.FavoriteDao
import com.campusconnectplus.data.local.entity.FavoriteEntity
import com.campusconnectplus.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFavoriteRepository(
    private val dao: FavoriteDao
) : FavoriteRepository {

    companion object {
        const val TYPE_EVENT = "EVENT"
        const val TYPE_MEDIA = "MEDIA"
    }

    override fun observeFavoriteEventIds(): Flow<Set<Long>> =
        dao.observeByType(TYPE_EVENT).map { list -> list.map { it.refId }.toSet() }

    override fun observeFavoriteMediaIds(): Flow<Set<Long>> =
        dao.observeByType(TYPE_MEDIA).map { list -> list.map { it.refId }.toSet() }

    override suspend fun isEventFavorite(eventId: Long): Boolean =
        dao.getOne(TYPE_EVENT, eventId) != null

    override suspend fun isMediaFavorite(mediaId: Long): Boolean =
        dao.getOne(TYPE_MEDIA, mediaId) != null

    override suspend fun toggleEvent(eventId: Long) {
        val exists = dao.getOne(TYPE_EVENT, eventId)
        if (exists != null) dao.delete(TYPE_EVENT, eventId)
        else dao.insert(FavoriteEntity(type = TYPE_EVENT, refId = eventId))
    }

    override suspend fun toggleMedia(mediaId: Long) {
        val exists = dao.getOne(TYPE_MEDIA, mediaId)
        if (exists != null) dao.delete(TYPE_MEDIA, mediaId)
        else dao.insert(FavoriteEntity(type = TYPE_MEDIA, refId = mediaId))
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}
