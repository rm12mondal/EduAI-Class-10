package com.example.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.data.media.NotebookImageHelper

/**
 * Decodes a local image file asynchronously on `Dispatchers.IO` using the in-memory `LruCache`
 * in [NotebookImageHelper].
 *
 * - On cache hit: returns immediately on the first composition frame with zero flicker and zero I/O.
 * - On cache miss: returns `null` initially and decodes a downsampled compressed thumbnail
 *   on `Dispatchers.IO`, never blocking the Compose UI thread during scrolling.
 */
@Composable
fun rememberAsyncCachedImageBitmap(
    path: String?,
    maxSizePx: Int = 480
): ImageBitmap? {
    val cached = if (path.isNullOrBlank()) {
        null
    } else {
        NotebookImageHelper.getCachedThumbnail(path, maxSizePx)?.asImageBitmap()
    }

    val loaded by produceState<ImageBitmap?>(
        initialValue = cached,
        key1 = path,
        key2 = maxSizePx
    ) {
        if (path.isNullOrBlank()) {
            value = null
            return@produceState
        }
        if (value != null) return@produceState
        val bmp = NotebookImageHelper.loadThumbnailAsync(path, maxSizePx)
        value = bmp?.asImageBitmap()
    }
    return loaded
}
