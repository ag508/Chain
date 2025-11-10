package com.chain.app.data.repository

import com.chain.app.data.local.dao.UserDao
import com.chain.app.data.local.entity.toDomain
import com.chain.app.data.local.entity.toEntity
import com.chain.app.data.preferences.UserPreferences
import com.chain.app.domain.model.P2PMessage
import com.chain.app.domain.model.P2PMessageType
import com.chain.app.domain.model.User
import com.chain.app.domain.model.UserStatus
import com.chain.app.domain.repository.P2PRepository
import com.chain.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserRepository.
 * Handles user data operations using Room database and DataStore preferences.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences,
    private val p2pRepository: P2PRepository
) : UserRepository {

    // Track typing status for each chat
    private val typingStatusMap = mutableMapOf<String, MutableStateFlow<Boolean>>()

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val userId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            val userEntity = userDao.getUser(userId)
                ?: return Result.failure(Exception("User not found"))

            Result.success(userEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUser(userId: String): Result<User> {
        return try {
            val userEntity = userDao.getUser(userId)
                ?: return Result.failure(Exception("User not found"))

            Result.success(userEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeUser(userId: String): Flow<User?> {
        return userDao.observeUser(userId).map { entity ->
            entity?.toDomain()
        }
    }

    override fun searchUsers(query: String): Flow<List<User>> {
        return userDao.searchUsers(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateProfile(
        displayName: String?,
        avatar: String?,
        about: String?
    ): Result<User> {
        return try {
            val userId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            val currentUser = userDao.getUser(userId)
                ?: return Result.failure(Exception("User not found"))

            val updatedUser = currentUser.copy(
                displayName = displayName ?: currentUser.displayName,
                avatar = avatar ?: currentUser.avatar,
                about = about ?: currentUser.about
            )

            userDao.updateUser(updatedUser)

            // Update display name in preferences
            if (displayName != null) {
                userPreferences.setDisplayName(displayName)
            }

            Result.success(updatedUser.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStatus(status: UserStatus): Result<Unit> {
        return try {
            val userId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            userDao.updateUserStatus(
                userId = userId,
                status = status.name,
                lastSeen = System.currentTimeMillis()
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getContacts(): Flow<List<User>> {
        return userDao.getContacts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addContact(userId: String): Result<Unit> {
        return try {
            userDao.setContact(userId, true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeContact(userId: String): Result<Unit> {
        return try {
            userDao.setContact(userId, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun blockUser(userId: String): Result<Unit> {
        return try {
            userDao.setBlocked(userId, true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unblockUser(userId: String): Result<Unit> {
        return try {
            userDao.setBlocked(userId, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getBlockedUsers(): Flow<List<com.chain.app.domain.model.User>> {
        return userDao.getBlockedUsers().map { entities: List<UserEntity> ->
            entities.map { entity: UserEntity -> entity.toDomain() }
        }
    }

    override fun observeUserStatus(userId: String): Flow<UserStatus> {
        return userDao.observeUser(userId).map { entity ->
            entity?.let { UserStatus.valueOf(it.status) } ?: UserStatus.OFFLINE
        }
    }

    override suspend fun updatePresence(status: UserStatus): Result<Unit> {
        return try {
            val userId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            // Update local status
            userDao.updateUserStatus(
                userId = userId,
                status = status.name,
                lastSeen = System.currentTimeMillis()
            )

            // Broadcast presence to all contacts via P2P
            val contacts = userDao.getContactsList() // Direct list instead of Flow
            contacts.forEach { contact ->
                try {
                    val presenceMessage = P2PMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        from = userId,
                        to = contact.id,
                        encryptedPayload = status.name.toByteArray(),
                        timestamp = System.currentTimeMillis(),
                        type = P2PMessageType.PRESENCE_UPDATE,
                        signature = ByteArray(0)
                    )
                    p2pRepository.sendMessage(presenceMessage)
                } catch (e: Exception) {
                    Timber.w("Failed to send presence to ${contact.id}: ${e.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendTypingIndicator(chatId: String, isTyping: Boolean): Result<Unit> {
        return try {
            val userId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            // Send typing indicator via P2P
            val typingMessage = P2PMessage(
                id = java.util.UUID.randomUUID().toString(),
                from = userId,
                to = chatId,
                encryptedPayload = isTyping.toString().toByteArray(),
                timestamp = System.currentTimeMillis(),
                type = P2PMessageType.TYPING_INDICATOR,
                signature = ByteArray(0)
            )
            p2pRepository.sendMessage(typingMessage)

            Timber.d("Typing indicator sent to $chatId: $isTyping")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeTypingStatus(chatId: String): Flow<Boolean> {
        return typingStatusMap.getOrPut(chatId) {
            MutableStateFlow(false)
        }
    }

    override suspend fun updateLastSeen(): Result<Unit> {
        return try {
            val userId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            userDao.updateUserStatus(
                userId = userId,
                status = UserStatus.OFFLINE.name,
                lastSeen = System.currentTimeMillis()
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Handle incoming typing indicator.
     * Called by MessageRepositoryImpl when TYPING_INDICATOR message is received.
     */
    fun handleTypingIndicator(chatId: String, isTyping: Boolean) {
        typingStatusMap.getOrPut(chatId) {
            MutableStateFlow(false)
        }.value = isTyping
    }

    /**
     * Handle incoming presence update.
     * Called by MessageRepositoryImpl when PRESENCE_UPDATE message is received.
     */
    suspend fun handlePresenceUpdate(userId: String, status: UserStatus) {
        try {
            userDao.updateUserStatus(
                userId = userId,
                status = status.name,
                lastSeen = System.currentTimeMillis()
            )
            Timber.d("Updated presence for $userId: $status")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update presence for $userId")
        }
    }
}
