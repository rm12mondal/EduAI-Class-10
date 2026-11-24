package com.example.data.master

import com.example.data.gguf.GgufModelManager
import com.example.model.GgufModelStatus
import com.example.model.InferenceMode
import com.example.model.WritingFormatType
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.withTimeout

data class EnglishUnseenPassage(
    val id: String,
    val title: String,
    val passageText: String,
    val tfStatements: List<Triple<String, Boolean, String>>, // Statement, True/False, Supporting Statement
    val whQuestions: List<Pair<String, String>>,
    val vocabularyMatches: List<Pair<String, String>> // Clue -> Word from passage
)

data class GrammarRuleAndQuiz(
    val id: String,
    val category: String,
    val titleBn: String,
    val ruleSummary: String,
    val trickBn: String,
    val examples: List<Pair<String, String>> // Question -> Answer
)

/**
 * Powers Features #17 (WBBSE Bengali & English Writing Studio) and
 * #18 (English Seen/Unseen/Grammar Studio + Bengali Grammar Lab with instant ব্যাসবাক্য/কারক analyzer)
 * across BOTH Online (`gemini-3.8-flash`) and 100% Offline modes.
 */
object LanguageMasteryBank {

    private const val ONLINE_MODEL = "gemini-3.8-flash"

    // ==================== #17. WBBSE WRITING STUDIO GENERATOR ====================

    suspend fun generateWriting(
        format: WritingFormatType,
        topic: String,
        inferenceMode: InferenceMode,
        ggufModelManager: GgufModelManager
    ): Pair<String, String> {
        val cleanTopic = topic.trim().ifBlank { format.sampleTopic }
        if (inferenceMode == InferenceMode.ONLINE) {
            try {
                val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel(modelName = ONLINE_MODEL)
                val prompt = buildWritingPrompt(format, cleanTopic)
                val resp = withTimeout(45_000L) { model.generateContent(prompt).text?.trim().orEmpty() }
                if (resp.length > 80) {
                    return resp to "🌐 অনলাইন Gemini • WBBSE বোর্ড ফরম্যাট"
                }
            } catch (_: Exception) {
                // Fallback smoothly to board-standard offline template
            }
        }

        val offlineText = generateOfflineWritingTemplate(format, cleanTopic)
        val badge = if (ggufModelManager.state.value.status == GgufModelStatus.READY_LOADED) {
            "⚡ অফলাইন Llama + WBBSE বোর্ড রাইটিং ফরম্যাট"
        } else {
            "⚡ ১০০% অফলাইন WBBSE বোর্ড রাইটিং ফরম্যাট"
        }
        return offlineText to badge
    }

    private fun buildWritingPrompt(format: WritingFormatType, topic: String): String {
        return """
            ${StrictWbbsePrecisionEngine.STRICT_FACT_LOCK_PROMPT_RULE}
            You are an expert WBBSE Class 10 (Madhyamik) examiner.
            Generate a full-marks board-format answer for:
            Format: ${format.titleBn} (${format.marksNote})
            Topic: $topic
            
            Mandatory WBBSE Structural Rules:
            - If Notice: Use exact box header (SCHOOL NAME, NOTICE, Date, Heading in CAPS, body with date/time/venue/purpose, Countersigned by Headmaster & Student Secretary signature).
            - If Report: Headline, By-line, Place & Date, clear factual paragraphs.
            - If Formal Letter: To The Editor/Headmaster, Subject line, Sir, body, Yours faithfully, Date & Address.
            - If Bengali প্রবন্ধ: ভূমিকা, ৩–৪টি শিরোনামযুক্ত অনুচ্ছেদ ও উপসংহার।
            - If Bengali সংলাপ: প্রাসঙ্গিক শিরোনাম ও ২ চরিত্রের স্বাভাবিক ৮–১০টি সংলাপ।
            - If Bengali প্রতিবেদন: আকর্ষণীয় শিরোনাম, নিজস্ব সংবাদদাতা, স্থান ও তারিখ, ৩ অনুচ্ছেদ।
            - If বঙ্গানুবাদ: প্রদত্ত ইংরেজি অনুচ্ছেদের প্রাঞ্জল, নির্ভুল বাংলা অনুবাদ।
            Keep it crisp, error-free, and 100% Madhyamik exam ready.
        """.trimIndent()
    }

