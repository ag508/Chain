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

        // Auto-download media preferences
        private val MOBILE_DATA_PHOTOS = booleanPreferencesKey("mobile_data_photos")
        private val MOBILE_DATA_VIDEOS = booleanPreferencesKey("mobile_data_videos")
        private val MOBILE_DATA_DOCUMENTS = booleanPreferencesKey("mobile_data_documents")
        private val WIFI_PHOTOS = booleanPreferencesKey("wifi_photos")
        private val WIFI_VIDEOS = booleanPreferencesKey("wifi_videos")
        private val WIFI_DOCUMENTS = booleanPreferencesKey("wifi_documents")
        private val ROAMING_PHOTOS = booleanPreferencesKey("roaming_photos")
        private val ROAMING_VIDEOS = booleanPreferencesKey("roaming_videos")
        private val ROAMING_DOCUMENTS = booleanPreferencesKey("roaming_documents")

        // Chat backup preferences
        private val LAST_BACKUP_DATE = longPreferencesKey("last_backup_date")
        private val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        private val BACKUP_FREQUENCY = stringPreferencesKey("backup_frequency")
        private val BACKUP_INCLUDE_VIDEOS = booleanPreferencesKey("backup_include_videos")
        private val BACKUP_OVER_CELLULAR = booleanPreferencesKey("backup_over_cellular")

        // Network usage preferences
        private val TOTAL_BYTES_SENT = longPreferencesKey("total_bytes_sent")
        private val TOTAL_BYTES_RECEIVED = longPreferencesKey("total_bytes_received")
        private val MESSAGES_BYTES_SENT = longPreferencesKey("messages_bytes_sent")
        private val MESSAGES_BYTES_RECEIVED = longPreferencesKey("messages_bytes_received")
        private val PHOTOS_BYTES_SENT = longPreferencesKey("photos_bytes_sent")
        private val PHOTOS_BYTES_RECEIVED = longPreferencesKey("photos_bytes_received")
        private val VIDEOS_BYTES_SENT = longPreferencesKey("videos_bytes_sent")
        private val VIDEOS_BYTES_RECEIVED = longPreferencesKey("videos_bytes_received")
        private val DOCUMENTS_BYTES_SENT = longPreferencesKey("documents_bytes_sent")
        private val DOCUMENTS_BYTES_RECEIVED = longPreferencesKey("documents_bytes_received")
        private val USAGE_RESET_DATE = longPreferencesKey("usage_reset_date")

        // Wallpaper preference
        private val SELECTED_WALLPAPER = stringPreferencesKey("selected_wallpaper")
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

    // Auto-download media flows
    fun getMobileDataPhotos(): Flow<Boolean> = context.dataStore.data
        .map { it[MOBILE_DATA_PHOTOS] ?: false }

    fun getMobileDataVideos(): Flow<Boolean> = context.dataStore.data
        .map { it[MOBILE_DATA_VIDEOS] ?: false }

    fun getMobileDataDocuments(): Flow<Boolean> = context.dataStore.data
        .map { it[MOBILE_DATA_DOCUMENTS] ?: false }

    fun getWifiPhotos(): Flow<Boolean> = context.dataStore.data
        .map { it[WIFI_PHOTOS] ?: true }

    fun getWifiVideos(): Flow<Boolean> = context.dataStore.data
        .map { it[WIFI_VIDEOS] ?: true }

    fun getWifiDocuments(): Flow<Boolean> = context.dataStore.data
        .map { it[WIFI_DOCUMENTS] ?: true }

    fun getRoamingPhotos(): Flow<Boolean> = context.dataStore.data
        .map { it[ROAMING_PHOTOS] ?: false }

    fun getRoamingVideos(): Flow<Boolean> = context.dataStore.data
        .map { it[ROAMING_VIDEOS] ?: false }

    fun getRoamingDocuments(): Flow<Boolean> = context.dataStore.data
        .map { it[ROAMING_DOCUMENTS] ?: false }

    // Chat backup flows
    fun getLastBackupDate(): Flow<Long?> = context.dataStore.data
        .map { it[LAST_BACKUP_DATE] }

    fun getAutoBackupEnabled(): Flow<Boolean> = context.dataStore.data
        .map { it[AUTO_BACKUP_ENABLED] ?: false }

    fun getBackupFrequency(): Flow<String> = context.dataStore.data
        .map { it[BACKUP_FREQUENCY] ?: "daily" }

    fun getBackupIncludeVideos(): Flow<Boolean> = context.dataStore.data
        .map { it[BACKUP_INCLUDE_VIDEOS] ?: true }

    fun getBackupOverCellular(): Flow<Boolean> = context.dataStore.data
        .map { it[BACKUP_OVER_CELLULAR] ?: false }

    // Network usage flows
    fun getTotalBytesSent(): Flow<Long> = context.dataStore.data
        .map { it[TOTAL_BYTES_SENT] ?: 0L }

    fun getTotalBytesReceived(): Flow<Long> = context.dataStore.data
        .map { it[TOTAL_BYTES_RECEIVED] ?: 0L }

    fun getMessagesBytesSent(): Flow<Long> = context.dataStore.data
        .map { it[MESSAGES_BYTES_SENT] ?: 0L }

    fun getMessagesBytesReceived(): Flow<Long> = context.dataStore.data
        .map { it[MESSAGES_BYTES_RECEIVED] ?: 0L }

    fun getPhotosBytesSent(): Flow<Long> = context.dataStore.data
        .map { it[PHOTOS_BYTES_SENT] ?: 0L }

    fun getPhotosBytesReceived(): Flow<Long> = context.dataStore.data
        .map { it[PHOTOS_BYTES_RECEIVED] ?: 0L }

    fun getVideosBytesSent(): Flow<Long> = context.dataStore.data
        .map { it[VIDEOS_BYTES_SENT] ?: 0L }

    fun getVideosBytesReceived(): Flow<Long> = context.dataStore.data
        .map { it[VIDEOS_BYTES_RECEIVED] ?: 0L }

    fun getDocumentsBytesSent(): Flow<Long> = context.dataStore.data
        .map { it[DOCUMENTS_BYTES_SENT] ?: 0L }

    fun getDocumentsBytesReceived(): Flow<Long> = context.dataStore.data
        .map { it[DOCUMENTS_BYTES_RECEIVED] ?: 0L }

    fun getUsageResetDate(): Flow<Long?> = context.dataStore.data
        .map { it[USAGE_RESET_DATE] }

    // Wallpaper flow
    fun getSelectedWallpaper(): Flow<String> = context.dataStore.data
        .map { it[SELECTED_WALLPAPER] ?: "gradient_default" }

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

    // Auto-download media setters
    suspend fun setMobileDataPhotos(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MOBILE_DATA_PHOTOS] = enabled
        }
    }

    suspend fun setMobileDataVideos(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MOBILE_DATA_VIDEOS] = enabled
        }
    }

    suspend fun setMobileDataDocuments(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MOBILE_DATA_DOCUMENTS] = enabled
        }
    }

    suspend fun setWifiPhotos(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_PHOTOS] = enabled
        }
    }

    suspend fun setWifiVideos(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_VIDEOS] = enabled
        }
    }

    suspend fun setWifiDocuments(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_DOCUMENTS] = enabled
        }
    }

    suspend fun setRoamingPhotos(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ROAMING_PHOTOS] = enabled
        }
    }

    suspend fun setRoamingVideos(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ROAMING_VIDEOS] = enabled
        }
    }

    suspend fun setRoamingDocuments(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ROAMING_DOCUMENTS] = enabled
        }
    }

    // Chat backup setters
    suspend fun setLastBackupDate(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_BACKUP_DATE] = timestamp
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setBackupFrequency(frequency: String) {
        context.dataStore.edit { preferences ->
            preferences[BACKUP_FREQUENCY] = frequency
        }
    }

    suspend fun setBackupIncludeVideos(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BACKUP_INCLUDE_VIDEOS] = enabled
        }
    }

    suspend fun setBackupOverCellular(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BACKUP_OVER_CELLULAR] = enabled
        }
    }

    // Network usage setters
    suspend fun incrementBytesSent(bytes: Long, type: String) {
        context.dataStore.edit { preferences ->
            val currentTotal = preferences[TOTAL_BYTES_SENT] ?: 0L
            preferences[TOTAL_BYTES_SENT] = currentTotal + bytes

            when (type.lowercase()) {
                "message" -> {
                    val current = preferences[MESSAGES_BYTES_SENT] ?: 0L
                    preferences[MESSAGES_BYTES_SENT] = current + bytes
                }
                "photo" -> {
                    val current = preferences[PHOTOS_BYTES_SENT] ?: 0L
                    preferences[PHOTOS_BYTES_SENT] = current + bytes
                }
                "video" -> {
                    val current = preferences[VIDEOS_BYTES_SENT] ?: 0L
                    preferences[VIDEOS_BYTES_SENT] = current + bytes
                }
                "document" -> {
                    val current = preferences[DOCUMENTS_BYTES_SENT] ?: 0L
                    preferences[DOCUMENTS_BYTES_SENT] = current + bytes
                }
            }
        }
    }

    suspend fun incrementBytesReceived(bytes: Long, type: String) {
        context.dataStore.edit { preferences ->
            val currentTotal = preferences[TOTAL_BYTES_RECEIVED] ?: 0L
            preferences[TOTAL_BYTES_RECEIVED] = currentTotal + bytes

            when (type.lowercase()) {
                "message" -> {
                    val current = preferences[MESSAGES_BYTES_RECEIVED] ?: 0L
                    preferences[MESSAGES_BYTES_RECEIVED] = current + bytes
                }
                "photo" -> {
                    val current = preferences[PHOTOS_BYTES_RECEIVED] ?: 0L
                    preferences[PHOTOS_BYTES_RECEIVED] = current + bytes
                }
                "video" -> {
                    val current = preferences[VIDEOS_BYTES_RECEIVED] ?: 0L
                    preferences[VIDEOS_BYTES_RECEIVED] = current + bytes
                }
                "document" -> {
                    val current = preferences[DOCUMENTS_BYTES_RECEIVED] ?: 0L
                    preferences[DOCUMENTS_BYTES_RECEIVED] = current + bytes
                }
            }
        }
    }

    suspend fun resetNetworkUsage() {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_BYTES_SENT] = 0L
            preferences[TOTAL_BYTES_RECEIVED] = 0L
            preferences[MESSAGES_BYTES_SENT] = 0L
            preferences[MESSAGES_BYTES_RECEIVED] = 0L
            preferences[PHOTOS_BYTES_SENT] = 0L
            preferences[PHOTOS_BYTES_RECEIVED] = 0L
            preferences[VIDEOS_BYTES_SENT] = 0L
            preferences[VIDEOS_BYTES_RECEIVED] = 0L
            preferences[DOCUMENTS_BYTES_SENT] = 0L
            preferences[DOCUMENTS_BYTES_RECEIVED] = 0L
            preferences[USAGE_RESET_DATE] = System.currentTimeMillis()
        }
    }

    // Wallpaper setter
    suspend fun setSelectedWallpaper(wallpaperId: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_WALLPAPER] = wallpaperId
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun clear() = clearAll()
}
