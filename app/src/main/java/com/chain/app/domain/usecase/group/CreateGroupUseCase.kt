package com.chain.app.domain.usecase.group

import com.chain.app.domain.model.GroupChat
import com.chain.app.domain.repository.ChatRepository
import com.chain.app.domain.usecase.auth.GetCurrentUserIdUseCase
import javax.inject.Inject

/**
 * Use case to create a new group chat.
 *
 * @param name Group name (required, min 3 characters)
 * @param participants List of user IDs to add as members (not including current user)
 * @param description Optional group description
 * @return Result with created GroupChat or error
 */
class CreateGroupUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend operator fun invoke(
        name: String,
        participants: List<String>,
        description: String? = null
    ): Result<GroupChat> {
        // Validate group name
        if (name.isBlank() || name.length < 3) {
            return Result.failure(Exception("Group name must be at least 3 characters"))
        }

        // Validate participants
        if (participants.isEmpty()) {
            return Result.failure(Exception("Group must have at least one member"))
        }

        // Get current user ID
        val currentUserId = getCurrentUserIdUseCase()
            ?: return Result.failure(Exception("User not authenticated"))

        // Participants list should include the current user (creator) + selected members
        val allParticipants = (participants + currentUserId).distinct()

        // Create the group via repository
        return chatRepository.createGroupChat(
            name = name.trim(),
            participants = allParticipants,
            description = description?.trim()
        )
    }
}
