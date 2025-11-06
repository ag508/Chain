package com.chain.app.presentation.contacts

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.usecase.user.GetCurrentUserUseCase
import com.chain.app.presentation.components.glass.GlassCard
import com.chain.app.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Screen that displays the user's QR code for others to scan.
 */
@Composable
fun MyQRCodeScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyQRCodeViewModel = hiltViewModel()
) {
    val qrBitmap by viewModel.qrBitmap.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bgBrush)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glass(shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = GlassText
                            )
                        }

                        Text(
                            text = "My QR Code",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GlassText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(color = GlassAccent)
                    }
                    qrBitmap != null -> {
                        // User name
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassText,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        // QR Code
                        GlassCard(
                            modifier = Modifier
                                .size(300.dp)
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = qrBitmap!!.asImageBitmap(),
                                    contentDescription = "My QR Code",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            2.dp,
                                            GlassBorder.copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Instructions
                        Text(
                            text = "Ask your friend to scan this QR code\nto add you as a contact",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassText.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        // Error state
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = ChainError,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Failed to generate QR code",
                                style = MaterialTheme.typography.bodyLarge,
                                color = GlassText
                            )
                        }
                    }
                }
            }
        }
    }
}

@HiltViewModel
class MyQRCodeViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        generateQRCode()
    }

    private fun generateQRCode() {
        viewModelScope.launch {
            getCurrentUserUseCase().fold(
                onSuccess = { user ->
                    _userName.value = user.displayName

                    // Generate QR code data
                    val qrData = QRCodeGenerator.generateUserQRData(
                        userId = user.id,
                        phoneNumber = user.phoneNumber,
                        displayName = user.displayName,
                        publicKey = user.publicKey
                    )

                    // Generate QR code bitmap
                    _qrBitmap.value = QRCodeGenerator.generateQRCode(qrData, size = 512)
                    _isLoading.value = false
                },
                onFailure = {
                    _isLoading.value = false
                }
            )
        }
    }
}
