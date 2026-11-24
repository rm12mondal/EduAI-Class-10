package com.example.data.master

import com.example.data.ai.ExamIntentRouter
import com.example.model.WbbseSubject

data class WbbseReferenceBadge(
    val labelBn: String,
    val isOutOfSyllabus: Boolean = false
)

/**
 * Automatic WBBSE Class 10 (Madhyamik) Syllabus Guard & Chapter Reference Resolver:
 * 1. Strictly declines out-of-syllabus questions (both Offline & Online) in Bengali:
 *    "এই প্রশ্নটি WBBSE ক্লাস ১০-এর সিলেবাসের অন্তর্ভুক্ত নয়"
 * 2. Resolves an accurate WBBSE Class 10 Subject + Chapter reference badge for every AI response card.
 */
object WbbseSyllabusGuard {

    const val OUT_OF_SYLLABUS_REFUSAL_PREFIX =
        "এই প্রশ্নটি WBBSE ক্লাস ১০-এর সিলেবাসের অন্তর্ভুক্ত নয়"

    const val OUT_OF_SYLLABUS_FULL_REPLY =
        "এই প্রশ্নটি WBBSE ক্লাস ১০-এর সিলেবাসের অন্তর্ভুক্ত নয়।\n\n" +
            "অনুগ্রহ করে পশ্চিমবঙ্গ মধ্যশিক্ষা পর্ষদের (WBBSE) ক্লাস ১০ মাধ্যমিক সিলেবাসের ৭টি বিষয়ের " +
            "(বাংলা, ইংরেজি, গণিত, ভৌত বিজ্ঞান, জীবন বিজ্ঞান, ইতিহাস বা ভূগোল) যেকোনো অধ্যায় থেকে প্রশ্ন করো।"

    const val STRICT_OUT_OF_SYLLABUS_PROMPT_RULE =
        "MANDATORY SYLLABUS BOUNDARY: You ONLY answer questions belonging to the West Bengal Board of Secondary Education (WBBSE) Class 10 (Madhyamik) syllabus across its 7 compulsory subjects (Bengali, English, Mathematics, Physical Science, Life Science, History, Geography). " +
            "If the user's question is clearly outside WBBSE Class 10 (for example: Class 11/12/college/JEE/NEET calculus, integration, differentiation, matrices, quantum mechanics, programming/coding, movies, sports scores, politics, celebrities, recipes, or unrelated general chit-chat), " +
            "you MUST STRICTLY refuse by replying ONLY in Bengali starting with the exact sentence: " +
            "\"এই প্রশ্নটি WBBSE ক্লাস ১০-এর সিলেবাসের অন্তর্ভুক্ত নয়।\" followed by one polite sentence asking them to ask from WBBSE Class 10 subjects. Do NOT answer the out-of-syllabus question."

    // Clearly out-of-syllabus higher-class / non-academic markers
    private val outOfSyllabusKeywords = listOf(
        // Higher Secondary / College / JEE / NEET Mathematics & Physics & Chemistry
        "calculus", "integration", "differentiation", "derivative", "eigenvalue", "eigenvector",
        "matrix multiplication", "determinant", "laplace transform", "fourier", "differential equation",
        "ক্যালকুলাস", "সমাকলন", "অবকলন", "ম্যাট্রিক্স", "নির্নায়ক", "নির্ণায়ক",
        "quantum mechanics", "schrodinger", "heisenberg uncertainty", "relativity", "black hole physics",
        "কোয়ান্টাম", "আপেক্ষিকতাবাদ", "কৃষ্ণগহ্বর", "গসের উপপাদ্য", "কার্শফের সূত্র", "kirchhoff",
        "gauss law", "maxwell equation", "thermodynamics entropy", "enthalpy", "gibbs free energy",
        "grignard", "aldol", "cannizzaro", "friedel-crafts", "friedel crafts", "sn1 reaction", "sn2 reaction",
        "hybridization sp3", "molecular orbital theory", "electrochemistry nernst",
        // Higher class / other board explicit tags
        "class 11", "class 12", "class xi", "class xii", "hs syllabus", "higher secondary",
        "ক্লাস ১১", "ক্লাস ১২", "একাদশ শ্রেণি", "দ্বাদশ শ্রেণি", "উচ্চ মাধ্যমিক",
        "iit jee", "jee mains", "jee advanced", "neet ug", "upsc", "wbcs", "gate exam",
        // Programming / Tech / Finance / Entertainment / Non-syllabus
        "python code", "java code", "javascript", "kotlin code", "c++ program", "html css",
        "react js", "sql query", "machine learning code", "প্রোগ্রামিং কোড", "পাইথন কোড",
        "bitcoin", "cryptocurrency", "share market", "stock price", "শেয়ার বাজার",
        "ipl score", "cricket match", "world cup winner", "football match", "ক্রিকেট স্কোর",
        "bollywood", "tollywood", "movie review", "web series", "সিনেমার রিভিউ",
        "biryani recipe", "cooking recipe", "বিরিয়ানি রান্না", "রান্নার রেসিপি",
        "joke বলো", "জোকস বলো", "tell me a joke", "love letter", "প্রেমপত্র", "horoscope", "রাশিফল"
    )

