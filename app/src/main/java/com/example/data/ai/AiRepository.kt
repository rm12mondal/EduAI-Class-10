package com.example.data.ai

import com.example.data.gguf.GgufFileInspector
import com.example.data.gguf.GgufModelManager
import com.example.data.gguf.LlamaNativeBridge
import com.example.data.local.MasterDataEntity
import com.example.data.local.StudyRepository
import com.example.model.GgufModelStatus
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.WbbseSubject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.min

sealed interface AiStreamEvent {
    data class Partial(val accumulatedText: String, val sourceLabel: String) : AiStreamEvent
    data class Completed(
        val fullText: String,
        val sourceLabel: String,
        val resolvedSubject: WbbseSubject?,
        val latencyMs: Long
    ) : AiStreamEvent
}

/**
 * Unified AI Repository contract routing both Offline (Local WBBSE Master Data + Llama-3.2 GGUF)
 * and Online (Cloud AI) requests through a single clean interface.
 */
interface AiRepository {
    fun generateAnswerStream(
        query: String,
        selectedSubject: WbbseSubject?,
        mode: InferenceMode,
        config: LlamaInferenceConfig
    ): Flow<AiStreamEvent>
}

class HybridEduAiRepository(
    private val studyRepository: StudyRepository,
    private val ggufModelManager: GgufModelManager
) : AiRepository {

    override fun generateAnswerStream(
        query: String,
        selectedSubject: WbbseSubject?,
        mode: InferenceMode,
        config: LlamaInferenceConfig
    ): Flow<AiStreamEvent> = flow {
        val startMs = System.currentTimeMillis()
        val allNotes = studyRepository.getMasterDataSnapshot(null)
        val scopedNotes = if (selectedSubject != null) {
            allNotes.filter { it.subjectId == selectedSubject.id }
        } else {
            allNotes
        }

        val rankedMatches = rankRelevantNotes(query, scopedNotes).ifEmpty {
            rankRelevantNotes(query, allNotes)
        }
        val topMatches = rankedMatches.take(2)
        val resolvedSubject = selectedSubject
            ?: topMatches.firstOrNull()?.let { WbbseSubject.fromId(it.subjectId) }

        val (fullText, sourceLabel) = when (mode) {
            InferenceMode.OFFLINE -> buildOfflineAnswer(
                query = query,
                subject = resolvedSubject,
                topMatches = topMatches,
                config = config
            )

            InferenceMode.ONLINE -> buildOnlinePlaceholderAnswer(
                query = query,
                subject = resolvedSubject,
                topMatches = topMatches
            )
        }

        // Stream response progressively for smooth student reading experience
        val words = fullText.split(" ")
        val builder = StringBuilder()
        val chunkSize = if (words.size > 80) 4 else 2
        var index = 0
        while (index < words.size) {
            val end = min(index + chunkSize, words.size)
            for (i in index until end) {
                if (builder.isNotEmpty()) builder.append(' ')
                builder.append(words[i])
            }
            emit(AiStreamEvent.Partial(builder.toString(), sourceLabel))
            index = end
            delay(22L)
        }

        val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
        emit(
            AiStreamEvent.Completed(
                fullText = fullText,
                sourceLabel = sourceLabel,
                resolvedSubject = resolvedSubject,
                latencyMs = elapsed
            )
        )
    }

    private fun buildOfflineAnswer(
        query: String,
        subject: WbbseSubject?,
        topMatches: List<MasterDataEntity>,
        config: LlamaInferenceConfig
    ): Pair<String, String> {
        val ggufState = ggufModelManager.state.value
        val activeMeta = ggufState.activeMetadata
        val isGgufReady = ggufState.status == GgufModelStatus.READY_LOADED && activeMeta != null

        val ragContext = topMatches.joinToString("\n\n") { note ->
            "অধ্যায়: ${note.chapterName}\nবিষয়বস্তু: ${note.content}\nমূল সূত্র/তথ্য: ${note.keyFormulaOrFact}"
        }

        // 1. If native llama.cpp (.so) + full GGUF weights are loaded, execute directly on-device
        val llamaPrompt = LlamaNativeBridge.buildLlama32Prompt(query, subject, ragContext)
        val nativeReply = if (isGgufReady && (activeMeta?.fileSizeBytes ?: 0L) >= 1_000_000L) {
            LlamaNativeBridge.generateNativeIfLoaded(llamaPrompt, config)
        } else {
            null
        }
        if (nativeReply != null) {
            return nativeReply to "অফলাইন Llama-3.2 GGUF (${activeMeta?.fileName})"
        }

        // 2. Local WBBSE Class 10 Master Data Engine + GGUF Readiness Attribution
        val sourceBadge = if (isGgufReady) {
            "অফলাইন WBBSE ডেটা • GGUF যাচাইকৃত (${activeMeta?.quantizationHint})"
        } else {
            "অফলাইন WBBSE মাস্টার ডেটা"
        }

        val response = if (topMatches.isNotEmpty()) {
            val primary = topMatches.first()
            val primarySubject = WbbseSubject.fromId(primary.subjectId)
            buildString {
                append("📘 ")
                append(primarySubject?.let { "${it.bengaliName} (${it.englishName})" } ?: "WBBSE ক্লাস ১০")
                append(" — ${primary.chapterName}\n\n")
                append("🔹 ${primary.topicTitle}\n")
                append(primary.content)
                append("\n\n📌 মনে রাখার মূল সূত্র / তথ্য:\n")
                append(primary.keyFormulaOrFact)
                append("\n\n🎯 মাধ্যমিক পরীক্ষার টিপস:\n")
                append(primary.examTip)

                if (topMatches.size > 1) {
                    val secondary = topMatches[1]
                    append("\n\n📎 সম্পর্কিত অধ্যায়: ${secondary.chapterName} — ${secondary.topicTitle}\n")
                    append("• ${secondary.keyFormulaOrFact}")
                }

                if (!isGgufReady) {
                    append("\n\n💡 অফলাইন Llama নোট: পূর্ণ জেনারেটিভ অফলাইন উত্তরের জন্য ‘মডেল ও সেটিংস’ ট্যাবে ${GgufFileInspector.TARGET_MODEL_FILENAME} ফাইল লোড করুন।")
                }
            }
        } else {
            val subjectLine = subject?.let { "${it.bengaliName} (${it.englishName})" }
                ?: "পশ্চিমবঙ্গ বোর্ড ক্লাস ১০ (৭টি বিষয়)"
            buildString {
                append("📚 $subjectLine — অফলাইন মাস্টার ডেটা অনুসন্ধান\n\n")
                append("তোমার প্রশ্ন “$query”-এর সঙ্গে সরাসরি মিল আছে এমন নির্দিষ্ট অধ্যায় পাওয়া যায়নি। তবে তুমি এই বিষয়গুলোর যেকোনোটি জিজ্ঞেস করতে পারো:\n")
                append("• বাংলা: জ্ঞানচক্ষু, অসুখী একজন, আফ্রিকা, কারক-সমাস\n")
                append("• ইংরেজি: Father's Help, Fable, Passing Away of Bapu, Voice/Narration\n")
                append("• গণিত: শ্রীধর আচার্যের সূত্র, সরল/চক্রবৃদ্ধি সুদ, পিথাগোরাস, ত্রিকোণমিতি\n")
                append("• ভৌত বিজ্ঞান: বয়েল-চার্লসের সূত্র, স্নেলের সূত্র, ওহমের সূত্র, পর্যায় সারণি\n")
                append("• জীবন বিজ্ঞান: অক্সিন-জিব্বেরেলিন, মাইটোসিস-মিয়োসিস, মেন্ডেলের সূত্র, নাইট্রোজেন চক্র\n")
                append("• ইতিহাস: রামমোহন-বিদ্যাসাগর, সাঁওতাল ও নীল বিদ্রোহ, ১৮৫৭ মহাবিদ্রোহ\n")
                append("• ভূগোল: নদী-হিমবাহ-বায়ু, বায়ুমণ্ডল, জোয়ার-ভাটা, ভারতের মৃত্তিকা ও শিল্প\n\n")
                append("অথবা ‘৭টি বিষয়’ ট্যাবে গিয়ে নিজের স্টাডি নোট যোগ করে অফলাইন নলেজ বেস সমৃদ্ধ করো!")
            }
        }

        return response to sourceBadge
    }

    /**
     * Clearly labeled placeholder Online AI response used when Firebase AI Logic is not enabled.
     * Kept behind the unified [AiRepository] interface so live Gemini AI can be connected cleanly
     * whenever the user enables Firebase.
     */
    private fun buildOnlinePlaceholderAnswer(
        query: String,
        subject: WbbseSubject?,
        topMatches: List<MasterDataEntity>
    ): Pair<String, String> {
        val sourceBadge = "অনলাইন স্যাম্পল প্রিভিউ (Firebase প্রয়োজন)"
        val subjectTitle = subject?.let { "${it.bengaliName} (${it.englishName})" }
            ?: "WBBSE Class 10"

        val response = buildString {
            append("🌐 [Sample Online Response — Firebase AI Logic প্রয়োজন]\n\n")
            append("বিষয়: $subjectTitle\n")
            append("প্রশ্ন: “$query”\n\n")
            if (topMatches.isNotEmpty()) {
                val best = topMatches.first()
                append("সিলেবাস ভিত্তিক নমুনা সারসংক্ষেপ (${best.chapterName}):\n")
                append(best.content)
                append("\n\nমূল সূত্র/তথ্য: ${best.keyFormulaOrFact}\n")
                append("পরীক্ষার প্রস্তুতি টিপস: ${best.examTip}\n\n")
            } else {
                append("এটি অনলাইন মোডের একটি নমুনা (Sample) উত্তর। লাইভ অনলাইন সার্চ ও ওপেন-এন্ডেড ব্যাখ্যার জন্য ক্লাউড Gemini AI সংযোগ প্রয়োজন।\n\n")
            }
            append("ℹ️ তথ্য: এই অ্যান্ড্রয়েড অ্যাপে লাইভ অনলাইন Gemini AI চালু করতে Firebase AI Logic সক্রিয় করতে হবে। সম্পূর্ণ অফলাইন উত্তরের জন্য উপরের টগল সুইচ দিয়ে ‘অফলাইন’ মোডে থাকুন।")
        }

        return response to sourceBadge
    }

    private fun rankRelevantNotes(
        query: String,
        candidates: List<MasterDataEntity>
    ): List<MasterDataEntity> {
        val normalizedQuery = query.lowercase().trim()
        val tokens = normalizedQuery
            .split(Regex("[\\s,?.!।;:'\"()\\-/]+"))
            .filter { it.length >= 2 }
            .distinct()

        if (tokens.isEmpty()) return emptyList()

        return candidates
            .map { note ->
                val keywordsLower = note.keywords.lowercase()
                val titleLower = note.topicTitle.lowercase()
                val chapterLower = note.chapterName.lowercase()
                val contentLower = note.content.lowercase()
                val factLower = note.keyFormulaOrFact.lowercase()

                var score = 0
                if (keywordsLower.contains(normalizedQuery) || titleLower.contains(normalizedQuery)) {
                    score += 18
                }
                for (token in tokens) {
                    if (keywordsLower.contains(token)) score += 8
                    if (titleLower.contains(token)) score += 6
                    if (chapterLower.contains(token)) score += 5
                    if (factLower.contains(token)) score += 4
                    if (contentLower.contains(token)) score += 2
                }
                note to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }
}
