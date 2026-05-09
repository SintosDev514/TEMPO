package com.campusconnectplus.feature_student.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.FavoriteRepository
import com.campusconnectplus.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentMediaViewModel @Inject constructor(
    private val mediaRepo: MediaRepository,
    private val favoriteRepo: FavoriteRepository
) : ViewModel() {

    val media = mediaRepo.observeMedia()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteMediaIds: StateFlow<Set<String>> =
        favoriteRepo.observeFavoriteMediaIds()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleFavorite(mediaId: String) {
        viewModelScope.launch { favoriteRepo.toggleMedia(mediaId) }
    }
}
