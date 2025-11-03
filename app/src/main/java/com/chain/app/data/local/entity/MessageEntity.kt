package com.chain.app.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.chain.app.domain.model.Message
import com.chain.app.domain.model.MessageStatus
import com.chain.app.domain.model.MessageType
import java.util.Date

/**
 * Room entity for Message.
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val type: String,
    val timestamp: Long,
    val status: String,
    val replyTo: String?,
    val isEncrypted: Boolean,
    val disappearAfter: Long?,
    val localFilePath: String? = null // For media files
)

/**
 * Full-text search table for messages.
 */
@Fts4(contentEntity = MessageEntity::class)
@Entity(tableName = "messages_fts")
data class MessageFts(
    @PrimaryKey
    val rowid: Long,
    val content: String
)

/**
 * Convert MessageEntity to domain Message model.
 */
fun MessageEntity.toDomain(): Message = Message(
    id = id,
    chatId = chatId,
    senderId = senderId,
    content = content,
    type = MessageType.valueOf(type),
    timestamp = Date(timestamp),
    status = MessageStatus.valueOf(status),
    replyTo = replyTo,
    reactions = emptyList(), // Would be loaded separately
    isEncrypted = isEncrypted,
    disappearAfter = disappearAfter
)

/**
 * Convert domain Message to MessageEntity.
 */
fun Message.toEntity(localFilePath: String? = null): MessageEntity = MessageEntity(
    id = id,
    chatId = chatId,
    senderId = senderId,
    content = content,
    type = type.name,
    timestamp = timestamp.time,
    status = status.name,
    replyTo = replyTo,
    isEncrypted = isEncrypted,
    disappearAfter = disappearAfter,
    localFilePath = localFilePath
)
