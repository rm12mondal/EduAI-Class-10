package com.example.data.master

import com.example.data.local.TopicImportance
import com.example.model.BrahmastramStudyNote
import com.example.model.WbbseSubject

/**
 * 100% Offline Curated Brahmastram (Sure-Shot Suggestions) Bank:
 * 20 unique high-yield WBBSE Class 10 Madhyamik topics for EACH of the 7 subjects (140 total),
 * complete with exam-ready notes, board exam tips, topper writing hints, separate-line Unicode formulas,
 * and tailored WBBSE board MCQs.
 */
internal data class BrahmastramSeedNote(
    val chapter: String,
    val title: String,
    val marksWeight: Int,
    val noteBn: String,
    val boardTipBn: String,
    val keyFormulaOrFact: String,
    val examQuestion: String,
    val options: List<String>,
    val correctIndex: Int,
    val topperHintBn: String = ""
)

private typealias SeedNote = BrahmastramSeedNote

object BrahmastramOfflineBank {

    fun getCuratedNotes(
        subject: WbbseSubject,
        completedIds: Set<String>
    ): List<BrahmastramStudyNote> {
        val seeds = when (subject) {
            WbbseSubject.BENGALI -> bengaliNotes + BrahmastramExtraLanguageMathBank.bengaliExtraNotes
            WbbseSubject.ENGLISH -> englishNotes + BrahmastramExtraLanguageMathBank.englishExtraNotes
            WbbseSubject.MATHEMATICS -> mathNotes + BrahmastramExtraLanguageMathBank.mathExtraNotes
            WbbseSubject.PHYSICAL_SCIENCE -> physicalScienceNotes + BrahmastramExtraScienceSocialBank.physicalScienceExtraNotes
            WbbseSubject.LIFE_SCIENCE -> lifeScienceNotes + BrahmastramExtraScienceSocialBank.lifeScienceExtraNotes
            WbbseSubject.HISTORY -> historyNotes + BrahmastramExtraScienceSocialBank.historyExtraNotes
            WbbseSubject.GEOGRAPHY -> geographyNotes + BrahmastramExtraScienceSocialBank.geographyExtraNotes
        }

        return seeds.mapIndexed { idx, seed ->
            val id = "br_${subject.id}_${idx + 1}"
            val pyq = StrictWbbsePrecisionEngine.getPyqTrend(
                subjectId = subject.id,
                chapterName = seed.chapter,
                topicTitle = seed.title,
                importanceLevel = TopicImportance.VVI
            )
            val fullNote = MathUnicodeFormatter.format("${seed.noteBn}\n\n💡 বোর্ড টিপ: ${seed.boardTipBn}")
            val formattedFormula = MathUnicodeFormatter.format(seed.keyFormulaOrFact)
            val baseTopperStructure = StrictWbbsePrecisionEngine.formatTopperAnswerSheet(
                seed.noteBn,
                subject,
                seed.marksWeight
            )
            val fullTopperStructure = if (seed.topperHintBn.isNotBlank()) {
                MathUnicodeFormatter.format("🎯 টপারের খাতা হিন্ট: ${seed.topperHintBn}\n\n$baseTopperStructure")
            } else {
                MathUnicodeFormatter.format(baseTopperStructure)
            }

            BrahmastramStudyNote(
                id = id,
                chapter = seed.chapter,
                title = seed.title,
                marksWeight = seed.marksWeight,
                pyqBadge = "${pyq.yearsBadge} • ${pyq.probabilityBadge}",
                sureShotNoteBn = fullNote,
                topperWritingStructureBn = fullTopperStructure,
                keyFormulaOrFact = formattedFormula,
                examQuestionPrompt = MathUnicodeFormatter.format(seed.examQuestion),
                mcqOptions = seed.options.map { MathUnicodeFormatter.format(it) },
                correctOptionIndex = seed.correctIndex,
                isCompleted = id in completedIds
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 1. বাংলা (BENGALI) — 10 High-Yield WBBSE Class 10 Sure-Shot Topics
    // ═══════════════════════════════════════════════════════════════════════
    private val bengaliNotes = listOf(
        SeedNote(
            chapter = "গল্প: জ্ঞানচক্ষু (আশাপূর্ণা দেবী)",
            title = "তপনের ‘জ্ঞানচক্ষু’ উন্মীলন ও আত্মমর্যাদাবোধ",
            marksWeight = 5,
            noteBn = "তপনের ধারণা ছিল লেখকেরা কোনো দূরের জগতের মানুষ; কিন্তু নিজের ছোটোমাসির স্বামী (নতুন মেসো) যে একজন সাধারণ মানুষের মতোই আচরণ করেন তা দেখে তার প্রথম জ্ঞানচক্ষু খুলে যায়। পরে মেসোর মাধ্যমে ‘সন্ধ্যাতারা’ পত্রিকায় ‘প্রথম দিন’ গল্পটি ছাপা হলেও মেসো গল্পটি ‘কারেকশন’ করে সম্পূর্ণ বদলে দেওয়ায় তপন লজ্জায় ও দুঃখে ভেঙে পড়ে এবং সংকল্প করে—এরপর কখনো ছাপতে দিলে নিজের লেখা নিজে গিয়ে দেবে।",
            boardTipBn = "উত্তরে ‘কুমকুম’ গল্পগ্রন্থের উৎস, ‘সন্ধ্যাতারা’ পত্রিকা এবং ‘প্রথম দিন’ গল্পের নাম অবশ্যই উদ্ধৃতি চিহ্নের মধ্যে লিখবে।",
            keyFormulaOrFact = "লেখিকা: আশাপূর্ণা দেবী | উৎস: কুমকুম গল্পগ্রন্থ | পত্রিকা: সন্ধ্যাতারা | তপনের গল্প: ‘প্রথম দিন’",
            examQuestion = "‘জ্ঞানচক্ষু’ গল্পে তপনের লেখা গল্পটির নাম কী ছিল?",
            options = listOf("প্রথম দিন", "সন্ধ্যাতারা", "কুমকুম", "নতুন মেসো"),
            correctIndex = 0
        ),
        SeedNote(
            chapter = "গল্প: পথের দাবী (শরৎচন্দ্র চট্টোপাধ্যায়)",
            title = "গিরীশ মহাপাত্রের ছদ্মবেশ ও সব্যসাচী মল্লিকের দেশপ্রেম",
            marksWeight = 5,
            noteBn = "পুলিশ কর্তা নিমাইবাবুর সামনে দাঁড়ানো রোগা, বাহারি পোশাকধারী, গাঁজার কলকে ও লেবুর তেল মাখা গিরীশ মহাপাত্র আসলে বিপ্লবী সব্যসাচী মল্লিকের নিখুঁত ছদ্মবেশ। পুলিশের চোখে ধুলো দিয়ে নিজের আদর্শ ও বিপ্লবী কর্মকাণ্ড অক্ষুণ্ণ রাখার অসাধারণ বুদ্ধিমত্তা ও নির্ভীক মানসিকতাই এই চরিত্রকে অনন্য করেছে।",
            boardTipBn = "৩ বা ৫ নম্বরের প্রশ্নে গিরীশ মহাপাত্রের পোশাকের বিবরণ (রামধনু রঙের ফুলমোজা, সবুজ ফুলমোজা/চামড়ার চটি, পকেটে এক টাকা ও গাঁজার কলকে) পয়েন্ট করে লিখবে।",
            keyFormulaOrFact = "লেখক: শরৎচন্দ্র চট্টোপাধ্যায় | মূল চরিত্র: সব্যসাচী মল্লিক (গিরীশ মহাপাত্র) | অপূর্ব ও নিমাইবাবু | স্থান: রেঙ্গুন (বর্মা)",
            examQuestion = "‘পথের দাবী’ পাঠ্যাংশে নিম্নলিখিত কোন চরিত্রটি পুলিশ অফিসার নন?",
            options = listOf("নিমাইবাবু", "জগদীশবাবু", "অপূর্ব", "তেওয়ারি"),
            correctIndex = 2
        ),
        SeedNote(
            chapter = "গল্প: বহুরূপী (সুবোধ ঘোষ)",
            title = "হরিদার ‘বিরাগী’ ছদ্মবেশ ও নির্লোভ শিল্পীসত্তা",
            marksWeight = 5,
            noteBn = "অত্যন্ত দরিদ্র হয়েও হরিদা ঘড়ির কাঁটা ধরে অফিসের বাঁধাধরা কাজ করতে চাননি; বহুরূপী সেজেই তাঁর আনন্দ। জগদীশবাবুর বাড়িতে বিরাগী সেজে তিনি যখন ১০১ টাকার প্রণামী ফিরিয়ে দেন, তখন তাঁর খাঁটি শিল্পীসত্তার জয় হয়—অর্থলোভের কাছে তিনি নিজের ‘ঢং’ বা শিল্পকে নষ্ট করতে চাননি।",
            boardTipBn = "‘খাঁটি মানুষ তো নয়, এই বহুরূপীর জীবনের এর চেয়ে বেশি কিছু আশা করতে নেই’—উদ্ধৃতিটি ব্যাখ্যায় ব্যবহার করলে পূর্ণ নম্বর মেলে।",
            keyFormulaOrFact = "লেখক: সুবোধ ঘোষ | বিরাগীর ঝোলায় বই: গীতা | জগদীশবাবুর প্রণামী: ১০১ টাকা | হরিদার পূর্ব ছদ্মবেশ: পাগল, বাঈজি, কাপালিক, পুলিশ",
            examQuestion = "জগদীশবাবু বিরাগীবেশী হরিদাকে কত টাকা প্রণামী দিতে চেয়েছিলেন?",
            options = listOf("৫১ টাকা", "১০১ টাকা", "১১ টাকা", "৫০০ টাকা"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "কবিতা: অসুখী একজন (পাবলো নেরুদা)",
            title = "যুদ্ধের ধ্বংসলীলা বনাম ভালোবাসা ও অপেক্ষার চিরন্তনতা",
            marksWeight = 5,
            noteBn = "চিলির কবি পাবলো নেরুদার মূল কবিতার বঙ্গানুবাদ করেছেন নবারুণ ভট্টাচার্য। রক্তের এক আগ্নেয়পাহাড়ের মতো ভয়াবহ যুদ্ধ এসে শান্ত হলুদ দেবতাদের, মিষ্টি বাড়ি ও গোলাপি গাছকে চূর্ণবিচূর্ণ করে দেয়। কিন্তু এত ধ্বংসের মাঝেও দরজায় দাঁড়িয়ে থাকা অপেক্ষারত মেয়েটির মৃত্যু হয়নি—যা মানবিক প্রেম ও আশার অমরত্বের প্রতীক।",
            boardTipBn = "‘রক্তের এক আগ্নেয়পাহাড়ের মতো’ উপমাটি এবং কবিতার নামকরণের সার্থকতা (‘অসুখী একজন’) আলাদা অনুচ্ছেদে লিখবে।",
            keyFormulaOrFact = "মূল কবি: পাবলো নেরুদা | অনুবাদক: নবারুণ ভট্টাচার্য | কাব্যগ্রন্থ: বিদেশী ফুলে রক্তের ছিটে | প্রতীক: অপেক্ষারত মেয়ে = অমর ভালোবাসা",
            examQuestion = "‘অসুখী একজন’ কবিতাটি বাংলায় কে অনুবাদ করেছেন?",
            options = listOf("শঙ্খ ঘোষ", "নবারুণ ভট্টাচার্য", "সুবোধ ঘোষ", "শ্রীপান্থ"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "কবিতা: আফ্রিকা (রবীন্দ্রনাথ ঠাকুর)",
            title = "ঔপনিবেশিক বর্বরতা ও যুগান্তের কবির কাছে ক্ষমা প্রার্থনা",
            marksWeight = 5,
            noteBn = "‘পত্রপুট’ কাব্যগ্রন্থের অন্তর্গত ‘আফ্রিকা’ কবিতায় রবীন্দ্রনাথ প্রাচী ধরিত্রীর বুক থেকে ছিনিয়ে নেওয়া আফ্রিকার সৃষ্টি ও ইউরোপীয় সাম্রাজ্যবাদীদের নগ্ন লোভের নিন্দা করেছেন। ‘সভ্যের বর্বর লোভ’ আফ্রিকার চিরচিহ্নিত ইতিহাসে রক্ত ও অশ্রুর দাগ এঁকেছে; তাই কবি যুগান্তের কবিকে মানহারা মানবীর দ্বারে দাঁড়িয়ে ‘ক্ষমা করো’ বলার আহ্বান জানিয়েছেন।",
            boardTipBn = "‘হায় ছায়াবৃতা’ সম্বোধন ও ‘ক্ষমা করো’—এই দুটি মূল পঙ্‌ক্তি উদ্ধৃত করে সাম্রাজ্যবাদ-বিরোধী চেতনা তুলে ধরবে।",
            keyFormulaOrFact = "কবি: রবীন্দ্রনাথ ঠাকুর | উৎস: পত্রপুট কাব্যগ্রন্থ (রচনা: ১৯৩৭) | প্রেক্ষাপট: ইতালির আবিসিনিয়া আক্রমণ | শেষ বাণী: ‘ক্ষমা করো’",
            examQuestion = "রবীন্দ্রনাথ ঠাকুরের ‘আফ্রিকা’ কবিতাটি কোন কাব্যগ্রন্থের অন্তর্গত?",
            options = listOf("গীতাঞ্জলি", "পত্রপুট", "সঞ্চয়িতা", "খেয়া"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "কবিতা: অভিষেক (মাইকেল মধুসূদন দত্ত)",
            title = "মেঘনাদের বীরত্ব, স্বদেশপ্রেম ও সেনাপতি পদে অভিষেক",
            marksWeight = 5,
            noteBn = "‘মেঘনাদবধ কাব্য’-এর প্রথম সর্গ থেকে নেওয়া ‘অভিষেক’ অংশে প্রিয় অনুজ বীরবাহুর মৃত্যুসংবাদ শুনে প্রমোদকাননে থাকা ইন্দ্রজিৎ ক্ষোভে ফুলমালা ছিঁড়ে ফেলেন। তিনি পিতা রাবণকে যুদ্ধে যেতে বাধা দিয়ে নিজেই লঙ্কার সেনাপতি পদে অভিষিক্ত হন—এখানে তাঁর পিতৃভক্তি, ভ্রাতৃস্নেহ ও অপরিসীম দেশপ্রেম ফুটে উঠেছে।",
            boardTipBn = "‘হায়, বিধি বাম মম প্রতি’ এবং প্রমীলার প্রতি মেঘনাদের স্নেহময় বিদায়—দুটি দিক সংক্ষেপে উল্লেখ করবে।",
            keyFormulaOrFact = "কবি: মাইকেল মধুসূদন দত্ত | উৎস: মেঘনাদবধ কাব্য (প্রথম সর্গ) | ছন্দ: অমিত্রাক্ষর ছন্দ | অভিষেককারী: রাবণ (গঙ্গোদকে)",
            examQuestion = "‘অভিষেক’ কাব্যাংশটি ‘মেঘনাদবধ কাব্য’-এর কোন সর্গ থেকে সংকলিত?",
            options = listOf("প্রথম সর্গ", "দ্বিতীয় সর্গ", "তৃতীয় সর্গ", "নবম সর্গ"),
            correctIndex = 0
        ),
        SeedNote(
            chapter = "প্রবন্ধ: হারিয়ে যাওয়া কালি কলম (শ্রীপান্থ)",
            title = "কলমের বিবর্তন, ফাউন্টেন পেনের জন্ম ও লেখকের স্মৃতিকথা",
            marksWeight = 5,
            noteBn = "শ্রীপান্থ (নিখিল সরকার) তাঁর প্রবন্ধে খাগের কলম, বাঁশের কঞ্চি, পালকের কলম (কুইল) থেকে লুইস এডসন ওয়াটারম্যানের ফাউন্টেন পেন আবিষ্কারের ইতিহাস বর্ণনা করেছেন। কম্পিউটারের যুগেও কালি-কলমের প্রতি বাঙালি লেখকদের আবেগ (যেমন সত্যজিৎ রায়, শৈলজানন্দ, অন্নদাশঙ্কর রায়) ও ‘আঘাতেও মরে না কলম’ ভাবনা এখানে প্রাণবন্ত।",
            boardTipBn = "‘লাঠি তোমার দিন ফুরাইয়াছে’ (বঙ্কিমচন্দ্র) ও ‘কালি নেই কলম নেই বলে আমি মুনশি’—প্রবাদগুলো সঠিকভাবে উদ্ধৃত করবে।",
            keyFormulaOrFact = "লেখক: শ্রীপান্থ (নিখিল সরকার) | ফাউন্টেন পেন আবিষ্কারক: ওয়াটারম্যান | কালি তৈরির প্রবাদ: তিল ত্রিফলা শিমূল ছালা | ‘কলম’ শব্দ: গ্রিক/আরবি উৎস",
            examQuestion = "নিম্নলিখিত কোনটি ‘হারিয়ে যাওয়া কালি কলম’ প্রবন্ধে উল্লিখিত কলমের প্রাচীন নাম নয়?",
            options = listOf("কুইল", "স্টাইলাস", "খাগের কলম", "লেজার প্রিন্টার"),
            correctIndex = 3
        ),
        SeedNote(
            chapter = "সহায়ক পাঠ: কোনি (মতি নন্দী)",
            title = "ক্ষিতীশ সিংহের কোচিং আদর্শ ও ‘ফাইট কোনি ফাইট’",
            marksWeight = 5,
            noteBn = "জুপিটার ক্লাব থেকে বিতাড়িত হয়েও ক্ষিতীশ সিংহ দরিদ্র বস্তির মেয়ে কনকচাঁপা পাল ওরফে কোনিকে সাঁতারের চ্যাম্পিয়ন বানানোর ব্রত নেন। দারিদ্র্য, হরিচরণের ষড়যন্ত্র ও শারীরিক ক্লান্তি জয় করে মাদ্রাজে জাতীয় সাঁতার প্রতিযোগিতায় ‘ফাইট কোনি ফাইট’ মন্ত্রে কোনি বাংলাকে চ্যাম্পিয়ন করে।",
            boardTipBn = "‘কোনি’ থেকে দুটি ৫ নম্বরের প্রশ্নের উত্তর দিতে হয় (১০ নম্বর)। ক্ষিতীশের ডায়লগ (‘যন্ত্রণাটাকে হজম কর’) ও কোনির পারিবারিক সংগ্রাম অবশ্যই লিখবে।",
            keyFormulaOrFact = "লেখক: মতি নন্দী | কোনির পুরো নাম: কনকচাঁপা পাল | প্রশিক্ষক: ক্ষিতীশ সিংহ | ক্লাব: অ্যাপোলো (প্রতিদ্বন্দ্বী: জুপিটার) | মূল মন্ত্র: ফাইট কোনি ফাইট",
            examQuestion = "‘কোনি’ উপন্যাসে কোনির সম্পূর্ণ ভালো নাম কী ছিল?",
            options = listOf("কনকলতা পাল", "কনকচাঁপা পাল", "কমলা পাল", "কবিতা পাল"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "বাংলা ব্যাকরণ: কারক, সমাস, বাক্য ও বাচ্য",
            title = "কারক-বিভক্তি, সমাস ও বাচ্য পরিবর্তনের নিশ্চিত সূত্র (১৬ নম্বর)",
            marksWeight = 3,
            noteBn = "ক্রিয়াপদকে ‘কে/কারা’ (কর্তৃ), ‘কী/কাকে’ (কর্ম), ‘কী দ্বারা’ (করণ), ‘কোথা হতে’ (অপাদান) এবং ‘কোথায়/কখন’ (অধিকরণ) দিয়ে প্রশ্ন করে কারক নির্ণয় করতে হয়। ব্যাসবাক্য দেখে কর্মধারয় (বিশেষণ+বিশেষ্য/উপমান-উপমেয়), তৎপুরুষ (বিভক্তি লোপ), বহুব্রীহি (অন্য পদের অর্থ প্রধান) ও দ্বন্দ্ব (‘ও/এবং’) আলাদা করতে হয়।",
            boardTipBn = "সমাস নির্ণয়ের সময় আগে সঠিক ব্যাসবাক্য লিখবে, তারপরে ‘—’ চিহ্ন দিয়ে সমাসের পূর্ণ শ্রেণিনাম (যেমন: মধ্যপদলোপী কর্মধারয় সমাস) লিখবে।",
            keyFormulaOrFact = "কর্তৃ: কে | কর্ম: কী/কাকে | করণ: দ্বারা/দিয়ে | অপাদান: হইতে/থেকে | অধিকরণ: স্থানে/কালে | কর্মবাচ্যে কর্তায় ‘দ্বারা/কর্তৃক’ বিভক্তি",
            examQuestion = "ব্যাসবাক্যে পূর্বপদের বিভক্তি লোপ পেয়ে পরপদের অর্থ প্রধান হলে তাকে কোন সমাস বলে?",
            options = listOf("দ্বন্দ্ব সমাস", "তৎপুরুষ সমাস", "বহুব্রীহি সমাস", "দ্বিগু সমাস"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "বাংলা নির্মিতি: প্রতিবেদন, সংলাপ ও বঙ্গানুবাদ",
            title = "প্রতিবেদন-সংলাপ রচনা ও বঙ্গানুবাদে পূর্ণ নম্বর পাওয়ার কৌশল",
            marksWeight = 5,
            noteBn = "সংবাদপত্রের প্রতিবেদনে আকর্ষণীয় শিরোনাম, ‘নিজস্ব সংবাদদাতা, স্থান, তারিখ:’ দিয়ে শুরু করে নিরপেক্ষ ভাষায় মূল ঘটনা, কারণ, ক্ষয়ক্ষতি ও প্রতিকার ২-৩টি অনুচ্ছেদে লিখতে হয়। বঙ্গানুবাদে আক্ষরিক অনুবাদ না করে মূল ইংরেজি অনুচ্ছেদের ভাবটি প্রাঞ্জল সাধু বা চলিত বাংলায় (একই রীতিতে) অনুবাদ করতে হয়।",
            boardTipBn = "প্রতিবেদনে নিজের আসল নাম বা বিদ্যালয়ের নাম লিখবে না; ‘ক.খ.গ’ বা ‘নিজস্ব সংবাদদাতা’ লিখবে এবং গুরুচণ্ডালী দোষ এড়িয়ে চলবে।",
            keyFormulaOrFact = "প্রতিবেদন = শিরোনাম + নিজস্ব সংবাদদাতা, স্থান, তারিখ + ২-৩ অনুচ্ছেদ | বঙ্গানুবাদ = ৪ নম্বর (ভাবানুবাদ + সঠিক কাল)",
            examQuestion = "সংবাদপত্রের প্রতিবেদন রচনার ক্ষেত্রে নিম্নলিখিত কোন নিয়মটি সঠিক নয়?",
            options = listOf("উপরে সংক্ষিপ্ত শিরোনাম দেওয়া", "নিজস্ব সংবাদদাতা, স্থান ও তারিখ লেখা", "ব্যক্তিগত আবেগপূর্ণ কবিতা লেখা", "নিরপেক্ষ ও তথ্যভিত্তিক ভাষা ব্যবহার করা"),
            correctIndex = 2
        )
    )

    // ═══════════════════════════════════════════════════════════════════════
    // 2. ইংরেজি (ENGLISH) — 10 High-Yield WBBSE Class 10 Sure-Shot Topics
    // ═══════════════════════════════════════════════════════════════════════
    private val englishNotes = listOf(
        SeedNote(
            chapter = "Lesson 1: Father’s Help (R.K. Narayan)",
            title = "Swami’s Excuse, Father’s Letter & Samuel’s True Character",
            marksWeight = 5,
            noteBn = "সোমবার সকালে স্কুলে যাওয়া এড়াতে স্বামীনাথন মাথাব্যথার অজুহাত দেয় এবং শিক্ষক স্যামুয়েলকে ভীষণ রাগী বলে বর্ণনা করে। স্বামীর বাবা কঠোর হয়ে স্যামুয়েলের বিরুদ্ধে হেডমাস্টারকে চিঠি লেখেন। স্কুলে গিয়ে স্বামী দেখে স্যামুয়েল আসলে অত্যন্ত স্নেহশীল, ফলে তার বিবেক দংশন করে; শেষমেশ হেডমাস্টার ছুটিতে থাকায় চিঠি না দিয়ে ফিরে এলে বাবা চিঠিটি ছিঁড়ে ফেলেন।",
            boardTipBn = "Seen Passage-এর প্রশ্নে ‘Swaminathan felt he was the worst boy on earth’ এবং স্যামুয়েলের দয়ালু আচরণের কারণ অবশ্যই টেক্সটের সঠিক Tense মেনে লিখবে।",
            keyFormulaOrFact = "Author: R.K. Narayan (Malgudi Days) | Teacher: Samuel | Excuse: Headache on Monday at 9:00 AM | Headmaster: On leave for 1 week",
            examQuestion = "In ‘Father’s Help’, why could Swami not deliver the letter to the Headmaster?",
            options = listOf("Swami lost the letter on the road", "The Headmaster was on leave for a week", "Samuel took the letter away", "The school was closed for rain"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "Lesson 2: Fable (Ralph Waldo Emerson)",
            title = "Mountain vs Squirrel — ‘Talents Differ’ Central Idea",
            marksWeight = 3,
            noteBn = "পাহাড় অহংকার করে কাঠবিড়ালিকে ‘Little Prig’ বলে উপহাস করে। উত্তরে কাঠবিড়ালি (Bun) শান্তভাবে জানায় যে পৃথিবী গঠনে ছোটো-বড়ো সব উপাদানেরই প্রয়োজন আছে; পাহাড় যেমন পিঠে অরণ্য ধারণ করতে পারে কিন্তু বাদাম ভাঙতে পারে না, তেমনি কাঠবিড়ালিরও নিজস্ব দক্ষতা রয়েছে। প্রত্যেকের প্রতিভা ভিন্ন এবং সমান গুরুত্বপূর্ণ।",
            boardTipBn = "‘Bun’ মানে কাঠবিড়ালি এবং ‘Spry’ মানে চটপটে/চঞ্চল—MCQ ও True/False-এ প্রায় প্রতি বছর আসে।",
            keyFormulaOrFact = "Poet: Ralph Waldo Emerson | Squirrel’s nickname: Bun | Mountain’s taunt: ‘Little Prig’ | Moral: Talents differ; all is well and wisely put",
            examQuestion = "In the poem ‘Fable’, what can the squirrel do that the mountain cannot?",
            options = listOf("Carry forests on its back", "Crack a nut", "Make a squirrel track", "Hold the sky"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "Lesson 3: The Passing Away of Bapu (Nayantara Sehgal)",
            title = "Gandhiji’s Assassination, Funeral March & Awakening of India",
            marksWeight = 5,
            noteBn = "১৯৪৮ সালের ৩০ জানুয়ারি সন্ধ্যায় বিড়লা হাউসে প্রার্থনাসভায় যাওয়ার পথে গান্ধীজি গুলিবিদ্ধ হন। হাজার হাজার শোকাহত মানুষ পদ্মার মতো নীরবে শোকমিছিলে হাঁটে। লেখিকা উপলব্ধি করেন, বাপুর সঙ্গে হাঁটা কেবল সাধারণ চলা নয়—তা ছিল ধীরগতিতে চিন্তা ও স্পষ্ট দৃষ্টি লাভের প্রতীক। বাপুর চিতাভস্ম এলাহাবাদে বিসর্জনের পরও তাঁর আদর্শ ভারতবাসীর মনে অমর।",
            boardTipBn = "‘To walk is to make slow progress’ এবং বাপুর মৃত্যুর তারিখ (30th January, 1948) ও স্থান (Birla House, Allahabad) মুখস্থ রাখবে।",
            keyFormulaOrFact = "Author: Nayantara Sehgal | Date: 30 January 1948 | Place: Birla House | Ashes immersed: Allahabad (Ganga) | ‘Bapu’ = Father",
            examQuestion = "Where were Gandhiji’s ashes taken by a special train to be immersed in the Ganga?",
            options = listOf("Varanasi", "Allahabad", "Haridwar", "Kolkata"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "Lesson 4: My Own True Family (Ted Hughes)",
            title = "The Oak-Trees’ Dream Oath & Ecological Message",
            marksWeight = 3,
            noteBn = "একটি হরিণের খোঁজে ওকবনে ঢুকে কবি এক বৃদ্ধার দেখা পান, যাঁর ছোট্ট ব্যাগের ভেতর কবির গোপন কথা লুকানো ছিল। স্বপ্নে ওকগাছেরা কবিকে ঘিরে ধরে বলে—তারা কবির আসল পরিবার; একটি ওকগাছ কাটা হলে কবিকে দুটি গাছ লাগাতে হবে, নচেৎ কালো ওকবাকল কবিকে জড়িয়ে ধরবে। বন থেকে বেরিয়ে কবির হৃদয় বৃক্ষপ্রেমে বদলে যায়।",
            boardTipBn = "কবিতার মূল বার্তা—বৃক্ষরোপণ ও প্রকৃতির সঙ্গে মানুষের অবিচ্ছেদ্য বন্ধন (‘My walk was the walk of a human child, but my heart was a tree’)।",
            keyFormulaOrFact = "Poet: Ted Hughes | Search: A stag in an oakwood | Oath: Plant 2 oak trees whenever 1 is felled | Theme: Conservation of trees",
            examQuestion = "In ‘My Own True Family’, what promise did the oak-trees force the poet to make?",
            options = listOf("Never enter the forest again", "Plant two oak trees whenever he sees one felled", "Hunt no more stags", "Live inside the oakwood forever"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "Lesson 5: Our Runaway Kite (Lucy Maud Montgomery)",
            title = "Big Half Moon Island & The Reunion Through the Patched Kite",
            marksWeight = 5,
            noteBn = "বিগ হাফ মুন দ্বীপের লাইটহাউস রক্ষক বাবার সঙ্গে ন্যারেটর ফিলিপা ও তার ভাই ক্লড থাকত। ঘুড়ি ওড়ানোর আগে কনুই দিয়ে ফুটো হয়ে যাওয়ায় তারা ঘুড়িটি বাবার পুরোনো চিঠির কাগজ দিয়ে তালি দেয়। সেই ঘুড়িটি উড়ে মূল ভূখণ্ডে Aunt Esther-এর কাছে পৌঁছায়; চিঠিতে নিজের ভাইয়ের ও মায়ের নাম দেখে Aunt Esther, Mimi ও Dick-সহ বিচ্ছিন্ন পরিবার আবার মিলিত হয়।",
            boardTipBn = "কার নাম কী—Narrator: Philippa (11 yrs), Brother: Claude (12 yrs), Aunt: Esther, Cousins: Dick & Mimi—চার্ট করে মনে রাখবে।",
            keyFormulaOrFact = "Author: Lucy Maud Montgomery | Island: Big Half Moon | Kite patched with: An old letter | Cousins: Dick & Mimi | Aunt: Esther",
            examQuestion = "Which of the following characters is NOT a member of Philippa’s reunited family in ‘Our Runaway Kite’?",
            options = listOf("Claude", "Aunt Esther", "Mimi", "Samuel"),
            correctIndex = 3
        ),
        SeedNote(
            chapter = "Lesson 6: Sea Fever (John Masefield)",
            title = "The Poet’s Irresistible Longing for Sea Voyage & Freedom",
            marksWeight = 3,
            noteBn = "কবি জন মেসফিল্ড আবার সেই নির্জন সমুদ্র ও আকাশে ফিরে যেতে চান। তাঁর প্রয়োজন কেবল একটি লম্বা জাহাজ, পথ দেখানোর জন্য একটি তারা, সাদা মেঘ, বাতাসের তীক্ষ্ণ শিস, গাঙচিলের ডাক এবং ভ্রমণ শেষে এক সহযাত্রী নাবিকের কাছ থেকে আনন্দময় গল্প ও শান্ত ঘুম। ‘Sea Fever’ অর্থ সমুদ্রযাত্রার প্রবল উন্মাদনা।",
            boardTipBn = "‘And a tall ship and a star to steer her by’ ও ‘vagrant gypsy life’—এই শব্দবন্ধগুলোর অর্থ ও ইমেজারি খুব গুরুত্বপূর্ণ।",
            keyFormulaOrFact = "Poet: John Masefield | Needs: Tall ship + Star to steer | Sounds: Wind’s song, Sea-gulls crying | End wish: Quiet sleep & sweet dream",
            examQuestion = "In ‘Sea Fever’, what does the poet ask for to steer his tall ship?",
            options = listOf("A compass", "A star", "A lighthouse", "A map"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "Lesson 7: The Cat (Andrew Barton Paterson)",
            title = "Daytime Laziness vs Nighttime True Athletic Life of the Cat",
            marksWeight = 5,
            noteBn = "বেশিরভাগ মানুষ ভাবেন বিড়াল কেবল দুধ আর ইঁদুর পছন্দ করে এবং সারাদিন অলসভাবে ঘুমায়; কিন্তু লেখক দেখিয়েছেন বিড়াল আসলে দারুণ বুদ্ধিমান ও অ্যাথলেট। সন্ধ্যা নামলেই সে নিজের ‘true life’ শুরু করে—পেছনের বাগানে রাজা হয়ে সঙ্গীদের সঙ্গে ঘোরে। পরিবার বাড়ি বদলালেও বিড়াল মানুষ নয়, বরং তার পরিচিত বাড়ি ও জমি ছাড়তে চায় না।",
            boardTipBn = "বিড়াল কীভাবে অতিথিদের পায়ে ঘেঁষে মাছ আদায় করে এবং সন্ধ্যায় কীভাবে ‘King Arthur’s knights’-এর মতো যোদ্ধা হয়ে ওঠে—দুটি পয়েন্ট মনে রাখবে।",
            keyFormulaOrFact = "Author: Andrew Barton Paterson | Daytime: Pretends to sleep near fire | Nighttime: King of backyards | Loyalty: Attached to land, not just people",
            examQuestion = "According to ‘The Cat’, when does the cat truly show its real athletic life?",
            options = listOf("Early in the morning", "When evening falls", "During midday tea", "Only in winter"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "Lesson 8: The Snail (William Cowper)",
            title = "Self-Sufficient Life & Instant Retreat Inside the Shell",
            marksWeight = 3,
            noteBn = "শামুক ঘাস, পাতা, ফল বা দেওয়ালে এমনভাবে লেগে থাকে যেন সেখানেই তার জন্ম; পড়ে যাওয়ার ভয় তার নেই। কোনো বিপদ বা আবহাওয়ার পরিবর্তন টের পেলেই সে নিজের খোলসের (house) ভেতরে ঢুকে সম্পূর্ণ নিরাপদ বোধ করে। তার শুঁড়ে (horns) সামান্য ছোঁয়া লাগলেই সে বিরক্ত হয়ে গুটিয়ে যায় এবং একাই নিজের সম্পদ হয়ে সুখে বাস করে।",
            boardTipBn = "‘He is his own whole treasure’—কবিতার শেষ স্তবকের আত্মনির্ভরশীলতার ভাবার্থ ২ নম্বরের প্রশ্নে প্রায়ই আসে।",
            keyFormulaOrFact = "Poet: William Cowper | House: Its shell | Danger response: Shrinks inside with displeasure | Motto: Self-contained, needs no chattels",
            examQuestion = "In ‘The Snail’, what happens if you give the snail’s horns the slightest touch?",
            options = listOf("It moves faster", "It shrinks into its house with much displeasure", "It falls off the wall", "It searches for another snail"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "WBBSE Grammar: Phrasal Verbs, Voice & Narration",
            title = "Phrasal Verbs, Voice Change, Narration & Joining Rules (12 Marks)",
            marksWeight = 3,
            noteBn = "মাধ্যমিক গ্রামারে ৩ নম্বরের Phrasal Verb (যেমন: Give up = ত্যাগ করা, Put out = নেভানো, Call off = বাতিল করা, Look after = যত্ন নেওয়া, Bear with = সহ্য করা) নিশ্চিত আসে। এছাড়া Active→Passive Voice-এ Object সামনে আসে এবং মূল verb-এর Past Participle (V₃) বসে; Indirect Speech-এ Reporting Verb Past হলে Tense পরিবর্তন হয়।",
            boardTipBn = "Phrasal Verb লেখার সময় মূল শব্দের Tense অনুযায়ী verb-এর রূপ বদলাতে ভুলবে না (যেমন: ‘resembles’ → ‘takes after’, ‘extinguished’ → ‘put out’)।",
            keyFormulaOrFact = "Put out = Extinguish | Call off = Cancel | Give up = Abandon | Look down upon = Hate | Passive: Obj + be + V₃ + by + Sub",
            examQuestion = "Which Phrasal Verb means ‘to cancel’ (as used in WBBSE Grammar)?",
            options = listOf("Call on", "Call off", "Call up", "Call in"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "WBBSE Writing Skill (30 Marks)",
            title = "Box Notice, Newspaper Report, Editorial Letter & Process Writing",
            marksWeight = 5,
            noteBn = "Writing Skill-এ ৩টি প্রশ্ন × ১০ নম্বর = ৩০ নম্বর থাকে। Notice অবশ্যই চারদিক ঘেরা BOX-এর মধ্যে লিখতে হবে (School Name, NOTICE, Date, Heading, Body with Date/Time/Venue, Signature & Countersigned by HM)। Process Writing সর্বদা Present Indefinite Passive Voice (is/are + V₃) এবং Linkers (At first, Next, Then, Finally) দিয়ে লিখতে হয়।",
            boardTipBn = "Notice-এ বক্স আঁকা, Report-এ By-line (By a Staff Reporter) + Place & Date, এবং Process-এ Passive Voice ব্যবহার করলে ২-৩ নম্বর বাড়তি নিশ্চিত হয়।",
            keyFormulaOrFact = "Notice: Box + School Name + NOTICE + Date + Counter-signature | Report: Headline + By a Staff Reporter + Place, Date | Process: is/are + V₃",
            examQuestion = "Which verb voice is NOT suitable as the primary style for WBBSE Process Writing?",
            options = listOf("Simple Present Passive (is washed, are mixed)", "Using linkers like First, Next, Finally", "Future Continuous Active tense throughout", "Adding an appropriate title at the top"),
            correctIndex = 2
        )
    )

    // ═══════════════════════════════════════════════════════════════════════
    // 3. গণিত (MATHEMATICS) — 10 High-Yield WBBSE Class 10 Sure-Shot Topics
    // ═══════════════════════════════════════════════════════════════════════
    private val mathNotes = listOf(
        SeedNote(
            chapter = "অধ্যায় ১: একচলবিশিষ্ট দ্বিঘাত সমীকরণ",
            title = "শ্রীধর আচার্যের সূত্র ও নিরূপক দ্বারা বীজের প্রকৃতি নির্ণয়",
            marksWeight = 3,
            noteBn = "ax² + bx + c = 0 (a ≠ 0) দ্বিঘাত সমীকরণের বীজদ্বয় x = (-b ± √(b² - 4ac)) / 2a। নিরূপক D = b² - 4ac হলে: D > 0 হলে বীজদ্বয় বাস্তব ও অসমান; D = 0 হলে বীজদ্বয় বাস্তব ও সমান (-b/2a); এবং D < 0 হলে কোনো বাস্তব বীজ থাকে না। বীজদ্বয় α ও β হলে α + β = -b/a এবং αβ = c/a।",
            boardTipBn = "সমাধানের শুরুতে ‘a ≠ 0’ শর্তটি লিখবে এবং প্রদত্ত বীজ α, β থেকে সমীকরণ গঠনের সময় x² - (α+β)x + αβ = 0 সূত্রটি বক্স করে দেখাবে।",
            keyFormulaOrFact = "x = (-b ± √(b² - 4ac)) / 2a | D = b² - 4ac | α + β = -b/a | αβ = c/a",
            examQuestion = "ax² + bx + c = 0 (a ≠ 0) সমীকরণের বীজদ্বয় বাস্তব ও সমান হওয়ার শর্ত কোনটি?",
            options = listOf("b² - 4ac > 0", "b² - 4ac = 0", "b² - 4ac < 0", "a + b + c = 0"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ২ ও ৬: সরল সুদকষা ও চক্রবৃদ্ধি সুদ",
            title = "সরল সুদ, বার্ষিক ও ষাণ্মাসিক চক্রবৃদ্ধি সুদ এবং সমহার বৃদ্ধি/হ্রাস",
            marksWeight = 5,
            noteBn = "আসল P, বার্ষিক সুদের হার r% ও সময় t বছর হলে সরল সুদ I = Prt/100 এবং সুদ-আসল A = P + I। বার্ষিক চক্রবৃদ্ধিতে সমূল চক্রবৃদ্ধি A = P(1 + r/100)ⁿ; ৬ মাস অন্তর দেয় (ষাণ্মাসিক) হলে A = P(1 + r/200)²ⁿ। সমহার হ্রাস (অবচয়) হলে মূল্য = P(1 - r/100)ⁿ।",
            boardTipBn = "পাটিগণিতের ৫ নম্বরের অঙ্কে শেষ উত্তরে অবশ্যই ‘টাকা’ বা ‘%’ একক লিখবে এবং সময় মাসে থাকলে ১২ দিয়ে ভাগ করে বছরে নেবে।",
            keyFormulaOrFact = "I = Prt / 100 | A = P(1 + r/100)ⁿ | ষাণ্মাসিক: A = P(1 + r/200)²ⁿ | অবচয়: P(1 - r/100)ⁿ",
            examQuestion = "সুদ ৬ মাস অন্তর (ষাণ্মাসিক) দেয় হলে P টাকার r% হারে n বছরের সমূল চক্রবৃদ্ধি কত?",
            options = listOf("P(1 + r/100)ⁿ", "P(1 + r/200)²ⁿ", "P(1 + r/400)⁴ⁿ", "P(1 - r/200)²ⁿ"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ১৪: অংশীদারি কারবার (Partnership Business)",
            title = "সরল ও মিশ্র অংশীদারি কারবারে লভ্যাংশ বণ্টন",
            marksWeight = 5,
            noteBn = "সরল অংশীদারি কারবারে মূলধন সমান সময় ধরে খাটলে লভ্যাংশ মূলধনের অনুপাতে ভাগ হয়। মিশ্র অংশীদারি কারবারে বিভিন্ন সময়ের জন্য টাকা খাটলে ‘মূলধন × সময়’ (এক মাস হিসেবে সমতুল্য মূলধন) এর অনুপাতে লাভ ভাগ হয়। মোট লাভের একাংশ কাজের ভাতা বা সমানভাগে বণ্টিত হলে আগে তা বাদ দিয়ে বাকি লাভ মূলধনের অনুপাতে ভাগ করতে হয়।",
            boardTipBn = "মিশ্র অংশীদারি অঙ্কে প্রত্যেক বন্ধুর ‘১ মাস হিসাবে মূলধন’ আলাদা লাইনে দেখিয়ে তারপরে অনুপাত বের করলে পূর্ণ ৫ নম্বর পাওয়া যায়।",
            keyFormulaOrFact = "লভ্যাংশের অনুপাত = (মূলধন₁ × সময়₁) : (মূলধন₂ × সময়₂) | প্রাপ্য লাভ = মোট লাভ × (নিজ অনুপাত / অনুপাতের সমষ্টি)",
            examQuestion = "অংশীদারি কারবারে মূলধন ভিন্ন ভিন্ন সময়ের জন্য নিযুক্ত থাকলে লভ্যাংশ কীভাবে ভাগ হয়?",
            options = listOf("সমান ভাগে", "শুধু সময়ের অনুপাতে", "(মূলধন × সময়)-এর অনুপাতে", "বয়সের অনুপাতে"),
            correctIndex = 2
        ),
        SeedNote(
            chapter = "অধ্যায় ৭: বৃত্তস্থ কোণ সম্পর্কিত উপপাদ্য (৩৪ ও ৩৭)",
            title = "কেন্দ্রস্থ কোণ পরিধিস্থ কোণের দ্বিগুণ ও অর্ধবৃত্তস্থ কোণ সমকোণ",
            marksWeight = 5,
            noteBn = "উপপাদ্য ৩৪: কোনো বৃত্তের একই বৃত্তচাপের ওপর অবস্থিত কেন্দ্রস্থ কোণ বৃত্তস্থ (পরিধিস্থ) কোণের দ্বিগুণ (∠AOB = 2∠ACB)। উপপাদ্য ৩৭: অর্ধবৃত্তস্থ কোণ সমকোণ (৯০°), কারণ অর্ধবৃত্তচাপের কেন্দ্রস্থ কোণ সরলকোণ (১৮০°), ফলে পরিধিস্থ কোণ = ১৮০°/২ = ৯০°।",
            boardTipBn = "উপপাদ্যে প্রদত্ত, প্রামাণ্য, অঙ্কন ও প্রমাণ এই ৪টি ধাপ স্পষ্ট শিরোনাম দিয়ে লিখবে এবং পেন্সিল-কম্পাসে পরিষ্কার চিত্র আঁকবে।",
            keyFormulaOrFact = "কেন্দ্রস্থ কোণ = 2 × বৃত্তস্থ কোণ | অর্ধবৃত্তস্থ কোণ = 90° | একই বৃত্তাংশস্থ সকল কোণ সমান (উপপাদ্য ৩৫)",
            examQuestion = "কোনো বৃত্তের একই বৃত্তচাপের ওপর অবস্থিত কেন্দ্রস্থ কোণ 120° হলে বৃত্তস্থ কোণের মান কত?",
            options = listOf("240°", "60°", "90°", "30°"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ১০ ও ১৫: বৃত্তস্থ চতুর্ভুজ ও স্পর্শক উপপাদ্য",
            title = "উপপাদ্য ৩৮ (বিপরীত কোণ সম্পূরক) এবং উপপাদ্য ৪০ ও ৪১ (স্পর্শক)",
            marksWeight = 5,
            noteBn = "উপপাদ্য ৩৮: বৃত্তস্থ চতুর্ভুজের বিপরীত কোণগুলি পরস্পর সম্পূরক (∠A + ∠C = 180°, ∠B + ∠D = 180°)। উপপাদ্য ৪০: বৃত্তের কোনো বিন্দুতে স্পর্শক ও ওই স্পর্শবিন্দুগামী ব্যাসার্ধ পরস্পর লম্ব। উপপাদ্য ৪১: বৃত্তের বহিঃস্থ কোনো বিন্দু থেকে অঙ্কিত দুটি স্পর্শকের দৈর্ঘ্য সমান এবং তারা কেন্দ্রে সমান কোণ উৎপন্ন করে।",
            boardTipBn = "স্পর্শকের ২ নম্বরের শর্ট অঙ্কে সমকোণী ত্রিভুজ গঠিত হয়: (স্পর্শকের দৈর্ঘ্য)² + (ব্যাসার্ধ)² = (কেন্দ্র থেকে দূরত্ব)²।",
            keyFormulaOrFact = "∠A + ∠C = 180° | OP ⊥ স্পর্শক | বহিঃস্থ বিন্দু থেকে দুটি স্পর্শক: PA = PB | l = √(d² - r²)",
            examQuestion = "বৃত্তস্থ চতুর্ভুজ ABCD-তে ∠A = 75° হলে বিপরীত কোণ ∠C-এর মান কত?",
            options = listOf("75°", "105°", "115°", "90°"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ২২: পিথাগোরাসের উপপাদ্য (উপপাদ্য ৪৯ ও ৫০)",
            title = "পিথাগোরাসের উপপাদ্য ও তার বিপরীত উপপাদ্য প্রমাণ",
            marksWeight = 5,
            noteBn = "উপপাদ্য ৪৯: যেকোনো সমকোণী ত্রিভুজের অতিভুজের ওপর অঙ্কিত বর্গক্ষেত্রের ক্ষেত্রফল অপর দুই বাহুর ওপর অঙ্কিত বর্গক্ষেত্রদ্বয়ের ক্ষেত্রফলের সমষ্টির সমান (AC² = AB² + BC²)। সমকৌণিক বিন্দু B থেকে অতিভুজ AC-এর ওপর লম্ব BD টেনে সদৃশকোণী ত্রিভুজ (উপপাদ্য ৪৮) প্রয়োগ করে এটি প্রমাণ করা হয়।",
            boardTipBn = "মাধ্যমিক ৫ নম্বরের উপপাদ্যে উপপাদ্য ৪৯ বা ৫০ সবচেয়ে বেশিবার আসে; △ABD ~ △ABC ও △BCD ~ △ABC স্পষ্ট করে দেখাবে।",
            keyFormulaOrFact = "অতিভুজ² = লম্ব² + ভূমি² | AC² = AB² + BC² | BD ⊥ AC হলে BD² = AD × DC",
            examQuestion = "পিথাগোরাসের উপপাদ্য প্রমাণের সময় সমকৌণিক বিন্দু থেকে অতিভুজের ওপর লম্ব টানলে কী পাওয়া যায়?",
            options = listOf("দুটি সমবাহু ত্রিভুজ", "মূল ত্রিভুজের সাথে সদৃশ দুটি সমকোণী ত্রিভুজ", "দুটি স্থূলকোণী ত্রিভুজ", "কোনো ত্রিভুজ নয়"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ১১ ও ২১: জ্যামিতিক সম্পাদ্য (৫ নম্বর নিশ্চিত)",
            title = "ত্রিভুজের পরিবৃত্ত, অন্তর্বৃত্ত এবং মধ্যসমানুপাতী (√x) অঙ্কন",
            marksWeight = 5,
            noteBn = "পরিবৃত্ত অঙ্কনে ত্রিভুজের যেকোনো দুটি বাহুর লম্ব সমদ্বিখন্ডক এঁকে পরিকেন্দ্র নির্ণয় করতে হয়। অন্তর্বৃত্ত অঙ্কনে যেকোনো দুটি অন্তঃকোণের সমদ্বিখন্ডক এঁকে অন্তঃকেন্দ্র বের করে যেকোনো বাহুর ওপর লম্ব টেনে ব্যাসার্ধ নিতে হয়। মধ্যসমানুপাতী (√ab) অঙ্কনে (a + b) ব্যাস নিয়ে অর্ধবৃত্ত এঁকে সংযোগবিন্দুতে লম্ব আঁকতে হয়।",
            boardTipBn = "সম্পাদ্যে শুধু অঙ্কন চিহ্ন (arcs) থাকলেই হয়, প্রমাণের দরকার নেই; তবে শেষে ‘অঙ্কন পরিচিতি’ (যেমন: নির্ণেয় পরিবৃত্তের ব্যাসার্ধ = ... সেমি) অবশ্যই লিখবে।",
            keyFormulaOrFact = "পরিবৃত্ত: ২ বাহুর লম্ব সমদ্বিখন্ডক | অন্তর্বৃত্ত: ২ কোণের সমদ্বিখন্ডক | মধ্যসমানুপাতী: x = √(ab)",
            examQuestion = "ত্রিভুজের অন্তর্বৃত্ত অঙ্কনের জন্য অন্তঃকেন্দ্র পেতে কী অঙ্কন করতে হয়?",
            options = listOf("দুটি বাহুর লম্ব সমদ্বিখন্ডক", "দুটি অন্তঃকোণের সমদ্বিখন্ডক", "তিনটি মধ্যমা", "দুটি বহিঃকোণ"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ২৩ ও ২৫: ত্রিকোণমিতি ও উচ্চতা-দূরত্ব",
            title = "ত্রিকোণমিতিক অভেদাবলী, পূরক কোণ ও উন্নতি-অবনতি কোণের অঙ্ক",
            marksWeight = 5,
            noteBn = "মূল অভেদ তিনটি: sin²θ + cos²θ = 1, sec²θ - tan²θ = 1, এবং cosec²θ - cot²θ = 1। পূরক কোণে sin(90° - θ) = cosθ, tan(90° - θ) = cotθ। উচ্চতা-দূরত্বের ৫ নম্বরের অঙ্কে উন্নতি ও অবনতি কোণ একান্তর কোণ হওয়ায় সমান হয় এবং সাধারণত tan 30° = 1/√3, tan 45° = 1, tan 60° = √3 ব্যবহার হয়।",
            boardTipBn = "উচ্চতা ও দূরত্বের অঙ্কে স্কেল-পেন্সিল দিয়ে সঠিক সমকোণী ত্রিভুজের চিত্র ও তীরচিহ্নসহ কোণ না আঁকলে বোর্ডে নম্বর কাটা যায়!",
            keyFormulaOrFact = "sin²θ + cos²θ = 1 | sec²θ - tan²θ = 1 | cosec²θ - cot²θ = 1 | tan 30° = 1/√3, tan 45° = 1, tan 60° = √3",
            examQuestion = "নিম্নলিখিত কোন ত্রিকোণমিতিক অভেদটি সঠিক নয়?",
            options = listOf("sin²θ + cos²θ = 1", "sec²θ - tan²θ = 1", "cosec²θ - cot²θ = 1", "sin²θ - cos²θ = 1"),
            correctIndex = 3
        ),
        SeedNote(
            chapter = "অধ্যায় ৮, ১২ ও ১৬: পরিমিতি (চোঙ, গোলক ও শঙ্কু)",
            title = "লম্ব বৃত্তাকার চোঙ, গোলক ও শঙ্কুর ক্ষেত্রফল ও আয়তন",
            marksWeight = 4,
            noteBn = "ব্যাসার্ধ r ও উচ্চতা h হলে লম্ব বৃত্তাকার চোঙের বক্রতল = 2πrh এবং আয়তন = πr²h। গোলকের সমগ্রতল = 4πr² এবং আয়তন = (4/3)πr³ (অর্ধগোলকের সমগ্রতল = 3πr²)। লম্ব বৃত্তাকার শঙ্কুর তির্যক উচ্চতা l = √(r² + h²), বক্রতল = πrl এবং আয়তন = (1/3)πr²h।",
            boardTipBn = "একটি নিরেট ঘনবস্তু গলিয়ে অন্য ঘনবস্তু তৈরি করলে তাদের ‘মোট আয়তন সমান’ হয়—এই শর্তে π উভয় দিক থেকে কাটাকাটি করলে হিসাব সহজ হয়।",
            keyFormulaOrFact = "চোঙ: V = πr²h, CSA = 2πrh | গোলক: V = (4/3)πr³, S = 4πr² | শঙ্কু: l = √(r²+h²), V = (1/3)πr²h",
            examQuestion = "r ব্যাসার্ধবিশিষ্ট একটি নিরেট অর্ধগোলকের সমগ্রতলের ক্ষেত্রফল কত?",
            options = listOf("2πr²", "3πr²", "4πr²", "(2/3)πr³"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ২৬: রাশিবিজ্ঞান (গড়, মধ্যমা, ওজাইভ ও সংখ্যাগুরু মান)",
            title = "যৌগিক গড়, মধ্যমা ও সংখ্যাগুরু মান (Mode) নির্ণয়ের বোর্ড সূত্র",
            marksWeight = 4,
            noteBn = "মাধ্যমিকে রাশিবিজ্ঞান থেকে দুটি ৪ নম্বরের প্রশ্ন (মোট ৮ নম্বর) প্রায় নিশ্চিত থাকে। কল্পিত গড় বা ক্রমবিচ্যুতি পদ্ধতিতে গড় x̄ = a + h × (Σfᵢuᵢ / Σfᵢ)। মধ্যমা = l + [(n/2 - cf) / f] × h। সংখ্যাগুরু মান (Mode) = l + [(f₁ - f₀) / (2f₁ - f₀ - f₂)] × h।",
            boardTipBn = "শ্রেণি-সীমানা অবিচ্ছিন্ন (continuous) না থাকলে আগে ০.৫ বিয়োগ/যোগ করে শ্রেণি-সীমানা তৈরি করে তবেই মধ্যমা বা সংখ্যাগুরু মান বের করবে।",
            keyFormulaOrFact = "গড়: x̄ = a + h(Σfᵢuᵢ / Σfᵢ) | মধ্যমা: l + [(n/2 - cf)/f] × h | Mode: l + [(f₁ - f₀)/(2f₁ - f₀ - f₂)] × h",
            examQuestion = "সংখ্যাগুরু মান (Mode) নির্ণয়ের সূত্রে f₁ কী নির্দেশ করে?",
            options = listOf("মোট পরিসংখ্যা", "সংখ্যাগুরু শ্রেণির পরিসংখ্যা", "পূর্ববর্তী শ্রেণির পরিসংখ্যা", "ক্রমযৌগিক পরিসংখ্যা"),
            correctIndex = 1
        )
    )

    // ═══════════════════════════════════════════════════════════════════════
    // 4. ভৌত বিজ্ঞান (PHYSICAL SCIENCE) — 10 High-Yield WBBSE Class 10 Topics
    // ═══════════════════════════════════════════════════════════════════════
    private val physicalScienceNotes = listOf(
        SeedNote(
            chapter = "অধ্যায় ১: পরিবেশের জন্য ভাবনা",
            title = "বায়ুমণ্ডলের স্তরবিন্যাস, ওজোন স্তর ক্ষয় ও গ্রিনহাউস প্রভাব",
            marksWeight = 3,
            noteBn = "ট্রপোস্ফিয়ারকে ক্ষুব্ধমণ্ডল (উচ্চতা বৃদ্ধিতে তাপমাত্রা ৬.৫°C/কিমি কমে) এবং স্ট্র্যাটোস্ফিয়ারকে শান্তমণ্ডল বলা হয়; স্ট্র্যাটোস্ফিয়ারের ওজোন স্তর ক্ষতিকর UV রশ্মি শোষণ করে। CFC থেকে নির্গত সক্রিয় ক্লোরিন পরমাণু (Cl•) ওজোন অণুকে (O₃) ভেঙে O₂-এ পরিণত করে। প্রধান গ্রিনহাউস গ্যাস CO₂, CH₄, N₂O, CFC ও জলীয় বাষ্প।",
            boardTipBn = "ওজোন ক্ষয়ের দুটি রাসায়নিক বিক্রিয়া (CFC + UV → Cl• এবং Cl• + O₃ → ClO• + O₂) লিখলে ৩-এ ৩ নিশ্চিত।",
            keyFormulaOrFact = "ট্রপোস্ফিয়ার = ক্ষুব্ধমণ্ডল | স্ট্র্যাটোস্ফিয়ার = ওজোন স্তর | Cl• + O₃ → ClO• + O₂ | মিথেন হাইড্রেট = ফায়ার আইস (CH₄)",
            examQuestion = "নিম্নলিখিত কোন গ্যাসটি গ্রিনহাউস গ্যাস নয়?",
            options = listOf("CO₂", "CH₄", "N₂O", "O₂"),
            correctIndex = 3
        ),
        SeedNote(
            chapter = "অধ্যায় ২: গ্যাসের আচরণ",
            title = "বয়েল ও চার্লসের সূত্র, পরমশূন্য উষ্ণতা ও আদর্শ গ্যাস সমীকরণ",
            marksWeight = 3,
            noteBn = "স্থির উষ্ণতায় নির্দিষ্ট ভরের গ্যাসের আয়তন চাপের ব্যস্তানুপাতিক (P₁V₁ = P₂V₂ — বয়েলের সূত্র)। স্থির চাপে নির্দিষ্ট ভরের গ্যাসের আয়তন পরম উষ্ণতার সমানুপাতিক (V₁/T₁ = V₂/T₂ — চার্লসের সূত্র)। উভয় সূত্রের সমন্বয়ে আদর্শ গ্যাস সমীকরণ PV = nRT = (W/M)RT। -273°C বা 0 K-কে পরমশূন্য উষ্ণতা বলে।",
            boardTipBn = "গ্যাসের অঙ্কে তাপমাত্রা সবসময় কেলভিনে (T = t + 273 K) নেবে এবং R = 0.082 L·atm·mol⁻¹·K⁻¹ বা 8.314 J·mol⁻¹·K⁻¹ একক মিলিয়ে বসাবে।",
            keyFormulaOrFact = "P₁V₁ = P₂V₂ | V₁/T₁ = V₂/T₂ | PV = nRT | PV = (W/M)RT | T(K) = t(°C) + 273",
            examQuestion = "সেলসিয়াস স্কেলে পরমশূন্য উষ্ণতার মান কত?",
            options = listOf("0°C", "-273°C", "273°C", "-100°C"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৪: তাপের ঘটনাসমূহ",
            title = "কঠিনের প্রসারণ গুণাঙ্ক (α : β : γ) ও তাপ পরিবাহিতাঙ্ক (K)",
            marksWeight = 3,
            noteBn = "কঠিনের দৈর্ঘ্য প্রসারণ গুণাঙ্ক (α), ক্ষেত্র প্রসারণ গুণাঙ্ক (β) ও আয়তন প্রসারণ গুণাঙ্ক (γ)-এর সম্পর্ক α = β/2 = γ/3 অর্থাৎ α : β : γ = 1 : 2 : 3। তরলের প্রকৃত প্রসারণ γᵣ = γₐ + γ_g। তাপ পরিবহনের হার Q/t = K·A·(θ₁ - θ₂)/d; তাপ পরিবাহিতাঙ্ক K-এর SI একক W·m⁻¹·K⁻¹।",
            boardTipBn = "আদর্শ পরিবাহীর K = অসীম (∞) এবং আদর্শ অন্তরকের K = 0; আর গ্যাসের আয়তন প্রসারণ গুণাঙ্ক γₚ = 1/273 °C⁻¹।",
            keyFormulaOrFact = "α : β : γ = 1 : 2 : 3 | γᵣ = γₐ + γ_g | Q = K·A·(θ₁ - θ₂)·t / d | K-এর SI একক: W·m⁻¹·K⁻¹",
            examQuestion = "কঠিনের দৈর্ঘ্য (α), ক্ষেত্র (β) ও আয়তন (γ) প্রসারণ গুণাঙ্কের অনুপাত কত?",
            options = listOf("1 : 2 : 3", "3 : 2 : 1", "1 : 3 : 2", "2 : 1 : 3"),
            correctIndex = 0
        ),
        SeedNote(
            chapter = "অধ্যায় ৫: আলো (Light)",
            title = "গলীয় দর্পণ, প্রিজমের চ্যুতি, লেন্স ও আলোর বিক্ষেপণ",
            marksWeight = 3,
            noteBn = "ক্ষুদ্র উন্মেষযুক্ত গলীয় দর্পণে f = r/2। প্রতিসরণের স্নেলের সূত্র μ = sin i / sin r = c / v। প্রিজমের মধ্য দিয়ে আলোর চ্যুতিকোণ δ = i₁ + i₂ - A। ক্ষীণদৃষ্টি (মায়োপিয়া) দূর করতে অবতল লেন্স এবং দীর্ঘদৃষ্টি (হাইপারমেট্রোপিয়া) দূর করতে উত্তল লেন্স ব্যবহৃত হয়। আলোর বিক্ষেপণ I ∝ 1/λ⁴ হওয়ায় আকাশ নীল দেখায়।",
            boardTipBn = "রশ্মিচিত্রে আলোর গতিপথের তীরচিহ্ন (→) দিতে ভুললে ১ নম্বর কাটা যায়; আর লাল আলোর তরঙ্গদৈর্ঘ্য (λ) সর্বাধিক হওয়ায় বিপদসংকেতে লাল আলো ব্যবহৃত হয়।",
            keyFormulaOrFact = "f = r / 2 | μ = sin i / sin r = c / v | δ = i₁ + i₂ - A | বিক্ষেপণ: I ∝ 1/λ⁴",
            examQuestion = "দীর্ঘদৃষ্টি (হাইপারমেট্রোপিয়া) ত্রুটি দূর করতে কোন লেন্স ব্যবহার করা হয়?",
            options = listOf("অবতল লেন্স", "উত্তল লেন্স", "সমতল কাঁচ", "প্রিজম"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৬: চলতড়িৎ (Current Electricity)",
            title = "ওহমের সূত্র, রোধের সমবায়, জুলের সূত্র ও ফ্লেমিংয়ের বামহস্ত নিয়ম",
            marksWeight = 3,
            noteBn = "তাপমাত্রা ও অন্যান্য ভৌত অবস্থা অপরিবর্তিত থাকলে পরিবাহীর প্রবাহমাত্রা বিভব প্রভেদের সমানুপাতিক (V = IR)। রোধ R = ρ(l/A)। তড়িৎপ্রবাহের তাপীয় ফলে উৎপন্ন তাপ H = I²Rt জুল = I²Rt / 4.2 ক্যালোরি। 1 BOT = 1 kWh = 3.6 × 10⁶ J। তড়িৎ মোটরে ফ্লেমিংয়ের বামহস্ত নিয়ম এবং জেনারেটরে ফ্যারাডে-লেঞ্জের সূত্র কাজ করে।",
            boardTipBn = "শ্রেণি সমবায়ে তুল্য রোধ R = R₁ + R₂ এবং সমান্তরাল সমবায়ে 1/R = 1/R₁ + 1/R₂; অঙ্কের উত্তরে Ω, A, V বা kWh একক অবশ্যই লিখবে।",
            keyFormulaOrFact = "V = IR | R = ρl / A | H = I²Rt | 1 BOT = 1 kWh = 3.6 × 10⁶ J | P = VI = I²R = V²/R",
            examQuestion = "1 BOT (1 kWh) সমান কত জুল (J) তড়িৎশক্তি?",
            options = listOf("3.6 × 10³ J", "3.6 × 10⁶ J", "3.6 × 10⁵ J", "1000 J"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৭: পরমাণুর নিউক্লিয়াস",
            title = "তেজস্ক্রিয়তা (α, β, γ রশ্মি), নিউক্লীয় বিভাজন ও সংযোজন",
            marksWeight = 3,
            noteBn = "তেজস্ক্রিয়তা একটি নিউক্লীয় ঘটনা। α-কণা (₂He⁴) নির্গমনে ভরসংখ্যা ৪ ও পারমাণবিক সংখ্যা ২ কমে; β-কণা (₋₁e⁰) নির্গমনে ভরসংখ্যা অপরিবর্তিত ও পারমাণবিক সংখ্যা ১ বাড়ে; γ-রশ্মি নিস্তড়িৎ ও সর্বাধিক ভেদনক্ষমতাসম্পন্ন। আইনস্টাইনের ভর-শক্তি সমীকরণ E = Δmc²। সূর্যের শক্তির উৎস নিউক্লীয় সংযোজন।",
            boardTipBn = "আয়নন ক্ষমতার ক্রম: α > β > γ এবং ভেদন ক্ষমতার ক্রম: γ > β > α — এই বিপরীত ক্রমটি বোর্ড MCQ-তে খুব কমন!",
            keyFormulaOrFact = "α নিঃসরণ: A-4, Z-2 | β নিঃসরণ: A একই, Z+1 | ভেদন ক্ষমতা: γ > β > α | E = Δmc²",
            examQuestion = "তেজস্ক্রিয় রশ্মিগুলির মধ্যে ভেদন ক্ষমতা সবচেয়ে বেশি কার?",
            options = listOf("α রশ্মি", "β রশ্মি", "γ রশ্মি", "ক্যাথোড রশ্মি"),
            correctIndex = 2
        ),
        SeedNote(
            chapter = "অধ্যায় ৮.১: পর্যায় সারণি ও মৌলদের ধর্মের পর্যায়বৃত্ততা",
            title = "আধুনিক পর্যায় সূত্র এবং পারমাণবিক ব্যাসার্ধ, আয়নন শক্তি ও তড়িৎঋণাত্মকতা",
            marksWeight = 3,
            noteBn = "আধুনিক পর্যায় সারণিতে ৭টি পর্যায় ও ১৮টি শ্রেণি আছে; মৌলের ভৌত ও রাসায়নিক ধর্ম পারমাণবিক সংখ্যার (Z) বৃদ্ধির সাথে পর্যায়ক্রমে পুনরাবৃত্ত হয়। পর্যায় বরাবর বাম থেকে ডানে পারমাণবিক ব্যাসার্ধ কমে, কিন্তু আয়নন শক্তি ও তড়িৎঋণাত্মকতা বাড়ে। শ্রেণি বরাবর উপর থেকে নিচে ব্যাসার্ধ বাড়ে, আয়নন শক্তি ও তড়িৎঋণাত্মকতা কমে।",
            boardTipBn = "সর্বাধিক তড়িৎঋণাত্মক মৌল ফ্লুরিন (F), ক্ষার ধাতু (শ্রেণি ১), হ্যালোজেন (শ্রেণি ১৭) ও নিষ্ক্রিয় গ্যাস (শ্রেণি ১৮) মনে রাখবে।",
            keyFormulaOrFact = "বাম → ডান: ব্যাসার্ধ ↓, আয়নন শক্তি ↑, তড়িৎঋণাত্মকতা ↑ | উপর → নিচ: ব্যাসার্ধ ↑ | সর্বোচ্চ তড়িৎঋণাত্মক: F",
            examQuestion = "আধুনিক পর্যায় সারণিতে হ্যালোজেন মৌলগুলি কোন শ্রেণিতে অবস্থিত?",
            options = listOf("শ্রেণি ১", "শ্রেণি ২", "শ্রেণি ১৭", "শ্রেণি ১৮"),
            correctIndex = 2
        ),
        SeedNote(
            chapter = "অধ্যায় ৮.২ ও ৮.৩: রাসায়নিক বন্ধন ও তড়িৎবিশ্লেষণ",
            title = "আয়নীয় বনাম সমযোজী যৌগ, লুইস ডট গঠন ও তড়িৎলেপন",
            marksWeight = 3,
            noteBn = "ইলেকট্রন বর্জন-গ্রহণে আয়নীয় বন্ধন (NaCl, CaO) এবং ইলেকট্রন জোড় সমভাবে ব্যবহারে সমযোজী বন্ধন (CH₄, H₂O, CO₂, N₂) গঠিত হয়। তড়িৎবিশ্লেষণে ক্যাথোডে বিজারণ ও অ্যানোডে জারণ ঘটে। তড়িৎলেপনে যে বস্তুতে প্রলেপ দিতে হবে তাকে ক্যাথোডে এবং যে ধাতুর প্রলেপ দেওয়া হবে তাকে অ্যানোডে রাখতে হয়।",
            boardTipBn = "সোনার প্রলেপের তড়িৎবিশ্লেষ্য পটাশিয়াম অরোসায়ানাইড K[Au(CN)₂] এবং রূপোর প্রলেপে পটাশিয়াম আর্জেন্টোসায়ানাইড K[Ag(CN)₂]।",
            keyFormulaOrFact = "ক্যাথোড = বিজারণ (বস্তু) | অ্যানোড = জারণ (প্রলেপের ধাতু) | অ্যাসিডযুক্ত জলে H₂ : O₂ = 2 : 1 | N₂-এ ত্রিবন্ধন (N≡N)",
            examQuestion = "লোহার চামচে নিকেলের তড়িৎলেপন করার সময় ক্যাথোড হিসেবে কী ব্যবহৃত হয়?",
            options = listOf("বিশুদ্ধ নিকেল দণ্ড", "লোহার চামচ", "প্ল্যাটিনাম তার", "গ্রাফাইট দণ্ড"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৮.৪: অজৈব রসায়ন (পরীক্ষাগার ও শিল্পোৎপাদন)",
            title = "NH₃ (হেবার), H₂SO₄ (স্পর্শ), HNO₃ (অস্টওয়াল্ড) ও ইউরিয়া উৎপাদন",
            marksWeight = 3,
            noteBn = "পরীক্ষাগারে NH₄Cl ও শুষ্ক কলিচুন Ca(OH)₂ উত্তপ্ত করে NH₃ প্রস্তুত হয় এবং পোড়াচুন (CaO) দিয়ে শুষ্ক করা হয়। শিল্পক্ষেত্রে হেবার পদ্ধতিতে NH₃ (অনুঘটক: Fe চূর্ণ), স্পর্শ পদ্ধতিতে H₂SO₄ (অনুঘটক: V₂O₅) এবং অস্টওয়াল্ড পদ্ধতিতে HNO₃ (অনুঘটক: Pt-Rh তারজালি) তৈরি হয়।",
            boardTipBn = "NH₃ শুষ্ক করতে গাঢ় H₂SO₄ বা অনার্দ্র CaCl₂ ব্যবহার করা যায় না—কারণ তারা ক্ষারীয় NH₃-এর সাথে বিক্রিয়া করে; তাই ক্ষারীয় CaO ব্যবহৃত হয়।",
            keyFormulaOrFact = "হেবার (NH₃): Fe, 200 atm, 450°C | স্পর্শ (H₂SO₄): V₂O₅ | অস্টওয়াল্ড (HNO₃): Pt-Rh | ওলিয়াম: H₂S₂O₇",
            examQuestion = "স্পর্শ পদ্ধতিতে সালফিউরিক অ্যাসিড (H₂SO₄) শিল্পোৎপাদনে কোন অনুঘটক ব্যবহৃত হয়?",
            options = listOf("লোহা চূর্ণ (Fe)", "ভ্যানাডিয়াম পেন্টক্সাইড (V₂O₅)", "নিকেল (Ni)", "পোড়াচুন (CaO)"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৮.৫ ও ৮.৬: ধাতুবিদ্যা ও জৈব রসায়ন",
            title = "আকরিক, থার্মিট পদ্ধতি, কার্যকরী মূলক ও IUPAC নামকরণ",
            marksWeight = 3,
            noteBn = "প্রধান আকরিক: লোহা (রেড হেমাটাইট Fe₂O₃), অ্যালুমিনিয়াম (বক্সাইট Al₂O₃·2H₂O), তামা (কপার পাইরাইটিস CuFeS₂), জিংক (জিংক ব্লেন্ড ZnS)। থার্মিট মিশ্রণে ৩ ভাগ Fe₂O₃ ও ১ ভাগ Al চূর্ণ থাকে (রেললাইন জোড়া লাগাতে)। জৈব রসায়নে অ্যালকেন CₙH₂ₙ₊₂, অ্যালকিন CₙH₂ₙ, অ্যালকাইন CₙH₂ₙ₋₂ এবং কার্যকরী মূলক -OH (অল), -CHO (অ্যাল), -COOH (ওইক অ্যাসিড)।",
            boardTipBn = "‘সকল আকরিকই খনিজ, কিন্তু সকল খনিজ আকরিক নয়’ এবং CH₃CH₂OH ও CH₃OCH₃ কার্যকরী মূলকঘটিত সমাবয়ব—অবশ্যই মনে রাখবে।",
            keyFormulaOrFact = "থার্মিট: 3Fe₂O₃ + 2Al | অ্যালকেন: CₙH₂ₙ₊₂ | অ্যালকিন: CₙH₂ₙ | অ্যালকাইন: CₙH₂ₙ₋₂ | ডিনেচার্ড স্পিরিট = CH₃OH + C₂H₅OH",
            examQuestion = "অ্যালকাইন (Alkyne) শ্রেণির জৈব যৌগের সাধারণ সংকেত কোনটি?",
            options = listOf("CₙH₂ₙ₊₂", "CₙH₂ₙ", "CₙH₂ₙ₋₂", "CₙH₂ₙ₊₁OH"),
            correctIndex = 2
        )
    )

    // ═══════════════════════════════════════════════════════════════════════
    // 5. জীবন বিজ্ঞান (LIFE SCIENCE) — 10 High-Yield WBBSE Class 10 Topics
    // ═══════════════════════════════════════════════════════════════════════
    private val lifeScienceNotes = listOf(
        SeedNote(
            chapter = "অধ্যায় ১: জীবজগতে নিয়ন্ত্রণ ও সমন্বয়",
            title = "উদ্ভিদের চলন (ট্রপিক, ট্যাকটিক, ন্যাস্টিক) ও উদ্ভিদ হরমোন",
            marksWeight = 5,
            noteBn = "উদ্দীপকের গতিপথ অনুসারে বক্রচলন হলো ট্রপিক চলন (অক্সিন নিয়ন্ত্রিত); আর তীব্রতা অনুসারে রসস্ফীতি তারতম্যে ঘটে ন্যাস্টিক চলন (যেমন লজ্জাবতীর সিসমোন্যাস্টি)। প্রধান উদ্ভিদ হরমোন: অক্সিন (IAA — অগ্রস্থ প্রকটতা ও কোশ বিভাজন), জিব্বেরেলিন (GA — নাইট্রোজেনবিহীন, বীজের সুপ্তাবস্থা ভঙ্গ ও পর্বমধ্যের বৃদ্ধি) এবং সাইটোকাইনিন (পাতার বার্ধক্য বিলম্বিত করে)।",
            boardTipBn = "কৃত্রিম অক্সিন (NAA, IBA, 2,4-D)-এর কৃষিজ প্রয়োগ ও ট্রপিক-ন্যাস্টিক চলনের ৩টি পার্থক্য ছক আকারে তৈরি রাখবে।",
            keyFormulaOrFact = "অক্সিন = ইন্ডোল অ্যাসিটিক অ্যাসিড (IAA) | নাইট্রোজেনবিহীন হরমোন: জিব্বেরেলিন | আগাছানাশক: 2,4-D | লজ্জাবতী = সিসমোন্যাস্টি",
            examQuestion = "নিম্নলিখিত কোন উদ্ভিদ হরমোনটি নাইট্রোজেনবিহীন?",
            options = listOf("অক্সিন", "জিব্বেরেলিন", "সাইটোকাইনিন", "কাইনেটিন"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ১: প্রাণী হরমোন ও অন্তঃক্ষরা গ্রন্থি",
            title = "পিটুইটারি, থাইরয়েড, অগ্ন্যাশয় (ইনসুলিন) ও অ্যাড্রিনালিন হরমোন",
            marksWeight = 5,
            noteBn = "পিটুইটারি গ্রন্থিকে ‘প্রভুগ্রন্থি’ (Master Gland) এবং হাইপোথ্যালামাসকে ‘সুপ্রিম কমান্ডার’ বলে। অগ্ন্যাশয় একটি মিশ্র গ্রন্থি—এর আইলেটস অব ল্যাঙ্গারহ্যান্সের β-কোশ থেকে নিঃসৃত ইনসুলিন রক্তে শর্করার মাত্রা কমায় (অভাবে ডায়াবেটিস মেলিটাস বা মধুমেহ হয়)। অ্যাড্রিনালিনকে ‘আপৎকালীন বা জরুরি হরমোন’ বলে।",
            boardTipBn = "ডায়াবেটিস মেলিটাস (ইনসুলিনের অভাব) এবং ডায়াবেটিস ইনসিপিডাস (ADH-এর অভাব)—এই দুটি গুলিয়ে ফেলবে না!",
            keyFormulaOrFact = "প্রভুগ্রন্থি: পিটুইটারি | মিশ্র গ্রন্থি: অগ্ন্যাশয় | ইনসুলিন: β-কোশ (শর্করা কমায়) | জরুরি হরমোন: অ্যাড্রিনালিন | BMR নিয়ন্ত্রণ: থাইরক্সিন",
            examQuestion = "রক্তে গ্লুকোজের মাত্রা নিয়ন্ত্রণকারী ইনসুলিন হরমোন অগ্ন্যাশয়ের কোন কোশ থেকে নিঃসৃত হয়?",
            options = listOf("α-কোশ", "β-কোশ", "δ-কোশ", "স্নায়ুকোশ"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ১: স্নায়ুতন্ত্র, প্রতিবর্ত চাপ ও মানবচক্ষু",
            title = "নিউরন, প্রতিবর্ত চাপ ও অক্ষিগোলকের চিহ্নিত চিত্র (৫ নম্বর নিশ্চিত)",
            marksWeight = 5,
            noteBn = "স্নায়ুতন্ত্রের গঠনগত ও কার্যগত একক নিউরন (কোশদেহ, ডেনড্রন, অ্যাক্সন ও মায়েলিন আবরণী)। প্রতিবর্ত চাপের পথ: গ্রাহক → সংজ্ঞাবহ নিউরন → স্নায়ুকেন্দ্র (সুষুম্নাকাণ্ড) → আজ্ঞাবহ নিউরন → কারক। মানবচক্ষুর রেটিনায় রড কোশ মৃদু আলোয় এবং কোন কোশ উজ্জ্বল আলো ও বর্ণ চিনতে সাহায্য করে; পীতবিন্দুতে সবচেয়ে স্পষ্ট প্রতিবিম্ব গঠিত হয়।",
            boardTipBn = "মাধ্যমিকে ৫ নম্বরের চিত্র হিসেবে (ক) আদর্শ নিউরন, (খ) প্রতিবর্ত চাপ অথবা (গ) মানবচক্ষুর লম্বচ্ছেদ (৪টি অংশ চিহ্নিতকরণ) প্রায় নিশ্চিত আসে!",
            keyFormulaOrFact = "প্রতিবর্ত চাপ: গ্রাহক → সংজ্ঞাবহ → সুষুম্নাকাণ্ড → আজ্ঞাবহ → কারক | ভারসাম্য = লঘুমস্তিষ্ক | রড = মৃদু আলো | কোন = বর্ণ",
            examQuestion = "মানবমস্তিষ্কের কোন অংশ দেহের ভারসাম্য নিয়ন্ত্রণ করে?",
            options = listOf("গুরুমস্তিষ্ক", "লঘুমস্তিষ্ক", "সুষুম্নাশীর্ষক", "থ্যালামাস"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ২: জীবনের প্রবহমানতা (কোশবিভাজন ও ক্রোমোজোম)",
            title = "মাইটোসিস বনাম মিয়োসিস এবং DNA-RNA ও ক্রোমোজোম গঠন",
            marksWeight = 5,
            noteBn = "মাইটোসিস দেহকোশে ঘটে (2n → 2n, সমবিভাজন; বৃদ্ধি ও ক্ষয়পূরণ)। মিয়োসিস জনন মাতৃকোশে ঘটে (2n → n, হ্রাস বিভাজন; গ্যামেট গঠন ও ক্রসিং ওভারের ফলে প্রকরণ সৃষ্টি)। মানুষের দেহকোশে 2n = 46 (44 অটোজোম + XX/XY)। DNA-তে A=T ও G≡C ক্ষারক জোড় থাকে; RNA-তে থাইমিনের পরিবর্তে ইউরাসিল (U) থাকে।",
            boardTipBn = "মাইটোসিসের মেটাফেজ দশায় ক্রোমোজোম সবচেয়ে স্পষ্টভাবে বিষুব অঞ্চলে সজ্জিত হয় এবং অ্যানাফেজে V, L, J, I আকারের ক্রোমোজোমীয় চলন দেখা যায়।",
            keyFormulaOrFact = "মাইটোসিস: 2n → 2n (দেহকোশ) | মিয়োসিস: 2n → n (গ্যামেট) | মানব ক্রোমোজোম: 44A + XX/XY | DNA: A,T,G,C | RNA: A,U,G,C",
            examQuestion = "কোশবিভাজনের কোন দশায় ক্রোমোজোমগুলি কোশের বিষুব অঞ্চলে সজ্জিত হয়?",
            options = listOf("প্রোফেজ", "মেটাফেজ", "অ্যানাফেজ", "টেলোফেজ"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ২: জনন, পরাগযোগ ও দ্বিনিষেক",
            title = "সপুষ্পক উদ্ভিদের দ্বিনিষেক, স্বপরাগযোগ-ইতরপরাগযোগ ও ফার্ণের জনুক্রম",
            marksWeight = 3,
            noteBn = "সপুষ্পক উদ্ভিদে পরাগনালী দিয়ে দুটি পুংগ্যামেট ভ্রূণস্থলীতে প্রবেশ করে: ১ম পুংগ্যামেট (n) + ডিম্বাণু (n) → জাইগোট (2n) এবং ২য় পুংগ্যামেট (n) + নির্ণীত নিউক্লিয়াস (2n) → শস্য নিউক্লিয়াস (3n); একে দ্বিনিষেক বলে। ফার্ণ (Dryopteris)-এর জীবনচক্রে রেণুধর (2n) ও লিঙ্গধর (n) দশার পর্যায়ক্রমিক আবর্তনকে জনুক্রম বলে।",
            boardTipBn = "জাইগোট (2n) ও শস্য নিউক্লিয়াস (3n)-এর প্লয়েডি সংখ্যা এবং বায়ু/জল/পতঙ্গপরাগী ফুলের উদাহরণ (ধান, পাতাঝাঁঝি, আম) মুখস্থ রাখবে।",
            keyFormulaOrFact = "জাইগোট = 2n (ডিম্বাণু + ১ম পুংগ্যামেট) | শস্য = 3n (নির্ণীত নিউক্লিয়াস + ২য় পুংগ্যামেট) | মাইক্রোপ্রোপাগেশন = কলাকর্ষণ",
            examQuestion = "সপুষ্পক উদ্ভিদের দ্বিনিষেকের ফলে উৎপন্ন শস্য নিউক্লিয়াসের ক্রোমোজোম সংখ্যা কত?",
            options = listOf("n", "2n", "3n", "4n"),
            correctIndex = 2
        ),
        SeedNote(
            chapter = "অধ্যায় ৩: বংশগতি (Heredity)",
            title = "মেন্ডেলের একসংকর ও দ্বিসংকর জনন, অসম্পূর্ণ প্রকটতা ও লিঙ্গ নির্ধারণ",
            marksWeight = 5,
            noteBn = "মেন্ডেলের মটরগাছে (Pisum sativum) একসংকর জননের F₂ জনুতে ফিনোটাইপ অনুপাত 3:1 ও জিনোটাইপ অনুপাত 1:2:1 (পৃথকীভবন সূত্র)। দ্বিসংকর জননে F₂ ফিনোটাইপ অনুপাত 9:3:3:1 (স্বাধীন বিন্যাস সূত্র)। সন্ধ্যামালতী ফুলে অসম্পূর্ণ প্রকটতায় F₂ ফিনোটাইপ ও জিনোটাইপ উভয়ই 1:2:1। মানুষের লিঙ্গ নির্ধারণে পিতার Y ক্রোমোজোম মুখ্য ভূমিকা নেয়।",
            boardTipBn = "চেকার বোর্ড এঁকে গ্যামেটগুলো বৃত্তের মধ্যে (○) দেখালে ও নিচে ফিনোটাইপ-জিনোটাইপ অনুপাত লিখলে ৫-এ ৫ নিশ্চিত!",
            keyFormulaOrFact = "একসংকর F₂: ফিনো 3:1, জিনো 1:2:1 | দ্বিসংকর F₂: ফিনো 9:3:3:1 | অসম্পূর্ণ প্রকটতা (সন্ধ্যামালতী): 1:2:1 | XX=কন্যা, XY=পুত্র",
            examQuestion = "সন্ধ্যামালতী উদ্ভিদে অসম্পূর্ণ প্রকটতার F₂ জনুতে ফিনোটাইপ ও জিনোটাইপ অনুপাত কী হয়?",
            options = listOf("3 : 1", "1 : 2 : 1", "9 : 3 : 3 : 1", "2 : 1"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৩: কয়েকটি সাধারণ জিনগত রোগ",
            title = "থ্যালাসেমিয়া, হিমোফিলিয়া ও বর্ণান্ধতা এবং জেনেটিক কাউন্সেলিং",
            marksWeight = 3,
            noteBn = "থ্যালাসেমিয়া একটি অটোজোমাল প্রচ্ছন্ন জিনগত রোগ (হিমোগ্লোবিনের α বা β পলিপেপটাইড শৃঙ্খল তৈরিতে ত্রুটি; বারবার রক্ত নেওয়ায় দেহে অতিরিক্ত লৌহ জমা হয়)। হিমোফিলিয়া (রক্ত তঞ্চন ব্যাহত — ফ্যাক্টর VIII/IX-এর অভাব) ও লাল-সবুজ বর্ণান্ধতা X-ক্রোমোজোমবাহিত প্রচ্ছন্ন রোগ। বিয়ের আগে জেনেটিক কাউন্সেলিং করে থ্যালাসেমিয়া প্রতিরোধ করা যায়।",
            boardTipBn = "থ্যালাসেমিয়া অটোজোমবাহিত, কিন্তু হিমোফিলিয়া ও বর্ণান্ধতা X-ক্রোমোজোমবাহিত—এই পার্থক্যটি বোর্ড MCQ-তে বারবার আসে।",
            keyFormulaOrFact = "থ্যালাসেমিয়া: অটোজোমাল প্রচ্ছন্ন (ক্রোমোজোম ১১ ও ১৬) | হিমোফিলিয়া A: ফ্যাক্টর VIII (X-বাহিত) | বর্ণান্ধতা: X-বাহিত প্রচ্ছন্ন",
            examQuestion = "নিম্নলিখিত কোন জিনগত রোগটি X-ক্রোমোজোমবাহিত নয়?",
            options = listOf("হিমোফিলিয়া A", "হিমোফিলিয়া B", "বর্ণান্ধতা", "থ্যালাসেমিয়া"),
            correctIndex = 3
        ),
        SeedNote(
            chapter = "অধ্যায় ৪: অভিব্যক্তি ও অভিযোজন",
            title = "ল্যামার্কবাদ, ডারউইনবাদ, সমসংস্থ-সমবৃত্তীয় অঙ্গ ও ঘোড়ার বিবর্তন",
            marksWeight = 5,
            noteBn = "ল্যামার্কের মূল প্রতিপাদ্য ‘অঙ্গের ব্যবহার ও অব্যবহার’ এবং ‘অর্জিত বৈশিষ্ট্যের বংশানুসরণ’। ডারউইনের প্রাকৃতিক নির্বাচনবাদে অত্যাধিক হারে বংশবৃদ্ধি, জীবনসংগ্রাম, প্রকরণ ও যোগ্যতমের উদ্বর্তন গুরুত্বপূর্ণ। সমসংস্থ অঙ্গ (পাখির ডানা ও মানুষের হাত — অপসারী বিবর্তন) এবং সমবৃত্তীয় অঙ্গ (পাখির ডানা ও পতঙ্গের ডানা — অভিসারী বিবর্তন)। ঘোড়ার আদি পূর্বপুরুষ ইওহিপ্পাস।",
            boardTipBn = "হৃৎপিণ্ডের প্রকোষ্ঠ বিবর্তন (মাছ ২ → উভচর ৩ → সরীসৃপ ৩½ → পাখি/স্তন্যপায়ী ৪) এবং মিলার-উরের পরীক্ষায় CH₄:NH₃:H₂ = 2:2:1 অনুপাত মনে রাখবে।",
            keyFormulaOrFact = "সমসংস্থ = উৎপত্তি এক, কাজ ভিন্ন (অপসারী) | সমবৃত্তীয় = কাজ এক, উৎপত্তি ভিন্ন (অভিসারী) | ঘোড়া: Eohippus → Equus",
            examQuestion = "পাখির ডানা ও পতঙ্গের ডানা কোন ধরনের অঙ্গের উদাহরণ?",
            options = listOf("সমসংস্থ অঙ্গ", "সমবৃত্তীয় অঙ্গ", "নিষ্ক্রিয় অঙ্গ", "আত্মরক্ষামূলক অঙ্গ"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৪: অভিযোজন (Adaptation)",
            title = "ক্যাকটাস, রুইমাছের পটকা, পায়রার বায়ুথলি, উটের RBC ও সুন্দরীর শ্বাসমূল",
            marksWeight = 3,
            noteBn = "ক্যাকটাসের পাতা কাঁটায় রূপান্তরিত (বাষ্পমোচন রোধ) ও কাণ্ড পর্ণকাণ্ডে পরিণত। রুইমাছের পটকার অগ্রপ্রকোষ্ঠে রেড গ্রন্থি গ্যাস উৎপন্ন করে (ভাসতে সাহায্য) ও পশ্চাৎ প্রকোষ্ঠে রেটিয়া মিরাবিলিয়া গ্যাস শোষণ করে (ডুবতে সাহায্য)। পায়রার ৯টি বায়ুথলি দ্বিশ্বসন ঘটায়। উটের ডিম্বাকার ও নিউক্লিয়াসযুক্ত RBC জলক্ষয় রোধ করে। সুন্দরীর শ্বাসমূলে (নিউম্যাটোফোর) শ্বাসছিদ্র থাকে।",
            boardTipBn = "পায়রার বায়ুথলির সংখ্যা (৯টি), মৌমাছির ওয়াগল নৃত্য (ইংরেজি ৮-এর মতো, ১০০ মিটারের বেশি দূরত্ব) ও শিম্পাঞ্জির উইপোকা শিকার—খুবই VVI!",
            keyFormulaOrFact = "পটকা: রেড গ্রন্থি (গ্যাস তৈরি), রেটিয়া মিরাবিলিয়া (শোষণ) | পায়রা: ৯টি বায়ুথলি | উটের RBC: ডিম্বাকার | সুন্দরী: শ্বাসমূল",
            examQuestion = "রুইমাছের পটকার কোন অংশটি গ্যাস উৎপন্ন করে মাছকে জলের উপরে ভাসতে সাহায্য করে?",
            options = listOf("রেড গ্রন্থি", "রেটিয়া মিরাবিলিয়া", "বায়ুথলি", "শ্বাসমূল"),
            correctIndex = 0
        ),
        SeedNote(
            chapter = "অধ্যায় ৫: পরিবেশ, তার সম্পদ এবং তাদের সংরক্ষণ",
            title = "নাইট্রোজেন চক্র, দূষণ, হটস্পট ও ইন-সিটু / এক্স-সিটু সংরক্ষণ",
            marksWeight = 5,
            noteBn = "নাইট্রোজেন চক্রে রাইজোবিয়াম ও অ্যাজোটোব্যাক্টর নাইট্রোজেন স্থিতিকরণ, নাইট্রোসোমোনাস-নাইট্রোব্যাক্টর নাইট্রিফিকেশন এবং সিউডোমোনাস ডিনাইট্রিফিকেশন ঘটায়। ভারতের ৪টি জীববৈচিত্র্য হটস্পটের মধ্যে পূর্ব হিমালয় ও সুন্দরল্যান্ড অন্যতম। ইন-সিটু সংরক্ষণ (জাতীয় উদ্যান, অভয়ারণ্য, বায়োস্ফিয়ার রিজার্ভ) এবং এক্স-সিটু সংরক্ষণ (চিড়িয়াখানা, বোটানিক্যাল গার্ডেন, ক্রায়োসংরক্ষণ -196°C তরল N₂)।",
            boardTipBn = "সুন্দরবনের পরিবেশগত সমস্যা, JFM (আড়াবাড়ি অরণ্য, পশ্চিম মেদিনীপুর) ও PBR (People’s Biodiversity Register) অবশ্যই পয়েন্ট করে লিখবে।",
            keyFormulaOrFact = "নাইট্রোজেন স্থিতিকরণ: Rhizobium | ডিনাইট্রিফিকেশন: Pseudomonas | ক্রায়োসংরক্ষণ: -196°C তরল N₂ | JFM: আড়াবাড়ি (১৯৭১)",
            examQuestion = "নিম্নলিখিত কোনটি এক্স-সিটু (Ex-situ) সংরক্ষণের উদাহরণ নয়?",
            options = listOf("চিড়িয়াখানা", "বোটানিক্যাল গার্ডেন", "ক্রায়োসংরক্ষণ", "জাতীয় উদ্যান (National Park)"),
            correctIndex = 3
        )
    )

    // ═══════════════════════════════════════════════════════════════════════
    // 6. ইতিহাস (HISTORY) — 10 High-Yield WBBSE Class 10 Sure-Shot Topics
    // ═══════════════════════════════════════════════════════════════════════
    private val historyNotes = listOf(
        SeedNote(
            chapter = "অধ্যায় ১: ইতিহাসের ধারণা",
            title = "নতুন সামাজিক ইতিহাস, আত্মজীবনী-স্মৃতিকথা ও সাময়িকপত্র",
            marksWeight = 4,
            noteBn = "১৯৬০-৭০-এর দশকে রাজা-মহারাজাদের বদলে সাধারণ মানুষ, খেলাধুলা, খাদ্য, পোশাক ও পরিবেশকে নিয়ে ‘নতুন সামাজিক ইতিহাস’ চর্চা শুরু হয়। আধুনিক ভারতের ইতিহাসের গুরুত্বপূর্ণ উপাদান হলো আত্মজীবনী—রবীন্দ্রনাথের ‘জীবনস্মৃতি’, সরলাদেবী চৌধুরানীর ‘জীবনের ঝরাপাতা’, বিপিনচন্দ্র পালের ‘সত্তর বৎসর’ এবং সাময়িকপত্র—বঙ্কিমচন্দ্রের ‘বঙ্গদর্শন’ (১৮৭২) ও দ্বারকানাথ বিদ্যাভূষণের ‘সোমপ্রকাশ’ (১৮৫৮)।",
            boardTipBn = "‘ইন্দিরাকে লেখা জওহরলাল নেহরুর চিঠি’ (Letters from a Father to His Daughter) ও ‘গ্রামবার্তা প্রকাশিকা’ (কাঙাল হরিনাথ)-র গুরুত্ব ৪ নম্বরে প্রায়ই আসে।",
            keyFormulaOrFact = "বঙ্গদর্শন: বঙ্কিমচন্দ্র (১৮৭২) | সোমপ্রকাশ: দ্বারকানাথ বিদ্যাভূষণ (১৮৫৮) | জীবনের ঝরাপাতা: সরলাদেবী | সত্তর বৎসর: বিপিনচন্দ্র পাল",
            examQuestion = "‘জীবনের ঝরাপাতা’ আত্মজীবনীটি কার রচনা?",
            options = listOf("রবীন্দ্রনাথ ঠাকুর", "সরলাদেবী চৌধুরানী", "বঙ্কিমচন্দ্র চট্টোপাধ্যায়", "বিপিনচন্দ্র পাল"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ২: সংস্কার: বৈশিষ্ট্য ও পর্যালোচনা",
            title = "ব্রাহ্মসমাজ, বিদ্যাসাগর, নব্যবঙ্গ ও রামকৃষ্ণদেবের সর্বধর্ম সমন্বয়",
            marksWeight = 4,
            noteBn = "রাজা রামমোহন রায় ১৮২৮ সালে ব্রাহ্মসভা ও ১৮৩০ সালে ব্রাহ্মসমাজ প্রতিষ্ঠা করেন এবং ১৮২৯ সালে সতীদাহ প্রথা রদ করান। ঈশ্বরচন্দ্র বিদ্যাসাগরের আন্দোলনে ১৮৫৬ সালে বিধবাবিবাহ আইন পাস হয়। হেনরি লুই ভিভিয়ান ডিরোজিওর অনুগামী নব্যবঙ্গ (Young Bengal) কুসংস্কারের বিরুদ্ধে লড়ে। শ্রীরামকৃষ্ণ ‘যত মত তত পথ’ বাণীতে সর্বধর্ম সমন্বয়ের আদর্শ প্রচার করেন।",
            boardTipBn = "বিবেকানন্দের ‘নব্য বেদান্ত’ ও হুতুম প্যাঁচার নকশা (কালীপ্রসন্ন সিংহ) এবং নীলদর্পণ (দীনবন্ধু মিত্র) থেকে ৪ নম্বরের টীকা খুব কমন।",
            keyFormulaOrFact = "সতীদাহ রদ: ১৮২৯ (লর্ড বেন্টিঙ্ক) | বিধবাবিবাহ আইন: ১৮৫৬ (বিদ্যাসাগর) | ব্রাহ্মসমাজ: ১৮৩০ | সর্বধর্ম সমন্বয়: ‘যত মত তত পথ’",
            examQuestion = "ঈশ্বরচন্দ্র বিদ্যাসাগরের প্রচেষ্টায় কত সালে বিধবাবিবাহ আইন পাস হয়েছিল?",
            options = listOf("১৮২৯ সালে", "১৮৫৬ সালে", "১৮৫৭ সালে", "১৮৭২ সালে"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ২: পাশ্চাত্য শিক্ষা ও চিকিৎসাশাস্ত্রের বিকাশ",
            title = "মেকলে মিনিট, উডের ডেসপ্যাচ, কলকাতা মেডিকেল কলেজ ও বিশ্ববিদ্যালয়",
            marksWeight = 4,
            noteBn = "১৮৩৫ সালের মেকলে মিনিটের মাধ্যমে ভারতে ইংরেজি ও পাশ্চাত্য শিক্ষাকে সরকারি নীতি হিসেবে গ্রহণ করা হয়। ১৮৫৪ সালের উডের ডেসপ্যাচকে ভারতে পাশ্চাত্য শিক্ষার ‘ম্যাগনা কার্টা’ বলা হয়; এর সুপারিশে ১৮৫৭ সালে কলকাতা বিশ্ববিদ্যালয় স্থাপিত হয়। ১৮৩৫ সালে কলকাতা মেডিকেল কলেজ স্থাপিত হলে ১৮৩৬ সালে মধুসূদন গুপ্ত প্রথম শবব্যবচ্ছেদ করেন।",
            boardTipBn = "বেথুন স্কুল (১৮৪৯), কাদম্বিনী গাঙ্গুলি ও চন্দ্রমুখী বসু (প্রথম মহিলা স্নাতক)—নারীশিক্ষার প্রশ্নে এই নামগুলো অবশ্যই লিখবে।",
            keyFormulaOrFact = "মেকলে মিনিট: ১৮৩৫ | কলকাতা মেডিকেল কলেজ: ১৮৩৫ (মধুসূদন গুপ্ত: ১৮৩৬) | উডের ডেসপ্যাচ: ১৮৫৪ | কলকাতা বিশ্ববিদ্যালয়: ১৮৫৭",
            examQuestion = "ভারতে পাশ্চাত্য শিক্ষার ‘ম্যাগনা কার্টা’ বলা হয় কোন দলিলকে?",
            options = listOf("মেকলে মিনিট (১৮৩৫)", "উডের ডেসপ্যাচ (১৮৫৪)", "হান্টার কমিশন (১৮৮২)", "সনদ আইন (১৮১৩)"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৩: প্রতিরোধ ও বিদ্রোহ",
            title = "সাঁওতাল হুল (১৮৫৫), মুণ্ডা বিদ্রোহ ও নীল বিদ্রোহ (১৮৫৯-৬০)",
            marksWeight = 8,
            noteBn = "দামিন-ই-কোহ অঞ্চলে ব্রিটিশ ও জমিদার-মহাজনদের শোষণের বিরুদ্ধে ১৮৫৫ সালে সিধু ও কানহুর নেতৃত্বে সাঁওতাল বিদ্রোহ (‘হুল’) ঘটে। ১৮৯৯-১৯০০ সালে বিরসা মুণ্ডার নেতৃত্বে ‘উলগুলান’ (মুণ্ডা বিদ্রোহ) হয়। ১৮৫৯-৬০ সালে দিগম্বর ও বিষ্ণুচরণ বিশ্বাসের নেতৃত্বে নীল বিদ্রোহে বাঙালি বুদ্ধিজীবী (হরিশচন্দ্র মুখোপাধ্যায় — হিন্দু প্যাট্রিয়ট, দীনবন্ধু মিত্র — নীলদর্পণ) সমর্থন জানান।",
            boardTipBn = "৮ নম্বরের প্রশ্নে নীল বিদ্রোহের কারণ, প্রকৃতি ও শিক্ষিত বাঙালি মধ্যবিত্ত শ্রেণির ভূমিকা পয়েন্ট করে লিখলে পুরো নম্বর মেলে।",
            keyFormulaOrFact = "সাঁওতাল হুল: ১৮৫৫ (সিধু-কানহু, দামিন-ই-কোহ) | উলগুলান: ১৮৯৯-১৯০০ (বিরসা মুণ্ডা) | নীল বিদ্রোহ: ১৮৫৯-৬০ (হিন্দু প্যাট্রিয়ট, নীলদর্পণ)",
            examQuestion = "১৮৫৫ সালের সাঁওতাল বিদ্রোহ (হুল)-এর প্রধান দুই নেতা কে ছিলেন?",
            options = listOf("দিগম্বর ও বিষ্ণুচরণ বিশ্বাস", "সিধু ও কানহু", "তিতুমির ও দুদু মিঞা", "বিরসা মুণ্ডা ও গঙ্গানারায়ণ"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৪: সংঘবদ্ধতার গোড়ার কথা (১৮৫৭ ও জাতীয়তাবাদ)",
            title = "মহাবিদ্রোহের (১৮৫৭) চরিত্র, মহারানির ঘোষণাপত্র ও ভারতসভা",
            marksWeight = 8,
            noteBn = "১৮৫৭ সালের মহাবিদ্রোহে (মঙ্গল পাণ্ডে, ঝাঁসির রানি লক্ষ্মীবাঈ) ব্রিটিশ ঐতিহাসিকরা একে ‘সিপাহী বিদ্রোহ’ বললেও বিনায়ক দামোদর সাভারকর একে ‘ভারতের প্রথম স্বাধীনতা যুদ্ধ’ বলেছেন। বিদ্রোহের পর ১৮৫৮ সালের ১ নভেম্বর মহারানি ভিক্টোরিয়ার ঘোষণাপত্রে ইস্ট ইন্ডিয়া কোম্পানির শাসনের অবসান ঘটে। ১৮৭৬ সালে সুরেন্দ্রনাথ বন্দ্যোপাধ্যায় ‘ভারতসভা’ (Indian Association) প্রতিষ্ঠা করেন।",
            boardTipBn = "জাতীয়তাবাদের বিকাশে বঙ্কিমচন্দ্রের ‘আনন্দমঠ’, বিবেকানন্দের ‘বর্তমান ভারত’, অবনীন্দ্রনাথের ‘ভারতমাতা’ চিত্র ও গগনেন্দ্রনাথের ব্যঙ্গচিত্র খুব গুরুত্বপূর্ণ।",
            keyFormulaOrFact = "মহাবিদ্রোহ: ১৮৫৭ | মহারানির ঘোষণাপত্র: ১ নভেম্বর ১৮৫৮ | ভারতসভা: ১৮৭৬ (সুরেন্দ্রনাথ) | ভারতমাতা চিত্র: অবনীন্দ্রনাথ ঠাকুর",
            examQuestion = "১৮৫৭ সালের বিদ্রোহকে ‘ভারতের প্রথম স্বাধীনতা যুদ্ধ’ বলে অভিহিত করেছিলেন কে?",
            options = listOf("রমেশচন্দ্র মজুমদার", "বিনায়ক দামোদর সাভারকর", "সুরেন্দ্রনাথ সেন", "জন লরেন্স"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৫: বিকল্প চিন্তা ও উদ্যোগ (ছাপাখানা ও বিজ্ঞানচর্চা)",
            title = "বাংলায় ছাপাখানার বিকাশ, IACS ও বসু বিজ্ঞান মন্দির",
            marksWeight = 4,
            noteBn = "১৭৭৮ সালে চার্লস উইলকিন্স ও পঞ্চানন কর্মকার প্রথম বাংলা ছাপার হরফ তৈরি করেন (হ্যালহেডের ‘এ গ্রামার অব দ্য বেঙ্গল ল্যাঙ্গুয়েজ’)। ১৮০০ সালে উইলিয়াম কেরির শ্রীরামপুর মিশন প্রেস এবং উপেন্দ্রকিশোর রায়চৌধুরীর ইউ. রায় অ্যান্ড সন্স (হাফটোন প্রিন্টিং) বিপ্লব আনে। বিজ্ঞানচর্চায় মহেন্দ্রলাল সরকারের IACS (১৮৭৬), জগদীশচন্দ্র বসুর বসু বিজ্ঞান মন্দির (১৯১৭) ও তারকনাথ পালিতের বেঙ্গল টেকনিক্যাল ইনস্টিটিউট (১৯০৬) অনন্য।",
            boardTipBn = "ছাপা বইয়ের সাথে শিক্ষাবিস্তারের সম্পর্ক এবং সি.ভি. রমনের IACS-এ নোবেলজয়ী আবিষ্কারের কথা উল্লেখ করলে উত্তর সমৃদ্ধ হয়।",
            keyFormulaOrFact = "বাংলা হরফ: ১৭৭৮ (উইলকিন্স ও পঞ্চানন কর্মকার) | শ্রীরামপুর প্রেস: ১৮০০ | IACS: ১৮৭৬ (মহেন্দ্রলাল) | বসু বিজ্ঞান মন্দির: ১৯১৭",
            examQuestion = "১৮৭৬ সালে ‘ইন্ডিয়ান অ্যাসোসিয়েশন ফর দ্য কাল্টিভেশন অব সায়েন্স’ (IACS) কে প্রতিষ্ঠা করেন?",
            options = listOf("জগদীশচন্দ্র বসু", "মহেন্দ্রলাল সরকার", "প্রফুল্লচন্দ্র রায়", "সত্যেন্দ্রনাথ বসু"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৫: প্রকৃতি, মানুষ ও শিক্ষার সমন্বয়",
            title = "রবীন্দ্রনাথের শান্তিনিকেতন, বিশ্বভারতী ও শ্রীনিকেতন ভাবনা",
            marksWeight = 4,
            noteBn = "ঔপনিবেশিক চার দেওয়ালের তোতাপাখির মতো শিক্ষাব্যবস্থার বিরোধিতা করে রবীন্দ্রনাথ ঠাকুর ১৯০১ সালে বোলপুরে প্রকৃতির কোলে ‘ব্রহ্মচর্যাশ্রম’ (শান্তিনিকেতন) প্রতিষ্ঠা করেন। ১৯২১ সালে ‘যত্র বিশ্বং ভবত্যেকনীড়ম্’ আদর্শে বিশ্বভারতী বিশ্ববিদ্যালয় গড়ে তোলেন এবং গ্রামোন্নয়ন ও বৃত্তিমূলক শিক্ষার জন্য ১৯২২ সালে শ্রীনিকেতন স্থাপন করেন।",
            boardTipBn = "প্রকৃতি, মানুষ ও শিক্ষার সমন্বয় এবং পাশ্চাত্য ও প্রাচ্য সংস্কৃতির মিলন—এই দুটি মূল পয়েন্ট ৪/৮ নম্বরের উত্তরে অবশ্যই থাকবে।",
            keyFormulaOrFact = "ব্রহ্মচর্যাশ্রম (শান্তিনিকেতন): ১৯০১ | বিশ্বভারতী: ১৯২১ | শ্রীনিকেতন (পল্লী পুনর্গঠন): ১৯২২ | মূল মন্ত্র: যত্র বিশ্বং ভবত্যেকনীড়ম্",
            examQuestion = "রবীন্দ্রনাথ ঠাকুর গ্রামোন্নয়ন ও বৃত্তিমূলক শিক্ষার উদ্দেশ্যে ১৯২২ সালে কী প্রতিষ্ঠা করেন?",
            options = listOf("জাতীয় শিক্ষা পরিষদ", "শ্রীনিকেতন", "বেঙ্গল টেকনিক্যাল ইনস্টিটিউট", "হিন্দু কলেজ"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৬: বিংশ শতকের ভারতের কৃষক, শ্রমিক ও বামপন্থী আন্দোলন",
            title = "বারদৌলি সত্যাগ্রহ (১৯২৮), মিরাট ষড়যন্ত্র মামলা ও বামপন্থী আন্দোলন",
            marksWeight = 4,
            noteBn = "১৯২৮ সালে গুজরাটের বারদৌলিতে খাজনা বৃদ্ধির প্রতিবাদে বল্লভভাই প্যাটেলের নেতৃত্বে সফল ‘বারদৌলি সত্যাগ্রহ’ হয়—এখান থেকেই তিনি ‘সর্দার’ উপাধি পান। ১৯২৯ সালে ব্রিটিশ সরকার কমিউনিস্ট ও শ্রমিক আন্দোলন দমনে ৩৩ জন নেতার বিরুদ্ধে ‘মিরাট ষড়যন্ত্র মামলা’ দায়ের করে। ১৯২০ সালে তাসখন্দে মানবেন্দ্রনাথ রায় ভারতের কমিউনিস্ট পার্টি ও ১৯৩৬ সালে স্বামী সহজানন্দ নিখিল ভারত কিষাণ সভা গঠন করেন।",
            boardTipBn = "বঙ্গভঙ্গ বিরোধী, অসহযোগ ও আইন অমান্য আন্দোলনে কৃষক ও শ্রমিকদের ভূমিকা তুলনামূলকভাবে মনে রাখবে।",
            keyFormulaOrFact = "বারদৌলি সত্যাগ্রহ: ১৯২৮ (সর্দার প্যাটেল) | AITUC: ১৯২০ (লালা লাজপত রায়) | মিরাট ষড়যন্ত্র মামলা: ১৯২৯ | কিষাণ সভা: ১৯৩৬",
            examQuestion = "কোন আন্দোলনে নেতৃত্ব দেওয়ার পর বল্লভভাই প্যাটেল ‘সর্দার’ উপাধি লাভ করেন?",
            options = listOf("চম্পারণ সত্যাগ্রহ", "বারদৌলি সত্যাগ্রহ", "একা আন্দোলন", "তেভাগা আন্দোলন"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৭: বিংশ শতকে নারী, ছাত্র ও প্রান্তিক জনগোষ্ঠীর আন্দোলন",
            title = "সশস্ত্র বিপ্লবী আন্দোলনে নারী ও ছাত্রসমাজ এবং দলিত-নমঃশূদ্র আন্দোলন",
            marksWeight = 8,
            noteBn = "বিনয়-বাদল-দীনেশের অলিন্দ যুদ্ধ (১৯৩০), দীপালি সংঘ (লীলা নাগ), মাস্টারদা সূর্য সেনের চট্টগ্রাম অস্ত্রাগার লুণ্ঠনে প্রীতিলতা ওয়াদ্দেদার ও কল্পনা দত্ত, বীণা দাসের স্ট্যানলি জ্যাকসনকে গুলি এবং ১৯৪২-এ মাতঙ্গিনী হাজরার আত্মবলিদান ইতিহাসে অমর। ১৯৪৬ সালে ১২ ফেব্রুয়ারি ‘রশিদ আলি দিবস’ পালিত হয়। দলিত আন্দোলনে গান্ধীজি ও বি.আর. আম্বেদকরের পুনা চুক্তি (১৯৩২) ও বাংলায় গুরুচাঁদ ঠাকুরের নমঃশূদ্র আন্দোলন উল্লেখযোগ্য।",
            boardTipBn = "সশস্ত্র বিপ্লবী আন্দোলনে নারীদের (প্রীতিলতা, কল্পনা দত্ত, বীণা দাস, শান্তি-সুনীতি) অবদান ৪ বা ৮ নম্বরের জন্য সুপার হট টপিক!",
            keyFormulaOrFact = "দীপালি সংঘ: লীলা নাগ (১৯২৩) | অলিন্দ যুদ্ধ: ৮ ডিসেম্বর ১৯৩০ | পুনা চুক্তি: ১৯৩২ | রশিদ আলি দিবস: ১২ ফেব্রুয়ারি ১৯৪৬",
            examQuestion = "নিম্নলিখিত কোন বিপ্লবী নারী ‘দীপালি সংঘ’ প্রতিষ্ঠা করেছিলেন?",
            options = listOf("প্রীতিলতা ওয়াদ্দেদার", "লীলা নাগ (রায়)", "বীণা দাস", "মাতঙ্গিনী হাজরা"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৮: উত্তর-ঔপনিবেশিক ভারত (১৯৪৭–১৯৬৪)",
            title = "দেশীয় রাজ্যের ভারতভুক্তি (হায়দরাবাদ, কাশ্মীর, জুনাগড়) ও উদ্বাস্তু সমস্যা",
            marksWeight = 4,
            noteBn = "স্বাধীনতার পর সর্দার বল্লভভাই প্যাটেল ও ভি.পি. মেননের কূটনীতিতে ৫৬০টির বেশি দেশীয় রাজ্য ‘ইনস্ট্রুমেন্ট অব অ্যাকসেশন’-এ সই করে ভারতে যোগ দেয়। জুনাগড়ে গণভোট, হায়দরাবাদে ‘অপারেশন পোলো’ (১৯৪৮) এবং কাশ্মীরে পাক হানাদার আক্রমণের পর মহারাজা হরি সিং ভারতভুক্তির দলিলে স্বাক্ষর করেন। দেশভাগের ফলে বাংলা ও পাঞ্জাবে ভয়াবহ উদ্বাস্তু সমস্যা ও ১৯৫০ সালে নেহরু-লিয়াকত চুক্তি ঘটে।",
            boardTipBn = "ভাষাভিত্তিক রাজ্য পুনর্গঠন আইন (১৯৫৬) ও উদ্বাস্তু পুনর্বাসনে দণ্ডকারণ্য প্রকল্প ও জবরদখল কলোনিগুলোর কথা লিখবে।",
            keyFormulaOrFact = "ভারতভুক্তির রূপকার: সর্দার প্যাটেল | হায়দরাবাদ: অপারেশন পোলো (১৯৪৮) | কাশ্মীর: মহারাজা হরি সিং | রাজ্য পুনর্গঠন: ১৯৫৬",
            examQuestion = "হায়দরাবাদ রাজ্যকে ভারতে যুক্ত করার জন্য কোন সামরিক অভিযান চালানো হয়েছিল?",
            options = listOf("অপারেশন বিজয়", "অপারেশন পোলো", "অপারেশন ব্লু স্টার", "অপারেশন ফ্লাড"),
            correctIndex = 1
        )
    )

    // ═══════════════════════════════════════════════════════════════════════
    // 7. ভূগোল (GEOGRAPHY) — 10 High-Yield WBBSE Class 10 Sure-Shot Topics
    // ═══════════════════════════════════════════════════════════════════════
    private val geographyNotes = listOf(
        SeedNote(
            chapter = "অধ্যায় ১: বহির্জাত প্রক্রিয়া — নদীর কার্য",
            title = "নদীর ক্ষয় ও সঞ্চয়কার্যের ফলে গঠিত প্রধান ভূমিরূপ (চিত্রসহ ৫ নম্বর)",
            marksWeight = 5,
            noteBn = "পার্বত্য প্রবাহে নদীর ক্ষয়কার্যে V-আকৃতির উপত্যকা, গিরিখাত ও শুষ্ক অঞ্চলে ক্যানিয়ন, জলপ্রপাত (ও নিচে প্রপাতকূপ) এবং শৃঙ্খলিত শৈলশিরা গঠিত হয়। মধ্য ও নিম্ন প্রবাহে সঞ্চয়কার্যের ফলে পলল ব্যজনী, প্লাবনভূমি, স্বাভাবিক বাঁধ, অশ্বক্ষুরাকৃতি হ্রদ ও মোহনায় বদ্বীপ (যেমন গঙ্গা-ব্রহ্মপুত্র বদ্বীপ) গঠিত হয়।",
            boardTipBn = "ভূগোলের ৫ নম্বরের প্রশ্নে অন্তত ৩টি ভূমিরূপের নাম, সংজ্ঞা, উৎপত্তি, উদাহরণ ও পাশে পেন্সিলে স্পষ্ট চিত্র আঁকলে ৫-এ ৫ নিশ্চিত!",
            keyFormulaOrFact = "ক্ষয়জাত: গিরিখাত, ক্যানিয়ন, জলপ্রপাত, মন্থকূপ | সঞ্চয়জাত: প্লাবনভূমি, অশ্বক্ষুরাকৃতি হ্রদ, বদ্বীপ | ‘ষষ্ঠঘাতের সূত্র’: বেগ ২ গুণ → বহনক্ষমতা ৬৪ গুণ",
            examQuestion = "নিম্নলিখিত কোন ভূমিরূপটি নদীর ক্ষয়কার্যের ফলে গঠিত নয়?",
            options = listOf("গিরিখাত", "মন্থকূপ", "জলপ্রপাত", "প্লাবনভূমি"),
            correctIndex = 3
        ),
        SeedNote(
            chapter = "অধ্যায় ১: হিমবাহ ও বায়ুর কার্য",
            title = "হিমবাহ ও বায়ুর ক্ষয়-সঞ্চয়জাত ভূমিরূপ (করি, ড্রামলিন, বার্খান, সিফ)",
            marksWeight = 5,
            noteBn = "হিমবাহের ক্ষয়কার্যে আরামকেদারার মতো করি/সার্ক, এরিটি, পিরামিড চূড়া, U-আকৃতির উপত্যকা, ঝুলন্ত উপত্যকা ও রসে মতানে গঠিত হয়; সঞ্চয়ে গ্রাবরেখা ও উল্টানো নৌকার মতো ড্রামলিন (‘বাস্কেট অব এগস টপোগ্রাফি’) সৃষ্টি হয়। মরু অঞ্চলে বায়ুর ক্ষয়ে গৌর, জিউগেন, ইয়ারদাং ও ইনসেলবার্জ এবং সঞ্চয়ে অর্ধচন্দ্রাকার বার্খান, অনুদৈর্ঘ্য সিফ বালিয়াড়ি ও লোয়েস সমভূমি গড়ে ওঠে।",
            boardTipBn = "বার্খান (বায়ুর সাথে আড়াআড়ি, অর্ধচন্দ্রাকার) বনাম সিফ বালিয়াড়ি (বায়ুর সমান্তরাল, তরবারির মতো)—চিত্রসহ পার্থক্য খুব VVI!",
            keyFormulaOrFact = "হিমবাহ: করি/সার্ক, U-উপত্যকা, ড্রামলিন (উল্টানো নৌকা) | বায়ু: গৌর, ইয়ারদাং, বার্খান (অর্ধচন্দ্রাকার), সিফ (অনুদৈর্ঘ্য)",
            examQuestion = "উল্টানো নৌকা বা চামচের মতো দেখতে হিমবাহের সঞ্চয়জাত ভূমিরূপকে কী বলে?",
            options = listOf("সার্ক", "ড্রামলিন", "বার্খান", "এরিটি"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ২: বায়ুমণ্ডল (Atmosphere)",
            title = "বায়ুমণ্ডলের স্তর, নিয়ত বায়ুপ্রবাহ ও শৈলোৎক্ষেপ বৃষ্টিপাত",
            marksWeight = 5,
            noteBn = "তাপমাত্রা অনুযায়ী বায়ুমণ্ডলের স্তর: ট্রপোস্ফিয়ার (মেঘ-বৃষ্টি), স্ট্র্যাটোস্ফিয়ার (ওজোন), মেসোস্ফিয়ার (উল্কা পোড়ে), থার্মোস্ফিয়ার (বেতার তরঙ্গ প্রতিফলন), এক্সোস্ফিয়ার। পৃথিবীর ৭টি চাপবলয়ের মধ্যে ৩টি নিয়ত বায়ু (আয়ন, পশ্চিমা, মেরু বায়ু) ফেরেলের সূত্র মেনে প্রবাহিত হয়। পর্বতের প্রতিবাত ঢালে শৈলোৎক্ষেপ বৃষ্টি এবং অনুবাত ঢালে বৃষ্টিচ্ছায় অঞ্চল (যেমন শিলং) তৈরি হয়।",
            boardTipBn = "উষ্ণতা-উচ্চতা লেখচিত্র ও চাপবলয়-নিয়ত বায়ুর গোলক চিত্র অনুশীলন করে রাখবে—প্রায় প্রতি বছর ৫ নম্বরে আসে।",
            keyFormulaOrFact = "স্বাভাবিক তাপহ্রাস হার: ৬.৪°C / ১০০০ মি | প্রতিবাত ঢাল = প্রচুর বৃষ্টি | অনুবাত ঢাল = বৃষ্টিচ্ছায় অঞ্চল (শিলং, পুনে)",
            examQuestion = "বায়ুমণ্ডলের কোন স্তরে বেতার তরঙ্গ প্রতিফলিত হয়ে পৃথিবীতে ফিরে আসে?",
            options = listOf("ট্রপোস্ফিয়ার", "স্ট্র্যাটোস্ফিয়ার", "আয়নোস্ফিয়ার (থার্মোস্ফিয়ার)", "মেসোস্ফিয়ার"),
            correctIndex = 2
        ),
        SeedNote(
            chapter = "অধ্যায় ৩: বারিমণ্ডল (Hydrosphere)",
            title = "সমুদ্রস্রোতের কারণ-প্রভাব ও জোয়ার-ভাটা (ভরা ও মরা কোটাল)",
            marksWeight = 5,
            noteBn = "নিয়ত বায়ুপ্রবাহ, পৃথিবীর আবর্তন (কোরিওলিস বল) ও সমুদ্রজলের উষ্ণতা-লবণতার তারতম্যে সমুদ্রস্রোত সৃষ্টি হয়। উষ্ণ ও শীতল স্রোতের মিলনস্থলে (যেমন নিউফাউন্ডল্যান্ডের গ্র্যান্ড ব্যাংক) মগ্নচড়া, ঘন কুয়াশা ও সমৃদ্ধ মৎস্যক্ষেত্র গড়ে ওঠে। অমাবস্যা ও পূর্ণিমায় সিজিগি অবস্থানে ‘ভরা কোটাল’ এবং শুক্ল ও কৃষ্ণপক্ষের অষ্টমী তিথিতে ‘মরা কোটাল’ হয়।",
            boardTipBn = "মুখ্য জোয়ার (চাঁদের মহাকর্ষ আকর্ষণ) ও গৌণ জোয়ার (পৃথিবীর আবর্তনজনিত কেন্দ্রাতিগ বল)—২টি মুখ্য জোয়ারের ব্যবধান ২৪ ঘণ্টা ৫২ মিনিট।",
            keyFormulaOrFact = "উষ্ণ+শীতল স্রোত = গ্র্যান্ড ব্যাংক (মৎস্যক্ষেত্র) | সিজিগি (অমাবস্যা/পূর্ণিমা) = ভরা কোটাল | অষ্টমী = মরা কোটাল | ব্যবধান: ২৪ ঘণ্টা ৫২ মি.",
            examQuestion = "একই স্থানে পরপর দুটি মুখ্য জোয়ারের মধ্যে সময়ের ব্যবধান কত?",
            options = listOf("১২ ঘণ্টা", "১২ ঘণ্টা ২৬ মিনিট", "২৪ ঘণ্টা", "২৪ ঘণ্টা ৫২ মিনিট"),
            correctIndex = 3
        ),
        SeedNote(
            chapter = "অধ্যায় ৪: বর্জ্য ব্যবস্থাপনা (Waste Management)",
            title = "জৈব ভঙ্গুর বনাম অভঙ্গুর বর্জ্য এবং 4R ও কম্পোস্টিং পদ্ধতি",
            marksWeight = 3,
            noteBn = "যেসব বর্জ্য জীবাণু দ্বারা বিশ্লিষ্ট হয়ে মাটিতে মিশে যায় তারা জৈব ভঙ্গুর (শাকসবজির খোসা, কাগজ); আর প্লাস্টিক, কাঁচ, ই-বর্জ্য হলো জৈব অভঙ্গুর। পরিবেশবান্ধব বর্জ্য ব্যবস্থাপনার মূল ভিত্তি 4R (Reduce, Reuse, Recycle, Refuse), স্ক্রাবার (বায়ুদূষণ রোধে), কম্পোস্টিং (জৈব সার) ও স্যানিটারি ল্যান্ডফিল।",
            boardTipBn = "লিচেট (আবর্জনা ধোয়া দূষিত জল), ইউট্রোফিকেশন ও জৈব-বিবর্ধন (Biomagnification)—২ নম্বরের সংজ্ঞার জন্য খুবই গুরুত্বপূর্ণ।",
            keyFormulaOrFact = "4R: Reduce, Reuse, Recycle, Refuse | গ্যাসীয় বর্জ্য শোধন: স্ক্রাবার | ই-বর্জ্য: পুরোনো মোবাইল/কম্পিউটার | জৈব সার: কম্পোস্টিং",
            examQuestion = "শিল্পকারখানার বিষাক্ত গ্যাসীয় বর্জ্য শোধনে কোন যন্ত্র ব্যবহৃত হয়?",
            options = listOf("স্যানিটারি ল্যান্ডফিল", "স্ক্রাবার (Scrubber)", "কম্পোস্টার", "থার্মোমিটার"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৫: ভারতের ভূপ্রকৃতি ও নদনদী",
            title = "হিমালয়ের প্রস্থ-দৈর্ঘ্য বিভাজন ও উত্তর বনাম দক্ষিণ ভারতের নদী",
            marksWeight = 5,
            noteBn = "প্রস্থ বরাবর হিমালয়: শিবালিক (দুন উপত্যকা), হিমাচল, হিমাদ্রি (সর্বোচ্চ শৃঙ্গ) ও টেথিস হিমালয়। দৈর্ঘ্য বরাবর: কাশ্মীর, কুমায়ুন (তাল হ্রদ), নেপাল ও অসম হিমালয়। উত্তর ভারতের নদীগুলি (গঙ্গা, সিন্ধু, ব্রহ্মপুত্র) বরফগলা জলে পুষ্ট নিত্যবহ; দক্ষিণ ভারতের নদীগুলি (গোদাবরী, কৃষ্ণা, কাবেরী) বৃষ্টির জলে পুষ্ট অনিত্যবহ (নর্মদা ও তাপ্তি গ্রস্ত উপত্যকা দিয়ে প্রবাহিত পশ্চিমবাহিনী নদী)।",
            boardTipBn = "পশ্চিমঘাট ও পূর্বঘাট পর্বতের পার্থক্য এবং গোদাবরী (‘দক্ষিণ ভারতের গঙ্গা’) ও নর্মদা-তাপ্তিতে বদ্বীপ না থাকার কারণ মনে রাখবে।",
            keyFormulaOrFact = "ভারতের সর্বোচ্চ শৃঙ্গ: গডউইন অস্টিন (K₂) | দক্ষিণ ভারতের দীর্ঘতম নদী: গোদাবরী | পশ্চিমবাহিনী: নর্মদা, তাপ্তি | কয়াল: মালাবার উপকূল",
            examQuestion = "নিম্নলিখিত কোন নদীটি পূর্ববাহিনী নয় (অর্থাৎ আরব সাগরে পড়েছে)?",
            options = listOf("গোদাবরী", "কৃষ্ণা", "কাবেরী", "নর্মদা"),
            correctIndex = 3
        ),
        SeedNote(
            chapter = "অধ্যায় ৫: ভারতের জলবায়ু, মৃত্তিকা ও স্বাভাবিক উদ্ভিদ",
            title = "ভারতের জলবায়ুতে মৌসুমি বায়ুর প্রভাব, কৃষ্ণ মৃত্তিকা ও ম্যানগ্রোভ অরণ্য",
            marksWeight = 5,
            noteBn = "ভারতের জলবায়ু ক্রান্তীয় মৌসুমি প্রকৃতির—দক্ষিণ-পশ্চিম মৌসুমি বায়ুতে গ্রীষ্মে বৃষ্টি হয় এবং করমণ্ডল উপকূলে (তামিলনাড়ু) বছরে দুবার বৃষ্টি হয়। দাক্ষিণাত্যের ব্যাসল্ট শিলা থেকে সৃষ্ট কৃষ্ণ মৃত্তিকা (রেগুর) কার্পাস চাষের সেরা। সুন্দরবনের লবণাক্ত মাটিতে শ্বাসমূল ও ঠেশমূলযুক্ত ম্যানগ্রোভ অরণ্য (সুন্দরী, গরান, গেঁওয়া) জন্মায়।",
            boardTipBn = "‘মৌসুমি বিস্ফোরণ’, ‘আশ্বিনের ঝড়’, ‘আম্রবৃষ্টি’ (দক্ষিণ ভারত) ও ‘কালবৈশাখী’ (পশ্চিমবঙ্গ)—১ ও ২ নম্বরের জন্য নিশ্চিত।",
            keyFormulaOrFact = "বছরে ২ বার বৃষ্টি: করমণ্ডল উপকূল | রেগুর (কৃষ্ণ মৃত্তিকা) = কার্পাস চাষ | ম্যানগ্রোভ: সুন্দরী, গরান | ভারতের অরণ্য গবেষণাগার: দেরাদুন",
            examQuestion = "ভারতের কোন উপকূলে বছরে দুবার (দক্ষিণ-পশ্চিম ও উত্তর-পূর্ব মৌসুমি বায়ুতে) বৃষ্টিপাত হয়?",
            options = listOf("মালাবার উপকূল", "কোঙ্কন উপকূল", "করমণ্ডল উপকূল", "কাথিয়াওয়াড় উপকূল"),
            correctIndex = 2
        ),
        SeedNote(
            chapter = "অধ্যায় ৫: ভারতের কৃষি (Agriculture)",
            title = "ধান, গম, চা ও কার্পাস চাষের অনুকূল ভৌগোলিক পরিবেশ",
            marksWeight = 5,
            noteBn = "ধান (খারিফ): উষ্ণতা ২০°-৩০°C, বৃষ্টিপাত ১০০-২০০ সেমি, পলিমাটি (শীর্ষ রাজ্য: পশ্চিমবঙ্গ)। গম (রবি): উষ্ণতা ১৫°-২০°C, বৃষ্টি ৫০-১০০ সেমি (শীর্ষ: উত্তরপ্রদেশ)। চা: উষ্ণতা ২০°-৩০°C, বৃষ্টি ২০০-২৫০ সেমি, ঢালু পার্বত্য জমি যাতে গোড়ায় জল না জমে (শীর্ষ: অসম)। কার্পাস: ২১°-৩০°C, ৫০০-১০০ সেমি বৃষ্টি, ২০০টি তুষারমুক্ত দিন ও কৃষ্ণ মৃত্তিকা।",
            boardTipBn = "৫ নম্বরের কৃষি প্রশ্নে ২টি মূল হেডিং দেবে: (১) প্রাকৃতিক পরিবেশ (উষ্ণতা, বৃষ্টি, মৃত্তিকা, ভূপ্রকৃতি) এবং (২) অর্থনৈতিক পরিবেশ (শ্রমিক, মূলধন, বাজার)।",
            keyFormulaOrFact = "ধান: ২০°-৩০°C, ১০০-২০০ সেমি | গম: ১৫°-২০°C, ৫০-১০০ সেমি | চা: ঢালু জমি, ২০০+ সেমি | কার্পাস: ২০০ তুষারমুক্ত দিন + রেগুর",
            examQuestion = "কার্পাস (তুলা) চাষের জন্য কমপক্ষে কত দিন তুষারমুক্ত আবহাওয়া প্রয়োজন?",
            options = listOf("৫০ দিন", "১০০ দিন", "২০০ দিন", "৩০০ দিন"),
            correctIndex = 2
        ),
        SeedNote(
            chapter = "অধ্যায় ৫: ভারতের শিল্প ও জনসংখ্যা",
            title = "লৌহ-ইস্পাত, কার্পাস বয়ন, পেট্রোরসায়ন শিল্প ও নগরায়ন",
            marksWeight = 5,
            noteBn = "পূর্ব ও মধ্য ভারতে (জামশেদপুর, দুর্গাপুর, ভিলাই, রৌরকেল্লা) কাঁচামাল (আকরিক লোহা, কয়লা, চুনাপাথর) সহজলভ্য হওয়ায় লৌহ-ইস্পাত শিল্প গড়ে উঠেছে। পশ্চিম ভারতে (মুম্বই, আহমেদাবাদ) আর্দ্র জলবায়ু, বন্দর ও কৃষ্ণ মৃত্তিকার তুলোর জন্য কার্পাস বয়ন শিল্প কেন্দ্রীভূত (আহমেদাবাদ = ভারতের ম্যানচেস্টার)। পেট্রোরসায়ন শিল্পকে ‘আধুনিক শিল্পদানব’ বলে।",
            boardTipBn = "ভারতের বৃহত্তম পেট্রোরসায়ন কেন্দ্র জামনগর, দুর্গাপুরকে ‘ভারতের রূঢ়’ এবং বেঙ্গালুরুকে ‘ভারতের সিলিকন ভ্যালি’ বলা হয়।",
            keyFormulaOrFact = "ভারতের ম্যানচেস্টার: আহমেদাবাদ | ভারতের রূঢ়: দুর্গাপুর | ডেট্রয়েট: চেন্নাই | সিলিকন ভ্যালি: বেঙ্গালুরু | শিল্পদানব: পেট্রোরসায়ন",
            examQuestion = "কোন শিল্পকে ‘আধুনিক শিল্পদানব’ বা ‘উদীয়মান শিল্প’ (Sunrise Industry) বলা হয়?",
            options = listOf("পাট শিল্প", "পেট্রোরসায়ন শিল্প", "চা শিল্প", "কাগজ শিল্প"),
            correctIndex = 1
        ),
        SeedNote(
            chapter = "অধ্যায় ৬: উপগ্রহ চিত্র, ভূবৈচিত্র্যসূচক মানচিত্র ও ম্যাপ পয়েন্টিং",
            title = "টপোশিট (R.F. স্কেল), উপগ্রহ চিত্র (FCC) ও ভারতের ম্যাপ পয়েন্টিং (১০ নম্বর)",
            marksWeight = 5,
            noteBn = "ভূবৈচিত্র্যসূচক মানচিত্র (Topographical Map) Survey of India প্রকাশ করে (মিলিয়ন শিট ১:১০,০০,০০০; ইঞ্চি শিট ১:৫০,০০০)। উপগ্রহ চিত্রে পিক্সেল ও ছদ্ম রঙ (FCC — অরণ্য লাল, জল নীল/কালো, বসতি নীলাভ-ধূসর) ব্যবহৃত হয়; সূর্যসমলয় উপগ্রহ (IRS) ও ভূসমলয় উপগ্রহ (INSAT) প্রধান। ভারতের ম্যাপ পয়েন্টিংয়ে ১০ নম্বর নিশ্চিত—সঠিক প্রতীক ও ইনডেক্স বক্স দিতে হবে।",
            boardTipBn = "ম্যাপ পয়েন্টিংয়ে পর্বত (▲), নদী (〰), বন্দর (⚓), শিল্পাঞ্চল (■) চিহ্ন ব্যবহার করে পাশে সূচক (Index) দিলে ১০-এ ১০ পাওয়া সহজ হয়!",
            keyFormulaOrFact = "SOI সদর দপ্তর: দেরাদুন | ইঞ্চি শিট R.F.: 1:50,000 | FCC: উদ্ভিদ = লাল, জল = নীল/কালো | IRS = সূর্যসমলয় | INSAT = ভূসমলয়",
            examQuestion = "উপগ্রহ চিত্রের ছদ্ম রঙে (FCC) গভীর বনভূমি বা স্বাভাবিক উদ্ভিদ কোন রঙে দেখানো হয়?",
            options = listOf("সবুজ", "লাল", "হলুদ", "সাদা"),
            correctIndex = 1
        )
    )
}
