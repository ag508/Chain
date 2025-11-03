package com.chain.app.data.local.dao

import androidx.room.*
import com.chain.app.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Message operations.
 */
@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getMessages(chatId: String, limit: Int, offset: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessage(messageId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(chatId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE chatId = :chatId")
    fun observeMessages(chatId: String): Flow<List<MessageEntity>>

    @Query("""
        SELECT messages.* FROM messages
        INNER JOIN messages_fts ON messages.rowid = messages_fts.rowid
        WHERE messages_fts.content MATCH :query
        ORDER BY timestamp DESC
    """)
    fun searchMessages(query: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id IN (:messageIds)")
    suspend fun deleteMessages(messageIds: List<String>)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)

    @Query("DELETE FROM messages WHERE disappearAfter IS NOT NULL AND timestamp + disappearAfter < :currentTime")
    suspend fun deleteExpiredMessages(currentTime: Long)

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId AND status != 'READ' AND senderId != :currentUserId")
    fun getUnreadCount(chatId: String, currentUserId: String): Flow<Int>
}
