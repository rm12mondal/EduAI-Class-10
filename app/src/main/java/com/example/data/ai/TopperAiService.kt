package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.model.SectionTimeBreakdown
import com.example.model.TopperPaper
import com.example.model.TopperQuestion
import com.example.model.TopperQuestionFeedback
import com.example.model.TopperSectionType
import com.example.model.TopperSubjectTrack
import com.example.model.TopperSubmissionReport
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import kotlin.math.roundToInt

private const val TOPPER_TAG = "TopperAiService"
private const val TOPPER_ONLINE_MODEL = "gemini-3.8-flash"
private const val TOPPER_TIMEOUT_MS = 60_000L

/**
 * Firebase AI Logic + Offline Board Evaluator powering flagship "টপার (TOPPER)" mode:
 * - Online: Generates zero-repeat rotational WBBSE 90-mark blueprint Master Suggestion papers
 *   with 30–40% Twisted Questions + Bengali Question Decoders + Presentation Tips, and evaluates
 *   typed + multi-page handwritten camera sheets with Topper Readiness & Pacing analytics.
 * - Offline: Evaluates MCQs & SAQs locally using board rubrics, presents verified model answers
 *   + Presentation Tips for Broad self-check, and transparently prompts Online Mode for photo grading.
 */
class TopperAiService {

