package com.chain.app.domain.usecase.group

import com.chain.app.domain.model.Chat
import com.chain.app.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Use case to get group chat information.
 *
 * @param chatId The group chat ID
 * @return Result with Chat information or error
 */
class GetGroupInfoUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(chatId: String): Result<Chat> {
        if (chatId.isBlank()) {
            return Result.failure(Exception("Chat ID cannot be empty"))
        }

        return chatRepository.getChat(chatId)
    }
}
