package com.campusconnectplus.data.remote.repository

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class RemoteUser(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val role: String,
    val active: Boolean = true,
    val updated_at: Long? = null
)

@Serializable
data class RemoteEvent(
    val id: String? = null,
    val title: String? = null,
    val date: String? = null,
    val venue: String? = null,
    val description: String? = null,
    val category: String? = null,
    val reaction_counts: Map<String, Int>? = null,
    val updated_at: Long? = null,
    val created_at: Long? = null
)

@Serializable
data class RemoteMedia(
    val id: String? = null,
    val event_id: String? = null,
    @Transient
    val user_id: String? = null, // Hidden from auto-serialization to avoid column errors
    val url: String? = null,
    val type: String? = null,
    val title: String? = null,
    val file_name: String? = null,
    val date: String? = null,
    val size_mb: Int? = null,
    val duration: String? = null,
    val saves: Int? = null,
    val cover_url: String? = null,
    val updated_at: Long? = null
)

@Serializable
data class RemoteAnnouncement(
    val id: String? = null,
    val title: String? = null,
    val content: String? = null,
    val priority: Int? = null,
    val status: String? = null,
    val created_at: Long? = null,
    val updated_at: Long? = null
)
