package com.campusconnectplus.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.campusconnectplus.data.local.dao.AnnouncementDao
import com.campusconnectplus.data.local.dao.EventDao
import com.campusconnectplus.data.local.dao.FavoriteDao
import com.campusconnectplus.data.local.dao.MediaDao
import com.campusconnectplus.data.local.dao.UserDao
import com.campusconnectplus.data.local.entity.AnnouncementEntity
import com.campusconnectplus.data.local.entity.EventEntity
import com.campusconnectplus.data.local.entity.FavoriteEntity
import com.campusconnectplus.data.local.entity.MediaEntity
import com.campusconnectplus.data.local.entity.UserEntity

@Database(
    entities = [
        EventEntity::class,
        MediaEntity::class,
        AnnouncementEntity::class,
        UserEntity::class,
        FavoriteEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun mediaDao(): MediaDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
}
