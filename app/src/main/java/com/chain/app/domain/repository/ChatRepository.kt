package com.chain.app.domain.repository

import com.chain.app.domain.model.Chat
import com.chain.app.domain.model.GroupChat
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat operations.
 */
interface ChatRepository {
    /**
     * Get all chats for the current user.
     */
    fun getChats(): Flow<List<Chat>>

    /**
     * Get a specific chat by ID.
     */
    suspend fun getChat(chatId: String): Result<Chat>

    /**
     * Create a direct chat with another user.
     */
    suspend fun createDirectChat(userId: String): Result<Chat>

    /**
     * Create a group chat.
     */
    suspend fun createGroupChat(
        name: String,
        participants: List<String>,
        description: String? = null
    ): Result<GroupChat>

    /**
     * Add members to a group chat.
     */
    suspend fun addMembers(chatId: String, userIds: List<String>): Result<Unit>

    /**
     * Remove members from a group chat.
     */
    suspend fun removeMembers(chatId: String, userIds: List<String>): Result<Unit>

    /**
     * Update chat settings.
     */
    suspend fun updateChatSettings(chatId: String, settings: Map<String, Any>): Result<Unit>

    /**
     * Pin/unpin a chat.
     */
    suspend fun setPinned(chatId: String, isPinned: Boolean): Result<Unit>

    /**
     * Archive/unarchive a chat.
     */
    suspend fun setArchived(chatId: String, isArchived: Boolean): Result<Unit>

    /**
     * Mute/unmute a chat.
     */
    suspend fun setMuted(chatId: String, isMuted: Boolean): Result<Unit>

    /**
     * Delete a chat.
     */
    suspend fun deleteChat(chatId: String): Result<Unit>

    /**
     * Observe chat updates.
     */
    fun observeChat(chatId: String): Flow<Chat>
}
