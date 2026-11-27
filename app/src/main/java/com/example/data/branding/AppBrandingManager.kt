package com.example.data.branding

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.media.NotebookImageHelper
import com.example.model.LauncherDisguisePreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Manages:
 * 1) Dynamic Android Home Screen Launcher Icon & Name switching via `<activity-alias>`
 *    (`PackageManager.setComponentEnabledSetting`) using a strict allowlist of 5 presets.
 * 2) Custom Gallery Logo crop, compression, and storage strictly inside `filesDir/custom_branding/`.
 * 3) Updating Android Recent Apps (`ActivityManager.TaskDescription`) so zero default branding leaks.
 */
object AppBrandingManager {

    private const val TAG = "AppBrandingManager"
    private const val BRANDING_DIR = "custom_branding"
    private const val CUSTOM_LOGO_FILENAME = "custom_app_logo.jpg"
    private const val LOGO_TARGET_PX = 512
    const val MAX_CUSTOM_TITLE_LENGTH = 30

    @Volatile
    private var cachedLogoPath: String? = null

    @Volatile
    private var cachedLogoMtime: Long = -1L

    @Volatile
    private var cachedLogoBitmap: Bitmap? = null

    /**
     * Switches the active Home Screen launcher alias safely:
     * enables the chosen alias FIRST before disabling the remaining 4 aliases,
     * with `PackageManager.DONT_KILL_APP` so the app never crashes mid-switch.
     */
    fun switchLauncherAlias(context: Context, targetPreset: LauncherDisguisePreset): Boolean {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName

            // 1. Enable the target alias first so there is always at least 1 active launcher entry
            val targetComponent = ComponentName(pkg, targetPreset.aliasClassName)
            pm.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // 2. Disable all other aliases
            LauncherDisguisePreset.entries.forEach { preset ->
                if (preset != targetPreset) {
                    val other = ComponentName(pkg, preset.aliasClassName)
                    pm.setComponentEnabledSetting(
                        other,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed switching launcher alias to ${targetPreset.id}", e)
            false
        }
    }

    /**
     * Reads current enabled `<activity-alias>` from PackageManager.
     */
    fun detectActiveLauncherPreset(context: Context): LauncherDisguisePreset {
        val pm = context.packageManager
        val pkg = context.packageName
        LauncherDisguisePreset.entries.forEach { preset ->
            val state = runCatching {
                pm.getComponentEnabledSetting(ComponentName(pkg, preset.aliasClassName))
            }.getOrDefault(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return preset
            }
        }
        return LauncherDisguisePreset.DEFAULT_EDUAI
    }

    /**
     * Loads a user-selected Gallery photo URI, center-crops to a crisp 512x512 square,
     * and saves strictly inside `filesDir/custom_branding/custom_app_logo.jpg`.
     */
    suspend fun saveCustomGalleryLogo(context: Context, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val decoded = NotebookImageHelper.loadScaledBitmap(
                    context = context,
                    uri = uri,
                    maxDimensionPx = 768
                ) ?: return@withContext null

                val square = centerCropSquare(decoded, LOGO_TARGET_PX)
                if (square !== decoded) decoded.recycle()

                val dir = File(context.filesDir, BRANDING_DIR).apply { mkdirs() }
                val outFile = File(dir, CUSTOM_LOGO_FILENAME)
                if (!isConfinedToBrandingDir(context, outFile)) {
                    square.recycle()
                    return@withContext null
                }

                FileOutputStream(outFile).use { fos ->
                    square.compress(Bitmap.CompressFormat.JPEG, 86, fos)
                }
                // Warm in-memory cache immediately with the saved bitmap (no extra disk read needed)
                cachedLogoPath = outFile.absolutePath
                cachedLogoMtime = outFile.lastModified()
                cachedLogoBitmap = square
                outFile.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Failed saving custom gallery logo", e)
                null
            }
        }

    suspend fun deleteCustomGalleryLogo(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            cachedLogoPath = null
            cachedLogoMtime = -1L
            cachedLogoBitmap = null
            val file = File(File(context.filesDir, BRANDING_DIR), CUSTOM_LOGO_FILENAME)
            if (!isConfinedToBrandingDir(context, file)) return@withContext false
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            Log.e(TAG, "Failed deleting custom gallery logo", e)
            false
        }
    }

    /**
     * Non-blocking in-memory cache read safe to call during Compose initial state setup.
     */
    fun getCachedCustomLogoBitmap(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        val bmp = cachedLogoBitmap
        return if (cachedLogoPath == path && bmp != null && !bmp.isRecycled) bmp else null
    }

    /**
     * Asynchronously decodes the custom gallery logo on `Dispatchers.IO` with in-memory caching.
     */
    suspend fun decodeCustomLogoBitmapAsync(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        getCachedCustomLogoBitmap(path)?.let { return it }
        return withContext(Dispatchers.IO) {
            decodeCustomLogoBitmap(path)
        }
    }

