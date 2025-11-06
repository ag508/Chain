package com.chain.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Contact entity for Room database.
 * Stores user's contacts.
 */
@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["phone_number"])
    ]
)
data class ContactEntity(
    @PrimaryKey
    val id: String,

    val user_id: String, // The user ID of the contact

    val phone_number: String,

    val display_name: String,

    val avatar: String? = null,

    val public_key: String? = null,

    val added_at: Long, // Timestamp when contact was added

    val is_blocked: Boolean = false,

    val last_seen: Long? = null
)