    // Guard against false positives if the query explicitly anchors to WBBSE Class 10 topics
    private val wbbseAnchorHints = listOf(
        "wbbse", "madhyamik", "মাধ্যমিক", "ক্লাস ১০", "class 10", "দশম শ্রেণি",
        "জ্ঞানচক্ষু", "অসুখী একজন", "আফ্রিকা", "বহুরূপী", "অভিষেক", "সিরাজদ্দৌলা", "পথের দাবী", "কোনি",
        "father's help", "runaway kite", "sea fever", "passing away of bapu",
        "বয়েল", "চার্লস", "ওহম", "স্নেল", "পর্যায় সারণি", "তড়িৎ বিশ্লেষণ",
        "মাইটোসিস", "মিয়োসিস", "অ্যানাফেজ", "অক্সিন", "প্রতিবর্ত", "মেন্ডেল", "নাইট্রোজেন চক্র",
        "শ্রীধর আচার্য", "পিথাগোরাস", "ত্রিকোণমিতি", "পরিমিতি", "বৃত্তস্থ",
        "মহাবিদ্রোহ", "সাঁওতাল", "নীল বিদ্রোহ", "স্বদেশী", "বঙ্গভঙ্গ",
        "হিমবাহ", "বার্খান", "বৃষ্টিচ্ছায়", "মৌসুমি", "সুন্দরবন", "বর্জ্য ব্যবস্থাপনা"
    )

    fun isClearlyOutOfSyllabus(query: String): Boolean {
        val lower = query.lowercase().trim()
        if (lower.length < 3) return false
        if (wbbseAnchorHints.any { lower.contains(it) }) return false
        return outOfSyllabusKeywords.any { lower.contains(it) }
    }

    private data class ChapterMatcher(
        val subject: WbbseSubject,
        val chapterBn: String,
        val keywords: List<String>
    )

