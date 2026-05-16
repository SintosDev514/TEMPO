package com.campusconnectplus.data.local.repository

import com.campusconnectplus.data.local.entity.*
import com.campusconnectplus.data.repository.*

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

fun EventEntity.toModel(): Event =
    Event(
        id = id,
        title = title,
        date = date,
        venue = venue,
        description = description,
        category = EventCategory.valueOf(category),
        imageUrl = imageUrl,
        reactionCounts = try {
            val rawMap = Json.decodeFromString<Map<String, Int>>(reactionCounts)
            rawMap.entries.associate { (key, value) ->
                val type = try { ReactionType.valueOf(key.uppercase()) } catch (e: Exception) { ReactionType.LIKE }
                type to value
            }
        } catch (e: Exception) {
            emptyMap()
        },
        userReaction = userReaction?.let { ReactionType.valueOf(it) },
        updatedAt = updatedAt
    )

fun Event.toEntity(): EventEntity =
    EventEntity(
        id = id,
        title = title,
        date = date,
        venue = venue,
        description = description,
        category = category.name,
        imageUrl = imageUrl,
        reactionCounts = Json.encodeToString(reactionCounts),
        userReaction = userReaction?.name,
        updatedAt = updatedAt
    )

fun MediaEntity.toModel(): Media =
    Media(
        id = id,
        eventId = eventId,
        url = url,
        type = MediaType.valueOf(type),
        title = title ?: "",
        fileName = fileName ?: "",
        date = date ?: "",
        sizeMb = sizeMb,
        duration = duration ?: "",
        saves = saves,
        coverUrl = coverUrl ?: "",
        localPath = localPath,
        updatedAt = updatedAt
    )

fun Media.toEntity(): MediaEntity =
    MediaEntity(
        id = id,
        eventId = eventId,
        url = url,
        type = type.name,
        title = title,
        fileName = fileName,
        date = date,
        sizeMb = sizeMb,
        duration = duration,
        saves = saves,
        coverUrl = coverUrl,
        localPath = localPath,
        updatedAt = updatedAt
    )

fun AnnouncementEntity.toModel(): Announcement =
    Announcement(
        id = id,
        title = title,
        content = message,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun Announcement.toEntity(): AnnouncementEntity =
    AnnouncementEntity(
        id = id,
        title = title,
        message = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun UserEntity.toModel(): User =
    User(
        id = id,
        name = name,
        email = email,
        role = UserRole.valueOf(role),
        active = active,
        updatedAt = updatedAt,
        passwordHash = null
    )

fun User.toEntity(): UserEntity =
    UserEntity(
        id = id,
        name = name,
        email = email,
        role = role.name,
        active = active,
        updatedAt = updatedAt,
        passwordHash = passwordHash
    )
