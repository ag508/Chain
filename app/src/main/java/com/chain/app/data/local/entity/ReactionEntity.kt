package com.chain.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chain.app.domain.model.Reaction
import java.util.Date

/**
 * Room entity for message reactions.
 */
@Entity(
    tableName = "reactions",
    primaryKeys = ["messageId", "emoji", "userId"]
)
data class ReactionEntity(
    val messageId: String,
    val emoji: String,
    val userId: String,
    val timestamp: Long
)

/**
 * Convert ReactionEntity to domain Reaction model.
 */
fun ReactionEntity.toDomain(): Reaction = Reaction(
    emoji = emoji,
    userId = userId,
    timestamp = Date(timestamp)
)

/**
 * Convert domain Reaction to ReactionEntity.
 */
fun Reaction.toEntity(messageId: String): ReactionEntity = ReactionEntity(
    messageId = messageId,
    emoji = emoji,
    userId = userId,
    timestamp = timestamp.time
)
