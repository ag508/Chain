package com.chain.app.presentation.chat.detail.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Media picker for selecting photos and videos
 * Uses the Android Photo Picker API for a consistent experience
 */
@Composable
fun rememberMediaPicker(
    onMediaSelected: (Uri) -> Unit
): MediaPickerState {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onMediaSelected(it) }
    }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        uris.forEach { uri ->
            onMediaSelected(uri)
        }
    }

    return remember(photoPickerLauncher, multiplePhotoPickerLauncher) {
        MediaPickerState(
            context = context,
            photoPickerLauncher = photoPickerLauncher,
            multiplePhotoPickerLauncher = multiplePhotoPickerLauncher
        )
    }
}

/**
 * State holder for media picker
 */
class MediaPickerState(
    private val context: Context,
    private val photoPickerLauncher: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
    private val multiplePhotoPickerLauncher: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>
) {
    /**
     * Launch photo picker for selecting images only
     */
    fun pickImage() {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    /**
     * Launch photo picker for selecting videos only
     */
    fun pickVideo() {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }

    /**
     * Launch photo picker for selecting images or videos
     */
    fun pickImageOrVideo() {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        )
    }

    /**
     * Launch photo picker for selecting multiple images
     */
    fun pickMultipleImages() {
        multiplePhotoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}

/**
 * Document picker for selecting files
 */
@Composable
fun rememberDocumentPicker(
    onDocumentSelected: (Uri) -> Unit
): DocumentPickerState {
    val context = LocalContext.current

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onDocumentSelected(it) }
    }

    return remember(documentPickerLauncher) {
        DocumentPickerState(
            context = context,
            documentPickerLauncher = documentPickerLauncher
        )
    }
}

/**
 * State holder for document picker
 */
class DocumentPickerState(
    private val context: Context,
    private val documentPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    /**
     * Launch document picker for all file types
     */
    fun pickDocument() {
        documentPickerLauncher.launch("*/*")
    }

    /**
     * Launch document picker for PDF files
     */
    fun pickPdf() {
        documentPickerLauncher.launch("application/pdf")
    }

    /**
     * Launch document picker for specific MIME type
     */
    fun pickFileOfType(mimeType: String) {
        documentPickerLauncher.launch(mimeType)
    }
}

/**
 * Helper functions for validating media files
 */
object MediaValidator {
    // Maximum file sizes
    const val MAX_IMAGE_SIZE_MB = 10
    const val MAX_VIDEO_SIZE_MB = 100
    const val MAX_DOCUMENT_SIZE_MB = 50

    private const val BYTES_PER_MB = 1024 * 1024

    /**
     * Check if file size is within limits
     */
    fun isFileSizeValid(
        context: Context,
        uri: Uri,
        maxSizeMB: Int
    ): Boolean {
        val fileSize = getFileSize(context, uri)
        return fileSize <= maxSizeMB * BYTES_PER_MB
    }

    /**
     * Get file size in bytes
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.available().toLong()
        } ?: 0L
    }

    /**
     * Get file size in MB
     */
    fun getFileSizeInMB(context: Context, uri: Uri): Double {
        val sizeInBytes = getFileSize(context, uri)
        return sizeInBytes.toDouble() / BYTES_PER_MB
    }

    /**
     * Get MIME type of file
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    /**
     * Check if URI is an image
     */
    fun isImage(context: Context, uri: Uri): Boolean {
        return getMimeType(context, uri)?.startsWith("image/") == true
    }

    /**
     * Check if URI is a video
     */
    fun isVideo(context: Context, uri: Uri): Boolean {
        return getMimeType(context, uri)?.startsWith("video/") == true
    }

    /**
     * Check if URI is a document
     */
    fun isDocument(context: Context, uri: Uri): Boolean {
        val mimeType = getMimeType(context, uri)
        return mimeType?.startsWith("application/") == true ||
                mimeType?.startsWith("text/") == true
    }
}
