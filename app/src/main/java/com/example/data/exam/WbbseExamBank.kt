package com.example.data.exam

import com.example.model.LongQuestion
import com.example.model.McqQuestion
import com.example.model.SaqQuestion
import com.example.model.WbbseSubject

/**
 * Comprehensive WBBSE Class 10 (Madhyamik) Exam Question Bank covering all 7 compulsory subjects
 * across MCQ (5 per subject), SAQ (3 per subject), and Long/Descriptive (2 per subject).
 */
object WbbseExamBank {

    fun getMcqs(subject: WbbseSubject): List<McqQuestion> = when (subject) {
        WbbseSubject.BENGALI -> listOf(
            McqQuestion(
                id = "bng_mcq_1",
                subject = subject,
                question = "‘জ্ঞানচক্ষু’ গল্পে তপনের লেখা গল্পটি কোন পত্রিকায় ছাপা হয়েছিল?",
                options = listOf("সন্দেশ", "সন্ধ্যাতারা", "শুকতারা", "আনন্দমেলা"),
                correctIndex = 1,
                explanation = "তপনের ছোটোমেসো ‘সন্ধ্যাতারা’ পত্রিকার সম্পাদকের পরিচিত ছিলেন এবং সেখানেই ‘প্রথম দিন’ গল্পটি ছাপা হয়।"
            ),
            McqQuestion(
                id = "bng_mcq_2",
                subject = subject,
                question = "‘অসুখী একজন’ কবিতাটির বাংলা অনুবাদক কে?",
                options = listOf("শঙ্খ ঘোষ", "সুভাষ মুখোপাধ্যায়", "নবারুণ ভট্টাচার্য", "শক্তি চট্টোপাধ্যায়"),
                correctIndex = 2,
                explanation = "পাবলো নেরুদার মূল কবিতাটি নবারুণ ভট্টাচার্য ‘বিদেশী ফুলে রক্তের ছিটে’ গ্রন্থে বাংলায় অনুবাদ করেন।"
            ),
            McqQuestion(
                id = "bng_mcq_3",
                subject = subject,
                question = "‘আফ্রিকা’ কবিতাটি রবীন্দ্রনাথ ঠাকুরের কোন কাব্যগ্রন্থের অন্তর্গত?",
                options = listOf("গীতাঞ্জলি", "পত্রপুট", "বলাকা", "পুনশ্চ"),
                correctIndex = 1,
                explanation = "১৯৩৭ সালে রচিত ‘আফ্রিকা’ কবিতাটি রবীন্দ্রনাথের ‘পত্রপুট’ কাব্যগ্রন্থের দ্বিতীয় সংস্করণে সংকলিত হয়।"
            ),
            McqQuestion(
                id = "bng_mcq_4",
                subject = subject,
                question = "‘মেঘে বৃষ্টি হয়’—এখানে ‘মেঘে’ পদটি কোন কারক?",
                options = listOf("অধিকরণ কারক", "করণ কারক", "অপাদান কারক", "কর্ম কারক"),
                correctIndex = 2,
                explanation = "মেঘ থেকে বৃষ্টি পতিত বা বিচ্যুত হয়, তাই ‘মেঘে’ হলো অপাদান কারকে ‘এ’ বিভক্তি।"
            ),
            McqQuestion(
                id = "bng_mcq_5",
                subject = subject,
                question = "কোন সমাসে উভয় পদের অর্থ প্রাধান্য পায়?",
                options = listOf("তৎপুরুষ", "দ্বন্দ্ব", "বহুব্রীহি", "অব্যয়ীভাব"),
                correctIndex = 1,
                explanation = "দ্বন্দ্ব সমাসে পূর্বপদ ও পরপদ—উভয় পদের অর্থই সমান প্রাধান্য পায় (যেমন: মাতা ও পিতা = মাতাপিতা)।"
            )
        )

        WbbseSubject.ENGLISH -> listOf(
            McqQuestion(
                id = "eng_mcq_1",
                subject = subject,
                question = "In 'Father's Help', what subject did Samuel teach Swami's class?",
                options = listOf("History", "English", "Arithmetic", "Geography"),
                correctIndex = 2,
                explanation = "Samuel was teaching arithmetic when Swaminathan entered the classroom half an hour late."
            ),
            McqQuestion(
                id = "eng_mcq_2",
                subject = subject,
                question = "In the poem 'Fable', what did the Mountain call the Squirrel?",
                options = listOf("Little Bun", "Little Prig", "Tiny Nut", "Forest King"),
                correctIndex = 1,
                explanation = "The Mountain mocked the Squirrel by calling it 'Little Prig' (meaning a self-righteous person)."
            ),
            McqQuestion(
                id = "eng_mcq_3",
                subject = subject,
                question = "When was Mahatma Gandhi assassinated according to 'The Passing Away of Bapu'?",
                options = listOf("15 August 1947", "26 January 1948", "30 January 1948", "2 October 1948"),
                correctIndex = 2,
                explanation = "Nayantara Sehgal describes the tragic evening of 30 January 1948 when Gandhiji was shot on his way to a prayer meeting."
            ),
            McqQuestion(
                id = "eng_mcq_4",
                subject = subject,
                question = "Choose the correct Phrasal Verb for 'extinguish':",
                options = listOf("put off", "put out", "put up with", "put on"),
                correctIndex = 1,
                explanation = "'Put out' means to extinguish fire/light; 'put off' means postpone."
            ),
            McqQuestion(
                id = "eng_mcq_5",
                subject = subject,
                question = "Passive voice of 'She writes a poem' is:",
                options = listOf(
                    "A poem was written by her.",
                    "A poem is written by her.",
                    "A poem has been written by her.",
                    "A poem is being written by her."
                ),
                correctIndex = 1,
                explanation = "Simple Present Tense Active (S + V1 + O) changes to Passive as O + is/am/are + V3 + by + S."
            )
        )

        WbbseSubject.MATHEMATICS -> listOf(
            McqQuestion(
                id = "mth_mcq_1",
                subject = subject,
                question = "ax² + bx + c = 0 (a ≠ 0) দ্বিঘাত সমীকরণের বীজদ্বয় বাস্তব ও সমান হওয়ার শর্ত কী?",
                options = listOf("b² - 4ac > 0", "b² - 4ac = 0", "b² - 4ac < 0", "b² + 4ac = 0"),
                correctIndex = 1,
                explanation = "নিরূপক D = b² - 4ac = 0 হলে দ্বিঘাত সমীকরণের দুটি বীজই বাস্তব ও সমান (-b/2a) হয়।"
            ),
            McqQuestion(
                id = "mth_mcq_2",
                subject = subject,
                question = "একটি অর্ধবৃত্তস্থ কোণের মান কত?",
                options = listOf("45°", "60°", "90°", "180°"),
                correctIndex = 2,
                explanation = "উপপাদ্য ৩৭ অনুসারে অর্ধবৃত্তস্থ কোণ সর্বদা সমকোণ বা 90° হয়।"
            ),
            McqQuestion(
                id = "mth_mcq_3",
                subject = subject,
                question = "sin² 35° + cos² 35°-এর মান কত?",
                options = listOf("0", "1/2", "1", "2"),
                correctIndex = 2,
                explanation = "ত্রিকোণমিতিক অভেদ sin²θ + cos²θ = 1 যেকোনো কোণ θ-এর জন্য সত্য।"
            ),
            McqQuestion(
                id = "mth_mcq_4",
                subject = subject,
                question = "r ব্যাসার্ধবিশিষ্ট একটি নিরেট গোলকের আয়তনের সূত্র কী?",
                options = listOf("4πr²", "2πr³", "(4/3)πr³", "(1/3)πr²h"),
                correctIndex = 2,
                explanation = "r ব্যাসার্ধের গোলকের সমগ্রতলের ক্ষেত্রফল 4πr² এবং আয়তন (4/3)πr³ ঘনএকক।"
            ),
            McqQuestion(
                id = "mth_mcq_5",
                subject = subject,
                question = "বার্ষিক 10% সরল সুদের হারে 500 টাকার 2 বছরের সুদ কত টাকা?",
                options = listOf("50 টাকা", "100 টাকা", "120 টাকা", "200 টাকা"),
                correctIndex = 1,
                explanation = "I = (P × r × t) / 100 = (500 × 10 × 2) / 100 = 100 টাকা।"
            )
        )

        WbbseSubject.PHYSICAL_SCIENCE -> listOf(
            McqQuestion(
                id = "psc_mcq_1",
                subject = subject,
                question = "বয়েলের সূত্রে কোন দুটি রাশি স্থির থাকে?",
                options = listOf("চাপ ও ভর", "উষ্ণতা ও ভর", "আয়তন ও উষ্ণতা", "চাপ ও আয়তন"),
                correctIndex = 1,
                explanation = "বয়েলের সূত্রানুযায়ী স্থির উষ্ণতায় নির্দিষ্ট ভরের গ্যাসের আয়তন চাপের ব্যস্তানুপাতিক (P₁V₁ = P₂V₂)।"
            ),
            McqQuestion(
                id = "psc_mcq_2",
                subject = subject,
                question = "ওজোন স্তর বায়ুমণ্ডলের কোন স্তরে অবস্থিত?",
                options = listOf("ট্রপোস্ফিয়ার", "স্ট্র্যাটোস্ফিয়ার", "মেসোস্ফিয়ার", "থার্মোস্ফিয়ার"),
                correctIndex = 1,
                explanation = "স্ট্র্যাটোস্ফিয়ার স্তরে থাকা ওজোন গ্যাস সূর্যের ক্ষতিকর UV রশ্মি শোষণ করে।"
            ),
            McqQuestion(
                id = "psc_mcq_3",
                subject = subject,
                question = "হ্রস্বদৃষ্টি (Myopia) প্রতিকারের জন্য কোন লেন্স ব্যবহৃত হয়?",
                options = listOf("উত্তল লেন্স", "অবতল লেন্স", "সমতল কাচ", "বাইফোকাল লেন্স"),
                correctIndex = 1,
                explanation = "হ্রস্বদৃষ্টিতে অবতল লেন্স (অপসারী) এবং দীর্ঘদৃষ্টিতে উত্তল লেন্স (অভিসারী) ব্যবহার করা হয়।"
            ),
            McqQuestion(
                id = "psc_mcq_4",
                subject = subject,
                question = "তড়িৎ রোধের SI একক কী?",
                options = listOf("ভোল্ট (V)", "অ্যাম্পিয়ার (A)", "ওহম (Ω)", "ওয়াট (W)"),
                correctIndex = 2,
                explanation = "ওহমের সূত্র V = IR থেকে রোধ R = V/I, যার SI একক ওহম (Ω)।"
            ),
            McqQuestion(
                id = "psc_mcq_5",
                subject = subject,
                question = "আধুনিক দীর্ঘ পর্যায় সারণিতে মোট কয়টি শ্রেণি (Group) আছে?",
                options = listOf("৭টি", "৯টি", "১৬টি", "১৮টি"),
                correctIndex = 3,
                explanation = "আধুনিক দীর্ঘ পর্যায় সারণিতে ৭টি পর্যায় (Period) এবং ১৮টি শ্রেণি (Group) রয়েছে।"
            )
        )

        WbbseSubject.LIFE_SCIENCE -> listOf(
            McqQuestion(
                id = "lsc_mcq_1",
                subject = subject,
                question = "বীজের সুপ্তাবস্থা ভঙ্গ করতে সাহায্য করে কোন উদ্ভিদ হরমোন?",
                options = listOf("অক্সিন", "জিব্বেরেলিন", "সাইটোকাইনিন", "ইথিলিন"),
                correctIndex = 1,
                explanation = "জিব্বেরেলিন (GA) বীজের সুপ্তাবস্থা ভঙ্গ করে ও পর্বমধ্যের দৈর্ঘ্য বৃদ্ধি ঘটায়।"
            ),
            McqQuestion(
                id = "lsc_mcq_2",
                subject = subject,
                question = "কোন কোষ বিভাজনকে ‘হ্রাস বিভাজন’ বলা হয়?",
                options = listOf("অ্যামাইটোসিস", "মাইটোসিস", "মিয়োসিস", "দ্বিবিভাজন"),
                correctIndex = 2,
                explanation = "মিয়োসিস বিভাজনে অপত্য কোষে ক্রোমোজোম সংখ্যা মাতৃকোষের অর্ধেক (2n → n) হয়ে যায়।"
            ),
            McqQuestion(
                id = "lsc_mcq_3",
                subject = subject,
                question = "মেন্ডেলের একসংকর জননের F₂ জনুতে ফিনোটাইপিক অনুপাত কত?",
                options = listOf("1 : 2 : 1", "3 : 1", "9 : 3 : 3 : 1", "1 : 1"),
                correctIndex = 1,
                explanation = "একসংকর জননের F₂ জনুতে ফিনোটাইপ অনুপাত 3:1 এবং জিনোটাইপ অনুপাত 1:2:1।"
            ),
            McqQuestion(
                id = "lsc_mcq_4",
                subject = subject,
                question = "DNA অণুতে অ্যাডেনিন (A)-এর সঙ্গে কোন নাইট্রোজেন ক্ষারক যুক্ত থাকে?",
                options = listOf("গুয়ানিন (G)", "সাইটোসিন (C)", "থাইমিন (T)", "ইউরাসিল (U)"),
                correctIndex = 2,
                explanation = "DNA-তে A দুটি হাইড্রোজেন বন্ধনী দ্বারা T-এর সাথে এবং G তিনটি বন্ধনী দ্বারা C-এর সাথে যুক্ত থাকে।"
            ),
            McqQuestion(
                id = "lsc_mcq_5",
                subject = subject,
                question = "নীচের কোনটি এক্স-সিটু (Ex-situ) সংরক্ষণের উদাহরণ?",
                options = listOf("সুন্দরবন বায়োস্ফিয়ার রিজার্ভ", "জলদাপাড়া অভয়ারণ্য", "বোটানিক্যাল গার্ডেন", "গরুমারা জাতীয় উদ্যান"),
                correctIndex = 2,
                explanation = "নিজস্ব প্রাকৃতিক বাসস্থানের বাইরে সংরক্ষণ (যেমন বোটানিক্যাল গার্ডেন, চিড়িয়াখানা, ক্রায়োসংরক্ষণ) হলো এক্স-সিটু সংরক্ষণ।"
            )
        )

        WbbseSubject.HISTORY -> listOf(
            McqQuestion(
                id = "his_mcq_1",
                subject = subject,
                question = "সতীদাহ প্রথা কোন সালে আইন করে নিষিদ্ধ করা হয়?",
                options = listOf("১৮১৫", "১৮২৮", "১৮২৯", "১৮৫৬"),
                correctIndex = 2,
                explanation = "রাজা রামমোহন রায়ের উদ্যোগে লর্ড উইলিয়াম বেন্টিঙ্ক ১৮২৯ সালের রেগুলেশন XVII দ্বারা সতীদাহ প্রথা রদ করেন।"
            ),
            McqQuestion(
                id = "his_mcq_2",
                subject = subject,
                question = "১৮৫৫ সালে সাঁওতাল বিদ্রোহের (হুল) প্রধান নেতা কারা ছিলেন?",
                options = listOf("তিতুমীর ও বিরসা মুন্ডা", "সিধু ও কানহু", "দিগম্বর ও বিষ্ণুচরণ বিশ্বাস", "ভবানী পাঠক ও দেবী চৌধুরাণী"),
                correctIndex = 1,
                explanation = "দামিন-ই-কোহ অঞ্চলে সিধু ও কানহু মুরমুর নেতৃত্বে সাঁওতাল বিদ্রোহ (হুল) সূচিত হয়েছিল।"
            ),
            McqQuestion(
                id = "his_mcq_3",
                subject = subject,
                question = "‘নীলদর্পণ’ নাটকের রচয়িতা কে?",
                options = listOf("বঙ্কিমচন্দ্র চট্টোপাধ্যায়", "দীনবন্ধু মিত্র", "মাইকেল মধুসূদন দত্ত", "কালীপ্রসন্ন সিংহ"),
                correctIndex = 1,
                explanation = "১৮৬০ সালে দীনবন্ধু মিত্র নীলকর সাহেবদের অত্যাচারের বিরুদ্ধে ‘নীলদর্পণ’ নাটক রচনা করেন।"
            ),
            McqQuestion(
                id = "his_mcq_4",
                subject = subject,
                question = "১৮৭৬ সালে ‘ভারতসভা’ (Indian Association) কে প্রতিষ্ঠা করেন?",
                options = listOf("সুরেন্দ্রনাথ বন্দ্যোপাধ্যায়", "দাদাভাই নওরোজি", "বাল গঙ্গাধর তিলক", "অরবিন্দ ঘোষ"),
                correctIndex = 0,
                explanation = "সুরেন্দ্রনাথ বন্দ্যোপাধ্যায় ও আনন্দমোহন বসু ১৮৭৬ সালে কলকাতায় ভারতসভা প্রতিষ্ঠা করেন।"
            ),
            McqQuestion(
                id = "his_mcq_5",
                subject = subject,
                question = "‘বসু বিজ্ঞান মন্দির’ ১৯১৭ সালে কে প্রতিষ্ঠা করেন?",
                options = listOf("প্রফুল্লচন্দ্র রায়", "মেঘনাদ সাহা", "জগদীশচন্দ্র বসু", "সত্যেন্দ্রনাথ বসু"),
                correctIndex = 2,
                explanation = "আচার্য জগদীশচন্দ্র বসু ১৯১৭ সালে কলকাতায় বিজ্ঞান গবেষণার জন্য বসু বিজ্ঞান মন্দির প্রতিষ্ঠা করেন।"
            )
        )

        WbbseSubject.GEOGRAPHY -> listOf(
            McqQuestion(
                id = "geo_mcq_1",
                subject = subject,
                question = "‘বার্খান’ কোন প্রাকৃতিক শক্তির সঞ্চয়কার্যের ফলে গঠিত ভূমিরূপ?",
                options = listOf("নদী", "হিমবাহ", "বায়ু", "সমুদ্রতরঙ্গ"),
                correctIndex = 2,
                explanation = "মরু অঞ্চলে বায়ুর সঞ্চয়কার্যের ফলে অর্ধচন্দ্রাকৃতি বালিয়াড়ি বা ‘বার্খান’ সৃষ্টি হয়।"
            ),
            McqQuestion(
                id = "geo_mcq_2",
                subject = subject,
                question = "ট্রপোস্ফিয়ার স্তরে প্রতি ১ কিমি উচ্চতা বৃদ্ধিতে উষ্ণতা গড়ে কত কমে?",
                options = listOf("2.5°C", "6.4°C", "10.0°C", "1.2°C"),
                correctIndex = 1,
                explanation = "ট্রপোস্ফিয়ারে স্বাভাবিক উষ্ণতা হ্রাসের হার (Normal Lapse Rate) হলো প্রতি কিমিতে 6.4°C।"
            ),
            McqQuestion(
                id = "geo_mcq_3",
                subject = subject,
                question = "দুটি মুখ্য জোয়ারের মধ্যে সময়ের ব্যবধান কত?",
                options = listOf("১২ ঘণ্টা", "১২ ঘণ্টা ২৬ মিনিট", "২৪ ঘণ্টা", "২৪ ঘণ্টা ৫২ মিনিট"),
                correctIndex = 3,
                explanation = "পৃথিবীর আবর্তন ও চাঁদের পরিক্রমণের কারণে দুটি মুখ্য জোয়ারের ব্যবধান ২৪ ঘণ্টা ৫২ মিনিট হয়।"
            ),
            McqQuestion(
                id = "geo_mcq_4",
                subject = subject,
                question = "ভারতের কোন মৃত্তিকা কার্পাস (তুলা) চাষের জন্য সবচেয়ে উপযোগী?",
                options = listOf("পলি মৃত্তিকা", "কৃষ্ণ বা রেগুর মৃত্তিকা", "ল্যাটেরাইট মৃত্তিকা", "মরু মৃত্তিকা"),
                correctIndex = 1,
                explanation = "দাক্ষিণাত্য মালভূমির ব্যাসল্ট শিলা থেকে সৃষ্ট কৃষ্ণ (রেগুর) মৃত্তিকা জলধারণ ক্ষমতার জন্য কার্পাস চাষে শ্রেষ্ঠ।"
            ),
            McqQuestion(
                id = "geo_mcq_5",
                subject = subject,
                question = "ভারতের কোন শহরকে ‘ভারতের রূঢ়’ বলা হয়?",
                options = listOf("জামশেদপুর", "আমেদাবাদ", "দুর্গাপুর", "বেঙ্গালুরু"),
                correctIndex = 2,
                explanation = "খনিজ ও শিল্পাঞ্চলের প্রাচুর্যের কারণে পশ্চিমবঙ্গের দুর্গাপুরকে ‘ভারতের রূঢ়’ বলা হয়।"
            )
        )
    }

