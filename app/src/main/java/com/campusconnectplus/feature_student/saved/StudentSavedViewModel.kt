package com.campusconnectplus.feature_student.saved

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.FavoriteRepository
import com.campusconnectplus.data.repository.Media
import com.campusconnectplus.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentSavedViewModel @Inject constructor(
    private val eventRepo: EventRepository,
    private val mediaRepo: MediaRepository,
    private val favoriteRepo: FavoriteRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val savedEvents: StateFlow<List<com.campusconnectplus.data.repository.Event>> =
        combine(
            eventRepo.observeEvents(),
            favoriteRepo.observeFavoriteEventIds()
        ) { events, ids ->
            events.filter { it.id in ids }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val savedMedia: StateFlow<List<com.campusconnectplus.data.repository.Media>> =
        combine(
            mediaRepo.observeMedia(),
            favoriteRepo.observeFavoriteMediaIds()
        ) { media, ids ->
            media.filter { it.id in ids }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun removeEvent(eventId: String) {
        viewModelScope.launch { favoriteRepo.toggleEvent(eventId) }
    }

    fun removeMedia(mediaId: String) {
        viewModelScope.launch { favoriteRepo.toggleMedia(mediaId) }
    }

    fun downloadMedia(item: Media) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(item.url)
            val request = DownloadManager.Request(uri)
                .setTitle(item.title.ifBlank { item.fileName })
                .setDescription("Downloading media file")
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
