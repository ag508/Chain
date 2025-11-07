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
    onViewProfileClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val viewModelUserId by viewModel.currentUserId.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val lastSeen by viewModel.lastSeen.collectAsState()
    val isBlocked by viewModel.isBlocked.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
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
                currentUserId = viewModelUserId,
                isOnline = isOnline,
                isTyping = isTyping,
                lastSeen = lastSeen,
                isBlocked = isBlocked,
                isMuted = isMuted,
                onBackClick = onBackClick,
                onSendMessage = viewModel::sendMessage,
                onVoiceCallClick = onVoiceCallClick,
                onVideoCallClick = onVideoCallClick,
                onViewProfileClick = onViewProfileClick,
                onToggleBlock = viewModel::toggleBlockUser,
                onToggleMute = viewModel::toggleMuteChat,
                onSendImage = viewModel::sendImageMessage,
                onSendVideo = viewModel::sendVideoMessage,
                onSendDocument = viewModel::sendDocumentMessage,
                onSendLocation = viewModel::sendLocationMessage,
                onSendContact = viewModel::sendContactMessage,
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
    isOnline: Boolean,
    isTyping: Boolean,
    lastSeen: Long?,
    isBlocked: Boolean,
    isMuted: Boolean,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onViewProfileClick: (String) -> Unit,
    onToggleBlock: () -> Unit,
    onToggleMute: () -> Unit,
    onSendImage: (android.net.Uri, String) -> Unit,
    onSendVideo: (android.net.Uri, String, Long) -> Unit,
    onSendDocument: (android.net.Uri, String) -> Unit,
    onSendLocation: (Double, Double, String) -> Unit,
    onSendContact: (String, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var replyingToMessage by remember { mutableStateOf<String?>(null) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showMuteDialog by remember { mutableStateOf(false) }
    var showDisappearingMessagesDialog by remember { mutableStateOf(false) }

    // Filter messages based on search query
    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) {
            messages
        } else {
            messages.filter { message ->
                message.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Media pickers - must be declared before permission launchers that use them
    val mediaPicker = rememberMediaPicker { uri ->
        // Determine if it's an image or video based on URI
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType?.startsWith("video") == true) {
            onSendVideo(uri, "", 0)
        } else {
            onSendImage(uri, "")
        }
    }

    val documentPicker = rememberDocumentPicker { uri ->
        val fileName = uri.lastPathSegment ?: "document"
        onSendDocument(uri, fileName)
    }

    val cameraCapture = rememberCameraCapture { uri ->
        onSendImage(uri, "")
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

    // Get the other user's ID for direct chats
    val otherUserId = remember(chat.participants, currentUserId) {
        if (chat.type == ChatType.DIRECT) {
            chat.participants.firstOrNull { it != currentUserId }
        } else null
    }

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
                        otherUserId?.let { userId ->
                            onViewProfileClick(userId)
                        } ?: run {
                            // For group chats, show a toast for now
                            Toast.makeText(context, "Group info coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onVoiceCallClick = onVoiceCallClick,
                    onVideoCallClick = onVideoCallClick,
                    onSearchQueryChange = { query -> searchQuery = query },
                    onSearchToggle = { searching -> isSearching = searching },
                    isOnline = isOnline,
                    isTyping = isTyping,
                    lastSeen = lastSeen,
                    participantCount = if (chat.type == ChatType.GROUP) chat.participants.size else null,
                    isBlocked = isBlocked,
                    isMuted = isMuted,
                    onViewProfile = {
                        otherUserId?.let { userId ->
                            onViewProfileClick(userId)
                        } ?: run {
                            // For group chats, show a toast for now
                            Toast.makeText(context, "Group info coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onMute = { showMuteDialog = true },
                    onBlock = { showBlockDialog = true },
                    onDisappearingMessages = { showDisappearingMessagesDialog = true }
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
                items(filteredMessages, key = { it.id }) { message ->
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
                        onSendLocation(
                            37.7749,
                            -122.4194,
                            "San Francisco, CA"
                        )
                        Toast.makeText(context, "Location sent!", Toast.LENGTH_SHORT).show()
                    },
                    onContactClick = {
                        showAttachmentMenu = false
                        // Send a sample contact (in production, would open contact picker)
                        onSendContact(
                            "John Doe",
                            "+1234567890",
                            "john.doe@example.com"
                        )
                        Toast.makeText(context, "Contact shared!", Toast.LENGTH_SHORT).show()
                    },
                    onPollClick = {
                        showAttachmentMenu = false
                        Toast.makeText(context, "Poll feature coming soon!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Block/Unblock user dialog
        if (showBlockDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showBlockDialog = false },
                title = {
                    Text(
                        text = if (isBlocked) "Unblock ${chat.name}?" else "Block ${chat.name}?",
                        style = MaterialTheme.typography.titleMedium,
                        color = GlassText
                    )
                },
                text = {
                    Text(
                        text = if (isBlocked) {
                            "${chat.name} will be able to send you messages and call you again."
                        } else {
                            "Blocked contacts won't be able to send you messages or call you."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassText.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onToggleBlock()
                            showBlockDialog = false
                            val message = if (isBlocked) "${chat.name} unblocked" else "${chat.name} blocked"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = if (isBlocked) "Unblock" else "Block",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBlockDialog = false }) {
                        Text("Cancel", color = GlassText)
                    }
                },
                containerColor = GlassGradientStart.copy(alpha = 0.95f)
            )
        }

        // Mute dialog
        if (showMuteDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showMuteDialog = false },
                title = {
                    Text(
                        text = "Mute notifications",
                        style = MaterialTheme.typography.titleMedium,
                        color = GlassText
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isMuted) "Mute options:" else "For how long?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassText.copy(alpha = 0.8f)
                        )

                        // Show "Off" option if currently muted
                        if (isMuted) {
                            TextButton(
                                onClick = {
                                    onToggleMute()
                                    showMuteDialog = false
                                    Toast.makeText(context, "Notifications unmuted", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Off",
                                    color = GlassAccent,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        listOf("15 minutes", "1 hour", "8 hours", "1 week", "Always").forEach { duration ->
                            TextButton(
                                onClick = {
                                    if (!isMuted) {
                                        onToggleMute()
                                    }
                                    showMuteDialog = false
                                    Toast.makeText(context, "Muted for $duration", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = duration,
                                    color = GlassText,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showMuteDialog = false }) {
                        Text("Cancel", color = GlassText)
                    }
                },
                containerColor = GlassGradientStart.copy(alpha = 0.95f)
            )
        }

        // Disappearing messages dialog
        if (showDisappearingMessagesDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDisappearingMessagesDialog = false },
                title = {
                    Text(
                        text = "Disappearing messages",
                        style = MaterialTheme.typography.titleMedium,
                        color = GlassText
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "New messages will disappear from this chat after the selected time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassText.copy(alpha = 0.8f)
                        )

                        listOf("Off", "24 hours", "7 days", "90 days").forEach { duration ->
                            TextButton(
                                onClick = {
                                    showDisappearingMessagesDialog = false
                                    Toast.makeText(context, "Disappearing messages: $duration", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = duration,
                                    color = GlassText,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showDisappearingMessagesDialog = false }) {
                        Text("Cancel", color = GlassText)
                    }
                },
                containerColor = GlassGradientStart.copy(alpha = 0.95f)
            )
        }
    }
}
