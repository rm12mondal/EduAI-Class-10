package com.example.data.media

import android.content.Context
import android.os.Environment
import com.example.data.local.ChatMessageEntity
import com.example.data.local.StudyRepository
import com.example.data.pdf.EduPdfManager
import com.example.model.ChatSection
import com.example.model.MediaVaultItem
import com.example.model.VaultMediaType
import com.example.model.WbbseSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class VaultScanSnapshot(
    val items: List<MediaVaultItem>,
    val totalStorageBytes: Long,
    val unusedFilesCount: Int,
    val unusedStorageBytes: Long
)

data class VaultCleanupResult(
    val deletedCount: Int,
    val freedBytes: Long
)

/**
 * Unified Subject-Wise Media & PDF Vault scanner and storage cleaner.
 * Automatically collects ALL uploaded photos, ALL AI-generated diagrams/mind maps,
 * and ALL saved/generated PDFs across every chat section under their WBBSE subject,
 * and confines every delete operation strictly inside app-private storage directories.
 */
object MediaVaultManager {

    suspend fun scanAllVaultItems(
        context: Context,
        repository: StudyRepository
    ): VaultScanSnapshot = withContext(Dispatchers.IO) {
        val allMessages = repository.getAllChatMessagesSnapshot()
        val seenCanonicalPaths = LinkedHashSet<String>()
        val collected = mutableListOf<MediaVaultItem>()

        // 1. Collect all referenced photos & AI diagrams from EVERY chat section
        allMessages.forEach { msg ->
            val sectionName = ChatSection.fromId(msg.sectionId).titleBn
            val inferredSubject = WbbseSubject.fromId(msg.subjectId)
                ?: inferSubjectFromText("${msg.topicLabel} ${msg.sourceLabel} ${msg.content.take(180)}")

            msg.imagePath?.takeIf { it.isNotBlank() }?.let { rawPath ->
                val file = File(rawPath)
                if (file.exists() && file.isFile) {
                    val canon = runCatching { file.canonicalPath }.getOrDefault(file.absolutePath)
                    if (seenCanonicalPaths.add(canon)) {
                        val isDiagram = canon.contains("ai_diagrams") ||
                            file.name.startsWith("diagram_", ignoreCase = true) ||
                            msg.role != "user"
                        val title = buildMediaTitle(msg, isDiagram, file.nameWithoutExtension)
                        collected.add(
                            MediaVaultItem(
                                id = canon,
                                filePath = file.absolutePath,
                                fileName = file.name,
                                displayTitle = title,
                                mediaType = if (isDiagram) VaultMediaType.DIAGRAM else VaultMediaType.PHOTO,
                                subject = inferredSubject,
                                sourceLabel = sectionName,
                                sizeBytes = file.length(),
                                lastModifiedMs = maxOf(file.lastModified(), msg.timestamp),
                                isUnusedOrOrphan = false
                            )
                        )
                    }
                }
            }

            msg.diagramPath?.takeIf { it.isNotBlank() }?.let { rawPath ->
                val file = File(rawPath)
                if (file.exists() && file.isFile) {
                    val canon = runCatching { file.canonicalPath }.getOrDefault(file.absolutePath)
                    if (seenCanonicalPaths.add(canon)) {
                        val title = buildMediaTitle(msg, isDiagram = true, fallback = file.nameWithoutExtension)
                        collected.add(
                            MediaVaultItem(
                                id = canon,
                                filePath = file.absolutePath,
                                fileName = file.name,
                                displayTitle = title,
                                mediaType = VaultMediaType.DIAGRAM,
                                subject = inferredSubject,
                                sourceLabel = sectionName,
                                sizeBytes = file.length(),
                                lastModifiedMs = maxOf(file.lastModified(), msg.timestamp),
                                isUnusedOrOrphan = false
                            )
                        )
                    }
                }
            }
        }

        // 2. Collect ALL saved/generated PDFs from the offline PDF library
        val savedPdfs = EduPdfManager.listSavedPdfs(context)
        savedPdfs.forEach { pdf ->
            val file = File(pdf.absolutePath)
            if (file.exists() && file.isFile) {
                val canon = runCatching { file.canonicalPath }.getOrDefault(file.absolutePath)
                if (seenCanonicalPaths.add(canon)) {
                    val subject = inferSubjectFromText("${pdf.displayName} ${pdf.fileName}")
                    val sourceTag = when {
                        pdf.displayName.contains("মাইন্ডম্যাপ", ignoreCase = true) ||
                            pdf.displayName.contains("mindmap", ignoreCase = true) -> "মাইন্ড-ম্যাপ PDF"
                        pdf.displayName.contains("ব্রহ্মাস্ত্রম", ignoreCase = true) ||
                            pdf.displayName.contains("brahmastram", ignoreCase = true) -> "ব্রহ্মাস্ত্রম PDF"
                        pdf.displayName.contains("পকেট", ignoreCase = true) -> "পকেট বুক PDF"
                        else -> "সেভড PDF লাইব্রেরি"
                    }
                    collected.add(
                        MediaVaultItem(
                            id = canon,
                            filePath = file.absolutePath,
                            fileName = pdf.fileName,
                            displayTitle = pdf.displayName,
                            mediaType = VaultMediaType.PDF,
                            subject = subject,
                            sourceLabel = sourceTag,
                            sizeBytes = pdf.sizeBytes,
                            lastModifiedMs = pdf.lastModifiedMs,
                            isUnusedOrOrphan = false
                        )
                    )
                }
            }
        }

        // 3. Collect any unreferenced / orphan image or diagram files left after clearing chats
        val orphanDirs = listOf(
            File(context.filesDir, "chat_uploads") to VaultMediaType.PHOTO,
            File(context.filesDir, "ai_diagrams") to VaultMediaType.DIAGRAM,
            File(context.cacheDir, "notebook_photos") to VaultMediaType.PHOTO
        )
        orphanDirs.forEach { (dir, type) ->
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles { f -> f.isFile && f.length() > 0L }?.forEach { file ->
                    val canon = runCatching { file.canonicalPath }.getOrDefault(file.absolutePath)
                    if (seenCanonicalPaths.add(canon)) {
                        collected.add(
                            MediaVaultItem(
                                id = canon,
                                filePath = file.absolutePath,
                                fileName = file.name,
                                displayTitle = if (type == VaultMediaType.DIAGRAM) {
                                    "সংরক্ষিত ডায়াগ্রাম (${file.nameWithoutExtension.takeLast(8)})"
                                } else {
                                    "অব্যবহৃত/ক্যাশ ছবি (${file.nameWithoutExtension.takeLast(8)})"
                                },
                                mediaType = type,
                                subject = null,
                                sourceLabel = "অব্যবহৃত ফাইল (Unused)",
                                sizeBytes = file.length(),
                                lastModifiedMs = file.lastModified(),
                                isUnusedOrOrphan = true
                            )
                        )
                    }
                }
            }
        }

