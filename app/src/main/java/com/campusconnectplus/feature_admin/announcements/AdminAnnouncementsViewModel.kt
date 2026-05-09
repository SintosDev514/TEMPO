package com.campusconnectplus.feature_admin.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.Announcement
import com.campusconnectplus.data.repository.AnnouncementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminAnnouncementsViewModel @Inject constructor(
    private val repo: AnnouncementRepository
) : ViewModel() {

    private val _snackbarMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    val announcements: StateFlow<List<Announcement>> =
        repo.observeAnnouncements().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun upsert(a: Announcement) {
        viewModelScope.launch {
            try {
                repo.upsert(a)
                _snackbarMessage.value = "Announcement saved successfully"
            } catch (e: Exception) {
                _snackbarMessage.value = "Error: ${e.localizedMessage ?: "Failed to save"}"
                e.printStackTrace()
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try {
                repo.delete(id)
                _snackbarMessage.value = "Announcement deleted"
            } catch (e: Exception) {
                _snackbarMessage.value = "Error: ${e.localizedMessage}"
            }
        }
    }
}