    private val chapterMatchers = listOf(
        // Physical Science
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "পরিবেশের জন্য ভাবনা", listOf("বায়ুমণ্ডল", "ট্রপোস্ফিয়ার", "ওজোন", "গ্রিনহাউস", "মিথেন হাইড্রেট", "জীবাশ্ম", "ozone", "greenhouse")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "গ্যাসের আচরণ", listOf("বয়েল", "চার্লস", "অ্যাভোগাড্রো", "আদর্শ গ্যাস", "পরম শূন্য", "pv=nrt", "boyle", "charles")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "রাসায়নিক গণনা", listOf("মোল", "আণবিক গুরুত্ব", "বাষ্পঘনত্ব", "রাসায়নিক গণনা", "stoichiometry")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "তাপের ঘটনাসমূহ", listOf("তাপীয় প্রসারণ", "তাপ পরিবাহিতা", "দৈর্ঘ্য প্রসারণ", "thermal expansion")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "আলো", listOf("আলো", "দর্পণ", "লেন্স", "প্রতিসরণ", "স্নেল", "প্রিজম", "বিচ্ছুরণ", "ফোকাস", "snell", "lens", "prism", "refraction")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "চলতড়িৎ", listOf("ওহম", "রোধ", "বিভব প্রভেদ", "তড়িৎ প্রবাহ", "জুলের সূত্র", "ফ্লেমিং", "বার্লো", "অ্যাম্পিয়ার", "কুলম্ব", "ohm", "resistance", "joule")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "পরমাণুর নিউক্লিয়াস", listOf("তেজস্ক্রিয়তা", "নিউক্লীয় বিভাজন", "নিউক্লীয় সংযোজন", "আলফা", "বিটা", "গামা", "radioactivity")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "পর্যায় সারণি ও মৌলদের ধর্ম", listOf("পর্যায় সারণি", "মেন্ডেলিফ", "মোজলে", "তড়িৎ ঋণাত্মকতা", "আয়নন বিভব", "periodic table", "mendeleev")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "আয়নীয় ও সমযোজী বন্ধন", listOf("আয়নীয় বন্ধন", "সমযোজী বন্ধন", "তড়িৎযোজী", "ionic bond", "covalent")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "তড়িৎ প্রবাহ ও রাসায়নিক বিক্রিয়া", listOf("তড়িৎ বিশ্লেষণ", "তড়িৎলেপন", "ক্যাথোড", "অ্যানোড", "electrolysis")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "অজৈব রসায়ন", listOf("অ্যামোনিয়া", "হাইড্রোজেন সালফাইড", "নাইট্রোজেন", "অসওয়াল্ড", "হেবার", "স্পর্শ পদ্ধতি", "ammonia", "haber")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "ধাতুবিদ্যা", listOf("ধাতুবিদ্যা", "আকরিক", "বক্সাইট", "হেমাটাইট", "সংকর ধাতু", "থার্মিট", "metallurgy")),
        ChapterMatcher(WbbseSubject.PHYSICAL_SCIENCE, "জৈব রসায়ন", listOf("জৈব রসায়ন", "মিথেন", "ইথিলিন", "অ্যাসিটিলিন", "কার্যকরী মূলক", "আইইউপিএসি", "iupac", "alkane", "alkene", "alkyne")),

        // Life Science
        ChapterMatcher(WbbseSubject.LIFE_SCIENCE, "জীবজগতে নিয়ন্ত্রণ ও সমন্বয়", listOf("অক্সিন", "জিব্বেরেলিন", "সাইটোকাইনিন", "ট্রপিক", "ন্যাস্টিক", "হরমোন", "নিউরোন", "প্রতিবর্ত", "অক্ষিগোলক", "সিন্যাপস", "auxin", "hormone", "neuron", "reflex")),
        ChapterMatcher(WbbseSubject.LIFE_SCIENCE, "জীবনের প্রবহমানতা", listOf("মাইটোসিস", "মিয়োসিস", "অ্যানাফেজ", "মেটাফেজ", "ক্রোমোজোম", "ডিএনএ", "dna", "কোশ চক্র", "পরাগযোগ", "নিষেক", "mitosis", "meiosis", "anaphase")),
        ChapterMatcher(WbbseSubject.LIFE_SCIENCE, "বংশগতি ও জিনগত রোগ", listOf("মেন্ডেল", "একসংকর", "দ্বিসংকর", "থ্যালাসেমিয়া", "হিমোফিলিয়া", "বর্ণান্ধতা", "চেকার বোর্ড", "mendel", "genetics", "thalassemia")),
        ChapterMatcher(WbbseSubject.LIFE_SCIENCE, "অভিব্যক্তি ও অভিযোজন", listOf("ল্যামার্ক", "ডারউইন", "যোগ্যতমের উদবর্তন", "অভিব্যক্তি", "অভিযোজন", "সুন্দরী", "উট", "পায়রা", "darwin", "lamarck", "evolution")),
        ChapterMatcher(WbbseSubject.LIFE_SCIENCE, "পরিবেশ, তার সম্পদ ও সংরক্ষণ", listOf("নাইট্রোজেন চক্র", "জীববৈচিত্র্য", "হটস্পট", "জলদূষণ", "বায়ুদূষণ", "রেড ডেটা", "ইন-সিটু", "এক্স-সিটু", "nitrogen cycle", "biodiversity")),

