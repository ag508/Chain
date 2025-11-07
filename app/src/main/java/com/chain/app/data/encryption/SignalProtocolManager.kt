package com.chain.app.data.encryption

import android.content.Context
import com.chain.app.domain.model.EncryptedMessage
import com.chain.app.domain.model.MessageType
import com.chain.app.domain.model.PreKeyBundle
import dagger.hilt.android.qualifiers.ApplicationContext
import org.signal.libsignal.protocol.*
import org.signal.libsignal.protocol.message.CiphertextMessage
import org.signal.libsignal.protocol.message.PreKeySignalMessage
import org.signal.libsignal.protocol.message.SignalMessage
import org.signal.libsignal.protocol.state.PreKeyBundle as SignalPreKeyBundle
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.SignedPreKeyRecord
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for Signal Protocol encryption and decryption operations.
 * Implements the Signal Protocol's Double Ratchet algorithm for end-to-end encryption.
 */
@Singleton
class SignalProtocolManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val signalProtocolStore: SignalProtocolStore
) {

    /**
     * Initialize a new session with a recipient using their pre-key bundle.
     */
    suspend fun initializeSession(recipientId: String, preKeyBundle: PreKeyBundle): Result<Unit> {
        return try {
            val signalAddress = SignalProtocolAddress(recipientId, 1)

            // Convert our PreKeyBundle to Signal's PreKeyBundle
            val signalBundle = SignalPreKeyBundle(
                preKeyBundle.registrationId,
                preKeyBundle.deviceId,
                preKeyBundle.preKeyId,
                org.signal.libsignal.protocol.ecc.Curve.decodePoint(preKeyBundle.preKeyPublic, 0),
                preKeyBundle.signedPreKeyId,
                org.signal.libsignal.protocol.ecc.Curve.decodePoint(preKeyBundle.signedPreKeyPublic, 0),
                preKeyBundle.signedPreKeySignature,
                org.signal.libsignal.protocol.IdentityKey(preKeyBundle.identityKey, 0)
            )

            // Process the pre-key bundle and establish session
            val sessionBuilder = SessionBuilder(signalProtocolStore, signalAddress)
            sessionBuilder.process(signalBundle)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Encrypt a message for a specific recipient.
     */
    suspend fun encryptMessage(plaintext: String, recipientId: String): Result<EncryptedMessage> {
        return try {
            val signalAddress = SignalProtocolAddress(recipientId, 1)
            val sessionCipher = SessionCipher(signalProtocolStore, signalAddress)

            val ciphertextMessage = sessionCipher.encrypt(plaintext.toByteArray())
            val encryptedContent = android.util.Base64.encodeToString(
                ciphertextMessage.serialize(),
                android.util.Base64.NO_WRAP
            )

            val encrypted = EncryptedMessage(
                content = encryptedContent,
                type = MessageType.TEXT,
                keyId = "key_${System.currentTimeMillis()}",
                timestamp = System.currentTimeMillis()
            )

            Result.success(encrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Decrypt a message from a specific sender.
     */
    suspend fun decryptMessage(ciphertext: EncryptedMessage, senderId: String): Result<String> {
        return try {
            val signalAddress = SignalProtocolAddress(senderId, 1)
            val sessionCipher = SessionCipher(signalProtocolStore, signalAddress)

            val encryptedData = android.util.Base64.decode(ciphertext.content, android.util.Base64.NO_WRAP)

            // Determine message type and decrypt accordingly
            val plaintext = if (encryptedData[0] == 3.toByte()) {
                // PreKeySignalMessage
                val preKeyMessage = PreKeySignalMessage(encryptedData)
                sessionCipher.decrypt(preKeyMessage)
            } else {
                // SignalMessage
                val signalMessage = SignalMessage(encryptedData)
                sessionCipher.decrypt(signalMessage)
            }

            Result.success(plaintext.decodeToString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a pre-key bundle for this device to share with others.
     */
    suspend fun generatePreKeyBundle(): Result<PreKeyBundle> {
        return try {
            val identityKeyPair = signalProtocolStore.identityKeyPair
            val registrationId = signalProtocolStore.localRegistrationId
            val deviceId = 1

            // Generate pre-key
            val preKeyId = ((Math.random() * 16777216).toInt())
            val preKeyPair = org.signal.libsignal.protocol.ecc.Curve.generateKeyPair()
            val preKeyRecord = PreKeyRecord(preKeyId, preKeyPair)
            signalProtocolStore.storePreKey(preKeyId, preKeyRecord)

            // Generate signed pre-key
            val signedPreKeyId = ((Math.random() * 16777216).toInt())
            val signedPreKeyPair = org.signal.libsignal.protocol.ecc.Curve.generateKeyPair()
            val signedPreKeySignature = org.signal.libsignal.protocol.ecc.Curve.calculateSignature(
                identityKeyPair.privateKey,
                signedPreKeyPair.publicKey.serialize()
            )
            val signedPreKeyRecord = SignedPreKeyRecord(
                signedPreKeyId,
                System.currentTimeMillis(),
                signedPreKeyPair,
                signedPreKeySignature
            )
            signalProtocolStore.storeSignedPreKey(signedPreKeyId, signedPreKeyRecord)

            val bundle = PreKeyBundle(
                registrationId = registrationId,
                deviceId = deviceId,
                preKeyId = preKeyId,
                preKeyPublic = preKeyPair.publicKey.serialize(),
                signedPreKeyId = signedPreKeyId,
                signedPreKeyPublic = signedPreKeyPair.publicKey.serialize(),
                signedPreKeySignature = signedPreKeySignature,
                identityKey = identityKeyPair.publicKey.serialize()
            )

            Result.success(bundle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get safety number for verifying identity with another user.
     */
    suspend fun getSafetyNumber(userId: String): Result<String> {
        return try {
            val signalAddress = SignalProtocolAddress(userId, 1)
            val theirIdentityKey = signalProtocolStore.getIdentity(signalAddress)
            val ourIdentityKey = signalProtocolStore.identityKeyPair.publicKey

            // Generate fingerprint
            val version = 0
            val ourFingerprint = ourIdentityKey.serialize()
            val theirFingerprint = theirIdentityKey?.serialize() ?: ByteArray(0)

            // Combine fingerprints to create safety number
            val combined = ourFingerprint + theirFingerprint
            val safetyNumber = combined.joinToString("") { "%02x".format(it) }
                .take(60) // Take first 60 characters
                .chunked(5) // Group into blocks of 5
                .joinToString(" ")

            Result.success(safetyNumber)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify safety number matches the expected value.
     */
    suspend fun verifySafetyNumber(userId: String, expectedSafetyNumber: String): Result<Boolean> {
        return try {
            val actualSafetyNumber = getSafetyNumber(userId).getOrThrow()
            Result.success(actualSafetyNumber == expectedSafetyNumber)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Rotate encryption keys for enhanced security.
     */
    suspend fun rotateKeys(): Result<Unit> {
        return try {
            // Generate new pre-keys
            generatePreKeyBundle()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign a message payload with the device's private key.
     */
    suspend fun signMessage(payload: ByteArray): Result<ByteArray> {
        return try {
            val identityKeyPair = signalProtocolStore.identityKeyPair
            val signature = org.signal.libsignal.protocol.ecc.Curve.calculateSignature(
                identityKeyPair.privateKey,
                payload
            )
            Result.success(signature)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify a message signature using the sender's public key.
     */
    suspend fun verifySignature(payload: ByteArray, signature: ByteArray, senderId: String): Result<Boolean> {
        return try {
            val signalAddress = SignalProtocolAddress(senderId, 1)
            val senderIdentityKey = signalProtocolStore.getIdentity(signalAddress)

            if (senderIdentityKey == null) {
                // No identity key stored for sender - cannot verify
                return Result.success(false)
            }

            val isValid = org.signal.libsignal.protocol.ecc.Curve.verifySignature(
                senderIdentityKey.publicKey,
                payload,
                signature
            )
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
