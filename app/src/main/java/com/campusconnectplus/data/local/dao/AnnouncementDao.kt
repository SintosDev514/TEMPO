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

    @Upsert
    suspend fun upsertAll(entities: List<AnnouncementEntity>)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM announcements WHERE id NOT IN (:ids)")
    suspend fun deleteExcept(ids: List<String>)

    @Transaction
    suspend fun sync(entities: List<AnnouncementEntity>) {
        upsertAll(entities)
        val ids = entities.map { it.id }
        if (ids.isEmpty()) {
            deleteAll()
        } else {
            deleteExcept(ids)
        }
    }

    @Query("DELETE FROM announcements")
    suspend fun deleteAll()
}
