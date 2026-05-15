package com.campusconnectplus.feature_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val eventRepo: EventRepository,
    private val mediaRepo: MediaRepository,
    private val annRepo: AnnouncementRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    data class DashboardStats(
        val totalEvents: Int = 0,
        val totalMedia: Int = 0,
        val totalAnnouncements: Int = 0,
        val totalUsers: Int = 0,
        val activeUsers: Int = 0,
        val totalReactions: Int = 0
    )

    val stats: StateFlow<DashboardStats> = combine(
        eventRepo.observeEvents(),
        mediaRepo.observeMedia(),
        annRepo.observeAnnouncements(),
        userRepo.observeUsers()
    ) { events, media, anns, users ->
        DashboardStats(
            totalEvents = events.size,
            totalMedia = media.size,
            totalAnnouncements = anns.size,
            totalUsers = users.size,
            activeUsers = users.count { it.active },
            totalReactions = events.sumOf { it.reactionCounts.values.sum() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())
}
