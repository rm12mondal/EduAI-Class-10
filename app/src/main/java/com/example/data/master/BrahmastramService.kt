package com.example.data.master

import com.example.data.exam.WbbseExamBank
import com.example.data.gguf.GgufModelManager
import com.example.data.local.MasterDataEntity
import com.example.data.local.TopicImportance
import com.example.model.BrahmastramExamQuestion
import com.example.model.BrahmastramExamReport
import com.example.model.BrahmastramGradedItem
import com.example.model.BrahmastramStudyNote
import com.example.model.GgufModelStatus
import com.example.model.InferenceMode
import com.example.model.WbbseSubject
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * Feature #4: "ব্রহ্মাস্ত্রম (Brahmastram — 3-Step Sure-Shot WBBSE Mastery Engine)"
 * Usable anytime (even 6+ months before exam):
 *  Step 1: Generates Sure-Shot WBBSE Suggestions & Complete Study Notes (Online Gemini JSON / Offline Verified DB)
 *  Step 2: Read / Listen via TTS / Export PDF / Mark Completed
 *  Step 3: Timed "ব্রহ্মাস্ত্রম প্রশ্নপত্র" Exam generated STRICTLY from those exact notes + Red-Pen & Trap Grading!
 */
class BrahmastramService(
    private val ggufModelManager: GgufModelManager
) {
    companion object {
        private const val ONLINE_MODEL = "gemini-3.8-flash"
    }

    suspend fun generateSureShotNotes(
        subject: WbbseSubject,
        inferenceMode: InferenceMode,
        allMasterNotes: List<MasterDataEntity>,
        completedIds: Set<String>
    ): Pair<List<BrahmastramStudyNote>, String> {
        val subjectNotes = allMasterNotes.filter { it.subjectId == subject.id }
            .sortedBy { if (it.importanceLevel == TopicImportance.VVI) 0 else 1 }

        val offlineNotes = buildOfflineBrahmastramNotes(subject, completedIds)

        if (inferenceMode == InferenceMode.ONLINE) {
            try {
                val aiNotes = generateOnlineBrahmastramNotes(subject, subjectNotes.take(14), completedIds)
                if (aiNotes.size >= 8) {
                    val existingTitles = aiNotes.map { it.title.trim().lowercase() }.toMutableSet()
                    val combined = aiNotes.toMutableList()
                    for (curated in offlineNotes) {
                        if (combined.size >= 20) break
                        val key = curated.title.trim().lowercase()
                        if (existingTitles.add(key)) {
                            val newId = "br_${subject.id}_${combined.size + 1}"
                            combined.add(curated.copy(id = newId, isCompleted = newId in completedIds))
                        }
                    }
                    return combined to "🌐 অনলাইন Gemini + ২০টি ব্রহ্মাস্ত্রম শিওর-শট সাজেশন"
                }
            } catch (_: Exception) {
                // Smooth fallback to verified offline WBBSE syllabus bank
            }
        }

        val badge = if (ggufModelManager.state.value.status == GgufModelStatus.READY_LOADED) {
            "⚡ অফলাইন Llama .gguf + ২০টি যাচাইকৃত WBBSE ব্রহ্মাস্ত্রম"
        } else {
            "⚡ ১০০% অফলাইন ২০টি যাচাইকৃত WBBSE ব্রহ্মাস্ত্রম নোটস"
        }
        return offlineNotes to badge
    }

    private suspend fun generateOnlineBrahmastramNotes(
        subject: WbbseSubject,
        syllabusSeeds: List<MasterDataEntity>,
        completedIds: Set<String>
    ): List<BrahmastramStudyNote> {
        val schema = Schema.obj(
            mapOf(
                "notes" to Schema.array(
                    Schema.obj(
                        mapOf(
                            "chapter" to Schema.string("WBBSE Chapter name in Bengali"),
                            "title" to Schema.string("Sure-shot topic title in Bengali"),
                            "marksWeight" to Schema.integer("Board marks: 2, 3, or 5"),
                            "sureShotNoteBn" to Schema.string("Complete exam-ready Bengali study note (4-6 clear sentences with keywords)"),
                            "keyFormulaOrFact" to Schema.string("Key formula, date, reaction, or author fact"),
                            "examQuestionPrompt" to Schema.string("Exact WBBSE board-style exam question strictly testing THIS note (include 1 tricky negative question like 'কোনটি নয়' if MCQ)"),
                            "mcqOptions" to Schema.array(Schema.string("Option text")),
                            "correctOptionIndex" to Schema.integer("0..3")
                        )
                    )
                )
            )
        )

        val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = ONLINE_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )

        val groundTruthChapters = syllabusSeeds.joinToString("\n") {
            "- ${it.chapterName}: ${it.topicTitle} (${it.keyFormulaOrFact})"
        }

        val prompt = """
            ${StrictWbbsePrecisionEngine.STRICT_FACT_LOCK_PROMPT_RULE}
            Generate 10 high-probability WBBSE Class 10 Madhyamik "ব্রহ্মাস্ত্রম (Brahmastram)" Sure-Shot Study Notes for:
            Subject: ${subject.bengaliName} (${subject.englishName})
            
            Ground every single note strictly in these verified WBBSE syllabus chapters:
            $groundTruthChapters
            
            For each of the 10 notes, include a clear board tip ("💡 বোর্ড টিপ:") inside sureShotNoteBn, key formulas/facts in keyFormulaOrFact, and generate 1 exact 4-option MCQ question strictly testing that note (with 4 options and correctOptionIndex 0..3). Include at least 2 questions with negative board trap words like 'নয়' or 'ব্যতীত'.
        """.trimIndent()

        val rawJson = withTimeout(45_000L) { model.generateContent(prompt).text.orEmpty() }
        val arr = JSONObject(rawJson).optJSONArray("notes") ?: JSONArray()
        val result = mutableListOf<BrahmastramStudyNote>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = "br_${subject.id}_${i + 1}"
            val chapter = o.optString("chapter").ifBlank { subject.bengaliName }
            val title = o.optString("title").ifBlank { continue }
            val marks = o.optInt("marksWeight", 3).coerceIn(2, 5)
            val noteBody = MathUnicodeFormatter.format(o.optString("sureShotNoteBn")).ifBlank { continue }
            val keyFact = MathUnicodeFormatter.format(o.optString("keyFormulaOrFact"))
            val qPrompt = MathUnicodeFormatter.format(
                o.optString("examQuestionPrompt").ifBlank { "$title সম্পর্কে মূল বোর্ড তথ্য কোনটি?" }
            )
            val optsArr = o.optJSONArray("mcqOptions")
            val opts = mutableListOf<String>()
            if (optsArr != null) {
                for (j in 0 until optsArr.length()) {
                    val s = optsArr.optString(j)
                    if (s.isNotBlank()) opts.add(MathUnicodeFormatter.format(s))
                }
            }
            val correctIdx = o.optInt("correctOptionIndex", 0).coerceIn(0, (opts.size - 1).coerceAtLeast(0))
            val pyq = StrictWbbsePrecisionEngine.getPyqTrend(subject.id, chapter, title, TopicImportance.VVI)
            result.add(
                BrahmastramStudyNote(
                    id = id,
                    chapter = chapter,
                    title = title,
                    marksWeight = marks,
                    pyqBadge = "${pyq.yearsBadge} • ${pyq.probabilityBadge}",
                    sureShotNoteBn = noteBody,
                    topperWritingStructureBn = StrictWbbsePrecisionEngine.formatTopperAnswerSheet(noteBody, subject, marks),
                    keyFormulaOrFact = keyFact,
                    examQuestionPrompt = qPrompt,
                    mcqOptions = if (opts.size >= 4) opts.take(4) else emptyList(),
                    correctOptionIndex = correctIdx,
                    isCompleted = id in completedIds
                )
            )
        }
        return result
    }

    private fun buildOfflineBrahmastramNotes(
        subject: WbbseSubject,
        completedIds: Set<String>
    ): List<BrahmastramStudyNote> {
        return BrahmastramOfflineBank.getCuratedNotes(subject, completedIds)
    }

    /**
     * Step 3: Builds a timed "ব্রহ্মাস্ত্রম প্রশ্নপত্র" STRICTLY from the generated Brahmastram notes!
     * Mixes 1-mark MCQs + 2/3-mark written answers tested directly from those exact notes.
     */
    fun buildExamPaperStrictlyFromNotes(
        notes: List<BrahmastramStudyNote>
    ): List<BrahmastramExamQuestion> {
        val questions = mutableListOf<BrahmastramExamQuestion>()
        notes.forEachIndexed { idx, note ->
            if (note.mcqOptions.size >= 4) {
                val trap = StrictWbbsePrecisionEngine.detectQuestionTraps(note.examQuestionPrompt)
                questions.add(
                    BrahmastramExamQuestion(
                        id = "brq_mcq_${idx + 1}",
                        sourceNoteId = note.id,
                        chapter = note.chapter,
                        questionText = note.examQuestionPrompt,
                        marks = 1,
                        options = note.mcqOptions,
                        correctOptionIndex = note.correctOptionIndex,
                        modelAnswer = note.mcqOptions.getOrElse(note.correctOptionIndex) { note.keyFormulaOrFact },
                        trapKeywords = trap.detectedTraps
                    )
                )
            }
            // Also include 2 written board questions from the top 2 notes (2/3 marks)
            if (idx < 2) {
                val writtenQ = "“${note.title}” (${note.chapter}) — এর মূল সূত্র/তথ্য ও ২টি গুরুত্বপূর্ণ বোর্ড পয়েন্ট লেখো।"
                questions.add(
                    BrahmastramExamQuestion(
                        id = "brq_saq_${idx + 1}",
                        sourceNoteId = note.id,
                        chapter = note.chapter,
                        questionText = writtenQ,
                        marks = 3,
                        options = emptyList(),
                        correctOptionIndex = -1,
                        modelAnswer = "${note.keyFormulaOrFact}\n${note.sureShotNoteBn.take(260)}",
                        trapKeywords = emptyList()
                    )
                )
            }
        }
        return questions
    }

    /**
     * Grades the Brahmastram Exam instantly offline with Red-Pen Deduction Log,
     * Question-Trap Alert, Math/Physics Step & Unit Checker, and Bengali/English Grammar Audit!
     */
    fun gradeBrahmastramExam(
        subject: WbbseSubject,
        questions: List<BrahmastramExamQuestion>,
        selectedOptions: Map<String, Int>,
        typedAnswers: Map<String, String>
    ): BrahmastramExamReport {
        var totalObtained = 0f
        var totalMax = 0f
        var trapCount = 0
        val gradedItems = mutableListOf<BrahmastramGradedItem>()

        questions.forEach { q ->
            totalMax += q.marks.toFloat()
            if (q.trapKeywords.isNotEmpty()) trapCount++

            if (q.options.isNotEmpty()) {
                val picked = selectedOptions[q.id]
                val isCorrect = picked == q.correctOptionIndex
                val score = if (isCorrect) q.marks.toFloat() else 0f
                totalObtained += score
                val deductions = mutableListOf<String>()
                if (!isCorrect) {
                    if (q.trapKeywords.isNotEmpty()) {
                        deductions.add("🚨 ট্র্যাপ অ্যালার্ট মিস: প্রশ্নে ‘${q.trapKeywords.joinToString("/")}’ ছিল — ভালো করে পড়তে হবে!")
                    }
                    deductions.add("🔴 সঠিক উত্তর: ${q.modelAnswer}")
                } else {
                    deductions.add("✅ সঠিক MCQ উত্তর (+${q.marks})")
                }
                gradedItems.add(
                    BrahmastramGradedItem(
                        question = q,
                        selectedOption = picked,
                        typedAnswer = "",
                        obtainedMarks = score,
                        maxMarks = q.marks.toFloat(),
                        redPenDeductions = deductions,
                        unitOrGrammarAudit = emptyList()
                    )
                )
            } else {
                val ans = typedAnswers[q.id].orEmpty().trim()
                val keyTerms = q.modelAnswer.split(Regex("""[\s,।:;()\-]+"""))
                    .filter { it.length >= 4 }
                    .distinct()
                    .take(8)
                val matched = keyTerms.count { term -> ans.contains(term, ignoreCase = true) }
                val missing = keyTerms.filterNot { term -> ans.contains(term, ignoreCase = true) }

                val baseRatio = when {
                    ans.isBlank() -> 0f
                    keyTerms.isEmpty() -> if (ans.length > 25) 0.8f else 0.4f
                    else -> ((matched.toFloat() / keyTerms.size.coerceAtLeast(1)) * 1.25f).coerceIn(0.25f, 1.0f)
                }
                val unitAudit = StrictWbbsePrecisionEngine.analyzeMathPhysicsStepsAndUnits(subject, q.questionText, ans)
                val langAudit = StrictWbbsePrecisionEngine.analyzeBengaliEnglishLanguage(subject, ans)

                var score = (q.marks * baseRatio * 2f).roundToInt() / 2f
                if (unitAudit != null && !unitAudit.hasProperUnit && score >= 0.5f) {
                    score = (score - 0.5f).coerceAtLeast(0f)
                }
                totalObtained += score

                val deductions = StrictWbbsePrecisionEngine.buildRedPenDeductionLog(
                    subject = subject,
                    questionText = q.questionText,
                    studentAnswer = ans,
                    obtainedMarks = score,
                    maxMarks = q.marks.toFloat(),
                    missingKeyTerms = missing
                )
                val extraAudits = buildList {
                    unitAudit?.let { add(it.summaryBn) }
                    langAudit?.let { add(it.summaryBn) }
                }
                gradedItems.add(
                    BrahmastramGradedItem(
                        question = q,
                        selectedOption = null,
                        typedAnswer = ans,
                        obtainedMarks = score,
                        maxMarks = q.marks.toFloat(),
                        redPenDeductions = deductions,
                        unitOrGrammarAudit = extraAudits
                    )
                )
            }
        }

        val accuracy = if (totalMax > 0f) ((totalObtained / totalMax) * 100f).roundToInt().coerceIn(0, 100) else 0
        val redSummary = when {
            accuracy >= 85 -> "🏆 অসাধারণ ব্রহ্মাস্ত্রম দখল! এই অধ্যায়ের বোর্ড প্রশ্নগুলোতে তুমি ১০০% প্রস্তুত।"
            accuracy >= 60 -> "👍 ভালো অগ্রগতি! লাল-কালির মন্তব্যগুলো দেখে নিয়ে আরও একবার ব্রহ্মাস্ত্রম পরীক্ষা দাও।"
            else -> "📚 ব্রহ্মাস্ত্রম নোটস আরেকবার পড়ে ও TTS-এ শুনে নিয়ে আবার পরীক্ষা দাও।"
        }

        return BrahmastramExamReport(
            subject = subject,
            obtainedMarks = totalObtained,
            maxMarks = totalMax,
            accuracyPercent = accuracy,
            gradedItems = gradedItems,
            trapAlertsCount = trapCount,
            redPenSummaryBn = redSummary
        )
    }
}
