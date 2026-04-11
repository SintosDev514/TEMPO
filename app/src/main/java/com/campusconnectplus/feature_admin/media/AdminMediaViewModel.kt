package com.campusconnectplus.feature_admin.media

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.Media
import com.campusconnectplus.data.repository.MediaRepository
import com.campusconnectplus.data.repository.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AdminMediaViewModel @Inject constructor(
    private val repo: MediaRepository
) : ViewModel() {

    private val eventId = MutableStateFlow<String?>(null)

    val media: StateFlow<List<Media>> =
        eventId.flatMapLatest { id ->
            if (id == null) repo.observeMedia()
            else repo.ofEvent(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalSizeMb: Int get() = media.value.sumOf { it.sizeMb }

    fun setEvent(eventId: String?) { this.eventId.value = eventId }

    fun upsert(media: Media) {
        viewModelScope.launch { repo.upsert(media) }
    }

    fun uploadMedia(uri: Uri, title: String, type: MediaType) {
        viewModelScope.launch {
            // In a real-world app, you'd copy the file to internal storage here.
            // For now, we'll store the URI string so it works locally.
            val newMedia = Media(
                id = System.currentTimeMillis(),
                eventId = 0L,
                url = uri.toString(),
                type = type,
                title = title,
                fileName = uri.path?.substringAfterLast('/') ?: "unknown",
                date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
                sizeMb = (1..50).random() // Mock size for now
            )
            repo.upsert(newMedia)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }
}
