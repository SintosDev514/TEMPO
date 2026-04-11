package com.campusconnectplus.core.di

import android.content.Context
import androidx.room.Room
import com.campusconnectplus.core.security.PasswordHasher
import com.campusconnectplus.core.util.Constants
import com.campusconnectplus.data.local.dao.AnnouncementDao
import com.campusconnectplus.data.local.dao.EventDao
import com.campusconnectplus.data.local.dao.FavoriteDao
import com.campusconnectplus.data.local.dao.MediaDao
import com.campusconnectplus.data.local.dao.UserDao
import com.campusconnectplus.data.local.db.AppDatabase
import com.campusconnectplus.data.local.entity.UserEntity
import com.campusconnectplus.data.repository.UserRole
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        userDaoProvider: Provider<UserDao>
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "campus_connect_db"
        )
        .addCallback(object : androidx.room.RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    seedDatabase(userDaoProvider.get())
                }
            }
        })
        .fallbackToDestructiveMigration()
        .build()
    }

    private suspend fun seedDatabase(userDao: UserDao) {
        val adminEmail = Constants.DEFAULT_ADMIN_EMAIL
        if (userDao.getByEmail(adminEmail) == null) {
            val now = System.currentTimeMillis()
            userDao.upsert(
                UserEntity(
                    id = now.toString(),
                    name = "System Administrator",
                    email = adminEmail,
                    role = UserRole.ADMIN.name,
                    active = true,
                    updatedAt = now,
                    passwordHash = PasswordHasher.hash("admin123")
                )
            )
        }
    }

    @Provides
    fun provideEventDao(database: AppDatabase): EventDao = database.eventDao()

    @Provides
    fun provideMediaDao(database: AppDatabase): MediaDao = database.mediaDao()

    @Provides
    fun provideAnnouncementDao(database: AppDatabase): AnnouncementDao = database.announcementDao()

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()
}
