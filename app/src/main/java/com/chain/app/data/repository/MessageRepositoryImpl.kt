package com.chain.app.data.repository

import com.chain.app.data.local.dao.MessageDao
import com.chain.app.data.local.dao.ReactionDao
import com.chain.app.data.local.entity.toEntity
import com.chain.app.data.local.entity.toDomain
import com.chain.app.domain.model.*
import com.chain.app.domain.repository.EncryptionRepository
import com.chain.app.domain.repository.MessageRepository
import com.chain.app.domain.repository.P2PRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MessageRepository using Room database and P2P networking.
 */
@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val reactionDao: ReactionDao,
    private val p2pRepository: P2PRepository,
    private val encryptionRepository: EncryptionRepository
) : MessageRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Subscribe to incoming P2P messages
        scope.launch {
            p2pRepository.subscribeToMessages().collect { p2pMessage ->
                handleIncomingP2PMessage(p2pMessage)
            }
        }
    }

    override suspend fun sendMessage(message: Message): Result<Message> {
        return try {
            // Save to local database
            messageDao.insertMessage(message.toEntity())

            // Encrypt message content
            val encryptedContent = encryptionRepository.encryptMessage(
                plaintext = message.content,
                recipientId = message.chatId // Use chatId as recipient
            ).getOrThrow()

            // Create P2P message
            val p2pMessage = P2PMessage(
                id = message.id,
                from = message.senderId,
                to = message.chatId, // Use chatId as recipient
                encryptedPayload = encryptedContent.content.toByteArray(),
                timestamp = message.timestamp.time,
                type = P2PMessageType.CHAT_MESSAGE,
                signature = ByteArray(0) // TODO: Sign with private key
            )

            // Send via P2P
            p2pRepository.sendMessage(p2pMessage).getOrThrow()

            // Update status to SENT
            updateMessageStatus(message.id, MessageStatus.SENT)

            Timber.d("Message sent via P2P: ${message.id}")
            Result.success(message)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send message")
            // Update status to FAILED
            updateMessageStatus(message.id, MessageStatus.FAILED)
            Result.failure(e)
        }
    }

    /**
     * Handle incoming P2P message.
     */
    private suspend fun handleIncomingP2PMessage(p2pMessage: P2PMessage) {
        try {
            when (p2pMessage.type) {
                P2PMessageType.CHAT_MESSAGE -> {
                    // TODO: Properly deserialize EncryptedMessage and decrypt
                    // For now, treat payload as plaintext until encryption is fully wired
                    val decryptedContent = String(p2pMessage.encryptedPayload)

                    // Create message entity
                    val message = Message(
                        id = p2pMessage.id,
                        chatId = p2pMessage.to, // Assuming direct message for now
                        senderId = p2pMessage.from,
                        content = decryptedContent,
                        type = MessageType.TEXT,
                        timestamp = Date(p2pMessage.timestamp),
                        status = MessageStatus.DELIVERED,
                        reactions = emptyList()
                    )

                    // Save to database
                    messageDao.insertMessage(message.toEntity())

                    // Send delivery receipt
                    p2pRepository.sendDeliveryReceipt(message.id, message.senderId)

                    Timber.d("Received and saved message: ${message.id}")
                }

                P2PMessageType.DELIVERY_RECEIPT -> {
                    val messageId = String(p2pMessage.encryptedPayload)
                    updateMessageStatus(messageId, MessageStatus.DELIVERED)
                    Timber.d("Message $messageId delivered")
                }

                P2PMessageType.READ_RECEIPT -> {
                    val messageId = String(p2pMessage.encryptedPayload)
                    updateMessageStatus(messageId, MessageStatus.READ)
                    Timber.d("Message $messageId read")
                }

                else -> {
                    Timber.d("Unhandled P2P message type: ${p2pMessage.type}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to handle incoming P2P message")
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
        // Observe messages from local database (P2P messages are automatically saved)
        return messageDao.getMessages(chatId, 1000, 0)
            .map { entities -> entities.firstOrNull()?.toDomain() }
            .map { it ?: throw NoSuchElementException("No messages") }
    }

    override suspend fun syncMessages(): Result<Unit> {
        return try {
            // Retrieve any stored offline messages
            val storedMessages = p2pRepository.retrieveStoredMessages().getOrThrow()

            storedMessages.forEach { storedMessage ->
                handleIncomingP2PMessage(storedMessage.encryptedMessage)
                // Mark as delivered
                p2pRepository.deleteStoredMessage(storedMessage.id)
            }

            Timber.d("Synced ${storedMessages.size} offline messages")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync messages")
            Result.failure(e)
        }
    }
}
