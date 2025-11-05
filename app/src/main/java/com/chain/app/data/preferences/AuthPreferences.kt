package com.chain.app.data.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages authentication state persistence using EncryptedSharedPreferences.
 * Handles user session, auth tokens, and authentication state.
 */
@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "chain_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _isAuthenticated = MutableStateFlow(isUserAuthenticated())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    /**
     * Save user authentication data after successful login/signup.
     */
    fun saveAuthData(
        userId: String,
        phoneNumber: String,
        displayName: String,
        publicKey: String
    ) {
        try {
            prefs.edit().apply {
                putString(KEY_USER_ID, userId)
                putString(KEY_PHONE_NUMBER, phoneNumber)
                putString(KEY_DISPLAY_NAME, displayName)
                putString(KEY_PUBLIC_KEY, publicKey)
                putLong(KEY_AUTH_TIMESTAMP, System.currentTimeMillis())
                putBoolean(KEY_IS_AUTHENTICATED, true)
                apply()
            }
            _isAuthenticated.value = true
            Timber.i("Auth data saved for user: $userId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save auth data")
        }
    }

    /**
     * Check if user is authenticated.
     */
    fun isUserAuthenticated(): Boolean {
        return try {
            prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
        } catch (e: Exception) {
            Timber.e(e, "Failed to check auth status")
            false
        }
    }

    /**
     * Get current user ID.
     */
    fun getUserId(): String? {
        return try {
            prefs.getString(KEY_USER_ID, null)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get user ID")
            null
        }
    }

    /**
     * Get phone number.
     */
    fun getPhoneNumber(): String? {
        return try {
            prefs.getString(KEY_PHONE_NUMBER, null)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get phone number")
            null
        }
    }

    /**
     * Get display name.
     */
    fun getDisplayName(): String? {
        return try {
            prefs.getString(KEY_DISPLAY_NAME, null)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get display name")
            null
        }
    }

    /**
     * Get public key.
     */
    fun getPublicKey(): String? {
        return try {
            prefs.getString(KEY_PUBLIC_KEY, null)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get public key")
            null
        }
    }

    /**
     * Get authentication timestamp.
     */
    fun getAuthTimestamp(): Long {
        return try {
            prefs.getLong(KEY_AUTH_TIMESTAMP, 0L)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get auth timestamp")
            0L
        }
    }

    /**
     * Update display name.
     */
    fun updateDisplayName(displayName: String) {
        try {
            prefs.edit().putString(KEY_DISPLAY_NAME, displayName).apply()
            Timber.d("Display name updated: $displayName")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update display name")
        }
    }

    /**
     * Clear all authentication data (logout).
     */
    fun clearAuthData() {
        try {
            prefs.edit().clear().apply()
            _isAuthenticated.value = false
            Timber.i("Auth data cleared")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear auth data")
        }
    }

    /**
     * Check if auth session is valid (not expired).
     * Sessions expire after 30 days of inactivity.
     */
    fun isSessionValid(): Boolean {
        val authTimestamp = getAuthTimestamp()
        if (authTimestamp == 0L) return false

        val now = System.currentTimeMillis()
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        return (now - authTimestamp) < thirtyDaysInMillis
    }

    /**
     * Update last activity timestamp.
     */
    fun updateLastActivity() {
        try {
            prefs.edit().putLong(KEY_AUTH_TIMESTAMP, System.currentTimeMillis()).apply()
        } catch (e: Exception) {
            Timber.e(e, "Failed to update last activity")
        }
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_PUBLIC_KEY = "public_key"
        private const val KEY_AUTH_TIMESTAMP = "auth_timestamp"
        private const val KEY_IS_AUTHENTICATED = "is_authenticated"
    }
}
