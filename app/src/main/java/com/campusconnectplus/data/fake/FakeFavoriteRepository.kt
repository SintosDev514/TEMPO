package com.campusconnectplus.data.fake

import com.campusconnectplus.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeFavoriteRepository : FavoriteRepository {

    private val mutex = Mutex()
    private val _eventIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _mediaIds = MutableStateFlow<Set<Long>>(emptySet())

    override fun observeFavoriteEventIds(): Flow<Set<Long>> = _eventIds

    override fun observeFavoriteMediaIds(): Flow<Set<Long>> = _mediaIds

    override suspend fun isEventFavorite(eventId: Long): Boolean =
        mutex.withLock { _eventIds.value.contains(eventId) }

    override suspend fun isMediaFavorite(mediaId: Long): Boolean =
        mutex.withLock { _mediaIds.value.contains(mediaId) }

    override suspend fun toggleEvent(eventId: Long) {
        mutex.withLock {
            _eventIds.value = if (_eventIds.value.contains(eventId)) {
                _eventIds.value - eventId
            } else {
                _eventIds.value + eventId
            }
        }
    }

    override suspend fun toggleMedia(mediaId: Long) {
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
