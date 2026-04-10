package com.campusconnectplus.data.fake

import com.campusconnectplus.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeMediaRepository : MediaRepository {
    private val mutex = Mutex()

    private val media = MutableStateFlow(
        listOf(
            Media(id = 1L, eventId = 1L, url = "https://example.com/1.jpg", type = MediaType.IMAGE, title = "Tech Summit Opening", date = "Feb 10, 2026", saves = 24),
            Media(id = 2L, eventId = 1L, url = "https://example.com/2.mp4", type = MediaType.VIDEO, title = "Campus Tour 2026", date = "Feb 8, 2026", duration = "5:32", saves = 45),
            Media(id = 3L, eventId = 2L, url = "https://example.com/3.jpg", type = MediaType.IMAGE, title = "Sports Day Highlights", date = "Feb 5, 2026", saves = 32),
            Media(id = 4L, eventId = 3L, url = "https://example.com/4.jpg", type = MediaType.IMAGE, title = "Cultural Festival", date = "Feb 3, 2026", saves = 56)
        )
    )

    override fun observeMedia(): Flow<List<Media>> = media

    override fun ofEvent(eventId: String): Flow<List<Media>> =
        media.map { list -> list.filter { it.eventId.toString() == eventId } }

    override suspend fun upsert(mediaItem: Media) {
        mutex.withLock {
            val list = media.value.toMutableList()
            val idx = list.indexOfFirst { it.id == mediaItem.id && mediaItem.id != 0L }
            if (idx >= 0) list[idx] = mediaItem
            else {
                val newId = (list.maxOfOrNull { it.id } ?: 0L) + 1
                list.add(mediaItem.copy(id = newId))
            }
            media.value = list
        }
    }

    override suspend fun delete(mediaId: String) {
        mutex.withLock {
            media.value = media.value.filterNot { it.id.toString() == mediaId }
        }
    }
}
