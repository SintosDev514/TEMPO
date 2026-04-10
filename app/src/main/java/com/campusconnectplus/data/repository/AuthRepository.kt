package com.campusconnectplus.data.repository

import com.campusconnectplus.core.security.PasswordHasher

/**
 * Result of a login or signup attempt.
 */
sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Handles admin authentication against local User table (email + hashed password).
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun signUp(role: UserRole, name: String, email: String, password: String): AuthResult
}
