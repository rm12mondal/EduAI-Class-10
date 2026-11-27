package com.example.model

import android.os.Build
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Automatically selected study plan computed strictly from `daysRemaining`
 * between Today's Date (device clock) and the chosen Madhyamik Exam Date.
 */
enum class AutoStudyPlan(
    val id: String,
    val labelBn: String,
    val shortBadgeBn: String,
    val descriptionBn: String,
    val aiStrategyEn: String,
    val defaultMode: DateStudyMode,
    val accentHex: Long
) {
    DEEP_STUDY(
        id = "deep_study",
        labelBn = "✨ স্মার্ট লার্নিং (Deep Study)",
        shortBadgeBn = "Deep Study",
        descriptionBn = "৯০+ দিন বাকি — প্রতিটি অধ্যায় সহজ বাংলায় গভীরভাবে বুঝে শক্ত ভিত তৈরি করো",
        aiStrategyEn = "Deep Study (>90 days left): Build strong conceptual foundations in simple Bengali with detailed explanations, examples, formulas, and steady chapter mastery.",
        defaultMode = DateStudyMode.SMART_LEARNING,
        accentHex = 0xFF2563EB
    ),
    CRASH_COURSE(
        id = "crash_course",
        labelBn = "⏳ ২/৩ মাসে সিলেবাস শেষ (Crash Course)",
        shortBadgeBn = "Crash Course",
        descriptionBn = "৪৫–৯০ দিন বাকি — দৈনিক অধ্যায় টার্গেট ও টাইম-বক্সড ক্র্যাশ কোর্সে সিলেবাস শেষ করো",
        aiStrategyEn = "Crash Course (45–90 days left): Provide strict daily chapter targets, prioritized syllabus roadmaps, time-boxed checklists, and high-yield board summaries.",
        defaultMode = DateStudyMode.SYLLABUS_PLAN,
        accentHex = 0xFF059669
    ),
    HIGH_WEIGHTAGE_VVI(
        id = "high_weightage_vvi",
        labelBn = "🚀 ফাস্ট লার্নিং (High-Weightage VVI)",
        shortBadgeBn = "High-Weightage VVI",
        descriptionBn = "১৫–৪৪ দিন বাকি — শুধু হাই-ওয়েটেজ VVI অধ্যায়, মূল সূত্র ও ফাস্ট মাইক্রো-সামারি পড়ো",
        aiStrategyEn = "High-Weightage VVI Fast Learning (15–44 days left): Focus strictly on maximum-marks VVI topics, crisp 3-bullet micro-concept summaries, separate-line formulas, and rapid memory tricks.",
        defaultMode = DateStudyMode.FAST_LEARNING,
        accentHex = 0xFF7C3AED
    ),
    FINAL_REVISION(
        id = "final_revision",
        labelBn = "⚡ ব্রহ্মাস্ত্র ও মক পরীক্ষা (Final Revision)",
        shortBadgeBn = "Final Revision",
        descriptionBn = "১৫ দিনের কম বাকি — ১০০% কমন ব্রহ্মাস্ত্র সাজেশন, লাস্ট-মিনিট সূত্র ও টাইমড মক টেস্ট",
        aiStrategyEn = "Final Revision (<15 days left): Deliver last-minute Brahmastram sure-shot board suggestions, exact topper keywords, separate-line formula sheets, and rapid mock exam drills.",
        defaultMode = DateStudyMode.BRAHMASTRAM,
        accentHex = 0xFFDC2626
    );

    val accentColor: Color
        get() = Color(accentHex)

    companion object {
        fun fromDaysRemaining(daysRemaining: Int): AutoStudyPlan = when {
            daysRemaining > 90 -> DEEP_STUDY
            daysRemaining in 45..90 -> CRASH_COURSE
            daysRemaining in 15..44 -> HIGH_WEIGHTAGE_VVI
            else -> FINAL_REVISION
        }
    }
}

/**
 * All 5 major study modes accessible inside the single All-In-One Auto-Paced Start Workspace.
 */
