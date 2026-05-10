package com.campusconnectplus.data.remote.repository

import com.campusconnectplus.data.repository.User
import com.campusconnectplus.data.repository.UserRepository
import com.campusconnectplus.data.repository.UserRole
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

fun RemoteUser.toModel() = User(
    id = id,
    name = name ?: "Unknown User",
    email = email ?: "",
    role = try { UserRole.valueOf(role.uppercase()) } catch (e: Exception) { UserRole.STUDENT },
    active = active,
    updatedAt = updated_at ?: System.currentTimeMillis()
)

fun User.toRemote() = RemoteUser(
    id = id,
    name = name,
    email = email,
    role = role.name,
    active = active,
    updated_at = System.currentTimeMillis()
)

class SupabaseUserRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime
) : UserRepository {

    override fun observeUsers(): Flow<List<User>> = flow {
        try {
            val initialUsers = postgrest["users"].select().decodeList<RemoteUser>()
            emit(initialUsers.map { it.toModel() })
        } catch (e: Exception) {
            println("Initial fetch error (users): ${e.message}")
            emit(emptyList())
        }

        val channelId = "users_realtime_${UUID.randomUUID()}"
        val channel = realtime.channel(channelId)
        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "users"
            }
            channel.subscribe()
            changeFlow.collect {
                val updatedUsers = postgrest["users"].select().decodeList<RemoteUser>()
                emit(updatedUsers.map { it.toModel() })
            }
        } catch (e: Exception) {
            println("Realtime error (users): ${e.message}")
        } finally {
            try { realtime.removeChannel(channel) } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun upsert(user: User) {
        withContext(Dispatchers.IO) {
            try {
                postgrest["users"].upsert(user.toRemote())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            try {
                postgrest["users"].delete {
                    filter { eq("id", id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        try {
            postgrest["users"].select {
                filter { eq("email", email) }
            }.decodeSingleOrNull<RemoteUser>()?.toModel()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun setRole(userId: String, role: UserRole) {
        withContext(Dispatchers.IO) {
            try {
                postgrest["users"].update({
                    set("role", role.name)
                }) {
                    filter { eq("id", userId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun setActive(userId: String, active: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                postgrest["users"].update({
                    set("active", active)
                }) {
                    filter { eq("id", userId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun updatePasswordHash(email: String, passwordHash: String) {}
}