        // Mathematics
        ChapterMatcher(WbbseSubject.MATHEMATICS, "একচলবিশিষ্ট দ্বিঘাত সমীকরণ", listOf("দ্বিঘাত", "শ্রীধর আচার্য", "নিরূপক", "বীজদ্বয়", "quadratic", "sridhar")),
        ChapterMatcher(WbbseSubject.MATHEMATICS, "সরল ও চক্রবৃদ্ধি সুদ", listOf("সরল সুদ", "চক্রবৃদ্ধি সুদ", "সমূল চক্রবৃদ্ধি", "অংশীদারি কারবার", "simple interest", "compound interest")),
        ChapterMatcher(WbbseSubject.MATHEMATICS, "বৃত্ত সম্পর্কিত উপপাদ্য", listOf("বৃত্তস্থ কোণ", "কেন্দ্রস্থ কোণ", "বৃত্তস্থ চতুর্ভুজ", "স্পর্শক", "জ্যা", "উপপাদ্য", "theorem", "tangent", "circle")),
        ChapterMatcher(WbbseSubject.MATHEMATICS, "পিথাগোরাসের উপপাদ্য ও সদৃশতা", listOf("পিথাগোরাস", "সদৃশকোণী", "সমকোণী ত্রিভুজ", "pythagoras", "similarity")),
        ChapterMatcher(WbbseSubject.MATHEMATICS, "পরিমিতি (চোঙ, গোলক, শঙ্কু)", listOf("গোলক", "শঙ্কু", "চোঙ", "আয়তঘন", "ঘনক", "পার্শ্বতল", "আয়তন", "sphere", "cone", "cylinder", "mensuration")),
        ChapterMatcher(WbbseSubject.MATHEMATICS, "ত্রিকোণমিতি ও উচ্চতা-দূরত্ব", listOf("ত্রিকোণমিতি", "sin", "cos", "tan", "পূরক কোণ", "উন্নতি কোণ", "অবনতি কোণ", "trigonometry")),
        ChapterMatcher(WbbseSubject.MATHEMATICS, "রাশিবিজ্ঞান", listOf("গড়", "মধ্যমা", "সংখ্যাগুরু মান", "ওজাইভ", "mean", "median", "mode", "ogive")),

        // History
        ChapterMatcher(WbbseSubject.HISTORY, "ইতিহাসের ধারণা", listOf("নতুন সামাজিক ইতিহাস", "আত্মজীবনী", "জীবনস্মৃতি", "বঙ্গদর্শন", "সোমপ্রকাশ")),
        ChapterMatcher(WbbseSubject.HISTORY, "সংস্কার: বৈশিষ্ট্য ও পর্যালোচনা", listOf("রামমোহন", "বিদ্যাসাগর", "সতীদাহ", "বিধবাবিবাহ", "ব্রাহ্মসমাজ", "হুতুম প্যাঁচার", "নীলদর্পণ", "নব্যবঙ্গ", "রামকৃষ্ণ", "বিবেকানন্দ")),
        ChapterMatcher(WbbseSubject.HISTORY, "প্রতিরোধ ও বিদ্রোহ", listOf("সাঁওতাল", "নীল বিদ্রোহ", "চুয়াড়", "কোল বিদ্রোহ", "মুন্ডা", "সন্ন্যাসী", "বারাসাত", "তিতুমীর", "ফরাজি")),
        ChapterMatcher(WbbseSubject.HISTORY, "সংঘবদ্ধতার গোড়ার কথা (১৮৫৭)", listOf("১৮৫৭", "মহাবিদ্রোহ", "সিপাহী বিদ্রোহ", "মহারানির ঘোষণাপত্র", "ভারতসভা", "হিন্দুমেলা", "আনন্দমঠ", "বর্তমান ভারত", "গগনেন্দ্রনাথ")),
        ChapterMatcher(WbbseSubject.HISTORY, "বিকল্প চিন্তা ও উদ্যোগ", listOf("ছাপাখানা", "শ্রীরামপুর", "উপেন্দ্রকিশোর", "বসু বিজ্ঞান মন্দির", "জাতীয় শিক্ষা পরিষদ", "বিশ্বভারতী", "রবীন্দ্রনাথ")),
        ChapterMatcher(WbbseSubject.HISTORY, "বিশ শতকের ভারতে কৃষক-শ্রমিক আন্দোলন", listOf("বঙ্গভঙ্গ", "অসহযোগ", "আইন অমান্য", "ভারত ছাড়ো", "বারদৌলি", "একা আন্দোলন", "তেভাগা", "মিরাট ষড়যন্ত্র")),
        ChapterMatcher(WbbseSubject.HISTORY, "বিশ শতকের ভারতে নারী-ছাত্র আন্দোলন", listOf("প্রীতিলতা", "মাতঙ্গিনী", "কল্পনা দত্ত", "বীণা দাস", "দীপালী সংঘ", "মাস্টারদা", "সূর্য সেন", "রশিদ আলি")),
        ChapterMatcher(WbbseSubject.HISTORY, "উত্তর-ঔপনিবেশিক ভারত", listOf("দেশীয় রাজ্য", "জুনাগড়", "হায়দরাবাদ", "কাশ্মীর", "উদ্বাস্তু", "ভাষাভিত্তিক রাজ্য")),

