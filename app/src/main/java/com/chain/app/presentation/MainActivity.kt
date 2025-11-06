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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChainApp()
                }
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
                onOtpSent = { phoneNumber ->
                    navController.navigate(NavRoutes.OtpVerification.createRoute(phoneNumber))
                }
            )
        }

        // OTP verification screen
        composable(
            route = NavRoutes.OtpVerification.route,
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpScreen(
                phoneNumber = phoneNumber,
                onBackClick = { navController.popBackStack() },
                onVerified = { userId ->
                    navController.navigate(NavRoutes.ProfileSetup.createRoute(userId, phoneNumber)) {
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
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "temp_user"
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""

            ProfileSetupScreen(
                userId = userId,
                phoneNumber = phoneNumber,
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
                    navController.navigate(NavRoutes.VoiceCall.createRoute(peerId, false))
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
                    // TODO: Implement video call screen
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
                    // TODO: Process QR code and add contact
                    navController.popBackStack()
                },
                onShowMyQRCode = {
                    // TODO: Show user's QR code
                }
            )
        }

        // Profile screen
        composable(NavRoutes.Profile.route) {
            com.chain.app.presentation.profile.ProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Settings screen
        composable(NavRoutes.Settings.route) {
            com.chain.app.presentation.settings.SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onAccountSettingsClick = { /* TODO: Navigate to account settings */ },
                onPrivacySettingsClick = { /* TODO: Navigate to privacy settings */ },
                onNotificationSettingsClick = { /* TODO: Navigate to notification settings */ }
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
