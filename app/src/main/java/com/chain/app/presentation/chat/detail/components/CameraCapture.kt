package com.chain.app.presentation.chat.detail.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Camera capture launcher for taking photos
 * Uses the system camera app for capture
 */
@Composable
fun rememberCameraCapture(
    onPhotoTaken: (Uri) -> Unit
): CameraCaptureState {
    val context = LocalContext.current

    var currentPhotoUri: Uri? by remember { mutableStateOf(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Photo was taken successfully, call callback with the URI
            currentPhotoUri?.let { uri ->
                onPhotoTaken(uri)
            }
        }
    }

    return remember(cameraLauncher) {
        CameraCaptureState(
            context = context,
            cameraLauncher = cameraLauncher,
            onUriCreated = { uri -> currentPhotoUri = uri }
        )
    }
}

/**
 * State holder for camera capture
 */
class CameraCaptureState(
    private val context: Context,
    private val cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    private val onUriCreated: (Uri) -> Unit
) {
    /**
     * Launch camera to take a photo
     */
    fun takePhoto() {
        val photoUri = createImageUri()
        cameraLauncher.launch(photoUri)
    }

    /**
     * Create a temporary file URI for the photo
     */
    private fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "CHAIN_${timeStamp}.jpg"

        val storageDir = File(context.cacheDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val imageFile = File(storageDir, imageFileName)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        ).also { uri ->
            // Store the URI so the callback can use it after capture
            onUriCreated(uri)
        }
    }
}

/**
 * Video capture launcher for recording videos
 */
@Composable
fun rememberVideoCapture(
    onVideoRecorded: (Uri) -> Unit
): VideoCaptureState {
    val context = LocalContext.current

    var currentVideoUri: Uri? by remember { mutableStateOf(null) }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            // Video was recorded successfully, call callback with the URI
            currentVideoUri?.let { uri ->
                onVideoRecorded(uri)
            }
        }
    }

    return remember(videoLauncher) {
        VideoCaptureState(
            context = context,
            videoLauncher = videoLauncher,
            onUriCreated = { uri -> currentVideoUri = uri }
        )
    }
}

/**
 * State holder for video capture
 */
class VideoCaptureState(
    private val context: Context,
    private val videoLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    private val onUriCreated: (Uri) -> Unit
) {
    /**
     * Launch camera to record a video
     */
    fun recordVideo() {
        val videoUri = createVideoUri()
        videoLauncher.launch(videoUri)
    }

    /**
     * Create a temporary file URI for the video
     */
    private fun createVideoUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "CHAIN_VIDEO_${timeStamp}.mp4"

        val storageDir = File(context.cacheDir, "videos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val videoFile = File(storageDir, videoFileName)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            videoFile
        ).also { uri ->
            // Store the URI so the callback can use it after capture
            onUriCreated(uri)
        }
    }
}
