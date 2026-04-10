package com.campusconnectplus.data.fake

import com.campusconnectplus.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeEventRepository : EventRepository {
    private val mutex = Mutex()

    private val events = MutableStateFlow(
        listOf(
            Event(
                id = 1L,
                title = "Tech Innovation Summit 2026",
                date = "Feb 15, 2026",
                venue = "Main Auditorium",
                category = EventCategory.ACADEMIC,
                description = "Annual technology summit."
            ),
            Event(
                id = 2L,
                title = "Annual Sports Festival",
                date = "Feb 20, 2026",
                venue = "Sports Complex",
                category = EventCategory.SPORTS,
                description = "Inter-department sports."
            ),
            Event(
                id = 3L,
                title = "Cultural Night 2026",
                date = "Feb 25, 2026",
                venue = "Open Theatre",
                category = EventCategory.CULTURAL,
                description = "Celebrate diversity."
            )
        )
    )

    override fun observeEvents(): Flow<List<Event>> = events

    override fun observeEvent(eventId: String): Flow<Event?> =
        events.map { list -> list.find { it.id.toString() == eventId } }

    override suspend fun upsert(event: Event) {
        mutex.withLock {
            val list = events.value.toMutableList()
            val idx = list.indexOfFirst { it.id == event.id && event.id != 0L }
            if (idx >= 0) list[idx] = event
            else {
                val newId = (list.maxOfOrNull { it.id } ?: 0L) + 1
                list.add(event.copy(id = newId))
            }
            events.value = list
        }
    }

    override suspend fun delete(eventId: String) {
        mutex.withLock {
            events.value = events.value.filterNot { it.id.toString() == eventId }
        }
    }
}
