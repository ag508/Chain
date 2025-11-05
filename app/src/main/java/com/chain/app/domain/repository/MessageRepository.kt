package com.chain.app.domain.repository

import com.chain.app.domain.model.Message
import com.chain.app.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for message operations.
 */
interface MessageRepository {
    /**
     * Send a message to a chat.
     */
    suspend fun sendMessage(message: Message): Result<Message>

    /**
     * Get messages for a specific chat.
     */
    fun getMessages(chatId: String, limit: Int, offset: Int): Flow<List<Message>>

    /**
     * Get a specific message by ID.
     */
    suspend fun getMessage(messageId: String): Result<Message>

    /**
     * Update message status.
     */
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus): Result<Unit>

    /**
     * Delete messages.
     */
    suspend fun deleteMessages(messageIds: List<String>): Result<Unit>

    /**
     * Search messages.
     */
    fun searchMessages(query: String): Flow<List<Message>>

    /**
     * Add reaction to a message.
     */
    suspend fun addReaction(messageId: String, emoji: String): Result<Unit>

    /**
     * Remove reaction from a message.
     */
    suspend fun removeReaction(messageId: String, emoji: String): Result<Unit>

    /**
     * Observe incoming messages for a chat.
     */
    fun observeMessages(chatId: String): Flow<Message>

    /**
     * Sync messages from P2P network.
     */
    suspend fun syncMessages(): Result<Unit>
}
