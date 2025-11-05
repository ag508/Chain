package com.chain.app.presentation.chat.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.chain.app.domain.model.Chat
import com.chain.app.domain.model.ChatType
import com.chain.app.domain.model.Message
import com.chain.app.presentation.chat.detail.components.*
import com.chain.app.presentation.theme.*

/**
 * Chat detail screen showing conversation with a contact or group
 * Includes header, message list, and input bar
 */
@Composable
fun ChatDetailScreen(
    chat: Chat,
    messages: List<Message>,
    currentUserId: String,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var replyingToMessage by remember { mutableStateOf<String?>(null) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Background gradient
    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bgBrush)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Chat header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 8.dp)
            ) {
                ChatHeader(
                    chatId = chat.id,
                    chatName = chat.name,
                    chatType = chat.type,
                    isOnline = false, // TODO: Get real online status
                    isTyping = false, // TODO: Get real typing status
                    lastSeen = null, // TODO: Get real last seen
                    participantCount = if (chat.type == ChatType.GROUP) chat.participants.size else null,
                    onBackClick = onBackClick,
                    onHeaderClick = {
                        // TODO: Navigate to profile/group info
                    },
                    onVoiceCallClick = onVoiceCallClick,
                    onVideoCallClick = onVideoCallClick,
                    onMoreClick = {
                        // TODO: Show more menu
                    }
                )
            }

            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        isSentByMe = message.senderId == currentUserId,
                        showSender = chat.type == ChatType.GROUP,
                        onLongPress = {
                            // TODO: Show message actions menu
                        },
                        onDoubleTap = {
                            // TODO: Add reaction
                        }
                    )
                }
            }

            // Message input bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                MessageInputBar(
                    text = messageText,
                    onTextChange = { messageText = it },
                    onSendMessage = {
                        if (messageText.text.isNotBlank()) {
                            onSendMessage(messageText.text.trim())
                            messageText = TextFieldValue("")
                            replyingToMessage = null
                        }
                    },
                    onEmojiClick = {
                        // TODO: Show emoji picker
                    },
                    onAttachmentClick = {
                        showAttachmentMenu = true
                    },
                    onVoiceRecordStart = {
                        isRecordingVoice = true
                        // TODO: Start voice recording
                    },
                    onVoiceRecordStop = {
                        isRecordingVoice = false
                        // TODO: Stop and send voice recording
                    },
                    isRecordingVoice = isRecordingVoice,
                    replyingTo = replyingToMessage,
                    onCancelReply = { replyingToMessage = null }
                )
            }
        }

        // Attachment menu modal
        if (showAttachmentMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                AttachmentMenu(
                    onDismiss = { showAttachmentMenu = false },
                    onCameraClick = {
                        // TODO: Open camera
                    },
                    onGalleryClick = {
                        // TODO: Open gallery
                    },
                    onDocumentClick = {
                        // TODO: Open document picker
                    },
                    onLocationClick = {
                        // TODO: Open location picker
                    },
                    onContactClick = {
                        // TODO: Open contact picker
                    },
                    onPollClick = {
                        // TODO: Create poll
                    }
                )
            }
        }
    }
}
