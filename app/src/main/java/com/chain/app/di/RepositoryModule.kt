package com.chain.app.di

import com.chain.app.data.repository.ChatRepositoryImpl
import com.chain.app.data.repository.EncryptionRepositoryImpl
import com.chain.app.data.repository.MessageRepositoryImpl
import com.chain.app.domain.repository.ChatRepository
import com.chain.app.domain.repository.EncryptionRepository
import com.chain.app.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for repository bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEncryptionRepository(
        impl: EncryptionRepositoryImpl
    ): EncryptionRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository
}
