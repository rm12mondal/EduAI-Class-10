package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * All 8 individual WBBSE Class 10 Syllabus Mastery tracks shown on the TOPPER Dashboard,
 * including a dedicated track for Koni & Bengali Grammar as required by the Madhyamik blueprint.
 */
enum class TopperSubjectTrack(
    val id: String,
    val bengaliName: String,
    val englishName: String,
    val linkedSubject: WbbseSubject,
    val totalRotationalSets: Int,
    val blueprintSummaryBn: String,
    val accentColor: Color,
    val icon: ImageVector
) {
    BENGALI(
        id = "topper_bengali",
        bengaliName = "বাংলা (গদ্য, পদ্য ও প্রবন্ধ)",
        englishName = "Bengali Literature & Essay",
        linkedSubject = WbbseSubject.BENGALI,
        totalRotationalSets = 8,
        blueprintSummaryBn = "বিভাগ ক MCQ (১৭) • খ VSAQ (১৯) • গ ৩/৫ নম্বর • প্রবন্ধ (১০) ও বঙ্গানুবাদ",
        accentColor = Color(0xFFC0392B),
        icon = Icons.AutoMirrored.Rounded.MenuBook
    ),
    KONI_GRAMMAR(
        id = "topper_koni_grammar",
        bengaliName = "কোনি ও বাংলা ব্যাকরণ",
        englishName = "Koni & Bengali Grammar",
        linkedSubject = WbbseSubject.BENGALI,
        totalRotationalSets = 6,
        blueprintSummaryBn = "কারক-সমাস-বাক্য-বাচ্য MCQ/SAQ (১৬) • কোনি ৫×২ = ১০ নম্বর • সংলাপ/প্রতিবেদন",
        accentColor = Color(0xFFD35400),
        icon = Icons.Rounded.AutoStories
    ),
    ENGLISH(
        id = "topper_english",
        bengaliName = "ইংরেজি (Text, Grammar & Writing)",
        englishName = "English (90 Marks)",
        linkedSubject = WbbseSubject.ENGLISH,
        totalRotationalSets = 8,
        blueprintSummaryBn = "Seen/Unseen MCQ & VSAQ • Grammar & Phrasal Verbs (20) • Writing Skills (30)",
        accentColor = Color(0xFF1F618D),
        icon = Icons.Rounded.Translate
    ),
    MATHEMATICS(
        id = "topper_math",
        bengaliName = "গণিত (পাটিগণিত, জ্যামিতি, ত্রিকোণমিতি)",
        englishName = "Mathematics (90 Marks)",
        linkedSubject = WbbseSubject.MATHEMATICS,
        totalRotationalSets = 8,
        blueprintSummaryBn = "MCQ (৬) • শূন্যস্থান/সত্য-মিথ্যা (১০) • ২ নম্বর (২০) • উপপাদ্য (৫) • সম্পাদ্য ও পরিমিতি",
        accentColor = Color(0xFF6C3483),
        icon = Icons.Rounded.Calculate
    ),
    PHYSICAL_SCIENCE(
        id = "topper_physical_science",
        bengaliName = "ভৌত বিজ্ঞান (পদার্থ ও রসায়ন)",
        englishName = "Physical Science (90 Marks)",
        linkedSubject = WbbseSubject.PHYSICAL_SCIENCE,
        totalRotationalSets = 8,
        blueprintSummaryBn = "বিভাগ ক MCQ (১৫) • খ VSAQ (২১) • গ ২ নম্বর (১৮) • ঘ ৩ নম্বর গাণিতিক ও ব্যাখ্যা (৩৬)",
        accentColor = Color(0xFF0E7490),
        icon = Icons.Rounded.Science
    ),
    LIFE_SCIENCE(
        id = "topper_life_science",
        bengaliName = "জীবন বিজ্ঞান (চিত্র ও বিশ্লেষণ)",
        englishName = "Life Science (90 Marks)",
        linkedSubject = WbbseSubject.LIFE_SCIENCE,
        totalRotationalSets = 8,
        blueprintSummaryBn = "বিভাগ ক MCQ (১৫) • খ VSAQ (২১) • গ ২ নম্বর (২৪) • ঘ ৫ নম্বর সচিত্র/বিশ্লেষণ (৩০)",
        accentColor = Color(0xFF1E8449),
        icon = Icons.Rounded.Biotech
    ),
    HISTORY(
        id = "topper_history",
        bengaliName = "ইতিহাস (৪ ও ৮ নম্বর ও মানচিত্র)",
        englishName = "History (90 Marks)",
        linkedSubject = WbbseSubject.HISTORY,
        totalRotationalSets = 8,
        blueprintSummaryBn = "বিভাগ ক MCQ (২০) • খ VSAQ ও ম্যাপ (১৬) • গ ২ নম্বর (২২) • ঘ ৪ নম্বর (২৪) • ঙ ৮ নম্বর (৮)",
        accentColor = Color(0xFFB9770E),
        icon = Icons.Rounded.HistoryEdu
    ),
    GEOGRAPHY(
        id = "topper_geography",
        bengaliName = "ভূগোল (প্রাকৃতিক, আঞ্চলিক ও ম্যাপ)",
        englishName = "Geography (90 Marks)",
        linkedSubject = WbbseSubject.GEOGRAPHY,
        totalRotationalSets = 8,
        blueprintSummaryBn = "বিভাগ ক MCQ (১৪) • খ VSAQ (২২) • গ ২ নম্বর (১২) • ঘ ৩ নম্বর (১২) • ঙ ৫ নম্বর (২০) • চ ম্যাপ (১০)",
        accentColor = Color(0xFF148F77),
        icon = Icons.Rounded.Public
    );

    companion object {
        fun fromId(id: String?): TopperSubjectTrack =
            entries.firstOrNull { it.id == id } ?: PHYSICAL_SCIENCE
    }
}