        val sorted = collected.sortedByDescending { it.lastModifiedMs }
        val totalBytes = sorted.sumOf { it.sizeBytes }
        val unusedItems = sorted.filter { it.isUnusedOrOrphan }

        VaultScanSnapshot(
            items = sorted,
            totalStorageBytes = totalBytes,
            unusedFilesCount = unusedItems.size,
            unusedStorageBytes = unusedItems.sumOf { it.sizeBytes }
        )
    }

    suspend fun deleteVaultItem(
        context: Context,
        item: MediaVaultItem,
        repository: StudyRepository
    ): Boolean = withContext(Dispatchers.IO) {
        val file = File(item.filePath)
        if (!isPathConfinedToAppStorage(context, file)) return@withContext false
        val deleted = if (file.exists()) file.delete() else true
        if (deleted) {
            repository.clearMediaPathReferences(item.filePath)
        }
        deleted
    }

    suspend fun clearUnusedFiles(
        context: Context,
        items: List<MediaVaultItem>,
        repository: StudyRepository
    ): VaultCleanupResult = withContext(Dispatchers.IO) {
        var count = 0
        var freed = 0L
        items.filter { it.isUnusedOrOrphan }.forEach { item ->
            val file = File(item.filePath)
            if (isPathConfinedToAppStorage(context, file) && (!file.exists() || file.delete())) {
                repository.clearMediaPathReferences(item.filePath)
                count++
                freed += item.sizeBytes
            }
        }
        // Also sweep any leftover temp camera cache files
        val camCache = File(context.cacheDir, "notebook_photos")
        if (camCache.exists() && camCache.isDirectory) {
            camCache.listFiles { f -> f.isFile }?.forEach { f ->
                val len = f.length()
                if (isPathConfinedToAppStorage(context, f) && f.delete()) {
                    count++
                    freed += len
                }
            }
        }
        VaultCleanupResult(deletedCount = count, freedBytes = freed)
    }

    suspend fun clearItemsBatch(
        context: Context,
        targetItems: List<MediaVaultItem>,
        repository: StudyRepository
    ): VaultCleanupResult = withContext(Dispatchers.IO) {
        var count = 0
        var freed = 0L
        targetItems.forEach { item ->
            val file = File(item.filePath)
            if (isPathConfinedToAppStorage(context, file) && (!file.exists() || file.delete())) {
                repository.clearMediaPathReferences(item.filePath)
                count++
                freed += item.sizeBytes
            }
        }
        VaultCleanupResult(deletedCount = count, freedBytes = freed)
    }

    private fun isPathConfinedToAppStorage(context: Context, target: File): Boolean {
        val targetCanon = runCatching { target.canonicalPath }.getOrNull() ?: return false
        val allowedRoots = listOfNotNull(
            context.filesDir,
            context.cacheDir,
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        ).mapNotNull { root ->
            runCatching { root.canonicalPath + File.separator }.getOrNull()
        }
        return allowedRoots.any { prefix -> targetCanon.startsWith(prefix) }
    }

    private fun buildMediaTitle(
        msg: ChatMessageEntity,
        isDiagram: Boolean,
        fallback: String
    ): String {
        val rawTopic = msg.topicLabel.ifBlank {
            msg.content.lines().firstOrNull { it.isNotBlank() }.orEmpty()
        }
            .replace(Regex("[#*📘🔹🎯📖🎨🧠🚀💡🔥]"), "")
            .trim()
        return if (rawTopic.length >= 4) {
            rawTopic.take(46)
        } else if (isDiagram) {
            "AI ডায়াগ্রাম ($fallback)"
        } else {
            "আপলোড করা ছবি ($fallback)"
        }
    }

    fun inferSubjectFromText(text: String): WbbseSubject? {
        val t = text.lowercase(Locale.ROOT)
        return when {
            t.contains("গণিত") || t.contains("math") || t.contains("ত্রিকোণমিতি") ||
                t.contains("পরিমিতি") || t.contains("জ্যামিতি") || t.contains("শ্রীধর") ||
                t.contains("সুদ") || t.contains("গোলক") || t.contains("শঙ্কু") -> WbbseSubject.MATHEMATICS

            t.contains("ভৌত") || t.contains("physical") || t.contains("physics") ||
                t.contains("chemistry") || t.contains("রসায়ন") || t.contains("বয়েল") ||
                t.contains("চার্লস") || t.contains("ওহম") || t.contains("আলো") ||
                t.contains("পর্যায়") -> WbbseSubject.PHYSICAL_SCIENCE

            t.contains("জীবন") || t.contains("life") || t.contains("biology") ||
                t.contains("মাইটোসিস") || t.contains("হরমোন") || t.contains("অক্সিন") ||
                t.contains("মেন্ডেল") || t.contains("স্নায়ু") || t.contains("নিউরন") -> WbbseSubject.LIFE_SCIENCE

            t.contains("ইতিহাস") || t.contains("history") || t.contains("বিদ্রোহ") ||
                t.contains("১৮৫৭") || t.contains("মহাবিদ্রোহ") || t.contains("নবজাগরণ") ||
                t.contains("গান্ধী") -> WbbseSubject.HISTORY

            t.contains("ভূগোল") || t.contains("geography") || t.contains("নদী") ||
                t.contains("হিমবাহ") || t.contains("বায়ুমণ্ডল") || t.contains("বার্খান") ||
                t.contains("মৃত্তিকা") || t.contains("ম্যাপ") -> WbbseSubject.GEOGRAPHY

            t.contains("বাংলা") || t.contains("bengali") || t.contains("ব্যাকরণ") ||
                t.contains("কারক") || t.contains("সমাস") || t.contains("বাচ্য") ||
                t.contains("জ্ঞানচক্ষু") || t.contains("আফ্রিকা") -> WbbseSubject.BENGALI

            t.contains("english") || t.contains("grammar") || t.contains("unseen") ||
                t.contains("writing") || t.contains("narration") || t.contains("voice") ||
                t.contains("phrasal") || t.contains("letter") -> WbbseSubject.ENGLISH

            else -> null
        }
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_048_576L -> String.format(Locale.US, "%.2f MB", bytes / 1_048_576.0)
            bytes >= 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
