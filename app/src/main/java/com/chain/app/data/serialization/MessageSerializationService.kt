package com.chain.app.data.serialization

import com.chain.app.domain.model.P2PMessage
import com.chain.app.domain.model.P2PMessageType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for serializing/deserializing P2P messages.
 * Uses Kotlin Serialization with JSON for efficient and compact message encoding.
 */
@Singleton
class MessageSerializationService @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Serialize P2P message to ByteArray.
     */
    fun serialize(message: P2PMessage): ByteArray {
        return try {
            val serializable = SerializableP2PMessage(
                id = message.id,
                from = message.from,
                to = message.to,
                encryptedPayload = message.encryptedPayload.toList(),
                timestamp = message.timestamp,
                type = message.type.name,
                signature = message.signature.toList()
            )

            val jsonString = json.encodeToString(serializable)
            jsonString.toByteArray()
        } catch (e: Exception) {
            Timber.e(e, "Failed to serialize P2P message")
            throw e
        }
    }

    /**
     * Deserialize ByteArray to P2P message.
     */
    fun deserialize(data: ByteArray): P2PMessage {
        return try {
            val jsonString = String(data)
            val serializable = json.decodeFromString<SerializableP2PMessage>(jsonString)

            P2PMessage(
                id = serializable.id,
                from = serializable.from,
                to = serializable.to,
                encryptedPayload = serializable.encryptedPayload.toByteArray(),
                timestamp = serializable.timestamp,
                type = P2PMessageType.valueOf(serializable.type),
                signature = serializable.signature.toByteArray()
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to deserialize P2P message")
            throw e
        }
    }

    /**
     * Serializable version of P2PMessage.
     * ByteArray is converted to List<Byte> for JSON serialization.
     */
    @Serializable
    private data class SerializableP2PMessage(
        val id: String,
        val from: String,
        val to: String,
        val encryptedPayload: List<Byte>,
        val timestamp: Long,
        val type: String,
        val signature: List<Byte>
    )
}
