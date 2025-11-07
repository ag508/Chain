package com.chain.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore-based preferences manager for user settings.
 */
class UserPreferences(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val PHONE_NUMBER = stringPreferencesKey("phone_number")
        private val DISPLAY_NAME = stringPreferencesKey("display_name")
        private val PUBLIC_KEY = stringPreferencesKey("public_key")
        private val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        private val READ_RECEIPTS = booleanPreferencesKey("read_receipts")
        private val ONLINE_STATUS = booleanPreferencesKey("online_status")
    }

    val userId: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_ID]
        }

    val isAuthenticated: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[IS_AUTHENTICATED] ?: false
        }

    val displayName: Flow<String?> = context.dataStore.data
        .map { it[DISPLAY_NAME] }

    val publicKey: Flow<String?> = context.dataStore.data
        .map { it[PUBLIC_KEY] }

    val themeMode: Flow<String> = context.dataStore.data
        .map { it[THEME_MODE] ?: "system" }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[NOTIFICATIONS_ENABLED] ?: true }

    val soundEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[SOUND_ENABLED] ?: true }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[VIBRATION_ENABLED] ?: true }

    val readReceipts: Flow<Boolean> = context.dataStore.data
        .map { it[READ_RECEIPTS] ?: true }

    val onlineStatus: Flow<Boolean> = context.dataStore.data
        .map { it[ONLINE_STATUS] ?: true }

    suspend fun getUserId(): String? {
        return context.dataStore.data.map { it[USER_ID] }.first()
    }

    suspend fun setUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }

    suspend fun saveUserId(userId: String) = setUserId(userId)

    suspend fun savePhoneNumber(phoneNumber: String) {
        context.dataStore.edit { preferences ->
            preferences[PHONE_NUMBER] = phoneNumber
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun isBiometricEnabled(): Boolean {
        return context.dataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }.first()
    }

    suspend fun setDisplayName(displayName: String) {
        context.dataStore.edit { preferences ->
            preferences[DISPLAY_NAME] = displayName
        }
    }

    suspend fun setPublicKey(publicKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PUBLIC_KEY] = publicKey
        }
    }

    suspend fun setAuthenticated(isAuthenticated: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_AUTHENTICATED] = isAuthenticated
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setReadReceipts(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[READ_RECEIPTS] = enabled
        }
    }

    suspend fun setOnlineStatus(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONLINE_STATUS] = enabled
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun clear() = clearAll()
}
