package com.chain.app.data.local.dao

import androidx.room.*
import com.chain.app.data.local.entity.CallEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Call operations.
 */
@Dao
interface CallDao {
    @Query("SELECT * FROM calls ORDER BY startTime DESC")
    fun getAllCalls(): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE chatId = :chatId ORDER BY startTime DESC")
    fun getCallsForChat(chatId: String): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE id = :callId")
    suspend fun getCall(callId: String): CallEntity?

    @Query("SELECT * FROM calls WHERE id = :callId")
    fun observeCall(callId: String): Flow<CallEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallEntity)

    @Update
    suspend fun updateCall(call: CallEntity)

    @Query("UPDATE calls SET status = :status WHERE id = :callId")
    suspend fun updateCallStatus(callId: String, status: String)

    @Query("UPDATE calls SET endTime = :endTime, duration = :duration WHERE id = :callId")
    suspend fun endCall(callId: String, endTime: Long, duration: Long)

    @Delete
    suspend fun deleteCall(call: CallEntity)

    @Query("DELETE FROM calls WHERE id = :callId")
    suspend fun deleteCallById(callId: String)

    @Query("DELETE FROM calls WHERE chatId = :chatId")
    suspend fun deleteCallsForChat(chatId: String)
}
