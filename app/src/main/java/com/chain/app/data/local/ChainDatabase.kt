package com.chain.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chain.app.data.local.converter.StringListConverter
import com.chain.app.data.local.dao.*
import com.chain.app.data.local.entity.*

/**
 * Main Room database for Chain app with SQLCipher encryption.
 */
@Database(
    entities = [
        UserEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        MessageFts::class,
        ReactionEntity::class,
        CallEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class ChainDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun reactionDao(): ReactionDao
    abstract fun callDao(): CallDao

    companion object {
        const val DATABASE_NAME = "chain_database"
    }
}
