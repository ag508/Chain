package com.chain.app.domain.usecase.group

import com.chain.app.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Use case to add members to an existing group chat.
 *
 * @param chatId The group chat ID
 * @param userIds List of user IDs to add as members
 * @return Result indicating success or failure
 */
class AddGroupMemberUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        chatId: String,
        userIds: List<String>
    ): Result<Unit> {
        // Validate inputs
        if (chatId.isBlank()) {
            return Result.failure(Exception("Chat ID cannot be empty"))
        }

        if (userIds.isEmpty()) {
            return Result.failure(Exception("Must provide at least one user ID"))
        }

        // Add members via repository
        return chatRepository.addMembers(chatId, userIds)
    }
}
