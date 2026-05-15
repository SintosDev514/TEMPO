package com.campusconnectplus.data.fake

import com.campusconnectplus.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class FakeEventRepository : EventRepository {
    private val mutex = Mutex()

    private val events = MutableStateFlow(
        listOf(
            Event(
                id = "1",
                title = "Tech Innovation Summit 2026",
                date = "Feb 15, 2026",
                venue = "Main Auditorium",
                category = EventCategory.ACADEMIC,
                description = "Annual technology summit.",
                reactionCounts = mapOf(ReactionType.LIKE to 45, ReactionType.LOVE to 12)
            ),
            Event(
                id = "2",
                title = "Annual Sports Festival",
                date = "Feb 20, 2026",
                venue = "Sports Complex",
                category = EventCategory.SPORTS,
                description = "Inter-department sports.",
                reactionCounts = mapOf(ReactionType.LIKE to 89, ReactionType.WOW to 5)
            ),
            Event(
                id = "3",
                title = "Cultural Night 2026",
                date = "Feb 25, 2026",
                venue = "Open Theatre",
                category = EventCategory.CULTURAL,
                description = "Celebrate diversity.",
                reactionCounts = mapOf(ReactionType.LOVE to 124, ReactionType.WOW to 18)
            )
        )
    )

    override fun observeEvents(): Flow<List<Event>> = events

    override fun observeEvent(eventId: String): Flow<Event?> =
        events.map { list -> list.find { it.id == eventId } }

    override suspend fun upsert(event: Event) {
        mutex.withLock {
            val list = events.value.toMutableList()
            val idx = list.indexOfFirst { it.id == event.id && event.id.isNotEmpty() }
            if (idx >= 0) list[idx] = event
            else {
                val newId = UUID.randomUUID().toString()
                list.add(event.copy(id = newId))
            }
            events.value = list
        }
    }

    override suspend fun delete(eventId: String) {
        mutex.withLock {
            events.value = events.value.filterNot { it.id == eventId }
        }
    }

    override suspend fun reactToEvent(eventId: String, reactionType: ReactionType?) {
        mutex.withLock {
            val list = events.value.toMutableList()
            val index = list.indexOfFirst { it.id == eventId }
            if (index != -1) {
                val oldEvent = list[index]
                val oldReaction = oldEvent.userReaction
                
                var newCounts = oldEvent.reactionCounts.toMutableMap()
                
                // Remove old reaction if exists
                if (oldReaction != null) {
                    val count = newCounts[oldReaction] ?: 0
                    if (count > 0) newCounts[oldReaction] = count - 1
                }
                
                // Add new reaction if exists
                if (reactionType != null) {
                    newCounts[reactionType] = (newCounts[reactionType] ?: 0) + 1
                }
                
                list[index] = oldEvent.copy(
                    userReaction = reactionType,
                    reactionCounts = newCounts
                )
                events.value = list
            }
        }
    }

    override suspend fun sync() {
        // No-op for fake repository
    }
}
