package com.chain.app.data.repository

import com.chain.app.data.local.dao.MessageDao
import com.chain.app.data.local.dao.ReactionDao
import com.chain.app.data.local.entity.toEntity
import com.chain.app.data.local.entity.toDomain
import com.chain.app.data.preferences.UserPreferences
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
    private val encryptionRepository: EncryptionRepository,
    private val userPreferences: UserPreferences,
    private val userDao: com.chain.app.data.local.dao.UserDao
) : MessageRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Track typing status for each chat
    private val typingStatusMap = mutableMapOf<String, kotlinx.coroutines.flow.MutableStateFlow<Boolean>>()

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

            // Try to encrypt message content, fall back to plaintext if no session exists
            // TODO for Production: Implement automatic session establishment
            // 1. Check if session exists with recipient
            // 2. If not, fetch recipient's pre-key bundle via P2P or key server
            // 3. Initialize session with their bundle using encryptionRepository.initializeSession()
            // 4. Then encrypt and send
            // 5. Store session for future messages
            val payloadBytes = try {
                val encryptedContent = encryptionRepository.encryptMessage(
                    plaintext = message.content,
                    recipientId = message.chatId // Use chatId as recipient
                ).getOrThrow()
                // Properly serialize the encrypted message
                android.util.Base64.decode(encryptedContent.content, android.util.Base64.NO_WRAP)
            } catch (e: org.signal.libsignal.protocol.NoSessionException) {
                // No session exists yet - in production, this should trigger session establishment
                // For now, fall back to plaintext to maintain functionality
                Timber.w("No session exists for ${message.chatId}, sending as plaintext. " +
                        "Production TODO: Establish session first.")
                message.content.toByteArray()
            }

            // Sign the message payload with private key
            val signature = try {
                encryptionRepository.signMessage(payloadBytes).getOrThrow()
            } catch (e: Exception) {
                Timber.w("Failed to sign message, sending without signature: ${e.message}")
                ByteArray(0)
            }

            // Create P2P message
            val p2pMessage = P2PMessage(
                id = message.id,
                from = message.senderId,
                to = message.chatId, // Use chatId as recipient
                encryptedPayload = payloadBytes,
                timestamp = message.timestamp.time,
                type = P2PMessageType.CHAT_MESSAGE,
                signature = signature
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
                    // Verify message signature if present
                    if (p2pMessage.signature.isNotEmpty()) {
                        val isValid = try {
                            encryptionRepository.verifySignature(
                                payload = p2pMessage.encryptedPayload,
                                signature = p2pMessage.signature,
                                senderId = p2pMessage.from
                            ).getOrElse { false }
                        } catch (e: Exception) {
                            Timber.w("Signature verification failed: ${e.message}")
                            false
                        }

                        if (!isValid) {
                            Timber.w("Invalid message signature from ${p2pMessage.from}, rejecting message")
                            return // Reject message with invalid signature
                        }
                        Timber.d("Message signature verified for ${p2pMessage.from}")
                    }

                    // Try to decrypt the message, fall back to plaintext if decryption fails
                    val decryptedContent = try {
                        // Try to decrypt as encrypted message
                        val encryptedMessage = EncryptedMessage(
                            content = android.util.Base64.encodeToString(
                                p2pMessage.encryptedPayload,
                                android.util.Base64.NO_WRAP
                            ),
                            type = MessageType.TEXT,
                            keyId = "",
                            timestamp = p2pMessage.timestamp
                        )
                        encryptionRepository.decryptMessage(
                            ciphertext = encryptedMessage,
                            senderId = p2pMessage.from
                        ).getOrThrow()
                    } catch (e: Exception) {
                        // If decryption fails (no session or plaintext), treat as plaintext
                        Timber.w("Decryption failed, treating as plaintext: ${e.message}")
                        String(p2pMessage.encryptedPayload)
                    }

                    // Create message entity
                    // chatId should be the sender's ID so the message appears in chat with them
                    val message = Message(
                        id = p2pMessage.id,
                        chatId = p2pMessage.from, // Use sender ID as chatId for direct messages
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

                    Timber.d("Received and saved message: ${message.id} from ${p2pMessage.from}")
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

                P2PMessageType.TYPING_INDICATOR -> {
                    val isTyping = String(p2pMessage.encryptedPayload).toBoolean()
                    val chatId = p2pMessage.from // Typing indicator comes from the sender
                    typingStatusMap.getOrPut(chatId) {
                        kotlinx.coroutines.flow.MutableStateFlow(false)
                    }.value = isTyping
                    Timber.d("Typing indicator from $chatId: $isTyping")
                }

                P2PMessageType.PRESENCE_UPDATE -> {
                    val statusName = String(p2pMessage.encryptedPayload)
                    val status = try {
                        UserStatus.valueOf(statusName)
                    } catch (e: IllegalArgumentException) {
                        UserStatus.OFFLINE
                    }
                    // Update user status in database
                    userDao.updateUserStatus(
                        userId = p2pMessage.from,
                        status = status.name,
                        lastSeen = p2pMessage.timestamp
                    )
                    Timber.d("Presence update from ${p2pMessage.from}: $status")
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
            val currentUserId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            val reaction = Reaction(
                emoji = emoji,
                userId = currentUserId,
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
            val currentUserId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            reactionDao.deleteReaction(messageId, emoji, currentUserId)
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
