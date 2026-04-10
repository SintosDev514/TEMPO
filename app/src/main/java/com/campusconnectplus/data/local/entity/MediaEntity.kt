package com.campusconnectplus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val url: String,
    val type: String,
    val title: String?,
    val description: String?,
    val updatedAt: Long
)
