package com.chain.app.domain.repository

import com.chain.app.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 */
interface AuthRepository {
    /**
     * Check if user is authenticated.
     */
    suspend fun isAuthenticated(): Boolean

    /**
     * Get current authenticated user's ID.
     * Returns null if no user is authenticated.
     */
    suspend fun getCurrentUserId(): String?

    /**
     * Send OTP to the given phone number and email.
     */
    suspend fun sendOtp(phoneNumber: String, email: String): Result<Unit>

    /**
     * Verify OTP code for the given phone number.
     */
    suspend fun verifyOtp(phoneNumber: String, otp: String): Result<String> // Returns user ID

    /**
     * Create user profile after verification.
     */
    suspend fun createUserProfile(
        userId: String,
        phoneNumber: String,
        email: String,
        displayName: String,
        avatar: String? = null
    ): Result<User>

    /**
     * Initialize Signal Protocol keys for the user.
     */
    suspend fun initializeEncryption(): Result<Unit>

    /**
     * Enable biometric authentication.
     */
    suspend fun enableBiometric(): Result<Unit>

    /**
     * Disable biometric authentication.
     */
    suspend fun disableBiometric(): Result<Unit>

    /**
     * Check if biometric is enabled.
     */
    suspend fun isBiometricEnabled(): Boolean

    /**
     * Authenticate with biometric.
     */
    suspend fun authenticateWithBiometric(): Result<Unit>

    /**
     * Logout user and clear all data.
     */
    suspend fun logout(): Result<Unit>

    /**
     * Observe authentication state changes.
     */
    fun observeAuthState(): Flow<AuthState>
}

/**
 * Authentication state.
 */
sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
