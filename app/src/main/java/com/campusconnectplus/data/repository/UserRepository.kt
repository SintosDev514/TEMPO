package com.campusconnectplus.data.repository

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUsers(): Flow<List<User>>
    suspend fun upsert(user: User)
    suspend fun delete(id: Long)
    suspend fun getUserByEmail(email: String): User?
    suspend fun updatePasswordHash(email: String, passwordHash: String)
    suspend fun setRole(userId: Long, role: UserRole)
    suspend fun setActive(userId: Long, active: Boolean)
}
