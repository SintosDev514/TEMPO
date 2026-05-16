package com.campusconnectplus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val date: String,
    val venue: String,
    val description: String,
    val category: String,
    val imageUrl: String? = null,
    val reactionCounts: String = "{}", // JSON Map
    val userReaction: String? = null,
    val updatedAt: Long
)
