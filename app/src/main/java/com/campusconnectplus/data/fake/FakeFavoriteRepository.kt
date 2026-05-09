package com.campusconnectplus.data.fake

import com.campusconnectplus.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeFavoriteRepository : FavoriteRepository {

    private val mutex = Mutex()
    private val _eventIds = MutableStateFlow<Set<String>>(emptySet())
    private val _mediaIds = MutableStateFlow<Set<String>>(emptySet())

    override fun observeFavoriteEventIds(): Flow<Set<String>> = _eventIds

    override fun observeFavoriteMediaIds(): Flow<Set<String>> = _mediaIds

    override suspend fun isEventFavorite(eventId: String): Boolean =
        mutex.withLock { _eventIds.value.contains(eventId) }

    override suspend fun isMediaFavorite(mediaId: String): Boolean =
        mutex.withLock { _mediaIds.value.contains(mediaId) }

    override suspend fun toggleEvent(eventId: String) {
        mutex.withLock {
            _eventIds.value = if (_eventIds.value.contains(eventId)) {
                _eventIds.value - eventId
            } else {
                _eventIds.value + eventId
            }
        }
    }

    override suspend fun toggleMedia(mediaId: String) {
        mutex.withLock {
            _mediaIds.value = if (_mediaIds.value.contains(mediaId)) {
                _mediaIds.value - mediaId
            } else {
                _mediaIds.value + mediaId
            }
        }
    }

    override suspend fun clearAll() {
        mutex.withLock {
            _eventIds.value = emptySet()
            _mediaIds.value = emptySet()
        }
    }
}
