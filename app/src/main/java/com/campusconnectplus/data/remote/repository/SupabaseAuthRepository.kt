package com.campusconnectplus.data.remote.repository

import com.campusconnectplus.core.util.Constants
import com.campusconnectplus.data.repository.AuthRepository
import com.campusconnectplus.data.repository.AuthResult
import com.campusconnectplus.data.repository.User
import com.campusconnectplus.data.repository.UserRole
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SupabaseAuthRepository @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val session = auth.currentSessionOrNull()
            if (session != null) {
                // Try to fetch custom role and name from public.users table
                val remoteUser = try {
                    postgrest["users"].select {
                        filter { eq("id", session.user?.id ?: "") }
                    }.decodeSingleOrNull<RemoteUser>()
                } catch (e: Exception) { null }

                val user = User(
                    id = session.user?.id ?: "",
                    name = remoteUser?.name ?: session.user?.userMetadata?.get("name")?.let { it.toString().removeSurrounding("\"") } ?: "",
                    email = session.user?.email ?: email,
                    role = when {
                        (session.user?.email ?: email) == Constants.DEFAULT_ADMIN_EMAIL -> UserRole.ADMIN
                        remoteUser != null -> try { UserRole.valueOf(remoteUser.role) } catch (e: Exception) { UserRole.STUDENT }
                        else -> UserRole.STUDENT
                    },
                    active = remoteUser?.active ?: true,
                    updatedAt = remoteUser?.updated_at ?: System.currentTimeMillis()
                )
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Login failed: Session is null")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun signUp(role: UserRole, name: String, email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("name", name)
                    put("role", role.name)
                }
            }
            
            val user = auth.currentSessionOrNull()?.user
            if (user != null) {
                // Create profile in public.users table
                try {
                    postgrest["users"].upsert(RemoteUser(
                        id = user.id,
                        name = name,
                        email = email,
                        role = role.name,
                        active = true,
                        updated_at = System.currentTimeMillis()
                    ))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            AuthResult.Success(
                User(
                    id = user?.id ?: "",
                    name = name,
                    email = email,
                    role = role,
                    active = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign up failed")
        }
    }
}
