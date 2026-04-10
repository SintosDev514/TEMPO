package com.campusconnectplus.data.local.repository

import com.campusconnectplus.core.security.PasswordHasher
import com.campusconnectplus.data.local.dao.UserDao
import com.campusconnectplus.data.repository.AuthRepository
import com.campusconnectplus.data.repository.AuthResult
import com.campusconnectplus.data.repository.User
import com.campusconnectplus.data.repository.UserRepository
import com.campusconnectplus.data.repository.UserRole

class RoomAuthRepository(
    private val userDao: UserDao,
    private val userRepository: UserRepository
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank())
            return AuthResult.Error("Email is required.")
        val entity = userDao.getByEmail(trimmedEmail)
            ?: return AuthResult.Error("Invalid email or password.")
        if (entity.passwordHash.isNullOrBlank())
            return AuthResult.Error("Invalid email or password.")
        if (!entity.active)
            return AuthResult.Error("This account is disabled. Contact an administrator.")
        if (!PasswordHasher.verify(password, entity.passwordHash))
            return AuthResult.Error("Invalid email or password.")
        return AuthResult.Success(entity.toModel())
    }

    override suspend fun signUp(role: UserRole, name: String, email: String, password: String): AuthResult {
        val trimmedEmail = email.trim()
        if (userDao.getByEmail(trimmedEmail) != null)
            return AuthResult.Error("An account with this email already exists.")
        val now = System.currentTimeMillis()
        val user = User(
            id = now,
            name = name.trim(),
            email = trimmedEmail,
            role = role,
            active = true,
            updatedAt = now,
            passwordHash = PasswordHasher.hash(password)
        )
        userRepository.upsert(user)
        return AuthResult.Success(user.copy(passwordHash = null))
    }
}
