package com.example.data.exam

import com.example.model.LongQuestion
import com.example.model.McqQuestion
import com.example.model.SaqQuestion
import com.example.model.WbbseSubject

/**
 * Extended WBBSE Class 10 Offline Bank (Part 1: Bengali, English, Mathematics, Physical Science).
 * Combined with WbbseExamBank and Part 2, provides 252 verified offline Madhyamik questions.
 */
internal object WbbseExamBankExtendedPart1 {

    fun extraMcqs(subject: WbbseSubject): List<McqQuestion> = when (subject) {
        WbbseSubject.BENGALI -> listOf(
            McqQuestion("bng_mcq_6", subject, "‘বহুরূপী’ গল্পে হরিদার আসল পেশা কী ছিল?", listOf("দোকানদারি", "বহুরূপী সাজা", "শিক্ষকতা", "রেলকর্মী"), 1, "হরিদা শহরের রাস্তায় বিভিন্ন বহুরূপী সেজে সামান্য রোজগার করতেন।"),
            McqQuestion("bng_mcq_7", subject, "‘অভিষেক’ কাব্যাংশটি মাইকেল মধুসূদন দত্তের কোন কাব্য থেকে নেওয়া?", listOf("তিলোত্তমাসম্ভব", "মেঘনাদবধ কাব্য", "বীরাঙ্গনা", "ব্রজাঙ্গনা"), 1, "মেঘনাদবধ কাব্যের প্রথম সর্গ ‘অভিষেক’ থেকে অংশটি সংকলিত।"),
            McqQuestion("bng_mcq_8", subject, "‘পথের দাবী’ গল্পে অপূর্ব কোন শহরের অফিসে চাকরি করত?", listOf("কলকাতা", "রেঙ্গুন", "ঢাকা", "দিল্লি"), 1, "অপূর্ব রেঙ্গুনের একটি ইংরেজ বণিক অফিসে কাজ করত।"),
            McqQuestion("bng_mcq_9", subject, "‘কোনি’ উপন্যাসের রচয়িতা কে?", listOf("মতি নন্দী", "শীর্ষেন্দু মুখোপাধ্যায়", "সুনীল গঙ্গোপাধ্যায়", "সমরেশ বসু"), 0, "ক্রীড়া সাংবাদিক ও সাহিত্যিক মতি নন্দীর বিখ্যাত উপন্যাস ‘কোনি’।"),
            McqQuestion("bng_mcq_10", subject, "কোনি উপন্যাসে ক্ষিতীশ সিংহ কোন ক্লাবের প্রশিক্ষক ছিলেন?", listOf("মোহনবাগান", "জুপিটার সুইমিং ক্লাব", "অ্যাপোলো ক্লাব", "ইস্টবেঙ্গল"), 1, "ক্ষিতীশ সিংহ দীর্ঘদিন জুপিটার ক্লাবে সাঁতারের প্রশিক্ষণ দিয়েছিলেন।"),
            McqQuestion("bng_mcq_11", subject, "‘ফাইট কোনি, ফাইট!’—সংলাপটি কার?", listOf("হরিচরণ", "কমল পাল", "ক্ষিতীশ সিংহ", "লীলাবতী"), 2, "ক্ষিতীশ সিংহ কোনির মধ্যে লড়াইয়ের জেদ জাগিয়ে তুলতে এই মন্ত্র দিয়েছিলেন।"),
            McqQuestion("bng_mcq_12", subject, "‘হারিয়ে যাওয়া কালি কলম’ প্রবন্ধের লেখক কে?", listOf("শ্রীপান্থ", "রাজশেখর বসু", "প্রমথ চৌধুরী", "সৈয়দ মুজতবা আলী"), 0, "নিখিল সরকার ‘শ্রীপান্থ’ ছদ্মনামে প্রবন্ধটি রচনা করেন।"),
            McqQuestion("bng_mcq_13", subject, "‘বাংলা ভাষায় বিজ্ঞান’ প্রবন্ধে লেখক কাকে বলেছেন ‘কলম পেশা’?", listOf("পাঠককে", "পরিভাষা রচয়িতাদের", "লেখক-অনুবাদকদের", "বিজ্ঞানীদের"), 2, "রাজশেখর বসু বাংলায় বৈজ্ঞানিক প্রবন্ধ রচনার অসুবিধা প্রসঙ্গে কথাটি বলেন।"),
            McqQuestion("bng_mcq_14", subject, "‘প্রলয়োল্লাস’ কবিতাটি কাজী নজরুল ইসলামের কোন কাব্যগ্রন্থের?", listOf("বিষের বাঁশি", "অগ্নিবীণা", "ভাঙার গান", "সর্বহারা"), 1, "‘অগ্নিবীণা’ কাব্যগ্রন্থের প্রথম কবিতা ‘প্রলয়োল্লাস’।"),
            McqQuestion("bng_mcq_15", subject, "‘আয় আরো বেঁধে বেঁধে থাকি’ কবিতার কবি কে?", listOf("জয় গোস্বামী", "শঙ্খ ঘোষ", "শক্তি চট্টোপাধ্যায়", "সুনীল গঙ্গোপাধ্যায়"), 1, "শঙ্খ ঘোষের ‘জলই পাষাণ হয়ে আছে’ কাব্য থেকে কবিতাটি নেওয়া।"),
            McqQuestion("bng_mcq_16", subject, "‘বুলবুলিতে ধান খেয়েছে’—এখানে ‘বুলবুলিতে’ কোন কারক?", listOf("কর্ম কারক", "কর্তৃকারক", "করণ কারক", "অধিকরণ কারক"), 1, "খাওয়া ক্রিয়ার কর্তা ‘বুলবুলি’, তাই কর্তৃকারকে ‘তে’ বিভক্তি।"),
            McqQuestion("bng_mcq_17", subject, "‘উপকূল’ পদটি কোন সমাসের উদাহরণ?", listOf("কর্মধারয়", "বহুব্রীহি", "অব্যয়ীভাব", "তৎপুরুষ"), 2, "কূলের সমীপে = উপকূল; এখানে ‘উপ’ অব্যয় পদটির অর্থ প্রাধান্য পেয়েছে।"),
            McqQuestion("bng_mcq_18", subject, "গঠনগতভাবে বাংলা বাক্য কয় প্রকার?", listOf("দুই", "তিন", "চার", "পাঁচ"), 1, "গঠনগতভাবে বাক্য তিন প্রকার—সরল, জটিল ও যৌগিক।"),
            McqQuestion("bng_mcq_19", subject, "‘বাঁশি বাজে’—এটি কোন বাচ্যের উদাহরণ?", listOf("কর্তৃবাচ্য", "কর্মবাচ্য", "ভাববাচ্য", "কর্মকর্তৃবাচ্য"), 3, "কর্তা উহ্য থেকে কর্মই কর্তার মতো কাজ করলে কর্মকর্তৃবাচ্য হয়।"),
            McqQuestion("bng_mcq_20", subject, "‘অদল বদল’ গল্পে অমৃত ও ইসাবের জামা বদলের মূল বার্তা কী?", listOf("প্রতিযোগিতা", "সাম্প্রদায়িক সম্প্রীতি ও বন্ধুত্ব", "ব্যবসায়িক বুদ্ধি", "ভ্রমণ"), 1, "পান্নালাল প্যাটেলের গল্পটি গভীর বন্ধুত্ব ও সাম্প্রদায়িক সম্প্রীতির প্রতীক।"),
            McqQuestion("bng_mcq_21", subject, "‘সিন্ধুতীরে’ কাব্যাংশটি সৈয়দ আলাওলের কোন কাব্য থেকে নেওয়া?", listOf("পদ্মাবতী", "সতীময়না", "সপ্তপয়কর", "তোহফা"), 0, "সৈয়দ আলাওলের ‘পদ্মাবতী’ কাব্যের ‘পদ্মা-সমুদ্র খণ্ড’ থেকে নেওয়া।")
        )

        WbbseSubject.ENGLISH -> listOf(
            McqQuestion("eng_mcq_6", subject, "Who wrote the poem 'My Own True Family'?", listOf("William Cowper", "Ted Hughes", "John Masefield", "R.W. Emerson"), 1, "Ted Hughes wrote 'My Own True Family' urging humans to protect oak trees."),
            McqQuestion("eng_mcq_7", subject, "In 'Our Runaway Kite', what was the name of the island where Philippa and Claude lived?", listOf("Big Half Moon", "Little Moon", "Green Island", "Lighthouse Cove"), 0, "The narrator Philippa, her brother Claude, and Father lived on Big Half Moon island."),
            McqQuestion("eng_mcq_8", subject, "In 'Sea Fever', what does the poet ask for to steer his ship by?", listOf("A compass", "A tall ship and a star", "A lighthouse", "A map"), 1, "John Masefield writes: 'All I ask is a tall ship and a star to steer her by.'"),
            McqQuestion("eng_mcq_9", subject, "According to Andrew Barton Paterson's 'The Cat', where does the cat show his true self?", listOf("In the kitchen", "In the backyard at night", "On the sofa", "In the garden at noon"), 1, "At dusk in the backyard, the cat becomes a grim fighter and true athlete."),
            McqQuestion("eng_mcq_10", subject, "In 'The Snail', what happens when the slightest danger threatens the snail?", listOf("It runs fast", "It shrinks into its house", "It climbs higher", "It makes a sound"), 1, "William Cowper describes how the snail shrinks into its shell with much displeasure."),
            McqQuestion("eng_mcq_11", subject, "Choose the correct Phrasal Verb for 'tolerate':", listOf("put up with", "give in", "carry out", "break into"), 0, "'Put up with' means to tolerate or endure."),
            McqQuestion("eng_mcq_12", subject, "Choose the correct Phrasal Verb for 'resemble':", listOf("take off", "take after", "look into", "set up"), 1, "'Take after' means to resemble a parent or elder."),
            McqQuestion("eng_mcq_13", subject, "Choose the correct Phrasal Verb for 'continue':", listOf("carry on", "call off", "give up", "turn down"), 0, "'Carry on' means to continue doing something."),
            McqQuestion("eng_mcq_14", subject, "Indirect speech of: He said, 'I am busy now.'", listOf("He said that he is busy now.", "He said that he was busy then.", "He told he was busy now.", "He said he had been busy then."), 1, "In reported speech with past reporting verb, 'am' changes to 'was' and 'now' changes to 'then'."),
            McqQuestion("eng_mcq_15", subject, "Fill in the blank: He is senior ___ me by two years.", listOf("than", "from", "to", "with"), 2, "Latin comparatives like senior, junior, superior, inferior take 'to', not 'than'."),
            McqQuestion("eng_mcq_16", subject, "Join into a simple sentence: He saw a snake. He ran away.", listOf("He saw a snake and ran away.", "Seeing a snake, he ran away.", "When he saw a snake, he ran away.", "He ran away because he saw a snake."), 1, "Using the present participle 'Seeing a snake' forms a crisp simple sentence."),
            McqQuestion("eng_mcq_17", subject, "In 'Father's Help', who was the headmaster's substitute on Monday?", listOf("Nobody, he was on leave", "Samuel", "Peon", "Swami's father"), 0, "The peon informed Swami that the headmaster had gone on a week's leave."),
            McqQuestion("eng_mcq_18", subject, "In 'Our Runaway Kite', what letter was pasted on the kite to patch it?", listOf("Father's old letter to Aunt Esther", "A newspaper page", "Claude's exam sheet", "A map"), 0, "The letter Father wrote to his sister Esther reunited the separated family."),
            McqQuestion("eng_mcq_19", subject, "Choose the right article: He is ___ honest man.", listOf("a", "an", "the", "no article"), 1, "'Honest' begins with a silent 'h' vowel sound, so it takes 'an'."),
            McqQuestion("eng_mcq_20", subject, "Change to negative without changing meaning: 'Everyone loves his motherland.'", listOf("No one loves his motherland.", "There is no one who does not love his motherland.", "Everyone hates his motherland.", "Someone loves his motherland."), 1, "Double negation preserves the original affirmative meaning."),
            McqQuestion("eng_mcq_21", subject, "In 'My Own True Family', the oak trees demand the poet swear to plant how many trees when one is felled?", listOf("One", "Two", "Three", "Five"), 1, "The oak trees warn: 'Whenever you see an oak-tree felled, swear now you will plant two.'")
        )

        WbbseSubject.MATHEMATICS -> listOf(
            McqQuestion("mth_mcq_6", subject, "কোনো আসল 10 বছরে দ্বিগুণ হলে বার্ষিক সরল সুদের হার কত?", listOf("5%", "8%", "10%", "12%"), 2, "সুদ I = P হলে r = (100×I)/(P×t) = 100/10 = 10%।"),
            McqQuestion("mth_mcq_7", subject, "x ∝ y এবং y = 8 হলে x = 4; তাহলে y = 12 হলে x কত?", listOf("4", "6", "8", "10"), 1, "x/y = 4/8 = 1/2 ⇒ x = 12/2 = 6।"),
            McqQuestion("mth_mcq_8", subject, "দুটি বৃত্ত পরস্পরকে বহিঃস্পর্শ করলে সাধারণ স্পর্শকের সংখ্যা কত?", listOf("১টি", "২টি", "৩টি", "৪টি"), 2, "বহিঃস্পর্শ করলে ২টি সরল ও ১টি তির্যক — মোট ৩টি সাধারণ স্পর্শক থাকে।"),
            McqQuestion("mth_mcq_9", subject, "বৃত্তস্থ চতুর্ভুজের বিপরীত কোণদ্বয়ের সমষ্টি কত?", listOf("90°", "120°", "180°", "360°"), 2, "উপপাদ্য ৩৮ অনুসারে বৃত্তস্থ চতুর্ভুজের বিপরীত কোণদ্বয় পরস্পর সম্পূরক (180°)।"),
            McqQuestion("mth_mcq_10", subject, "একটি লম্ব বৃত্তাকার চোঙের বক্রতলের ক্ষেত্রফলের সূত্র কী?", listOf("πr²h", "2πrh", "2πr(r+h)", "πrl"), 1, "ব্যাসার্ধ r ও উচ্চতা h হলে চোঙের পার্শ্বতল বা বক্রতলের ক্ষেত্রফল = 2πrh।"),
            McqQuestion("mth_mcq_11", subject, "tan 45° × cot 45° + sin 30°-এর মান কত?", listOf("1", "1.5", "2", "0.5"), 1, "1 × 1 + 0.5 = 1.5।"),
            McqQuestion("mth_mcq_12", subject, "60° কোণের বৃত্তীয় মান (Radian) কত?", listOf("π/6", "π/4", "π/3", "π/2"), 2, "180° = π রেডিয়ান ⇒ 60° = π/3 রেডিয়ান।"),
            McqQuestion("mth_mcq_13", subject, "একটি ঘনকের প্রতিটি বাহুর দৈর্ঘ্য a হলে কর্ণের দৈর্ঘ্য কত?", listOf("a√2", "a√3", "3a", "6a²"), 1, "ঘনকের কর্ণ = √(a²+a²+a²) = a√3 একক।"),
            McqQuestion("mth_mcq_14", subject, "3, 5, 7, 9, 11 তথ্যগুলির যৌগিক গড় কত?", listOf("5", "6", "7", "8"), 2, "(3+5+7+9+11)/5 = 35/5 = 7।"),
            McqQuestion("mth_mcq_15", subject, "অংশীদারি কারবারে লাভ বণ্টিত হয় কোন অনুপাতে?", listOf("বয়সের অনুপাতে", "মূলধনের অনুপাতে", "সমান ভাগে", "শুধু সময়ের অনুপাতে"), 1, "চুক্তি না থাকলে মূলধন ও সময়ের গুণফলের (মূলধনের) অনুপাতে লাভ বণ্টিত হয়।"),
            McqQuestion("mth_mcq_16", subject, "sec²θ - tan²θ এর মান সর্বদা কত?", listOf("0", "1", "-1", "2"), 1, "মৌলিক ত্রিকোণমিতিক অভেদ: sec²θ - tan²θ = 1।"),
            McqQuestion("mth_mcq_17", subject, "বৃত্তের বহিঃস্থ কোনো বিন্দু থেকে বৃত্তের ওপর সর্বাধিক কয়টি স্পর্শক আঁকা যায়?", listOf("১টি", "২টি", "৩টি", "অসংখ্য"), 1, "বহিঃস্থ বিন্দু থেকে বৃত্তে ঠিক ২টি সমান দৈর্ঘ্যের স্পর্শক অঙ্কন করা যায়।"),
            McqQuestion("mth_mcq_18", subject, "চক্রবৃদ্ধি সুদে সমূল চক্রবৃদ্ধির সূত্র (বার্ষিক পর্ব) কোনটি?", listOf("P(1 + rt/100)", "P(1 + r/100)ⁿ", "P × r × n / 100", "P(1 - r/100)ⁿ"), 1, "বার্ষিক r% হারে n বছরের সমূল চক্রবৃদ্ধি A = P(1 + r/100)ⁿ।"),
            McqQuestion("mth_mcq_19", subject, "sin(90° - θ) সমান কত?", listOf("sin θ", "cos θ", "tan θ", "sec θ"), 1, "পূরক কোণের সম্পর্ক: sin(90° - θ) = cos θ।"),
            McqQuestion("mth_mcq_20", subject, "একটি নিরেট অর্ধগোলকের সমগ্রতলের ক্ষেত্রফল কত?", listOf("2πr²", "3πr²", "4πr²", "πr²"), 1, "বক্রতল 2πr² + ভূমিতল πr² = 3πr²।"),
            McqQuestion("mth_mcq_21", subject, "2, 3, 5, 3, 7, 3, 9 তথ্যগুলির সংখ্যাগুরু মান (Mode) কত?", listOf("2", "3", "5", "7"), 1, "3 সবচেয়ে বেশি (৩ বার) এসেছে, তাই সংখ্যাগুরু মান 3।")
        )

        WbbseSubject.PHYSICAL_SCIENCE -> listOf(
            McqQuestion("psc_mcq_6", subject, "কোনটি গ্রিনহাউস গ্যাস নয়?", listOf("CO₂", "CH₄", "N₂O", "N₂ (নাইট্রোজেন)"), 3, "N₂ ও O₂ গ্রিনহাউস গ্যাস নয়; CO₂, CH₄, CFC, N₂O প্রধান গ্রিনহাউস গ্যাস।"),
            McqQuestion("psc_mcq_7", subject, "NTP-তে ১ মোল যেকোনো আদর্শ গ্যাসের আয়তন কত?", listOf("11.2 লিটার", "22.4 লিটার", "24.0 লিটার", "1.0 লিটার"), 1, "অ্যাভোগাড্রো প্রকল্প অনুযায়ী STP/NTP-তে ১ মোল গ্যাসের আয়তন 22.4 L।"),
            McqQuestion("psc_mcq_8", subject, "কঠিনের দৈর্ঘ্য প্রসারণ গুণাঙ্ক (α), ক্ষেত্রফল (β) ও আয়তন প্রসারণ গুণাঙ্ক (γ)-এর সম্পর্ক কী?", listOf("α = β = γ", "α = β/2 = γ/3", "α = 2β = 3γ", "6α = 3β = 2γ"), 1, "α : β : γ = 1 : 2 : 3 অর্থাৎ α = β/2 = γ/3।"),
            McqQuestion("psc_mcq_9", subject, "দন্তচিকিৎসকরা দাঁত দেখার জন্য কোন দর্পণ ব্যবহার করেন?", listOf("সমতল দর্পণ", "অবতল দর্পণ", "উত্তল দর্পণ", "পরাবৃত্তাকার দর্পণ"), 1, "অবতল দর্পণের ফোকাসের মধ্যে বস্তু রাখলে সোজা ও বিবর্ধিত অসদ্‌বিম্ব গঠিত হয়।"),
            McqQuestion("psc_mcq_10", subject, "বাড়ির বৈদ্যুতিক বর্তনীতে যন্ত্রপাতি কোন সমবায়ে যুক্ত থাকে?", listOf("শ্রেণি সমবায়", "সমান্তরাল সমবায়", "মিশ্র সমবায়", "বৃত্তাকার সমবায়"), 1, "সমান্তরাল সমবায়ে প্রতিটি যন্ত্র সমান বিভবপ্রভেদ (220V) পায় ও স্বাধীনভাবে কাজ করে।"),
            McqQuestion("psc_mcq_11", subject, "তড়িৎ ক্ষমতার (Electric Power) SI একক কী?", listOf("জুল", "ওয়াট (W)", "কুলম্ব", "ভোল্ট"), 1, "ক্ষমতা P = VI, যার SI একক ওয়াট (W = J/s)।"),
            McqQuestion("psc_mcq_12", subject, "তেজস্ক্রিয় রশ্মিগুলির মধ্যে কার ভেদন ক্ষমতা সর্বাধিক?", listOf("α-রশ্মি", "β-রশ্মি", "γ-রশ্মি", "ক্যাথোড রশ্মি"), 2, "তড়িৎচুম্বকীয় তরঙ্গ হওয়ায় γ-রশ্মির ভেদন ক্ষমতা সবচেয়ে বেশি।"),
            McqQuestion("psc_mcq_13", subject, "পর্যায় সারণিতে তড়িৎ-ঋণাত্মকতা সর্বাধিক কোন মৌলের?", listOf("ক্লোরিন (Cl)", "অক্সিজেন (O)", "ফ্লুরিন (F)", "নাইট্রোজেন (N)"), 2, "শ্রেণি ১৭-র শীর্ষে থাকা ফ্লুরিনের (F) তড়িৎ-ঋণাত্মকতা সর্বাধিক (4.0)।"),
            McqQuestion("psc_mcq_14", subject, "নীচের কোন যৌগটিতে সমযোজী ত্রিবন্ধন (Triple Bond) রয়েছে?", listOf("CH₄", "C₂H₄", "C₂H₂ (অ্যাসিটিলিন)", "H₂O"), 2, "অ্যাসিটিলিন বা ইথাইনে (H-C≡C-H) কার্বন-কার্বন ত্রিবন্ধন থাকে।"),
            McqQuestion("psc_mcq_15", subject, "তড়িৎ বিশ্লেষণে ক্যাথোডে কোন বিক্রিয়া ঘটে?", listOf("জারণ", "বিজারণ", "প্রশমন", "প্রতিস্থাপন"), 1, "ক্যাথোডে ইলেকট্রন গ্রহণ ঘটে, তাই সর্বদা বিজারণ (Reduction) হয়।"),
            McqQuestion("psc_mcq_16", subject, "পরীক্ষাগারে অ্যামোনিয়া (NH₃) গ্যাস শুষ্ক করতে কী ব্যবহৃত হয়?", listOf("গাঢ় H₂SO₄", "পোড়া চুন (CaO)", "অনাদ্র CaCl₂", "P₂O₅"), 1, "NH₃ ক্ষারীয় হওয়ায় অম্লীয় শোষকের সাথে বিক্রিয়া করে; তাই ক্ষারীয় CaO ব্যবহৃত হয়।"),
            McqQuestion("psc_mcq_17", subject, "অ্যালুমিনিয়ামের প্রধান আকরিক কোনটি?", listOf("হেমাটাইট", "বক্সাইট (Al₂O₃·2H₂O)", "ক্যালামাইন", "কপার পাইরাইটিস"), 1, "বক্সাইট থেকে তড়িৎ বিজারণ পদ্ধতিতে অ্যালুমিনিয়াম নিষ্কাশন করা হয়।"),
            McqQuestion("psc_mcq_18", subject, "অ্যালকোহলের কার্যকরী মূলক (Functional Group) কোনটি?", listOf("-CHO", "-COOH", "-OH", "-CO-"), 2, "হাইড্রক্সিল মূলক (-OH) হলো অ্যালকোহল শ্রেণির কার্যকরী মূলক।"),
            McqQuestion("psc_mcq_19", subject, "প্রিজমের মধ্যে সাদা আলোর বিচ্ছুরণে কোন বর্ণের আলোর চ্যুতি সর্বনিম্ন?", listOf("বেগুনি", "নীল", "সবুজ", "লাল"), 3, "লাল আলোর তরঙ্গদৈর্ঘ্য সর্বাধিক হওয়ায় এর প্রতিসরাঙ্ক ও চ্যুতি সর্বনিম্ন।"),
            McqQuestion("psc_mcq_20", subject, "ফ্লেমিং-এর বামহস্ত নিয়মে মধ্যমা কী নির্দেশ করে?", listOf("চৌম্বক ক্ষেত্র", "তড়িৎপ্রবাহের অভিমুখ", "পরিবাহীর গতির অভিমুখ", "রোধ"), 1, "তর্জনী = চৌম্বক ক্ষেত্র, মধ্যমা = তড়িৎপ্রবাহ, বৃদ্ধাঙ্গুষ্ঠ = বল/গতি।"),
            McqQuestion("psc_mcq_21", subject, "1 BOT (Board of Trade Unit) সমান কত কিলোওয়াট-ঘণ্টা?", listOf("1 kWh", "10 kWh", "100 Wh", "1000 kWh"), 0, "1 BOT = 1 kWh = 3.6 × 10⁶ জুল।")
        )

        else -> emptyList()
    }

