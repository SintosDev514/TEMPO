package com.campusconnectplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.campusconnectplus.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites WHERE type = :type ORDER BY id DESC")
    fun observeByType(type: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE type = :type AND refId = :refId LIMIT 1")
    suspend fun getOne(type: String, refId: Long): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE type = :type AND refId = :refId")
    suspend fun delete(type: String, refId: Long)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()
}
