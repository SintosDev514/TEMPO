package com.campusconnectplus.feature_student.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.AnnouncementRepository
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.FavoriteRepository
import com.campusconnectplus.data.repository.MediaRepository
import com.campusconnectplus.core.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeStats(
    val eventsCount: Int = 0,
    val mediaCount: Int = 0,
    val savedCount: Int = 0,
    val announcementsCount: Int = 0
)

@HiltViewModel
class StudentHomeViewModel @Inject constructor(
    private val eventRepo: EventRepository,
    private val mediaRepo: MediaRepository,
    private val annRepo: AnnouncementRepository,
    private val favoriteRepo: FavoriteRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val events: StateFlow<List<Event>> = eventRepo.observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun getMediaForEvent(eventId: String) = mediaRepo.ofEvent(eventId)

    val homeStats: StateFlow<HomeStats> = combine(
        eventRepo.observeEvents(),
        mediaRepo.observeMedia(),
        annRepo.observeAnnouncements(),
        combine(
            favoriteRepo.observeFavoriteEventIds(),
            favoriteRepo.observeFavoriteMediaIds(),
            isOnline
        ) { eventFavIds, mediaFavIds, _ ->
            Pair(eventFavIds, mediaFavIds)
        }
    ) { events, media, anns, favs ->
        HomeStats(
            eventsCount = events.size,
            mediaCount = media.size,
            announcementsCount = anns.size,
            savedCount = favs.first.size + favs.second.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeStats())
}
