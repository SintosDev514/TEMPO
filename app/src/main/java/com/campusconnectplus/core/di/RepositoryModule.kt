package com.campusconnectplus.core.di

import com.campusconnectplus.data.remote.repository.*
import com.campusconnectplus.data.repository.*
import com.campusconnectplus.data.repository.OfflineFirstAnnouncementRepository
import com.campusconnectplus.data.repository.OfflineFirstEventRepository
import com.campusconnectplus.data.repository.OfflineFirstMediaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideEventRepository(
        postgrest: Postgrest,
        realtime: Realtime,
        auth: Auth,
        dao: com.campusconnectplus.data.local.dao.EventDao
    ): EventRepository {
        val remote = SupabaseEventRepository(postgrest, realtime, auth)
        return OfflineFirstEventRepository(remote, dao)
    }

    @Provides
    @Singleton
    fun provideMediaRepository(
        postgrest: Postgrest,
        realtime: Realtime,
        storage: Storage,
        auth: Auth,
        dao: com.campusconnectplus.data.local.dao.MediaDao
    ): MediaRepository {
        val remote = SupabaseMediaRepository(postgrest, realtime, storage, auth)
        return OfflineFirstMediaRepository(remote, dao)
    }

    @Provides
    @Singleton
    fun provideAnnouncementRepository(
        postgrest: Postgrest,
        realtime: Realtime,
        dao: com.campusconnectplus.data.local.dao.AnnouncementDao
    ): AnnouncementRepository {
        val remote = SupabaseAnnouncementRepository(postgrest, realtime)
        return OfflineFirstAnnouncementRepository(remote, dao)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        postgrest: Postgrest,
        realtime: Realtime
    ): UserRepository = SupabaseUserRepository(postgrest, realtime)

    @Provides
    @Singleton
    fun provideFavoriteRepository(dao: com.campusconnectplus.data.local.dao.FavoriteDao): FavoriteRepository = 
        com.campusconnectplus.data.local.repository.RoomFavoriteRepository(dao)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: Auth,
        postgrest: Postgrest
    ): AuthRepository = SupabaseAuthRepository(auth, postgrest)
}
