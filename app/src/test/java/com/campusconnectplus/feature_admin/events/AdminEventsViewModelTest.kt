package com.campusconnectplus.feature_admin.events

import com.campusconnectplus.data.fake.FakeEventRepository
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminEventsViewModelTest {

    private lateinit var repo: FakeEventRepository
    private lateinit var viewModel: AdminEventsViewModel

    @Before
    fun setup() {
        repo = FakeEventRepository()
        viewModel = AdminEventsViewModel(repo)
    }

    @Test
    fun events_emitsFromRepository() = runTest {
        val events = viewModel.events.first()
        assertTrue(events.size >= 3)
        assertTrue(events.any { it.title == "Tech Innovation Summit 2026" })
    }

    @Test
    fun delete_eventRemovedAndSnackbarShown() = runTest {
        val eventsBefore = viewModel.events.first()
        val firstId = eventsBefore.first().id

        viewModel.delete(firstId.toString())

        val eventsAfter = viewModel.events.first()
        assertEquals(eventsBefore.size - 1, eventsAfter.size)
        assertTrue(eventsAfter.none { it.id == firstId })
        assertEquals("Event deleted", viewModel.snackbarMessage.first())
    }

    @Test
    fun upsert_newEvent_snackbarSaysCreated() = runTest {
        viewModel.upsert(
            Event(
                title = "New Event",
                date = "Mar 10, 2026",
                venue = "Room A",
                description = "Desc",
                category = EventCategory.ACADEMIC
            )
        )
        assertEquals("Event created", viewModel.snackbarMessage.first())
        val events = viewModel.events.first()
        assertTrue(events.any { it.title == "New Event" })
    }

    @Test
    fun upsert_existingEvent_snackbarSaysUpdated() = runTest {
        val events = viewModel.events.first()
        val existing = events.first().copy(title = "Updated Title")

        viewModel.upsert(existing)

        assertEquals("Event updated", viewModel.snackbarMessage.first())
        val after = viewModel.events.first()
        assertTrue(after.any { it.title == "Updated Title" })
    }
}
