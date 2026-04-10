package com.campusconnectplus.core.security

import java.security.MessageDigest
import java.util.Locale

/**
 * Simple SHA-256 hashing with salt for local auth.
 * Do not use for high-security; prefer Firebase Auth or a proper backend in production.
 */
object PasswordHasher {

    private const val SALT = "CampusConnectPlus.v1"
    private const val ALGORITHM = "SHA-256"

    fun hash(password: String): String {
        val digest = MessageDigest.getInstance(ALGORITHM)
        val bytes = digest.digest((SALT + password).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(Locale.US, it) }
    }

    /**
     * Verifies password against stored hash using constant-time comparison to reduce timing attacks.
     */
    fun verify(password: String, storedHash: String): Boolean {
        val computed = hash(password)
        if (computed.length != storedHash.length) return false
        var diff = 0
        for (i in computed.indices) {
            diff = diff or (computed[i].code xor storedHash[i].code)
        }
        return diff == 0
    }
}
