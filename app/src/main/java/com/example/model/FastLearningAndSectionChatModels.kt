package com.example.model

import com.example.data.local.BrahmastramProgressEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.MasterDataEntity
import com.example.data.local.TopicImportance
import com.example.viewmodel.PendingPhotoAttachment
import com.example.viewmodel.StreamingDraft
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Isolated persistent chat namespaces stored in Room (`chat_messages.sectionId`)
 * so conversations across all 6 core sections never mix up and can be resumed anytime.
 */
enum class ChatSection(
    val id: String,
    val titleBn: String,
    val subtitleBn: String,
    val placeholderBn: String
) {
    MAIN_CHAT(
        id = "main_chat",
        titleBn = "মেইন চ্যাট",
        subtitleBn = "WBBSE ৭টি বিষয়ের AI টিউটর",
        placeholderBn = "যেকোনো প্রশ্ন লেখো বা মাইকে বলো…"
    ),
    SMART_LEARNING(
        id = "smart_learning",
        titleBn = "স্মার্ট লার্নিং চ্যাট",
        subtitleBn = "সহজ বাংলায় ধাপে ধাপে ব্যাখ্যা",
        placeholderBn = "যেকোনো টপিক সহজ বাংলায় বুঝতে প্রশ্ন লেখো…"
    ),
    BRAHMASTRAM(
        id = "brahmastram",
        titleBn = "ব্রহ্মাস্ত্রম চ্যাট",
        subtitleBn = "শিওর-শট সাজেশন ও বোর্ড ডাউট সলভার",
        placeholderBn = "ব্রহ্মাস্ত্রম নোটস বা সাজেশন নিয়ে প্রশ্ন লেখো…"
    ),
    MOCK_EXAM(
        id = "mock_exam",
        titleBn = "মক এক্সাম চ্যাট",
        subtitleBn = "৩-ইন-১ পরীক্ষক ও খাতা মূল্যায়ন",
        placeholderBn = "টেস্ট, প্রশ্ন বা খাতার ছবি নিয়ে লেখো…"
    ),
    MIND_MAP(
        id = "mind_map",
        titleBn = "মাইন্ড-ম্যাপ চ্যাট",
        subtitleBn = "ফ্লোচার্ট বিশ্লেষণ ও দ্রুত ডাউট ক্লিয়ার",
        placeholderBn = "মাইন্ড-ম্যাপের কোনো শাখা বা নতুন টপিক জিজ্ঞেস করো…"
    ),
    FAST_LEARNING(
        id = "fast_learning",
        titleBn = "ফাস্ট লার্নিং চ্যাট",
        subtitleBn = "আসন্ন বোর্ড প্যাটার্ন • মাইক্রো-কনসেপ্ট সামারি",
        placeholderBn = "যেকোনো VVI অধ্যায়ের ফাস্ট মাইক্রো-সামারি চাও…"
    ),
    STEP_BY_STEP_MEMORY(
        id = "step_by_step_memory",
        titleBn = "ধাপে ধাপে শিখি ও মনে রাখার টেকনিক",
        subtitleBn = "স্টেপ-বাই-স্টেপ টিউটর ও ছড়া-গল্পে মেমোরি ট্রিক",
        placeholderBn = "অঙ্ক, সংজ্ঞা, তারিখ বা ব্যাকরণের প্রশ্ন লেখো…"
    );

    companion object {
        fun fromId(id: String?): ChatSection = entries.firstOrNull { it.id == id } ?: MAIN_CHAT
    }
}

/**
 * Selectable Syllabus Completion Plans inside Fast Learning Hub.
 */
enum class SyllabusPlanOption(
    val months: Float,
    val totalDays: Int,
    val labelBn: String,
    val labelEn: String
) {
    MONTHS_2_5(2.5f, 75, "২.৫ মাস", "2.5 Months"),
    MONTHS_3(3.0f, 90, "৩ মাস", "3 Months"),
    MONTHS_4(4.0f, 120, "৪ মাস", "4 Months"),
    MONTHS_5(5.0f, 150, "৫ মাস", "5 Months");

    companion object {
        fun fromMonths(value: Float): SyllabusPlanOption =
            entries.minByOrNull { kotlin.math.abs(it.months - value) } ?: MONTHS_3
    }
}

