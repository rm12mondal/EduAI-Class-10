package com.example.data.ai

import com.example.model.ActiveExamSession
import com.example.model.ChatPromptOptions
import com.example.model.ExamChatIntent
import com.example.model.ExamSessionMode
import com.example.model.WbbseSubject

data class RoutedPromptAnalysis(
    val intent: ExamChatIntent,
    val effectiveSmartAnswer: Boolean,
    val effectiveVisualDiagram: Boolean,
    val effectiveTranslate: Boolean,
    val detectedSubject: WbbseSubject?,
    val detectedMarks: Int?,
    val chapterHint: String?,
    val pageHint: String?,
    val recommendedMadhyamikDurationMinutes: Int
)

/**
 * Bilingual (Bengali + English) intent router and parameter parser for:
 * - Main Chat: auto-detects Smart To-The-Point Answer, AI Visual Diagram, Translation, and mark weightage.
 * - Exam Chat Box: routes seamlessly across Intent A (Direct Study/Condense), Intent B (Normal Untimed Test),
 *   Intent C (Madhyamik Exam Hall Timed Test), Handwritten Sheet Grading, and Translation.
 */
object ExamIntentRouter {

    private val smartAnswerKeywords = listOf(
        "আমার কাছে উত্তর নেই", "উত্তরটা দাও", "উত্তর দাও", "যতটুকু দরকার", "ততটুকুই দাও",
        "ছোট করে দাও", "সংক্ষেপে", "টু-দ্য-পয়েন্ট", "টু দ্য পয়েন্ট", "ফুল মার্কস",
        "পূর্ণ নম্বর", "বোর্ড উত্তর", "পয়েন্ট করে লেখো", "নোটটা ছোট করো",
        "to the point", "to-the-point", "condense", "shorten", "full marks",
        "exam ready", "exam-ready", "model answer", "crisp answer", "summarize for exam"
    )

    private val visualDiagramKeywords = listOf(
        "ছবি দিয়ে বুঝিয়ে দাও", "ছবি দিয়ে বোঝাও", "চিত্র দিয়ে", "চিত্র আঁকো", "ছবি আঁকো",
        "ডায়াগ্রাম", "চিত্র দেখাও", "ছবি দেখাও", "ফ্লোচার্ট", "গ্রাফ দেখাও", "মানচিত্র দেখাও",
        "ম্যাপ দেখাও", "দশা দেখাও", "চিত্রসহ", "সচিত্র",
        "diagram", "visual explain", "draw", "illustrate", "flowchart", "labeled diagram",
        "plot graph", "show map", "picture explain", "visualize"
    )

    private val translateKeywords = listOf(
        "অনুবাদ", "ট্রান্সলেট", "ইংরেজিতে লেখো", "ইংরেজিতে বলো", "ইংরেজিতে করো",
        "বাংলায় লেখো", "বাংলায় বলো", "বাংলায় অনুবাদ", "বাংলা মানে",
        "translate", "in english", "to english", "in bengali", "to bengali", "bengali meaning"
    )

    private val madhyamikTestKeywords = listOf(
        "মাধ্যমিক টেস্ট", "মাধ্যমিক পরীক্ষা", "মডেল পেপার", "বিগত বছর", "আগের বছর",
        "সালের প্রশ্ন", "ফুল মার্কস টেস্ট", "৯০ নম্বরের", "টাইমড টেস্ট", "এক্সাম হল",
        "madhyamik test", "madhyamik exam", "previous year", "pyq", "board paper",
        "mock paper", "exam hall", "timed test", "90 marks", "full paper"
    )

    private val normalTestKeywords = listOf(
        "নরমাল টেস্ট", "অধ্যায় থেকে টেস্ট", "পাতা থেকে টেস্ট", "পৃষ্ঠা থেকে টেস্ট",
        "প্রশ্ন ধরো", "টেস্ট নাও", "পরীক্ষা নাও", "কুইজ নাও", "অনুশীলন টেস্ট",
        "normal test", "practice test", "chapter test", "page test", "quiz me", "take a test"
    )

