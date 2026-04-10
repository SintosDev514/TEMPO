package com.campusconnectplus.feature_student.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.FavoriteRepository
import com.campusconnectplus.data.repository.MediaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentMediaViewModel(
    private val mediaRepo: MediaRepository,
    private val favoriteRepo: FavoriteRepository
) : ViewModel() {

    val media = mediaRepo.observeMedia()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteMediaIds: StateFlow<Set<Long>> =
        favoriteRepo.observeFavoriteMediaIds()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleFavorite(mediaId: Long) {
        viewModelScope.launch { favoriteRepo.toggleMedia(mediaId) }
    }
}
