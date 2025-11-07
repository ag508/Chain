package com.chain.app.data.repository

import com.chain.app.data.local.dao.ChatDao
import com.chain.app.data.local.entity.toEntity
import com.chain.app.data.local.entity.toDomain
import com.chain.app.data.preferences.UserPreferences
import com.chain.app.domain.model.Chat
import com.chain.app.domain.model.ChatType
import com.chain.app.domain.model.GroupChat
import com.chain.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ChatRepository using Room database.
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val userPreferences: UserPreferences
) : ChatRepository {

    override fun getChats(): Flow<List<Chat>> {
        return chatDao.getChats()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getChat(chatId: String): Result<Chat> {
        return try {
            val entity = chatDao.getChat(chatId)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception("Chat not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDirectChat(userId: String): Result<Chat> {
        return try {
            // Check if chat already exists
            val existing = chatDao.getDirectChatWithUser(userId)
            if (existing != null) {
                return Result.success(existing.toDomain())
            }

            // Get current user ID
            val currentUserId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            // Create new chat
            val chat = Chat(
                id = UUID.randomUUID().toString(),
                type = ChatType.DIRECT,
                name = "", // Will be set to user's display name
                participants = listOf(currentUserId, userId),
                createdAt = Date(),
                updatedAt = Date()
            )
            chatDao.insertChat(chat.toEntity())
            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createGroupChat(
        name: String,
        participants: List<String>,
        description: String?
    ): Result<GroupChat> {
        return try {
            // The participants list should already include the creator (added by use case)
            // Creator is the last participant (current user added at the end by use case)
            val creator = participants.lastOrNull()
                ?: return Result.failure(Exception("No participants provided"))

            val chat = Chat(
                id = UUID.randomUUID().toString(),
                type = ChatType.GROUP,
                name = name,
                participants = participants,
                admins = listOf(creator),
                createdAt = Date(),
                updatedAt = Date()
            )
            chatDao.insertChat(chat.toEntity())

            val groupChat = GroupChat(
                chat = chat,
                description = description
            )
            Result.success(groupChat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMembers(chatId: String, userIds: List<String>): Result<Unit> {
        return try {
            val chat = chatDao.getChat(chatId)
                ?: return Result.failure(Exception("Chat not found"))

            val updatedChat = chat.copy(
                participants = (chat.participants + userIds).distinct()
            )
            chatDao.updateChat(updatedChat)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeMembers(chatId: String, userIds: List<String>): Result<Unit> {
        return try {
            val chat = chatDao.getChat(chatId)
                ?: return Result.failure(Exception("Chat not found"))

            val updatedChat = chat.copy(
                participants = chat.participants.filterNot { it in userIds }
            )
            chatDao.updateChat(updatedChat)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateChatSettings(chatId: String, settings: Map<String, Any>): Result<Unit> {
        // TODO: Implement settings update
        return Result.failure(NotImplementedError("Chat settings update not yet implemented"))
    }

    override suspend fun setPinned(chatId: String, isPinned: Boolean): Result<Unit> {
        return try {
            chatDao.setPinned(chatId, isPinned)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setArchived(chatId: String, isArchived: Boolean): Result<Unit> {
        return try {
            chatDao.setArchived(chatId, isArchived)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setMuted(chatId: String, isMuted: Boolean): Result<Unit> {
        return try {
            chatDao.setMuted(chatId, isMuted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            chatDao.deleteChatById(chatId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeChat(chatId: String): Flow<Chat> {
        return chatDao.observeChat(chatId)
            .map { it?.toDomain() ?: throw Exception("Chat not found") }
    }
}
