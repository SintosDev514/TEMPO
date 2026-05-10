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

    @Upsert
    suspend fun upsertAll(entities: List<MediaEntity>)

    @Query("DELETE FROM media WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM media WHERE id NOT IN (:ids)")
    suspend fun deleteExcept(ids: List<String>)

    @Transaction
    suspend fun sync(entities: List<MediaEntity>) {
        upsertAll(entities)
        val ids = entities.map { it.id }
        if (ids.isEmpty()) {
            deleteAll()
        } else {
            deleteExcept(ids)
        }
    }

    @Query("DELETE FROM media")
    suspend fun deleteAll()

    @Transaction
    suspend fun syncForEvent(eventId: String, entities: List<MediaEntity>) {
        upsertAll(entities)
        val ids = entities.map { it.id }
        if (ids.isEmpty()) {
            deleteAllForEvent(eventId)
        } else {
            deleteExceptForEvent(eventId, ids)
        }
    }

    @Query("DELETE FROM media WHERE eventId = :eventId")
    suspend fun deleteAllForEvent(eventId: String)

    @Query("DELETE FROM media WHERE eventId = :eventId AND id NOT IN (:ids)")
    suspend fun deleteExceptForEvent(eventId: String, ids: List<String>)
}
