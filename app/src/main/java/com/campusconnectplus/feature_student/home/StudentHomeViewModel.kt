package com.campusconnectplus.feature_student.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.AnnouncementRepository
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.FavoriteRepository
import com.campusconnectplus.data.repository.MediaRepository
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
    private val favoriteRepo: FavoriteRepository
) : ViewModel() {

    val events: StateFlow<List<Event>> = eventRepo.observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val homeStats: StateFlow<HomeStats> = combine(
        eventRepo.observeEvents(),
        mediaRepo.observeMedia(),
        annRepo.observeAnnouncements(),
        favoriteRepo.observeFavoriteEventIds(),
        favoriteRepo.observeFavoriteMediaIds()
    ) { events, media, anns, eventFavIds, mediaFavIds ->
        HomeStats(
            eventsCount = events.size,
            mediaCount = media.size,
            announcementsCount = anns.size,
            savedCount = eventFavIds.size + mediaFavIds.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeStats())
}
