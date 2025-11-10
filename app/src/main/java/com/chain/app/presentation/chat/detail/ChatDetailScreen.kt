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
import java.io.File

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
    val selectedMessages by viewModel.selectedMessages.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val replyingToMessage by viewModel.replyingToMessage.collectAsState()

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
                selectedMessages = selectedMessages,
                isSelectionMode = isSelectionMode,
                replyingToMessage = replyingToMessage,
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
                onSendAudio = viewModel::sendAudioMessage,
                onSendLocation = viewModel::sendLocationMessage,
                onSendContact = viewModel::sendContactMessage,
                onToggleMessageSelection = viewModel::toggleMessageSelection,
                onClearSelection = viewModel::clearSelection,
                onSelectAll = viewModel::selectAllMessages,
                onDeleteSelected = viewModel::deleteSelectedMessages,
                onAddReaction = viewModel::addReaction,
                onSetReplyingTo = viewModel::setReplyingToMessage,
                onCancelReply = viewModel::cancelReply,
                onSendReply = viewModel::sendReply,
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
    selectedMessages: Set<String>,
    isSelectionMode: Boolean,
    replyingToMessage: com.chain.app.domain.model.Message?,
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
    onSendAudio: (android.net.Uri, Long) -> Unit,
    onSendLocation: (Double, Double, String) -> Unit,
    onSendContact: (String, String, String?) -> Unit,
    onToggleMessageSelection: (String) -> Unit,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onAddReaction: (String, String) -> Unit,
    onSetReplyingTo: (com.chain.app.domain.model.Message?) -> Unit,
    onCancelReply: () -> Unit,
    onSendReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showMuteDialog by remember { mutableStateOf(false) }
    var showDisappearingMessagesDialog by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showMessageActionsSheet by remember { mutableStateOf<com.chain.app.domain.model.Message?>(null) }
    var showQuickReactionPicker by remember { mutableStateOf<com.chain.app.domain.model.Message?>(null) }
    var showMessageInfo by remember { mutableStateOf<com.chain.app.domain.model.Message?>(null) }

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
        // Copy content URI to permanent storage
        val permanentUri = copyUriToInternalStorage(context, uri)

        // Determine if it's an image or video based on URI
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType?.startsWith("video") == true) {
            onSendVideo(permanentUri, "", 0)
        } else {
            onSendImage(permanentUri, "")
        }
    }

    val documentPicker = rememberDocumentPicker { uri ->
        // Copy content URI to permanent storage
        val permanentUri = copyUriToInternalStorage(context, uri)
        val fileName = getFileName(context, uri) ?: "document"
        onSendDocument(permanentUri, fileName)
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

    // Location permission state
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
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
        // VoiceRecordButton will handle recording after permission is granted
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            // Permission granted, get current location
            getCurrentLocationAndShare(context, onSendLocation)
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Contact picker launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            val contactData = getContactData(context, it)
            contactData?.let { (name, phone, email) ->
                onSendContact(name, phone, email)
                Toast.makeText(context, "Contact shared!", Toast.LENGTH_SHORT).show()
            }
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
            // Message actions bar (shown in selection mode)
            if (isSelectionMode) {
                MessageActionsBar(
                    selectedCount = selectedMessages.size,
                    onClearSelection = onClearSelection,
                    onDelete = onDeleteSelected,
                    onForward = {
                        // TODO: Implement forward functionality
                        Toast.makeText(context, "Forward coming soon!", Toast.LENGTH_SHORT).show()
                    },
                    onCopy = {
                        // Copy selected messages to clipboard
                        val textToCopy = messages
                            .filter { selectedMessages.contains(it.id) }
                            .joinToString("\n") { it.content }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("messages", textToCopy)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        onClearSelection()
                    },
                    onSelectAll = onSelectAll
                )
            }

            // Chat header
            if (!isSelectionMode) {
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
                    val isSelected = selectedMessages.contains(message.id)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isSelected) {
                                    Modifier.background(GlassAccent.copy(alpha = 0.1f))
                                } else {
                                    Modifier
                                }
                            )
                            .clickable(enabled = isSelectionMode) {
                                onToggleMessageSelection(message.id)
                            }
                    ) {
                        MessageBubble(
                            message = message,
                            isSentByMe = message.senderId == currentUserId,
                            showSender = chat.type == ChatType.GROUP,
                            allMessages = messages,
                            onLongPress = {
                                if (isSelectionMode) {
                                    onToggleMessageSelection(message.id)
                                } else {
                                    showMessageActionsSheet = message
                                }
                            },
                            onDoubleTap = {
                                showQuickReactionPicker = message
                            },
                            onReply = {
                                onSetReplyingTo(message)
                            }
                        )
                    }
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
                            if (replyingToMessage != null) {
                                onSendReply(messageText.text.trim())
                            } else {
                                onSendMessage(messageText.text.trim())
                            }
                            messageText = TextFieldValue("")

                            // Auto-scroll to bottom after sending
                            coroutineScope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        }
                    },
                    onEmojiClick = {
                        showEmojiPicker = true
                    },
                    onAttachmentClick = {
                        showAttachmentMenu = true
                    },
                    onSendAudio = { uri, duration ->
                        onSendAudio(uri, duration)
                    },
                    hasMicrophonePermission = hasMicrophonePermission,
                    onRequestMicrophonePermission = {
                        microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    replyingTo = replyingToMessage,
                    onCancelReply = onCancelReply
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
                        if (hasLocationPermission) {
                            getCurrentLocationAndShare(context, onSendLocation)
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    onContactClick = {
                        showAttachmentMenu = false
                        contactPickerLauncher.launch(null)
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

        // Emoji picker modal
        if (showEmojiPicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                    .clickable { showEmojiPicker = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                EmojiPicker(
                    onEmojiSelected = { emoji ->
                        messageText = TextFieldValue(messageText.text + emoji)
                        showEmojiPicker = false
                    },
                    onDismiss = { showEmojiPicker = false }
                )
            }
        }

        // Single message actions sheet
        showMessageActionsSheet?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                    .clickable { showMessageActionsSheet = null },
                contentAlignment = Alignment.BottomCenter
            ) {
                SingleMessageActionsSheet(
                    onDismiss = { showMessageActionsSheet = null },
                    onReply = {
                        onSetReplyingTo(message)
                        showMessageActionsSheet = null
                    },
                    onForward = {
                        Toast.makeText(context, "Forward: Select chat to forward to", Toast.LENGTH_SHORT).show()
                        showMessageActionsSheet = null
                    },
                    onCopy = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("message", message.content)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        showMessageActionsSheet = null
                    },
                    onDelete = {
                        onToggleMessageSelection(message.id)
                        onDeleteSelected()
                        showMessageActionsSheet = null
                    },
                    onStar = {
                        Toast.makeText(context, "Star coming soon!", Toast.LENGTH_SHORT).show()
                        showMessageActionsSheet = null
                    },
                    onReact = { emoji ->
                        onAddReaction(message.id, emoji)
                    },
                    onInfo = {
                        showMessageInfo = message
                    },
                    isSentByMe = message.senderId == currentUserId
                )
            }
        }

        // Quick reaction picker (on double-tap)
        showQuickReactionPicker?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showQuickReactionPicker = null },
                contentAlignment = Alignment.Center
            ) {
                QuickReactionPicker(
                    onReactionSelected = { emoji ->
                        onAddReaction(message.id, emoji)
                        showQuickReactionPicker = null
                    },
                    onDismiss = { showQuickReactionPicker = null }
                )
            }
        }

        // Message info dialog
        showMessageInfo?.let { message ->
            MessageInfoDialog(
                message = message,
                onDismiss = { showMessageInfo = null }
            )
        }
    }
}

