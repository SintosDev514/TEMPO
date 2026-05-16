package com.campusconnectplus.data.repository

import kotlinx.serialization.Serializable

@Serializable
enum class MediaType { IMAGE, VIDEO }

@Serializable
enum class EventCategory { ACADEMIC, CULTURAL, SPORTS }

@Serializable
enum class ReactionType { LIKE, LOVE, WOW, SAD, ANGRY }

@Serializable
enum class AnnouncementStatus { ACTIVE, ARCHIVED }

@Serializable
enum class UserRole { STUDENT, ADMIN, ORGANIZER, MEDIA_TEAM }

@Serializable
data class Event(
    val id: String = "",
    val title: String,
    val date: String,   
    val venue: String,
    val description: String,
    val category: EventCategory,
    val imageUrl: String? = null,
    val reactionCounts: Map<ReactionType, Int> = emptyMap(),
    val userReaction: ReactionType? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class Media(
    val id: String = "",
    val eventId: String,
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
    val localPath: String? = null,

    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class Announcement(
    val id: String = "",
    val title: String,

    // your fake repo uses "content" (not "message")
    val content: String,

    // your fake repo/UI is trying to pass these
    val priority: Int = 0,
    val status: AnnouncementStatus = AnnouncementStatus.ACTIVE,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class User(
    val id: String = "",
    val name: String,
    val email: String,
    val role: UserRole,
    val active: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    /** Only set when creating/updating credentials; never exposed from repository. */
    val passwordHash: String? = null
)
