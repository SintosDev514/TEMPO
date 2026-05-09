package com.campusconnectplus.data.fake

import com.campusconnectplus.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class FakeUserRepository : UserRepository {
    private val mutex = Mutex()
    private val users = MutableStateFlow(
        listOf(
            User(id = "1", name = "John Doe", email = "john.doe@campus.edu", role = UserRole.ADMIN, active = true)
        )
    )

    override fun observeUsers(): Flow<List<User>> = users

    override suspend fun upsert(user: User) {
        mutex.withLock {
            val list = users.value.toMutableList()
            val idx = list.indexOfFirst { it.id == user.id && user.id.isNotEmpty() }
            if (idx >= 0) list[idx] = user else {
                val newId = UUID.randomUUID().toString()
                list.add(user.copy(id = newId))
            }
            users.value = list
        }
    }

    override suspend fun setRole(userId: String, role: UserRole) {
        mutex.withLock {
            users.value = users.value.map { if (it.id == userId) it.copy(role = role) else it }
        }
    }

    override suspend fun setActive(userId: String, active: Boolean) {
        mutex.withLock {
            users.value = users.value.map { if (it.id == userId) it.copy(active = active) else it }
        }
    }

    override suspend fun delete(id: String) {
        mutex.withLock {
            users.value = users.value.filterNot { it.id == id }
        }
    }

    override suspend fun getUserByEmail(email: String): User? = mutex.withLock {
        users.value.find { it.email.equals(email.trim(), ignoreCase = true) }?.copy(passwordHash = null)
    }

    override suspend fun updatePasswordHash(email: String, passwordHash: String) {
        mutex.withLock {
            val list = users.value.toMutableList()
            val idx = list.indexOfFirst { it.email.equals(email.trim(), ignoreCase = true) }
            if (idx >= 0) {
                list[idx] = list[idx].copy(passwordHash = passwordHash)
                users.value = list
            }
        }
    }
}
