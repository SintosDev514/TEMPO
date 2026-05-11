package com.campusconnectplus.feature_student.events

import android.content.Context
import com.campusconnectplus.MainDispatcherRule
import com.campusconnectplus.core.ui.util.UiState
import com.campusconnectplus.data.fake.FakeEventRepository
import com.campusconnectplus.data.repository.FavoriteRepository
import com.campusconnectplus.data.repository.MediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudentEventsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var eventRepo: FakeEventRepository
    private lateinit var favoriteRepo: FavoriteRepository
    private lateinit var mediaRepo: MediaRepository
    private lateinit var context: Context
    private lateinit var viewModel: StudentEventsViewModel

    @Before
    fun setup() {
        eventRepo = FakeEventRepository()
        
        favoriteRepo = object : FavoriteRepository {
            private val eventIds = MutableStateFlow<Set<String>>(emptySet())
            private val mediaIds = MutableStateFlow<Set<String>>(emptySet())
            
            override fun observeFavoriteEventIds(): Flow<Set<String>> = eventIds
            override fun observeFavoriteMediaIds(): Flow<Set<String>> = mediaIds

            override suspend fun isEventFavorite(eventId: String): Boolean = eventIds.value.contains(eventId)
            override suspend fun isMediaFavorite(mediaId: String): Boolean = mediaIds.value.contains(mediaId)

            override suspend fun toggleEvent(eventId: String) {
                if (eventIds.value.contains(eventId)) eventIds.value -= eventId
                else eventIds.value += eventId
            }
            override suspend fun toggleMedia(mediaId: String) {
                if (mediaIds.value.contains(mediaId)) mediaIds.value -= mediaId
                else mediaIds.value += mediaId
            }
            override suspend fun clearAll() {
                eventIds.value = emptySet()
                mediaIds.value = emptySet()
            }
        }
        
        mediaRepo = object : MediaRepository {
            override fun observeMedia(): Flow<List<com.campusconnectplus.data.repository.Media>> = flowOf(emptyList())
            override fun ofEvent(eventId: String): Flow<List<com.campusconnectplus.data.repository.Media>> = flowOf(emptyList())
            override suspend fun upsert(media: com.campusconnectplus.data.repository.Media) {}
            override suspend fun delete(mediaId: String) {}
            override suspend fun uploadFile(bucket: String, path: String, byteArray: ByteArray): String = ""
        }
        
        context = object : android.content.ContextWrapper(null) {
            override fun getSystemService(name: String): Any? = null
        }

        viewModel = StudentEventsViewModel(
            eventRepo = eventRepo,
            favoriteRepo = favoriteRepo,
            mediaRepo = mediaRepo,
            context = context
        )
    }

    @Test
    fun eventsState_initiallyLoading_thenSuccess() = runTest {
        val job = viewModel.eventsState.launchIn(this)
        
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.eventsState.value
        assertTrue("State was $state", state is UiState.Success)
        assertEquals(3, (state as UiState.Success).data.size)
        
        job.cancel()
    }

    @Test
    fun toggleFavorite_updatesSnackbar() = runTest {
        val job = viewModel.snackbarMessage.launchIn(this)
        val eventJob = viewModel.favoriteEventIds.launchIn(this)
        val eventId = "1"
        
        testScheduler.advanceUntilIdle()
        
        viewModel.toggleFavorite(eventId)
        testScheduler.advanceUntilIdle()
        assertEquals("Saved to favorites", viewModel.snackbarMessage.value)
        
        viewModel.toggleFavorite(eventId)
        testScheduler.advanceUntilIdle()
        assertEquals("Removed from saved", viewModel.snackbarMessage.value)
        
        job.cancel()
        eventJob.cancel()
    }
}
