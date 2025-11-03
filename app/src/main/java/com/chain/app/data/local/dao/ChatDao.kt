package com.chain.app.data.local.dao

import androidx.room.*
import com.chain.app.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Chat operations.
 */
@Dao
interface ChatDao {
    @Query("SELECT * FROM chats WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChat(chatId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun observeChat(chatId: String): Flow<ChatEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("UPDATE chats SET unreadCount = :count WHERE id = :chatId")
    suspend fun updateUnreadCount(chatId: String, count: Int)

    @Query("UPDATE chats SET isPinned = :isPinned WHERE id = :chatId")
    suspend fun setPinned(chatId: String, isPinned: Boolean)

    @Query("UPDATE chats SET isArchived = :isArchived WHERE id = :chatId")
    suspend fun setArchived(chatId: String, isArchived: Boolean)

    @Query("UPDATE chats SET isMuted = :isMuted WHERE id = :chatId")
    suspend fun setMuted(chatId: String, isMuted: Boolean)

    @Query("UPDATE chats SET updatedAt = :timestamp WHERE id = :chatId")
    suspend fun updateTimestamp(chatId: String, timestamp: Long)

    @Delete
    suspend fun deleteChat(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: String)

    @Query("SELECT * FROM chats WHERE type = 'DIRECT' AND :userId IN (SELECT value FROM json_each(participants))")
    suspend fun getDirectChatWithUser(userId: String): ChatEntity?
}
