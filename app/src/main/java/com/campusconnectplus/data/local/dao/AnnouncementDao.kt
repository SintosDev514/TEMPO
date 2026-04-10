package com.campusconnectplus.data.local.dao

import androidx.room.*
import com.campusconnectplus.data.local.entity.AnnouncementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<AnnouncementEntity>>

    @Upsert
    suspend fun upsert(entity: AnnouncementEntity)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun delete(id: String)
}
