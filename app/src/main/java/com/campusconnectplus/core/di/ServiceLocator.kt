package com.campusconnectplus.core.di

import android.content.Context
import com.campusconnectplus.data.local.db.DatabaseProvider
import com.campusconnectplus.data.local.repository.RoomAnnouncementRepository
import com.campusconnectplus.data.local.repository.RoomAuthRepository
import com.campusconnectplus.data.local.repository.RoomEventRepository
import com.campusconnectplus.data.local.repository.RoomFavoriteRepository
import com.campusconnectplus.data.local.repository.RoomMediaRepository
import com.campusconnectplus.data.local.repository.RoomUserRepository
import com.campusconnectplus.data.repository.AnnouncementRepository
import com.campusconnectplus.data.repository.AuthRepository
import com.campusconnectplus.data.repository.EventRepository
import com.campusconnectplus.data.repository.FavoriteRepository
import com.campusconnectplus.data.repository.MediaRepository
import com.campusconnectplus.data.repository.UserRepository

object ServiceLocator {

    fun provideContainer(context: Context): AppContainer {
        val db = DatabaseProvider.get(context)

        val eventRepo: EventRepository = RoomEventRepository(db.eventDao())
        val mediaRepo: MediaRepository = RoomMediaRepository(db.mediaDao())
        val annRepo: AnnouncementRepository = RoomAnnouncementRepository(db.announcementDao())
        val userRepo: UserRepository = RoomUserRepository(db.userDao())
        val favRepo: FavoriteRepository = RoomFavoriteRepository(db.favoriteDao())
        val authRepo: AuthRepository = RoomAuthRepository(db.userDao(), userRepo)

        return object : AppContainer {
            override val eventRepository = eventRepo
            override val mediaRepository = mediaRepo
            override val announcementRepository = annRepo
            override val userRepository = userRepo
            override val favoriteRepository = favRepo
            override val authRepository = authRepo
        }
    }
}
