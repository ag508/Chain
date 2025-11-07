package com.chain.app.domain.repository

import com.chain.app.domain.model.User
import com.chain.app.domain.model.UserStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user operations.
 */
interface UserRepository {
    /**
     * Get the current authenticated user.
     */
    suspend fun getCurrentUser(): Result<User>

    /**
     * Get a user by ID.
     */
    suspend fun getUser(userId: String): Result<User>

    /**
     * Observe a user by ID with real-time updates.
     */
    fun observeUser(userId: String): Flow<User?>

    /**
     * Search users by name or ID.
     */
    fun searchUsers(query: String): Flow<List<User>>

    /**
     * Update user profile.
     */
    suspend fun updateProfile(
        displayName: String? = null,
        avatar: String? = null,
        about: String? = null
    ): Result<User>

    /**
     * Update user status.
     */
    suspend fun updateStatus(status: UserStatus): Result<Unit>

    /**
     * Get user contacts.
     */
    fun getContacts(): Flow<List<User>>

    /**
     * Add a contact.
     */
    suspend fun addContact(userId: String): Result<Unit>

    /**
     * Remove a contact.
     */
    suspend fun removeContact(userId: String): Result<Unit>

    /**
     * Block a user.
     */
    suspend fun blockUser(userId: String): Result<Unit>

    /**
     * Unblock a user.
     */
    suspend fun unblockUser(userId: String): Result<Unit>

    /**
     * Observe user status changes.
     */
    fun observeUserStatus(userId: String): Flow<UserStatus>

    /**
     * Update current user's presence status and broadcast to contacts.
     */
    suspend fun updatePresence(status: UserStatus): Result<Unit>

    /**
     * Send typing indicator to a specific chat.
     */
    suspend fun sendTypingIndicator(chatId: String, isTyping: Boolean): Result<Unit>

    /**
     * Observe typing status for a chat.
     */
    fun observeTypingStatus(chatId: String): Flow<Boolean>

    /**
     * Update last seen timestamp.
     */
    suspend fun updateLastSeen(): Result<Unit>
}
