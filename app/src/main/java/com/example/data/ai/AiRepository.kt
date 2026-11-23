package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.gguf.GgufFileInspector
import com.example.data.gguf.GgufModelManager
import com.example.data.gguf.LlamaNativeBridge
import com.example.data.local.MasterDataEntity
import com.example.data.local.StudyRepository
import com.example.model.GgufModelStatus
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.SubjectiveEvaluationResult
import com.example.model.WbbseSubject
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import kotlin.math.min
import kotlin.math.roundToInt

private const val TAG = "HybridEduAiRepository"
private const val ONLINE_MODEL_NAME = "gemini-3.8-flash"
private const val ONLINE_STREAM_TIMEOUT_MS = 60_000L

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
 * Unified AI Repository routing:
 * - Offline chat (Local WBBSE Master Data + Llama-3.2 GGUF pipeline)
 * - Online streaming text & multimodal handwritten notebook evaluation via Firebase AI Logic
 * - Exam SAQ & Long answer evaluation (Online structured Gemini JSON + Offline rubric fallback)
 */
interface AiRepository {
    fun generateAnswerStream(
        query: String,
        selectedSubject: WbbseSubject?,
        mode: InferenceMode,
        config: LlamaInferenceConfig,
        imageBitmap: Bitmap? = null
    ): Flow<AiStreamEvent>

    suspend fun evaluateSubjectiveAnswer(
        subject: WbbseSubject,
        question: String,
        maxMarks: Int,
        modelAnswer: String,
        keyTerms: List<String>,
        studentTypedAnswer: String,
        handwrittenImage: Bitmap?,
        mode: InferenceMode
    ): SubjectiveEvaluationResult
}

