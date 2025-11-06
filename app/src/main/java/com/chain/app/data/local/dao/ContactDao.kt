package com.chain.app.data.local.dao

import androidx.room.*
import com.chain.app.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for contact operations.
 */
@Dao
interface ContactDao {
    /**
     * Insert or replace a contact.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    /**
     * Insert multiple contacts.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    /**
     * Get all contacts.
     */
    @Query("SELECT * FROM contacts WHERE is_blocked = 0 ORDER BY display_name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    /**
     * Get a contact by ID.
     */
    @Query("SELECT * FROM contacts WHERE id = :contactId LIMIT 1")
    suspend fun getContactById(contactId: String): ContactEntity?

    /**
     * Get a contact by user ID.
     */
    @Query("SELECT * FROM contacts WHERE user_id = :userId LIMIT 1")
    suspend fun getContactByUserId(userId: String): ContactEntity?

    /**
     * Get a contact by phone number.
     */
    @Query("SELECT * FROM contacts WHERE phone_number = :phoneNumber LIMIT 1")
    suspend fun getContactByPhoneNumber(phoneNumber: String): ContactEntity?

    /**
     * Search contacts by name or phone number.
     */
    @Query("""
        SELECT * FROM contacts
        WHERE (display_name LIKE '%' || :query || '%'
            OR phone_number LIKE '%' || :query || '%')
            AND is_blocked = 0
        ORDER BY display_name ASC
    """)
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    /**
     * Get blocked contacts.
     */
    @Query("SELECT * FROM contacts WHERE is_blocked = 1 ORDER BY display_name ASC")
    fun getBlockedContacts(): Flow<List<ContactEntity>>

    /**
     * Block a contact.
     */
    @Query("UPDATE contacts SET is_blocked = 1 WHERE id = :contactId")
    suspend fun blockContact(contactId: String)

    /**
     * Unblock a contact.
     */
    @Query("UPDATE contacts SET is_blocked = 0 WHERE id = :contactId")
    suspend fun unblockContact(contactId: String)

    /**
     * Delete a contact.
     */
    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)

    /**
     * Delete all contacts.
     */
    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()

    /**
     * Update contact last seen.
     */
    @Query("UPDATE contacts SET last_seen = :lastSeen WHERE user_id = :userId")
    suspend fun updateLastSeen(userId: String, lastSeen: Long)
}