        // Geography
        ChapterMatcher(WbbseSubject.GEOGRAPHY, "বহির্জাত প্রক্রিয়া ও ভূমিরূপ", listOf("নদী", "জলপ্রপাত", "অশ্বক্ষুরাকৃতি", "বদ্বীপ", "হিমবাহ", "করি", "ঝুলন্ত উপত্যকা", "বার্খান", "ইনসেলবার্জ", "ইয়ার্দাং", "glacier", "river")),
        ChapterMatcher(WbbseSubject.GEOGRAPHY, "বায়ুমণ্ডল", listOf("বায়ুমণ্ডল", "অ্যালবেডো", "বৈপরীত্য উত্তাপ", "আয়ন বায়ু", "মৌসুমি", "ঘূর্ণবাত", "বৃষ্টিচ্ছায়", "শৈলোৎক্ষেপ")),
        ChapterMatcher(WbbseSubject.GEOGRAPHY, "বারিমণ্ডল", listOf("সমুদ্রস্রোত", "শৈবাল সাগর", "মগ্নচড়া", "জোয়ার", "ভাটা", "ভরা কোটাল", "মরা কোটাল", "সিজিগি")),
        ChapterMatcher(WbbseSubject.GEOGRAPHY, "বর্জ্য ব্যবস্থাপনা", listOf("বর্জ্য", "3r", "কম্পোস্টিং", "স্ক্রাবার", "ল্যাণ্ডফিল", "waste management")),
        ChapterMatcher(WbbseSubject.GEOGRAPHY, "ভারত (প্রাকৃতিক ও অর্থনৈতিক)", listOf("হিমালয়", "শিবালিক", "পশ্চিমঘাট", "গঙ্গা", "নর্মদা", "রেগুর", "কৃষ্ণমৃত্তিকা", "সুন্দরবন", "ম্যানগ্রোভ", "ধান", "গম", "চা", "লৌহ ইস্পাত", "পেট্রোরসায়ন")),
        ChapterMatcher(WbbseSubject.GEOGRAPHY, "উপগ্রহ চিত্র ও ভূবৈচিত্র্যসূচক মানচিত্র", listOf("উপগ্রহ চিত্র", "দূর সংবেদন", "টপোশিট", "ভূবৈচিত্র্যসূচক", "fcc", "remote sensing")),

