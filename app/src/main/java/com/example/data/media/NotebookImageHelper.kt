package com.example.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Zero-storage-permission helper for creating FileProvider camera capture URIs,
 * downsampling notebook photos well under Firebase AI Logic's 20MB inline limit,
 * and persisting local thumbnails for chat history display.
 */
object NotebookImageHelper {
    private const val TAG = "NotebookImageHelper"

    fun createTempCameraImageUri(context: Context): Uri {
        val photosDir = File(context.cacheDir, "notebook_photos").apply {
            if (!exists()) mkdirs()
        }
        val imageFile = File(photosDir, "notebook_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    suspend fun loadScaledBitmap(
        context: Context,
        uri: Uri,
        maxDimensionPx: Int = 1440
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val rawWidth = boundsOptions.outWidth
            val rawHeight = boundsOptions.outHeight
            if (rawWidth <= 0 || rawHeight <= 0) return@withContext null

            var sampleSize = 1
            val longestSide = max(rawWidth, rawHeight)
            while (longestSide / (sampleSize * 2) >= maxDimensionPx) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decoded = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@withContext null

            val maxCurrent = max(decoded.width, decoded.height)
            if (maxCurrent > maxDimensionPx) {
                val scale = maxDimensionPx.toFloat() / maxCurrent.toFloat()
                val targetW = (decoded.width * scale).toInt().coerceAtLeast(1)
                val targetH = (decoded.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
                if (scaled !== decoded) decoded.recycle()
                scaled
            } else {
                decoded
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load scaled bitmap from uri=$uri", e)
            null
        }
    }

    suspend fun persistThumbnail(context: Context, bitmap: Bitmap): String? =
        withContext(Dispatchers.IO) {
            try {
                val uploadsDir = File(context.filesDir, "chat_uploads").apply {
                    if (!exists()) mkdirs()
                }
                val outFile = File(uploadsDir, "note_${System.currentTimeMillis()}.jpg")
                FileOutputStream(outFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 82, fos)
                    fos.flush()
                }
                outFile.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist notebook image thumbnail", e)
                null
            }
        }

    fun decodeFileThumbnail(path: String, maxSizePx: Int = 720): Bitmap? {
        return try {
            val file = File(path)
            if (!file.exists()) return null
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, opts)
            val longest = max(opts.outWidth, opts.outHeight)
            var sample = 1
            while (longest / (sample * 2) >= maxSizePx) sample *= 2
            BitmapFactory.decodeFile(
                path,
                BitmapFactory.Options().apply { inSampleSize = sample }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode thumbnail $path", e)
            null
        }
    }
}