    fun getSaqs(subject: WbbseSubject): List<SaqQuestion> = when (subject) {
        WbbseSubject.BENGALI -> listOf(
            SaqQuestion(
                id = "bng_saq_1",
                subject = subject,
                question = "‘জ্ঞানচক্ষু’ গল্পে তপনের প্রকৃত জ্ঞানচক্ষু কীভাবে উন্মীলিত হয়েছিল?",
                modelAnswer = "‘সন্ধ্যাতারা’ পত্রিকায় ছাপা গল্প পড়ে তপন দেখে ছোটোমেসো ‘কারেকশন’-এর নামে পুরো গল্পটি বদলে দিয়েছেন। নিজের নামে অন্যের লেখা ছাপা হওয়ার লজ্জায় ও বেদনায় তপনের প্রকৃত জ্ঞানচক্ষু খুলে যায়।",
                keyTerms = listOf("সন্ধ্যাতারা", "ছোটোমেসো", "কারেকশন", "বদলে", "লজ্জা", "তপন")
            ),
            SaqQuestion(
                id = "bng_saq_2",
                subject = subject,
                question = "‘কারক’ কাকে বলে? উদাহরণ দাও।",
                modelAnswer = "বাক্যস্থিত ক্রিয়াপদের সঙ্গে নামপদের (বিশেষ্য বা সর্বনাম) যে প্রত্যক্ষ সম্পর্ক থাকে, তাকে কারক বলে। যেমন: ‘রাম বই পড়ে’—এখানে ‘রাম’ কর্তৃকারক এবং ‘বই’ কর্মকারক।",
                keyTerms = listOf("ক্রিয়াপদ", "নামপদ", "সম্পর্ক", "কর্তৃ", "কর্ম")
            ),
            SaqQuestion(
                id = "bng_saq_3",
                subject = subject,
                question = "‘আফ্রিকা’ কবিতায় কবি যুগান্তরের কবিকে কী বলতে আহ্বান জানিয়েছেন?",
                modelAnswer = "রবীন্দ্রনাথ ঠাকুর যুগান্তরের কবিকে ইউরোপীয় সাম্রাজ্যবাদের অত্যাচারে লাঞ্ছিত ‘মানহারা মানবী’ আফ্রিকার দ্বারে দাঁড়িয়ে সভ্যতার শেষ পুণ্যবাণী হিসেবে ‘ক্ষমা করো’ বলতে আহ্বান জানিয়েছেন।",
                keyTerms = listOf("ক্ষমা করো", "মানহারা", "সভ্যতা", "পুণ্যবাণী", "আফ্রিকা")
            )
        )

        WbbseSubject.ENGLISH -> listOf(
            SaqQuestion(
                id = "eng_saq_1",
                subject = subject,
                question = "Why did Swaminathan feel guilty while going to school in 'Father's Help'?",
                modelAnswer = "Swaminathan felt guilty because his conscience bothered him for lying about Samuel. He realized Samuel was actually gentle, friendly, and did not deserve the harsh complaint letter written by his father.",
                keyTerms = listOf("conscience", "guilty", "samuel", "letter", "gentle", "lie", "lying")
            ),
            SaqQuestion(
                id = "eng_saq_2",
                subject = subject,
                question = "What message does the Squirrel give the Mountain in 'Fable'?",
                modelAnswer = "The Squirrel reminds the Mountain that talents differ and every creature has a unique place in nature. Just as the squirrel cannot carry forests on its back, the mountain cannot crack a nut.",
                keyTerms = listOf("talents", "differ", "forest", "crack", "nut", "unique")
            ),
            SaqQuestion(
                id = "eng_saq_3",
                subject = subject,
                question = "Change the voice: 'The teacher praised the student.' and 'Open the door.'",
                modelAnswer = "1. The student was praised by the teacher.\n2. Let the door be opened.",
                keyTerms = listOf("student was praised", "by the teacher", "let the door be opened")
            )
        )

        WbbseSubject.MATHEMATICS -> listOf(
            SaqQuestion(
                id = "mth_saq_1",
                subject = subject,
                question = "শ্রীধর আচার্যের সূত্রটি লেখো এবং বীজদ্বয়ের সমষ্টি ও গুণফল নির্ণয়ের সূত্র দাও।",
                modelAnswer = "ax² + bx + c = 0 (a ≠ 0) এর বীজ x = (-b ± √(b² - 4ac)) / 2a। বীজদ্বয় α, β হলে সমষ্টি α + β = -b/a এবং গুণফল αβ = c/a।",
                keyTerms = listOf("-b", "4ac", "2a", "-b/a", "c/a", "a ≠ 0")
            ),
            SaqQuestion(
                id = "mth_saq_2",
                subject = subject,
                question = "কোনো বৃত্তের কেন্দ্রস্থ কোণ 120° হলে একই বৃত্তচাপের ওপর অবস্থিত বৃত্তস্থ কোণের মান কত? যুক্তি দাও।",
                modelAnswer = "একই বৃত্তচাপের ওপর অবস্থিত কেন্দ্রস্থ কোণ বৃত্তস্থ কোণের দ্বিগুণ হয়। তাই বৃত্তস্থ কোণ = 120° / 2 = 60°।",
                keyTerms = listOf("দ্বিগুণ", "60", "৬০", "অর্ধেক", "কেন্দ্রস্থ")
            ),
            SaqQuestion(
                id = "mth_saq_3",
                subject = subject,
                question = "একটি লম্ব বৃত্তাকার শঙ্কুর ব্যাসার্ধ r এবং উচ্চতা h হলে তির্যক উচ্চতা (l) ও আয়তনের (V) সূত্র লেখো।",
                modelAnswer = "শঙ্কুর তির্যক উচ্চতা l = √(r² + h²) একক এবং আয়তন V = (1/3)πr²h ঘনএকক।",
                keyTerms = listOf("r² + h²", "1/3", "πr²h", "tirjak", "√")
            )
        )

        WbbseSubject.PHYSICAL_SCIENCE -> listOf(
            SaqQuestion(
                id = "psc_saq_1",
                subject = subject,
                question = "ওহমের সূত্রটি বিবৃত করো এবং এর গাণিতিক রূপ লেখো।",
                modelAnswer = "উষ্ণতা ও অন্যান্য ভৌত অবস্থা অপরিবর্তিত থাকলে কোনো পরিবাহীর মধ্য দিয়ে তড়িৎপ্রবাহমাত্রা (I) তার দুই প্রান্তের বিভবপ্রভেদের (V) সমানুপাতিক। গাণিতিক রূপ: V = IR (যেখানে R = পরিবাহীর রোধ)।",
                keyTerms = listOf("উষ্ণতা", "অপরিবর্তিত", "সমানুপাতিক", "v = ir", "v=ir", "রোধ")
            ),
            SaqQuestion(
                id = "psc_saq_2",
                subject = subject,
                question = "আলোর প্রতিসরণের দ্বিতীয় সূত্র (স্নেলের সূত্র) লেখো।",
                modelAnswer = "নির্দিষ্ট বর্ণের আলোকরশ্মি দুটি নির্দিষ্ট মাধ্যমের বিভেদতলে তির্যকভাবে আপতিত হলে আপতন কোণের সাইন (sin i) ও প্রতিসরণ কোণের সাইন (sin r)-এর অনুপাত সর্বদা ধ্রুবক থাকে: sin i / sin r = μ।",
                keyTerms = listOf("sin i", "sin r", "ধ্রুবক", "μ", "অনুপাত", "বর্ণ")
            ),
            SaqQuestion(
                id = "psc_saq_3",
                subject = subject,
                question = "পরম শূন্য উষ্ণতা কাকে বলে? এর মান কত?",
                modelAnswer = "চার্লসের সূত্রানুসারে যে সর্বনিম্ন উষ্ণতায় যেকোনো আদর্শ গ্যাসের আয়তন তাত্ত্বিকভাবে শূন্য হয়ে যায়, তাকে পরম শূন্য উষ্ণতা বলে। এর মান -273.15°C বা 0 K।",
                keyTerms = listOf("আয়তন", "শূন্য", "-273", "0 k", "চার্লস")
            )
        )

        WbbseSubject.LIFE_SCIENCE -> listOf(
            SaqQuestion(
                id = "lsc_saq_1",
                subject = subject,
                question = "অক্সিন হরমোনের দুটি প্রধান কাজ লেখো।",
                modelAnswer = "১. অক্সিন (IAA) উদ্ভিদের অগ্রস্থ প্রকটতা ঘটায় ও কাণ্ডের দৈর্ঘ্য বৃদ্ধি করে।\n২. ফটোট্রপিক চলন নিয়ন্ত্রণ করে এবং নিষেক ছাড়াই বীজবিহীন ফল (পার্থেনোকার্পি) সৃষ্টিতে সাহায্য করে।",
                keyTerms = listOf("অগ্রস্থ", "দৈর্ঘ্য", "ফটোট্রপিক", "পার্থেনোকার্পি", "বীজবিহীন", "বৃদ্ধি")
            ),
            SaqQuestion(
                id = "lsc_saq_2",
                subject = subject,
                question = "প্রতিবর্ত ক্রিয়া (Reflex Action) কাকে বলে? একটি উদাহরণ দাও।",
                modelAnswer = "সুষুম্নাকাণ্ড দ্বারা নিয়ন্ত্রিত দ্রুত, অনৈচ্ছিক ও স্বতঃস্ফূর্ত সাড়াপ্রদানকে প্রতিবর্ত ক্রিয়া বলে। উদাহরণ: অসাবধানতাবশত গরম বস্তুতে হাত পড়লে তৎক্ষণাৎ হাত সরিয়ে নেওয়া।",
                keyTerms = listOf("সুষুম্নাকাণ্ড", "অনৈচ্ছিক", "দ্রুত", "স্বতঃস্ফূর্ত", "গরম", "হাত")
            ),
            SaqQuestion(
                id = "lsc_saq_3",
                subject = subject,
                question = "DNA এবং RNA-এর দুটি গঠনগত পার্থক্য লেখো।",
                modelAnswer = "১. DNA-তে ডিঅক্সিরাইবোজ শর্করা থাকে, কিন্তু RNA-তে রাইবোজ শর্করা থাকে।\n২. DNA-তে পিরিমিডিন ক্ষারক হিসেবে থাইমিন (T) থাকে, RNA-তে তার বদলে ইউরাসিল (U) থাকে।",
                keyTerms = listOf("ডিঅক্সিরাইবোজ", "রাইবোজ", "থাইমিন", "ইউরাসিল", "দ্বিতন্ত্রী", "একতন্ত্রী")
            )
        )

        WbbseSubject.HISTORY -> listOf(
            SaqQuestion(
                id = "his_saq_1",
                subject = subject,
                question = "সাঁওতাল বিদ্রোহের (১৮৫৫) দুটি প্রধান কারণ লেখো।",
                modelAnswer = "১. দামিন-ই-কোহ অঞ্চলে ব্রিটিশ সরকারের অতিরিক্ত ভূমি রাজস্ব বৃদ্ধি।\n২. বহিরাগত মহাজন ও জমিদারদের উচ্চ সুদের ঋণের ফাঁদ এবং পুলিশের অত্যাচার।",
                keyTerms = listOf("রাজস্ব", "মহাজন", "ঋণ", "দামিন", "পুলিশ", "জমিদার")
            ),
            SaqQuestion(
                id = "his_saq_2",
                subject = subject,
                question = "১৮৫৮ সালের মহারানির ঘোষণাপত্রের দুটি গুরুত্ব লেখো।",
                modelAnswer = "১. ভারতে ইস্ট ইন্ডিয়া কোম্পানির শাসনের অবসান ঘটে এবং সরাসরি ব্রিটিশ ক্রাউনের শাসন শুরু হয়।\n২. স্বত্ববিলোপ নীতি বাতিল করা হয় এবং ভারতীয়দের ধর্মীয় স্বাধীনতা ও সমানাধিকারের আশ্বাস দেওয়া হয়।",
                keyTerms = listOf("কোম্পানি", "ক্রাউন", "স্বত্ববিলোপ", "ভিক্টোরিয়া", "শাসন")
            ),
            SaqQuestion(
                id = "his_saq_3",
                subject = subject,
                question = "ঈশ্বরচন্দ্র বিদ্যাসাগরের সমাজসংস্কারের দুটি উল্লেখযোগ্য অবদান লেখো।",
                modelAnswer = "১. তাঁর অক্লান্ত প্রচেষ্টায় ১৮৫৬ সালে লর্ড ডালহৌসি বিধবাবিবাহ আইন পাস করেন।\n২. নারীশিক্ষা বিস্তারে তিনি বাংলায় ৩৫টি বালিকা বিদ্যালয় স্থাপন করেন এবং বহুবিবাহ রোধে আন্দোলন করেন।",
                keyTerms = listOf("বিধবাবিবাহ", "১৮৫৬", "নারীশিক্ষা", "বালিকা বিদ্যালয়", "বহুবিবাহ")
            )
        )

        WbbseSubject.GEOGRAPHY -> listOf(
            SaqQuestion(
                id = "geo_saq_1",
                subject = subject,
                question = "বৃষ্টিচ্ছায় অঞ্চল (Rain Shadow Region) কাকে বলে? ভারতের একটি উদাহরণ দাও।",
                modelAnswer = "শৈলোৎক্ষেপ বৃষ্টিপাতের সময় পর্বতের প্রতিবাত ঢালে প্রচুর বৃষ্টিপাতের পর জলীয় বাষ্পহীন বায়ু বিপরীত অনুবাত ঢালে পৌঁছালে সেখানে বৃষ্টি প্রায় হয় না; এই শুষ্ক অঞ্চলকে বৃষ্টিচ্ছায় অঞ্চল বলে। উদাহরণ: মেঘালয়ের শিলং বা পশ্চিমঘাটের পূর্ব ঢাল।",
                keyTerms = listOf("অনুবাত", "প্রতিবাত", "শৈলোৎক্ষেপ", "শিলং", "পশ্চিমঘাট", "শুষ্ক")
            ),
            SaqQuestion(
                id = "geo_saq_2",
                subject = subject,
                question = "ভরা কোটাল (Spring Tide) কখন এবং কেন হয়?",
                modelAnswer = "অমাবস্যা ও পূর্ণিমা তিথিতে চাঁদ, সূর্য ও পৃথিবী একই সরলরেখায় (সিজিগি অবস্থান) থাকে। ফলে চাঁদ ও সূর্যের মিলিত মহাকর্ষীয় আকর্ষণে সমুদ্রের জল খুব বেশি ফুলে ওঠে—একে ভরা কোটাল বলে।",
                keyTerms = listOf("অমাবস্যা", "পূর্ণিমা", "সিজিগি", "সরলরেখা", "আকর্ষণ")
            ),
            SaqQuestion(
                id = "geo_saq_3",
                subject = subject,
                question = "বর্জ্য ব্যবস্থাপনার 4R নীতি কী?",
                modelAnswer = "বর্জ্য ব্যবস্থাপনার 4R নীতি হলো: Reduce (বর্জ্য উৎপাদন হ্রাস), Reuse (পুনর্ব্যবহার), Recycle (পুনর্নবীকরণ) এবং Refuse (অপ্রয়োজনীয় প্লাস্টিক/বর্জ্য বর্জন)।",
                keyTerms = listOf("reduce", "reuse", "recycle", "refuse", "হ্রাস", "পুনর্ব্যবহার")
            )
        )
    }