data class FastLearningChapterItem(
    val chapterKey: String,
    val subject: WbbseSubject,
    val chapterName: String,
    val topicsCount: Int,
    val vviCount: Int,
    val estimatedBoardMarks: Int,
    val priorityScore: Int,
    val priorityBadgeBn: String,
    val keyTopicsPreview: String,
    val firstFormulaHint: String,
    val isCompleted: Boolean
)

data class FastLearningSelfReport(
    val targetExamYear: Int,
    val deviceDateTimeBn: String,
    val daysRemainingToExam: Int,
    val planOption: SyllabusPlanOption,
    val completedChaptersCount: Int,
    val totalChaptersCount: Int,
    val syllabusCompletionPercent: Int,
    val recommendedChaptersPerDay: Int,
    val mockTestsTaken: Int,
    val mockAveragePercent: Int,
    val motivationTitleBn: String,
    val motivationMessageBn: String
)

data class FastLearningUiState(
    val isOpen: Boolean = false,
    val activeTabIndex: Int = 0, // 0 = VVI Priority Syllabus & Self-Report, 1 = Fast Learning Chat
    val selectedSubject: WbbseSubject? = null,
    val targetExamYear: Int = FastLearningContextHelper.defaultTargetExamYear(),
    val availableExamYears: List<Int> = FastLearningContextHelper.availableExamYears(),
    val syllabusPlan: SyllabusPlanOption = SyllabusPlanOption.MONTHS_3,
    val completedChapterKeys: Set<String> = emptySet()
)

data class SectionChatUiState(
    val section: ChatSection,
    val messages: List<ChatMessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingDraft: StreamingDraft? = null,
    val pendingPhoto: PendingPhotoAttachment? = null,
    val errorMessage: String? = null
)

data class StepByStepMemoryUiState(
    val isOpen: Boolean = false,
    val selectedSubject: WbbseSubject = WbbseSubject.MATHEMATICS,
    val memoryTrickMode: Boolean = false
)

/**
 * Computes real-time device date/time, WBBSE Madhyamik countdown for selected Exam Year,
 * high-weightage VVI chapter sorting across all 7 subjects, Self-Report Card stats,
 * and AI prompt temporal/upcoming-pattern context.
 */
object FastLearningContextHelper {

