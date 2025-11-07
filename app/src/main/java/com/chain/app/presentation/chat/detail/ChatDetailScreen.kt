package com.chain.app.presentation.chat.detail
import android.widget.Toast

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.app.domain.model.ChatType
import com.chain.app.presentation.chat.detail.components.*
import com.chain.app.presentation.theme.*
import kotlinx.coroutines.launch

/**
 * Chat detail screen showing conversation with a contact or group
 * Includes header, message list, and input bar
 */
@Composable
fun ChatDetailScreen(
    chatId: String,
    currentUserId: String,
    onBackClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()

    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
        viewModel.setCurrentUserId(currentUserId)
    }

    when (val state = uiState) {
        is ChatDetailUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GlassAccent)
            }
        }
        is ChatDetailUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        is ChatDetailUiState.Success -> {
            ChatDetailContent(
                chat = state.chat,
                messages = messages,
                currentUserId = currentUserId,
                onBackClick = onBackClick,
                onSendMessage = viewModel::sendMessage,
                onVoiceCallClick = onVoiceCallClick,
                onVideoCallClick = onVideoCallClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ChatDetailContent(
    chat: com.chain.app.domain.model.Chat,
    messages: List<com.chain.app.domain.model.Message>,
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
    val context = LocalContext.current

    // Media pickers - must be declared before permission launchers that use them
    val mediaPicker = rememberMediaPicker { uri ->
        viewModel.sendImageMessage(uri, caption = "")
    }

    val documentPicker = rememberDocumentPicker { uri ->
        val fileName = uri.lastPathSegment ?: "document"
        viewModel.sendDocumentMessage(uri, fileName)
    }

    val cameraCapture = rememberCameraCapture { uri ->
        viewModel.sendImageMessage(uri, caption = "")
    }

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    // Microphone permission state
    var hasMicrophonePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            // Permission granted, take photo
            cameraCapture.takePhoto()
        }
    }

    // Microphone permission launcher
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicrophonePermission = isGranted
        if (isGranted) {
            // Permission granted, start recording
            isRecordingVoice = true
            // TODO: Start voice recording
        }
    }

    // Background gradient
    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bgBrush)
            .systemBarsPadding()
            .imePadding()
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
                    chatName = chat.name,
                    chatType = chat.type,
                    onBackClick = onBackClick,
                    onHeaderClick = {
                        // TODO: Navigate to profile/group info
                    },
                    onVoiceCallClick = onVoiceCallClick,
                    onVideoCallClick = onVideoCallClick,
                    isOnline = false, // TODO: Get real online status
                    isTyping = false, // TODO: Get real typing status
                    lastSeen = null, // TODO: Get real last seen
                    participantCount = if (chat.type == ChatType.GROUP) chat.participants.size else null
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

                            // Auto-scroll to bottom after sending
                            coroutineScope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        }
                    },
                    onEmojiClick = {
                        // TODO: Show emoji picker
                    },
                    onAttachmentClick = {
                        showAttachmentMenu = true
                    },
                    onVoiceRecordStart = {
                        if (hasMicrophonePermission) {
                            isRecordingVoice = true
                            // TODO: Start voice recording
                        } else {
                            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
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

        // Attachment menu modal - with opaque background
        if (showAttachmentMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f))
                    .clickable { showAttachmentMenu = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                AttachmentMenu(
                    onDismiss = { showAttachmentMenu = false },
                    onCameraClick = {
                        showAttachmentMenu = false
                        if (hasCameraPermission) {
                            cameraCapture.takePhoto()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onGalleryClick = {
                        showAttachmentMenu = false
                        mediaPicker.pickImageOrVideo()
                    },
                    onDocumentClick = {
                        showAttachmentMenu = false
                        documentPicker.pickDocument()
                    },
                    onLocationClick = {
                        showAttachmentMenu = false
                        // Send current location (placeholder coordinates)
                        viewModel.sendLocationMessage(
                            latitude = 37.7749,
                            longitude = -122.4194,
                            address = "San Francisco, CA"
                        )
                        Toast.makeText(context, "Location sent!", Toast.LENGTH_SHORT).show()
                    },
                    onContactClick = {
                        showAttachmentMenu = false
                        // Contact sharing not yet implemented
                        Toast.makeText(context, "Contact sharing coming soon", Toast.LENGTH_SHORT).show()
                    },
                    onPollClick = {
                        showAttachmentMenu = false
                        // Send a sample poll
                        viewModel.sendPollMessage(
                            question = "What's your favorite feature?",
                            options = listOf("Secure Messaging", "Video Calls", "File Sharing", "Polls")
                        )
                        Toast.makeText(context, "Poll created!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
