package com.chain.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.chain.app.data.local.converter.StringListConverter
import com.chain.app.domain.model.Chat
import com.chain.app.domain.model.ChatSettings
import com.chain.app.domain.model.ChatType
import java.util.Date

/**
 * Room entity for Chat.
 */
@Entity(tableName = "chats")
@TypeConverters(StringListConverter::class)
data class ChatEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val name: String,
    val participants: List<String>,
    val admins: List<String>,
    val disappearingMessagesEnabled: Boolean,
    val disappearingMessagesDuration: Long?,
    val notificationsEnabled: Boolean,
    val encryptionEnabled: Boolean,
    val unreadCount: Int,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isMuted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Convert ChatEntity to domain Chat model.
 */
fun ChatEntity.toDomain(): Chat = Chat(
    id = id,
    type = ChatType.valueOf(type),
    name = name,
    participants = participants,
    admins = admins,
    settings = ChatSettings(
        disappearingMessagesEnabled = disappearingMessagesEnabled,
        disappearingMessagesDuration = disappearingMessagesDuration,
        notificationsEnabled = notificationsEnabled,
        encryptionEnabled = encryptionEnabled
    ),
    lastMessage = null, // Would be loaded with a relation query
    unreadCount = unreadCount,
    isPinned = isPinned,
    isArchived = isArchived,
    isMuted = isMuted,
    createdAt = Date(createdAt),
    updatedAt = Date(updatedAt)
)

/**
 * Convert domain Chat to ChatEntity.
 */
fun Chat.toEntity(): ChatEntity = ChatEntity(
    id = id,
    type = type.name,
    name = name,
    participants = participants,
    admins = admins,
    disappearingMessagesEnabled = settings.disappearingMessagesEnabled,
    disappearingMessagesDuration = settings.disappearingMessagesDuration,
    notificationsEnabled = settings.notificationsEnabled,
    encryptionEnabled = settings.encryptionEnabled,
    unreadCount = unreadCount,
    isPinned = isPinned,
    isArchived = isArchived,
    isMuted = isMuted,
    createdAt = createdAt.time,
    updatedAt = updatedAt.time
)
