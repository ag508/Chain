package com.chain.app.data.local.dao

import androidx.room.*
import com.chain.app.data.local.entity.ReactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Reaction operations.
 */
@Dao
interface ReactionDao {
    @Query("SELECT * FROM reactions WHERE messageId = :messageId")
    fun getReactions(messageId: String): Flow<List<ReactionEntity>>

    @Query("SELECT * FROM reactions WHERE messageId = :messageId AND userId = :userId")
    suspend fun getUserReaction(messageId: String, userId: String): ReactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaction(reaction: ReactionEntity)

    @Delete
    suspend fun deleteReaction(reaction: ReactionEntity)

    @Query("DELETE FROM reactions WHERE messageId = :messageId AND emoji = :emoji AND userId = :userId")
    suspend fun deleteReaction(messageId: String, emoji: String, userId: String)

    @Query("DELETE FROM reactions WHERE messageId = :messageId")
    suspend fun deleteAllReactions(messageId: String)

    @Query("DELETE FROM reactions WHERE messageId IN (SELECT id FROM messages WHERE chatId = :chatId)")
    suspend fun deleteReactionsForChat(chatId: String)
}
