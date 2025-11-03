package com.chain.app.domain.model

import java.util.Date

/**
 * Chat domain model representing a conversation.
 */
data class Chat(
    val id: String,
    val type: ChatType,
    val name: String,
    val participants: List<String>,
    val admins: List<String> = emptyList(),
    val settings: ChatSettings = ChatSettings(),
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val createdAt: Date,
    val updatedAt: Date
)

enum class ChatType {
    DIRECT,
    GROUP
}

/**
 * Settings for a chat/conversation.
 */
data class ChatSettings(
    val disappearingMessagesEnabled: Boolean = false,
    val disappearingMessagesDuration: Long? = null, // milliseconds
    val notificationsEnabled: Boolean = true,
    val encryptionEnabled: Boolean = true
)

/**
 * Group-specific chat data.
 */
data class GroupChat(
    val chat: Chat,
    val maxMembers: Int = 100_000,
    val inviteLink: String? = null,
    val permissions: GroupPermissions = GroupPermissions(),
    val description: String? = null
) {
    init {
        require(chat.type == ChatType.GROUP) { "Chat must be of type GROUP" }
    }
}

/**
 * Permissions for group chat operations.
 */
data class GroupPermissions(
    val anyoneCanAddMembers: Boolean = false,
    val anyoneCanEditInfo: Boolean = false,
    val anyoneCanSendMessages: Boolean = true
)