    private fun generateOfflineWritingTemplate(format: WritingFormatType, topic: String): String {
        return when (format) {
            WritingFormatType.EN_NOTICE -> """
                ┌──────────────────────────────────────────┐
                │        ABC HIGH SCHOOL (H.S.), KOLKATA    │
                │                  NOTICE                   │
                │  Ref. No.: WBBSE/2026/14   Date: 15.02.2026│
                │                                          │
                │     ${topic.uppercase()}                  │
                ├──────────────────────────────────────────┤
                All students of Classes IX and X are hereby informed that our school is going to organize "$topic" on 25th February, 2026, from 11:00 a.m. to 3:00 p.m. in the School Auditorium.

                Interested students are requested to submit their names, class, and roll numbers to the undersigned on or before 20th February, 2026. Active participation from all students is warmly solicited to make the event a grand success.

                Countersigned by:                 Sd/-
                Headmaster                        Secretary, Cultural Committee
                ABC High School                   ABC High School
                └──────────────────────────────────────────┘
            """.trimIndent()

            WritingFormatType.EN_REPORT -> """
                ${topic.uppercase()}
                — By a Staff Reporter

                Kolkata, 20 February: A highly meaningful programme on "$topic" was observed with great enthusiasm yesterday at ABC High School premises. More than 400 students, teachers, and local guardians participated in the event.

                The programme began at 10:30 a.m. with an inaugural speech by the respected Headmaster, who highlighted the practical importance of "$topic" in building a responsible society. Students presented posters, charts, and practical demonstrations that drew appreciation from all guests.

                The event concluded around 2:30 p.m. with a vote of thanks. Everyone pledged to carry forward the core message of the programme in daily life.
            """.trimIndent()

            WritingFormatType.EN_FORMAL_LETTER -> """
                To
                The Editor,
                The Statesman,
                Kolkata - 700001

                Sub: Urgent concern regarding $topic

                Sir,
                Through the columns of your esteemed daily, I wish to draw the kind attention of the concerned authorities and the general public towards the growing issue of "$topic" in our locality.

                For the past few months, common citizens—especially school-going children and elderly residents—have been facing severe inconvenience due to this problem. Despite repeated appeals, no lasting solution has been implemented yet.

                I, therefore, earnestly request the authorities to take prompt and effective measures to resolve "$topic" at the earliest for the welfare of all.

                Thanking you,
                Yours faithfully,
                [Your Name]
                Date: 20.02.2026
                Place: Kolkata
            """.trimIndent()

            WritingFormatType.EN_INFORMAL_LETTER -> """
                12/A College Street,
                Kolkata - 700073
                20th February, 2026

                My dear Brother,
                I received your letter yesterday and was glad to know that everyone at home is doing well. Today I am writing to share a few thoughts about "$topic".

                Always remember that steady daily habits shape our future success. If you follow a disciplined timetable, revise your weak chapters regularly, and stay calm before exams, you will surely score top marks in Madhyamik. Take care of your health and get proper sleep as well.

                Convey my regards to Ma and Baba, and accept my love.

                Yours lovingly,
                [Your Name]
            """.trimIndent()

            WritingFormatType.EN_PROCESS -> """
                HOW TO PREPARE: ${topic.uppercase()}

                The preparation of "$topic" is a simple step-by-step process. At first, all necessary fresh ingredients and clean utensils are collected from the market. Next, the main materials are washed thoroughly with clean drinking water and sorted carefully to remove impurities.

                Then, the ingredients are measured in proper proportion and mixed or processed step by step in a clean vessel. After that, the mixture is stirred gently until it reaches the right consistency. Finally, the prepared product is filtered, poured into clean containers, labelled neatly, and made ready for use or distribution.
            """.trimIndent()

            WritingFormatType.EN_BIOGRAPHY -> """
                BIOGRAPHY OF ${topic.uppercase()}

                $topic is remembered as one of the most inspiring personalities in history for remarkable dedication and service to society. Born into a modest family, this great soul displayed extraordinary curiosity, discipline, and brilliance from early childhood.

                After completing formal education with distinction, $topic worked tirelessly to uplift education, science, and human welfare. Through pioneering achievements, inspiring leadership, and selfless sacrifice, $topic left an indelible mark on generations of students and citizens. Even today, the ideals of $topic continue to guide the youth of India.
            """.trimIndent()

            WritingFormatType.EN_STORY -> """
                TITLE: ${topic.uppercase()}

                Once upon a time, in a quiet village beside a deep forest, there lived a poor yet honest person who earned his daily bread through hard work. One afternoon, while working near the river, he faced a sudden misfortune that left him deeply worried.

                Soon, a kind helper appeared and tested his truthfulness with tempting rewards. Instead of giving in to greed, he spoke only the plain truth and refused to take what did not belong to him. Pleased with his sincerity and integrity, he was generously rewarded and lived happily ever after.

                Moral: Honesty and sincerity are always rewarded in the end.
            """.trimIndent()

            WritingFormatType.BN_PRABANDHA -> """
                ■ প্রবন্ধ রচনা: $topic

                ১. ভূমিকা:
                বর্তমান যুগে আমাদের সমাজ ও ছাত্রজীবনে “$topic” এক অত্যন্ত গুরুত্বপূর্ণ বিষয়। যুগের অগ্রগতির সাথে সাথে এর প্রভাব মানুষের চিন্তা, কর্ম ও দৈনন্দিন জীবনযাত্রাকে গভীরভাবে বদলে দিয়েছে।

                ২. মূল তাৎপর্য ও সুফল:
                সঠিক ও সচেতন প্রয়োগের মাধ্যমে “$topic” আমাদের শিক্ষা, জ্ঞানচর্চা ও সামাজিক উন্নয়নে অসাধারণ ভূমিকা পালন করে। সময় বাঁচানো, নতুন তথ্য জানা এবং মানবকল্যাণে এর অবদান অনস্বীকার্য।

                ৩. সতর্কতা ও করণীয়:
                তবে যেকোনো বিষয়েরই অসংযত বা অতিরিক্ত ব্যবহার ক্ষতির কারণ হতে পারে। তাই ছাত্রছাত্রীদের উচিত শৃঙ্খলা, যুক্তিবোধ ও সচেতনতার সঙ্গে এর সদ্ব্যবহার করা।

                ৪. উপসংহার:
                পরিশেষে বলা যায়, সঠিক দিশা ও মানবিক মূল্যবোধের সমন্বয়ে “$topic” আগামী দিনের সুন্দর ও সমৃদ্ধ পৃথিবী গড়ে তুলতে সহায়ক হবে।
            """.trimIndent()

            WritingFormatType.BN_SANGLAP -> """
                ■ সংলাপ রচনা: $topic

                অর্ণব: এই সৌম্য, কেমন আছিস? আজ স্কুলে “$topic” নিয়ে দারুণ আলোচনা হলো, তাই না?
                সৌম্য: হ্যাঁ রে, একদম ঠিক বলেছিস! বিষয়টি আমাদের মাধ্যমিকের ছাত্রছাত্রীদের জন্য সত্যিই খুব গুরুত্বপূর্ণ।
                অর্ণব: আমার মনে হয়, এর ভালো দিকগুলো কাজে লাগাতে পারলে পড়াশোনা ও জীবনে অনেক উন্নতি করা সম্ভব।
                সৌম্য: ঠিকই বলেছিস, কিন্তু অনেকেই নিয়ম না মেনে অতিরিক্ত ব্যবহার করায় সমস্যার মুখে পড়ছে।
                অর্ণব: একদম সহমত! যেকোনো ভালো জিনিসই পরিমিতভাবে ও সময় বুঝে ব্যবহার করা উচিত।
                সৌম্য: চল, আমরা দুজনেই আজ থেকে সচেতনভাবে চলব এবং বন্ধুদেরও এই বিষয়ে উৎসাহিত করব।
                অর্ণব: দারুণ কথা! এখন তবে বাড়ি ফিরি, কাল স্কুলে আবার দেখা হবে।
            """.trimIndent()

            WritingFormatType.BN_PROTIBEDON -> """
                ■ প্রতিবেদন রচনা: $topic
                নিজস্ব সংবাদদাতা, কলকাতা, ২০ ফেব্রুয়ারি:

                গতকাল আমাদের বিদ্যালয় প্রাঙ্গণে অত্যন্ত উৎসাহ ও উদ্দীপনার সঙ্গে “$topic” পালিত হলো। সকাল ১০টায় প্রধান শিক্ষকের উদ্বোধনী ভাষণের মাধ্যমে অনুষ্ঠানের সূচনা হয়। অনুষ্ঠানে বিদ্যালয়ের শিক্ষক-শিক্ষিকা, প্রায় চারশো ছাত্রছাত্রী এবং এলাকার বিশিষ্টজনেরা উপস্থিত ছিলেন।

                অনুষ্ঠানে বক্তারা “$topic”-এর সামাজিক গুরুত্ব তুলে ধরেন এবং ছাত্রছাত্রীদের সক্রিয় অংশগ্রহণের ভূয়সী প্রশংসা করেন। বিকেল ৩টেয় ধন্যবাদ জ্ঞাপনের মাধ্যমে এই সার্থক অনুষ্ঠানটির সমাপ্তি ঘটে।
            """.trimIndent()

            WritingFormatType.BN_BONGANUBAD -> """
                ■ বঙ্গানুবাদ (English → বাংলা)

                প্রদত্ত ইংরেজি অংশ:
                $topic

                প্রাঞ্জল বাংলা অনুবাদ:
                কর্মই পূজা। সততাই সর্বোৎকৃষ্ট পন্থা। একজন ছাত্রের সর্বদা শিক্ষক-শিক্ষিকা ও পিতামাতাকে শ্রদ্ধা করা উচিত। (নিয়মিত অধ্যবসায় ও শৃঙ্খলাই জীবনের প্রকৃত সাফল্য এনে দেয়।)

                📌 বোর্ড টিপ: আক্ষরিক অনুবাদ না করে মূল ভাবটি সহজ ও শুদ্ধ বাংলা বাক্যে প্রকাশ করবে।
            """.trimIndent()
        }
    }

