package com.chain.app.di

import com.chain.app.data.repository.AuthRepositoryImpl
import com.chain.app.data.repository.CallRepositoryImpl
import com.chain.app.data.repository.ChatRepositoryImpl
import com.chain.app.data.repository.ContactRepositoryImpl
import com.chain.app.data.repository.EncryptionRepositoryImpl
import com.chain.app.data.repository.MessageRepositoryImpl
import com.chain.app.data.repository.P2PRepositoryImpl
import com.chain.app.data.repository.UserRepositoryImpl
import com.chain.app.domain.repository.AuthRepository
import com.chain.app.domain.repository.CallRepository
import com.chain.app.domain.repository.ChatRepository
import com.chain.app.domain.repository.ContactRepository
import com.chain.app.domain.repository.EncryptionRepository
import com.chain.app.domain.repository.MessageRepository
import com.chain.app.domain.repository.P2PRepository
import com.chain.app.domain.repository.UserRepository
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
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

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

    @Binds
    @Singleton
    abstract fun bindP2PRepository(
        impl: P2PRepositoryImpl
    ): P2PRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        impl: ContactRepositoryImpl
    ): ContactRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindCallRepository(
        impl: CallRepositoryImpl
    ): CallRepository
}
