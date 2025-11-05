package com.chain.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
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

        // Initialize Timber logging
        initializeLogging()

        // Initialize notification channels
        createNotificationChannels()

        // Log application start
        Timber.i("Chain Application started - Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            // Debug tree for development
            Timber.plant(Timber.DebugTree())
            Timber.d("Debug logging enabled")
        } else {
            // Production tree (you can add Crashlytics tree here)
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    // Only log warnings and errors in production
                    if (priority >= android.util.Log.WARN) {
                        // Send to crash reporting service (e.g., Crashlytics)
                        // Crashlytics.log(message)
                        // if (t != null) Crashlytics.recordException(t)
                    }
                }
            })
        }
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
