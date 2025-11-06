package com.chain.app.presentation.chat.detail.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Photo was taken successfully
            // The URI passed to takePicture() contains the photo
        }
    }

    return remember(cameraLauncher) {
        CameraCaptureState(
            context = context,
            cameraLauncher = cameraLauncher,
            onPhotoTaken = onPhotoTaken
        )
    }
}

/**
 * State holder for camera capture
 */
class CameraCaptureState(
    private val context: Context,
    private val cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    private val onPhotoTaken: (Uri) -> Unit
) {
    private var currentPhotoUri: Uri? = null

    /**
     * Launch camera to take a photo
     */
    fun takePhoto() {
        val photoUri = createImageUri()
        currentPhotoUri = photoUri
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
            // Store the URI so we can use it after capture
            currentPhotoUri = uri
            onPhotoTaken(uri)
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

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            // Video was recorded successfully
        }
    }

    return remember(videoLauncher) {
        VideoCaptureState(
            context = context,
            videoLauncher = videoLauncher,
            onVideoRecorded = onVideoRecorded
        )
    }
}

/**
 * State holder for video capture
 */
class VideoCaptureState(
    private val context: Context,
    private val videoLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    private val onVideoRecorded: (Uri) -> Unit
) {
    private var currentVideoUri: Uri? = null

    /**
     * Launch camera to record a video
     */
    fun recordVideo() {
        val videoUri = createVideoUri()
        currentVideoUri = videoUri
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
            currentVideoUri = uri
            onVideoRecorded(uri)
        }
    }
}
