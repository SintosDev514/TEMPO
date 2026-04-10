package com.campusconnectplus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String,
    val active: Boolean = true,
    val updatedAt: Long,
    /** Hashed password (SHA-256 + salt); null for legacy or non-admin users. */
    val passwordHash: String? = null
)
