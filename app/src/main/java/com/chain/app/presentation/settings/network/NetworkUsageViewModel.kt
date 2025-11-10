package com.chain.app.presentation.settings.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkUsageViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _networkUsage = MutableStateFlow(NetworkUsage())
    val networkUsage: StateFlow<NetworkUsage> = _networkUsage.asStateFlow()

    init {
        loadUsageStats()
    }

    private fun loadUsageStats() {
        viewModelScope.launch {
            combine(
                userPreferences.getTotalBytesSent(),
                userPreferences.getTotalBytesReceived(),
                userPreferences.getMessagesBytesSent(),
                userPreferences.getMessagesBytesReceived(),
                userPreferences.getPhotosBytesSent(),
                userPreferences.getPhotosBytesReceived(),
                userPreferences.getVideosBytesSent(),
                userPreferences.getVideosBytesReceived(),
                userPreferences.getDocumentsBytesSent(),
                userPreferences.getDocumentsBytesReceived(),
                userPreferences.getUsageResetDate()
            ) { values ->
                NetworkUsage(
                    totalBytesSent = values[0] as Long,
                    totalBytesReceived = values[1] as Long,
                    messagesBytesSent = values[2] as Long,
                    messagesBytesReceived = values[3] as Long,
                    photosBytesSent = values[4] as Long,
                    photosBytesReceived = values[5] as Long,
                    videosBytesSent = values[6] as Long,
                    videosBytesReceived = values[7] as Long,
                    documentsBytesSent = values[8] as Long,
                    documentsBytesReceived = values[9] as Long,
                    resetDate = values[10] as Long?
                )
            }.collect {
                _networkUsage.value = it
            }
        }
    }

    fun resetUsageStats() {
        viewModelScope.launch {
            userPreferences.resetNetworkUsage()
        }
    }
}

data class NetworkUsage(
    val totalBytesSent: Long = 0L,
    val totalBytesReceived: Long = 0L,
    val messagesBytesSent: Long = 0L,
    val messagesBytesReceived: Long = 0L,
    val photosBytesSent: Long = 0L,
    val photosBytesReceived: Long = 0L,
    val videosBytesSent: Long = 0L,
    val videosBytesReceived: Long = 0L,
    val documentsBytesSent: Long = 0L,
    val documentsBytesReceived: Long = 0L,
    val resetDate: Long? = null
) {
    val totalBytes: Long
        get() = totalBytesSent + totalBytesReceived

    val messagesTotal: Long
        get() = messagesBytesSent + messagesBytesReceived

    val photosTotal: Long
        get() = photosBytesSent + photosBytesReceived

    val videosTotal: Long
        get() = videosBytesSent + videosBytesReceived

    val documentsTotal: Long
        get() = documentsBytesSent + documentsBytesReceived
}

// Helper extension for formatting bytes
fun Long.formatBytes(): String {
    return when {
        this >= 1_073_741_824 -> String.format("%.2f GB", this / 1_073_741_824.0)
        this >= 1_048_576 -> String.format("%.2f MB", this / 1_048_576.0)
        this >= 1024 -> String.format("%.2f KB", this / 1024.0)
        else -> "$this B"
    }
}
