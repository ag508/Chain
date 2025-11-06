package com.chain.app.domain.usecase.debug

import android.util.Log
import com.chain.app.data.local.dao.CallDao
import com.chain.app.data.local.dao.ChatDao
import com.chain.app.data.local.dao.ContactDao
import com.chain.app.data.local.dao.MessageDao
import com.chain.app.data.local.entity.CallEntity
import com.chain.app.data.local.entity.ChatEntity
import com.chain.app.data.local.entity.ContactEntity
import com.chain.app.data.local.entity.MessageEntity
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Debug use case to seed sample data for testing.
 * Creates sample contacts, chats, messages, and call history.
 */
class SeedSampleDataUseCase @Inject constructor(
    private val contactDao: ContactDao,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val callDao: CallDao
) {
    suspend operator fun invoke(currentUserId: String) {
        Log.d("SeedSampleData", "Starting seed with userId: $currentUserId")

        try {
            // Check if data already exists
            Log.d("SeedSampleData", "Checking for existing contacts...")
            val existingContacts = contactDao.getContactById("sample-contact-1")
            if (existingContacts != null) {
                Log.d("SeedSampleData", "Sample data already exists, skipping")
                return
            }
            Log.d("SeedSampleData", "No existing data found, proceeding with seed")
        } catch (e: Exception) {
            Log.e("SeedSampleData", "Error checking existing contacts", e)
            throw e
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
        Log.d("SeedSampleData", "Inserting ${contacts.size} contacts...")
        try {
            contacts.forEach { contactDao.insertContact(it) }
            Log.d("SeedSampleData", "Contacts inserted successfully")
        } catch (e: Exception) {
            Log.e("SeedSampleData", "Error inserting contacts", e)
            throw e
        }

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
        Log.d("SeedSampleData", "Inserting ${chats.size} chats...")
        try {
            chats.forEach { chatDao.insertChat(it) }
            Log.d("SeedSampleData", "Chats inserted successfully")
        } catch (e: Exception) {
            Log.e("SeedSampleData", "Error inserting chats", e)
            throw e
        }

        // Create sample messages for Chat 1 (Alice)
        Log.d("SeedSampleData", "Creating sample messages...")
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
        Log.d("SeedSampleData", "Inserting messages for chat 1 (${messagesChat1.size} messages)...")
        try {
            messageDao.insertMessages(messagesChat1)
            Log.d("SeedSampleData", "Chat 1 messages inserted")
        } catch (e: Exception) {
            Log.e("SeedSampleData", "Error inserting chat 1 messages", e)
            throw e
        }

        Log.d("SeedSampleData", "Inserting messages for chat 2 (${messagesChat2.size} messages)...")
        try {
            messageDao.insertMessages(messagesChat2)
            Log.d("SeedSampleData", "Chat 2 messages inserted")
        } catch (e: Exception) {
            Log.e("SeedSampleData", "Error inserting chat 2 messages", e)
            throw e
        }

        Log.d("SeedSampleData", "Inserting messages for chat 3 (${messagesChat3.size} messages)...")
        try {
            messageDao.insertMessages(messagesChat3)
            Log.d("SeedSampleData", "Chat 3 messages inserted")
        } catch (e: Exception) {
            Log.e("SeedSampleData", "Error inserting chat 3 messages", e)
            throw e
        }

        // Create sample call history
        val calls = listOf(
            // Recent answered voice call with Alice
            CallEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat1Id,
                initiator = "user-alice",
                participants = listOf(currentUserId, "user-alice"),
                type = "VOICE",
                status = "ENDED",
                startTime = Date().time - 3600000 * 2, // 2 hours ago
                endTime = Date().time - 3600000 * 2 + 323000, // 5 min 23 sec call
                duration = 323000L
            ),
            // Missed call from Bob (1 hour ago)
            CallEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat2Id,
                initiator = "user-bob",
                participants = listOf(currentUserId, "user-bob"),
                type = "VOICE",
                status = "MISSED",
                startTime = Date().time - 3600000, // 1 hour ago
                endTime = null,
                duration = null
            ),
            // Video call with Charlie (yesterday)
            CallEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat3Id,
                initiator = currentUserId,
                participants = listOf(currentUserId, "user-charlie"),
                type = "VIDEO",
                status = "ENDED",
                startTime = Date().time - 86400000, // 1 day ago
                endTime = Date().time - 86400000 + 725000, // 12 min 5 sec call
                duration = 725000L
            ),
            // Multi-party call with Alice, Bob, Charlie (3 days ago)
            CallEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat1Id,
                initiator = currentUserId,
                participants = listOf(currentUserId, "user-alice", "user-bob", "user-charlie"),
                type = "VOICE",
                status = "ENDED",
                startTime = Date().time - 86400000 * 3, // 3 days ago
                endTime = Date().time - 86400000 * 3 + 1845000, // 30 min 45 sec call
                duration = 1845000L
            ),
            // Another missed call from Alice (5 days ago)
            CallEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat1Id,
                initiator = "user-alice",
                participants = listOf(currentUserId, "user-alice"),
                type = "VOICE",
                status = "MISSED",
                startTime = Date().time - 86400000 * 5, // 5 days ago
                endTime = null,
                duration = null
            ),
            // Multi-party video call (1 week ago)
            CallEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat2Id,
                initiator = "user-bob",
                participants = listOf(currentUserId, "user-bob", "user-alice"),
                type = "VIDEO",
                status = "ENDED",
                startTime = Date().time - 86400000 * 7, // 1 week ago
                endTime = Date().time - 86400000 * 7 + 452000, // 7 min 32 sec call
                duration = 452000L
            ),
            // Short answered call with Bob (30 minutes ago)
            CallEntity(
                id = UUID.randomUUID().toString(),
                chatId = chat2Id,
                initiator = currentUserId,
                participants = listOf(currentUserId, "user-bob"),
                type = "VOICE",
                status = "ENDED",
                startTime = Date().time - 1800000, // 30 minutes ago
                endTime = Date().time - 1800000 + 45000, // 45 second call
                duration = 45000L
            )
        )

        Log.d("SeedSampleData", "Inserting ${calls.size} sample calls...")
        try {
            calls.forEach { callDao.insertCall(it) }
            Log.d("SeedSampleData", "Sample calls inserted successfully")
        } catch (e: Exception) {
            Log.e("SeedSampleData", "Error inserting sample calls", e)
            throw e
        }

        Log.d("SeedSampleData", "Sample data seeding completed successfully!")
    }
}
