package com.chain.app.presentation.navigation

/**
 * Navigation routes for the Chain app.
 * Defines all screen destinations.
 */
sealed class NavRoutes(val route: String) {
    // Authentication flow
    object Welcome : NavRoutes("welcome")
    object PhoneNumber : NavRoutes("phone_number")
    object OtpVerification : NavRoutes("otp_verification/{phoneNumber}/{email}") {
        fun createRoute(phoneNumber: String, email: String) = "otp_verification/$phoneNumber/$email"
    }
    object ProfileSetup : NavRoutes("profile_setup/{userId}/{phoneNumber}/{email}") {
        fun createRoute(userId: String, phoneNumber: String, email: String) = "profile_setup/$userId/$phoneNumber/$email"
    }
    object BiometricSetup : NavRoutes("biometric_setup")

    // Main app flow
    object ChatList : NavRoutes("chat_list")
    object ChatDetail : NavRoutes("chat_detail/{chatId}") {
        fun createRoute(chatId: String) = "chat_detail/$chatId"
    }
    object Settings : NavRoutes("settings")
    object Profile : NavRoutes("profile")
    object UserProfile : NavRoutes("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }

    // Contacts and Groups
    object ContactSearch : NavRoutes("contact_search")
    object QRCodeScanner : NavRoutes("qr_code_scanner")
    object MyQRCode : NavRoutes("my_qr_code")
    object CreateGroup : NavRoutes("create_group")
    object GroupSetup : NavRoutes("group_setup/{selectedContactIds}") {
        fun createRoute(selectedContactIds: String) = "group_setup/$selectedContactIds"
    }

    // Calls
    object VoiceCall : NavRoutes("voice_call/{peerId}?isIncoming={isIncoming}") {
        fun createRoute(peerId: String, isIncoming: Boolean = false) =
            "voice_call/$peerId?isIncoming=$isIncoming"
    }
    object VideoCall : NavRoutes("video_call/{peerId}?isIncoming={isIncoming}") {
        fun createRoute(peerId: String, isIncoming: Boolean = false) =
            "video_call/$peerId?isIncoming=$isIncoming"
    }

    // Settings screens
    object AccountSettings : NavRoutes("account_settings")
    object BlockedContacts : NavRoutes("blocked_contacts")
    object AutoDownloadMedia : NavRoutes("auto_download_media")
    object ChatBackup : NavRoutes("chat_backup")
    object NetworkUsage : NavRoutes("network_usage")
    object Wallpaper : NavRoutes("wallpaper")
    object FAQ : NavRoutes("faq")
    object Support : NavRoutes("support")
    object Terms : NavRoutes("terms")
    object Privacy : NavRoutes("privacy")
}
