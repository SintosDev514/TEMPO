package com.campusconnectplus.data.local.dao

import androidx.room.*
import com.campusconnectplus.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE eventId = :eventId ORDER BY updatedAt DESC")
    fun observeForEvent(eventId: String): Flow<List<MediaEntity>>

    @Upsert
    suspend fun upsert(entity: MediaEntity)

    @Query("DELETE FROM media WHERE id = :id")
    suspend fun delete(id: String)
}