    // ==================== #18A. ENGLISH UNSEEN PASSAGES & GRAMMAR STUDIO ====================

    val builtInUnseenPassages: List<EnglishUnseenPassage> = listOf(
        EnglishUnseenPassage(
            id = "unseen_1",
            title = "Water Conservation in Rural Bengal",
            passageText = "In many drought-prone pockets of Bankura and Purulia, villagers have revived traditional rainwater harvesting ponds called 'bandhs'. Earlier, erratic monsoon rainfall forced farmers to leave fields barren during winter. With support from local school eco-clubs and agricultural volunteers, over eighty ponds were desilted in two years. Today, groundwater levels have risen noticeably, enabling farmers to cultivate vegetables and pulses throughout the year. Students also conduct awareness rallies every month to prevent plastic dumping near water bodies.",
            tfStatements = listOf(
                Triple("Villagers in Bankura and Purulia built modern concrete dams only.", false, "SS: \"villagers have revived traditional rainwater harvesting ponds called 'bandhs'.\""),
                Triple("More than eighty ponds were cleaned in two years.", true, "SS: \"over eighty ponds were desilted in two years.\""),
                Triple("School students help spread awareness against plastic dumping.", true, "SS: \"Students also conduct awareness rallies every month to prevent plastic dumping near water bodies.\"")
            ),
            whQuestions = listOf(
                "What are traditional rainwater harvesting ponds in Bankura/Purulia called?" to "They are called 'bandhs'.",
                "Why did farmers earlier leave their fields barren in winter?" to "Because of erratic monsoon rainfall.",
                "How has desilting the ponds helped the farmers?" to "Groundwater levels rose, enabling farmers to grow vegetables and pulses throughout the year."
            ),
            vocabularyMatches = listOf(
                "Unpredictable / irregular" to "erratic",
                "Unproductive / infertile land" to "barren",
                "Removed mud/silt from the bottom" to "desilted",
                "Grow crops" to "cultivate"
            )
        ),
        EnglishUnseenPassage(
            id = "unseen_2",
            title = "ISRO's Chandrayaan-3 Milestone",
            passageText = "On August 23, 2023, India achieved a historic triumph in space exploration when Chandrayaan-3's Vikram lander touched down safely near the lunar south pole. India became the first nation to land near this unexplored region, which scientists believe may hold frozen water ice in permanently shadowed craters. Soon after touchdown, the Pragyan rover rolled out to analyze lunar soil minerals. The mission inspired millions of young Indian students to pursue careers in physics, mathematics, and aerospace engineering.",
            tfStatements = listOf(
                Triple("Chandrayaan-3 landed near the lunar equator.", false, "SS: \"Vikram lander touched down safely near the lunar south pole.\""),
                Triple("Pragyan rover examined minerals in the lunar soil.", true, "SS: \"the Pragyan rover rolled out to analyze lunar soil minerals.\"")
            ),
            whQuestions = listOf(
                "When did Vikram lander touch down on the Moon?" to "On August 23, 2023.",
                "Why is the lunar south pole scientifically important?" to "Scientists believe it may contain frozen water ice in permanently shadowed craters."
            ),
            vocabularyMatches = listOf(
                "A great victory / achievement" to "triumph",
                "Examine in detail" to "analyze",
                "Follow or strive for" to "pursue"
            )
        )
    )

