package com.campusconnectplus.feature_admin.media

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.Media
import com.campusconnectplus.data.repository.MediaRepository
import com.campusconnectplus.data.repository.MediaType
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
                _snackbarMessage.value = "Uploading file..."
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch
                val fileName = "${UUID.randomUUID()}_${uri.lastPathSegment ?: "file"}"
                
                val publicUrl = repo.uploadFile("media", fileName, bytes)

                val newMedia = Media(
                    id = "", // Let Supabase generate UUID
                    eventId = targetEventId ?: eventId.value ?: "",
                    url = publicUrl,
                    type = type,
                    title = title,
                    fileName = fileName,
                    date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
                    sizeMb = (bytes.size / (1024.0 * 1024.0)).toInt().coerceAtLeast(1)
                )
                repo.upsert(newMedia)
                _snackbarMessage.value = "Media uploaded successfully"
            } catch (e: Exception) {
                _snackbarMessage.value = "Upload failed: ${e.localizedMessage}"
                e.printStackTrace()
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }
}
