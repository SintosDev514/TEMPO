package com.campusconnectplus.data.local.repository

import com.campusconnectplus.data.local.entity.*
import com.campusconnectplus.data.repository.*

fun EventEntity.toModel(): Event =
    Event(
        id = id.toLongOrNull() ?: 0L,
        title = title,
        date = date,
        venue = venue,
        description = description,
        category = EventCategory.valueOf(category),
        updatedAt = updatedAt
    )

fun Event.toEntity(): EventEntity =
    EventEntity(
        id = id.toString(),
        title = title,
        date = date,
        venue = venue,
        description = description,
        category = category.name,
        updatedAt = updatedAt
    )

fun MediaEntity.toModel(): Media =
    Media(
        id = id.toLongOrNull() ?: 0L,
        eventId = eventId.toLongOrNull() ?: 0L,
        url = url,
        type = MediaType.valueOf(type),
        title = title ?: "",
        fileName = description ?: "",
        updatedAt = updatedAt
    )

fun Media.toEntity(): MediaEntity =
    MediaEntity(
        id = id.toString(),
        eventId = eventId.toString(),
        url = url,
        type = type.name,
        title = title,
        description = fileName,
        updatedAt = updatedAt
    )

fun AnnouncementEntity.toModel(): Announcement =
    Announcement(
        id = id.toLongOrNull() ?: 0L,
        title = title,
        content = message,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun Announcement.toEntity(): AnnouncementEntity =
    AnnouncementEntity(
        id = id.toString(),
        title = title,
        message = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun UserEntity.toModel(): User =
    User(
        id = id.toLongOrNull() ?: 0L,
        name = name,
        email = email,
        role = UserRole.valueOf(role),
        active = active,
        updatedAt = updatedAt,
        passwordHash = null
    )

fun User.toEntity(): UserEntity =
    UserEntity(
        id = id.toString(),
        name = name,
        email = email,
        role = role.name,
        active = active,
        updatedAt = updatedAt,
        passwordHash = passwordHash
    )
