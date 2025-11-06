package com.chain.app.presentation.contacts

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.components.glass.GlassButton
import com.chain.app.presentation.theme.*

/**
 * QR Code Scanner Screen for adding contacts via QR code
 *
 * Note: Actual camera integration requires:
 * - CameraX library
 * - ML Kit Barcode Scanning
 * - Camera permissions
 *
 * This is a placeholder UI that will be connected when camera functionality is implemented
 */
@Composable
fun QRCodeScannerScreen(
    onBackClick: () -> Unit,
    onQRCodeScanned: (String) -> Unit,
    onShowMyQRCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isScanningActive by remember { mutableStateOf(true) }
    var hasPermission by remember { mutableStateOf(false) }

    // Background gradient
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
                            text = "Scan QR Code",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GlassText,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = onShowMyQRCode,
                            modifier = Modifier
                                .size(40.dp)
                                .glassIconButton()
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "My QR Code",
                                tint = GlassText
                            )
                        }
                    }
                }
            }

            // Camera preview area (placeholder)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (!hasPermission) {
                    // Permission request UI
                    CameraPermissionRequest(
                        onRequestPermission = {
                            // TODO: Request camera permission
                            hasPermission = true
                        }
                    )
                } else if (isScanningActive) {
                    // Scanning active UI
                    QRCodeScannerView(
                        onQRCodeDetected = { qrData ->
                            isScanningActive = false
                            onQRCodeScanned(qrData)
                        }
                    )
                } else {
                    // Scanning paused
                    ScanningPausedView(
                        onResume = { isScanningActive = true }
                    )
                }
            }

            // Instructions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glass(shape = RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GlassAccent,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Point your camera at a QR code to add contact",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionRequest(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GlassTextSecondary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleLarge,
            color = GlassText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Chain needs access to your camera to scan QR codes",
            style = MaterialTheme.typography.bodyMedium,
            color = GlassTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        GlassButton(
            onClick = onRequestPermission
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Grant Permission")
        }
    }
}

@Composable
private fun QRCodeScannerView(
    onQRCodeDetected: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Camera preview would go here
        // For now, show a placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            // Scanning frame
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .border(
                        width = 3.dp,
                        color = GlassAccent,
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            // Corner indicators
            Box(modifier = Modifier.size(250.dp)) {
                // Top-left corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(40.dp, 5.dp)
                        .background(GlassAccent, RoundedCornerShape(topStart = 16.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(5.dp, 40.dp)
                        .background(GlassAccent, RoundedCornerShape(topStart = 16.dp))
                )

                // Top-right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(40.dp, 5.dp)
                        .background(GlassAccent, RoundedCornerShape(topEnd = 16.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(5.dp, 40.dp)
                        .background(GlassAccent, RoundedCornerShape(topEnd = 16.dp))
                )

                // Bottom-left corner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(40.dp, 5.dp)
                        .background(GlassAccent, RoundedCornerShape(bottomStart = 16.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(5.dp, 40.dp)
                        .background(GlassAccent, RoundedCornerShape(bottomStart = 16.dp))
                )

                // Bottom-right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(40.dp, 5.dp)
                        .background(GlassAccent, RoundedCornerShape(bottomEnd = 16.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(5.dp, 40.dp)
                        .background(GlassAccent, RoundedCornerShape(bottomEnd = 16.dp))
                )
            }

            // Scanning line animation (placeholder)
            Text(
                text = "📷 Camera Preview\n(Coming Soon)",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = GlassText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 300.dp)
            )
        }
    }
}

@Composable
private fun ScanningPausedView(
    onResume: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Pause,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GlassAccent
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Scanning Paused",
            style = MaterialTheme.typography.titleLarge,
            color = GlassText
        )

        Spacer(modifier = Modifier.height(24.dp))

        GlassButton(
            onClick = onResume
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Resume Scanning")
        }
    }
}
