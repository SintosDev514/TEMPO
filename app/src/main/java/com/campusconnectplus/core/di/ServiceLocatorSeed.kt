package com.campusconnectplus.core.di

import android.content.Context
import com.campusconnectplus.core.security.PasswordHasher
import com.campusconnectplus.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ServiceLocatorSeed {

    private const val PREF = "cc_seed"
    private const val KEY_DONE = "seed_done_v1"
    private const val KEY_ADMIN_PASSWORD = "seed_admin_password_v1"
    private const val DEFAULT_ADMIN_PASSWORD = "admin123"

    /** Email of the built-in admin; used by seed and by UI to protect role. */
    internal const val DEFAULT_ADMIN_EMAIL = "admin@campus.edu"

    suspend fun ensureSeed(context: Context, container: AppContainer) = withContext(Dispatchers.IO) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (sp.getBoolean(KEY_DONE, false)) return@withContext

        val e1 = Event(
            id = 1L,
            title = "Campus Tech Talk 2026",
            date = "Feb 20, 2026",
            venue = "Auditorium",
            description = "A talk about mobile dev, AI tools, and industry trends.",
            category = EventCategory.ACADEMIC
        )
        val e2 = Event(
            id = 2L,
            title = "Cultural Night",
            date = "Mar 05, 2026",
            venue = "Open Grounds",
            description = "Dance, music, booths, and student performances.",
            category = EventCategory.CULTURAL
        )
        val e3 = Event(
            id = 3L,
            title = "Intramurals Finals",
            date = "Mar 18, 2026",
            venue = "Gymnasium",
            description = "Final matches for basketball and volleyball.",
            category = EventCategory.SPORTS
        )

        container.eventRepository.upsert(e1)
        container.eventRepository.upsert(e2)
        container.eventRepository.upsert(e3)

        container.mediaRepository.upsert(
            Media(
                id = 1L,
                eventId = 1L,
                url = "https://example.com/tech_talk.jpg",
                type = MediaType.IMAGE,
                title = "Tech Talk Poster"
            )
        )
        container.mediaRepository.upsert(
            Media(
                id = 2L,
                eventId = 2L,
                url = "https://example.com/cultural_night.mp4",
                type = MediaType.VIDEO,
                title = "Highlights"
            )
        )

        container.announcementRepository.upsert(
            Announcement(
                id = 1L,
                title = "App Update",
                content = "CampusConnect+ now supports offline metadata caching."
            )
        )

        container.userRepository.upsert(
            User(
                id = 1L,
                name = "System Admin",
                email = DEFAULT_ADMIN_EMAIL,
                role = UserRole.ADMIN,
                passwordHash = PasswordHasher.hash(DEFAULT_ADMIN_PASSWORD)
            )
        )

        sp.edit().putBoolean(KEY_DONE, true).apply()
    }

    /** One-time: set default password for existing admin user if missing (e.g. after schema upgrade). */
    suspend fun ensureDefaultAdminPassword(container: AppContainer, context: Context) = withContext(Dispatchers.IO) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (sp.getBoolean(KEY_ADMIN_PASSWORD, false)) return@withContext
        val user = container.userRepository.getUserByEmail(DEFAULT_ADMIN_EMAIL) ?: return@withContext
        container.userRepository.updatePasswordHash(DEFAULT_ADMIN_EMAIL, PasswordHasher.hash(DEFAULT_ADMIN_PASSWORD))
        sp.edit().putBoolean(KEY_ADMIN_PASSWORD, true).apply()
    }

    /** Ensures the default admin account (admin@campus.edu) always has ADMIN role so login goes to Dashboard, not Media/Staff. */
    suspend fun ensureDefaultAdminRole(container: AppContainer) = withContext(Dispatchers.IO) {
        val user = container.userRepository.getUserByEmail(DEFAULT_ADMIN_EMAIL) ?: return@withContext
        if (user.role != UserRole.ADMIN) {
            container.userRepository.setRole(user.id, UserRole.ADMIN)
        }
    }
}
