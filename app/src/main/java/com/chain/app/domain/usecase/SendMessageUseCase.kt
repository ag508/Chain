package com.chain.app.domain.usecase

import com.chain.app.domain.model.Message
import com.chain.app.domain.model.MessageStatus
import com.chain.app.domain.model.MessageType
import com.chain.app.domain.repository.EncryptionRepository
import com.chain.app.domain.repository.MessageRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for sending messages.
 * Handles encryption and message transmission.
 */
class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val encryptionRepository: EncryptionRepository
) {
    suspend operator fun invoke(
        chatId: String,
        content: String,
        recipientId: String,
        type: MessageType = MessageType.TEXT
    ): Result<Message> {
        return try {
            // Create message
            val message = Message(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = "current_user", // TODO: Get from UserRepository
                content = content,
                type = type,
                timestamp = Date(),
                status = MessageStatus.SENDING
            )

            // Encrypt message content
            val encryptionResult = encryptionRepository.encryptMessage(content, recipientId)
            if (encryptionResult.isFailure) {
                return Result.failure(encryptionResult.exceptionOrNull()!!)
            }

            // Save to local database
            val saveResult = messageRepository.sendMessage(message)
            if (saveResult.isFailure) {
                return Result.failure(saveResult.exceptionOrNull()!!)
            }

            // Update status to sent
            messageRepository.updateMessageStatus(message.id, MessageStatus.SENT)

            Result.success(message.copy(status = MessageStatus.SENT))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