    fun extraSaqs(subject: WbbseSubject): List<SaqQuestion> = when (subject) {
        WbbseSubject.BENGALI -> listOf(
            SaqQuestion("bng_saq_4", subject, "‘বহুরূপী’ গল্পে হরিদা বিরাগী সেজে জগদীশবাবুর বাড়ি থেকে প্রণামী নেননি কেন?", 2, "হরিদা প্রকৃত শিল্পীসত্তা ও বিরাগীর আদর্শ অক্ষুণ্ণ রাখতে চেয়েছিলেন। টাকা নিলে তাঁর সন্নাসীর অভিনয়ের পবিত্রতা ও শিল্পগৌরব নষ্ট হয়ে যেত।", listOf("শিল্পী", "বিরাগী", "আদর্শ", "টাকা", "হরিদা")),
            SaqQuestion("bng_saq_5", subject, "‘ফাইট কোনি, ফাইট!’—ক্ষিতীশ সিংহ কেন এই কথা বলেছিলেন?", 2, "দারিদ্র্য, অনাহার ও ক্লাব-রাজনীতির প্রতিকূলতার বিরুদ্ধে লড়াই করে কোনিকে জাতীয় স্তরের সাঁতারু হিসেবে গড়ে তোলার অদম্য জেদ জাগাতে ক্ষিতীশ এই মন্ত্র দিয়েছিলেন।", listOf("দারিদ্র্য", "লড়াই", "সাঁতারু", "জেদ", "ক্ষিতীশ")),
            SaqQuestion("bng_saq_6", subject, "কর্মধারয় ও বহুব্রীহি সমাসের একটি মূল পার্থক্য উদাহরণসহ লেখো।", 2, "কর্মধারয় সমাসে পরপদের অর্থ প্রাধান্য পায় (যেমন: নীল যে পদ্ম = নীলপদ্ম), কিন্তু বহুব্রীহি সমাসে তৃতীয় কোনো অন্য পদের অর্থ প্রধান হয় (যেমন: দশ আনন যার = দশানন/রাবণ)।", listOf("পরপদ", "তৃতীয়", "অন্য পদ", "দশানন", "নীলপদ্ম")),
            SaqQuestion("bng_saq_7", subject, "‘পথের দাবী’ গল্পে গিরীশ মহাপাত্রের চেহারার অদ্ভুত বৈশিষ্ট্য লেখো।", 2, "গিরীশ মহাপাত্র ছিলেন রোগাপাতলা, কাশিতে আক্রান্ত ও বাহারি পোশাকধারী (চকচকে বাহারি জামা, রঙিন মোজা, বাঘ-আঁকা রুমাল), যা ছিল বিপ্লবী সব্যসাচীর নিখুঁত ছদ্মবেশ।", listOf("সব্যসাচী", "ছদ্মবেশ", "বাহারি", "রোগা", "রুমাল")),
            SaqQuestion("bng_saq_8", subject, "‘হারিয়ে যাওয়া কালি কলম’ প্রবন্ধে ফাউন্টেন পেনের বাংলা নাম কী দেওয়া হয়েছে ও কে দিয়েছেন?", 2, "ফাউন্টেন পেনের বাংলা নাম ‘ঝরনা কলম’। রবীন্দ্রনাথ ঠাকুর এই সুন্দর বাংলা পরিভাষাটি দিয়েছেন বলে লেখক শ্রীপান্থ উল্লেখ করেছেন।", listOf("ঝরনা কলম", "রবীন্দ্রনাথ", "শ্রীপান্থ")),
            SaqQuestion("bng_saq_9", subject, "কর্তৃবাচ্য থেকে কর্মবাচ্যে রূপান্তরের দুটি নিয়ম লেখো।", 2, "১. কর্তার সঙ্গে ‘দ্বারা/দিয়া/কর্তৃক’ অনুসর্গ যুক্ত হয়।\n২. ক্রিয়া কর্মের অনুসারী হয় (যেমন: আমি বই পড়ি → আমার দ্বারা বই পঠিত হয়)।", listOf("দ্বারা", "কর্তৃক", "কর্ম", "ক্রিয়া")),
            SaqQuestion("bng_saq_10", subject, "‘অভিষেক’ কাব্যাংশে ইন্দ্রজিৎ নিজেকে ধিক্কার দিয়েছেন কেন?", 2, "প্রিয় ভাই বীরবাহুর মৃত্যু এবং শত্রু রামচন্দ্রের লঙ্কা অবরোধের খবর না জেনে প্রমোদকাননে বিলাসিতায় মত্ত থাকায় বীর ইন্দ্রজিৎ নিজেকে ধিক্কার দিয়েছেন।", listOf("বীরবাহু", "লঙ্কা", "প্রমোদকানন", "ইন্দ্রজিৎ"))
        )

        WbbseSubject.ENGLISH -> listOf(
            SaqQuestion("eng_saq_4", subject, "What did the poet realize after waking up from his dream in 'My Own True Family'?", 2, "When the poet came out of the oakwood, his walk was that of a human child, but his heart had become that of a tree, reminding him of humanity's bond with nature.", listOf("oakwood", "human child", "heart", "tree", "nature")),
            SaqQuestion("eng_saq_5", subject, "Why did Father cry when he saw the patched kite in 'Our Runaway Kite'?", 2, "Father recognized the old letter pasted on the kite that he had written to his estranged sister Esther years ago, realizing she was still alive and thinking of him.", listOf("letter", "esther", "sister", "kite", "reunited")),
            SaqQuestion("eng_saq_6", subject, "What kind of life does the poet long for in 'Sea Fever'?", 2, "The poet longs for a vagrant gypsy life on the lonely sea, guided by a tall ship, a star, wheel's kick, wind's song, and the cry of sea-gulls.", listOf("gypsy", "tall ship", "star", "sea", "wind")),
            SaqQuestion("eng_saq_7", subject, "Why does the author say the cat has more character than most human beings?", 2, "The cat gets more out of life, maintains independence, never submits blindly, and transforms from a gentle house pet into a fearless hunter at night.", listOf("independence", "character", "hunter", "life", "cat")),
            SaqQuestion("eng_saq_8", subject, "Rewrite using Phrasal Verbs: (a) The meeting was postponed. (b) Do not despise the poor.", 2, "(a) The meeting was put off.\n(b) Do not look down upon the poor.", listOf("put off", "look down upon")),
            SaqQuestion("eng_saq_9", subject, "Change the narration: The teacher said to the boys, 'Do not waste time.'", 2, "The teacher advised (or forbade) the boys not to waste time.", listOf("advised", "not to waste", "forbade")),
            SaqQuestion("eng_saq_10", subject, "Split into two simple sentences: 'Hearing the news, she burst into tears.'", 2, "1. She heard the news.\n2. She burst into tears.", listOf("heard the news", "burst into tears"))
        )

        WbbseSubject.MATHEMATICS -> listOf(
            SaqQuestion("mth_saq_4", subject, "বার্ষিক 5% চক্রবৃদ্ধি সুদের হারে 4000 টাকার 2 বছরের সমূল চক্রবৃদ্ধি নির্ণয় করো।", 2, "A = P(1 + r/100)ⁿ = 4000 × (1 + 5/100)² = 4000 × (21/20)² = 4000 × 441/400 = 4410 টাকা।", listOf("4410", "21/20", "4000")),
            SaqQuestion("mth_saq_5", subject, "x² - 6x + k = 0 সমীকরণের বীজদ্বয় বাস্তব ও সমান হলে k-এর মান কত?", 2, "নিরূপক b² - 4ac = 0 ⇒ (-6)² - 4·1·k = 0 ⇒ 36 - 4k = 0 ⇒ k = 9।", listOf("36", "4k", "9", "b² - 4ac")),
            SaqQuestion("mth_saq_6", subject, "যদি tan θ = 4/3 হয় তবে sin θ ও cos θ-এর মান নির্ণয় করো।", 2, "লম্ব = 4k, ভূমি = 3k ⇒ অতিভুজ = √(4²+3²)k = 5k। অতএব sin θ = 4/5 এবং cos θ = 3/5।", listOf("5", "4/5", "3/5")),
            SaqQuestion("mth_saq_7", subject, "একটি গোলকের ব্যাসার্ধ দ্বিগুণ করলে তার আয়তন কত গুণ বৃদ্ধি পাবে?", 2, "আদি আয়তন V₁ = (4/3)πr³। ব্যাসার্ধ 2r হলে V₂ = (4/3)π(2r)³ = 8V₁। অর্থাৎ আয়তন 8 গুণ হবে।", listOf("8", "৮", "2r", "πr³")),
            SaqQuestion("mth_saq_8", subject, "দুটি সদৃশ ত্রিভুজের অনুরূপ বাহুর অনুপাত 3:5 হলে তাদের ক্ষেত্রফলের অনুপাত কত?", 2, "সদৃশ ত্রিভুজের ক্ষেত্রফলের অনুপাত অনুরূপ বাহুর বর্গের অনুপাতের সমান = 3² : 5² = 9 : 25।", listOf("9:25", "9 : 25", "বর্গ")),
            SaqQuestion("mth_saq_9", subject, "একটি লম্ব বৃত্তাকার চোঙের ব্যাসার্ধ 7 সেমি এবং উচ্চতা 10 সেমি হলে আয়তন কত?", 2, "আয়তন V = πr²h = (22/7) × 7 × 7 × 10 = 1540 ঘনসেমি।", listOf("1540", "πr²h", "22/7")),
            SaqQuestion("mth_saq_10", subject, "যদি x ∝ yz এবং y = 2, z = 3 হলে x = 12 হয়, তবে ভেদ ধ্রুবকের মান কত?", 2, "x = kyz ⇒ 12 = k × 2 × 3 ⇒ 6k = 12 ⇒ ভেদ ধ্রুবক k = 2।", listOf("k = 2", "kyz", "2"))
        )

        WbbseSubject.PHYSICAL_SCIENCE -> listOf(
            SaqQuestion("psc_saq_4", subject, "স্থির উষ্ণতায় 2 atm চাপে কোনো গ্যাসের আয়তন 6 লিটার। চাপ 3 atm করলে আয়তন কত হবে?", 2, "বয়েলের সূত্র P₁V₁ = P₂V₂ ⇒ 2 × 6 = 3 × V₂ ⇒ V₂ = 12/3 = 4 লিটার।", listOf("p1v1", "4", "৪", "বয়েল")),
            SaqQuestion("psc_saq_5", subject, "অবতল লেন্সকে অপসারী লেন্স এবং উত্তল লেন্সকে অভিসারী লেন্স বলা হয় কেন?", 2, "উত্তল লেন্স সমান্তরাল আলোকরশ্মিগুচ্ছকে একটি নির্দিষ্ট ফোকাস বিন্দুতে মিলিত (অভিসারী) করে, আর অবতল লেন্স রশ্মিগুচ্ছকে ছড়িয়ে (অপসারী) দেয়।", listOf("ফোকাস", "মিলিত", "অভিসারী", "অপসারী")),
            SaqQuestion("psc_saq_6", subject, "তড়িৎযোজী ও সমযোজী যৌগের দুটি পার্থক্য লেখো।", 2, "১. তড়িৎযোজী যৌগ আয়ন দ্বারা গঠিত ও উচ্চ গলনাঙ্কবিশিষ্ট (যেমন NaCl); সমযোজী যৌগ অণু দ্বারা গঠিত ও নিম্ন গলনাঙ্কবিশিষ্ট (যেমন CH₄)।\n২. তড়িৎযোজী যৌগ গলিত/দ্রবীভূত অবস্থায় তড়িৎ পরিবহণ করে, সমযোজী যৌগ সাধারণত করে না।", listOf("আয়ন", "অণু", "গলনাঙ্ক", "তড়িৎ পরিবহণ")),
            SaqQuestion("psc_saq_7", subject, "অসওয়াল্ড বা হেবার পদ্ধতিতে অ্যামোনিয়া শিল্পোৎপাদনের শর্ত ও সমীকরণ লেখো।", 2, "সমীকরণ: N₂ + 3H₂ ⇌ 2NH₃ + তাপ। শর্ত: লৌহচূর্ণ অনুঘটক, Mo উদ্দীপক, 450°–550°C উষ্ণতা এবং 200 atm চাপ।", listOf("n2", "3h2", "2nh3", "200", "লৌহ")),
            SaqQuestion("psc_saq_8", subject, "ডোবেরাইনারের ত্রয়ী সূত্র ও মেন্ডেলিফের পর্যায় সূত্রের মূল পার্থক্য কী?", 2, "মেন্ডেলিফ পারমাণবিক গুরুত্বের ভিত্তিতে পর্যায় সূত্র দেন, কিন্তু আধুনিক দীর্ঘ পর্যায় সারণি পারমাণবিক ক্রমাঙ্কের (Atomic Number) ওপর প্রতিষ্ঠিত।", listOf("পারমাণবিক গুরুত্ব", "পারমাণবিক ক্রমাঙ্ক", "মেন্ডেলিফ")),
            SaqQuestion("psc_saq_9", subject, "অ্যাম্পিয়ারের সন্তরণ নিয়ম বা ডানহস্ত মুষ্টি নিয়মটি লেখো।", 2, "ডান হাতের বৃদ্ধাঙ্গুষ্ঠ তড়িৎপ্রবাহের অভিমুখে রেখে পরিবাহী ধরলে বাঁকানো আঙুলগুলি সৃষ্ট চৌম্বক বলরেখার অভিমুখ নির্দেশ করে।", listOf("ডান হাত", "তড়িৎপ্রবাহ", "চৌম্বক", "বলরেখা")),
            SaqQuestion("psc_saq_10", subject, "সমাবয়বতা (Isomerism) কাকে বলে? উদাহরণ দাও।", 2, "একই আণবিক সংকেতবিশিষ্ট কিন্তু ভিন্ন গঠন সংকেত ও ধর্মবিশিষ্ট জৈব যৌগগুলিকে সমাবয়ব এবং এই ঘটনাকে সমাবয়বতা বলে। যেমন C₂H₆O-এর দুটি সমাবয়ব: ইথানল (CH₃CH₂OH) ও ডাইমিথাইল ইথার (CH₃OCH₃)।", listOf("আণবিক সংকেত", "গঠন", "ইথানল", "ইথার"))
        )

        else -> emptyList()
    }

