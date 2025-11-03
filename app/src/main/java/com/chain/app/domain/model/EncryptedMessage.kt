package com.chain.app.domain.model

/**
 * Encrypted message for Signal Protocol transmission.
 */
data class EncryptedMessage(
    val content: String,
    val type: MessageType,
    val keyId: String,
    val timestamp: Long
)

/**
 * Pre-key bundle for Signal Protocol key exchange.
 */
data class PreKeyBundle(
    val registrationId: Int,
    val deviceId: Int,
    val preKeyId: Int,
    val preKeyPublic: ByteArray,
    val signedPreKeyId: Int,
    val signedPreKeyPublic: ByteArray,
    val signedPreKeySignature: ByteArray,
    val identityKey: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PreKeyBundle

        if (registrationId != other.registrationId) return false
        if (deviceId != other.deviceId) return false
        if (preKeyId != other.preKeyId) return false
        if (!preKeyPublic.contentEquals(other.preKeyPublic)) return false
        if (signedPreKeyId != other.signedPreKeyId) return false
        if (!signedPreKeyPublic.contentEquals(other.signedPreKeyPublic)) return false
        if (!signedPreKeySignature.contentEquals(other.signedPreKeySignature)) return false
        if (!identityKey.contentEquals(other.identityKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = registrationId
        result = 31 * result + deviceId
        result = 31 * result + preKeyId
        result = 31 * result + preKeyPublic.contentHashCode()
        result = 31 * result + signedPreKeyId
        result = 31 * result + signedPreKeyPublic.contentHashCode()
        result = 31 * result + signedPreKeySignature.contentHashCode()
        result = 31 * result + identityKey.contentHashCode()
        return result
    }
}

/**
 * Encrypted link for cloud storage media sharing.
 */
data class EncryptedLink(
    val url: String,
    val encryptionKey: String,
    val service: CloudService,
    val expiresAt: Long
)

enum class CloudService {
    GOOGLE_DRIVE,
    ONE_DRIVE,
    ICLOUD,
    DROPBOX
}