    private val paperSchema = Schema.obj(
        properties = mapOf(
            "paperTitle" to Schema.string(description = "Bengali/English title of the Topper Master Suggestion Set"),
            "previousYearTrendSummary" to Schema.string(description = "2-line WBBSE previous year pattern analysis in Bengali"),
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "sectionCode" to Schema.enumeration(listOf("A", "B", "C", "D", "E")),
                        "marks" to Schema.integer(description = "Marks weightage: 1, 2, 3, 4, 5, 8, or 10"),
                        "chapter" to Schema.string(description = "WBBSE Class 10 chapter name"),
                        "questionText" to Schema.string(description = "Complete question text in Bengali (or English for English track)"),
                        "options" to Schema.array(
                            items = Schema.string(),
                            description = "Exactly 4 options for Section A MCQ; empty array for non-MCQ"
                        ),
                        "correctOptionIndex" to Schema.integer(description = "0-3 for Section A MCQ; -1 for non-MCQ"),
                        "isTwisted" to Schema.boolean(description = "True if this is an indirect/twisted/conceptual question"),
                        "simpleMeaningBn" to Schema.string(description = "If isTwisted is true, simple Bengali explanation of what the question is actually asking; otherwise empty string"),
                        "modelAnswer" to Schema.string(description = "Crisp to-the-point WBBSE full-marks model answer"),
                        "presentationTip" to Schema.string(description = "Concise Bengali 'টপার টিপস' on how to score full marks (subheadings, keywords, pencil diagram/map, word limit)"),
                        "keyTerms" to Schema.array(items = Schema.string(), description = "3 to 6 essential board keywords")
                    )
                )
            )
        )
    )

    private val evaluationSchema = Schema.obj(
        properties = mapOf(
            "verdictTitle" to Schema.string(description = "Encouraging yet honest Bengali headline under 'তুমি কতটা ভালো করেছ'"),
            "strengths" to Schema.string(description = "Specific things the student did well across typed and handwritten answers"),
            "mistakesAndMissingKeywords" to Schema.string(description = "Specific factual/spelling/formula errors and missing WBBSE keywords"),
            "improvementAdvice" to Schema.string(description = "Actionable next steps to jump to 90%+ Topper level"),
            "subjectiveScores" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "questionId" to Schema.string(),
                        "marksAwarded" to Schema.double(description = "Marks awarded in 0.5 increments up to question maxMarks"),
                        "feedbackNote" to Schema.string(description = "Concise Bengali/English examiner comment for this answer")
                    )
                )
            )
        )
    )

    private val paperGeneratorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = TOPPER_ONLINE_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = paperSchema
                temperature = 0.65f
            }
        )
    }

    private val topperEvaluatorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = TOPPER_ONLINE_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = evaluationSchema
                temperature = 0.2f
            }
        )
    }

    suspend fun generateOnlineTopperPaper(
        track: TopperSubjectTrack,
        nextSetNumber: Int,
        previouslyCoveredChapters: List<String>,
        twistedChallengeMode: Boolean,
        fullBoardMode: Boolean
    ): TopperPaper {
        val twistedInstruction = if (twistedChallengeMode) {
            "TWISTED QUESTION CHALLENGE IS ON: At least 75% of the questions MUST be Twisted / Indirect / Conceptual ('ঘুরিয়ে আসা প্রশ্ন') where familiar WBBSE textbook concepts are framed from a tricky board-exam angle."
        } else {
            "Include 35% to 40% Twisted / Indirect / Conceptual Questions ('ঘুরিয়ে আসা প্রশ্ন') mixed with high-probability direct Madhyamik board questions."
        }

        val composition = if (fullBoardMode) {
            "Generate a rich 14-question WBBSE 90-Mark Blueprint Mock Set: 5 Section A MCQs (1 mark each), 3 Section B VSAQs (1 mark each), 3 Section C Analytical SAQs (2 marks each), 2 Section D Broad Questions (5 or 8 marks each), and 1 Section E Special Question (Koni / Writing Skill / Map Pointing — 5 or 10 marks)."
        } else {
            "Generate a focused 10-question Daily Rotational WBBSE Topper Set: 4 Section A MCQs (1 mark each), 2 Section B VSAQs (1 mark each), 2 Section C SAQs (2 marks each), 1 Section D Broad Question (5 or 8 marks), and 1 Section E Special Question (Koni / Writing / Map Pointing — 4 or 5 marks)."
        }

        val prompt = buildString {
            append("You are the Chief WBBSE Madhyamik Question Paper Setter for West Bengal Class 10.\n")
            append("Subject Track: ${track.bengaliName} (${track.englishName})\n")
            append("Blueprint: ${track.blueprintSummaryBn}\n")
            append("Daily Rotational Set Number: Set $nextSetNumber\n")
            if (previouslyCoveredChapters.isNotEmpty()) {
                append("Previously attempted topics to AVOID repeating (ensure fresh zero-repeat coverage): ${previouslyCoveredChapters.takeLast(12).joinToString(", ")}\n")
            }
            append("\nREQUIREMENTS:\n")
            append("1. $composition\n")
            append("2. $twistedInstruction\n")
            append("3. For EVERY question where isTwisted = true, write a crystal-clear simple Bengali 'simpleMeaningBn' decoding what straightforward textbook question it really means.\n")
            append("4. For EVERY question, provide a concise Bengali 'presentationTip' ('খাতা সাজানোর কৌশল' — subheadings, keywords to underline, pencil diagram/map, word limit).\n")
            append("5. Use Bengali language for all subjects EXCEPT English track (use English for questions/answers in English track, with Bengali simpleMeaningBn and presentationTip).\n")
        }

        val response = try {
            withTimeout(TOPPER_TIMEOUT_MS) {
                paperGeneratorModel.generateContent(prompt)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TOPPER_TAG, "Online Topper paper generation timed out", e)
            throw IllegalStateException("টপার প্রশ্নপত্র তৈরিতে ৬০ সেকেন্ডের বেশি সময় লেগেছে (Timeout)। আবার চেষ্টা করো।")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TOPPER_TAG, "Online Topper paper generation failed", e)
            throw e
        }

        val rawJson = response.text
            ?: throw IllegalStateException("AI টপার প্রশ্নপত্র তৈরি করতে পারেনি। আবার চেষ্টা করো।")

        val root = JSONObject(rawJson)
        val questionsJson = root.getJSONArray("questions")
        val parsedQuestions = mutableListOf<TopperQuestion>()
        for (i in 0 until questionsJson.length()) {
            val item = questionsJson.getJSONObject(i)
            val sectionCode = item.optString("sectionCode", "C")
            val section = TopperSectionType.entries.firstOrNull { it.code == sectionCode }
                ?: TopperSectionType.SECTION_C_SAQ_2M
            val optsArray = item.optJSONArray("options")
            val options = if (optsArray != null) {
                (0 until optsArray.length()).map { optsArray.getString(it) }
            } else {
                emptyList()
            }
            val keyTermsArray = item.optJSONArray("keyTerms")
            val keyTerms = if (keyTermsArray != null) {
                (0 until keyTermsArray.length()).map { keyTermsArray.getString(it) }
            } else {
                emptyList()
            }
            val marks = item.optInt("marks", if (section == TopperSectionType.SECTION_A_MCQ) 1 else 2).coerceIn(1, 10)
            parsedQuestions.add(
                TopperQuestion(
                    id = "ai_${track.id}_s${nextSetNumber}_q${i + 1}_${System.currentTimeMillis() % 10000}",
                    section = section,
                    marks = marks,
                    chapter = item.optString("chapter", track.bengaliName),
                    questionText = item.optString("questionText", ""),
                    options = if (section == TopperSectionType.SECTION_A_MCQ && options.size >= 2) options else emptyList(),
                    correctOptionIndex = if (section == TopperSectionType.SECTION_A_MCQ) {
                        item.optInt("correctOptionIndex", 0).coerceIn(0, (options.size - 1).coerceAtLeast(0))
                    } else {
                        -1
                    },
                    isTwisted = item.optBoolean("isTwisted", false),
                    simpleMeaningBn = item.optString("simpleMeaningBn", ""),
                    modelAnswer = item.optString("modelAnswer", ""),
                    presentationTip = item.optString(
                        "presentationTip",
                        "মূল কীওয়ার্ড আন্ডারলাইন করে পয়েন্ট আকারে স্পষ্টভাবে লিখবে।"
                    ),
                    keyTerms = keyTerms
                )
            )
        }

        if (parsedQuestions.isEmpty()) {
            throw IllegalStateException("AI কোনো প্রশ্ন পাঠায়নি। আবার চেষ্টা করো।")
        }

        val totalMarks = parsedQuestions.sumOf { it.marks }
        val durationSec = if (fullBoardMode) 195L * 60L else 60L * 60L // 3h 15m official or 1h compact

        return TopperPaper(
            setNumber = nextSetNumber,
            track = track,
            title = root.optString("paperTitle", "${track.bengaliName} — AI টপার সেট $nextSetNumber"),
            blueprintSubtitle = track.blueprintSummaryBn,
            isTwistedChallenge = twistedChallengeMode,
            isCapsule15Min = false,
            isMistakeRecovery = false,
            fullMarks = totalMarks,
            durationSeconds = durationSec,
            sourceBadge = "অনলাইন AI মাস্টার সাজেশন • Gemini ($TOPPER_ONLINE_MODEL)",
            previousYearTrendSummary = root.optString(
                "previousYearTrendSummary",
                "বিগত মাধ্যমিক ট্রেন্ড ও অধ্যায়ভিত্তিক ওয়েটেজ বিশ্লেষণ করে এই সেট তৈরি হয়েছে।"
            ),
            questions = parsedQuestions
        )
    }

    suspend fun evaluateTopperSubmissionOnline(
        paper: TopperPaper,
        mcqAnswers: Map<String, Int>,
        typedAnswers: Map<String, String>,
        photoPagesByQuestion: Map<String, List<Bitmap>>,
        timeBreakdown: SectionTimeBreakdown
    ): TopperSubmissionReport {
        val subjectiveQuestions = paper.questions.filter { it.section != TopperSectionType.SECTION_A_MCQ }

        // 1. Score all MCQs deterministically
        val mcqFeedbacks = paper.questions
            .filter { it.section == TopperSectionType.SECTION_A_MCQ }
            .map { q ->
                val chosen = mcqAnswers[q.id]
                val correct = chosen == q.correctOptionIndex
                val awarded = if (correct) q.marks.toFloat() else 0f
                val studentLabel = chosen?.let { q.options.getOrElse(it) { "উত্তর দেওয়া হয়নি" } } ?: "উত্তর দেওয়া হয়নি"
                TopperQuestionFeedback(
                    question = q,
                    marksAwarded = awarded,
                    maxMarks = q.marks.toFloat(),
                    isCorrectOrStrong = correct,
                    studentSummary = "তোমার উত্তর: $studentLabel",
                    feedbackNote = if (correct) "সঠিক উত্তর!" else "সঠিক উত্তর হবে: ${q.options.getOrElse(q.correctOptionIndex) { q.modelAnswer }}",
                    decoderNoteBn = buildDecoderNote(q),
                    presentationTip = q.presentationTip,
                    modelAnswer = q.modelAnswer
                )
            }

        // 2. Evaluate subjective typed + multi-page handwritten sheet photos via Gemini 3.8 Flash
        val evalPrompt = buildString {
            append("You are an official WBBSE Madhyamik Head Examiner grading a student's TOPPER Mock Test submission.\n")
            append("Paper: ${paper.title} (${paper.track.bengaliName})\n\n")
            append("Grade each subjective question strictly per WBBSE board rubrics (award 0 if completely blank and no photo attached).\n")
            append("Provide feedback in clear Bengali (or English for English writing/grammar details).\n\n")

            subjectiveQuestions.forEachIndexed { idx, q ->
                val typed = typedAnswers[q.id].orEmpty().trim()
                val photos = photoPagesByQuestion[q.id].orEmpty()
                append("--- Question ${idx + 1} [ID: ${q.id}] (${q.marks} Marks | Section ${q.section.code}) ---\n")
                append("Question: ${q.questionText}\n")
                append("Model Answer: ${q.modelAnswer}\n")
                append("Required Key Terms: ${q.keyTerms.joinToString(", ")}\n")
                append("Student Typed Answer: ${typed.ifBlank { "(No typed text)" }}\n")
                append("Handwritten Answer Sheet Photos Attached: ${photos.size} page(s)\n\n")
            }
            append("Attached images follow in the exact order of questions that have photos attached. Read every handwritten page carefully.")
        }

        val inputContent = content {
            text(evalPrompt)
            subjectiveQuestions.forEach { q ->
                val photos = photoPagesByQuestion[q.id].orEmpty()
                if (photos.isNotEmpty()) {
                    text("Handwritten sheet photo(s) for Question ID ${q.id} (${q.marks} marks):")
                    photos.take(3).forEach { bmp ->
                        image(bmp)
                    }
                }
            }
        }

        val response = try {
            withTimeout(TOPPER_TIMEOUT_MS) {
                topperEvaluatorModel.generateContent(inputContent)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TOPPER_TAG, "Online Topper grading timed out", e)
            throw IllegalStateException("টপার খাতা মূল্যায়নে ৬০ সেকেন্ডের বেশি সময় লেগেছে। আবার জমা দাও।")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TOPPER_TAG, "Online Topper grading failed", e)
            throw e
        }

        val jsonText = response.text
            ?: throw IllegalStateException("AI মূল্যায়নকারী কোনো ফলাফল পাঠায়নি। আবার চেষ্টা করো।")
        val json = JSONObject(jsonText)

        val scoresMap = mutableMapOf<String, Pair<Float, String>>()
        val scoresArray = json.optJSONArray("subjectiveScores")
        if (scoresArray != null) {
            for (i in 0 until scoresArray.length()) {
                val entry = scoresArray.getJSONObject(i)
                val qId = entry.optString("questionId", "")
                val raw = entry.optDouble("marksAwarded", 0.0).toFloat()
                val note = entry.optString("feedbackNote", "মূল্যায়ন সম্পন্ন।")
                scoresMap[qId] = raw to note
            }
        }

        val subjectiveFeedbacks = subjectiveQuestions.map { q ->
            val typed = typedAnswers[q.id].orEmpty().trim()
            val photoCount = photoPagesByQuestion[q.id].orEmpty().size
            val hasSubmission = typed.isNotBlank() || photoCount > 0
            val pair = scoresMap[q.id]
            val clamped = if (!hasSubmission) {
                0f
            } else {
                val raw = pair?.first ?: 0f
                ((raw * 2f).roundToInt() / 2f).coerceIn(0f, q.marks.toFloat())
            }
            val summary = buildString {
                if (typed.isNotBlank()) append("টাইপ উত্তর: ${typed.take(90)}")
                if (photoCount > 0) {
                    if (isNotEmpty()) append(" + ")
                    append("📷 $photoCount পাতা খাতার ছবি জমা দেওয়া হয়েছে")
                }
                if (isEmpty()) append("কোনো উত্তর জমা দেওয়া হয়নি")
            }

            TopperQuestionFeedback(
                question = q,
                marksAwarded = clamped,
                maxMarks = q.marks.toFloat(),
                isCorrectOrStrong = clamped >= q.marks * 0.7f,
                studentSummary = summary,
                feedbackNote = pair?.second ?: if (hasSubmission) "বোর্ড রুব্রিক অনুযায়ী মূল্যায়িত।" else "উত্তর ফাঁকা ছিল — মডেল উত্তরটি মুখস্থ করে নাও।",
                decoderNoteBn = buildDecoderNote(q),
                presentationTip = q.presentationTip,
                modelAnswer = q.modelAnswer
            )
        }

        val allFeedbacks = (mcqFeedbacks + subjectiveFeedbacks).sortedBy { fb ->
            paper.questions.indexOfFirst { it.id == fb.question.id }
        }
        val obtained = allFeedbacks.sumOf { it.marksAwarded.toDouble() }.toFloat()
        val total = paper.fullMarks.toFloat().coerceAtLeast(1f)
        val pct = ((obtained / total) * 100f).roundToInt().coerceIn(0, 100)

        return TopperSubmissionReport(
            paperTitle = paper.title,
            track = paper.track,
            setNumber = paper.setNumber,
            obtainedMarks = obtained,
            totalMarks = total,
            readinessPercent = pct,
            verdictTitle = json.optString("verdictTitle", defaultVerdict(pct)),
            strengths = json.optString("strengths", "MCQ ও মূল ধারণাগুলিতে ভালো দখল দেখা গেছে।"),
            mistakesAndMissingKeywords = json.optString("mistakesAndMissingKeywords", "কিছু বড় প্রশ্নে গুরুত্বপূর্ণ কীওয়ার্ড/সূত্র বাদ পড়েছে।"),
            improvementAdvice = json.optString("improvementAdvice", "ভুলের খাতা থেকে দুর্বল প্রশ্নগুলো রিভিশন করে পরবর্তী টপার সেট দাও।"),
            pacingAdviceBn = buildPacingAdvice(timeBreakdown),
            timeBreakdown = timeBreakdown,
            questionFeedbacks = allFeedbacks,
            evaluatorSource = "Firebase AI Logic • WBBSE Head Examiner ($TOPPER_ONLINE_MODEL)"
        )
    }

    fun evaluateTopperSubmissionOffline(
        paper: TopperPaper,
        mcqAnswers: Map<String, Int>,
        typedAnswers: Map<String, String>,
        photoPagesByQuestion: Map<String, List<Bitmap>>,
        timeBreakdown: SectionTimeBreakdown
    ): TopperSubmissionReport {
        var attachedAnyPhoto = false

        val feedbacks = paper.questions.map { q ->
            if (q.section == TopperSectionType.SECTION_A_MCQ) {
                val chosen = mcqAnswers[q.id]
                val correct = chosen == q.correctOptionIndex
                TopperQuestionFeedback(
                    question = q,
                    marksAwarded = if (correct) q.marks.toFloat() else 0f,
                    maxMarks = q.marks.toFloat(),
                    isCorrectOrStrong = correct,
                    studentSummary = "তোমার উত্তর: ${chosen?.let { q.options.getOrElse(it) { "উত্তর দেওয়া হয়নি" } } ?: "উত্তর দেওয়া হয়নি"}",
                    feedbackNote = if (correct) "✅ একদম সঠিক উত্তর!" else "❌ সঠিক উত্তর: ${q.options.getOrElse(q.correctOptionIndex) { q.modelAnswer }}",
                    decoderNoteBn = buildDecoderNote(q),
                    presentationTip = q.presentationTip,
                    modelAnswer = q.modelAnswer
                )
            } else {
                val typed = typedAnswers[q.id].orEmpty().trim()
                val photoCount = photoPagesByQuestion[q.id].orEmpty().size
                if (photoCount > 0) attachedAnyPhoto = true

                val isBroad = q.section == TopperSectionType.SECTION_D_BROAD || q.section == TopperSectionType.SECTION_E_SPECIAL
                val normalized = typed.lowercase()
                val matched = q.keyTerms.filter { normalized.contains(it.lowercase()) }
                val missing = q.keyTerms.filterNot { normalized.contains(it.lowercase()) }

                val awarded = when {
                    typed.isBlank() && photoCount == 0 -> 0f
                    typed.isBlank() && photoCount > 0 -> 0f // Needs Online Vision AI
                    else -> {
                        val ratio = if (q.keyTerms.isEmpty()) 0.6f else matched.size.toFloat() / q.keyTerms.size.toFloat()
                        val lengthBoost = if (typed.length >= 40) 0.15f else 0f
                        val raw = ((ratio + lengthBoost).coerceIn(0f, 1f) * q.marks)
                        ((raw * 2f).roundToInt() / 2f).coerceIn(0f, q.marks.toFloat())
                    }
                }

                val note = when {
                    photoCount > 0 && typed.isBlank() ->
                        "📷 $photoCount পাতা খাতার ছবি যুক্ত হয়েছে — ছবি মূল্যায়নের জন্য অনলাইন মোড চালু করো, অথবা নীচের যাচাইকৃত মডেল উত্তর দেখে নিজে মিলিয়ে নাও।"
                    isBroad ->
                        "অফলাইন যাচাইকৃত মডেল উত্তর দেওয়া হলো (স্ব-মূল্যায়নের জন্য)। মিল পাওয়া কীওয়ার্ড: ${matched.ifEmpty { listOf("কোনোটিই নয়") }.joinToString(", ")}।"
                    else ->
                        if (missing.isEmpty()) "দারুণ! সব মূল শব্দ তোমার উত্তরে আছে।" else "অনুপস্থিত কীওয়ার্ড: ${missing.take(4).joinToString(", ")}"
                }

                val summary = buildString {
                    if (typed.isNotBlank()) append("টাইপ উত্তর: ${typed.take(85)}")
                    if (photoCount > 0) {
                        if (isNotEmpty()) append(" + ")
                        append("📷 $photoCount পাতা ছবি")
                    }
                    if (isEmpty()) append("উত্তর দেওয়া হয়নি")
                }

                TopperQuestionFeedback(
                    question = q,
                    marksAwarded = awarded,
                    maxMarks = q.marks.toFloat(),
                    isCorrectOrStrong = awarded >= q.marks * 0.7f,
                    studentSummary = summary,
                    feedbackNote = note,
                    decoderNoteBn = buildDecoderNote(q),
                    presentationTip = q.presentationTip,
                    modelAnswer = q.modelAnswer
                )
            }
        }

        val obtained = feedbacks.sumOf { it.marksAwarded.toDouble() }.toFloat()
        val total = paper.fullMarks.toFloat().coerceAtLeast(1f)
        val pct = ((obtained / total) * 100f).roundToInt().coerceIn(0, 100)
        val strongCount = feedbacks.count { it.isCorrectOrStrong }
        val weakQuestions = feedbacks.filterNot { it.isCorrectOrStrong }

        return TopperSubmissionReport(
            paperTitle = paper.title,
            track = paper.track,
            setNumber = paper.setNumber,
            obtainedMarks = obtained,
            totalMarks = total,
            readinessPercent = pct,
            verdictTitle = defaultVerdict(pct),
            strengths = "মোট ${feedbacks.size}টি প্রশ্নের মধ্যে ${strongCount}টিতে তুমি চমৎকার ফল করেছ।",
            mistakesAndMissingKeywords = if (weakQuestions.isNotEmpty()) {
                "${weakQuestions.size}টি প্রশ্ন ভুলের খাতায় সংরক্ষিত হয়েছে — বিশেষ করে ${weakQuestions.take(2).joinToString(", ") { it.question.chapter }} অধ্যায়ে আরও অনুশীলন দরকার।"
            } else {
                "কোনো বড় ঘাটতি পাওয়া যায়নি — দুর্দান্ত প্রস্তুতি!"
            },
            improvementAdvice = "বড় প্রশ্নের মডেল উত্তর ও ‘টপার টিপস’ দেখে খাতা সাজানোর অভ্যাস করো এবং ভুলের খাতা থেকে রিকভারি টেস্ট দাও।",
            pacingAdviceBn = buildPacingAdvice(timeBreakdown),
            timeBreakdown = timeBreakdown,
            questionFeedbacks = feedbacks,
            evaluatorSource = "অফলাইন WBBSE যাচাইকৃত রুব্রিক ও মডেল উত্তর",
            offlinePhotoWarning = if (attachedAnyPhoto) {
                "তুমি খাতার ছবি আপলোড করেছ — হাতের লেখা খাতার ছবি AI মূল্যায়নের জন্য ইন্টারনেট সংযোগসহ অনলাইন মোড (Online Mode) চালু করো।"
            } else {
                null
            }
        )
    }

    private fun buildDecoderNote(q: TopperQuestion): String? {
        if (!q.isTwisted) return null
        val decoded = q.simpleMeaningBn.ifBlank { q.chapter }
        return "এই প্রশ্নটি ঘুরিয়ে করা হয়েছে — এর আসল মানে হলো: $decoded, এবং এখানে পাঠ্যবইয়ের এই মূল উত্তরটি পয়েন্ট করে লিখতে হবে।"
    }

    private fun defaultVerdict(pct: Int): String = when {
        pct >= 85 -> "🏆 অসাধারণ! তুমি মাধ্যমিক টপার স্তরের কাছাকাছি!"
        pct >= 65 -> "🌟 ভালো প্রস্তুতি! আর একটু ঘষামাজা করলেই ৯০%+ নিশ্চিত।"
        pct >= 40 -> "📘 মাঝারি প্রস্তুতি — দুর্বল অধ্যায় ও ঘোরানো প্রশ্নগুলো বেশি প্র্যাকটিস করো।"
        else -> "💪 শুরুটা ভালো হয়েছে — ভুলের খাতা ও মডেল উত্তর পড়ে আবার চেষ্টা করো!"
    }

    private fun buildPacingAdvice(t: SectionTimeBreakdown): String {
        val mcqMin = (t.mcqSeconds / 60f)
        val saqMin = (t.saqSeconds / 60f)
        val broadMin = (t.broadSeconds / 60f)
        return buildString {
            append("বিভাগভিত্তিক সময় বিশ্লেষণ: MCQ = ${formatMinSec(t.mcqSeconds)}, SAQ = ${formatMinSec(t.saqSeconds)}, বড় প্রশ্ন = ${formatMinSec(t.broadSeconds)}। ")
            when {
                mcqMin > 8f -> append("MCQ-তে তুলনামূলক বেশি সময় লেগেছে — ৩ ঘণ্টা ১৫ মিনিটের আসল পরীক্ষায় বিভাগ ‘ক’ ২৫ মিনিটের মধ্যে শেষ করার লক্ষ্য রাখো।")
                broadMin < 1f && saqMin > 0f -> append("বড় প্রশ্নে পর্যাপ্ত সময় দিয়ে সাব-হেডিং ও পেন্সিল চিত্রসহ উত্তর লেখার অভ্যাস করো (আসল পরীক্ষায় প্রায় ১ ঘণ্টা ৪০ মিনিট হাতে রাখবে)।")
                else -> append("তোমার সময়ের গতি ভারসাম্যপূর্ণ! ৩ ঘণ্টা ১৫ মিনিটের মাধ্যমিক পরীক্ষার শেষ ১৫ মিনিট সর্বদা খাতা রিভিশনের জন্য রাখবে।")
            }
        }
    }

    private fun formatMinSec(seconds: Long): String {
        val m = seconds / 60L
        val s = seconds % 60L
        return "${m} মি. ${s} সে."
    }
}
