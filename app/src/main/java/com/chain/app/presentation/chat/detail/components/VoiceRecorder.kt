package com.chain.app.presentation.chat.detail.components

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chain.app.presentation.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

/**
 * Voice recording button with slide-to-cancel functionality.
 */
@Composable
fun VoiceRecordButton(
    onRecordingComplete: (Uri, Long) -> Unit,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0L) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var outputFile: File? by remember { mutableStateOf(null) }

    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordingDuration++
            }
        } else {
            recordingDuration = 0
        }
    }

    // Cleanup on unmount
    DisposableEffect(Unit) {
        onDispose {
            recorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    // Ignore
                }
                release()
            }
        }
    }

    if (isRecording) {
        RecordingIndicator(
            duration = recordingDuration,
            onCancel = {
                recorder?.apply {
                    try {
                        stop()
                    } catch (e: Exception) {
                        // Ignore
                    }
                    release()
                }
                outputFile?.delete()
                recorder = null
                isRecording = false
            },
            onSend = {
                recorder?.apply {
                    try {
                        stop()
                    } catch (e: Exception) {
                        // Ignore
                    }
                    release()
                }
                outputFile?.let { file ->
                    onRecordingComplete(Uri.fromFile(file), recordingDuration * 1000)
                }
                recorder = null
                isRecording = false
            },
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        IconButton(
            onClick = {
                if (hasPermission) {
                    // Start recording
                    scope.launch {
                        val file = createAudioFile(context)
                        outputFile = file

                        try {
                            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                MediaRecorder(context)
                            } else {
                                @Suppress("DEPRECATION")
                                MediaRecorder()
                            }.apply {
                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                setOutputFile(file.absolutePath)
                                setAudioEncodingBitRate(128000)
                                setAudioSamplingRate(44100)

                                prepare()
                                start()
                            }

                            isRecording = true
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    onRequestPermission()
                }
            },
            modifier = modifier
                .size(48.dp)
                .scale(if (isRecording) scale else 1f)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Record voice",
                tint = if (isRecording) ChainError else GlassAccent
            )
        }
    }
}

/**
 * Recording indicator with timer and controls.
 */
@Composable
private fun RecordingIndicator(
    duration: Long,
    onCancel: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    val cancelThreshold = -200f

    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .glass()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX < cancelThreshold) {
                            onCancel()
                        } else {
                            onSend()
                        }
                        offsetX = 0f
                    },
                    onDrag = { _, dragAmount ->
                        offsetX += dragAmount.x
                    }
                )
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Cancel button
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel",
                tint = ChainError
            )
        }

        // Recording info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pulsing red dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(ChainError)
            )

            // Timer
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = GlassText
            )
        }

        // Slide to cancel hint
        Text(
            text = "← Slide to cancel",
            style = MaterialTheme.typography.bodySmall,
            color = GlassText.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

/**
 * Format duration in mm:ss format.
 */
private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

/**
 * Create a temporary audio file.
 */
private fun createAudioFile(context: Context): File {
    val timeStamp = System.currentTimeMillis()
    val fileName = "VOICE_$timeStamp.m4a"
    return File(context.cacheDir, fileName)
}