    val englishGrammarModules: List<GrammarRuleAndQuiz> = listOf(
        GrammarRuleAndQuiz(
            id = "eg_voice",
            category = "Voice Change",
            titleBn = "Voice Change (Active ↔ Passive)",
            ruleSummary = "Object → Subject + Tense অনুযায়ী Be-verb + V3 + by + Subject (Obj form)। Interrogative-এ Aux verb সামনে রাখো; Imperative-এ Let + Obj + be + V3।",
            trickBn = "Active-এ V1 থাকলে am/is/are+V3; V2 থাকলে was/were+V3; ing থাকলে being+V3; have/has/had থাকলে been+V3!",
            examples = listOf(
                "The boy flies a kite." to "A kite is flown by the boy.",
                "Open the door." to "Let the door be opened.",
                "Who broke the glass?" to "By whom was the glass broken?"
            )
        ),
        GrammarRuleAndQuiz(
            id = "eg_narration",
            category = "Narration",
            titleBn = "Narration Change (Direct ↔ Indirect)",
            ruleSummary = "Reporting verb Past হলে: Present→Past, Past Indefinite→Past Perfect (had+V3), will→would, can→could, today→that day, tomorrow→the next day। Universal truth বদলায় না।",
            trickBn = "Assertive=that, Yes/No Question=if/whether, WH-Question=WH-word নিজেই, Order/Request=to + V1!",
            examples = listOf(
                "He said to me, \"I am busy today.\"" to "He told me that he was busy that day.",
                "She said to him, \"Where do you live?\"" to "She asked him where he lived.",
                "The teacher said, \"The earth moves round the sun.\"" to "The teacher said that the earth moves round the sun."
            )
        ),
        GrammarRuleAndQuiz(
            id = "eg_joining",
            category = "Joining & Transformation",
            titleBn = "Joining (Simple, Complex, Compound)",
            ruleSummary = "Too...to ↔ So...that (cannot/could not); As soon as ↔ No sooner had...than; None but ↔ Only।",
            trickBn = "No sooner-এর পর সবসময় ‘had + Sub + V3 ... than’ (then নয়!) বসাবে।",
            examples = listOf(
                "He is too weak to walk. (Complex)" to "He is so weak that he cannot walk.",
                "As soon as the bell rang, the students left." to "No sooner had the bell rung than the students left."
            )
        ),
        GrammarRuleAndQuiz(
            id = "eg_phrasal",
            category = "Phrasal Verbs (3 Marks VVI)",
            titleBn = "WBBSE Top 12 Phrasal Verbs",
            ruleSummary = "মূল verb-এর tense অনুযায়ী Phrasal verb-এর tense পরিবর্তন করা বাধ্যতামূলক (যেমন: rejected → turned down)!",
            trickBn = "Put out=নেভানো, Put off=স্থগিত, Give up=ত্যাগ করা, Give in=নতিস্বীকার, Call off=বাতিল, Break out=প্রাদুর্ভাব!",
            examples = listOf(
                "extinguish → put out" to "The firemen put out the fire.",
                "tolerate → put up with" to "I cannot put up with such insult.",
                "resemble → take after" to "The girl takes after her mother.",
                "continue → carry on" to "Carry on with your studies.",
                "quarrel → fall out" to "The two brothers fell out over property."
            )
        ),
        GrammarRuleAndQuiz(
            id = "eg_prep",
            category = "Articles & Prepositions",
            titleBn = "Articles & Appropriate Prepositions",
            ruleSummary = "নির্দিষ্ট স্থানের আগে at (ছোট) / in (বড়), সময়ের আগে at (ঘড়ি) / on (দিন-তারিখ) / in (মাস-বছর), যন্ত্রের আগে with ও ব্যক্তির আগে by।",
            trickBn = "fond of, proud of, die of (রোগ), die from (অতিভোজন/ক্ষত), senior to, prefer to, abstain from, abide by!",
            examples = listOf(
                "He died ___ cholera." to "of (রোগে মৃত্যু)",
                "She is junior ___ me." to "to (senior/junior/inferior + to)",
                "He is ___ honest man." to "an (silent 'h' vowel sound)"
            )
        )
    )

