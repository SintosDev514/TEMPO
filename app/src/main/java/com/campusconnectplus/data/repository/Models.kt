package com.campusconnectplus.data.repository

enum class MediaType { IMAGE, VIDEO }
enum class EventCategory { ACADEMIC, CULTURAL, SPORTS }

// Optional: use this if your Announcement UI is showing status
enum class AnnouncementStatus { ACTIVE, ARCHIVED }

// Optional: use this if your User UI toggles "active"
enum class UserRole { STUDENT, ADMIN, ORGANIZER, MEDIA_TEAM }

data class Event(
    val id: Long = 0L,
    val title: String,
    val date: String,          // keep as String (UI shows it as text)
    val venue: String,
    val description: String,
    val category: EventCategory,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Media(
    val id: Long = 0L,
    val eventId: Long,
    val url: String,
    val type: MediaType,

    // these are the fields your fake repos / admin UI are trying to use
    val title: String = "",
    val fileName: String = "",
    val date: String = "",           // shown in admin/media lists
    val sizeMb: Int = 0,             // shown in admin/media lists
    val duration: String = "",       // only meaningful for VIDEO; keep String for simplicity
    val saves: Int = 0,              // used by fake repo (“saves”)
    val coverUrl: String = "",       // used by event/media fake repo (“coverUrl”)

    val updatedAt: Long = System.currentTimeMillis()
)

data class Announcement(
    val id: Long = 0L,
    val title: String,

    // your fake repo uses "content" (not "message")
    val content: String,

    // your fake repo/UI is trying to pass these
    val priority: Int = 0,
    val status: AnnouncementStatus = AnnouncementStatus.ACTIVE,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class User(
    val id: Long = 0L,
    val name: String,
    val email: String,
    val role: UserRole,
    val active: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    /** Only set when creating/updating credentials; never exposed from repository. */
    val passwordHash: String? = null
)
