package com.chain.app.data.repository

import com.chain.app.data.local.dao.ContactDao
import com.chain.app.data.local.entity.ContactEntity
import com.chain.app.domain.model.Contact
import com.chain.app.domain.model.UserStatus
import com.chain.app.domain.repository.ContactRepository
import com.chain.app.domain.repository.P2PRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        // First check if contact already exists locally
        val existingContact = contactDao.getContactByPhoneNumber(phoneNumber)
        if (existingContact != null) {
            return existingContact.toDomainModel()
        }

        // TODO: Search for contact via P2P network using DHT
        // 1. Hash the phone number
        // 2. Query DHT for peer with that hash
        // 3. If found, create a Contact object with the peer's info
        // For now, return null (not found)

        // Example implementation when P2P discovery is ready:
        // val phoneHash = hashPhoneNumber(phoneNumber)
        // val peerInfo = p2pRepository.findPeerByKey(phoneHash)
        // if (peerInfo != null) {
        //     return Contact(
        //         id = UUID.randomUUID().toString(),
        //         userId = peerInfo.id,
        //         phoneNumber = phoneNumber,
        //         displayName = peerInfo.displayName ?: "Unknown",
        //         avatar = null,
        //         publicKey = peerInfo.publicKey,
        //         addedAt = Date(),
        //         isBlocked = false
        //     )
        // }

        return null
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