    fun analyzeForMainChat(
        query: String,
        selectedSubject: WbbseSubject?,
        options: ChatPromptOptions
    ): RoutedPromptAnalysis {
        val lower = query.lowercase()
        val smart = options.smartToThePoint || smartAnswerKeywords.any { lower.contains(it) }
        val visual = options.visualDiagramRequested || visualDiagramKeywords.any { lower.contains(it) }
        val translate = options.translateMode || translateKeywords.any { lower.contains(it) }
        val subject = selectedSubject ?: detectSubject(query)
        val marks = options.targetMarks ?: detectMarksWeightage(query)

        return RoutedPromptAnalysis(
            intent = if (translate) ExamChatIntent.TRANSLATION else ExamChatIntent.DIRECT_STUDY,
            effectiveSmartAnswer = smart,
            effectiveVisualDiagram = visual,
            effectiveTranslate = translate,
            detectedSubject = subject,
            detectedMarks = marks,
            chapterHint = extractChapterHint(query),
            pageHint = extractPageHint(query),
            recommendedMadhyamikDurationMinutes = 60
        )
    }

    fun analyzeForExamChat(
        query: String,
        selectedSubject: WbbseSubject?,
        hasPhotoAttachment: Boolean,
        activeSession: ActiveExamSession?,
        options: ChatPromptOptions
    ): RoutedPromptAnalysis {
        val lower = query.lowercase()
        val smart = options.smartToThePoint || smartAnswerKeywords.any { lower.contains(it) }
        val visual = options.visualDiagramRequested || visualDiagramKeywords.any { lower.contains(it) }
        val translate = options.translateMode || translateKeywords.any { lower.contains(it) }
        val subject = detectSubject(query) ?: selectedSubject ?: activeSession?.subject
        val marks = options.targetMarks ?: detectMarksWeightage(query)
        val chapterHint = extractChapterHint(query)
        val pageHint = extractPageHint(query)

        val asksForMadhyamik = madhyamikTestKeywords.any { lower.contains(it) }
        val asksForNormalTest = normalTestKeywords.any { lower.contains(it) } ||
            (pageHint != null && (lower.contains("টেস্ট") || lower.contains("test") || lower.contains("প্রশ্ন")))

        val intent = when {
            asksForMadhyamik -> ExamChatIntent.MADHYAMIK_TEST
            asksForNormalTest -> ExamChatIntent.NORMAL_TEST
            hasPhotoAttachment && !smart && !translate -> ExamChatIntent.HANDWRITTEN_GRADING
            translate -> ExamChatIntent.TRANSLATION
            smart -> ExamChatIntent.DIRECT_STUDY
            activeSession?.mode == ExamSessionMode.MADHYAMIK_TIMED -> ExamChatIntent.MADHYAMIK_TEST
            activeSession?.mode == ExamSessionMode.NORMAL_UNTIMED -> ExamChatIntent.NORMAL_TEST
            else -> ExamChatIntent.DIRECT_STUDY
        }

        val madhyamikMinutes = when {
            lower.contains("৯০") || lower.contains("90") || lower.contains("পূর্ণ") || lower.contains("full") || lower.contains("previous year") || lower.contains("বিগত") -> 195 // 3 hours 15 mins WBBSE Madhyamik official time
            lower.contains("৩০ মিনিট") || lower.contains("30 min") -> 30
            lower.contains("৪৫ মিনিট") || lower.contains("45 min") -> 45
            lower.contains("২ ঘণ্টা") || lower.contains("2 hour") -> 120
            else -> 90 // 1 hour 30 mins compact board simulator
        }

        return RoutedPromptAnalysis(
            intent = intent,
            effectiveSmartAnswer = smart || intent == ExamChatIntent.DIRECT_STUDY,
            effectiveVisualDiagram = visual,
            effectiveTranslate = translate,
            detectedSubject = subject,
            detectedMarks = marks,
            chapterHint = chapterHint,
            pageHint = pageHint,
            recommendedMadhyamikDurationMinutes = madhyamikMinutes
        )
    }