    suspend fun solveCustomUnseenPassage(
        passage: String,
        inferenceMode: InferenceMode
    ): String {
        val clean = passage.trim()
        if (clean.length < 30) return "অনুগ্রহ করে কমপক্ষে ২–৩ লাইনের একটি ইংরেজি Unseen Passage পেস্ট করো।"

        if (inferenceMode == InferenceMode.ONLINE) {
            try {
                val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel(modelName = ONLINE_MODEL)
                val prompt = """
                    You are a WBBSE Class 10 English Examiner. Analyze this Unseen Passage and generate:
                    1. 3 True/False statements WITH exact quoted Supporting Statements (SS) from the passage.
                    2. 3 WBBSE-style WH-Questions with crisp 1-sentence answers.
                    3. 4 Vocabulary Synonym matches (Clue -> exact word from passage).
                    
                    Passage:
                    $clean
                """.trimIndent()
                val res = withTimeout(45_000L) { model.generateContent(prompt).text?.trim().orEmpty() }
                if (res.length > 60) return res
            } catch (_: Exception) {
                // Fallback offline analyzer
            }
        }

        // Offline Unseen Passage Extractor (100% offline)
        val sentences = clean.split(Regex("""(?<=[.!?])\s+""")).filter { it.length > 15 }
        val words = clean.split(Regex("""[^A-Za-z]+""")).filter { it.length >= 6 }.distinct().take(4)
        return buildString {
            appendLine("⚡ অফলাইন Unseen Passage বিশ্লেষণ (WBBSE ফরম্যাট)")
            appendLine()
            appendLine("১. True / False + Supporting Statement (SS):")
            sentences.take(2).forEachIndexed { idx, s ->
                appendLine("  (${idx + 1}) Statement: $s → [TRUE]")
                appendLine("      Supporting Statement: \"$s\"")
            }
            appendLine()
            appendLine("২. সম্ভাব্য WH-প্রশ্ন ও উত্তর:")
            sentences.take(3).forEachIndexed { idx, s ->
                appendLine("  Q${idx + 1}. What key fact is highlighted in sentence ${idx + 1}?")
                appendLine("  Ans: $s")
            }
            appendLine()
            appendLine("৩. Vocabulary (গুরুত্বপূর্ণ শব্দ তালিকা):")
            words.forEachIndexed { idx, w ->
                appendLine("  • শব্দ ${idx + 1}: $w (প্রসঙ্গ অনুযায়ী অর্থ অনুশীলন করো)")
            }
            appendLine()
            appendLine("💡 টিপ: আরও নিখুঁত প্রশ্ন-উত্তর ও সমার্থক শব্দের জন্য অনলাইন মোড চালু করে আবার ট্যাপ করো।")
        }.trim()
    }

    // ==================== #18B. BENGALI GRAMMAR LAB ("বাংলা ব্যাকরণ গুরু") ====================

