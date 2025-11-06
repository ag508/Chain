package com.chain.app.domain.model

import java.util.Date

/**
 * Message domain model representing a single message in a conversation.
 */
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val type: MessageType,
    val timestamp: Date,
    val status: MessageStatus,
    val replyTo: String? = null,
    val reactions: List<Reaction> = emptyList(),
    val isEncrypted: Boolean = true,
    val disappearAfter: Long? = null, // Milliseconds after which message should disappear
    val metadata: Map<String, Any?>? = null // For media messages: caption, fileName, fileSize, duration, etc.
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    LOCATION,
    CONTACT,
    POLL,
    SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

/**
 * Emoji reaction to a message.
 */
data class Reaction(
    val emoji: String,
    val userId: String,
    val timestamp: Date
)
