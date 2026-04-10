package com.campusconnectplus.data.local.dao

import androidx.room.*
import com.campusconnectplus.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<EventEntity?>

    @Upsert
    suspend fun upsert(entity: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun delete(id: String)
}
