package com.chain.app.domain.usecase.contact

import android.util.Log
import com.chain.app.domain.repository.P2PRepository
import org.json.JSONObject
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Use case to publish user's information to DHT for discovery.
 * This makes the user discoverable by others who have their phone number.
 *
 * @param phoneNumber User's phone number
 * @param userId User's unique ID
 * @param displayName User's display name
 * @param publicKey User's public encryption key
 * @param avatar Optional avatar URL
 */
class PublishUserToDHTUseCase @Inject constructor(
    private val p2pRepository: P2PRepository
) {
    suspend operator fun invoke(
        phoneNumber: String,
        userId: String,
        displayName: String,
        publicKey: String?,
        avatar: String? = null
    ): Result<Unit> {
        return try {
            // Hash the phone number for privacy
            val phoneHash = hashPhoneNumber(phoneNumber)
            Log.d(TAG, "Publishing user to DHT with phone hash: $phoneHash")

            // Create peer info JSON
            val peerInfo = JSONObject().apply {
                put("userId", userId)
                put("displayName", displayName)
                if (publicKey != null) {
                    put("publicKey", publicKey)
                }
                if (avatar != null) {
                    put("avatar", avatar)
                }
            }

            // Publish to DHT
            p2pRepository.publishToDHT(phoneHash, peerInfo.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to publish user to DHT", e)
            Result.failure(e)
        }
    }

    /**
     * Hash phone number using SHA-256.
     * Same algorithm as ContactRepositoryImpl to ensure consistency.
     */
    private fun hashPhoneNumber(phoneNumber: String): String {
        val cleanedPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(cleanedPhone.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val TAG = "PublishUserToDHTUseCase"
    }
}
