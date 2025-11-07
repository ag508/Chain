package com.chain.app.presentation.profile

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.chain.app.domain.model.User
import com.chain.app.presentation.components.glass.GlassButton
import com.chain.app.presentation.components.glass.GlassTextField
import com.chain.app.presentation.contacts.QRCodeGenerator
import com.chain.app.presentation.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bgBrush)
            .systemBarsPadding()
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = GlassAccent
                )
            }
            is ProfileUiState.Success -> {
                if (isEditing) {
                    EditProfileContent(
                        user = state.user,
                        onBackClick = { viewModel.cancelEditing() },
                        onSave = { displayName, avatar, about ->
                            viewModel.updateProfile(
                                displayName = displayName,
                                avatar = avatar,
                                about = about,
                                onSuccess = { /* Show success message */ },
                                onError = { /* Show error message */ }
                            )
                        }
                    )
                } else {
                    ProfileContent(
                        user = state.user,
                        onBackClick = onBackClick,
                        onEditClick = { viewModel.startEditing() }
                    )
                }
            }
            is ProfileUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadProfile() },
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: User,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header with back button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glass()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .glassIconButton()
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = GlassText
                )
            }

            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall,
                color = GlassText,
                modifier = Modifier.align(Alignment.Center)
            )

            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .glassIconButton()
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = GlassAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile photo
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(120.dp)
                .clip(CircleShape)
                .border(3.dp, GlassBorder, CircleShape)
                .clickable { /* TODO: View full-screen photo */ },
            contentAlignment = Alignment.Center
        ) {
            if (user.avatar != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.avatar)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .glass(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile photo",
                        tint = GlassText,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display name
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = GlassText,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Phone number
        Text(
            text = user.phoneNumber,
            style = MaterialTheme.typography.bodyLarge,
            color = GlassText.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Info sections
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileInfoCard(
                icon = Icons.Default.Info,
                label = "About",
                value = user.about ?: "Hey there! I'm using Chain."
            )

            ProfileInfoCard(
                icon = Icons.Default.Key,
                label = "Public Key",
                value = user.publicKey?.take(16) ?: "Not available"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // QR Code section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .glass()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "My QR Code",
                style = MaterialTheme.typography.titleMedium,
                color = GlassText,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Generate QR code
            val qrData = QRCodeGenerator.generateUserQRData(
                userId = user.id,
                phoneNumber = user.phoneNumber,
                displayName = user.displayName,
                publicKey = user.publicKey
            )
            val qrBitmap = remember(user.id, user.phoneNumber, user.displayName) {
                QRCodeGenerator.generateQRCode(qrData, size = 400)
            }

            qrBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlassButton(
                    onClick = {
                        try {
                            // Save QR code to cache directory
                            val cachePath = File(context.cacheDir, "images")
                            cachePath.mkdirs()
                            val file = File(cachePath, "qr_code.png")
                            val outputStream = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.flush()
                            outputStream.close()

                            // Get URI using FileProvider
                            val contentUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )

                            // Create share intent
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, contentUri)
                                putExtra(Intent.EXTRA_TEXT, "Add me on Chain! Phone: ${user.phoneNumber}")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = GlassText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Profile", color = GlassText)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun EditProfileContent(
    user: User,
    onBackClick: () -> Unit,
    onSave: (String, String?, String?) -> Unit
) {
    var displayName by remember { mutableStateOf(user.displayName) }
    var about by remember { mutableStateOf(user.about ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glass()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .glassIconButton()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = GlassText
                )
            }

            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineSmall,
                color = GlassText,
                modifier = Modifier.align(Alignment.Center)
            )

            TextButton(
                onClick = {
                    if (displayName.isNotBlank()) {
                        onSave(
                            displayName,
                            selectedImageUri?.toString(),
                            about.takeIf { it.isNotBlank() }
                        )
                    } else {
                        errorMessage = "Display name cannot be empty"
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("Save", color = GlassAccent, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile photo with edit option
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(120.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, GlassBorder, CircleShape)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    selectedImageUri != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    user.avatar != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user.avatar)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .glass(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile photo",
                                tint = GlassText,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassAccent)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change photo",
                    tint = GlassText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Edit fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = "Display Name",
                placeholder = "Enter your name",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = GlassText.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            GlassTextField(
                value = about,
                onValueChange = { about = it },
                label = "About",
                placeholder = "Hey there! I'm using Chain.",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GlassText.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Phone number (read-only)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = GlassText.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Phone Number",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassText.copy(alpha = 0.6f)
                        )
                        Text(
                            text = user.phoneNumber,
                            style = MaterialTheme.typography.bodyLarge,
                            color = GlassText
                        )
                    }
                }
            }
        }

        // Error message
        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GlassAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassText.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GlassText
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = GlassText
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassButton(onClick = onBackClick) {
                Text("Go Back")
            }
            GlassButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
