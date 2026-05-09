package com.campusconnectplus.data.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavoriteEventIds(): Flow<Set<String>>
    fun observeFavoriteMediaIds(): Flow<Set<String>>

    suspend fun isEventFavorite(eventId: String): Boolean
    suspend fun isMediaFavorite(mediaId: String): Boolean

    suspend fun toggleEvent(eventId: String)
    suspend fun toggleMedia(mediaId: String)

    suspend fun clearAll()
}
