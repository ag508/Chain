package com.chain.app.domain.repository

import com.chain.app.domain.model.Contact
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for contact operations.
 */
interface ContactRepository {
    /**
     * Get all contacts.
     */
    fun getAllContacts(): Flow<List<Contact>>

    /**
     * Get a contact by ID.
     */
    suspend fun getContactById(contactId: String): Contact?

    /**
     * Get a contact by user ID.
     */
    suspend fun getContactByUserId(userId: String): Contact?

    /**
     * Search for a contact by phone number via P2P network.
     * Returns null if no user with that phone number exists.
     */
    suspend fun searchContactByPhone(phoneNumber: String): Contact?

    /**
     * Add a contact to the user's contact list.
     */
    suspend fun addContact(contact: Contact): Result<Unit>

    /**
     * Delete a contact.
     */
    suspend fun deleteContact(contactId: String): Result<Unit>

    /**
     * Block a contact.
     */
    suspend fun blockContact(contactId: String): Result<Unit>

    /**
     * Unblock a contact.
     */
    suspend fun unblockContact(contactId: String): Result<Unit>

    /**
     * Get blocked contacts.
     */
    fun getBlockedContacts(): Flow<List<Contact>>

    /**
     * Search contacts by name or phone number.
     */
    fun searchContacts(query: String): Flow<List<Contact>>

    /**
     * Update contact's last seen timestamp.
     */
    suspend fun updateLastSeen(userId: String, lastSeen: Long): Result<Unit>
}
