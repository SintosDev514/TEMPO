package com.campusconnectplus.feature_student.events

import com.campusconnectplus.core.ui.util.UiState
import com.campusconnectplus.data.fake.FakeEventRepository
import com.campusconnectplus.data.fake.FakeFavoriteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudentEventsViewModelTest {

    private lateinit var eventRepo: FakeEventRepository
    private lateinit var favoriteRepo: FakeFavoriteRepository
    private lateinit var viewModel: StudentEventsViewModel

    @Before
    fun setup() {
        eventRepo = FakeEventRepository()
        favoriteRepo = FakeFavoriteRepository()
        viewModel = StudentEventsViewModel(eventRepo, favoriteRepo)
    }

    @Test
    fun initialEventsState_isLoading() = runTest {
        val first = viewModel.eventsState.first()
        assertTrue(first is UiState.Loading)
    }

    @Test
    fun favoriteEventIds_startsEmpty() = runTest {
        val ids = viewModel.favoriteEventIds.first()
        assertTrue(ids.isEmpty())
    }

    @Test
    fun toggleFavorite_addsThenRemovesFromFavorites() = runTest {
        viewModel.toggleFavorite(1L)
        var ids = viewModel.favoriteEventIds.first()
        assertTrue(ids.contains(1L))

        viewModel.toggleFavorite(1L)
        ids = viewModel.favoriteEventIds.first()
        assertTrue(ids.isEmpty())
    }

    @Test
    fun snackbarMessage_shownAfterToggleFavorite() = runTest {
        viewModel.toggleFavorite(1L)
        val msg = viewModel.snackbarMessage.first()
        assertEquals("Saved to favorites", msg)

        viewModel.clearSnackbarMessage()
        viewModel.toggleFavorite(1L)
        val msg2 = viewModel.snackbarMessage.first()
        assertEquals("Removed from saved", msg2)
    }
}