/**
 * Get current location using FusedLocationProviderClient and share it
 */
@android.annotation.SuppressLint("MissingPermission")
private fun getCurrentLocationAndShare(
    context: android.content.Context,
    onSendLocation: (Double, Double, String) -> Unit
) {
    val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                // Try to get address from coordinates
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address = if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        listOfNotNull(
                            addr.thoroughfare, // Street
                            addr.locality, // City
                            addr.adminArea, // State
                            addr.countryName // Country
                        ).joinToString(", ")
                    } else {
                        "Location: $latitude, $longitude"
                    }

                    onSendLocation(latitude, longitude, address)
                    Toast.makeText(context, "Location sent!", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    // Geocoder failed, send with coordinates only
                    onSendLocation(latitude, longitude, "Location: $latitude, $longitude")
                    Toast.makeText(context, "Location sent!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to get location: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    } catch (_: SecurityException) {
        Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Get contact data from contact URI
 */
private fun getContactData(
    context: android.content.Context,
    contactUri: android.net.Uri
): Triple<String, String, String?>? {
    try {
        val cursor = context.contentResolver.query(
            contactUri,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME)
                val idIndex = it.getColumnIndex(android.provider.ContactsContract.Contacts._ID)

                val name = if (nameIndex >= 0) it.getString(nameIndex) else "Unknown"
                val contactId = if (idIndex >= 0) it.getString(idIndex) else return null

                // Get phone number
                val phoneCursor = context.contentResolver.query(
                    android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    "${android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )

                var phone = ""
                phoneCursor?.use { pc ->
                    if (pc.moveToFirst()) {
                        val phoneIndex = pc.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                        phone = if (phoneIndex >= 0) pc.getString(phoneIndex) else ""
                    }
                }

                // Get email (optional)
                val emailCursor = context.contentResolver.query(
                    android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    "${android.provider.ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )

                var email: String? = null
                emailCursor?.use { ec ->
                    if (ec.moveToFirst()) {
                        val emailIndex = ec.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Email.ADDRESS)
                        email = if (emailIndex >= 0) ec.getString(emailIndex) else null
                    }
                }

                return if (phone.isNotBlank()) {
                    Triple(name, phone, email)
                } else {
                    null
                }
            }
        }
    } catch (_: Exception) {
        Toast.makeText(context, "Failed to read contact", Toast.LENGTH_SHORT).show()
    }

    return null
}

/**
 * Copy content URI to app's internal storage for permanent access
 * This is necessary because content:// URIs from gallery lose permissions when app restarts
 */
private fun copyUriToInternalStorage(context: android.content.Context, uri: android.net.Uri): android.net.Uri {
    try {
        // If it's already a file:// URI (like from camera capture), return as-is
        if (uri.scheme == "file") {
            return uri
        }

        // Create a permanent storage directory
        val storageDir = File(context.filesDir, "media")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        // Generate unique filename
        val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
        val extension = getFileExtension(context, uri)
        val fileName = "CHAIN_${timeStamp}.$extension"

        // Create destination file
        val destFile = File(storageDir, fileName)

        // Copy content
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // Return file URI via FileProvider
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            destFile
        )
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Failed to save media: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        return uri // Return original URI as fallback
    }
}

/**
 * Get file extension from content URI
 */
private fun getFileExtension(context: android.content.Context, uri: android.net.Uri): String {
    val mimeType = context.contentResolver.getType(uri)
    return when {
        mimeType?.startsWith("image/") == true -> {
            when {
                mimeType.contains("png") -> "png"
                mimeType.contains("gif") -> "gif"
                mimeType.contains("webp") -> "webp"
                else -> "jpg"
            }
        }
        mimeType?.startsWith("video/") == true -> "mp4"
        mimeType?.contains("pdf") == true -> "pdf"
        else -> {
            // Try to get extension from URI path
            uri.lastPathSegment?.substringAfterLast('.', "dat") ?: "dat"
        }
    }
}

/**
 * Get filename from content URI
 */
private fun getFileName(context: android.content.Context, uri: android.net.Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    result = it.getString(nameIndex)
                }
            }
        }
    }
    if (result == null) {
        result = uri.lastPathSegment
    }
    return result
}