        // Bengali
        ChapterMatcher(WbbseSubject.BENGALI, "সাহিত্য সঞ্চয়ন (গল্প/কবিতা/নাটক)", listOf("জ্ঞানচক্ষু", "তপন", "অসুখী একজন", "বেঁধে বেঁধে", "আফ্রিকা", "কালি কলম", "বহুরূপী", "হরিদা", "অভিষেক", "মেঘনাদ", "সিরাজদ্দৌলা", "প্রলয়োল্লাস", "পথের দাবী", "সব্যসাচী", "সিন্ধুতীরে", "অদল বদল", "অস্ত্রের বিরুদ্ধে গান", "নদীর বিদ্রোহ", "বাংলা ভাষায় বিজ্ঞান")),
        ChapterMatcher(WbbseSubject.BENGALI, "সহায়ক পাঠ — কোনি", listOf("কোনি", "ক্ষিতীশ", "ক্ষিদ্দা", "কমলদিঘি", "ফাইট কোনি")),
        ChapterMatcher(WbbseSubject.BENGALI, "বাংলা ব্যাকরণ ও নির্মিতি", listOf("কারক", "বিভক্তি", "সমাস", "ব্যাসবাক্য", "বাক্য পরিবর্তন", "বাচ্য", "প্রবন্ধ", "প্রতিবেদন", "সংলাপ", "বঙ্গানুবাদ")),

        // English
        ChapterMatcher(WbbseSubject.ENGLISH, "Bliss — Prose & Poetry", listOf("father's help", "swami", "samuel", "fable", "mountain and the squirrel", "passing away of bapu", "my own true family", "runaway kite", "sea fever", "the cat", "the snail")),
        ChapterMatcher(WbbseSubject.ENGLISH, "Grammar & Writing Skills", listOf("tense", "voice change", "narration", "phrasal verb", "joining", "splitting", "notice writing", "process writing", "biography", "story writing", "editorial letter", "paragraph"))
    )

    /**
     * Resolves a concise, accurate WBBSE Class 10 Subject + Chapter reference badge for every AI response card.
     */
    fun resolveBadge(
        answerText: String,
        subject: WbbseSubject?,
        chapterHint: String? = null
    ): WbbseReferenceBadge {
        if (answerText.contains(OUT_OF_SYLLABUS_REFUSAL_PREFIX)) {
            return WbbseReferenceBadge(
                labelBn = "🚫 WBBSE ক্লাস ১০ সিলেবাসের বাইরে",
                isOutOfSyllabus = true
            )
        }

        val lower = answerText.lowercase()
        val matched = chapterMatchers.firstOrNull { matcher ->
            (subject == null || matcher.subject == subject) &&
                matcher.keywords.any { kw -> lower.contains(kw) }
        } ?: chapterMatchers.firstOrNull { matcher ->
            matcher.keywords.any { kw -> lower.contains(kw) }
        }

        val resolvedSubject = subject ?: matched?.subject ?: ExamIntentRouter.detectSubject(answerText)
        val extractedChapter = chapterHint?.takeIf { it.isNotBlank() }
            ?: matched?.chapterBn
            ?: extractChapterFromHeader(answerText)

        val label = when {
            resolvedSubject != null && !extractedChapter.isNullOrBlank() ->
                "📘 WBBSE ক্লাস ১০ • ${resolvedSubject.bengaliName} — ${extractedChapter.take(30)}"
            resolvedSubject != null ->
                "📘 WBBSE ক্লাস ১০ • ${resolvedSubject.bengaliName} (${resolvedSubject.englishName})"
            !extractedChapter.isNullOrBlank() ->
                "📘 WBBSE ক্লাস ১০ • ${extractedChapter.take(34)}"
            else ->
                "📘 WBBSE ক্লাস ১০ সিলেবাস রেফারেন্স"
        }

        return WbbseReferenceBadge(labelBn = label, isOutOfSyllabus = false)
    }

    private fun extractChapterFromHeader(text: String): String? {
        val headerRegex = Regex("""(?:অধ্যায়(?:\s*রেফারেন্স)?\s*[:：]|—)\s*([^\n(]{3,36})""")
        val firstChunk = text.take(260)
        val found = headerRegex.find(firstChunk)?.groupValues?.getOrNull(1)?.trim()
        return found?.replace(Regex("[*#📘🔹📌]"), "")?.trim()?.takeIf { it.length >= 3 }
    }
}
