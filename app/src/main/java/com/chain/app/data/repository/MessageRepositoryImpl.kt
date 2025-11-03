package com.chain.app.data.repository

import com.chain.app.data.local.dao.MessageDao
import com.chain.app.data.local.dao.ReactionDao
import com.chain.app.data.local.entity.toEntity
import com.chain.app.data.local.entity.toDomain
import com.chain.app.domain.model.Message
import com.chain.app.domain.model.MessageStatus
import com.chain.app.domain.model.Reaction
import com.chain.app.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MessageRepository using Room database.
 */
@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val reactionDao: ReactionDao
) : MessageRepository {

    override suspend fun sendMessage(message: Message): Result<Message> {
        return try {
            messageDao.insertMessage(message.toEntity())
            // TODO: Broadcast message to blockchain
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMessages(chatId: String, limit: Int, offset: Int): Flow<List<Message>> {
        return messageDao.getMessages(chatId, limit, offset)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getMessage(messageId: String): Result<Message> {
        return try {
            val entity = messageDao.getMessage(messageId)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception("Message not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus): Result<Unit> {
        return try {
            messageDao.updateMessageStatus(messageId, status.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMessages(messageIds: List<String>): Result<Unit> {
        return try {
            messageDao.deleteMessages(messageIds)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchMessages(query: String): Flow<List<Message>> {
        return messageDao.searchMessages(query)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addReaction(messageId: String, emoji: String): Result<Unit> {
        return try {
            val reaction = Reaction(
                emoji = emoji,
                userId = "current_user", // TODO: Get from UserRepository
                timestamp = Date()
            )
            reactionDao.insertReaction(reaction.toEntity(messageId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeReaction(messageId: String, emoji: String): Result<Unit> {
        return try {
            reactionDao.deleteReaction(messageId, emoji, "current_user")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeMessages(chatId: String): Flow<Message> {
        // TODO: Implement real-time message observation from blockchain
        throw NotImplementedError("Real-time message observation not yet implemented")
    }

    override suspend fun syncMessages(): Result<Unit> {
        // TODO: Implement blockchain sync
        return Result.failure(NotImplementedError("Message sync not yet implemented"))
    }
}
