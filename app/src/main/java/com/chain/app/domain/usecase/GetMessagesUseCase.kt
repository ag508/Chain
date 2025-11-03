package com.chain.app.domain.usecase

import com.chain.app.domain.model.Message
import com.chain.app.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving messages for a chat.
 */
class GetMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(
        chatId: String,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<Message>> {
        return messageRepository.getMessages(chatId, limit, offset)
    }
}
