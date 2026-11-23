package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.exam.WbbseExamBank
import com.example.data.gguf.GgufFileInspector
import com.example.data.gguf.GgufModelManager
import com.example.data.gguf.LlamaNativeBridge
import com.example.data.local.MasterDataEntity
import com.example.data.local.StudyRepository
import com.example.model.ActiveExamSession
import com.example.model.ChatPromptOptions
import com.example.model.ExamChatIntent
import com.example.model.GgufModelStatus
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.SubjectiveEvaluationResult
import com.example.model.WbbseSubject
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ResponseModality
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
private const val VISUAL_DIAGRAM_MODEL_NAME = "gemini-nano-banana-2.1"
private const val ONLINE_TIMEOUT_MS = 60_000L

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
 * - Main Chat (Offline WBBSE + Llama GGUF / Online streaming Gemini 3.8 Flash)
 * - Smart Exam-Ready To-The-Point Answer Mode (Zero fluff, exact WBBSE mark limits)
 * - AI Visual Explanations (Labeled 2D Diagrams/Flowcharts/Graphs via Gemini Nano Banana 2.1)
 * - 3-in-1 Exam Chat Box (Direct Study/Condense, Normal Untimed Test, Madhyamik Exam Hall, Camera Grading, Translation)
 * - Standalone Exam Bank SAQ/Long subjective evaluation
 */
interface AiRepository {
    fun generateAnswerStream(
        query: String,
        selectedSubject: WbbseSubject?,
        mode: InferenceMode,
        config: LlamaInferenceConfig,
        imageBitmap: Bitmap? = null,
        options: ChatPromptOptions = ChatPromptOptions()
    ): Flow<AiStreamEvent>

    suspend fun generateVisualDiagram(
        conceptQuery: String,
        subject: WbbseSubject?,
        mode: InferenceMode
    ): Bitmap

