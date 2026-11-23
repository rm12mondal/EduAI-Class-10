package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.exam.WbbseExamBank
import com.example.data.gguf.GgufModelManager
import com.example.data.gguf.LlamaNativeBridge
import com.example.data.local.MasterDataEntity
import com.example.data.local.StudyRepository
import com.example.model.GgufModelStatus
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.MemorizerPointCard
import com.example.model.QuickQuizQuestion
import com.example.model.QuickTopicQuiz
import com.example.model.SmartMemorizerDeck
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

private const val TAG = "SmartLearningService"
private const val TUTOR_MODEL_NAME = "gemini-3.8-flash"
private const val TIMEOUT_MS = 60_000L

/**
 * Dedicated engine for "স্মার্ট লার্নিং (Smart Learning)":
 * 1. Friendly, ultra-simple Bengali AI Tutor streaming explanations (Online Gemini 3.8 Flash + Offline .gguf & verified notes)
 * 2. Multimodal Camera/Gallery textbook page, diagram & math problem solver
 * 3. Auto-detection of complex visual Science/Geo/History/Geometry concepts for AI Diagram generation
 * 4. 2-Minute Quick Topic Quiz generator (Online JSON schema + Offline verified WBBSE bank)
 * 5. Smart Memorizer Broad-Question Deck generator (Memory Hook + Step-by-Step Active Recall Cards)
 */
