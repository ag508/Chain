package com.chain.app.domain.usecase

import com.chain.app.domain.model.Message
import com.chain.app.domain.model.MessageStatus
import com.chain.app.domain.model.MessageType
import com.chain.app.domain.repository.MessageRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to send a message in a chat
 */
class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(
        chatId: String,
        content: String,
        senderId: String
    ): Result<Message> {
        val message = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            content = content,
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENDING
        )

        return messageRepository.sendMessage(message)
    }
}
