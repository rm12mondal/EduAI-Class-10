package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Science
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.SubjectBengaliColor
import com.example.ui.theme.SubjectEnglishColor
import com.example.ui.theme.SubjectGeographyColor
import com.example.ui.theme.SubjectHistoryColor
import com.example.ui.theme.SubjectLifeSciColor
import com.example.ui.theme.SubjectMathColor
import com.example.ui.theme.SubjectPhysicalSciColor

/**
 * The 7 compulsory subjects of the West Bengal Board of Secondary Education (WBBSE) Class 10
 * (Madhyamik Pariksha) curriculum.
 */
enum class WbbseSubject(
    val id: String,
    val bengaliName: String,
    val englishName: String,
    val shortCode: String,
    val accentColor: Color,
    val icon: ImageVector,
    val sampleQuestion: String,
    val descriptionBn: String
) {
    BENGALI(
        id = "bengali",
        bengaliName = "বাংলা",
        englishName = "Bengali (1st Lang)",
        shortCode = "BNG",
        accentColor = SubjectBengaliColor,
        icon = Icons.Rounded.AutoStories,
        sampleQuestion = "জ্ঞানচক্ষু গল্পে তপনের চরিত্র ও কারক-বিভক্তির সংজ্ঞা ব্যাখ্যা করো",
        descriptionBn = "সাহিত্য সঞ্চয়ন, কোনি ও বাংলা ব্যাকরণ"
    ),
    ENGLISH(
        id = "english",
        bengaliName = "ইংরেজি",
        englishName = "English (2nd Lang)",
        shortCode = "ENG",
        accentColor = SubjectEnglishColor,
        icon = Icons.Rounded.MenuBook,
        sampleQuestion = "Summarize Father's Help and explain Voice Change rules for Madhyamik",
        descriptionBn = "Bliss (Prose & Poetry), Grammar & Writing"
    ),
    MATHEMATICS(
        id = "mathematics",
        bengaliName = "গণিত",
        englishName = "Mathematics",
        shortCode = "MTH",
        accentColor = SubjectMathColor,
        icon = Icons.Rounded.Calculate,
        sampleQuestion = "শ্রীধর আচার্যের সূত্র ও পিথাগোরাসের উপপাদ্য লেখো",
        descriptionBn = "পাটিগণিত, বীজগণিত, জ্যামিতি, ত্রিকোণমিতি ও পরিমিতি"
    ),
    PHYSICAL_SCIENCE(
        id = "physical_science",
        bengaliName = "ভৌত বিজ্ঞান",
        englishName = "Physical Science",
        shortCode = "PSC",
        accentColor = SubjectPhysicalSciColor,
        icon = Icons.Rounded.Science,
        sampleQuestion = "ওহমের সূত্র, বয়েলের সূত্র এবং স্নেলের সূত্র ব্যাখ্যা করো",
        descriptionBn = "গ্যাসের আচরণ, আলো, চলতড়িৎ, পর্যায় সারণি ও রসায়ন"
    ),
    LIFE_SCIENCE(
        id = "life_science",
        bengaliName = "জীবন বিজ্ঞান",
        englishName = "Life Science",
        shortCode = "LSC",
        accentColor = SubjectLifeSciColor,
        icon = Icons.Rounded.Biotech,
        sampleQuestion = "মেন্ডেলের পৃথকীভবন সূত্র ও অক্সিন হরমোনের কাজ কী?",
        descriptionBn = "নিয়ন্ত্রণ ও সমন্বয়, কোষ বিভাজন, বংশগতি ও পরিবেশ"
    ),
    HISTORY(
        id = "history",
        bengaliName = "ইতিহাস",
        englishName = "History",
        shortCode = "HIS",
        accentColor = SubjectHistoryColor,
        icon = Icons.Rounded.HistoryEdu,
        sampleQuestion = "সাঁওতাল বিদ্রোহ ও নীল বিদ্রোহের প্রধান কারণ কী ছিল?",
        descriptionBn = "আধুনিক ভারতের ইতিহাস, সংস্কার ও স্বাধীনতা আন্দোলন"
    ),
    GEOGRAPHY(
        id = "geography",
        bengaliName = "ভূগোল",
        englishName = "Geography",
        shortCode = "GEO",
        accentColor = SubjectGeographyColor,
        icon = Icons.Rounded.Public,
        sampleQuestion = "নদীর ক্ষয়কার্যে সৃষ্ট ভূমিরূপ ও ভারতের মৌসুমি বায়ুর বৈশিষ্ট্য কী?",
        descriptionBn = "বহির্জাত প্রক্রিয়া, বায়ুমণ্ডল, বারিমণ্ডল ও ভারত"
    );

    companion object {
        fun fromId(id: String?): WbbseSubject? = entries.firstOrNull { it.id == id }
    }
}
