package com.campusconnectplus.core.di

import com.campusconnectplus.data.local.dao.*
import com.campusconnectplus.data.local.repository.*
import com.campusconnectplus.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideEventRepository(dao: EventDao): EventRepository = RoomEventRepository(dao)

    @Provides
    @Singleton
    fun provideMediaRepository(dao: MediaDao): MediaRepository = RoomMediaRepository(dao)

    @Provides
    @Singleton
    fun provideAnnouncementRepository(dao: AnnouncementDao): AnnouncementRepository = RoomAnnouncementRepository(dao)

    @Provides
    @Singleton
    fun provideUserRepository(dao: UserDao): UserRepository = RoomUserRepository(dao)

    @Provides
    @Singleton
    fun provideFavoriteRepository(dao: FavoriteDao): FavoriteRepository = RoomFavoriteRepository(dao)

    @Provides
    @Singleton
    fun provideAuthRepository(userDao: UserDao, userRepository: UserRepository): AuthRepository = 
        RoomAuthRepository(userDao, userRepository)
}
