package com.campusconnectplus.data.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavoriteEventIds(): Flow<Set<Long>>
    fun observeFavoriteMediaIds(): Flow<Set<Long>>

    suspend fun isEventFavorite(eventId: Long): Boolean
    suspend fun isMediaFavorite(mediaId: Long): Boolean

    suspend fun toggleEvent(eventId: Long)
    suspend fun toggleMedia(mediaId: Long)

    suspend fun clearAll()
}
