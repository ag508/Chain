package com.chain.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chain.app.domain.repository.AuthRepository
import com.chain.app.domain.usecase.auth.GetCurrentUserIdUseCase
import com.chain.app.domain.usecase.debug.SeedSampleDataUseCase
import com.chain.app.presentation.auth.biometric.BiometricSetupScreen
import com.chain.app.presentation.auth.otp.OtpScreen
import com.chain.app.presentation.auth.phone.PhoneNumberScreen
import com.chain.app.presentation.auth.profile.ProfileSetupScreen
import com.chain.app.presentation.auth.welcome.WelcomeScreen
import com.chain.app.presentation.chat.ChatScreen
import com.chain.app.presentation.chat.detail.ChatDetailScreen
import com.chain.app.presentation.navigation.NavRoutes
import com.chain.app.presentation.theme.ChainTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main activity for Chain messaging platform.
 * Hosts the Compose UI and handles navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display to fill the entire screen
        enableEdgeToEdge()

        setContent {
            ChainTheme {
                // Remove Surface to allow screens to control their own backgrounds edge-to-edge
                ChainApp()
            }
        }
    }
}

@Composable
fun ChainApp(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isAuthenticated by mainViewModel.isAuthenticated.collectAsState()

    // Determine start destination
    val startDestination = if (isAuthenticated) {
        NavRoutes.ChatList.route
    } else {
        NavRoutes.Welcome.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Welcome screen
        composable(NavRoutes.Welcome.route) {
            WelcomeScreen(
                onGetStartedClick = {
                    navController.navigate(NavRoutes.PhoneNumber.route)
                }
            )
        }

        // Phone number screen
        composable(NavRoutes.PhoneNumber.route) {
            PhoneNumberScreen(
                onBackClick = { navController.popBackStack() },
                onOtpSent = { phoneNumber, email ->
                    navController.navigate(NavRoutes.OtpVerification.createRoute(phoneNumber, email))
                }
            )
        }

        // OTP verification screen
        composable(
            route = NavRoutes.OtpVerification.route,
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OtpScreen(
                phoneNumber = phoneNumber,
                email = email,
                onBackClick = { navController.popBackStack() },
                onVerified = { userId ->
                    navController.navigate(NavRoutes.ProfileSetup.createRoute(userId, phoneNumber, email)) {
                        // Clear back stack up to welcome
                        popUpTo(NavRoutes.Welcome.route) { inclusive = false }
                    }
                }
            )
        }

        // Profile setup screen
        composable(
            route = NavRoutes.ProfileSetup.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "temp_user"
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""

            ProfileSetupScreen(
                userId = userId,
                phoneNumber = phoneNumber,
                email = email,
                onProfileCreated = {
                    println("DEBUG MainActivity: onProfileCreated callback triggered, navigating to BiometricSetup")
                    navController.navigate(NavRoutes.BiometricSetup.route) {
                        // Don't clear back stack yet - allow back navigation if needed
                        popUpTo(NavRoutes.ProfileSetup.route) { inclusive = true }
                    }
                    println("DEBUG MainActivity: Navigation to BiometricSetup completed")
                }
            )
        }

        // Biometric setup screen
        composable(NavRoutes.BiometricSetup.route) {
            BiometricSetupScreen(
                onSetupComplete = {
                    navController.navigate(NavRoutes.ChatList.route) {
                        // Clear entire back stack
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Chat list screen
        composable(NavRoutes.ChatList.route) {
            ChatScreen(
                onChatClick = { chat ->
                    navController.navigate(NavRoutes.ChatDetail.createRoute(chat.id))
                },
                onNewChatClick = {
                    navController.navigate(NavRoutes.ContactSearch.route)
                },
                onProfileClick = {
                    navController.navigate(NavRoutes.Profile.route)
                },
                onSettingsClick = {
                    navController.navigate(NavRoutes.Settings.route)
                },
                onLogoutClick = {
                    mainViewModel.logout()
                    navController.navigate(NavRoutes.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCreateGroupClick = {
                    navController.navigate(NavRoutes.CreateGroup.route)
                },
                onScanQRCodeClick = {
                    navController.navigate(NavRoutes.QRCodeScanner.route)
                },
                onVoiceCallClick = { peerId ->
                    navController.navigate(NavRoutes.VoiceCall.createRoute(peerId, false))
                },
                onVideoCallClick = { peerId ->
                    navController.navigate(NavRoutes.VideoCall.createRoute(peerId, false))
                }
            )
        }

        // Chat detail screen
        composable(
            route = NavRoutes.ChatDetail.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val currentUserId by mainViewModel.currentUserId.collectAsState()

            ChatDetailScreen(
                chatId = chatId,
                currentUserId = currentUserId ?: "unknown",
                onBackClick = { navController.popBackStack() },
                onVoiceCallClick = {
                    navController.navigate(NavRoutes.VoiceCall.createRoute(chatId, isIncoming = false))
                },
                onVideoCallClick = {
                    navController.navigate(NavRoutes.VideoCall.createRoute(chatId, isIncoming = false))
                },
                onViewProfileClick = { userId ->
                    navController.navigate(NavRoutes.UserProfile.createRoute(userId))
                }
            )
        }

        // Contact search screen
        composable(NavRoutes.ContactSearch.route) {
            com.chain.app.presentation.contacts.ContactSearchScreen(
                onBackClick = { navController.popBackStack() },
                onContactSelected = { phoneNumber ->
                    // TODO: Create chat with contact and navigate to it
                    navController.popBackStack()
                },
                onQRCodeClick = {
                    navController.navigate(NavRoutes.QRCodeScanner.route)
                }
            )
        }

        // QR code scanner screen
        composable(NavRoutes.QRCodeScanner.route) {
            com.chain.app.presentation.contacts.QRCodeScannerScreen(
                onBackClick = { navController.popBackStack() },
                onQRCodeScanned = { qrData ->
                    // Parse QR code and navigate to contact search to add
                    val parsedData = com.chain.app.presentation.contacts.QRCodeGenerator.parseUserQRData(qrData)
                    if (parsedData != null) {
                        val phoneNumber = parsedData["phone"]
                        if (phoneNumber != null) {
                            // Navigate back and show success message
                            navController.popBackStack()
                            // The contact will be added via ContactSearchScreen or AddContactDialog
                        }
                    }
                    navController.popBackStack()
                },
                onShowMyQRCode = {
                    navController.navigate(NavRoutes.MyQRCode.route)
                }
            )
        }

        // My QR code screen
        composable(NavRoutes.MyQRCode.route) {
            com.chain.app.presentation.contacts.MyQRCodeScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Profile screen
        composable(NavRoutes.Profile.route) {
            com.chain.app.presentation.profile.ProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // User profile screen (for viewing other users)
        composable(
            route = NavRoutes.UserProfile.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            com.chain.app.presentation.profile.UserProfileScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onSendMessageClick = {
                    // TODO: Navigate to or create chat with this user
                    navController.popBackStack()
                },
                onVoiceCallClick = {
                    navController.navigate(NavRoutes.VoiceCall.createRoute(userId, isIncoming = false))
                },
                onVideoCallClick = {
                    navController.navigate(NavRoutes.VideoCall.createRoute(userId, isIncoming = false))
                },
                onBlockClick = {
                    // TODO: Implement block user functionality
                    navController.popBackStack()
                }
            )
        }

        // Settings screen
        composable(NavRoutes.Settings.route) {
            com.chain.app.presentation.settings.SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onAccountSettingsClick = {
                    navController.navigate(NavRoutes.AccountSettings.route)
                },
                onPrivacySettingsClick = { /* TODO: Navigate to privacy settings */ },
                onNotificationSettingsClick = { /* TODO: Navigate to notification settings */ },
                onBlockedContactsClick = {
                    navController.navigate(NavRoutes.BlockedContacts.route)
                },
                onAutoDownloadMediaClick = {
                    navController.navigate(NavRoutes.AutoDownloadMedia.route)
                },
                onChatBackupClick = {
                    navController.navigate(NavRoutes.ChatBackup.route)
                },
                onNetworkUsageClick = {
                    navController.navigate(NavRoutes.NetworkUsage.route)
                },
                onWallpaperClick = {
                    navController.navigate(NavRoutes.Wallpaper.route)
                },
                onFAQClick = {
                    navController.navigate(NavRoutes.FAQ.route)
                },
                onSupportClick = {
                    navController.navigate(NavRoutes.Support.route)
                },
                onTermsClick = {
                    navController.navigate(NavRoutes.Terms.route)
                },
                onPrivacyPolicyClick = {
                    navController.navigate(NavRoutes.Privacy.route)
                },
                onLogout = {
                    // Navigate to welcome screen and clear back stack
                    navController.navigate(NavRoutes.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Account Settings screen
        composable(NavRoutes.AccountSettings.route) {
            com.chain.app.presentation.settings.account.AccountSettingsScreen(
                onBackClick = { navController.popBackStack() },
                onPrivacyClick = { /* TODO: Navigate to privacy settings */ },
                onSecurityClick = { /* TODO: Navigate to security settings */ }
            )
        }

        // Blocked Contacts screen
        composable(NavRoutes.BlockedContacts.route) {
            com.chain.app.presentation.settings.blocked.BlockedContactsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Auto-Download Media screen
        composable(NavRoutes.AutoDownloadMedia.route) {
            com.chain.app.presentation.settings.media.AutoDownloadMediaScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Chat Backup screen
        composable(NavRoutes.ChatBackup.route) {
            com.chain.app.presentation.settings.backup.ChatBackupScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Network Usage screen
        composable(NavRoutes.NetworkUsage.route) {
            com.chain.app.presentation.settings.network.NetworkUsageScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Wallpaper screen
        composable(NavRoutes.Wallpaper.route) {
            com.chain.app.presentation.settings.wallpaper.WallpaperSelectorScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // FAQ screen
        composable(NavRoutes.FAQ.route) {
            com.chain.app.presentation.settings.info.FAQScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Support screen
        composable(NavRoutes.Support.route) {
            com.chain.app.presentation.settings.info.SupportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Terms of Service screen
        composable(NavRoutes.Terms.route) {
            com.chain.app.presentation.settings.info.TermsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Privacy Policy screen
        composable(NavRoutes.Privacy.route) {
            com.chain.app.presentation.settings.info.PrivacyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Create group screen
        composable(NavRoutes.CreateGroup.route) {
            com.chain.app.presentation.groups.CreateGroupScreen(
                onBackClick = { navController.popBackStack() },
                onNextClick = { selectedContactIds ->
                    val contactIdsString = selectedContactIds.joinToString(",")
                    navController.navigate(NavRoutes.GroupSetup.createRoute(contactIdsString))
                }
            )
        }

        // Group setup screen
        composable(
            route = NavRoutes.GroupSetup.route,
            arguments = listOf(
                navArgument("selectedContactIds") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val selectedContactIdsString = backStackEntry.arguments?.getString("selectedContactIds") ?: ""
            val selectedContactIds = selectedContactIdsString.split(",").filter { it.isNotEmpty() }

            com.chain.app.presentation.groups.GroupSetupScreen(
                selectedContactIds = selectedContactIds,
                onBackClick = { navController.popBackStack() },
                onCreateGroup = { groupName, groupIcon ->
                    // Group is created in the ViewModel, callback is only for navigation
                    // Navigate back to chat list where the new group will appear
                    navController.popBackStack(NavRoutes.ChatList.route, inclusive = false)
                }
            )
        }

        // Voice call screen
        composable(
            route = NavRoutes.VoiceCall.route,
            arguments = listOf(
                navArgument("peerId") { type = NavType.StringType },
                navArgument("isIncoming") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val peerId = backStackEntry.arguments?.getString("peerId")
            val isIncoming = backStackEntry.arguments?.getBoolean("isIncoming") ?: false

            com.chain.app.presentation.call.VoiceCallScreen(
                peerId = peerId,
                isIncoming = isIncoming,
                onCallEnded = {
                    navController.popBackStack()
                }
            )
        }

        // Video call screen
        composable(
            route = NavRoutes.VideoCall.route,
            arguments = listOf(
                navArgument("peerId") { type = NavType.StringType },
                navArgument("isIncoming") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val peerId = backStackEntry.arguments?.getString("peerId")
            val isIncoming = backStackEntry.arguments?.getBoolean("isIncoming") ?: false

            com.chain.app.presentation.call.VideoCallScreen(
                peerId = peerId,
                isIncoming = isIncoming,
                onCallEnded = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * ViewModel to check authentication status and manage user session.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val seedSampleDataUseCase: SeedSampleDataUseCase
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()

    init {
        checkAuthStatus()
        loadCurrentUserId()
        seedSampleData()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _isAuthenticated.value = authRepository.isAuthenticated()
        }
    }

    private fun loadCurrentUserId() {
        viewModelScope.launch {
            _currentUserId.value = getCurrentUserIdUseCase()
        }
    }

    private fun seedSampleData() {
        viewModelScope.launch {
            val userId = getCurrentUserIdUseCase()
            if (userId != null) {
                // Seed sample data for testing (only runs once)
                seedSampleDataUseCase(userId)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isAuthenticated.value = false
            _currentUserId.value = null
        }
    }
}
