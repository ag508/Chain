package com.chain.app.data.local.dao

import androidx.room.*
import com.chain.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for User operations.
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUser(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isContact = 1")
    fun getContacts(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isBlocked = 1")
    fun getBlockedUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE displayName LIKE '%' || :query || '%' OR id LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET status = :status, lastSeen = :lastSeen WHERE id = :userId")
    suspend fun updateUserStatus(userId: String, status: String, lastSeen: Long)

    @Query("UPDATE users SET isContact = :isContact WHERE id = :userId")
    suspend fun setContact(userId: String, isContact: Boolean)

    @Query("UPDATE users SET isBlocked = :isBlocked WHERE id = :userId")
    suspend fun setBlocked(userId: String, isBlocked: Boolean)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
}
