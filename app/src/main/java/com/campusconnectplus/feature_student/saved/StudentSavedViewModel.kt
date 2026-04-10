package com.campusconnectplus.feature_student.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.FavoriteRepository
import com.campusconnectplus.data.repository.MediaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentSavedViewModel(
    private val eventRepo: EventRepository,
    private val mediaRepo: MediaRepository,
    private val favoriteRepo: FavoriteRepository
) : ViewModel() {

    val savedEvents: StateFlow<List<com.campusconnectplus.data.repository.Event>> =
        combine(
            eventRepo.observeEvents(),
            favoriteRepo.observeFavoriteEventIds()
        ) { events, ids ->
            events.filter { it.id in ids }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val savedMedia: StateFlow<List<com.campusconnectplus.data.repository.Media>> =
        combine(
            mediaRepo.observeMedia(),
            favoriteRepo.observeFavoriteMediaIds()
        ) { media, ids ->
            media.filter { it.id in ids }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun removeEvent(eventId: Long) {
        viewModelScope.launch { favoriteRepo.toggleEvent(eventId) }
    }

    fun removeMedia(mediaId: Long) {
        viewModelScope.launch { favoriteRepo.toggleMedia(mediaId) }
    }
}
