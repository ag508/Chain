package com.chain.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main Application class for Chain messaging platform.
 * Initializes Hilt dependency injection and sets up core application components.
 */
@HiltAndroidApp
class ChainApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Initialize notification channels
        createNotificationChannels()

        // Initialize crash reporting and analytics (if needed in future)
        // initializeCrashReporting()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Messages notification channel
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New message notifications"
                enableVibration(true)
            }

            // Calls notification channel
            val callsChannel = NotificationChannel(
                CHANNEL_CALLS,
                "Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming call notifications"
                enableVibration(true)
            }

            // Service notification channel
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service notifications"
            }

            notificationManager.createNotificationChannels(
                listOf(messagesChannel, callsChannel, serviceChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_MESSAGES = "messages_channel"
        const val CHANNEL_CALLS = "calls_channel"
        const val CHANNEL_SERVICE = "service_channel"
    }
}