    fun getLongQuestions(subject: WbbseSubject): List<LongQuestion> = when (subject) {
        WbbseSubject.BENGALI -> listOf(
            LongQuestion(
                id = "bng_long_1",
                subject = subject,
                question = "‘জ্ঞানচক্ষু’ গল্প অবলম্বনে তপনের চরিত্র ও গল্পটির নামকরণের সার্থকতা বিশ্লেষণ করো। (৫ নম্বর)",
                modelOutline = "ভূমিকা (আশাপূর্ণা দেবী, কুমকুম) → তপনের কল্পনাপ্রবণ কিশোর মন → লেখক সম্পর্কে ভুল ধারণা ভাঙা → ‘প্রথম দিন’ গল্প রচনা → ছোটোমেসোর কারেকশন ও ‘সন্ধ্যাতারা’য় প্রকাশ → আত্মমর্যাদাবোধ ও প্রকৃত জ্ঞানচক্ষু উন্মীলন → উপসংহার।",
                keyTerms = listOf("আশাপূর্ণা", "তপন", "সন্ধ্যাতারা", "কারেকশন", "ছোটোমেসো", "জ্ঞানচক্ষু", "আত্মমর্যাদা")
            ),
            LongQuestion(
                id = "bng_long_2",
                subject = subject,
                question = "‘অসুখী একজন’ কবিতায় যুদ্ধের ভয়াবহ ধ্বংসলীলা এবং অপেক্ষারত মেয়েটির প্রতীকী তাৎপর্য আলোচনা করো। (৫ নম্বর)",
                modelOutline = "কবি পাবলো নেরুদা ও অনুবাদক নবারুণ ভট্টাচার্য → প্রিয়তমার অপেক্ষা → ‘রক্তের এক আগ্নেয়পাহাড়’ রূপে যুদ্ধের আগমন → শহর, শিশু, বাড়ি ও শান্ত হলুদ দেবতার ধ্বংস → তবুও মেয়েটির অমরত্ব (ভালোবাসা ও শান্তির প্রতীক)।",
                keyTerms = listOf("নেরুদা", "নবারুণ", "যুদ্ধ", "আগ্নেয়পাহাড়", "কাঠকয়লা", "দেবতা", "অপেক্ষা", "শান্তি")
            )
        )

        WbbseSubject.ENGLISH -> listOf(
            LongQuestion(
                id = "eng_long_1",
                subject = subject,
                question = "Write a concise character sketch of Swaminathan from 'Father's Help' and explain how his view of Samuel changed. (5 Marks)",
                modelOutline = "Introduction (R.K. Narayan, Malgudi Days) → Swami's Monday morning reluctance & headache excuse → Father's strictness & complaint letter → Realizing Samuel's kindness at school → Inner guilt & provocation → Unexpected twist at the end.",
                keyTerms = listOf("narayan", "swaminathan", "headache", "father", "letter", "samuel", "guilty", "kind")
            ),
            LongQuestion(
                id = "eng_long_2",
                subject = subject,
                question = "Write a short paragraph (100 words) on 'Importance of Protecting the Environment' or 'Your Aim in Life' for Madhyamik Writing Skill. (5 Marks)",
                modelOutline = "Clear title → Strong topic sentence → 3 well-connected supporting points with concrete examples → Grammar accuracy & cohesive linkers → Memorable concluding sentence.",
                keyTerms = listOf("environment", "trees", "pollution", "future", "aim", "society", "responsibility")
            )
        )

        WbbseSubject.MATHEMATICS -> listOf(
            LongQuestion(
                id = "mth_long_1",
                subject = subject,
                question = "পিথাগোরাসের উপপাদ্যটি বিবৃত করো এবং জ্যামিতিকভাবে প্রমাণ করো। (৫ নম্বর)",
                modelOutline = "১. সাধারণ নির্বচন (অতিভুজ² = লম্ব² + ভূমি²) → ২. চিত্র ও প্রদত্ত (সমকোণী ΔABC, ∠A = 90°) → ৩. প্রামাণ্য (BC² = AB² + AC²) → ৪. অঙ্কন (AD ⊥ BC) → ৫. সদৃশকোণী ত্রিভুজ প্রমাণ ও যোগফল → প্রমাণিত।",
                keyTerms = listOf("সমকোণী", "অতিভুজ", "প্রদত্ত", "প্রামাণ্য", "অঙ্কন", "সদৃশ", "bc²", "ab²", "ac²")
            ),
            LongQuestion(
                id = "mth_long_2",
                subject = subject,
                question = "প্রমাণ করো: কোনো বৃত্তের একটি বৃত্তচাপের দ্বারা গঠিত সম্মুখ কেন্দ্রস্থ কোণ ওই চাপের দ্বারা গঠিত যেকোনো বৃত্তস্থ কোণের দ্বিগুণ। (৫ নম্বর)",
                modelOutline = "১. প্রদত্ত (O কেন্দ্রবিশিষ্ট বৃত্ত, চাপ APB) → ২. প্রামাণ্য (∠AOB = 2∠ACB) → ৩. অঙ্কন (C, O যুক্ত করে D পর্যন্ত বর্ধিত) → ৪. বহিঃস্থ কোণ = অন্তঃস্থ বিপরীত কোণদ্বয়ের সমষ্টি ও ব্যাসার্ধ সমান → যোগ করে প্রমাণ।",
                keyTerms = listOf("কেন্দ্রস্থ", "বৃত্তস্থ", "দ্বিগুণ", "ব্যাসার্ধ", "বহিঃস্থ", "প্রামাণ্য", "অঙ্কন")
            )
        )

        WbbseSubject.PHYSICAL_SCIENCE -> listOf(
            LongQuestion(
                id = "psc_long_1",
                subject = subject,
                question = "বয়েলের সূত্র, চার্লসের সূত্র ও অ্যাভোগাড্রো সূত্রের সমন্বয়ে আদর্শ গ্যাস সমীকরণ PV = nRT প্রতিষ্ঠা করো। (৫ নম্বর)",
                modelOutline = "১. বয়েলের সূত্র (V ∝ 1/P) → ২. চার্লসের সূত্র (V ∝ T) → ৩. অ্যাভোগাড্রো সূত্র (V ∝ n) → ৪. যৌগিক ভেদের সূত্রানুসারে V ∝ nT/P → ৫. সমানুপাতিক ধ্রুবক R বসিয়ে PV = nRT এবং R-এর SI মান (8.314 J·mol⁻¹·K⁻¹)।",
                keyTerms = listOf("বয়েল", "চার্লস", "অ্যাভোগাড্রো", "ভেদ", "pv = nrt", "pv=nrt", "8.314", "ধ্রুবক")
            ),
            LongQuestion(
                id = "psc_long_2",
                subject = subject,
                question = "তড়িৎপ্রবাহের তাপীয় ফল সংক্রান্ত জুলের সূত্র তিনটি লেখো এবং প্রমাণ করো H = I²Rt / J। (৫ নম্বর)",
                modelOutline = "১. প্রবাহমাত্রার সূত্র (H ∝ I²) → ২. রোধের সূত্র (H ∝ R) → ৩. সময়ের সূত্র (H ∝ t) → ৪. যৌগিক ভেদ থেকে H ∝ I²Rt → ৫. তড়িৎশক্তি W = VIt = I²Rt এবং জুলের তুল্যাঙ্ক J বসিয়ে H = I²Rt / J।",
                keyTerms = listOf("জুল", "i²", "রোধ", "সময়", "তাপ", "h = i²rt", "তুল্যাঙ্ক")
            )
        )

        WbbseSubject.LIFE_SCIENCE -> listOf(
            LongQuestion(
                id = "lsc_long_1",
                subject = subject,
                question = "মেন্ডেলের একসংকর জনন পরীক্ষাটি চেকার বোর্ডের সাহায্যে ব্যাখ্যা করো এবং এ থেকে প্রাপ্ত সূত্রটি লেখো। (৫ নম্বর)",
                modelOutline = "মটর গাছ (বিশুদ্ধ লম্বা TT × বিশুদ্ধ খর্ব tt) → F₁ জনু (সব সংকর লম্বা Tt) → স্ব-পরাগযোগ → F₂ চেকার বোর্ড (TT, Tt, Tt, tt) → ফিনোটাইপ 3:1 ও জিনোটাইপ 1:2:1 → পৃথকীভবন সূত্র।",
                keyTerms = listOf("মেন্ডেল", "tt", "চেকার", "3:1", "1:2:1", "ফিনোটাইপ", "জিনোটাইপ", "পৃথকীভবন")
            ),
            LongQuestion(
                id = "lsc_long_2",
                subject = subject,
                question = "নাইট্রোজেন চক্রের প্রধান ধাপগুলি ব্যাখ্যা করো এবং মানবজীবনে নাইট্রোজেন চক্রের গুরুত্ব লেখো। (৫ নম্বর)",
                modelOutline = "১. নাইট্রোজেন স্থিতিকরণ (রাইজোবিয়াম, বজ্রপাত) → ২. অ্যামোনিফিকেশন → ৩. নাইট্রিফিকেশন (নাইট্রোসোমোনাস, নাইট্রোব্যাকটর) → ৪. ডিনাইট্রিফিকেশন (সিউডোমোনাস) → ৫. বাস্তুতন্ত্রে ভারসাম্য ও প্রোটিন সংশ্লেষে গুরুত্ব।",
                keyTerms = listOf("স্থিতিকরণ", "রাইজোবিয়াম", "অ্যামোনিফিকেশন", "নাইট্রিফিকেশন", "ডিনাইট্রিফিকেশন", "সিউডোমোনাস")
            )
        )

        WbbseSubject.HISTORY -> listOf(
            LongQuestion(
                id = "his_long_1",
                subject = subject,
                question = "১৮৫৯-৬০ সালের নীল বিদ্রোহের কারণ, বিস্তার এবং এই বিদ্রোহে শিক্ষিত বাঙালি মধ্যবিত্ত শ্রেণির ভূমিকা আলোচনা করো। (৫ নম্বর)",
                modelOutline = "ভূমিকা (নদীয়ার চৌগাছা, দিগম্বর ও বিষ্ণুচরণ বিশ্বাস) → কারণ (দাদন প্রথা, নীলকরদের অত্যাচার, কম দাম) → শিক্ষিত মধ্যবিত্তের সমর্থন (হরিশ্চন্দ্রের হিন্দু প্যাট্রিয়ট, দীনবন্ধু মিত্রের নীলদর্পণ) → ফলাফল (নীল কমিশন ১৮৬০) → উপসংহার।",
                keyTerms = listOf("নীল", "দাদন", "দিগম্বর", "বিষ্ণুচরণ", "হিন্দু প্যাট্রিয়ট", "নীলদর্পণ", "কমিশন")
            ),
            LongQuestion(
                id = "his_long_2",
                subject = subject,
                question = "উনিশ শতকে বাংলার সমাজ ও শিক্ষাসংস্কারে রাজা রামমোহন রায় ও ঈশ্বরচন্দ্র বিদ্যাসাগরের অবদান মূল্যায়ন করো। (৫ নম্বর)",
                modelOutline = "রামমোহন রায়: আত্মীয় সভা, ব্রাহ্মসমাজ (১৮২৮), সতীদাহ প্রথা রদ (১৮২৯), পাশ্চাত্য শিক্ষার প্রসার → বিদ্যাসাগর: বিধবাবিবাহ আইন (১৮৫৬), বালিকা বিদ্যালয় স্থাপন, বর্ণপরিচয় → নবজাগরণে যুগ্ম প্রভাব।",
                keyTerms = listOf("রামমোহন", "ব্রাহ্মসমাজ", "সতীদাহ", "১৮২৯", "বিদ্যাসাগর", "বিধবাবিবাহ", "১৮৫৬", "নারীশিক্ষা")
            )
        )

        WbbseSubject.GEOGRAPHY -> listOf(
            LongQuestion(
                id = "geo_long_1",
                subject = subject,
                question = "নদীর ক্ষয়কার্যের ফলে সৃষ্ট তিনটি প্রধান ভূমিরূপের সচিত্র বিবরণ দাও। (৫ নম্বর)",
                modelOutline = "নদীর উচ্চগতিতে নিম্নক্ষয় → ১. V-আকৃতির উপত্যকা ও গিরিখাত/ক্যানিয়ন → ২. জলপ্রপাত ও প্রপাতকূপ → ৩. শৃঙ্খলিত শৈলশিরা বা মন্থকূপ → প্রতিটি ভূমিরূপের উদাহরণ ও স্পষ্ট চিত্র।",
                keyTerms = listOf("উচ্চগতি", "গিরিখাত", "ক্যানিয়ন", "জলপ্রপাত", "প্রপাতকূপ", "v-আকৃতি", "ক্ষয়")
            ),
            LongQuestion(
                id = "geo_long_2",
                subject = subject,
                question = "পূর্ব ও মধ্য ভারতে লৌহ-ইস্পাত শিল্পের কেন্দ্রীভবনের প্রধান কারণগুলি ব্যাখ্যা করো। (৫ নম্বর)",
                modelOutline = "ছোটনাগপুর মালভূমি অঞ্চল → ১. কাঁচামালের সহজলভ্যতা (আকরিক লোহা, কয়লা, চুনাপাথর, ডলোমাইট) → ২. দামোদর ও সুবর্ণরেখা থেকে জল সরবরাহ → ৩. সুলভ বিদ্যুৎ ও দক্ষ শ্রমিক → ৪. কলকাতা বন্দর ও রেল পরিবহণ → প্রধান কেন্দ্র (জামশেদপুর, দুর্গাপুর, ভিলাই)।",
                keyTerms = listOf("আকরিক লোহা", "কয়লা", "চুনাপাথর", "দামোদর", "শ্রমিক", "কলকাতা", "জামশেদপুর", "দুর্গাপুর")
            )
        )
    }
}