    fun detectSubject(query: String): WbbseSubject? {
        val lower = query.lowercase()
        return when {
            listOf("জীবন বিজ্ঞান", "জীবনবিজ্ঞান", "life science", "biology", "অ্যানাফেজ", "মাইটোসিস", "মিয়োসিস", "হরমোন", "অক্সিন", "মেন্ডেল", "dna", "প্রতিবর্ত").any { lower.contains(it) } ->
                WbbseSubject.LIFE_SCIENCE

            listOf("ভৌত বিজ্ঞান", "ভৌতবিজ্ঞান", "physical science", "physics", "chemistry", "বয়েল", "চার্লস", "ওহম", "স্নেল", "রাসায়নিক", "বিক্রিয়া", "পর্যায় সারণি", "লেন্স", "তড়িৎ").any { lower.contains(it) } ->
                WbbseSubject.PHYSICAL_SCIENCE

            listOf("গণিত", "অঙ্ক", "math", "mathematics", "ত্রিকোণমিতি", "দ্বিঘাত", "উপপাদ্য", "জ্যামিতি", "পরিমিতি", "পিথাগোরাস", "সুদ", "শ্রীধর").any { lower.contains(it) } ->
                WbbseSubject.MATHEMATICS

            listOf("ইতিহাস", "history", "সিপাহী", "বিদ্রোহ", "সাঁওতাল", "নীল", "রামমোহন", "বিদ্যাসাগর", "১৮৫৭", "মহারানি", "স্বদেশী", "ভারতসভা").any { lower.contains(it) } ->
                WbbseSubject.HISTORY

            listOf("ভূগোল", "geography", "নদী", "হিমবাহ", "বায়ুমণ্ডল", "জোয়ার", "মৃত্তিকা", "বার্খান", "বৃষ্টিচ্ছায়", "বর্জ্য", "মানচিত্র").any { lower.contains(it) } ->
                WbbseSubject.GEOGRAPHY

            listOf("বাংলা", "bengali", "জ্ঞানচক্ষু", "অসুখী একজন", "আফ্রিকা", "অভিষেক", "কারক", "সমাস", "বাক্য", "বাচ্য", "কোনি", "তপন").any { lower.contains(it) } ->
                WbbseSubject.BENGALI

            listOf("ইংরেজি", "english", "father's help", "fable", "bapu", "swami", "voice change", "narration", "phrasal verb", "paragraph", "notice writing").any { lower.contains(it) } ->
                WbbseSubject.ENGLISH

            else -> null
        }
    }

    fun detectMarksWeightage(query: String): Int? {
        val normalized = convertBengaliDigits(query).lowercase()
        val explicitMatch = Regex("""(\b[23458]\b)\s*(নম্বর|মার্কস|মার্ক|marks?|mark)""").find(normalized)
        if (explicitMatch != null) {
            return explicitMatch.groupValues[1].toIntOrNull()
        }
        val reverseMatch = Regex("""(নম্বর|মার্কস|marks?)\s*[:=-]?\s*(\b[23458]\b)""").find(normalized)
        if (reverseMatch != null) {
            return reverseMatch.groupValues[2].toIntOrNull()
        }
        return null
    }

    fun extractChapterHint(query: String): String? {
        val bnRegex = Regex("""([০-৯0-9]+(?:ম|য়|র্থ|ষ্ট|ষ্ঠ|শ)?\s*অধ্যায়|অধ্যায়\s*[০-৯0-9]+)""")
        bnRegex.find(query)?.let { return it.value.trim() }

        val enRegex = Regex("""(?i)(chapter\s*\d+|ch\.?\s*\d+|unit\s*\d+)""")
        enRegex.find(query)?.let { return it.value.trim() }
        return null
    }

    fun extractPageHint(query: String): String? {
        val bnRegex = Regex("""([০-৯0-9]+\s*(?:নম্বর\s*)?(?:পাতা|পৃষ্ঠা)|(?:পাতা|পৃষ্ঠা)\s*[০-৯0-9]+)""")
        bnRegex.find(query)?.let { return it.value.trim() }

        val enRegex = Regex("""(?i)(page\s*(?:no\.?\s*)?\d+|pg\.?\s*\d+)""")
        enRegex.find(query)?.let { return it.value.trim() }
        return null
    }

    fun convertBengaliDigits(input: String): String {
        val map = mapOf(
            '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
            '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
        )
        return buildString(input.length) {
            for (ch in input) {
                append(map[ch] ?: ch)
            }
        }
    }

    fun formatCountdownBengali(totalSeconds: Long): String {
        val safe = totalSeconds.coerceAtLeast(0L)
        val hours = safe / 3600L
        val minutes = (safe % 3600L) / 60L
        val seconds = safe % 60L
        return if (hours > 0L) {
            "তোমার কাছে বাকি সময়: ${hours} ঘণ্টা ${minutes} মিনিট ${seconds} সেকেন্ড"
        } else {
            "তোমার কাছে বাকি সময়: ${minutes} মিনিট ${seconds} সেকেন্ড"
        }
    }
}
