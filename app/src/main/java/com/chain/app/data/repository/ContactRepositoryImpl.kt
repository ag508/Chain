package com.chain.app.data.repository

import android.util.Log
import com.chain.app.data.local.dao.ContactDao
import com.chain.app.data.local.entity.ContactEntity
import com.chain.app.domain.model.Contact
import com.chain.app.domain.model.UserStatus
import com.chain.app.domain.repository.ContactRepository
import com.chain.app.domain.repository.P2PRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ContactRepository.
 * Handles contact storage and P2P discovery.
 */
@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    private val p2pRepository: P2PRepository
) : ContactRepository {

    override fun getAllContacts(): Flow<List<Contact>> {
        return contactDao.getAllContacts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getContactById(contactId: String): Contact? {
        return contactDao.getContactById(contactId)?.toDomainModel()
    }

    override suspend fun getContactByUserId(userId: String): Contact? {
        return contactDao.getContactByUserId(userId)?.toDomainModel()
    }

    override suspend fun searchContactByPhone(phoneNumber: String): Contact? {
        Log.d(TAG, "Searching for contact with phone: $phoneNumber")

        // First check if contact already exists locally
        val existingContact = contactDao.getContactByPhoneNumber(phoneNumber)
        if (existingContact != null) {
            Log.d(TAG, "Found existing contact locally")
            return existingContact.toDomainModel()
        }

        // Search for contact via P2P network using DHT
        return try {
            // 1. Hash the phone number for DHT lookup
            val phoneHash = hashPhoneNumber(phoneNumber)
            Log.d(TAG, "Hashed phone number: $phoneHash")

            // 2. Query DHT for peer info with that hash
            val result = p2pRepository.lookupInDHT(phoneHash)
            result.fold(
                onSuccess = { peerInfoJson ->
                    if (peerInfoJson != null) {
                        Log.d(TAG, "Found peer info in DHT: $peerInfoJson")
                        // 3. Parse peer info and create Contact object
                        parsePeerInfoToContact(peerInfoJson, phoneNumber)
                    } else {
                        Log.d(TAG, "No peer info found in DHT for phone: $phoneNumber")
                        null
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to lookup in DHT", error)
                    null
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during P2P contact search", e)
            null
        }
    }

    /**
     * Hash phone number using SHA-256 for DHT lookup.
     * This provides privacy - the actual phone number is not stored in the DHT.
     */
    private fun hashPhoneNumber(phoneNumber: String): String {
        val cleanedPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(cleanedPhone.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Parse peer info JSON from DHT into a Contact object.
     * Expected JSON format:
     * {
     *   "userId": "peer_id",
     *   "displayName": "John Doe",
     *   "publicKey": "base64_encoded_public_key",
     *   "avatar": "url_or_null"
     * }
     */
    private fun parsePeerInfoToContact(peerInfoJson: String, phoneNumber: String): Contact? {
        return try {
            val json = JSONObject(peerInfoJson)
            Contact(
                id = UUID.randomUUID().toString(),
                userId = json.getString("userId"),
                phoneNumber = phoneNumber,
                displayName = json.optString("displayName", "Unknown User"),
                avatar = json.optString("avatar").takeIf { it.isNotEmpty() },
                publicKey = json.optString("publicKey").takeIf { it.isNotEmpty() },
                addedAt = Date(),
                isBlocked = false,
                lastSeen = null,
                status = UserStatus.OFFLINE // Will be updated when peer connects
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse peer info JSON", e)
            null
        }
    }

    companion object {
        private const val TAG = "ContactRepositoryImpl"
    }

    override suspend fun addContact(contact: Contact): Result<Unit> {
        return try {
            val entity = contact.toEntity()
            contactDao.insertContact(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteContact(contactId: String): Result<Unit> {
        return try {
            contactDao.deleteContact(contactId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun blockContact(contactId: String): Result<Unit> {
        return try {
            contactDao.blockContact(contactId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unblockContact(contactId: String): Result<Unit> {
        return try {
            contactDao.unblockContact(contactId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getBlockedContacts(): Flow<List<Contact>> {
        return contactDao.getBlockedContacts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun searchContacts(query: String): Flow<List<Contact>> {
        return contactDao.searchContacts(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateLastSeen(userId: String, lastSeen: Long): Result<Unit> {
        return try {
            contactDao.updateLastSeen(userId, lastSeen)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extension function to convert ContactEntity to domain Contact model.
     */
    private fun ContactEntity.toDomainModel(): Contact {
        return Contact(
            id = id,
            userId = user_id,
            phoneNumber = phone_number,
            displayName = display_name,
            avatar = avatar,
            publicKey = public_key,
            addedAt = Date(added_at),
            isBlocked = is_blocked,
            lastSeen = last_seen?.let { Date(it) },
            status = UserStatus.OFFLINE // TODO: Get real status from P2P network
        )
    }

    /**
     * Extension function to convert domain Contact to ContactEntity.
     */
    private fun Contact.toEntity(): ContactEntity {
        return ContactEntity(
            id = id,
            user_id = userId,
            phone_number = phoneNumber,
            display_name = displayName,
            avatar = avatar,
            public_key = publicKey,
            added_at = addedAt.time,
            is_blocked = isBlocked,
            last_seen = lastSeen?.time
        )
    }
}
