package com.chain.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppSettings(
    val biometricEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val themeMode: String = "system", // "light", "dark", "system"
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val readReceipts: Boolean = true,
    val onlineStatus: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val authRepository: com.chain.app.domain.repository.AuthRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val biometric = userPreferences.isBiometricEnabled()
            userPreferences.notificationsEnabled.collect { notifs ->
                userPreferences.themeMode.collect { theme ->
                    userPreferences.soundEnabled.collect { sound ->
                        userPreferences.vibrationEnabled.collect { vibration ->
                            userPreferences.readReceipts.collect { readReceipts ->
                                userPreferences.onlineStatus.collect { onlineStatus ->
                                    _settings.value = AppSettings(
                                        biometricEnabled = biometric,
                                        notificationsEnabled = notifs,
                                        themeMode = theme,
                                        soundEnabled = sound,
                                        vibrationEnabled = vibration,
                                        readReceipts = readReceipts,
                                        onlineStatus = onlineStatus
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setBiometricEnabled(enabled)
            _settings.value = _settings.value.copy(biometricEnabled = enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
            _settings.value = _settings.value.copy(notificationsEnabled = enabled)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
            _settings.value = _settings.value.copy(themeMode = mode)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setSoundEnabled(enabled)
            _settings.value = _settings.value.copy(soundEnabled = enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setVibrationEnabled(enabled)
            _settings.value = _settings.value.copy(vibrationEnabled = enabled)
        }
    }

    fun setReadReceipts(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setReadReceipts(enabled)
            _settings.value = _settings.value.copy(readReceipts = enabled)
        }
    }

    fun setOnlineStatus(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setOnlineStatus(enabled)
            _settings.value = _settings.value.copy(onlineStatus = enabled)
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.logout()
            if (result.isSuccess) {
                onSuccess()
            }
        }
    }
}