enum class TopperSectionType(
    val code: String,
    val bengaliTitle: String,
    val timingBucket: TimingBucket
) {
    SECTION_A_MCQ("A", "বিভাগ ‘ক’ — MCQ (বহুবিকল্প)", TimingBucket.MCQ),
    SECTION_B_VSAQ("B", "বিভাগ ‘খ’ — VSAQ / অতিসংক্ষিপ্ত (১ নম্বর)", TimingBucket.SAQ),
    SECTION_C_SAQ_2M("C", "বিভাগ ‘গ’ — বিশ্লেষণধর্মী SAQ (২ নম্বর)", TimingBucket.SAQ),
    SECTION_D_BROAD("D", "বিভাগ ‘ঘ’ — বড় প্রশ্ন (৩/৪/৫/৮ নম্বর)", TimingBucket.BROAD),
    SECTION_E_SPECIAL("E", "বিভাগ ‘ঙ’ — কোনি / Writing / ম্যাপ পয়েন্টিং", TimingBucket.BROAD)
}

enum class TimingBucket(val bengaliLabel: String, val targetBoardMinutes: Int) {
    MCQ("বিভাগ ক (MCQ)", 25),
    SAQ("বিভাগ খ ও গ (VSAQ / SAQ)", 55),
    BROAD("বিভাগ ঘ ও ঙ (বড় প্রশ্ন / ম্যাপ)", 100)
}

data class TopperQuestion(
    val id: String,
    val section: TopperSectionType,
    val marks: Int,
    val chapter: String,
    val questionText: String,
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = -1,
    val isTwisted: Boolean = false,
    val simpleMeaningBn: String = "",
    val modelAnswer: String,
    val presentationTip: String,
    val keyTerms: List<String> = emptyList()
)

data class TopperPaper(
    val setNumber: Int,
    val track: TopperSubjectTrack,
    val title: String,
    val blueprintSubtitle: String,
    val isTwistedChallenge: Boolean,
    val isCapsule15Min: Boolean,
    val isMistakeRecovery: Boolean,
    val fullMarks: Int,
    val durationSeconds: Long,
    val sourceBadge: String,
    val previousYearTrendSummary: String,
    val questions: List<TopperQuestion>
)

data class SectionTimeBreakdown(
    val mcqSeconds: Long = 0L,
    val saqSeconds: Long = 0L,
    val broadSeconds: Long = 0L
) {
    val totalSeconds: Long get() = mcqSeconds + saqSeconds + broadSeconds
}

data class TopperQuestionFeedback(
    val question: TopperQuestion,
    val marksAwarded: Float,
    val maxMarks: Float,
    val isCorrectOrStrong: Boolean,
    val studentSummary: String,
    val feedbackNote: String,
    val decoderNoteBn: String?,
    val presentationTip: String,
    val modelAnswer: String
)

data class TopperSubmissionReport(
    val paperTitle: String,
    val track: TopperSubjectTrack,
    val setNumber: Int,
    val obtainedMarks: Float,
    val totalMarks: Float,
    val readinessPercent: Int,
    val verdictTitle: String, // "তুমি কতটা ভালো করেছ"
    val strengths: String,
    val mistakesAndMissingKeywords: String,
    val improvementAdvice: String,
    val pacingAdviceBn: String,
    val timeBreakdown: SectionTimeBreakdown,
    val questionFeedbacks: List<TopperQuestionFeedback>,
    val evaluatorSource: String,
    val offlinePhotoWarning: String? = null
)

data class SubjectMasteryStatus(
    val track: TopperSubjectTrack,
    val completedSetsCount: Int,
    val totalSetsCount: Int,
    val coveredPercent: Int,
    val remainingSetsCount: Int,
    val pendingMistakesCount: Int
)
