package com.chain.app.presentation.settings.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatBackupViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _backupSettings = MutableStateFlow(BackupSettings())
    val backupSettings: StateFlow<BackupSettings> = _backupSettings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                userPreferences.getLastBackupDate(),
                userPreferences.getAutoBackupEnabled(),
                userPreferences.getBackupFrequency(),
                userPreferences.getBackupIncludeVideos(),
                userPreferences.getBackupOverCellular()
            ) { values ->
                BackupSettings(
                    lastBackupDate = values[0] as Long?,
                    autoBackupEnabled = values[1] as Boolean,
                    backupFrequency = values[2] as String,
                    includeVideos = values[3] as Boolean,
                    backupOverCellular = values[4] as Boolean
                )
            }.collect {
                _backupSettings.value = it
            }
        }
    }

    fun backupNow() {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Backing
            try {
                // Simulate backup process
                kotlinx.coroutines.delay(2000)

                // Update last backup date
                userPreferences.setLastBackupDate(System.currentTimeMillis())

                _uiState.value = BackupUiState.Success("Backup completed successfully")
            } catch (e: Exception) {
                _uiState.value = BackupUiState.Error(e.message ?: "Backup failed")
            } finally {
                // Reset to idle after a delay
                kotlinx.coroutines.delay(2000)
                _uiState.value = BackupUiState.Idle
            }
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAutoBackupEnabled(enabled)
        }
    }

    fun setBackupFrequency(frequency: String) {
        viewModelScope.launch {
            userPreferences.setBackupFrequency(frequency)
        }
    }

    fun setIncludeVideos(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setBackupIncludeVideos(enabled)
        }
    }

    fun setBackupOverCellular(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setBackupOverCellular(enabled)
        }
    }
}

data class BackupSettings(
    val lastBackupDate: Long? = null,
    val autoBackupEnabled: Boolean = false,
    val backupFrequency: String = "daily",
    val includeVideos: Boolean = true,
    val backupOverCellular: Boolean = false
)

sealed class BackupUiState {
    object Idle : BackupUiState()
    object Backing : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}
