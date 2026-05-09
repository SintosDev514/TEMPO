package com.campusconnectplus.core.di

import com.campusconnectplus.data.remote.repository.*
import com.campusconnectplus.data.repository.*
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
        realtime: Realtime
    ): EventRepository = SupabaseEventRepository(postgrest, realtime)

    @Provides
    @Singleton
    fun provideMediaRepository(
        postgrest: Postgrest,
        realtime: Realtime,
        storage: Storage
    ): MediaRepository = SupabaseMediaRepository(postgrest, realtime, storage)

    @Provides
    @Singleton
    fun provideAnnouncementRepository(
        postgrest: Postgrest,
        realtime: Realtime
    ): AnnouncementRepository = SupabaseAnnouncementRepository(postgrest, realtime)

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