enum class DateStudyMode(
    val id: String,
    val labelBn: String,
    val shortHeaderBn: String,
    val subtitleBn: String,
    val oneTapActionLabelBn: String,
    val accentHex: Long
) {
    SMART_LEARNING(
        id = "smart_learning",
        labelBn = "✨ স্মার্ট লার্নিং",
        shortHeaderBn = "✨ স্মার্ট লার্নিং মোড",
        subtitleBn = "সহজ বাংলায় গল্পের মতো ব্যাখ্যা ও কনসেপ্ট ক্লিয়ার",
        oneTapActionLabelBn = "✨ আজকের টপিক সহজ বাংলায় বুঝিয়ে দাও",
        accentHex = 0xFF2563EB
    ),
    FAST_LEARNING(
        id = "fast_learning",
        labelBn = "🚀 ফাস্ট লার্নিং",
        shortHeaderBn = "🚀 ফাস্ট লার্নিং মোড",
        subtitleBn = "কম সময়ে VVI মাইক্রো-কনসেপ্ট সামারি ও মূল সূত্র",
        oneTapActionLabelBn = "🚀 আজকের VVI ফাস্ট মাইক্রো-সামারি দাও",
        accentHex = 0xFF7C3AED
    ),
    SYLLABUS_PLAN(
        id = "syllabus_plan",
        labelBn = "⏳ ২/৩ মাসে সিলেবাস শেষ",
        shortHeaderBn = "⏳ ক্র্যাশ কোর্স রোডম্যাপ",
        subtitleBn = "বাকি দিন অনুযায়ী দৈনিক সিলেবাস টার্গেট ও চেকলিস্ট",
        oneTapActionLabelBn = "⏳ বাকি দিন অনুযায়ী আজকের ডেইলি রুটিন বানাও",
        accentHex = 0xFF059669
    ),
    BRAHMASTRAM(
        id = "brahmastram",
        labelBn = "⚡ ব্রহ্মাস্ত্র",
        shortHeaderBn = "⚡ ব্রহ্মাস্ত্র শিওর-শট",
        subtitleBn = "১০০% সম্ভাব্য বোর্ড সাজেশন, সূত্র ও টপার টিপস",
        oneTapActionLabelBn = "⚡ আজকের ব্রহ্মাস্ত্র শিওর-শট সাজেশন দাও",
        accentHex = 0xFFDC2626
    ),
    MOCK_EXAM(
        id = "mock_exam",
        labelBn = "📝 পরীক্ষা (Mock Exam)",
        shortHeaderBn = "📝 মক এক্সাম (পরীক্ষা)",
        subtitleBn = "তাৎক্ষণিক প্রশ্নপত্র, উত্তর যাচাই ও বোর্ড নম্বর মূল্যায়ন",
        oneTapActionLabelBn = "📝 আজকের ১৫ নম্বরের মক টেস্ট শুরু করো",
        accentHex = 0xFFD97706
    );

    val accentColor: Color
        get() = Color(accentHex)
}

/**
 * Date-wise logged activity summary shown in the Date-Wise History sheet.
 */
data class DateStudyHistoryLogItem(
    val dateIso: String,
    val displayDateBn: String,
    val messageCount: Int,
    val lastTopic: String,
    val lastModeLabel: String,
    val lastTimestampMs: Long
)

data class DateStudyHubUiState(
    val isDatePickerDialogOpen: Boolean = false,
    val isWorkspaceOpen: Boolean = false,
    val examDateEpochMs: Long = DateStudyDateUtils.defaultExamDateUtcMs(),
    val examDateIso: String = DateStudyDateUtils.utcMillisToIso(DateStudyDateUtils.defaultExamDateUtcMs()),
    val daysRemaining: Int = DateStudyDateUtils.computeDaysRemaining(DateStudyDateUtils.defaultExamDateUtcMs()),
    val autoPlan: AutoStudyPlan = AutoStudyPlan.fromDaysRemaining(
        DateStudyDateUtils.computeDaysRemaining(DateStudyDateUtils.defaultExamDateUtcMs())
    ),
    val selectedDateIso: String = DateStudyDateUtils.todayIso(),
    val selectedMode: DateStudyMode = AutoStudyPlan.fromDaysRemaining(
        DateStudyDateUtils.computeDaysRemaining(DateStudyDateUtils.defaultExamDateUtcMs())
    ).defaultMode,
    val selectedSubject: WbbseSubject? = null,
    val historyLogs: List<DateStudyHistoryLogItem> = emptyList(),
    val showHistorySheet: Boolean = false
)

object DateStudyDateUtils {
    private val utcZone: TimeZone = TimeZone.getTimeZone("UTC")
    private const val ONE_DAY_MS: Long = 24L * 60L * 60L * 1000L

    fun todayUtcMidnightMs(): Long {
        val local = Calendar.getInstance()
        val utc = Calendar.getInstance(utcZone).apply {
            clear()
            set(
                local.get(Calendar.YEAR),
                local.get(Calendar.MONTH),
                local.get(Calendar.DAY_OF_MONTH),
                0, 0, 0
            )
            set(Calendar.MILLISECOND, 0)
        }
        return utc.timeInMillis
    }

    fun offsetDaysUtcMidnightMs(daysFromToday: Int): Long {
        return todayUtcMidnightMs() + daysFromToday.toLong() * ONE_DAY_MS
    }

