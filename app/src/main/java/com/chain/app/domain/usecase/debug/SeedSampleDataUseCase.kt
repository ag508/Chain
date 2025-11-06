package com.chain.app.domain.usecase.debug

import com.chain.app.data.local.dao.ChatDao
import com.chain.app.data.local.dao.ContactDao
import com.chain.app.data.local.dao.MessageDao
import com.chain.app.data.local.entity.ChatEntity
import com.chain.app.data.local.entity.ContactEntity
import com.chain.app.data.local.entity.MessageEntity
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Debug use case to seed sample data for testing.
 * Creates sample contacts, chats, and messages.
 */
class SeedSampleDataUseCase @Inject constructor(
    private val contactDao: ContactDao,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) {
    suspend operator fun invoke(currentUserId: String) {
        // Check if data already exists
        val existingContacts = contactDao.getContactById("sample-contact-1")
        if (existingContacts != null) {
            // Data already seeded
            return
        }

        // Create sample contacts
        val contacts = listOf(
            ContactEntity(
                id = "sample-contact-1",
                user_id = "user-alice",
                phone_number = "+1234567890",
                display_name = "Alice Smith",
                avatar = null,
                public_key = null,
                added_at = Date().time - 86400000 * 7, // 7 days ago
                is_blocked = false,
                last_seen = Date().time - 3600000 // 1 hour ago
            ),
            ContactEntity(
                id = "sample-contact-2",
                user_id = "user-bob",
                phone_number = "+0987654321",
                display_name = "Bob Johnson",
                avatar = null,
                public_key = null,
                added_at = Date().time - 86400000 * 3, // 3 days ago
                is_blocked = false,
                last_seen = Date().time - 7200000 // 2 hours ago
            ),
            ContactEntity(
                id = "sample-contact-3",
                user_id = "user-charlie",
                phone_number = "+1122334455",
                display_name = "Charlie Brown",
                avatar = null,
                public_key = null,
                added_at = Date().time - 86400000, // 1 day ago
                is_blocked = false,
                last_seen = Date().time - 1800000 // 30 minutes ago
            )
        )

        // Insert contacts
        contacts.forEach { contactDao.insertContact(it) }

        // Create sample chats
        val chat1Id = "sample-chat-1"
        val chat2Id = "sample-chat-2"
        val chat3Id = "sample-chat-3"

        val chats = listOf(
            ChatEntity(
                id = chat1Id,
                type = "DIRECT",
                name = "Alice Smith",
                participants = listOf(currentUserId, "user-alice"),
                admins = emptyList(),
                disappearingMessagesEnabled = false,
                disappearingMessagesDuration = null,
                notificationsEnabled = true,
                encryptionEnabled = true,
                unreadCount = 2,
                isPinned = true,
                isArchived = false,
                isMuted = false,
                createdAt = Date().time - 86400000 * 7, // 7 days ago
                updatedAt = Date().time - 300000 // 5 minutes ago
            ),
            ChatEntity(
                id = chat2Id,
                type = "DIRECT",
                name = "Bob Johnson",
                participants = listOf(currentUserId, "user-bob"),
                admins = emptyList(),
                disappearingMessagesEnabled = false,
                disappearingMessagesDuration = null,
                notificationsEnabled = true,
                encryptionEnabled = true,
                unreadCount = 0,
                isPinned = false,
                isArchived = false,
                isMuted = false,
                createdAt = Date().time - 86400000 * 3, // 3 days ago
                updatedAt = Date().time - 3600000 // 1 hour ago
            ),
            ChatEntity(
                id = chat3Id,
                type = "DIRECT",
                name = "Charlie Brown",
                participants = listOf(currentUserId, "user-charlie"),
                admins = emptyList(),
                disappearingMessagesEnabled = false,
                disappearingMessagesDuration = null,
                notificationsEnabled = true,
                encryptionEnabled = true,
                unreadCount = 1,
                isPinned = false,
                isArchived = false,
                isMuted = false,
                createdAt = Date().time - 86400000, // 1 day ago
                updatedAt = Date().time - 1800000 // 30 minutes ago
            )
        )

        // Insert chats
        chats.forEach { chatDao.insertChat(it) }

        // Create sample messages for Chat 1 (Alice)
        val messagesChat1 = listOf(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat1Id,
                senderId = currentUserId,
                content = "Hey Alice! How are you doing?",
                type = "TEXT",
                timestamp = Date().time - 3600000 * 5, // 5 hours ago
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat1Id,
                senderId = "user-alice",
                content = "Hi! I'm doing great, thanks for asking! How about you?",
                type = "TEXT",
                timestamp = Date().time - 3600000 * 4 - 1800000, // 4.5 hours ago
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat1Id,
                senderId = currentUserId,
                content = "I'm good too! Working on the Chain app 🚀",
                type = "TEXT",
                timestamp = Date().time - 3600000 * 4, // 4 hours ago
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat1Id,
                senderId = "user-alice",
                content = "That's awesome! Can't wait to try it out!",
                type = "TEXT",
                timestamp = Date().time - 3600000 * 3 - 1800000, // 3.5 hours ago
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat1Id,
                senderId = "user-alice",
                content = "Let me know when you need beta testers!",
                type = "TEXT",
                timestamp = Date().time - 3600000 * 3, // 3 hours ago
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat1Id,
                senderId = currentUserId,
                content = "Will do! I'll send you an invite soon",
                type = "TEXT",
                timestamp = Date().time - 3600000 * 2, // 2 hours ago
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat1Id,
                senderId = "user-alice",
                content = "Are you still working on it now?",
                type = "TEXT",
                timestamp = Date().time - 600000, // 10 minutes ago
                status = "DELIVERED",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat1Id,
                senderId = "user-alice",
                content = "I'm excited to see the progress!",
                type = "TEXT",
                timestamp = Date().time - 300000, // 5 minutes ago
                status = "DELIVERED",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            )
        )

        // Create sample messages for Chat 2 (Bob)
        val messagesChat2 = listOf(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat2Id,
                senderId = "user-bob",
                content = "Hey! Did you see the game last night?",
                type = "TEXT",
                timestamp = Date().time - 86400000 * 2, // 2 days ago
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat2Id,
                senderId = currentUserId,
                content = "Yes! It was incredible!",
                type = "TEXT",
                timestamp = Date().time - 86400000 * 2 + 3600000, // 2 days ago + 1 hour
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat2Id,
                senderId = "user-bob",
                content = "That last minute goal was insane!",
                type = "TEXT",
                timestamp = Date().time - 86400000 * 2 + 3600000 * 2, // 2 days ago + 2 hours
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat2Id,
                senderId = currentUserId,
                content = "I know right! Can't believe they pulled it off",
                type = "TEXT",
                timestamp = Date().time - 86400000 * 2 + 3600000 * 3, // 2 days ago + 3 hours
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat2Id,
                senderId = "user-bob",
                content = "Want to watch the next game together?",
                type = "TEXT",
                timestamp = Date().time - 3600000, // 1 hour ago
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            )
        )

        // Create sample messages for Chat 3 (Charlie)
        val messagesChat3 = listOf(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat3Id,
                senderId = currentUserId,
                content = "Hey Charlie! Welcome to Chain!",
                type = "TEXT",
                timestamp = Date().time - 86400000, // 1 day ago
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat3Id,
                senderId = "user-charlie",
                content = "Thanks! This app looks really cool",
                type = "TEXT",
                timestamp = Date().time - 86400000 + 3600000, // 1 day ago + 1 hour
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat3Id,
                senderId = "user-charlie",
                content = "I love the glassmorphism design!",
                type = "TEXT",
                timestamp = Date().time - 86400000 + 3600000 * 2, // 1 day ago + 2 hours
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat3Id,
                senderId = currentUserId,
                content = "Glad you like it! Let me know if you have any feedback",
                type = "TEXT",
                timestamp = Date().time - 86400000 + 3600000 * 3, // 1 day ago + 3 hours
                status = "READ",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            ),
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat3Id,
                senderId = "user-charlie",
                content = "Quick question - how do I add more contacts?",
                type = "TEXT",
                timestamp = Date().time - 1800000, // 30 minutes ago
                status = "DELIVERED",
                replyTo = null,
                isEncrypted = true,
                disappearAfter = null
            )
        )

        // Insert all messages
        messageDao.insertMessages(messagesChat1)
        messageDao.insertMessages(messagesChat2)
        messageDao.insertMessages(messagesChat3)
    }
}
