package com.chain.app.data.p2p.storeforward

import com.chain.app.domain.model.P2PMessage
import com.chain.app.domain.model.StoredMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages store-and-forward mechanism for offline message delivery.
 * Stores messages temporarily when recipients are offline and delivers when they come online.
 */
@Singleton
class StoreForwardManager @Inject constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Stored messages: messageId -> StoredMessage
    private val storedMessages = ConcurrentHashMap<String, StoredMessage>()

    // Messages we're storing for others: recipientId -> List<StoredMessage>
    private val messagesForOthers = ConcurrentHashMap<String, MutableList<StoredMessage>>()

    // Our pending messages: recipientId -> List<StoredMessage>
    private val pendingMessages = ConcurrentHashMap<String, MutableList<StoredMessage>>()

    // Message expiry time (7 days)
    private val messageExpiryMs = TimeUnit.DAYS.toMillis(7)

    // Maximum delivery attempts
    private val maxDeliveryAttempts = 5

    init {
        // Start cleanup job
        scope.launch {
            while (true) {
                delay(TimeUnit.HOURS.toMillis(1)) // Run every hour
                cleanupExpiredMessages()
            }
        }
    }

    /**
     * Store a message for offline delivery.
     */
    suspend fun storeMessage(recipientId: String, message: P2PMessage): Result<String> {
        return try {
            val storedMessage = StoredMessage(
                id = UUID.randomUUID().toString(),
                recipientId = recipientId,
                encryptedMessage = message,
                storedAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + messageExpiryMs,
                attempts = 0,
                maxAttempts = maxDeliveryAttempts
            )

            storedMessages[storedMessage.id] = storedMessage

            // Add to pending messages for this recipient
            pendingMessages.getOrPut(recipientId) { mutableListOf() }.add(storedMessage)

            Timber.d("Stored message ${storedMessage.id} for offline recipient: $recipientId")
            Result.success(storedMessage.id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to store message for recipient: $recipientId")
            Result.failure(e)
        }
    }

    /**
     * Store a message on behalf of another peer (acting as relay).
     */
    suspend fun storeMessageForOther(recipientId: String, message: P2PMessage): Result<String> {
        return try {
            val storedMessage = StoredMessage(
                id = UUID.randomUUID().toString(),
                recipientId = recipientId,
                encryptedMessage = message,
                storedAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + messageExpiryMs,
                attempts = 0,
                maxAttempts = maxDeliveryAttempts
            )

            storedMessages[storedMessage.id] = storedMessage

            // Add to messages we're storing for others
            messagesForOthers.getOrPut(recipientId) { mutableListOf() }.add(storedMessage)

            Timber.d("Storing message ${storedMessage.id} for peer: $recipientId (relay)")
            Result.success(storedMessage.id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to store message for peer: $recipientId")
            Result.failure(e)
        }
    }

    /**
     * Retrieve stored messages for a recipient (when they come online).
     */
    suspend fun retrieveMessages(recipientId: String): Result<List<StoredMessage>> {
        return try {
            val messages = messagesForOthers[recipientId]?.toList() ?: emptyList()
            Timber.d("Retrieved ${messages.size} stored messages for: $recipientId")
            Result.success(messages)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve messages for: $recipientId")
            Result.failure(e)
        }
    }

    /**
     * Retrieve our own pending messages.
     */
    suspend fun retrieveOwnMessages(): Result<List<StoredMessage>> {
        return try {
            val messages = pendingMessages.values.flatten()
            Timber.d("Retrieved ${messages.size} own pending messages")
            Result.success(messages)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve own messages")
            Result.failure(e)
        }
    }

    /**
     * Mark a message as delivered and remove it.
     */
    suspend fun markAsDelivered(messageId: String): Result<Unit> {
        return try {
            val message = storedMessages.remove(messageId)
            if (message != null) {
                // Remove from other storage locations
                messagesForOthers[message.recipientId]?.removeIf { it.id == messageId }
                pendingMessages[message.recipientId]?.removeIf { it.id == messageId }

                Timber.d("Message $messageId marked as delivered and removed")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to mark message as delivered: $messageId")
            Result.failure(e)
        }
    }

    /**
     * Increment delivery attempt for a message.
     */
    suspend fun incrementAttempt(messageId: String): Result<StoredMessage?> {
        return try {
            val message = storedMessages[messageId]
            if (message != null) {
                val updated = message.copy(attempts = message.attempts + 1)
                storedMessages[messageId] = updated

                // Update in collections
                messagesForOthers[message.recipientId]?.let { list ->
                    val index = list.indexOfFirst { it.id == messageId }
                    if (index != -1) {
                        list[index] = updated
                    }
                }

                pendingMessages[message.recipientId]?.let { list ->
                    val index = list.indexOfFirst { it.id == messageId }
                    if (index != -1) {
                        list[index] = updated
                    }
                }

                // Remove if max attempts reached
                if (updated.attempts >= updated.maxAttempts) {
                    Timber.w("Message $messageId reached max delivery attempts, removing")
                    markAsDelivered(messageId)
                    return Result.success(null)
                }

                Timber.d("Incremented attempt for message $messageId: ${updated.attempts}/${updated.maxAttempts}")
                Result.success(updated)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to increment attempt for message: $messageId")
            Result.failure(e)
        }
    }

    /**
     * Clean up expired messages.
     */
    suspend fun cleanupExpiredMessages(): Result<Int> {
        return try {
            val now = System.currentTimeMillis()
            var removedCount = 0

            val expired = storedMessages.values.filter { it.expiresAt < now }

            expired.forEach { message ->
                storedMessages.remove(message.id)
                messagesForOthers[message.recipientId]?.removeIf { it.id == message.id }
                pendingMessages[message.recipientId]?.removeIf { it.id == message.id }
                removedCount++
            }

            if (removedCount > 0) {
                Timber.d("Cleaned up $removedCount expired messages")
            }

            Result.success(removedCount)
        } catch (e: Exception) {
            Timber.e(e, "Failed to cleanup expired messages")
            Result.failure(e)
        }
    }

    /**
     * Get statistics about stored messages.
     */
    fun getStatistics(): StoreForwardStatistics {
        return StoreForwardStatistics(
            totalStored = storedMessages.size,
            storingForOthers = messagesForOthers.values.sumOf { it.size },
            ownPending = pendingMessages.values.sumOf { it.size },
            uniqueRecipients = (messagesForOthers.keys + pendingMessages.keys).distinct().size
        )
    }

    /**
     * Clear all stored messages (use with caution).
     */
    fun clearAll() {
        storedMessages.clear()
        messagesForOthers.clear()
        pendingMessages.clear()
        Timber.w("All stored messages cleared")
    }
}

/**
 * Statistics about store-and-forward operation.
 */
data class StoreForwardStatistics(
    val totalStored: Int,
    val storingForOthers: Int,
    val ownPending: Int,
    val uniqueRecipients: Int
)
