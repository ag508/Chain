package com.chain.app.domain.model

import java.util.Date

/**
 * User domain model representing a Chain user identity.
 */
data class User(
    val id: String,
    val phoneNumber: String,
    val email: String? = null,
    val displayName: String,
    val avatar: String? = null,
    val publicKey: String? = null,
    val status: UserStatus = UserStatus.OFFLINE,
    val lastSeen: Date = Date(),
    val devices: List<Device> = emptyList()
)

/**
 * Device information for multi-device support.
 */
data class Device(
    val id: String,
    val name: String,
    val platform: DevicePlatform,
    val lastActive: Date
)

enum class UserStatus {
    ONLINE,
    OFFLINE,
    AWAY,
    DO_NOT_DISTURB
}

enum class DevicePlatform {
    ANDROID,
    IOS,
    WEB,
    DESKTOP,
    DOCKER
}
