package com.campusconnectplus.data.fake

import com.campusconnectplus.data.repository.Announcement
import com.campusconnectplus.data.repository.AnnouncementRepository
import com.campusconnectplus.data.repository.AnnouncementStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeAnnouncementRepository : AnnouncementRepository {
    private val mutex = Mutex()
    private val items = MutableStateFlow<List<Announcement>>(
        listOf(
            Announcement(id = 1L, title = "Semester Registration Open", content = "Registration for Spring 2026 is now open.", priority = 0, status = AnnouncementStatus.ACTIVE),
            Announcement(id = 2L, title = "Library Extended Hours", content = "Library will be open 24/7 during exam week.", priority = 1, status = AnnouncementStatus.ACTIVE)
        )
    )

    override fun observeAnnouncements(): Flow<List<Announcement>> = items

    override suspend fun upsert(announcement: Announcement) {
        mutex.withLock {
            val list = items.value.toMutableList()
            val idx = list.indexOfFirst { it.id == announcement.id && announcement.id != 0L }
            if (idx >= 0) list[idx] = announcement else {
                val newId = (list.maxOfOrNull { it.id } ?: 0L) + 1
                list.add(announcement.copy(id = newId))
            }
            items.value = list
        }
    }

    override suspend fun delete(id: String) {
        mutex.withLock {
            items.value = items.value.filterNot { it.id.toString() == id }
        }
    }
}
