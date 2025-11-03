package com.chain.app.data.repository

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.chain.app.data.encryption.SignalProtocolManager
import com.chain.app.data.local.dao.UserDao
import com.chain.app.data.local.entity.UserEntity
import com.chain.app.data.preferences.UserPreferences
import com.chain.app.domain.model.User
import com.chain.app.domain.model.UserStatus
import com.chain.app.domain.repository.AuthRepository
import com.chain.app.domain.repository.AuthState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository.
 * Handles authentication, user creation, and encryption initialization.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences,
    private val signalProtocolManager: SignalProtocolManager
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)

    // In-memory OTP storage for demo (in production, this would be handled by backend)
    private val otpStorage = mutableMapOf<String, String>()

    override suspend fun isAuthenticated(): Boolean {
        return userPreferences.getUserId() != null
    }

    override suspend fun sendOtp(phoneNumber: String): Result<Unit> {
        return try {
            // Simulate API call delay
            delay(1000)

            // In a real app, this would call a backend API to send SMS
            // For demo purposes, we'll generate a random OTP and store it
            val otp = generateOtp()
            otpStorage[phoneNumber] = otp

            // In development, log the OTP (remove in production!)
            println("DEBUG: OTP for $phoneNumber is $otp")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(phoneNumber: String, otp: String): Result<String> {
        return try {
            // Simulate API call delay
            delay(500)

            // In a real app, this would verify with backend
            val storedOtp = otpStorage[phoneNumber]

            if (storedOtp == null) {
                return Result.failure(Exception("OTP not found. Please request a new OTP."))
            }

            if (storedOtp != otp) {
                return Result.failure(Exception("Invalid OTP. Please try again."))
            }

            // OTP verified successfully, generate user ID
            val userId = UUID.randomUUID().toString()

            // Clear the OTP
            otpStorage.remove(phoneNumber)

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUserProfile(
        userId: String,
        phoneNumber: String,
        displayName: String,
        avatar: String?
    ): Result<User> {
        return try {
            // Create user entity
            val userEntity = UserEntity(
                id = userId,
                phoneNumber = phoneNumber,
                displayName = displayName,
                avatar = avatar,
                publicKey = null, // Will be set during encryption initialization
                status = UserStatus.AVAILABLE.name,
                lastSeen = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )

            // Save to database
            userDao.insertUser(userEntity)

            // Save user ID to preferences
            userPreferences.saveUserId(userId)
            userPreferences.savePhoneNumber(phoneNumber)

            // Update auth state
            _authState.value = AuthState.Authenticated

            // Convert to domain model
            val user = User(
                id = userId,
                phoneNumber = phoneNumber,
                displayName = displayName,
                avatar = avatar,
                publicKey = null,
                status = UserStatus.AVAILABLE,
                lastSeen = System.currentTimeMillis(),
                devices = emptyList()
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun initializeEncryption(): Result<Unit> {
        return try {
            val userId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            // Generate Signal Protocol pre-key bundle
            val preKeyBundleResult = signalProtocolManager.generatePreKeyBundle()

            if (preKeyBundleResult.isFailure) {
                return Result.failure(
                    preKeyBundleResult.exceptionOrNull()
                        ?: Exception("Failed to generate encryption keys")
                )
            }

            val preKeyBundle = preKeyBundleResult.getOrNull()!!

            // Update user with public key
            val userEntity = userDao.getUserById(userId)
            if (userEntity != null) {
                val updatedUser = userEntity.copy(
                    publicKey = android.util.Base64.encodeToString(
                        preKeyBundle.identityKey,
                        android.util.Base64.NO_WRAP
                    )
                )
                userDao.updateUser(updatedUser)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enableBiometric(): Result<Unit> {
        return try {
            val biometricManager = BiometricManager.from(context)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    userPreferences.setBiometricEnabled(true)
                    Result.success(Unit)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    Result.failure(Exception("No biometric hardware available"))
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    Result.failure(Exception("Biometric hardware is currently unavailable"))
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    Result.failure(Exception("No biometric credentials enrolled"))
                else ->
                    Result.failure(Exception("Biometric authentication not available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun disableBiometric(): Result<Unit> {
        return try {
            userPreferences.setBiometricEnabled(false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isBiometricEnabled(): Boolean {
        return userPreferences.isBiometricEnabled()
    }

    override suspend fun authenticateWithBiometric(): Result<Unit> {
        // This method is typically called from UI with BiometricPrompt
        // The actual authentication is handled in the UI layer
        return if (isBiometricEnabled()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Biometric authentication is not enabled"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val userId = userPreferences.getUserId()

            // Clear user preferences
            userPreferences.clear()

            // Update auth state
            _authState.value = AuthState.Unauthenticated

            // Note: We don't delete user from database for data recovery purposes
            // In a production app, you might want to sync data before logout

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAuthState(): Flow<AuthState> {
        return _authState.asStateFlow()
    }

    // Helper methods

    private fun generateOtp(): String {
        return (100000..999999).random().toString()
    }
}
