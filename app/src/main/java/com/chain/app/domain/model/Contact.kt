package com.chain.app.domain.model

import java.util.Date

/**
 * Contact domain model.
 * Represents a user's contact in the app.
 */
data class Contact(
    val id: String,
    val userId: String, // The user ID of the contact
    val phoneNumber: String,
    val displayName: String,
    val avatar: String? = null,
    val publicKey: String? = null,
    val addedAt: Date,
    val isBlocked: Boolean = false,
    val lastSeen: Date? = null,
    val status: UserStatus = UserStatus.OFFLINE
)
