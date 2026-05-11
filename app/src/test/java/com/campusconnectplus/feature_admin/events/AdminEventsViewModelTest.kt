package com.campusconnectplus.feature_admin.events

import com.campusconnectplus.MainDispatcherRule
import com.campusconnectplus.data.fake.FakeEventRepository
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminEventsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: FakeEventRepository
    private lateinit var viewModel: AdminEventsViewModel

    @Before
    fun setup() {
        repo = FakeEventRepository()
        viewModel = AdminEventsViewModel(repo)
    }

    @Test
    fun events_emitsFromRepository() = runTest {
        val job = viewModel.events.launchIn(this)
        
        // Wait for flow to emit
        testScheduler.advanceUntilIdle()
        
        val events = viewModel.events.value
        assertTrue("Expected at least 3 events, found ${events.size}", events.size >= 3)
        assertTrue(events.any { it.title == "Tech Innovation Summit 2026" })
        
        job.cancel()
    }

    @Test
    fun delete_eventRemovedAndSnackbarShown() = runTest {
        val job = viewModel.events.launchIn(this)
        val snackJob = viewModel.snackbarMessage.launchIn(this)
        
        testScheduler.advanceUntilIdle()
        
        val eventsBefore = viewModel.events.value
        val firstId = eventsBefore.first().id

        viewModel.delete(firstId)
        testScheduler.advanceUntilIdle()

        val eventsAfter = viewModel.events.value
        assertEquals(eventsBefore.size - 1, eventsAfter.size)
        assertTrue(eventsAfter.none { it.id == firstId })
        assertEquals("Event deleted", viewModel.snackbarMessage.value)
        
        job.cancel()
        snackJob.cancel()
    }

    @Test
    fun upsert_newEvent_snackbarSaysCreated() = runTest {
        val job = viewModel.events.launchIn(this)
        val snackJob = viewModel.snackbarMessage.launchIn(this)
        
        testScheduler.advanceUntilIdle()
        
        viewModel.upsert(
            Event(
                id = "",
                title = "New Event",
                date = "Mar 10, 2026",
                venue = "Room A",
                description = "Desc",
                category = EventCategory.ACADEMIC
            )
        )
        testScheduler.advanceUntilIdle()

        assertEquals("Event created successfully", viewModel.snackbarMessage.value)
        val events = viewModel.events.value
        assertTrue(events.any { it.title == "New Event" })
        
        job.cancel()
        snackJob.cancel()
    }

    @Test
    fun upsert_existingEvent_snackbarSaysUpdated() = runTest {
        val job = viewModel.events.launchIn(this)
        val snackJob = viewModel.snackbarMessage.launchIn(this)
        
        testScheduler.advanceUntilIdle()
        
        val events = viewModel.events.value
        val existing = events.first().copy(title = "Updated Title")

        viewModel.upsert(existing)
        testScheduler.advanceUntilIdle()

        assertEquals("Event updated successfully", viewModel.snackbarMessage.value)
        val after = viewModel.events.value
        assertTrue(after.any { it.title == "Updated Title" })
        
        job.cancel()
        snackJob.cancel()
    }
}
