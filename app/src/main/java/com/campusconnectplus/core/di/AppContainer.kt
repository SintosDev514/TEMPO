package com.campusconnectplus.core.di

import com.campusconnectplus.data.repository.AnnouncementRepository
import com.campusconnectplus.data.repository.AuthRepository
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.FavoriteRepository
import com.campusconnectplus.data.repository.MediaRepository
import com.campusconnectplus.data.repository.UserRepository

interface AppContainer {
    val eventRepository: EventRepository
    val mediaRepository: MediaRepository
    val announcementRepository: AnnouncementRepository
    val userRepository: UserRepository
    val favoriteRepository: FavoriteRepository
    val authRepository: AuthRepository
}