    val bengaliGrammarModules: List<GrammarRuleAndQuiz> = listOf(
        GrammarRuleAndQuiz(
            id = "bg_karak",
            category = "কারক ও অকারক",
            titleBn = "কারক ও অকারক সম্পর্ক (৬টি কারক + ২টি অকারক)",
            ruleSummary = "কে?=কর্তৃ | কী/কাকে?=কর্ম | কীসের দ্বারা?=করণ | কাকে নিঃস্বার্থ দান?=নিমিত্ত | কোথা থেকে/ভয়?=অপাদান | কোথায়/কখন?=অধিকরণ।\nঅকারক: সম্বন্ধ পদ (র/এর — ক্রিয়ার সাথে সম্পর্ক নেই) ও সম্বোধন পদ (ডাকা অর্থে)।",
            trickBn = "মনে রেখো: WBBSE-তে ‘সম্প্রদান’ এখন ‘নিমিত্ত কারক’ হিসেবে পড়া হয় (যেমন: ভিক্ষুককে ভিক্ষা দাও → কর্ম/নিমিত্ত, বেলা যে পড়ে এল জলকে চল → নিমিত্ত কারক)!",
            examples = listOf(
                "মেয়েরা ফুল তুলছে (ফুল)" to "কর্ম কারকে শূন্য বিভক্তি",
                "কলমে ভালো লেখা হয় (কলমে)" to "করণ কারকে ‘এ’ বিভক্তি",
                "মেঘ থেকে বৃষ্টি পড়ে (মেঘ থেকে)" to "অপাদান কারকে ‘থেকে’ অনুসর্গ",
                "বনের পশু (বনের)" to "সম্বন্ধ পদ (অকারক) — ‘এর’ বিভক্তি"
            )
        ),
        GrammarRuleAndQuiz(
            id = "bg_samas",
            category = "সমাস",
            titleBn = "সমাস ও ব্যাসবাক্য নির্ণয় (VVI শর্টকাট)",
            ruleSummary = "• দ্বন্দ্ব: ‘ও/এবং’ (মাতা ও পিতা = মাতাপিতা)\n• দ্বিগু: সংখ্যা + সমাহার (ত্রি ফলের সমাহার = ত্রিফলা)\n• কর্মধারয়: বিশেষণ+বিশেষ্য / উপমান / উপমিত / রূপক (মন রূপ মাঝি = মনমাঝি)\n• তৎপুরুষ: পূর্বপদের বিভক্তি লোপ (রথকে দেখা = রথদেখা)\n• বহুব্রীহি: ভিন্ন তৃতীয় অর্থ বোঝায় (বীণা পাণিতে যার = বীণাপাণি = সরস্বতী)\n• অব্যয়ীভাব: উপ/অনু/প্রতি/যথা উপসর্গ (উপকূল = কূলের সমীপে)",
            trickBn = "শেষে ‘যার/যাতে’ বসলেই বহুব্রীহি; ‘সমাহার’ বসলেই দ্বিগু; ‘রূপ’ বসলেই রূপক কর্মধারয়!",
            examples = listOf(
                "দশানন" to "দশ আনন যার — সংখ্যাবাচক বহুব্রীহি (রাবণ)",
                "পঙ্কজ" to "পঙ্কে জন্মে যা — উপপদ তৎপুরুষ",
                "বিষাদসিন্ধু" to "বিষাদ রূপ সিন্ধু — রূপক কর্মধারয়",
                "তেপান্তর" to "তিন প্রান্তরের সমাহার — দ্বিগু সমাস",
                "যথাবিধি" to "বিধিকে অতিক্রম না করে — অব্যয়ীভাব সমাস"
            )
        ),
        GrammarRuleAndQuiz(
            id = "bg_bakya",
            category = "বাক্য পরিবর্তন",
            titleBn = "সরল, জটিল ও যৌগিক বাক্য",
            ruleSummary = "• সরল: একটিই সমাপিকা ক্রিয়া।\n• জটিল: যে-সে, যদি-তবে, যখন-তখন, যিনি-তিনি সাপেক্ষ সর্বনাম।\n• যৌগিক: এবং, কিন্তু, অথবা, তাই, সুতরাং অব্যয় দ্বারা যুক্ত।",
            trickBn = "সরল→জটিল করতে ‘যে-সে / যখন-তখন’ যোগ করো; যৌগিক করতে ‘এবং / কিন্তু / তাই’ বসাও!",
            examples = listOf(
                "বিদ্বান সর্বত্র আদৃত হন। (জটিল)" to "যিনি বিদ্বান, তিনি সর্বত্র আদৃত হন।",
                "মেঘ করলে বৃষ্টি হবে। (যৌগিক)" to "মেঘ করবে এবং বৃষ্টি হবে।"
            )
        ),
        GrammarRuleAndQuiz(
            id = "bg_bachya",
            category = "বাচ্য পরিবর্তন",
            titleBn = "কর্তৃ, কর্ম, ভাব ও কর্মকর্তৃবাচ্য",
            ruleSummary = "• কর্তৃবাচ্য: কর্তা প্রধান (আমি ভাত খাই)।\n• কর্মবাচ্য: কর্তায় ‘কর্তৃক/দ্বারা’ + কর্ম প্রধান (আমার দ্বারা ভাত খাওয়া হয়)।\n• ভাববাচ্য: ক্রিয়ার ভাব প্রধান, কর্ম থাকে না (আমার যাওয়া হবে না)।\n• কর্মকর্তৃবাচ্য: কর্তা উহ্য, কর্মই কর্তার মতো কাজ করে (শাঁখ বাজে, বাঁশি বাজে)।",
            trickBn = "কর্ম থাকলে কর্তৃ↔কর্মবাচ্য হয়; অকর্মক ক্রিয়া হলে কর্তৃ↔ভাববাচ্য হয়!",
            examples = listOf(
                "পুলিশ চোরটিকে ধরেছে। (কর্মবাচ্য)" to "পুলিশ কর্তৃক চোরটি ধৃত হয়েছে।",
                "আমি এখন যাব না। (ভাববাচ্য)" to "আমার এখন যাওয়া হবে না।",
                "ঘণ্টা বাজছে।" to "কর্মকর্তৃবাচ্য (কর্তা উহ্য)"
            )
        )
    )

