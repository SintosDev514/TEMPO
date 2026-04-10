package com.campusconnectplus.feature_admin.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.Announcement
import com.campusconnectplus.data.repository.AnnouncementRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminAnnouncementsViewModel(
    private val repo: AnnouncementRepository
) : ViewModel() {

    val announcements: StateFlow<List<Announcement>> =
        repo.observeAnnouncements().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun upsert(a: Announcement) {
        viewModelScope.launch { repo.upsert(a) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }
}
