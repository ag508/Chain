package com.chain.app.domain.usecase

import com.chain.app.domain.model.Chat
import com.chain.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get a specific chat by ID
 */
class GetChatByIdUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(chatId: String): Flow<Chat> {
        return chatRepository.observeChat(chatId)
    }
}
