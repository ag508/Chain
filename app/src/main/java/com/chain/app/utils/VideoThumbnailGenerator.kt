package com.chain.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility for generating video thumbnails.
 */
object VideoThumbnailGenerator {

    /**
     * Generate a thumbnail for a video at the specified time.
     *
     * @param videoUri URI of the video file
     * @param timeUs Time in microseconds to capture the frame (default: 1 second)
     * @param width Desired thumbnail width (default: 320px)
     * @param height Desired thumbnail height (default: 180px)
     * @return Bitmap of the thumbnail, or null if generation fails
     */
    suspend fun generateThumbnail(
        context: Context,
        videoUri: Uri,
        timeUs: Long = 1_000_000L, // 1 second
        width: Int = 320,
        height: Int = 180
    ): Bitmap? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, videoUri)

            // Get frame at specified time
            val bitmap = retriever.getFrameAtTime(
                timeUs,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )

            // Scale to desired size
            bitmap?.let {
                Bitmap.createScaledBitmap(it, width, height, true)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate video thumbnail")
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Timber.e(e, "Failed to release MediaMetadataRetriever")
            }
        }
    }

    /**
     * Generate and save a thumbnail to cache directory.
     *
     * @param videoUri URI of the video file
     * @param context Application context
     * @return URI of the saved thumbnail, or null if generation fails
     */
    suspend fun generateAndSaveThumbnail(
        context: Context,
        videoUri: Uri,
        timeUs: Long = 1_000_000L
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap = generateThumbnail(context, videoUri, timeUs) ?: return@withContext null

            // Create thumbnail file
            val thumbnailFile = File(
                context.cacheDir,
                "thumbnail_${System.currentTimeMillis()}.jpg"
            )

            // Save bitmap to file
            FileOutputStream(thumbnailFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            Timber.d("Thumbnail saved: ${thumbnailFile.absolutePath}")
            thumbnailFile.toUri()
        } catch (e: IOException) {
            Timber.e(e, "Failed to save thumbnail")
            null
        }
    }

    /**
     * Get video duration in milliseconds.
     */
    suspend fun getVideoDuration(
        context: Context,
        videoUri: Uri
    ): Long = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, videoUri)

            val durationStr = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )

            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Timber.e(e, "Failed to get video duration")
            0L
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Timber.e(e, "Failed to release MediaMetadataRetriever")
            }
        }
    }

    /**
     * Get video dimensions.
     */
    suspend fun getVideoDimensions(
        context: Context,
        videoUri: Uri
    ): Pair<Int, Int>? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, videoUri)

            val width = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
            )?.toIntOrNull() ?: return@withContext null

            val height = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
            )?.toIntOrNull() ?: return@withContext null

            Pair(width, height)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get video dimensions")
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Timber.e(e, "Failed to release MediaMetadataRetriever")
            }
        }
    }

    /**
     * Generate multiple thumbnails for video preview/scrubbing.
     *
     * @param videoUri URI of the video file
     * @param count Number of thumbnails to generate
     * @return List of bitmaps at evenly spaced intervals
     */
    suspend fun generateThumbnailStrip(
        context: Context,
        videoUri: Uri,
        count: Int = 5
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        val thumbnails = mutableListOf<Bitmap>()

        try {
            retriever.setDataSource(context, videoUri)

            val duration = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: return@withContext emptyList()

            val interval = duration / (count + 1)

            for (i in 1..count) {
                val timeUs = (interval * i) * 1000 // Convert to microseconds

                val bitmap = retriever.getFrameAtTime(
                    timeUs,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )

                bitmap?.let {
                    val scaled = Bitmap.createScaledBitmap(it, 160, 90, true)
                    thumbnails.add(scaled)
                }
            }

            thumbnails
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate thumbnail strip")
            emptyList()
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Timber.e(e, "Failed to release MediaMetadataRetriever")
            }
        }
    }
}
