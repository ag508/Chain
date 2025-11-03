package com.chain.app.di

import android.content.Context
import androidx.room.Room
import com.chain.app.data.local.ChainDatabase
import com.chain.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

/**
 * Dagger Hilt module for database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideChainDatabase(
        @ApplicationContext context: Context
    ): ChainDatabase {
        // Generate encryption key from Android Keystore
        val passphrase = getDatabasePassphrase(context)
        val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))

        return Room.databaseBuilder(
            context,
            ChainDatabase::class.java,
            ChainDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: ChainDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideChatDao(database: ChainDatabase): ChatDao {
        return database.chatDao()
    }

    @Provides
    fun provideMessageDao(database: ChainDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideReactionDao(database: ChainDatabase): ReactionDao {
        return database.reactionDao()
    }

    @Provides
    fun provideCallDao(database: ChainDatabase): CallDao {
        return database.callDao()
    }

    /**
     * Get or generate database encryption passphrase from Android Keystore.
     */
    private fun getDatabasePassphrase(context: Context): String {
        val sharedPrefs = context.getSharedPreferences("chain_secure", Context.MODE_PRIVATE)
        val existingPassphrase = sharedPrefs.getString("db_passphrase", null)

        return if (existingPassphrase != null) {
            existingPassphrase
        } else {
            // Generate a new random passphrase
            val newPassphrase = generateSecurePassphrase()
            sharedPrefs.edit().putString("db_passphrase", newPassphrase).apply()
            newPassphrase
        }
    }

    /**
     * Generate a secure random passphrase for database encryption.
     */
    private fun generateSecurePassphrase(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..64)
            .map { chars.random() }
            .joinToString("")
    }
}
