package com.campusconnectplus.feature_student.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.AnnouncementRepository
import com.campusconnectplus.data.repository.AnnouncementStatus
import com.campusconnectplus.core.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentAnnouncementsViewModel @Inject constructor(
    private val repo: AnnouncementRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val announcements: StateFlow<List<com.campusconnectplus.data.repository.Announcement>> =
        repo.observeAnnouncements()
            .map { list -> list.filter { it.status == AnnouncementStatus.ACTIVE } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repo.sync()
            } catch (e: Exception) {
                // Ignore
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