    fun decodeCustomLogoBitmap(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        val mtime = file.lastModified()
        val existing = cachedLogoBitmap
        if (cachedLogoPath == path && cachedLogoMtime == mtime && existing != null && !existing.isRecycled) {
            return existing
        }
        val decoded = runCatching {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, opts)
            val longest = maxOf(opts.outWidth, opts.outHeight)
            var sample = 1
            while (longest / (sample * 2) >= LOGO_TARGET_PX) sample *= 2
            BitmapFactory.decodeFile(
                file.absolutePath,
                BitmapFactory.Options().apply { inSampleSize = sample }
            )
        }.getOrNull()
        if (decoded != null) {
            cachedLogoPath = path
            cachedLogoMtime = mtime
            cachedLogoBitmap = decoded
        }
        return decoded
    }

    fun sanitizeCustomTitle(raw: String): String {
        val cleaned = raw
            .replace(Regex("[\\p{Cntrl}]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return cleaned.take(MAX_CUSTOM_TITLE_LENGTH)
    }

    /**
     * Updates the Android Recent Apps task switcher card label and icon dynamically
     * (`ActivityManager.TaskDescription`) so zero default branding remains visible.
     */
    /**
     * Async-safe Recent Apps TaskDescription updater: decodes logo/icon strictly on `Dispatchers.IO`
     * and applies `Activity.setTaskDescription` on the Main thread.
     */
    @Suppress("DEPRECATION")
    suspend fun applyTaskDescriptionAsync(
        activity: Activity,
        effectiveTitle: String,
        customLogoPath: String?,
        preset: LauncherDisguisePreset
    ) {
        val safeTitle = effectiveTitle.trim().ifBlank { preset.launcherLabel }
        val (customBmp, iconBmp) = withContext(Dispatchers.IO) {
            val c = decodeCustomLogoBitmap(customLogoPath)
            val i = c ?: renderPresetIconBitmap(activity.applicationContext, preset)
            c to i
        }
        withContext(Dispatchers.Main) {
            try {
                if (activity.isFinishing || activity.isDestroyed) return@withContext
                val taskDesc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val builder = ActivityManager.TaskDescription.Builder()
                        .setLabel(safeTitle)
                    if (customBmp == null && preset.iconDrawableRes != null) {
                        builder.setIcon(preset.iconDrawableRes)
                    }
                    builder.build()
                } else {
                    ActivityManager.TaskDescription(safeTitle, iconBmp)
                }
                activity.setTaskDescription(taskDesc)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                    iconBmp != null
                ) {
                    activity.setTaskDescription(ActivityManager.TaskDescription(safeTitle, iconBmp))
                }
            } catch (e: Exception) {
                Log.w(TAG, "TaskDescription update skipped: ${e.localizedMessage}")
            }
        }
    }

    @Suppress("DEPRECATION")
    fun applyTaskDescription(
        activity: Activity,
        effectiveTitle: String,
        customLogoPath: String?,
        preset: LauncherDisguisePreset
    ) {
        try {
            val safeTitle = effectiveTitle.trim().ifBlank { preset.launcherLabel }
            val customBmp = getCachedCustomLogoBitmap(customLogoPath)
            val iconBmp = customBmp ?: renderPresetIconBitmap(activity, preset)

            val taskDesc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val builder = ActivityManager.TaskDescription.Builder()
                    .setLabel(safeTitle)
                if (customBmp == null && preset.iconDrawableRes != null) {
                    builder.setIcon(preset.iconDrawableRes)
                }
                builder.build()
            } else {
                ActivityManager.TaskDescription(safeTitle, iconBmp)
            }
            activity.setTaskDescription(taskDesc)
        } catch (e: Exception) {
            Log.w(TAG, "TaskDescription update skipped: ${e.localizedMessage}")
        }
    }

    private fun renderPresetIconBitmap(context: Context, preset: LauncherDisguisePreset): Bitmap? {
        val resId = preset.iconDrawableRes ?: return null
        return runCatching {
            val drawable = ContextCompat.getDrawable(context, resId) ?: return null
            val size = 192
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, size, size)
            drawable.draw(canvas)
            bmp
        }.getOrNull()
    }

    private fun centerCropSquare(src: Bitmap, targetPx: Int): Bitmap {
        val side = minOf(src.width, src.height).coerceAtLeast(1)
        val x = ((src.width - side) / 2).coerceAtLeast(0)
        val y = ((src.height - side) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(src, x, y, side, side)
        return if (side > targetPx) {
            val scaled = Bitmap.createScaledBitmap(cropped, targetPx, targetPx, true)
            if (scaled !== cropped) cropped.recycle()
            scaled
        } else {
            cropped
        }
    }

    private fun isConfinedToBrandingDir(context: Context, file: File): Boolean {
        return runCatching {
            val baseCanonical = File(context.filesDir, BRANDING_DIR).canonicalPath + File.separator
            file.canonicalPath.startsWith(baseCanonical)
        }.getOrDefault(false)
    }
}
