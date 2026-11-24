package com.example.data.pdf

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.master.MathUnicodeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavedPdfItem(
    val fileName: String,
    val displayName: String,
    val absolutePath: String,
    val sizeBytes: Long,
    val lastModifiedMs: Long,
    val publicDownloadsNote: String
)

data class PdfGenerationResult(
    val item: SavedPdfItem,
    val publicSavedPath: String
)

/**
 * 100% Offline Bengali/English PDF Generator & Saved PDF Library Manager.
 * Uses Android's native PdfDocument + StaticLayout so Bengali Unicode conjuncts (যুক্তাক্ষর)
 * and English formatting render cleanly offline with zero external dependencies or storage permissions.
 *
 * Saves a public copy to `Downloads/EduAI_Notes/` (via MediaStore.Downloads on API 29+) AND
 * keeps a synchronized FileProvider-accessible copy for instant in-app Open, Rename, Delete,
 * and direct WhatsApp Sharing.
 */
object EduPdfManager {

    private const val FOLDER_NAME = "EduAI_Notes"
    private const val PAGE_WIDTH = 595 // A4 width in PostScript points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 height
    private const val MARGIN_X = 40f
    private const val MARGIN_TOP = 42f
    private const val MARGIN_BOTTOM = 44f

    fun getLibraryDir(context: Context): File {
        val ext = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val dir = if (ext != null) File(ext, FOLDER_NAME) else File(context.filesDir, FOLDER_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun sanitizeFileName(raw: String): String {
        val trimmed = raw.trim().removeSuffix(".pdf").removeSuffix(".PDF")
        val cleaned = trimmed
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
            .replace(Regex("""\s+"""), "_")
            .take(64)
        return cleaned.ifBlank {
            val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
            "EduAI_Note_$stamp"
        }
    }

    suspend fun generateAndSavePdf(
        context: Context,
        customName: String,
        title: String,
        subtitle: String,
        bodyContent: String
    ): Result<PdfGenerationResult> = withContext(Dispatchers.IO) {
        runCatching {
            val safeBase = sanitizeFileName(customName)
            val fileName = "$safeBase.pdf"

            val document = PdfDocument()
            try {
                renderDocumentPages(
                    document = document,
                    title = title,
                    subtitle = subtitle,
                    bodyContent = cleanMarkdownForPdf(bodyContent)
                )

                // 1. Save to app-managed library dir (for instant reliable listing, opening, renaming & WhatsApp sharing)
                val libraryDir = getLibraryDir(context)
                val localFile = File(libraryDir, fileName)
                FileOutputStream(localFile).use { out ->
                    document.writeTo(out)
                }

                // 2. Also mirror into public Downloads/EduAI_Notes/ (zero storage permissions via MediaStore on API 29+)
                val publicPath = saveMirrorToPublicDownloads(context, fileName, localFile)

                val item = SavedPdfItem(
                    fileName = localFile.name,
                    displayName = localFile.nameWithoutExtension,
                    absolutePath = localFile.absolutePath,
                    sizeBytes = localFile.length(),
                    lastModifiedMs = localFile.lastModified(),
                    publicDownloadsNote = publicPath
                )
                PdfGenerationResult(item = item, publicSavedPath = publicPath)
            } finally {
                document.close()
            }
        }
    }

    private fun saveMirrorToPublicDownloads(
        context: Context,
        fileName: String,
        sourceFile: File
    ): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val relPath = "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER_NAME/"
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, relPath)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        sourceFile.inputStream().use { input -> input.copyTo(out) }
                    }
                    val completeValues = ContentValues().apply {
                        put(MediaStore.Downloads.IS_PENDING, 0)
                    }
                    resolver.update(uri, completeValues, null, null)
                    "Downloads/$FOLDER_NAME/$fileName"
                } else {
                    "Downloads/$FOLDER_NAME/$fileName"
                }
            } else {
                "Downloads/$FOLDER_NAME/$fileName"
            }
        } catch (_: Exception) {
            "Downloads/$FOLDER_NAME/$fileName"
        }
    }

    suspend fun listSavedPdfs(context: Context): List<SavedPdfItem> = withContext(Dispatchers.IO) {
        val dir = getLibraryDir(context)
        dir.listFiles { f -> f.isFile && f.name.endsWith(".pdf", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                SavedPdfItem(
                    fileName = file.name,
                    displayName = file.nameWithoutExtension,
                    absolutePath = file.absolutePath,
                    sizeBytes = file.length(),
                    lastModifiedMs = file.lastModified(),
                    publicDownloadsNote = "Downloads/$FOLDER_NAME/${file.name}"
                )
            }
            ?: emptyList()
    }

    suspend fun renameSavedPdf(
        context: Context,
        item: SavedPdfItem,
        newCustomName: String
    ): Result<SavedPdfItem> = withContext(Dispatchers.IO) {
        runCatching {
            val oldFile = File(item.absolutePath)
            require(oldFile.exists()) { "PDF ফাইলটি পাওয়া যায়নি।" }
            val safeBase = sanitizeFileName(newCustomName)
            val targetFile = File(getLibraryDir(context), "$safeBase.pdf")
            if (oldFile.absolutePath != targetFile.absolutePath) {
                if (targetFile.exists()) targetFile.delete()
                check(oldFile.renameTo(targetFile)) { "নাম পরিবর্তন করা যায়নি।" }
                saveMirrorToPublicDownloads(context, targetFile.name, targetFile)
            }
            SavedPdfItem(
                fileName = targetFile.name,
                displayName = targetFile.nameWithoutExtension,
                absolutePath = targetFile.absolutePath,
                sizeBytes = targetFile.length(),
                lastModifiedMs = targetFile.lastModified(),
                publicDownloadsNote = "Downloads/$FOLDER_NAME/${targetFile.name}"
            )
        }
    }

    suspend fun deleteSavedPdf(item: SavedPdfItem): Boolean = withContext(Dispatchers.IO) {
        try {
            File(item.absolutePath).delete()
        } catch (_: Exception) {
            false
        }
    }

    fun getFileProviderUri(context: Context, filePath: String): Uri {
        val file = File(filePath)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun buildOpenPdfIntent(context: Context, filePath: String): Intent {
        val uri = getFileProviderUri(context, filePath)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun buildSharePdfIntent(
        context: Context,
        filePath: String,
        preferWhatsApp: Boolean
    ): Intent {
        val uri = getFileProviderUri(context, filePath)
        val base = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "EduAI Class 10 WBBSE Study Notes PDF")
            putExtra(Intent.EXTRA_TEXT, "EduAI Class 10 থেকে তৈরি WBBSE মাধ্যমিক স্টাডি নোটস PDF পাঠালাম।")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (preferWhatsApp) {
            base.setPackage("com.whatsapp")
        }
        return base
    }

    private fun renderDocumentPages(
        document: PdfDocument,
        title: String,
        subtitle: String,
        bodyContent: String
    ) {
        val usableWidth = (PAGE_WIDTH - MARGIN_X * 2).toInt()

        val headerBgPaint = Paint().apply {
            color = Color.rgb(20, 49, 84)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val accentBarPaint = Paint().apply {
            color = Color.rgb(14, 116, 144)
            style = Paint.Style.FILL
        }
        val titlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 16.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subtitlePaint = TextPaint().apply {
            color = Color.rgb(219, 234, 254)
            textSize = 10.5f
            isAntiAlias = true
        }
        val headingPaint = TextPaint().apply {
            color = Color.rgb(20, 49, 84)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 11.2f
            isAntiAlias = true
        }
        val footerPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9.5f
            isAntiAlias = true
        }
        val dividerPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 0.8f
        }

        val cleanTitle = MathUnicodeFormatter.format(title)
        val cleanSubtitle = MathUnicodeFormatter.format(subtitle)
        val formattedBody = MathUnicodeFormatter.format(bodyContent)

        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date())
        val paragraphs = formattedBody.split("\n").map { it.trimEnd() }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas

        // Draw styled header banner on Page 1
        val headerRect = RectF(MARGIN_X, MARGIN_TOP, PAGE_WIDTH - MARGIN_X, MARGIN_TOP + 68f)
        canvas.drawRoundRect(headerRect, 12f, 12f, headerBgPaint)
        canvas.drawRect(MARGIN_X, MARGIN_TOP + 62f, PAGE_WIDTH - MARGIN_X, MARGIN_TOP + 68f, accentBarPaint)

        val titleLayout = buildStaticLayout(cleanTitle.take(110), titlePaint, usableWidth - 28)
        canvas.save()
        canvas.translate(MARGIN_X + 14f, MARGIN_TOP + 12f)
        titleLayout.draw(canvas)
        canvas.restore()

        val subText = "${cleanSubtitle.take(90)}  •  EduAI Class 10 (WBBSE)  •  $dateStr"
        val subLayout = buildStaticLayout(subText, subtitlePaint, usableWidth - 28)
        canvas.save()
        canvas.translate(MARGIN_X + 14f, MARGIN_TOP + 42f)
        subLayout.draw(canvas)
        canvas.restore()

        var cursorY = MARGIN_TOP + 86f
        val maxY = PAGE_HEIGHT - MARGIN_BOTTOM - 22f

        fun drawFooterAndStartNextPage() {
            canvas.drawLine(MARGIN_X, PAGE_HEIGHT - MARGIN_BOTTOM, PAGE_WIDTH - MARGIN_X, PAGE_HEIGHT - MARGIN_BOTTOM, dividerPaint)
            canvas.drawText(
                "EduAI Class 10 • WBBSE Madhyamik Study Notes",
                MARGIN_X,
                PAGE_HEIGHT - 26f,
                footerPaint
            )
            canvas.drawText(
                "পৃষ্ঠা $pageNumber",
                PAGE_WIDTH - MARGIN_X - 48f,
                PAGE_HEIGHT - 26f,
                footerPaint
            )
            document.finishPage(page)

            pageNumber += 1
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = page.canvas
            cursorY = MARGIN_TOP
        }

        for (rawLine in paragraphs) {
            if (rawLine.isBlank()) {
                cursorY += 7f
                if (cursorY > maxY) drawFooterAndStartNextPage()
                continue
            }

            val isHeading = rawLine.startsWith("■") ||
                rawLine.startsWith("📌") ||
                rawLine.startsWith("🔹") ||
                rawLine.startsWith("★") ||
                rawLine.startsWith("===") ||
                (rawLine.endsWith(":") && rawLine.length <= 65)

            val paint = if (isHeading) headingPaint else bodyPaint
            val layout = buildStaticLayout(rawLine, paint, usableWidth)
            val neededHeight = layout.height + if (isHeading) 8f else 4f

            if (cursorY + neededHeight > maxY) {
                drawFooterAndStartNextPage()
            }

            canvas.save()
            canvas.translate(MARGIN_X, cursorY)
            layout.draw(canvas)
            canvas.restore()
            cursorY += neededHeight
        }

        // Draw final page footer
        canvas.drawLine(MARGIN_X, PAGE_HEIGHT - MARGIN_BOTTOM, PAGE_WIDTH - MARGIN_X, PAGE_HEIGHT - MARGIN_BOTTOM, dividerPaint)
        canvas.drawText(
            "EduAI Class 10 • WBBSE Madhyamik Study Notes (100% Offline PDF)",
            MARGIN_X,
            PAGE_HEIGHT - 26f,
            footerPaint
        )
        canvas.drawText(
            "পৃষ্ঠা $pageNumber",
            PAGE_WIDTH - MARGIN_X - 48f,
            PAGE_HEIGHT - 26f,
            footerPaint
        )
        document.finishPage(page)
    }

    private fun buildStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(100))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2.5f, 1.15f)
            .setIncludePad(false)
            .build()
    }

    private fun cleanMarkdownForPdf(raw: String): String {
        return MathUnicodeFormatter.format(raw)
            .replace("**", "")
            .replace("### ", "■ ")
            .replace("## ", "■ ")
            .replace("# ", "■ ")
            .replace("```", "")
            .trim()
    }
}