    fun generateExamAssistantStream(
        query: String,
        analysis: RoutedPromptAnalysis,
        activeSession: ActiveExamSession?,
        mode: InferenceMode,
        config: LlamaInferenceConfig,
        handwrittenImage: Bitmap? = null
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
                        "(WBBSE Madhyamik) Class 10 tutor and senior Madhyamik head examiner covering all 7 compulsory subjects: " +
                        "Bengali (বাংলা), English, Mathematics (গণিত), Physical Science (ভৌত বিজ্ঞান), " +
                        "Life Science (জীবন বিজ্ঞান), History (ইতিহাস), and Geography (ভূগোল). " +
                        "Respond clearly in the student's language (Bengali or English). " +
                        "Include exact formulas, dates, scientific terms, and WBBSE board marking tips."
                )
            }
        )
    }

    // Minimal, safe zero-configuration baseline for Firebase AI Logic runtime image generation
    private val visualDiagramModel by lazy {
        Firebase.ai.generativeModel(
            modelName = VISUAL_DIAGRAM_MODEL_NAME,
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.IMAGE)
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
        imageBitmap: Bitmap?,
        options: ChatPromptOptions
    ): Flow<AiStreamEvent> = flow {
        val startMs = System.currentTimeMillis()
        val analysis = ExamIntentRouter.analyzeForMainChat(query, selectedSubject, options)
        val allNotes = studyRepository.getMasterDataSnapshot(null)
        val targetSubject = selectedSubject ?: analysis.detectedSubject
        val scopedNotes = if (targetSubject != null) {
            allNotes.filter { it.subjectId == targetSubject.id }
        } else {
            allNotes
        }

        val rankedMatches = rankRelevantNotes(query, scopedNotes).ifEmpty {
            rankRelevantNotes(query, allNotes)
        }
        val topMatches = rankedMatches.take(2)
        val resolvedSubject = targetSubject
            ?: topMatches.firstOrNull()?.let { WbbseSubject.fromId(it.subjectId) }

        when (mode) {
            InferenceMode.OFFLINE -> {
                val (fullText, sourceLabel) = if (imageBitmap != null) {
                    buildOfflineImagePromptNotice(resolvedSubject)
                } else {
                    buildOfflineAnswer(
                        query = query,
                        subject = resolvedSubject,
                        topMatches = topMatches,
                        config = config,
                        smartToThePoint = analysis.effectiveSmartAnswer,
                        visualRequested = analysis.effectiveVisualDiagram,
                        targetMarks = analysis.detectedMarks
                    )
                }

                emitWordStream(fullText, sourceLabel, resolvedSubject, startMs)
            }

            InferenceMode.ONLINE -> {
                val sourceLabel = when {
                    imageBitmap != null -> "অনলাইন খাতা মূল্যায়ন • Gemini ($ONLINE_MODEL_NAME)"
                    analysis.effectiveTranslate -> "অনলাইন অনুবাদ (বাংলা ↔ English) • Gemini"
                    analysis.effectiveSmartAnswer -> "🎯 টু-দ্য-পয়েন্ট বোর্ড উত্তর • Gemini"
                    analysis.effectiveVisualDiagram -> "🎨 AI ভিজ্যুয়াল ব্যাখ্যা • Gemini"
                    else -> "অনলাইন Gemini ($ONLINE_MODEL_NAME)"
                }

                val inputContent = if (imageBitmap != null) {
                    val evalPrompt = buildNotebookEvaluationPrompt(
                        userPrompt = query,
                        subject = resolvedSubject,
                        smartCondense = analysis.effectiveSmartAnswer,
                        targetMarks = analysis.detectedMarks
                    )
                    content {
                        image(imageBitmap)
                        text(evalPrompt)
                    }
                } else {
                    val textPrompt = buildOnlinePrompt(
                        query = query,
                        subject = resolvedSubject,
                        topMatches = topMatches,
                        analysis = analysis
                    )
                    content {
                        text(textPrompt)
                    }
                }

                streamFromOnlineModel(inputContent, sourceLabel, resolvedSubject, startMs)
            }
        }
    }

    override suspend fun generateVisualDiagram(
        conceptQuery: String,
        subject: WbbseSubject?,
        mode: InferenceMode
    ): Bitmap {
        if (mode == InferenceMode.OFFLINE) {
            throw IllegalStateException(
                "AI ভিজ্যুয়াল ডায়াগ্রাম (ছবি দিয়ে বোঝানো) তৈরি করতে উপরের হাইব্রিড সুইচ দিয়ে অনলাইন মোড (Online Mode) চালু করো।"
            )
        }

        val subjectContext = subject?.let { "${it.englishName} (${it.bengaliName})" }
            ?: "WBBSE Class 10 Science, Mathematics, History or Geography"

        val diagramPrompt = buildString {
            append("Create a crystal-clear, textbook-style 2D educational diagram, labeled scientific illustration, ")
            append("flowchart, math graph, or clean historical/geographical map for a West Bengal Board (WBBSE) Class 10 student.\n")
            append("Subject: $subjectContext\n")
            append("Topic / Concept to illustrate: $conceptQuery\n\n")
            append("STRICT VISUAL REQUIREMENTS:\n")
            append("- Clean pure white or warm-ivory textbook background with crisp dark vector-style lines and high-contrast colors.\n")
            append("- Include clear, accurate, legible labels in easy-to-understand English and Bengali terms where appropriate (e.g., 'Anaphase / অ্যানাফেজ', 'Spindle Fibres', 'P vs V Curve', or clear map locations).\n")
            append("- Show clear directional arrows, numbered stages, or axes/equations if it is a process, reaction, or graph.\n")
            append("- No clutter, no photorealistic distractions, no watermarks — pure high-scoring Madhyamik textbook diagram clarity.")
        }

        return try {
            withTimeout(ONLINE_TIMEOUT_MS) {
                val response = visualDiagramModel.generateContent(diagramPrompt)
                val imagePart = response.candidates.firstOrNull()
                    ?.content?.parts?.filterIsInstance<ImagePart>()
                    ?.firstOrNull()
                imagePart?.image
                    ?: throw IllegalStateException(
                        "AI মডেল এই অনুরোধে কোনো ডায়াগ্রাম ছবি ফেরত দেয়নি। আরও স্পষ্ট টপিক লিখে আবার চেষ্টা করো।"
                    )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Visual diagram generation timed out after 60s", e)
            throw IllegalStateException("ভিজ্যুয়াল ডায়াগ্রাম তৈরি করতে ৬০ সেকেন্ডের বেশি সময় লেগেছে (Timeout)। আবার চেষ্টা করো।")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Visual diagram generation failed", e)
            throw e
        }
    }

    override fun generateExamAssistantStream(
        query: String,
        analysis: RoutedPromptAnalysis,
        activeSession: ActiveExamSession?,
        mode: InferenceMode,
        config: LlamaInferenceConfig,
        handwrittenImage: Bitmap?
    ): Flow<AiStreamEvent> = flow {
        val startMs = System.currentTimeMillis()
        val resolvedSubject = analysis.detectedSubject ?: activeSession?.subject
        val allNotes = studyRepository.getMasterDataSnapshot(resolvedSubject?.id)
        val topMatches = rankRelevantNotes(query, allNotes).take(2)

        if (mode == InferenceMode.OFFLINE) {
            val (text, badge) = buildOfflineExamChatReply(
                query = query,
                analysis = analysis,
                subject = resolvedSubject,
                topMatches = topMatches,
                hasPhoto = handwrittenImage != null,
                config = config
            )
            emitWordStream(text, badge, resolvedSubject, startMs)
            return@flow
        }

        val sourceLabel = when (analysis.intent) {
            ExamChatIntent.DIRECT_STUDY -> "🎯 টু-দ্য-পয়েন্ট বোর্ড উত্তর • Gemini"
            ExamChatIntent.NORMAL_TEST -> "📚 নরমাল টেস্ট (অধ্যায়ভিত্তিক) • Gemini"
            ExamChatIntent.MADHYAMIK_TEST -> "⏱️ মাধ্যমিক এক্সাম হল • Gemini"
            ExamChatIntent.HANDWRITTEN_GRADING -> "📷 খাতার উত্তর মূল্যায়ন • Gemini"
            ExamChatIntent.TRANSLATION -> "🌐 বাংলা ↔ English অনুবাদ • Gemini"
        }

        val prompt = buildExamChatOnlinePrompt(
            query = query,
            analysis = analysis,
            activeSession = activeSession,
            subject = resolvedSubject,
            topMatches = topMatches,
            hasPhoto = handwrittenImage != null
        )

        val inputContent = content {
            if (handwrittenImage != null) {
                image(handwrittenImage)
            }
            text(prompt)
        }

        streamFromOnlineModel(inputContent, sourceLabel, resolvedSubject, startMs)
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

    private suspend fun kotlinx.coroutines.flow.FlowCollector<AiStreamEvent>.streamFromOnlineModel(
        inputContent: com.google.firebase.ai.type.Content,
        sourceLabel: String,
        resolvedSubject: WbbseSubject?,
        startMs: Long
    ) {
        val accumulatedText = StringBuilder()
        try {
            withTimeout(ONLINE_TIMEOUT_MS) {
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
            throw IllegalStateException("Gemini কোনো উত্তর দেয়নি। আবার চেষ্টা করো।")
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

    private suspend fun kotlinx.coroutines.flow.FlowCollector<AiStreamEvent>.emitWordStream(
        fullText: String,
        sourceLabel: String,
        resolvedSubject: WbbseSubject?,
        startMs: Long
    ) {
        val words = fullText.split(" ")
        val builder = StringBuilder()
        val chunkSize = if (words.size > 80) 5 else 2
        var index = 0
        while (index < words.size) {
            val end = min(index + chunkSize, words.size)
            for (i in index until end) {
                if (builder.isNotEmpty()) builder.append(' ')
                builder.append(words[i])
            }
            emit(AiStreamEvent.Partial(builder.toString(), sourceLabel))
            index = end
            delay(18L)
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
            withTimeout(ONLINE_TIMEOUT_MS) {
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

    private fun buildSmartMarksRule(marks: Int?): String = when (marks) {
        2 -> "TARGET WEIGHTAGE: 2 Marks (Strict limit: 30–40 words, exactly 2 crisp bullet points or definition + 1 formula/example)."
        3 -> "TARGET WEIGHTAGE: 3 Marks (Strict limit: 60–75 words, 3 bolded bullet points with formula/equation/date)."
        4 -> "TARGET WEIGHTAGE: 4 Marks (Strict limit: 90–110 words, 4 structured bold sub-headings + dates/names)."
        5 -> "TARGET WEIGHTAGE: 5 Marks (Strict limit: 120–150 words, 1-line intro + 4–5 bold sub-headed points + diagram/formula note + 1-line conclusion)."
        8 -> "TARGET WEIGHTAGE: 8 Marks (Strict limit: 220–260 words, crisp intro + 6 bold analytical sub-headings with historical dates/names + crisp conclusion)."
        else -> "Auto-match WBBSE Madhyamik mark weightage (2/3/4/5/8 marks) with strict concise word limit."
    }

    private fun buildNotebookEvaluationPrompt(
        userPrompt: String,
        subject: WbbseSubject?,
        smartCondense: Boolean,
        targetMarks: Int?
    ): String = buildString {
        val subjectHint = subject?.let { "${it.bengaliName} (${it.englishName})" }
            ?: "WBBSE Class 10 (Madhyamik)"
        append("A West Bengal Board (WBBSE) Class 10 student uploaded this image.\n")
        append("Subject: $subjectHint\n")
        append("Student instruction: ${userPrompt.ifBlank { "Evaluate my handwritten answer for WBBSE Class 10." }}\n\n")
        if (smartCondense) {
            append("SMART TO-THE-POINT MODE IS ACTIVE: If the image is a textbook/long note or question, extract and deliver a ZERO-FLUFF, 100% full-marks WBBSE Madhyamik board answer with bold keywords and exact bullet points. ${buildSmartMarksRule(targetMarks)}\n")
            append("If the image is a student's handwritten answer sheet, evaluate it strictly and provide exact marks + crisp full-marks correction.\n")
        } else {
            append("Please respond clearly in Bengali (or English if the notebook is in English) with these 5 structured sections:\n")
            append("1. 📝 খাতায় যা লেখা আছে (Brief reading/transcription summary)\n")
            append("2. ✍️ হাতের লেখা ও উপস্থাপনা (Handwriting legibility, neatness & presentation)\n")
            append("3. ✅ সঠিক তথ্য ও ভুলত্রুটি (What is correct vs factual/formula/spelling mistakes)\n")
            append("4. 📊 প্রাপ্ত মাধ্যমিক নম্বর (Exact marks awarded out of ${targetMarks ?: 5})\n")
            append("5. 🎯 পূর্ণ নম্বর পাওয়ার টু-দ্য-পয়েন্ট পরামর্শ (How to get 100% full marks)")
        }
    }

    private fun buildOnlinePrompt(
        query: String,
        subject: WbbseSubject?,
        topMatches: List<MasterDataEntity>,
        analysis: RoutedPromptAnalysis
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

        if (analysis.effectiveTranslate) {
            append("\nTRANSLATION MODE (Bengali ↔ English): Provide an accurate, natural WBBSE Class 10 academic translation between Bengali and English, plus 2–3 key vocabulary terms.\n")
        }

        if (analysis.effectiveSmartAnswer) {
            append("\n[SMART EXAM-READY TO-THE-POINT MODE ACTIVE — ZERO FLUFF]\n")
            append("- Eliminate ALL greetings, filler, long introductions, and irrelevant stories.\n")
            append("- Output ONLY the exact, high-scoring WBBSE Madhyamik board answer structured with numbered/bullet points, **bolded keywords**, exact formulas/dates/diagrams where required.\n")
            append("- ${buildSmartMarksRule(analysis.detectedMarks)}\n")
            append("- End with a 1-line ‘🏆 বোর্ড টিপস’ showing how to secure 100% full marks.\n")
        }

        if (analysis.effectiveVisualDiagram) {
            append("\n[AI VISUAL EXPLANATION ACTIVE]\n")
            append("- A labeled 2D visual diagram is being generated right alongside your text.\n")
            append("- Write a crisp, step-by-step visual walkthrough explaining each labeled part of the diagram in easy Bengali & English.\n")
        }

        append("\nStudent question:\n")
        append(query.trim())
    }

    private fun buildExamChatOnlinePrompt(
        query: String,
        analysis: RoutedPromptAnalysis,
        activeSession: ActiveExamSession?,
        subject: WbbseSubject?,
        topMatches: List<MasterDataEntity>,
        hasPhoto: Boolean
    ): String = buildString {
        val subjectLabel = subject?.let { "${it.bengaliName} (${it.englishName})" } ?: "WBBSE Class 10"
        append("You are inside EduAI Class 10's 3-in-1 WBBSE Madhyamik Exam & Quiz Assistant.\n")
        append("Subject: $subjectLabel\n")
        if (analysis.chapterHint != null) append("Chapter specified: ${analysis.chapterHint}\n")
        if (analysis.pageHint != null) append("Textbook Page specified: ${analysis.pageHint}\n")
        if (activeSession != null) {
            append("Active Exam Session: ${activeSession.title} (${activeSession.mode}, Full Marks: ${activeSession.fullMarks})\n")
        }
        if (topMatches.isNotEmpty()) {
            append("Syllabus reference:\n")
            topMatches.forEach { append("- ${it.chapterName}: ${it.topicTitle} (${it.keyFormulaOrFact})\n") }
        }
        append("\n")

        when (analysis.intent) {
            ExamChatIntent.DIRECT_STUDY -> {
                append("[INTENT A — DIRECT STUDY / SMART TO-THE-POINT BOARD ANSWER & CONDENSING]\n")
                append("- Zero fluff, zero long introduction.\n")
                append("- Give the exact, crisp, ready-to-memorize WBBSE Madhyamik answer with bold keywords, bullet points, formulas/dates.\n")
                append("- ${buildSmartMarksRule(analysis.detectedMarks)}\n")
            }

            ExamChatIntent.NORMAL_TEST -> {
                append("[INTENT B — NORMAL TEST MODE (UNTIMED RELAXED PRACTICE)]\n")
                append("- If the student is requesting a test, create a friendly, untimed chapter/page-focused WBBSE practice test (3 MCQs + 2 SAQs of 2 marks + 1 Broad question of 3/5 marks) strictly matching the requested subject, chapter (${analysis.chapterHint ?: "syllabus"}), and textbook page (${analysis.pageHint ?: "relevant pages"}).\n")
                append("- If the student is submitting answers to the active Normal Test, grade each answer warmly, show correct answers with simple explanations, and write 'প্রাপ্ত নম্বর: X/Y' clearly on its own line.\n")
            }

            ExamChatIntent.MADHYAMIK_TEST -> {
                append("[INTENT C — OFFICIAL WBBSE MADHYAMIK EXAM HALL SIMULATOR]\n")
                append("- Use official West Bengal Board of Secondary Education (WBBSE) Madhyamik paper structure:\n")
                append("  • বিভাগ ‘ক’ (MCQ — ১ নম্বর)\n")
                append("  • বিভাগ ‘খ’ (VSAQ — ১ নম্বর)\n")
                append("  • বিভাগ ‘গ’ (SAQ — ২ নম্বর)\n")
                append("  • বিভাগ ‘ঘ’ (বড় প্রশ্ন — ৩/৪/৫/৮ নম্বর)\n")
                append("- If the student is starting the test, print a formal, realistic Madhyamik board question set (with previous-year pattern questions) and instruct them to reply with typed answers or upload a handwritten sheet photo.\n")
                append("- If the student is submitting answers (typed or photo), grade strictly like a WBBSE Head Examiner, deduct marks for missing keywords/formulas/diagrams, and output 'প্রাপ্ত নম্বর: X/Y' clearly.\n")
            }

            ExamChatIntent.HANDWRITTEN_GRADING -> {
                val marks = analysis.detectedMarks ?: 5
                append("[CAMERA HANDWRITTEN BROAD ANSWER SHEET GRADING — $marks MARKS]\n")
                append("Evaluate the attached handwritten WBBSE Class 10 broad answer sheet strictly out of $marks marks.\n")
                append("Format clearly in Bengali (or English for English subject):\n")
                append("1. ✅ কতটুকু সঠিক লেখা হয়েছে\n")
                append("2. ❌ কোথায় ভুল বা ঘাটতি আছে (missing keywords/formulas/dates/diagrams)\n")
                append("3. 📊 প্রাপ্ত নম্বর: X/$marks\n")
                append("4. 🏆 পূর্ণ নম্বর পাওয়ার টু-দ্য-পয়েন্ট সংশোধিত রূপরেখা\n")
            }

            ExamChatIntent.TRANSLATION -> {
                append("[BENGALI ↔ ENGLISH TRANSLATION MODE]\n")
                append("Translate accurately between Bengali and English for WBBSE Class 10 study, keeping technical terms accurate.\n")
            }
        }

        if (analysis.effectiveVisualDiagram) {
            append("\nNote: An AI labeled 2D diagram is also being generated — include brief labeled key points explaining the visual.\n")
        }

        if (hasPhoto && analysis.intent != ExamChatIntent.HANDWRITTEN_GRADING) {
            append("\nThe student also attached a photo — read and incorporate it directly in your response.\n")
        }

        append("\nStudent message:\n${query.trim()}")
    }

    private fun buildOfflineExamChatReply(
        query: String,
        analysis: RoutedPromptAnalysis,
        subject: WbbseSubject?,
        topMatches: List<MasterDataEntity>,
        hasPhoto: Boolean,
        config: LlamaInferenceConfig
    ): Pair<String, String> {
        if (hasPhoto) {
            return buildOfflineImagePromptNotice(subject)
        }
        val targetSubject = subject ?: WbbseSubject.PHYSICAL_SCIENCE
        return when (analysis.intent) {
            ExamChatIntent.NORMAL_TEST -> {
                val mcqs = WbbseExamBank.getMcqs(targetSubject).shuffled().take(3)
                val saqs = WbbseExamBank.getSaqs(targetSubject).shuffled().take(2)
                val broad = WbbseExamBank.getLongQuestions(targetSubject).shuffled().take(1)
                val body = buildString {
                    append("📚 নরমাল টেস্ট (অফলাইন ব্যাংক • সময়সীমা নেই)\n")
                    append("বিষয়: ${targetSubject.bengaliName}")
                    if (analysis.chapterHint != null) append(" • ${analysis.chapterHint}")
                    if (analysis.pageHint != null) append(" • ${analysis.pageHint}")
                    append("\n\n**বিভাগ ক — MCQ (১×৩ = ৩ নম্বর):**\n")
                    mcqs.forEachIndexed { idx, q ->
                        append("${idx + 1}. ${q.question}\n   (${q.options.joinToString(" / ")})\n")
                    }
                    append("\n**বিভাগ খ — SAQ (২×২ = ৪ নম্বর):**\n")
                    saqs.forEachIndexed { idx, q ->
                        append("${idx + 1}. ${q.question}\n")
                    }
                    append("\n**বিভাগ গ — বড় প্রশ্ন (${broad.firstOrNull()?.maxMarks ?: 5} নম্বর):**\n")
                    broad.forEach { q -> append("• ${q.question}\n") }
                    append("\n💡 সম্পূর্ণ ২৪৫+ অফলাইন প্রশ্ন ও অটো-স্কোরিংয়ের জন্য উপরের **‘অফলাইন ব্যাংক’** ট্যাবেও যেতে পারো, অথবা অনলাইন মোডে চালু করলে যেকোনো নির্দিষ্ট পাতা থেকে নতুন AI প্রশ্ন পাবে!")
                }
                body to "অফলাইন নরমাল টেস্ট • ${WbbseExamBank.totalQuestionCount} প্রশ্ন ব্যাংক"
            }

            ExamChatIntent.MADHYAMIK_TEST -> {
                val mcqs = WbbseExamBank.getMcqs(targetSubject).shuffled().take(4)
                val saqs = WbbseExamBank.getSaqs(targetSubject).shuffled().take(3)
                val broads = WbbseExamBank.getLongQuestions(targetSubject).shuffled().take(2)
                val body = buildString {
                    append("🏛️ পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদ — মাধ্যমিক মক টেস্ট (অফলাইন সেট)\n")
                    append("বিষয়: ${targetSubject.bengaliName} (${targetSubject.englishName})\n\n")
                    append("**বিভাগ ‘ক’ — MCQ (১×৪ = ৪):**\n")
                    mcqs.forEachIndexed { i, q -> append("${i + 1}. ${q.question} [${q.options.joinToString(" | ")}]\n") }
                    append("\n**বিভাগ ‘গ’ — SAQ (২×৩ = ৬):**\n")
                    saqs.forEachIndexed { i, q -> append("${i + 1}. ${q.question}\n") }
                    append("\n**বিভাগ ‘ঘ’ — বড় প্রশ্ন (${broads.sumOf { it.maxMarks }} নম্বর):**\n")
                    broads.forEachIndexed { i, q -> append("${i + 1}. ${q.question}\n") }
                    append("\n⏱️ উপরের লাইভ কাউন্টডাউন টাইমার চালু হয়ে গেছে! খাতায় উত্তর লিখে অনলাইন মোডে ছবি তুললে সরাসরি বোর্ড-মানের নম্বর পাবে।")
                }
                body to "অফলাইন মাধ্যমিক এক্সাম হল সেট"
            }

            else -> {
                buildOfflineAnswer(
                    query = query,
                    subject = subject,
                    topMatches = topMatches,
                    config = config,
                    smartToThePoint = analysis.effectiveSmartAnswer,
                    visualRequested = analysis.effectiveVisualDiagram,
                    targetMarks = analysis.detectedMarks
                )
            }
        }
    }

    private fun buildOfflineImagePromptNotice(subject: WbbseSubject?): Pair<String, String> {
        val subjectName = subject?.bengaliName ?: "WBBSE ক্লাস ১০"
        val message = buildString {
            append("📷 হাতের লেখা খাতার ছবি মূল্যায়ন ($subjectName)\n\n")
            append("খাতার ছবি পড়ে হাতের লেখা, বানান ও মাধ্যমিক মানের নির্ভুলতা যাচাই করতে ভিশন AI প্রয়োজন, যা বর্তমানে **অনলাইন মোডে (Firebase AI Logic • Gemini)** কাজ করে।\n\n")
            append("👉 কী করবে:\n")
            append("১. চ্যাটের উপরের হাইব্রিড টগল সুইচটি **অনলাইন (Online)** মোডে চালু করো।\n")
            append("২. এরপর খাতার ছবিটি আবার যুক্ত করে পাঠাও—সঙ্গে সঙ্গে সঠিক নম্বর, ভুল সংশোধন ও পূর্ণ নম্বর পাওয়ার টিপস পেয়ে যাবে!")
        }
        return message to "অফলাইন মোড • অনলাইন সুইচ প্রয়োজন"
    }

    private fun buildOfflineAnswer(
        query: String,
        subject: WbbseSubject?,
        topMatches: List<MasterDataEntity>,
        config: LlamaInferenceConfig,
        smartToThePoint: Boolean = false,
        visualRequested: Boolean = false,
        targetMarks: Int? = null
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

        val sourceBadge = when {
            smartToThePoint && isGgufReady -> "🎯 অফলাইন টু-দ্য-পয়েন্ট • GGUF (${activeMeta?.quantizationHint})"
            smartToThePoint -> "🎯 অফলাইন টু-দ্য-পয়েন্ট WBBSE উত্তর"
            isGgufReady -> "অফলাইন WBBSE ডেটা • GGUF (${activeMeta?.quantizationHint})"
            else -> "অফলাইন WBBSE মাস্টার ডেটা"
        }

        val response = if (topMatches.isNotEmpty()) {
            val primary = topMatches.first()
            val primarySubject = WbbseSubject.fromId(primary.subjectId)
            buildString {
                if (smartToThePoint) {
                    append("🎯 **টু-দ্য-পয়েন্ট বোর্ড উত্তর")
                    if (targetMarks != null) append(" (${targetMarks} নম্বর)")
                    append(" — ${primary.topicTitle}**\n")
                    append("• **মূল পয়েন্ট:** ${primary.content}\n")
                    append("• **অবশ্যই লিখবে (সূত্র/তথ্য):** ${primary.keyFormulaOrFact}\n")
                    append("• **🏆 বোর্ড টিপস:** ${primary.examTip}")
                } else {
                    append("📘 ")
                    append(primarySubject?.let { "${it.bengaliName} (${it.englishName})" } ?: "WBBSE ক্লাস ১০")
                    append(" — ${primary.chapterName}\n\n")
                    append("🔹 ${primary.topicTitle}\n")
                    append(primary.content)
                    append("\n\n📌 মনে রাখার মূল সূত্র / তথ্য:\n")
                    append(primary.keyFormulaOrFact)
                    append("\n\n🎯 মাধ্যমিক পরীক্ষার টিপস:\n")
                    append(primary.examTip)
                }

                if (visualRequested) {
                    append("\n\n🎨 **ভিজ্যুয়াল ডায়াগ্রাম নোট:** AI লেবেলযুক্ত ২ডি ডায়াগ্রাম জেনারেট করতে উপরের সুইচ দিয়ে **অনলাইন মোড (Online)** চালু করে আবার পাঠাও।")
                }

                if (!isGgufReady && !smartToThePoint) {
                    append("\n\n💡 অফলাইন Llama নোট: পূর্ণ অফলাইন জেনারেটিভ উত্তরের জন্য ‘মডেল’ ট্যাবে ${GgufFileInspector.TARGET_MODEL_FILENAME} লোড করুন।")
                }
            }
        } else {
            val subjectLine = subject?.let { "${it.bengaliName} (${it.englishName})" }
                ?: "পশ্চিমবঙ্গ বোর্ড ক্লাস ১০ (৭টি বিষয়)"
            buildString {
                append("📚 $subjectLine — অফলাইন মাস্টার ডেটা অনুসন্ধান\n\n")
                append("তোমার প্রশ্ন “$query”-এর সরাসরি উত্তর অফলাইন মাস্টার ডেটায় পাওয়া যায়নি। বিস্তারিত টু-দ্য-পয়েন্ট উত্তর, অনুবাদ বা AI ভিজ্যুয়াল ডায়াগ্রামের জন্য উপরের সুইচ দিয়ে **অনলাইন মোড (Online)** চালু করো, অথবা এই অধ্যায়গুলো থেকে জিজ্ঞেস করো:\n")
                append("• ভৌত বিজ্ঞান: বয়েল-চার্লস, ওহমের সূত্র, স্নেলের সূত্র, পর্যায় সারণি\n")
                append("• জীবন বিজ্ঞান: অ্যানাফেজ-মাইটোসিস, অক্সিন, মেন্ডেল, নাইট্রোজেন চক্র\n")
                append("• গণিত: শ্রীধর আচার্য, পিথাগোরাস, বৃত্তস্থ চতুর্ভুজ, ত্রিকোণমিতি\n")
                append("• ইতিহাস ও ভূগোল: ১৮৫৭ মহাবিদ্রোহ, নীল-সাঁওতাল বিদ্রোহ, নদী-হিমবাহ, বার্খান")
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