    fun defaultTargetExamYear(): Int {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) // 0-indexed (0=Jan, 1=Feb)
        // WBBSE Madhyamik is held in February; after Feb, next board exam is year + 1
        return if (month <= Calendar.FEBRUARY) year.coerceAtLeast(2027) else (year + 1).coerceAtLeast(2027)
    }

    fun availableExamYears(): List<Int> {
        val base = defaultTargetExamYear()
        return listOf(base, base + 1, base + 2).distinct()
    }

    fun formatCurrentDeviceDateTime(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, EEE • hh:mm a", Locale.US)
        return sdf.format(Date())
    }

    fun daysRemainingUntilMadhyamik(targetExamYear: Int): Int {
        val now = Calendar.getInstance()
        val examDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, targetExamYear)
            set(Calendar.MONTH, Calendar.FEBRUARY)
            set(Calendar.DAY_OF_MONTH, 10)
            set(Calendar.HOUR_OF_DAY, 11)
            set(Calendar.MINUTE, 45)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = examDate.timeInMillis - now.timeInMillis
        val days = ceil(diffMs / (24.0 * 60.0 * 60.0 * 1000.0)).toInt()
        return days.coerceAtLeast(0)
    }

    fun buildUpcomingExamAiContextBlock(
        targetExamYear: Int,
        syllabusPlanLabel: String,
        fastMicroMode: Boolean
    ): String = buildString {
        val nowFormatted = formatCurrentDeviceDateTime()
        val daysLeft = daysRemainingUntilMadhyamik(targetExamYear)
        val prevYear = targetExamYear - 1
        append("\n[REAL-TIME EXAM TARGET & UPCOMING BOARD PATTERN CONTEXT]\n")
        append("- Current Device Date & Time: $nowFormatted\n")
        append("- Student's Target Exam: WBBSE Madhyamik $targetExamYear (Approx. $daysLeft days remaining until Feb $targetExamYear)\n")
        append("- Active Syllabus Completion Plan: $syllabusPlanLabel\n")
        append("- IMPORTANT PRIORITY: Prioritize upcoming WBBSE Madhyamik $targetExamYear expected patterns, high-weightage VVI concepts, analytical/application twists, and topics likely to appear in $targetExamYear (rather than merely repeating $prevYear paper questions verbatim).\n")
        if (fastMicroMode) {
            append("- FAST LEARNING MICRO-CONCEPT MODE ACTIVE: Deliver an ultra-concise, high-retention micro-concept summary for rapid mastery:\n")
            append("  1. ⚡ ৩-লাইনে মূল ধারণা (Core concept in 3 crisp bullets)\n")
            append("  2. 📌 মূল সূত্র / তারিখ / একক (Clean Unicode symbols: ², ³, ₂, ₄, ×, ÷, √, π, θ, Δ)\n")
            append("  3. 🎯 মাধ্যমিক $targetExamYear সম্ভাব্য বোর্ড প্রশ্ন ও নম্বর\n")
            append("  4. 🧠 ১০-সেকেন্ড মেমোরি ট্রিক (Easy 1-line mnemonic)\n")
        }
    }

    fun buildPrioritizedChapters(
        masterNotes: List<MasterDataEntity>,
        completedChapterKeys: Set<String>,
        selectedSubject: WbbseSubject? = null
    ): List<FastLearningChapterItem> {
        val filtered = if (selectedSubject != null) {
            masterNotes.filter { it.subjectId == selectedSubject.id }
        } else {
            masterNotes
        }

        val grouped = filtered.groupBy { "${it.subjectId}||${it.chapterName}" }
        return grouped.mapNotNull { (key, notes) ->
            val first = notes.firstOrNull() ?: return@mapNotNull null
            val subject = WbbseSubject.fromId(first.subjectId) ?: return@mapNotNull null
            val vviCount = notes.count { it.importanceLevel == TopicImportance.VVI }
            val mediumCount = notes.count { it.importanceLevel == TopicImportance.MEDIUM }

            // Compute high-weightage VVI priority score
            val estimatedMarks = (vviCount * 4 + mediumCount * 2 + (notes.size - vviCount - mediumCount))
                .coerceIn(3, 18)
            val priorityScore = vviCount * 30 + estimatedMarks * 4 + notes.size * 3

            val priorityBadge = when {
                vviCount >= 2 || estimatedMarks >= 10 -> "🔥 সুপার হাই-ওয়েটেজ VVI"
                vviCount >= 1 || estimatedMarks >= 6 -> "⚡ হাই-ওয়েটেজ VVI"
                else -> "📘 স্ট্যান্ডার্ড অধ্যায়"
            }

            val preview = notes.take(3).joinToString(" • ") { it.topicTitle }
            val formulaHint = notes.firstOrNull { it.keyFormulaOrFact.isNotBlank() }?.keyFormulaOrFact.orEmpty()

            FastLearningChapterItem(
                chapterKey = key,
                subject = subject,
                chapterName = first.chapterName,
                topicsCount = notes.size,
                vviCount = vviCount,
                estimatedBoardMarks = estimatedMarks,
                priorityScore = priorityScore,
                priorityBadgeBn = priorityBadge,
                keyTopicsPreview = preview,
                firstFormulaHint = formulaHint,
                isCompleted = key in completedChapterKeys
            )
        }.sortedWith(
            compareByDescending<FastLearningChapterItem> { it.priorityScore }
                .thenByDescending { it.vviCount }
                .thenBy { it.subject.ordinal }
                .thenBy { it.chapterName }
        )
    }

    fun computeSelfReport(
        targetExamYear: Int,
        planOption: SyllabusPlanOption,
        allMasterNotes: List<MasterDataEntity>,
        completedChapterKeys: Set<String>,
        examAttempts: List<ExamAttemptEntity>,
        brahmastramProgress: List<BrahmastramProgressEntity> = emptyList()
    ): FastLearningSelfReport {
        val allChapters = buildPrioritizedChapters(allMasterNotes, completedChapterKeys, selectedSubject = null)
        val totalChapters = allChapters.size.coerceAtLeast(1)
        val completedChapters = allChapters.count { it.isCompleted }

        // Blend completed Fast Learning chapters + Brahmastram progress bonus if any
        val brahmastramCompleted = brahmastramProgress.sumOf {
            it.completedNoteIdsCsv.split(",").count { id -> id.isNotBlank() }
        }
        val brahmastramTotal = (WbbseSubject.entries.size * 20).coerceAtLeast(1)
        val chapterPct = (completedChapters.toFloat() / totalChapters.toFloat()) * 100f
        val brahmastramBonus = (brahmastramCompleted.toFloat() / brahmastramTotal.toFloat()) * 15f
        val syllabusPct = (chapterPct + brahmastramBonus).roundToInt().coerceIn(0, 100)

        val daysLeft = daysRemainingUntilMadhyamik(targetExamYear)
        val effectivePlanDays = minOf(planOption.totalDays, daysLeft.coerceAtLeast(1)).coerceAtLeast(1)
        val remainingChapters = (totalChapters - completedChapters).coerceAtLeast(0)
        val chaptersPerDay = if (remainingChapters == 0) 0 else {
            ceil(remainingChapters.toDouble() / effectivePlanDays.toDouble()).toInt().coerceAtLeast(1)
        }

        val totalTests = examAttempts.size
        val totalObt = examAttempts.sumOf { it.scoreObtained.toDouble() }
        val totalMax = examAttempts.sumOf { it.maxMarks.toDouble() }
        val mockAvg = if (totalMax > 0.0) ((totalObt / totalMax) * 100.0).roundToInt().coerceIn(0, 100) else 0

        val (motTitle, motMsg) = when {
            syllabusPct >= 80 && mockAvg >= 80 ->
                "🏆 টপার লেভেল প্রস্তুতি!" to "অসাধারণ! তোমার সিলেবাস ও মক স্কোর দুটোই দুর্দান্ত—এখন প্রতিদিন VVI রিভিশন আর টাইমড মক চালিয়ে যাও!"
            syllabusPct >= 45 ->
                "🚀 দারুণ গতিতে এগোচ্ছো!" to "মাধ্যমিক $targetExamYear-এর জন্য প্রতিদিন মাত্র ${chaptersPerDay}টি VVI অধ্যায় শেষ করলেই ${planOption.labelBn}-এর আগেই ১০০% প্রস্তুতি পাকা!"
            totalTests > 0 ->
                "🔥 মক টেস্ট শুরু করে ফেলেছো!" to "তোমার গড় স্কোর $mockAvg%—উপরের হাই-ওয়েটেজ VVI অধ্যায়গুলো টিক দিয়ে প্রতিদিন ${chaptersPerDay}টি করে শেষ করো!"
            else ->
                "🌟 আজই সেরা শুরু হোক!" to "হাতে এখনো $daysLeft দিন বাকি! নিচের 🔥 হাই-ওয়েটেজ VVI অধ্যায় থেকে শুরু করলে ${planOption.labelBn}-এ সহজেই পুরো সিলেবাস জয় করতে পারবে।"
        }

        return FastLearningSelfReport(
            targetExamYear = targetExamYear,
            deviceDateTimeBn = formatCurrentDeviceDateTime(),
            daysRemainingToExam = daysLeft,
            planOption = planOption,
            completedChaptersCount = completedChapters,
            totalChaptersCount = totalChapters,
            syllabusCompletionPercent = syllabusPct,
            recommendedChaptersPerDay = chaptersPerDay,
            mockTestsTaken = totalTests,
            mockAveragePercent = mockAvg,
            motivationTitleBn = motTitle,
            motivationMessageBn = motMsg
        )
    }
}