    /**
     * Defaults to the upcoming WBBSE Madhyamik February 10 date if it falls within
     * 1..365 days from today, otherwise defaults to +60 days from today.
     */
    fun defaultExamDateUtcMs(): Long {
        val local = Calendar.getInstance()
        val currentYear = local.get(Calendar.YEAR)
        val feb10ThisYear = Calendar.getInstance(utcZone).apply {
            clear()
            set(currentYear, Calendar.FEBRUARY, 10, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val feb10NextYear = Calendar.getInstance(utcZone).apply {
            clear()
            set(currentYear + 1, Calendar.FEBRUARY, 10, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val today = todayUtcMidnightMs()
        val candidate = if (feb10ThisYear > today) feb10ThisYear else feb10NextYear
        val days = computeDaysRemaining(candidate)
        return if (days in 1..365) candidate else offsetDaysUtcMidnightMs(60)
    }

    /**
     * Automatically takes Today's Date from the device clock and computes
     * `ChronoUnit.DAYS.between(today, examDate)` (with exact UTC-midnight fallback for API 24-25).
     */
    fun computeDaysRemaining(examDateUtcMs: Long): Int {
        val rawDays = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val today: LocalDate = LocalDate.now()
            val examDate: LocalDate = Instant.ofEpochMilli(examDateUtcMs)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
            ChronoUnit.DAYS.between(today, examDate)
        } else {
            val todayMs = todayUtcMidnightMs()
            val normalizedExamMs = isoToUtcMillis(utcMillisToIso(examDateUtcMs))
            (normalizedExamMs - todayMs) / ONE_DAY_MS
        }
        return rawDays.toInt().coerceAtLeast(0)
    }

    fun todayIso(): String = utcMillisToIso(todayUtcMidnightMs())

    fun utcMillisToIso(utcMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = utcZone
        }
        return sdf.format(Date(utcMillis))
    }

    fun isoToUtcMillis(dateIso: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = utcZone
            }
            sdf.parse(dateIso)?.time ?: todayUtcMidnightMs()
        } catch (_: Exception) {
            todayUtcMidnightMs()
        }
    }

    fun formatExamDateReadable(dateIso: String): String {
        val millis = isoToUtcMillis(dateIso)
        val sdf = SimpleDateFormat("dd MMM yyyy (EEE)", Locale.US).apply {
            timeZone = utcZone
        }
        return sdf.format(Date(millis))
    }

    fun formatReadableDate(dateIso: String): String {
        val millis = isoToUtcMillis(dateIso)
        val sdf = SimpleDateFormat("dd MMM yyyy (EEE)", Locale.US).apply {
            timeZone = utcZone
        }
        val base = sdf.format(Date(millis))
        val today = todayIso()
        val yesterday = utcMillisToIso(offsetDaysUtcMidnightMs(-1))
        return when (dateIso) {
            today -> "$base • আজ"
            yesterday -> "$base • গতকাল"
            else -> base
        }
    }

