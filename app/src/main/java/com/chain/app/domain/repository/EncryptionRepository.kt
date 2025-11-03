package com.chain.app.domain.repository

import com.chain.app.domain.model.EncryptedMessage
import com.chain.app.domain.model.PreKeyBundle

/**
 * Repository interface for encryption operations using Signal Protocol.
 */
interface EncryptionRepository {
    /**
     * Initialize encryption session with a recipient.
     */
    suspend fun initializeSession(recipientId: String, preKeyBundle: PreKeyBundle): Result<Unit>

    /**
     * Encrypt a message for a recipient.
     */
    suspend fun encryptMessage(plaintext: String, recipientId: String): Result<EncryptedMessage>

    /**
     * Decrypt a message from a sender.
     */
    suspend fun decryptMessage(ciphertext: EncryptedMessage, senderId: String): Result<String>

    /**
     * Generate pre-key bundle for this device.
     */
    suspend fun generatePreKeyBundle(): Result<PreKeyBundle>

    /**
     * Rotate encryption keys.
     */
    suspend fun rotateKeys(): Result<Unit>

    /**
     * Verify safety number with a user.
     */
    suspend fun verifySafetyNumber(userId: String, safetyNumber: String): Result<Boolean>

    /**
     * Get safety number for a user.
     */
    suspend fun getSafetyNumber(userId: String): Result<String>

    /**
     * Initialize group encryption.
     */
    suspend fun initializeGroupEncryption(groupId: String, members: List<String>): Result<Unit>

    /**
     * Encrypt group message.
     */
    suspend fun encryptGroupMessage(plaintext: String, groupId: String): Result<EncryptedMessage>

    /**
     * Decrypt group message.
     */
    suspend fun decryptGroupMessage(ciphertext: EncryptedMessage, groupId: String, senderId: String): Result<String>
}
