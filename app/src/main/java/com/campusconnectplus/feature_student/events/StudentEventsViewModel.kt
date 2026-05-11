package com.campusconnectplus.feature_student.events

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.core.ui.util.UiState
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.FavoriteRepository
import com.campusconnectplus.data.repository.Media
import com.campusconnectplus.data.repository.MediaRepository
import com.campusconnectplus.core.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentEventsViewModel @Inject constructor(
    private val eventRepo: EventRepository,
    private val favoriteRepo: FavoriteRepository,
    private val mediaRepo: MediaRepository,
    networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val eventsState: StateFlow<UiState<List<Event>>> = combine(
        eventRepo.observeEvents(),
        isOnline
    ) { events, _ ->
        if (events.isEmpty()) UiState.Loading else UiState.Success(events)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )

    val favoriteEventIds: StateFlow<Set<String>> =
        favoriteRepo.observeFavoriteEventIds()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun toggleFavorite(eventId: String) {
        viewModelScope.launch {
            val wasFavorite = favoriteEventIds.value.contains(eventId)
            favoriteRepo.toggleEvent(eventId)
            _snackbarMessage.value = if (wasFavorite) "Removed from saved" else "Saved to favorites"
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(600)
            _isRefreshing.value = false
        }
    }

    fun getMediaForEvent(eventId: String): kotlinx.coroutines.flow.Flow<List<Media>> {
        return mediaRepo.ofEvent(eventId)
    }

    fun downloadMedia(item: Media) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(item.url)
            val request = DownloadManager.Request(uri)
                .setTitle(item.title.ifBlank { item.fileName })
                .setDescription("Downloading event media")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, item.fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            downloadManager.enqueue(request)
            Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