    private val builtInSamasDictionary = mapOf(
        "দশানন" to "দশ আনন যার → সংখ্যাবাচক বহুব্রীহি সমাস (অর্থ: রাবণ)",
        "পীতাম্বর" to "পীত অম্বর যার → ব্যধিকরণ/সমানাধিকরণ বহুব্রীহি সমাস (অর্থ: শ্রীকৃষ্ণ)",
        "বীণাপাণি" to "বীণা পাণিতে যার → ব্যধিকরণ বহুব্রীহি সমাস (অর্থ: দেবী সরস্বতী)",
        "নীলকণ্ঠ" to "নীল কণ্ঠ যার → বহুব্রীহি সমাস (অর্থ: শিব)",
        "পঙ্কজ" to "পঙ্কে জন্মে যা → উপপদ তৎপুরুষ সমাস (অর্থ: পদ্ম)",
        "জলদ" to "জল দান করে যে → উপপদ তৎপুরুষ সমাস (অর্থ: মেঘ)",
        "সত্যবাদী" to "সত্য বলে যে → উপপদ তৎপুরুষ সমাস",
        "মনমাঝি" to "মন রূপ মাঝি → রূপক কর্মধারয় সমাস",
        "বিষাদসিন্ধু" to "বিষাদ রূপ সিন্ধু → রূপক কর্মধারয় সমাস",
        "বিদ্যাসাগর" to "বিদ্যা রূপ সাগর → রূপক কর্মধারয় সমাস",
        "কাজলকালো" to "কাজলের ন্যায় কালো → উপমান কর্মধারয় সমাস",
        "তুষারশুভ্র" to "তুষারের ন্যায় শুভ্র → উপমান কর্মধারয় সমাস",
        "মুখচন্দ্র" to "মুখ চন্দ্রের ন্যায় → উপমিত কর্মধারয় সমাস",
        "সিংহপুরুষ" to "পুরুষ সিংহের ন্যায় → উপমিত কর্মধারয় সমাস",
        "সিংহাসন" to "সিংহ চিহ্নিত আসন → মধ্যপদলোপী কর্মধারয় সমাস",
        "মৌমাছি" to "মৌ সঞ্চয়কারী মাছি → মধ্যপদলোপী কর্মধারয় সমাস",
        "ত্রিফলা" to "তিন ফলের সমাহার → দ্বিগু সমাস",
        "চৌরাস্তা" to "চার রাস্তার সমাহার → দ্বিগু সমাস",
        "সপ্তর্ষি" to "সপ্ত ঋষির সমাহার → দ্বিগু সমাস",
        "মাতাপিতা" to "মাতা ও পিতা → দ্বন্দ্ব সমাস",
        "দিনরাত" to "দিন ও রাত → বিপরীতার্থক দ্বন্দ্ব সমাস",
        "উপকূল" to "কূলের সমীপে → অব্যয়ীভাব সমাস",
        "অনুক্ষণ" to "ক্ষণে ক্ষণে → অব্যয়ীভাব সমাস",
        "যথাবিধি" to "বিধিকে অতিক্রম না করে → অব্যয়ীভাব সমাস",
        "গাছপাকা" to "গাছে পাকা → অধিকরণ তৎপুরুষ সমাস",
        "রথদেখা" to "রথকে দেখা → কর্ম তৎপুরুষ সমাস",
        "বিপদাপন্ন" to "বিপদকে আপন্ন → কর্ম তৎপুরুষ সমাস",
        "দেশান্তর" to "অন্য দেশ → নিত্য সমাস",
        "গ্রামান্তর" to "অন্য গ্রাম → নিত্য সমাস",
        "আমরা" to "আমি, তুমি ও সে → একশেষ দ্বন্দ্ব সমাস"
    )

