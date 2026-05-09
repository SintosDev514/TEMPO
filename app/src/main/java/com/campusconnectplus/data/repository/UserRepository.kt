package com.campusconnectplus.data.repository

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUsers(): Flow<List<User>>
    suspend fun upsert(user: User)
    suspend fun delete(id: String)
    suspend fun getUserByEmail(email: String): User?
    suspend fun updatePasswordHash(email: String, passwordHash: String)
    suspend fun setRole(userId: String, role: UserRole)
    suspend fun setActive(userId: String, active: Boolean)
}
