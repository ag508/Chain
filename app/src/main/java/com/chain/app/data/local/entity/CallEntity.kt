package com.chain.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.chain.app.data.local.converter.StringListConverter
import com.chain.app.domain.model.Call
import com.chain.app.domain.model.CallStatus
import com.chain.app.domain.model.CallType
import java.util.Date

/**
 * Room entity for Call history.
 */
@Entity(tableName = "calls")
@TypeConverters(StringListConverter::class)
data class CallEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val initiator: String,
    val participants: List<String>,
    val type: String,
    val status: String,
    val startTime: Long,
    val endTime: Long?,
    val duration: Long?
)

/**
 * Convert CallEntity to domain Call model.
 */
fun CallEntity.toDomain(): Call = Call(
    id = id,
    chatId = chatId,
    initiator = initiator,
    participants = participants,
    type = CallType.valueOf(type),
    status = CallStatus.valueOf(status),
    startTime = Date(startTime),
    endTime = endTime?.let { Date(it) },
    duration = duration
)

/**
 * Convert domain Call to CallEntity.
 */
fun Call.toEntity(): CallEntity = CallEntity(
    id = id,
    chatId = chatId,
    initiator = initiator,
    participants = participants,
    type = type.name,
    status = status.name,
    startTime = startTime.time,
    endTime = endTime?.time,
    duration = duration
)
