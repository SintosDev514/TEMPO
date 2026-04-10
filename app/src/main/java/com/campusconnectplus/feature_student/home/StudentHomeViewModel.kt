package com.campusconnectplus.feature_student.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.FavoriteRepository
import com.campusconnectplus.data.repository.MediaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeStats(
    val eventsCount: Int = 0,
    val mediaCount: Int = 0,
    val savedCount: Int = 0
)

class StudentHomeViewModel(
    private val eventRepo: EventRepository,
    private val mediaRepo: MediaRepository,
    private val favoriteRepo: FavoriteRepository
) : ViewModel() {

    val homeStats: StateFlow<HomeStats> = combine(
        eventRepo.observeEvents(),
        mediaRepo.observeMedia(),
        favoriteRepo.observeFavoriteEventIds(),
        favoriteRepo.observeFavoriteMediaIds()
    ) { events, media, eventFavIds, mediaFavIds ->
        HomeStats(
            eventsCount = events.size,
            mediaCount = media.size,
            savedCount = eventFavIds.size + mediaFavIds.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeStats())
}