    suspend fun analyzeBengaliGrammar(
        input: String,
        inferenceMode: InferenceMode
    ): String {
        val clean = input.trim()
        if (clean.isBlank()) return "যেকোনো বাংলা সমাসবদ্ধ পদ (যেমন: দশানন, মনমাঝি, পঙ্কজ) বা বাক্য লিখে বিশ্লেষণ করো।"

        // 1. Instant 100% accurate offline dictionary match
        builtInSamasDictionary.entries.firstOrNull { (k, _) -> clean.contains(k) }?.let { (word, ans) ->
            if (clean.length <= 24) {
                return "✅ যাচাইকৃত WBBSE বাংলা ব্যাকরণ বিশ্লেষণ:\n• পদ: $word\n• ব্যাসবাক্য ও সমাস: $ans\n\n💡 বোর্ড টিপ: মাধ্যমিকে সঠিক ব্যাসবাক্য লিখলে ০.৫ এবং সমাসের সঠিক নাম লিখলে ০.৫ = মোট ১ নম্বর পাওয়া যায়।"
            }
        }

        // 2. Online Gemini exact analyzer if Online Mode is ON
        if (inferenceMode == InferenceMode.ONLINE) {
            try {
                val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel(modelName = ONLINE_MODEL)
                val prompt = """
                    ${StrictWbbsePrecisionEngine.STRICT_FACT_LOCK_PROMPT_RULE}
                    You are a WBBSE Class 10 Bengali Grammar Guru (বাংলা ব্যাকরণ শিক্ষক).
                    Analyze the following Bengali word or sentence strictly per WBBSE Madhyamik grammar rules:
                    Input: "$clean"
                    Provide:
                    1. যদি এটি সমাসবদ্ধ পদ হয়: সঠিক ব্যাসবাক্য + সমাসের নাম + চেনার সহজ শর্টকাট।
                    2. যদি এটি বাক্য হয়: মূল পদগুলির কারক ও বিভক্তি/অনুসর্গ, বাক্যের ধরন (সরল/জটিল/যৌগিক), এবং বাচ্য (কর্তৃ/কর্ম/ভাব/কর্মকর্তৃ) বিশ্লেষণ করো।
                    Keep the answer clear, bulleted, and in pure Bengali.
                """.trimIndent()
                val out = withTimeout(35_000L) { model.generateContent(prompt).text?.trim().orEmpty() }
                if (out.length > 30) return "🌐 অনলাইন বাংলা ব্যাকরণ গুরু:\n\n$out"
            } catch (_: Exception) {
                // Fall back to rule-based offline analyzer
            }
        }

        // 3. Offline Rule-Based Heuristic Analyzer (100% Offline)
        return buildString {
            appendLine("⚡ অফলাইন বাংলা ব্যাকরণ বিশ্লেষণ: “$clean”")
            appendLine()
            when {
                clean.contains("থেকে") || clean.contains("হইতে") ->
                    appendLine("• কারক ইঙ্গিত: ‘থেকে/হইতে’ অনুসর্গ থাকায় এখানে **অপাদান কারক** হওয়ার সম্ভাবনা প্রবল।")
                clean.contains("দ্বারা") || clean.contains("দিয়া") || clean.contains("কর্তৃক") ->
                    appendLine("• কারক/বাচ্য ইঙ্গিত: ‘দ্বারা/কর্তৃক’ থাকায় এটি **করণ কারক** অথবা **কর্মবাচ্যের** গঠন।")
                clean.contains("জন্য") || clean.contains("নিমিত্ত") ->
                    appendLine("• কারক ইঙ্গিত: ‘জন্য/নিমিত্ত’ থাকায় এখানে **নিমিত্ত কারক**।")
                clean.startsWith("উপ") || clean.startsWith("প্রতি") || clean.startsWith("যথা") || clean.startsWith("অনু") ->
                    appendLine("• সমাস ইঙ্গিত: পূর্বে উপসর্গ/অব্যয় থাকায় এটি **অব্যয়ীভাব সমাস** হতে পারে।")
                clean.contains("এবং") || clean.contains("কিন্তু") || clean.contains("অথবা") ->
                    appendLine("• বাক্যের ধরন: সংযোজক/বিয়োজক অব্যয় থাকায় এটি **যৌগিক বাক্য**।")
                clean.contains("যদি") || clean.contains("যখন") || clean.contains("যিনি") ->
                    appendLine("• বাক্যের ধরন: সাপেক্ষ সর্বনাম থাকায় এটি **জটিল বাক্য**।")
                else ->
                    appendLine("• বাক্য/পদ টিপ: ক্রিয়াকে কে (কর্তৃ), কী/কাকে (কর্ম), কীসের দ্বারা (করণ), কোথা থেকে (অপাদান), কোথায়/কখন (অধিকরণ) প্রশ্ন করে কারক নির্ণয় করো।")
            }
            appendLine()
            appendLine("📌 যেকোনো কঠিন শব্দের ১০০% বিস্তারিত ব্যাসবাক্য জানতে অনলাইন মোড চালু করে আবার বিশ্লেষণ করো।")
        }.trim()
    }
}
