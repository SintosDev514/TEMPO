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

    @Upsert
    suspend fun upsertAll(entities: List<EventEntity>)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM events WHERE id NOT IN (:ids)")
    suspend fun deleteExcept(ids: List<String>)

    @Transaction
    suspend fun sync(entities: List<EventEntity>) {
        upsertAll(entities)
        val ids = entities.map { it.id }
        if (ids.isEmpty()) {
            deleteAll()
        } else {
            deleteExcept(ids)
        }
    }

    @Query("DELETE FROM events")
    suspend fun deleteAll()
}