class HybridEduAiRepository(
    private val studyRepository: StudyRepository,
    private val ggufModelManager: GgufModelManager
) : AiRepository {

    // Lazy initialization prevents evaluating Firebase.ai eagerly at class construction time
    private val onlineGenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = ONLINE_MODEL_NAME,
            systemInstruction = content {
                text(
                    "You are EduAI Class 10, an expert West Bengal Board of Secondary Education " +
                        "(WBBSE Madhyamik) Class 10 tutor and senior Madhyamik examiner covering all 7 compulsory subjects: " +
                        "Bengali (বাংলা), English, Mathematics (গণিত), Physical Science (ভৌত বিজ্ঞান), " +
                        "Life Science (জীবন বিজ্ঞান), History (ইতিহাস), and Geography (ভূগোল). " +
                        "Respond clearly and concisely in the same language the student uses (Bengali or English). " +
                        "Include key formulas, important historical dates/facts, and concise Madhyamik exam tips where helpful."
                )
            }
        )
    }

    private val examGraderSchema = Schema.obj(
        properties = mapOf(
            "scoreObtained" to Schema.double(description = "Marks awarded between 0 and maxMarks, in 0.5 increments"),
            "verdict" to Schema.string(description = "Short 1-line summary verdict in Bengali or English matching the question"),
            "strengths" to Schema.string(description = "What the student wrote correctly, including handwriting/structure feedback if an image was provided"),
            "mistakesAndGaps" to Schema.string(description = "Specific factual, formula, spelling, or presentation mistakes and missing points"),
            "improvementTips" to Schema.string(description = "Actionable steps to score full marks in WBBSE Madhyamik examination")
        )
    )

    private val examGraderModel by lazy {
        Firebase.ai.generativeModel(
            modelName = ONLINE_MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = examGraderSchema
                temperature = 0.2f
            }
        )
    }

    override fun generateAnswerStream(
        query: String,
        selectedSubject: WbbseSubject?,
        mode: InferenceMode,
        config: LlamaInferenceConfig,
        imageBitmap: Bitmap?
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

        when (mode) {
            InferenceMode.OFFLINE -> {
                val (fullText, sourceLabel) = if (imageBitmap != null) {
                    buildOfflineImagePromptNotice(selectedSubject)
                } else {
                    buildOfflineAnswer(
                        query = query,
                        subject = resolvedSubject,
                        topMatches = topMatches,
                        config = config
                    )
                }

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
                    delay(20L)
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

            InferenceMode.ONLINE -> {
                val sourceLabel = if (imageBitmap != null) {
                    "অনলাইন খাতা মূল্যায়ন • Gemini ($ONLINE_MODEL_NAME)"
                } else {
                    "অনলাইন Gemini ($ONLINE_MODEL_NAME)"
                }
                val accumulatedText = StringBuilder()

                val inputContent = if (imageBitmap != null) {
                    val evalPrompt = buildNotebookEvaluationPrompt(query, resolvedSubject)
                    content {
                        image(imageBitmap)
                        text(evalPrompt)
                    }
                } else {
                    val textPrompt = buildOnlinePrompt(query, resolvedSubject, topMatches)
                    content {
                        text(textPrompt)
                    }
                }

                try {
                    withTimeout(ONLINE_STREAM_TIMEOUT_MS) {
                        onlineGenerativeModel.generateContentStream(inputContent).collect { chunk ->
                            val chunkText = chunk.text.orEmpty()
                            if (chunkText.isNotEmpty()) {
                                accumulatedText.append(chunkText)
                                emit(
                                    AiStreamEvent.Partial(
                                        accumulatedText = accumulatedText.toString(),
                                        sourceLabel = sourceLabel
                                    )
                                )
                            }
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    Log.e(TAG, "Firebase AI Logic streaming timed out after 60s", e)
                    throw e
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase AI Logic streaming failed", e)
                    throw e
                }

                val finalText = accumulatedText.toString().trim()
                if (finalText.isBlank()) {
                    throw IllegalStateException("Gemini returned an empty response. Please try again.")
                }

                val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
                emit(
                    AiStreamEvent.Completed(
                        fullText = finalText,
                        sourceLabel = sourceLabel,
                        resolvedSubject = resolvedSubject,
                        latencyMs = elapsed
                    )
                )
            }
        }
    }

    override suspend fun evaluateSubjectiveAnswer(
        subject: WbbseSubject,
        question: String,
        maxMarks: Int,
        modelAnswer: String,
        keyTerms: List<String>,
        studentTypedAnswer: String,
        handwrittenImage: Bitmap?,
        mode: InferenceMode
    ): SubjectiveEvaluationResult {
        if (mode == InferenceMode.ONLINE) {
            return evaluateOnlineWithGemini(
                subject = subject,
                question = question,
                maxMarks = maxMarks,
                modelAnswer = modelAnswer,
                studentTypedAnswer = studentTypedAnswer,
                handwrittenImage = handwrittenImage
            )
        }

        // Offline Mode
        if (handwrittenImage != null && studentTypedAnswer.isBlank()) {
            throw IllegalStateException(
                "হাতের লেখা খাতার ছবি মূল্যায়নের জন্য উপরের টগল দিয়ে অনলাইন মোড (Online Mode) চালু করো অথবা উত্তরটি টাইপ করে জমা দাও।"
            )
        }

        return evaluateOfflineByRubric(
            maxMarks = maxMarks,
            modelAnswer = modelAnswer,
            keyTerms = keyTerms,
            studentTypedAnswer = studentTypedAnswer
        )
    }

    private suspend fun evaluateOnlineWithGemini(
        subject: WbbseSubject,
        question: String,
        maxMarks: Int,
        modelAnswer: String,
        studentTypedAnswer: String,
        handwrittenImage: Bitmap?
    ): SubjectiveEvaluationResult {
        val gradingPrompt = buildString {
            append("You are an official West Bengal Board of Secondary Education (WBBSE) Class 10 Madhyamik Examiner.\n")
            append("Subject: ${subject.bengaliName} (${subject.englishName})\n")
            append("Question (Max Marks: $maxMarks): $question\n")
            append("Reference Model Answer / Marking Outline: $modelAnswer\n\n")
            if (studentTypedAnswer.isNotBlank()) {
                append("Student's Typed Answer:\n$studentTypedAnswer\n\n")
            }
            if (handwrittenImage != null) {
                append("The student has also attached a photo of their handwritten answer sheet. ")
                append("Carefully read the handwriting, judge legibility, diagram/structure, factual accuracy, and formulas.\n\n")
            }
            append("Grade strictly but constructively out of $maxMarks marks for WBBSE Class 10 standard. ")
            append("Write feedback in the same language as the question (Bengali for Bengali-script questions, English for English questions).")
        }

        val inputContent = content {
            if (handwrittenImage != null) {
                image(handwrittenImage)
            }
            text(gradingPrompt)
        }

        val response = try {
            withTimeout(ONLINE_STREAM_TIMEOUT_MS) {
                examGraderModel.generateContent(inputContent)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Online exam evaluation timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Online exam evaluation failed", e)
            throw e
        }

        val jsonText = response.text
            ?: throw IllegalStateException("AI মূল্যায়নকারী কোনো উত্তর দেয়নি। আবার চেষ্টা করো।")

        val json = JSONObject(jsonText)
        val rawScore = json.optDouble("scoreObtained", 0.0).toFloat()
        val clampedScore = ((rawScore * 2f).roundToInt() / 2f).coerceIn(0f, maxMarks.toFloat())

        return SubjectiveEvaluationResult(
            scoreObtained = clampedScore,
            maxMarks = maxMarks.toFloat(),
            verdict = json.optString("verdict", "মূল্যায়ন সম্পন্ন হয়েছে।"),
            strengths = json.optString("strengths", "প্রাসঙ্গিক প্রচেষ্টা করা হয়েছে।"),
            mistakesAndGaps = json.optString("mistakesAndGaps", "কোনো বড় ত্রুটি পাওয়া যায়নি।"),
            improvementTips = json.optString("improvementTips", "পয়েন্ট করে ও সঠিক সূত্র/চিত্রসহ লিখলে পূর্ণ নম্বর পাবে।"),
            modelAnswer = modelAnswer,
            evaluatorSource = "Firebase AI Logic ($ONLINE_MODEL_NAME)"
        )
    }

    private fun evaluateOfflineByRubric(
        maxMarks: Int,
        modelAnswer: String,
        keyTerms: List<String>,
        studentTypedAnswer: String
    ): SubjectiveEvaluationResult {
        val normalized = studentTypedAnswer.lowercase()
        val matchedTerms = keyTerms.filter { normalized.contains(it.lowercase()) }
        val missingTerms = keyTerms.filterNot { normalized.contains(it.lowercase()) }
        val ratio = if (keyTerms.isEmpty()) 0.5f else matchedTerms.size.toFloat() / keyTerms.size.toFloat()
        val lengthBonus = if (studentTypedAnswer.trim().length >= 35) 0.12f else 0f
        val rawMarks = ((ratio + lengthBonus).coerceIn(0f, 1f) * maxMarks)
        val roundedMarks = ((rawMarks * 2f).roundToInt() / 2f).coerceIn(0f, maxMarks.toFloat())

        val verdict = when {
            roundedMarks >= maxMarks * 0.8f -> "দারুণ উত্তর! মাধ্যমিক মানের কাছাকাছি।"
            roundedMarks >= maxMarks * 0.5f -> "ভালো চেষ্টা, তবে আরও কিছু মূল পয়েন্ট যোগ করতে হবে।"
            else -> "উত্তরটি অসম্পূর্ণ—নীচের আদর্শ উত্তরটি ভালো করে পড়ে আবার অনুশীলন করো।"
        }

        val strengths = if (matchedTerms.isNotEmpty()) {
            "সঠিক মূল শব্দ/তথ্য পাওয়া গেছে: ${matchedTerms.joinToString(", ")}"
        } else {
            "তুমি উত্তর লেখার চেষ্টা করেছ, তবে পাঠ্যবইয়ের মূল পরিভাষাগুলো যুক্ত করতে হবে।"
        }

        val gaps = if (missingTerms.isNotEmpty()) {
            "অনুপস্থিত গুরুত্বপূর্ণ পয়েন্ট/শব্দ: ${missingTerms.take(5).joinToString(", ")}"
        } else {
            "প্রায় সব মূল পয়েন্টই তোমার উত্তরে এসেছে!"
        }

        return SubjectiveEvaluationResult(
            scoreObtained = roundedMarks,
            maxMarks = maxMarks.toFloat(),
            verdict = verdict,
            strengths = strengths,
            mistakesAndGaps = gaps,
            improvementTips = "বিস্তারিত AI বিশ্লেষণ ও খাতার ছবি মূল্যায়নের জন্য অনলাইন মোড চালু করতে পারো।",
            modelAnswer = modelAnswer,
            evaluatorSource = "অফলাইন WBBSE রুব্রিক মূল্যায়ন"
        )
    }

    private fun buildNotebookEvaluationPrompt(
        userPrompt: String,
        subject: WbbseSubject?
    ): String = buildString {
        val subjectHint = subject?.let { "${it.bengaliName} (${it.englishName})" }
            ?: "WBBSE Class 10 (Madhyamik)"
        append("A West Bengal Board (WBBSE) Class 10 student uploaded this handwritten notebook photo.\n")
        append("Subject: $subjectHint\n")
        append("Student instruction: ${userPrompt.ifBlank { "Evaluate my handwritten answer for WBBSE Class 10." }}\n\n")
        append("Please respond clearly in Bengali (or English if the notebook is in English) with these 5 structured sections:\n")
        append("1. 📝 খাতায় যা লেখা আছে (Brief reading/transcription summary)\n")
        append("2. ✍️ হাতের লেখা ও উপস্থাপনা (Handwriting legibility, neatness & presentation)\n")
        append("3. ✅ সঠিক তথ্য ও ভুলত্রুটি (Factual accuracy, formulas, grammar/spelling errors per WBBSE Class 10 standards)\n")
        append("4. 📊 আনুমানিক মাধ্যমিক নম্বর (Estimated score & marking breakdown)\n")
        append("5. 🎯 পূর্ণ নম্বর পাওয়ার পরামর্শ (Concrete tips & improved model points to get full marks)")
    }

    private fun buildOfflineImagePromptNotice(subject: WbbseSubject?): Pair<String, String> {
        val subjectName = subject?.bengaliName ?: "WBBSE ক্লাস ১০"
        val message = buildString {
            append("📷 হাতের লেখা খাতার ছবি মূল্যায়ন ($subjectName)\n\n")
            append("খাতার ছবি পড়ে হাতের লেখা, বানান ও মাধ্যমিক মানের নির্ভুলতা যাচাই করতে ভিশন AI প্রয়োজন, যা বর্তমানে **অনলাইন মোডে (Firebase AI Logic • Gemini)** কাজ করে।\n\n")
            append("👉 কী করবে:\n")
            append("১. চ্যাটের উপরের হাইব্রিড টগল সুইচটি **অনলাইন (Online)** মোডে চালু করো।\n")
            append("২. এরপর খাতার ছবিটি আবার যুক্ত করে পাঠাও—সঙ্গে সঙ্গে ভুল সংশোধন ও পূর্ণ নম্বর পাওয়ার টিপস পেয়ে যাবে!")
        }
        return message to "অফলাইন মোড • অনলাইন সুইচ প্রয়োজন"
    }

    private fun buildOnlinePrompt(
        query: String,
        subject: WbbseSubject?,
        topMatches: List<MasterDataEntity>
    ): String = buildString {
        val subjectHint = subject?.let { "${it.bengaliName} (${it.englishName})" }
            ?: "WBBSE Class 10 (All 7 Subjects)"
        append("Subject focus: $subjectHint\n")
        if (topMatches.isNotEmpty()) {
            append("\nRelevant WBBSE Class 10 syllabus reference:\n")
            topMatches.forEach { note ->
                append("- ${note.chapterName} (${note.topicTitle}): ${note.keyFormulaOrFact}\n")
            }
        }
        append("\nStudent question:\n")
        append(query.trim())
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

        val llamaPrompt = LlamaNativeBridge.buildLlama32Prompt(query, subject, ragContext)
        val nativeReply = if (isGgufReady && (activeMeta?.fileSizeBytes ?: 0L) >= 1_000_000L) {
            LlamaNativeBridge.generateNativeIfLoaded(llamaPrompt, config)
        } else {
            null
        }
        if (nativeReply != null) {
            return nativeReply to "অফলাইন Llama-3.2 GGUF (${activeMeta?.fileName})"
        }

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
                    append("\n\n💡 অফলাইন Llama নোট: পূর্ণ জেনারেটিভ অফলাইন উত্তরের জন্য ‘মডেল’ ট্যাবে ${GgufFileInspector.TARGET_MODEL_FILENAME} ফাইল লোড করুন।")
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
