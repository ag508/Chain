package com.chain.app.presentation.settings.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutoDownloadMediaViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _settings = MutableStateFlow(AutoDownloadSettings())
    val settings: StateFlow<AutoDownloadSettings> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                userPreferences.getMobileDataPhotos(),
                userPreferences.getMobileDataVideos(),
                userPreferences.getMobileDataDocuments(),
                userPreferences.getWifiPhotos(),
                userPreferences.getWifiVideos(),
                userPreferences.getWifiDocuments(),
                userPreferences.getRoamingPhotos(),
                userPreferences.getRoamingVideos(),
                userPreferences.getRoamingDocuments()
            ) { values ->
                AutoDownloadSettings(
                    mobileDataPhotos = values[0] as Boolean,
                    mobileDataVideos = values[1] as Boolean,
                    mobileDataDocuments = values[2] as Boolean,
                    wifiPhotos = values[3] as Boolean,
                    wifiVideos = values[4] as Boolean,
                    wifiDocuments = values[5] as Boolean,
                    roamingPhotos = values[6] as Boolean,
                    roamingVideos = values[7] as Boolean,
                    roamingDocuments = values[8] as Boolean
                )
            }.collect {
                _settings.value = it
            }
        }
    }

    fun setMobileDataPhotos(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setMobileDataPhotos(enabled) }
    }

    fun setMobileDataVideos(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setMobileDataVideos(enabled) }
    }

    fun setMobileDataDocuments(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setMobileDataDocuments(enabled) }
    }

    fun setWifiPhotos(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setWifiPhotos(enabled) }
    }

    fun setWifiVideos(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setWifiVideos(enabled) }
    }

    fun setWifiDocuments(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setWifiDocuments(enabled) }
    }

    fun setRoamingPhotos(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setRoamingPhotos(enabled) }
    }

    fun setRoamingVideos(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setRoamingVideos(enabled) }
    }

    fun setRoamingDocuments(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setRoamingDocuments(enabled) }
    }
}

data class AutoDownloadSettings(
    val mobileDataPhotos: Boolean = false,
    val mobileDataVideos: Boolean = false,
    val mobileDataDocuments: Boolean = false,
    val wifiPhotos: Boolean = true,
    val wifiVideos: Boolean = true,
    val wifiDocuments: Boolean = true,
    val roamingPhotos: Boolean = false,
    val roamingVideos: Boolean = false,
    val roamingDocuments: Boolean = false
)
