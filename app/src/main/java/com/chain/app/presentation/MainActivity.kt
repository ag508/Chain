package com.chain.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.chain.app.presentation.auth.biometric.BiometricSetupScreen
import com.chain.app.presentation.auth.otp.OtpScreen
import com.chain.app.presentation.auth.phone.PhoneNumberScreen
import com.chain.app.presentation.auth.profile.ProfileSetupScreen
import com.chain.app.presentation.auth.welcome.WelcomeScreen
import com.chain.app.presentation.chat.ChatScreen
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
                },
                onSignInClick = {
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
                    navController.navigate(NavRoutes.ProfileSetup.route) {
                        // Clear back stack up to welcome
                        popUpTo(NavRoutes.Welcome.route) { inclusive = false }
                    }
                }
            )
        }

        // Profile setup screen
        composable(NavRoutes.ProfileSetup.route) {
            // Get userId and phoneNumber from previous screen
            val previousEntry = navController.previousBackStackEntry
            val userId = previousEntry?.savedStateHandle?.get<String>("userId") ?: "temp_user"
            val phoneNumber = previousEntry?.savedStateHandle?.get<String>("phoneNumber") ?: ""

            ProfileSetupScreen(
                userId = userId,
                phoneNumber = phoneNumber,
                onProfileCreated = {
                    navController.navigate(NavRoutes.BiometricSetup.route)
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

        // Chat list screen (placeholder)
        composable(NavRoutes.ChatList.route) {
            ChatScreen()
        }
    }
}

/**
 * ViewModel to check authentication status.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _isAuthenticated.value = authRepository.isAuthenticated()
        }
    }
}