class SmartLearningService(
    private val studyRepository: StudyRepository,
    private val ggufModelManager: GgufModelManager
) {
    private val tutorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = TUTOR_MODEL_NAME,
            systemInstruction = content {
                text(
                    "You are 'স্মার্ট লার্নিং টিউটর' (Smart Learning Tutor), the kindest, most engaging " +
                        "West Bengal Board (WBBSE Class 10 / Madhyamik) private tutor. " +
                        "Your mission is to make EVERY student—even a struggling learner—say 'ওহ! এটা তো এত সহজ!' " +
                        "Always explain in ultra-simple, warm, conversational Bengali (keeping key English scientific/math terms in brackets). " +
                        "Use everyday Bengali real-life examples (like cooking, cricket, bicycle, monsoon rain, school life), " +
                        "short analogies or mini-stories, and clear numbered step-by-step breakdowns. " +
                        "For math/physics problems, show every algebraic step clearly without skipping lines. " +
                        "End every explanation with a tiny 2-line '💡 মনে রাখার শর্টকাট' (Easy Recap)."
                )
            }
        )
    }

    private val quickQuizSchema = Schema.obj(
        properties = mapOf(
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "question" to Schema.string(description = "Short clear WBBSE Class 10 MCQ question in Bengali (or English if English subject)"),
                        "options" to Schema.array(items = Schema.string(), description = "Exactly 4 answer choices"),
                        "correctIndex" to Schema.integer(description = "0-based index (0 to 3) of the correct option"),
                        "explanationBn" to Schema.string(description = "1-2 line simple Bengali explanation of why this option is right")
                    )
                ),
                description = "Exactly 4 high-yield MCQs testing understanding of the explained topic"
            )
        )
    )

    private val quickQuizModel by lazy {
        Firebase.ai.generativeModel(
            modelName = TUTOR_MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = quickQuizSchema
                temperature = 0.35f
            }
        )
    }

    private val memorizerSchema = Schema.obj(
        properties = mapOf(
            "broadQuestionBn" to Schema.string(description = "Most probable 5-mark or 8-mark WBBSE Madhyamik board question on this topic"),
            "targetMarks" to Schema.integer(description = "Board marks weightage: 3, 4, 5, or 8"),
            "memoryHookBn" to Schema.string(description = "A fun Bengali mnemonic, acronym, or easy story trick to remember all main points effortlessly"),
            "keywords" to Schema.array(items = Schema.string(), description = "5-7 must-write board keywords/formulas/dates"),
            "points" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "headingBn" to Schema.string(description = "Short bold sub-heading for the board answer"),
                        "bodyBn" to Schema.string(description = "Concise 2-3 sentence exam-ready explanation in simple Bengali"),
                        "recallCueBn" to Schema.string(description = "A 3-6 word hint clue to help the student recall this point when hidden")
                    )
                ),
                description = "4 to 5 structured board points easy to memorize step-by-step"
            ),
            "examTipBn" to Schema.string(description = "1-line board presentation tip (diagram, underline, formula box) for full marks")
        )
    )

    private val memorizerModel by lazy {
        Firebase.ai.generativeModel(
            modelName = TUTOR_MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = memorizerSchema
                temperature = 0.35f
            }
        )
    }

    /**
     * Auto-detects when a student's Smart Learning query involves a complex Science, Geography,
     * History, or Geometry topic where a visual 2D diagram/mind-map dramatically aids comprehension.
     */
    fun shouldAutoGenerateDiagram(
        query: String,
        subject: WbbseSubject?,
        chapterName: String?
    ): Boolean {
        val combined = "$query ${chapterName.orEmpty()}".lowercase()
        val explicitVisualKeywords = listOf(
            "ছবি", "চিত্র", "ডায়াগ্রাম", "diagram", "graph", "গ্রাফ", "ম্যাপ", "মানচিত্র", "map",
            "flowchart", "ফ্লোচার্ট", "mind map", "মাইন্ড ম্যাপ", "আঁক", "দেখাও", "visual"
        )
        if (explicitVisualKeywords.any { combined.contains(it) }) return true

        val visualConceptKeywords = listOf(
            "অ্যানাফেজ", "মেটাফেজ", "মাইটোসিস", "মিয়োসিস", "নিউরোন", "প্রতিবর্ত", "সিন্যাপস", "চোখ", "অক্ষিগোলক",
            "হরমোন", "ক্রোমোজোম", "ডিএনএ", "dna", "নাইট্রোজেন চক্র", "চেকার বোর্ড", "মেন্ডেল",
            "বয়েলের সূত্র", "চার্লসের সূত্র", "প্রিজম", "প্রতিসরণ", "লেন্স", "দর্পণ", "তড়িৎ", "রোধ", "ওহম", "পর্যায় সারণি",
            "তড়িৎ বিশ্লেষণ", "অ্যামোনিয়া", "হাইড্রোজেন সালফাইড", "জৈব রসায়ন",
            "নদী", "হিমবাহ", "বায়ুপ্রবাহ", "বৃষ্টিচ্ছায়", "মৌসুমি", "সমুদ্রস্রোত", "জোয়ার", "ভাটা", "পর্বত", "মৃত্তিকা",
            "উপপাদ্য", "পিথাগোরাস", "বৃত্ত", "স্পর্শক", "গোলক", "শঙ্কু", "চোঙ", "ত্রিকোণমিতি", "উচ্চতা ও দূরত্ব",
            "সিপাহী বিদ্রোহ", "মহাবিদ্রোহ", "সাঁওতাল", "নীল বিদ্রোহ", "বঙ্গভঙ্গ", "অসহযোগ",
            "anaphase", "neuron", "reflex", "boyle", "prism", "lens", "ohm", "theorem", "circle", "glacier", "river"
        )
        if (visualConceptKeywords.any { combined.contains(it) }) return true

        // For Science/Geo subjects with "explain / বোঝাও / কীভাবে কাজ করে", auto-aid visually
        val isVisualSubject = subject in listOf(
            WbbseSubject.LIFE_SCIENCE,
            WbbseSubject.PHYSICAL_SCIENCE,
            WbbseSubject.GEOGRAPHY
        )
        val asksProcess = listOf("কীভাবে", "পদ্ধতি", "দশা", "গঠন", "প্রক্রিয়া", "how does", "process", "structure")
            .any { combined.contains(it) }
        return isVisualSubject && asksProcess
    }

    fun streamTutorExplanation(
        query: String,
        selectedSubject: WbbseSubject?,
        selectedChapter: String?,
        mode: InferenceMode,
        config: LlamaInferenceConfig,
        textbookPhoto: Bitmap?,
        diagramRequested: Boolean
    ): Flow<AiStreamEvent> = flow {
        val startMs = System.currentTimeMillis()
        val allNotes = studyRepository.getMasterDataSnapshot(null)
        val detectedSubject = selectedSubject ?: detectSubjectFromQuery(query)
        val scopedNotes = if (detectedSubject != null) {
            allNotes.filter { it.subjectId == detectedSubject.id }
        } else {
            allNotes
        }

        val matchedNotes = findBestNotes(query, selectedChapter, scopedNotes)
            .ifEmpty { findBestNotes(query, selectedChapter, allNotes) }
            .take(2)

        val resolvedSubject = detectedSubject
            ?: matchedNotes.firstOrNull()?.let { WbbseSubject.fromId(it.subjectId) }

        if (mode == InferenceMode.OFFLINE) {
            val (offlineText, sourceBadge) = buildOfflineTutorExplanation(
                query = query,
                subject = resolvedSubject,
                chapterName = selectedChapter,
                matchedNotes = matchedNotes,
                config = config,
                diagramRequested = diagramRequested
            )
            emitWordByWord(offlineText, sourceBadge, resolvedSubject, startMs)
            return@flow
        }

        val sourceLabel = when {
            textbookPhoto != null -> "📷 স্মার্ট লার্নিং ছবি ব্যাখ্যা • Gemini"
            diagramRequested -> "🎨 স্মার্ট লার্নিং + ভিজ্যুয়াল টিউটর • Gemini"
            else -> "🎓 স্মার্ট লার্নিং AI টিউটর • Gemini"
        }

        val prompt = buildOnlineTutorPrompt(
            query = query,
            subject = resolvedSubject,
            chapterName = selectedChapter,
            matchedNotes = matchedNotes,
            hasPhoto = textbookPhoto != null,
            diagramRequested = diagramRequested
        )

        val inputContent = content {
            if (textbookPhoto != null) {
                image(textbookPhoto)
            }
            text(prompt)
        }

        val accumulated = StringBuilder()
        try {
            withTimeout(TIMEOUT_MS) {
                tutorModel.generateContentStream(inputContent).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(AiStreamEvent.Partial(accumulated.toString(), sourceLabel))
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Smart Learning tutor timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Smart Learning tutor streaming failed", e)
            throw e
        }

        val finalText = accumulated.toString().trim()
        if (finalText.isBlank()) {
            throw IllegalStateException("AI টিউটর কোনো উত্তর দেয়নি। আবার চেষ্টা করো।")
        }

        emit(
            AiStreamEvent.Completed(
                fullText = finalText,
                sourceLabel = sourceLabel,
                resolvedSubject = resolvedSubject,
                latencyMs = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
            )
        )
    }

    /**
     * Generates a 2-Minute Quick Quiz (4 MCQs) on the exact topic just explained.
     */
    suspend fun generateQuickTopicQuiz(
        topicTitle: String,
        explanationContext: String,
        subject: WbbseSubject?,
        chapterName: String?,
        mode: InferenceMode
    ): QuickTopicQuiz {
        if (mode == InferenceMode.ONLINE) {
            val subjectName = subject?.let { "${it.bengaliName} (${it.englishName})" } ?: "WBBSE Class 10"
            val prompt = buildString {
                append("Create a 2-minute rapid conceptual check quiz (4 MCQs) for a WBBSE Class 10 student ")
                append("who just studied this topic in Smart Learning.\n")
                append("Subject: $subjectName\n")
                if (!chapterName.isNullOrBlank()) append("Chapter: $chapterName\n")
                append("Topic: $topicTitle\n")
                append("Recent explanation context:\n${explanationContext.take(1200)}\n\n")
                append("Requirements: Exactly 4 short, clear WBBSE Class 10 MCQs with 4 options each and a 1-line friendly Bengali explanation.")
            }

            return try {
                val response = withTimeout(TIMEOUT_MS) {
                    quickQuizModel.generateContent(prompt)
                }
                val jsonText = response.text ?: throw IllegalStateException("কুইজ তৈরি করা যায়নি।")
                val json = JSONObject(jsonText)
                val arr = json.getJSONArray("questions")
                val questions = (0 until arr.length()).map { idx ->
                    val item = arr.getJSONObject(idx)
                    val optsArr = item.getJSONArray("options")
                    val opts = (0 until optsArr.length()).map { optsArr.getString(it) }
                    QuickQuizQuestion(
                        id = "sl_quiz_${System.currentTimeMillis()}_$idx",
                        question = item.getString("question"),
                        options = opts,
                        correctIndex = item.optInt("correctIndex", 0).coerceIn(0, (opts.size - 1).coerceAtLeast(0)),
                        explanationBn = item.optString("explanationBn", "পাঠ্যবইয়ের মূল ধারণা অনুযায়ী এটি সঠিক উত্তর।")
                    )
                }
                if (questions.isEmpty()) throw IllegalStateException("কোনো প্রশ্ন পাওয়া যায়নি।")
                QuickTopicQuiz(
                    topicTitle = topicTitle,
                    subject = subject,
                    chapterName = chapterName,
                    sourceLabel = "অনলাইন ২-মিনিট কুইজ • Gemini",
                    questions = questions,
                    durationSeconds = 120
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Online quick quiz failed", e)
                throw e
            }
        }

        // Offline Mode: Topic-matched verified WBBSE MCQs from built-in 252+ question bank
        val targetSubject = subject ?: detectSubjectFromQuery(topicTitle) ?: WbbseSubject.PHYSICAL_SCIENCE
        val bankMcqs = WbbseExamBank.getMcqs(targetSubject)
        val tokens = tokenize(topicTitle + " " + chapterName.orEmpty())
        val prioritized = bankMcqs.sortedByDescending { q ->
            val hay = "${q.question} ${q.explanation}".lowercase()
            tokens.count { hay.contains(it) }
        }.take(4)

        val questions = prioritized.map { q ->
            QuickQuizQuestion(
                id = "offline_sl_${q.id}",
                question = q.question,
                options = q.options,
                correctIndex = q.correctIndex,
                explanationBn = q.explanation
            )
        }

        return QuickTopicQuiz(
            topicTitle = topicTitle,
            subject = targetSubject,
            chapterName = chapterName,
            sourceLabel = "অফলাইন ২-মিনিট কুইজ • WBBSE ব্যাংক",
            questions = questions,
            durationSeconds = 120
        )
    }

    /**
     * Generates a Smart Memorizer Deck for memorizing the topic's high-probability Broad Question
     * step-by-step using a Bengali memory hook, keywords, and Active Recall flashcards.
     */
    suspend fun generateSmartMemorizerDeck(
        topicTitle: String,
        explanationContext: String,
        subject: WbbseSubject?,
        chapterName: String?,
        mode: InferenceMode
    ): SmartMemorizerDeck {
        if (mode == InferenceMode.ONLINE) {
            val subjectName = subject?.let { "${it.bengaliName} (${it.englishName})" } ?: "WBBSE Class 10"
            val prompt = buildString {
                append("Create a 'Smart Memorizer' step-by-step broad question memorization deck for a WBBSE Class 10 (Madhyamik) student.\n")
                append("Subject: $subjectName\n")
                if (!chapterName.isNullOrBlank()) append("Chapter: $chapterName\n")
                append("Topic: $topicTitle\n")
                append("Explanation context:\n${explanationContext.take(1200)}\n\n")
                append("Pick the most important 3, 4, 5, or 8-mark WBBSE Madhyamik board question on this topic and break it into 4–5 bite-sized Bengali memorization cards with a catchy memory trick (mnemonic) and short recall cues.")
            }

            return try {
                val response = withTimeout(TIMEOUT_MS) {
                    memorizerModel.generateContent(prompt)
                }
                val jsonText = response.text ?: throw IllegalStateException("মেমোরাইজার তৈরি করা যায়নি।")
                val json = JSONObject(jsonText)
                val kwArr = json.getJSONArray("keywords")
                val keywords = (0 until kwArr.length()).map { kwArr.getString(it) }
                val ptsArr = json.getJSONArray("points")
                val points = (0 until ptsArr.length()).map { idx ->
                    val p = ptsArr.getJSONObject(idx)
                    MemorizerPointCard(
                        stepNumber = idx + 1,
                        headingBn = p.getString("headingBn"),
                        bodyBn = p.getString("bodyBn"),
                        recallCueBn = p.optString("recallCueBn", p.getString("headingBn"))
                    )
                }
                if (points.isEmpty()) throw IllegalStateException("মেমোরাইজার পয়েন্ট পাওয়া যায়নি।")

                SmartMemorizerDeck(
                    topicTitle = topicTitle,
                    subject = subject,
                    broadQuestionBn = json.getString("broadQuestionBn"),
                    targetMarks = json.optInt("targetMarks", 5).coerceIn(2, 8),
                    memoryHookBn = json.getString("memoryHookBn"),
                    keywords = keywords,
                    points = points,
                    examTipBn = json.optString("examTipBn", "বোর্ডের খাতায় প্রতিটি সাব-হেডিং আন্ডারলাইন করে পয়েন্ট আকারে লিখবে।"),
                    sourceLabel = "অনলাইন স্মার্ট মেমোরাইজার • Gemini"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Online smart memorizer failed", e)
                throw e
            }
        }

        // Offline Mode: Build a verified Smart Memorizer Deck from WBBSE Broad Question Bank + Master Notes
        val targetSubject = subject ?: detectSubjectFromQuery(topicTitle) ?: WbbseSubject.PHYSICAL_SCIENCE
        val broadBank = WbbseExamBank.getLongQuestions(targetSubject)
        val tokens = tokenize("$topicTitle ${chapterName.orEmpty()}")
        val bestBroad = broadBank.maxByOrNull { q ->
            val hay = "${q.question} ${q.modelOutline} ${q.keyTerms.joinToString(" ")}".lowercase()
            tokens.count { hay.contains(it) }
        } ?: broadBank.first()

        val allNotes = studyRepository.getMasterDataSnapshot(targetSubject.id)
        val matchedNote = findBestNotes(topicTitle, chapterName, allNotes).firstOrNull()

        val outlineLines = bestBroad.modelOutline
            .split("\n", "।")
            .map { it.trim().trimStart('•', '-', '*', '১', '২', '৩', '৪', '৫', '.', ' ') }
            .filter { it.length >= 10 }
            .take(4)

        val cards = if (outlineLines.isNotEmpty()) {
            outlineLines.mapIndexed { idx, line ->
                val parts = line.split(":", "—", limit = 2)
                val heading = if (parts.size == 2) parts[0].trim() else "ধাপ ${idx + 1} — মূল পয়েন্ট"
                val body = if (parts.size == 2) parts[1].trim() else line
                MemorizerPointCard(
                    stepNumber = idx + 1,
                    headingBn = heading,
                    bodyBn = body,
                    recallCueBn = bestBroad.keyTerms.getOrNull(idx)?.let { "মূল শব্দ: $it" } ?: heading
                )
            }
        } else {
            listOf(
                MemorizerPointCard(
                    stepNumber = 1,
                    headingBn = matchedNote?.topicTitle ?: "মূল ধারণা",
                    bodyBn = matchedNote?.content ?: bestBroad.modelOutline,
                    recallCueBn = matchedNote?.keyFormulaOrFact?.take(36) ?: "মূল সূত্র ও সংজ্ঞা"
                )
            )
        }

        val hookTerms = bestBroad.keyTerms.take(4).joinToString(" → ")
        return SmartMemorizerDeck(
            topicTitle = topicTitle,
            subject = targetSubject,
            broadQuestionBn = bestBroad.question,
            targetMarks = bestBroad.maxMarks,
            memoryHookBn = "সহজ সূত্র চেইন মনে রাখো: $hookTerms — এই শব্দগুলো পরপর মনে রাখলেই পুরো উত্তর সহজে লেখা যাবে!",
            keywords = bestBroad.keyTerms,
            points = cards,
            examTipBn = matchedNote?.examTip
                ?: "বোর্ডের খাতায় প্রতিটি পয়েন্টের নিচে আন্ডারলাইন ও প্রয়োজনে পেন্সিল দিয়ে চিহ্নিত চিত্র দেবে।",
            sourceLabel = "অফলাইন স্মার্ট মেমোরাইজার • WBBSE ব্যাংক"
        )
    }

    private fun buildOnlineTutorPrompt(
        query: String,
        subject: WbbseSubject?,
        chapterName: String?,
        matchedNotes: List<MasterDataEntity>,
        hasPhoto: Boolean,
        diagramRequested: Boolean
    ): String = buildString {
        append("Subject: ${subject?.let { "${it.bengaliName} (${it.englishName})" } ?: "WBBSE Class 10 (All Subjects)"}\n")
        if (!chapterName.isNullOrBlank()) {
            append("Selected Chapter: $chapterName\n")
        }
        if (matchedNotes.isNotEmpty()) {
            append("\nBuilt-in WBBSE Syllabus Anchor:\n")
            matchedNotes.forEach { n ->
                append("- ${n.chapterName} / ${n.topicTitle}: ${n.keyFormulaOrFact}\n")
            }
        }
        if (hasPhoto) {
            append("\n[TEXTBOOK / MATH PROBLEM PHOTO ATTACHED]\n")
            append("- Carefully read the printed textbook paragraph, diagram, or math problem in the image.\n")
            append("- Explain the exact concept shown in ultra-simple Bengali with a real-life example, OR if it is a math/physics numerical problem, solve it step-by-step clearly without skipping any calculation step.\n")
        }
        if (diagramRequested) {
            append("\n[VISUAL DIAGRAM BEING GENERATED ALONGSIDE]\n")
            append("- A labeled 2D educational diagram/mind-map is being created right next to your reply. Walk the student through the visual parts step-by-step in simple Bengali.\n")
        }
        append("\nRESPONSE FORMAT (Warm, Easy-to-Understand Bengali Tutor):\n")
        append("1. 🌱 সহজ কথায় ব্যাপারটা কী? (1-2 super simple lines)\n")
        append("2. 🚲 বাস্তব জীবনের উদাহরণ (An everyday Bengali real-life story/analogy)\n")
        append("3. 🪜 ধাপে ধাপে ব্যাখ্যা / সমাধান (Clear numbered steps; full formula/derivation if math/science)\n")
        append("4. 💡 মনে রাখার শর্টকাট (2-line easy recap for Madhyamik exam)\n\n")
        append("Student asks: $query")
    }

    private fun buildOfflineTutorExplanation(
        query: String,
        subject: WbbseSubject?,
        chapterName: String?,
        matchedNotes: List<MasterDataEntity>,
        config: LlamaInferenceConfig,
        diagramRequested: Boolean
    ): Pair<String, String> {
        val ggufState = ggufModelManager.state.value
        val activeMeta = ggufState.activeMetadata
        val isGgufLoaded = ggufState.status == GgufModelStatus.READY_LOADED && activeMeta != null

        val primaryNote = matchedNotes.firstOrNull()
        val llamaSupplement = if (isGgufLoaded && activeMeta != null) {
            val ragContext = primaryNote?.let {
                "${it.topicTitle} (${it.chapterName}): ${it.content}\nKey Formula/Fact: ${it.keyFormulaOrFact}"
            } ?: "WBBSE Class 10 ${subject?.englishName ?: "General"} conceptual definition."
            val formattedPrompt = LlamaNativeBridge.buildLlama32Prompt(
                userQuery = "Explain concisely in simple Bengali/English without inventing facts: $query",
                subject = subject,
                ragContext = ragContext
            )
            LlamaNativeBridge.generateNativeIfLoaded(
                prompt = formattedPrompt,
                config = config.copy(
                    maxTokens = config.maxTokens.coerceAtMost(256),
                    temperature = config.temperature.coerceAtMost(0.35f)
                )
            )?.trim()?.takeIf { it.isNotBlank() }
        } else null

        val sourceLabel = if (isGgufLoaded && activeMeta != null) {
            "অফলাইন স্মার্ট লার্নিং • ${activeMeta.fileName}"
        } else {
            "অফলাইন স্মার্ট লার্নিং • WBBSE অধ্যায় নোট"
        }

        val text = buildString {
            if (primaryNote != null) {
                val subj = WbbseSubject.fromId(primaryNote.subjectId) ?: subject
                append("🎓 **${primaryNote.topicTitle}** (${subj?.bengaliName ?: "ক্লাস ১০"} • ${primaryNote.chapterName})\n\n")
                append("🌱 **সহজ ভাষায় মূল কথা:**\n${primaryNote.content}\n\n")
                append("📐 **অবশ্যই মনে রাখার সূত্র / মূল তথ্য:**\n${primaryNote.keyFormulaOrFact}\n\n")
                if (!llamaSupplement.isNullOrBlank()) {
                    append("🤖 **অফলাইন Llama-3.2 সংক্ষিপ্ত ব্যাখ্যা:**\n$llamaSupplement\n\n")
                }
                append("💡 **মাধ্যমিক বোর্ডের সহজ টিপস:**\n${primaryNote.examTip}")
            } else {
                append("🎓 **অফলাইন স্মার্ট লার্নিং সহায়ক (${subject?.bengaliName ?: chapterName ?: "WBBSE ক্লাস ১০"})**\n\n")
                if (!llamaSupplement.isNullOrBlank()) {
                    append("🤖 **অফলাইন Llama-3.2 উত্তর:**\n$llamaSupplement\n\n")
                } else {
                    append("তোমার প্রশ্ন “$query”-এর সরাসরি অধ্যায় নোট অফলাইন ডেটাবেসে খুঁজে পাওয়া যায়নি।\n\n")
                    append("• উপরের **অধ্যায় (Chapter)** ড্রপডাউন থেকে যেকোনো অধ্যায় বেছে নিয়ে সরাসরি পড়তে পারো।\n")
                    append("• অথবা **মডেল** ট্যাব থেকে `Llama-3.2-1B-Instruct.Q4_K_M.gguf` লোড করলে অফলাইনেও চ্যাট উত্তর পাবে।\n")
                    append("• বিস্তারিত গল্পের মতো ব্যাখ্যা, অঙ্কের ধাপে ধাপে সমাধান ও বইয়ের ছবির জন্য উপরের সুইচ দিয়ে **অনলাইন মোড** চালু করো।")
                }
            }

            if (diagramRequested) {
                append("\n\n🎨 *নোট: AI দিয়ে নতুন ২ডি ডায়াগ্রাম আঁকতে উপরের সুইচ দিয়ে অনলাইন মোড (Online Mode) চালু করো।*")
            }
        }

        return text to sourceLabel
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<AiStreamEvent>.emitWordByWord(
        fullText: String,
        sourceLabel: String,
        resolvedSubject: WbbseSubject?,
        startMs: Long
    ) {
        val words = fullText.split(" ")
        val sb = StringBuilder()
        val step = if (words.size > 70) 5 else 2
        var i = 0
        while (i < words.size) {
            val end = min(i + step, words.size)
            for (k in i until end) {
                if (sb.isNotEmpty()) sb.append(' ')
                sb.append(words[k])
            }
            emit(AiStreamEvent.Partial(sb.toString(), sourceLabel))
            i = end
            delay(16L)
        }
        emit(
            AiStreamEvent.Completed(
                fullText = fullText,
                sourceLabel = sourceLabel,
                resolvedSubject = resolvedSubject,
                latencyMs = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
            )
        )
    }

    private fun findBestNotes(
        query: String,
        chapterName: String?,
        notes: List<MasterDataEntity>
    ): List<MasterDataEntity> {
        if (notes.isEmpty()) return emptyList()
        val queryTokens = tokenize(query)
        val chapterLower = chapterName?.trim()?.lowercase().orEmpty()

        val scored = notes.map { note ->
            var score = 0
            val chapterMatch = chapterLower.isNotEmpty() && note.chapterName.lowercase().contains(chapterLower)
            if (chapterMatch) score += 12
            val titleLower = note.topicTitle.lowercase()
            val bodyLower = "${note.chapterName} ${note.keywords} ${note.keyFormulaOrFact} ${note.content}".lowercase()
            queryTokens.forEach { tok ->
                if (titleLower.contains(tok)) score += 6
                else if (bodyLower.contains(tok)) score += 2
            }
            note to score
        }.filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }

        return if (scored.isNotEmpty()) scored else notes.filter {
            chapterLower.isNotEmpty() && it.chapterName.lowercase().contains(chapterLower)
        }
    }

    private fun detectSubjectFromQuery(query: String): WbbseSubject? {
        val lower = query.lowercase()
        return when {
            listOf("বাংলা", "জ্ঞানচক্ষু", "বহুরূপী", "কোনি", "কারক", "সমাস", "আফ্রিকা").any { lower.contains(it) } -> WbbseSubject.BENGALI
            listOf("english", "grammar", "voice", "narration", "phrasal", "fable", "father's help").any { lower.contains(it) } -> WbbseSubject.ENGLISH
            listOf("গণিত", "অঙ্ক", "উপপাদ্য", "ত্রিকোণমিতি", "দ্বিঘাত", "গোলক", "শঙ্কু", "সুদ", "math", "theorem").any { lower.contains(it) } -> WbbseSubject.MATHEMATICS
            listOf("ভৌত", "বয়েল", "চার্লস", "ওহম", "প্রিজম", "লেন্স", "পর্যায়", "রসায়ন", "physics", "chemistry").any { lower.contains(it) } -> WbbseSubject.PHYSICAL_SCIENCE
            listOf("জীবন", "হরমোন", "নিউরোন", "কোষ", "মাইটোসিস", "অ্যানাফেজ", "মেন্ডেল", "biology", "neuron").any { lower.contains(it) } -> WbbseSubject.LIFE_SCIENCE
            listOf("ইতিহাস", "বিদ্রোহ", "বিদ্যাসাগর", "রামমোহন", "গান্ধী", "বঙ্গভঙ্গ", "history").any { lower.contains(it) } -> WbbseSubject.HISTORY
            listOf("ভূগোল", "নদী", "হিমবাহ", "মৌসুমি", "বায়ু", "মৃত্তিকা", "মানচিত্র", "geography").any { lower.contains(it) } -> WbbseSubject.GEOGRAPHY
            else -> null
        }
    }

    private fun tokenize(text: String): List<String> =
        text.lowercase()
            .split(Regex("[\\s,।?!.:;()\"'\\-/]+"))
            .map { it.trim() }
            .filter { it.length >= 2 }
            .distinct()
}
