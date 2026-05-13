package com.campusconnectplus.feature_admin.media

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.Media
import com.campusconnectplus.data.repository.MediaRepository
import com.campusconnectplus.data.repository.MediaType
import com.campusconnectplus.core.util.FileCache
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AdminMediaViewModel @Inject constructor(
    private val repo: MediaRepository,
    private val eventRepo: com.campusconnectplus.data.repository.EventRepository,
    private val fileCache: FileCache,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val eventId = MutableStateFlow<String?>(null)

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbarMessage() { _snackbarMessage.value = null }

    val events = eventRepo.observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val media: StateFlow<List<Media>> =
        eventId.flatMapLatest { id ->
            if (id == null) repo.observeMedia()
            else repo.ofEvent(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalSizeMb: Int get() = media.value.sumOf { it.sizeMb }

    fun setEvent(eventId: String?) { this.eventId.value = eventId }

    fun uploadMedia(uri: Uri, title: String, type: MediaType, targetEventId: String? = null) {
        viewModelScope.launch {
            try {
                _snackbarMessage.value = "Saving locally..."
                
                // Cache the file locally first
                val cachedFile = fileCache.cacheFile(uri)
                val fileName = cachedFile.name
                val bytes = cachedFile.readBytes()

                val newMedia = Media(
                    id = UUID.randomUUID().toString(), // Temp ID for local
                    eventId = targetEventId ?: eventId.value ?: "",
                    url = "", // Will be updated after remote upload
                    type = type,
                    title = title,
                    fileName = fileName,
                    date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
                    sizeMb = (bytes.size / (1024.0 * 1024.0)).toInt().coerceAtLeast(1),
                    localPath = cachedFile.absolutePath
                )
                
                // Save to local DB via repo (which should handle local first)
                repo.upsert(newMedia)
                _snackbarMessage.value = "Media saved locally. Uploading..."

                // Remote upload
                val publicUrl = repo.uploadFile("media", fileName, bytes)
                
                // Update with public URL
                repo.upsert(newMedia.copy(url = publicUrl))
                
                _snackbarMessage.value = "Media uploaded successfully"
            } catch (e: Exception) {
                _snackbarMessage.value = "Upload failed: ${e.localizedMessage}. It will retry when online."
                e.printStackTrace()
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }

    fun updateMediaEvent(media: Media, newEventId: String?) {
        viewModelScope.launch {
            try {
                val updated = media.copy(eventId = newEventId ?: "")
                repo.upsert(updated)
                _snackbarMessage.value = "Link updated"
            } catch (e: Exception) {
                _snackbarMessage.value = "Update failed: ${e.localizedMessage}"
            }
        }
    }
}