    fun extraLongs(subject: WbbseSubject): List<LongQuestion> = when (subject) {
        WbbseSubject.BENGALI -> listOf(
            LongQuestion("bng_long_3", subject, "‘কোনি’ উপন্যাস অবলম্বনে ক্ষিতীশ সিংহ চরিত্রের দৃঢ়তা ও ক্রীড়াপ্রেমের পরিচয় দাও। (৫ নম্বর)", 5, "ভূমিকা (মতি নন্দী, জুপিটার ক্লাব) → সাঁতারের প্রতি নিঃস্বার্থ ভালোবাসা → ষড়যন্ত্রের শিকার হয়েও হার না মানা → বস্তির মেয়ে কোনিকে চ্যাম্পিয়ন গড়ে তোলা → ‘ফাইট কোনি, ফাইট’ আদর্শ → উপসংহার।", listOf("ক্ষিতীশ", "জুপিটার", "কোনি", "ফাইট", "সাঁতার", "মতি নন্দী")),
            LongQuestion("bng_long_4", subject, "‘বহুরূপী’ গল্পে হরিদার চরিত্র বিশ্লেষণ করো এবং গল্পটির নামকরণের সার্থকতা লেখো। (৫ নম্বর)", 5, "লেখক সুবোধ ঘোষ → হরিদার দারিদ্র্য ও স্বাধীন শিল্পীসত্তা → পাগল, বাইজি, পুলিশ ও বিরাগী সাজ → জগদীশবাবুর প্রণামী প্রত্যাখ্যান → বহুরূপী নামকরণের ব্যঞ্জনা।", listOf("সুবোধ ঘোষ", "হরিদা", "বিরাগী", "জগদীশবাবু", "শিল্পী", "বহুরূপী")),
            LongQuestion("bng_long_5", subject, "‘আফ্রিকা’ কবিতায় রবীন্দ্রনাথ ঠাকুরের সাম্রাজ্যবাদ-বিরোধী মানসিকতা ও মানবতাবোধের পরিচয় দাও। (৫ নম্বর)", 5, "রচনা পটভূমি (১৯৩৭, পত্রপুট) → প্রকৃতির কোলে আফ্রিকার জন্ম → ইউরোপীয় বর্বর লোভ ও দাসব্যবসা → ‘মানহারা মানবী’ → যুগান্তরের কবির প্রতি ‘ক্ষমা করো’ আহ্বান।", listOf("রবীন্দ্রনাথ", "সাম্রাজ্যবাদ", "আফ্রিকা", "মানহারা", "ক্ষমা করো", "পত্রপুট"))
        )

        WbbseSubject.ENGLISH -> listOf(
            LongQuestion("eng_long_3", subject, "Write a formal letter to the Editor of an English daily expressing concern about reckless driving and noise pollution in your locality. (10 Marks / Scaled 5)", 5, "Sender's address & date → The Editor, Newspaper Name, Kolkata → Subject line → Salutation (Sir) → Para 1: Purpose → Para 2: Causes & Hazards → Para 3: Practical Remedies → Subscription (Yours faithfully).", listOf("editor", "subject", "sir", "reckless", "noise", "authorities", "yours faithfully")),
            LongQuestion("eng_long_4", subject, "Draft a School Notice as Cultural Secretary inviting students to participate in the Annual Tree Plantation Programme. (5 Marks)", 5, "School Name Header → NOTICE in capitals → Date & Ref No → Catchy Heading → Body (Event, Date, Time, Venue, Eligibility, Last date of submission) → Signature & Designation + Counter-signed by Headmaster.", listOf("notice", "date", "venue", "tree plantation", "headmaster", "secretary")),
            LongQuestion("eng_long_5", subject, "Write a biography of Acharya Jagadish Chandra Bose or Ishwar Chandra Vidyasagar based on WBBSE Writing Skill format. (5 Marks)", 5, "Title → Birth & parentage → Early education & struggle → Major achievements/discoveries → Honours & legacy → Death & lasting tribute.", listOf("born", "education", "contribution", "achievements", "legacy", "bengal"))
        )

        WbbseSubject.MATHEMATICS -> listOf(
            LongQuestion("mth_long_3", subject, "প্রমাণ করো: বৃত্তস্থ চতুর্ভুজের বিপরীত কোণগুলি পরস্পর সম্পূরক। (উপপাদ্য ৩৮) (৫ নম্বর)", 5, "১. প্রদত্ত (O কেন্দ্রবিশিষ্ট বৃত্তে ABCD বৃত্তস্থ চতুর্ভুজ) → ২. প্রামাণ্য (∠ABC + ∠ADC = 180°) → ৩. অঙ্কন (A, O এবং C, O যুক্ত) → ৪. প্রবৃদ্ধ কেন্দ্রস্থ কোণ + কেন্দ্রস্থ কোণ = 360° → ২ দ্বারা ভাগ করে প্রমাণিত।", listOf("বৃত্তস্থ", "সম্পূরক", "180", "প্রবৃদ্ধ", "কেন্দ্রস্থ", "প্রামাণ্য")),
            LongQuestion("mth_long_4", subject, "প্রমাণ করো: বৃত্তের কোনো বিন্দুতে স্পর্শক এবং ওই স্পর্শবিন্দুগামী ব্যাসার্ধ পরস্পর লম্ব। (উপপাদ্য ৪০) (৫ নম্বর)", 5, "১. প্রদত্ত (O কেন্দ্র, AB স্পর্শক, P স্পর্শবিন্দু) → ২. প্রামাণ্য (OP ⊥ AB) → ৩. অঙ্কন (AB-এর ওপর যেকোনো বিন্দু Q নিয়ে OQ যুক্ত) → ৪. Q বৃত্তের বাইরে হওয়ায় OQ > OP → ক্ষুদ্রতম দূরত্বই লম্ব → প্রমাণিত।", listOf("স্পর্শক", "ব্যাসার্ধ", "লম্ব", "ক্ষুদ্রতম", "op", "প্রামাণ্য")),
            LongQuestion("mth_long_5", subject, "একটি স্তম্ভের ছায়ার দৈর্ঘ্য এবং স্তম্ভের উচ্চতার অনুপাত √3 : 1 হলে সূর্যের উন্নতি কোণ নির্ণয় করো এবং ত্রিকোণমিতিক উচ্চতা-দূরত্বের দুটি নিয়ম লেখো। (৫ নম্বর)", 5, "ধরি উচ্চতা h, ছায়া √3h → tan θ = লম্ব/ভূমি = h / (√3h) = 1/√3 = tan 30° → উন্নতি কোণ θ = 30° → সমকোণী ত্রিভুজ চিত্র ও উন্নতি/অবনতি কোণের সংজ্ঞা।", listOf("tan", "1/√3", "30", "উন্নতি কোণ", "লম্ব", "ভূমি"))
        )

        WbbseSubject.PHYSICAL_SCIENCE -> listOf(
            LongQuestion("psc_long_3", subject, "প্রিজমের মধ্য দিয়ে আলোকরশ্মির প্রতিসরণে প্রমাণ করো যে চ্যুতিকোণ δ = i₁ + i₂ - A। (৫ নম্বর)", 5, "রশ্মিচিত্র (প্রিজম ABC, প্রতিসারক কোণ A) → আপতন কোণ i₁, প্রতিসরণ r₁, r₂, নির্গমন i₂ → মোট চ্যুতি δ = (i₁ - r₁) + (i₂ - r₂) → চতুর্ভুজ ও ত্রিভুজ ধর্ম থেকে r₁ + r₂ = A → অতএব δ = i₁ + i₂ - A।", listOf("প্রিজম", "i1", "i2", "r1", "r2", "δ", "a")),
            LongQuestion("psc_long_4", subject, "আধুনিক দীর্ঘ পর্যায় সারণির বরাবর পর্যায় ও শ্রেণি অনুযায়ী পারমাণবিক ব্যাসার্ধ, আয়নায়ন শক্তি ও তড়িৎ-ঋণাত্মকতার পরিবর্তন ব্যাখ্যা করো। (৫ নম্বর)", 5, "পর্যায় বরাবর (বাম→ডান): নিউক্লীয় আধান বৃদ্ধি → ব্যাসার্ধ কমে, আয়নায়ন শক্তি ও তড়িৎ-ঋণাত্মকতা বাড়ে। শ্রেণি বরাবর (উপর→নীচ): নতুন কক্ষপথ যুক্ত → ব্যাসার্ধ বাড়ে, আয়নায়ন শক্তি ও তড়িৎ-ঋণাত্মকতা কমে।", listOf("পর্যায়", "শ্রেণি", "ব্যাসার্ধ", "আয়নায়ন", "তড়িৎ-ঋণাত্মকতা", "কক্ষপথ")),
            LongQuestion("psc_long_5", subject, "তড়িৎ বিশ্লেষণ কাকে বলে? কপার তড়িদ্দ্বার ব্যবহার করে CuSO₄-এর জলীয় দ্রবণের তড়িৎ বিশ্লেষণের ক্যাথোড ও অ্যানোড বিক্রিয়া লেখো। (৫ নম্বর)", 5, "সংজ্ঞা → আয়নন (CuSO₄ → Cu²⁺ + SO₄²⁻) → ক্যাথোড বিক্রিয়া (Cu²⁺ + 2e⁻ → Cu সঞ্চিত) → অ্যানোড বিক্রিয়া (Cu → Cu²⁺ + 2e⁻ ক্ষয়প্রাপ্ত) → তড়িৎ লেপন ও ধাতু নিষ্কাশনে প্রয়োগ।", listOf("ক্যাথোড", "অ্যানোড", "cu2+", "cuso4", "বিজারণ", "জারণ"))
        )

        else -> emptyList()
    }
}
