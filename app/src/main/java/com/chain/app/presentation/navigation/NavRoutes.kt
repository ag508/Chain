package com.chain.app.presentation.navigation

/**
 * Navigation routes for the Chain app.
 * Defines all screen destinations.
 */
sealed class NavRoutes(val route: String) {
    // Authentication flow
    object Welcome : NavRoutes("welcome")
    object PhoneNumber : NavRoutes("phone_number")
    object OtpVerification : NavRoutes("otp_verification/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp_verification/$phoneNumber"
    }
    object ProfileSetup : NavRoutes("profile_setup/{userId}/{phoneNumber}") {
        fun createRoute(userId: String, phoneNumber: String) = "profile_setup/$userId/$phoneNumber"
    }
    object BiometricSetup : NavRoutes("biometric_setup")

    // Main app flow
    object ChatList : NavRoutes("chat_list")
    object ChatDetail : NavRoutes("chat_detail/{chatId}") {
        fun createRoute(chatId: String) = "chat_detail/$chatId"
    }
    object Settings : NavRoutes("settings")
    object Profile : NavRoutes("profile")
}
