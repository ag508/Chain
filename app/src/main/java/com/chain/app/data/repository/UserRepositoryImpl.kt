package com.chain.app.data.repository

import com.chain.app.data.local.dao.UserDao
import com.chain.app.data.local.entity.toDomain
import com.chain.app.data.local.entity.toEntity
import com.chain.app.data.preferences.UserPreferences
import com.chain.app.domain.model.User
import com.chain.app.domain.model.UserStatus
import com.chain.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserRepository.
 * Handles user data operations using Room database and DataStore preferences.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) : UserRepository {

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

    override fun searchUsers(query: String): Flow<List<User>> {
        return userDao.searchUsers(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateProfile(
        displayName: String?,
        avatar: String?
    ): Result<User> {
        return try {
            val userId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            val currentUser = userDao.getUser(userId)
                ?: return Result.failure(Exception("User not found"))

            val updatedUser = currentUser.copy(
                displayName = displayName ?: currentUser.displayName,
                avatar = avatar ?: currentUser.avatar
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

    override fun observeUserStatus(userId: String): Flow<UserStatus> {
        return userDao.observeUser(userId).map { entity ->
            entity?.let { UserStatus.valueOf(it.status) } ?: UserStatus.OFFLINE
        }
    }
}
