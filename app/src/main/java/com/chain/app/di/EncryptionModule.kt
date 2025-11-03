package com.chain.app.di

import android.content.Context
import com.chain.app.data.encryption.SignalProtocolManager
import com.chain.app.data.encryption.SignalProtocolStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for encryption dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object EncryptionModule {

    @Provides
    @Singleton
    fun provideSignalProtocolStore(
        @ApplicationContext context: Context
    ): SignalProtocolStore {
        return SignalProtocolStore(context)
    }

    @Provides
    @Singleton
    fun provideSignalProtocolManager(
        @ApplicationContext context: Context,
        signalProtocolStore: SignalProtocolStore
    ): SignalProtocolManager {
        return SignalProtocolManager(context, signalProtocolStore)
    }
}
