package com.chain.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chain.app.domain.model.User
import com.chain.app.domain.model.UserStatus
import java.util.Date

/**
 * Room entity for User.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val phoneNumber: String,
    val email: String? = null,
    val displayName: String,
    val avatar: String?,
    val about: String? = null,
    val publicKey: String?,
    val status: String,
    val lastSeen: Long,
    val createdAt: Long,
    val isContact: Boolean = false,
    val isBlocked: Boolean = false
)

/**
 * Convert UserEntity to domain User model.
 */
fun UserEntity.toDomain(): User = User(
    id = id,
    phoneNumber = phoneNumber,
    email = email,
    publicKey = publicKey,
    displayName = displayName,
    avatar = avatar,
    about = about,
    status = UserStatus.valueOf(status),
    lastSeen = Date(lastSeen),
    devices = emptyList() // Devices would be loaded separately if needed
)

/**
 * Convert domain User to UserEntity.
 */
fun User.toEntity(isContact: Boolean = false, isBlocked: Boolean = false): UserEntity = UserEntity(
    id = id,
    phoneNumber = phoneNumber,
    email = email,
    publicKey = publicKey,
    displayName = displayName,
    avatar = avatar,
    about = about,
    status = status.name,
    lastSeen = lastSeen.time,
    createdAt = System.currentTimeMillis(),
    isContact = isContact,
    isBlocked = isBlocked
)
