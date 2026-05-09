package com.campusconnectplus.data.local.repository

import com.campusconnectplus.data.local.dao.UserDao
import com.campusconnectplus.data.repository.User
import com.campusconnectplus.data.repository.UserRepository
import com.campusconnectplus.data.repository.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomUserRepository(
    private val dao: UserDao
) : UserRepository {

    override fun observeUsers(): Flow<List<User>> =
        dao.observeAll().map { it.map { u -> u.toModel() } }

    override suspend fun upsert(user: User) {
        dao.upsert(user.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }

    override suspend fun getUserByEmail(email: String): User? =
        dao.getByEmail(email)?.toModel()

    override suspend fun updatePasswordHash(email: String, passwordHash: String) {
        dao.updatePasswordHash(email, passwordHash)
    }

    override suspend fun setRole(userId: String, role: UserRole) {
        dao.updateRole(userId, role.name)
    }

    override suspend fun setActive(userId: String, active: Boolean) {
        dao.updateActive(userId, active)
    }
}