    fun formatCompactDate(dateIso: String): String {
        val millis = isoToUtcMillis(dateIso)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US).apply {
            timeZone = utcZone
        }
        return sdf.format(Date(millis))
    }

    fun buildStarterPrompts(
        mode: DateStudyMode,
        subject: WbbseSubject?,
        autoPlan: AutoStudyPlan,
        daysRemaining: Int,
        examDateIso: String
    ): List<String> {
        val subjName = subject?.bengaliName ?: "WBBSE সব বিষয়"
        val examLabel = formatCompactDate(examDateIso)
        return when (mode) {
            DateStudyMode.SMART_LEARNING -> listOf(
                "পরীক্ষার $daysRemaining দিন বাকি ($examLabel)—$subjName-এর সবচেয়ে জরুরি টপিকটি সহজ বাংলায় বাস্তব উদাহরণসহ বুঝিয়ে দাও।",
                "$subjName-এর মূল সূত্র ও কনসেপ্টগুলো ধাপে ধাপে গভীরভাবে ব্যাখ্যা করো।"
            )
            DateStudyMode.FAST_LEARNING -> listOf(
                "পরীক্ষার আর মাত্র $daysRemaining দিন বাকি—$subjName-এর হাই-ওয়েটেজ VVI অধ্যায়ের ফাস্ট মাইক্রো-কনসেপ্ট সামারি দাও।",
                "$daysRemaining দিনের প্রস্তুতির জন্য $subjName-এর ১০ মিনিটের দ্রুত রিভিশন সূত্র ও পয়েন্ট দাও।"
            )
            DateStudyMode.SYLLABUS_PLAN -> listOf(
                "মাধ্যমিক পরীক্ষার ($examLabel) আর $daysRemaining দিন বাকি—${autoPlan.shortBadgeBn} অনুযায়ী $subjName-এর আজকের স্টাডি টার্গেট ও ডেইলি রুটিন বানিয়ে দাও।",
                "এই $daysRemaining দিনে $subjName-এর কোন কোন VVI অধ্যায় আগে শেষ করা উচিত?"
            )
            DateStudyMode.BRAHMASTRAM -> listOf(
                "পরীক্ষার $daysRemaining দিন আগে $subjName-এর সেরা ৫টি ব্রহ্মাস্ত্র শিওর-শট প্রশ্ন ও উত্তর লেখার কৌশল দাও।",
                "$subjName-এর শেষ মুহূর্তের কমন সাজেশন ও গুরুত্বপূর্ণ সূত্রগুলো আলাদা লাইনে সাজিয়ে দাও।"
            )
            DateStudyMode.MOCK_EXAM -> listOf(
                "পরীক্ষার $daysRemaining দিন বাকি—$subjName-এর ওপর ১৫ নম্বরের (MCQ + সংক্ষিপ্ত + ২/৩ নম্বরের) মক টেস্ট প্রশ্নপত্র দাও।",
                "$subjName-এর ৫টি বোর্ড স্ট্যান্ডার্ড MCQ প্রশ্ন করো, আমি উত্তর দিলে মূল্যায়ন করে নম্বর দেবে।"
            )
        }
    }

    fun buildOneTapPrompt(
        mode: DateStudyMode,
        subject: WbbseSubject?,
        autoPlan: AutoStudyPlan,
        daysRemaining: Int,
        examDateIso: String
    ): String {
        val subjName = subject?.bengaliName ?: "WBBSE ক্লাস ১০ (সব ৭টি বিষয়)"
        val examLabel = formatCompactDate(examDateIso)
        return when (mode) {
            DateStudyMode.SMART_LEARNING ->
                "মাধ্যমিক পরীক্ষার ($examLabel) আর $daysRemaining দিন বাকি | অটো প্ল্যান: ${autoPlan.labelBn}। $subjName-এর একটি অত্যন্ত গুরুত্বপূর্ণ WBBSE অধ্যায় বেছে নিয়ে সহজ বাংলায়, ধাপে ধাপে বাস্তব উদাহরণ ও আলাদা লাইনে সূত্রসহ বুঝিয়ে দাও।"
            DateStudyMode.FAST_LEARNING ->
                "মাধ্যমিক পরীক্ষার ($examLabel) আর মাত্র $daysRemaining দিন বাকি | অটো প্ল্যান: ${autoPlan.labelBn}। $subjName-এর হাই-ওয়েটেজ VVI টপিকের সুপার-ফাস্ট মাইক্রো-কনসেপ্ট সামারি, আলাদা লাইনে মূল সূত্র ও ১০-সেকেন্ড মেমোরি ট্রিক দাও।"
            DateStudyMode.SYLLABUS_PLAN ->
                "মাধ্যমিক পরীক্ষা: $examLabel (আর ঠিক $daysRemaining দিন বাকি) | অটো প্ল্যান: ${autoPlan.labelBn} | বিষয়: $subjName — এই ঠিক $daysRemaining দিনের হিসেবে আজকের জন্য একটি নিখুঁত দৈনিক স্টাডি রুটিন, অধ্যায় টার্গেট, সময় বিভাজন এবং দিনের শেষে রিভিশন চেকলিস্ট বানিয়ে দাও।"
            DateStudyMode.BRAHMASTRAM ->
                "মাধ্যমিক পরীক্ষা: $examLabel (আর $daysRemaining দিন বাকি) | অটো প্ল্যান: ${autoPlan.labelBn}। $subjName-এর টপ ৫টি ব্রহ্মাস্ত্র শিওর-শট বোর্ড সাজেশন, প্রতিটি প্রশ্নের নম্বর-পাওয়ার মূল পয়েন্ট, আলাদা লাইনে সূত্র ও টপার রাইটিং টিপস দাও।"
            DateStudyMode.MOCK_EXAM ->
                "মাধ্যমিক পরীক্ষা: $examLabel (আর $daysRemaining দিন বাকি) | অটো প্ল্যান: ${autoPlan.labelBn}। $subjName-এর ওপর WBBSE বোর্ড প্যাটার্নে ১৫ নম্বরের একটি লাইভ মক টেস্ট প্রশ্নপত্র দাও (৫টি MCQ + ৫টি ১-নম্বরের প্রশ্ন + ১টি ৫-নম্বরের প্রশ্ন)। উত্তর এখনই দিও না—আমি উত্তর টাইপ করে বা খাতার ছবি তুলে পাঠালে তুমি মার্কিং করে নম্বর দেবে।"
        }
    }
}
