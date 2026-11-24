package com.example.data.master

import com.example.model.ConfusionComparisonRow
import com.example.model.ConfusionPairItem
import com.example.model.PocketBookCategory
import com.example.model.PocketBookEntry
import com.example.model.WbbseSubject

/**
 * 100% Offline Built-in Vaults for:
 *  - Feature #5: পকেট বুক (Pocket Book — Math formulas/theorems, Physics reactions/formulas,
 *    Life Science hormones/facts, History dates/treaties/leaders, Bengali/English author-source lists)
 *  - Feature #8: কনফিউশন ক্লিনার (Side-by-side comparison tables & memory tricks for all 7 WBBSE subjects)
 */
object PocketBookAndConfusionBank {

    val pocketBookEntries: List<PocketBookEntry> = listOf(
        // ==================== MATH FORMULAS & THEOREMS ====================
        PocketBookEntry(
            id = "pb_math_1",
            category = PocketBookCategory.MATH_FORMULAS,
            subject = WbbseSubject.MATHEMATICS,
            title = "সরল সুদ (Simple Interest)",
            formulaOrFact = "I = (P × t × r) / 100   |   সবৃদ্ধিমূল (A) = P + I",
            examTip = "t সবসময় বছরে ও r শতকরা বার্ষিক হারে বসাবে; উত্তরে ‘টাকা’ একক লিখবে।"
        ),
        PocketBookEntry(
            id = "pb_math_2",
            category = PocketBookCategory.MATH_FORMULAS,
            subject = WbbseSubject.MATHEMATICS,
            title = "চক্রবৃদ্ধি সুদ ও সমহার বৃদ্ধি/হ্রাস",
            formulaOrFact = "A = P(1 + r/100)ⁿ  |  ষাণ্মাসিক: A = P(1 + r/200)²ⁿ  |  হ্রাস: A = P(1 - r/100)ⁿ",
            examTip = "চক্রবৃদ্ধি সুদ (CI) = A - P; পর্বসংখ্যা (বার্ষিক/ষাণ্মাসিক/ত্রৈমাসিক) আগে চিহ্নিত করো।"
        ),
        PocketBookEntry(
            id = "pb_math_3",
            category = PocketBookCategory.MATH_FORMULAS,
            subject = WbbseSubject.MATHEMATICS,
            title = "শ্রীধর আচার্যের সূত্র ও নিরূপক (Quadratic)",
            formulaOrFact = "ax² + bx + c = 0 ⇒ x = (-b ± √(b² - 4ac)) / 2a\nনিরূপক D = b² - 4ac (D>0 বাস্তব অসমান, D=0 বাস্তব সমান, D<0 অবাস্তব)\nবীজদ্বয়ের সমষ্টি α+β = -b/a, গুণফল αβ = c/a",
            examTip = "a ≠ 0 শর্তটি অবশ্যই লিখবে।"
        ),
        PocketBookEntry(
            id = "pb_math_4",
            category = PocketBookCategory.MATH_FORMULAS,
            subject = WbbseSubject.MATHEMATICS,
            title = "গোলক ও অর্ধগোলক (Sphere & Hemisphere)",
            formulaOrFact = "গোলকের বক্রতলের ক্ষেত্রফল = 4πr²  |  আয়তন = (4/3)πr³\nনিরেট অর্ধগোলকের সমগ্রতল = 3πr²  |  আয়তন = (2/3)πr³",
            examTip = "ব্যাস দেওয়া থাকলে আগে r = ব্যাস / 2 বের করে নাও।"
        ),
        PocketBookEntry(
            id = "pb_math_5",
            category = PocketBookCategory.MATH_FORMULAS,
            subject = WbbseSubject.MATHEMATICS,
            title = "লম্ব বৃত্তাকার চোঙ (Right Circular Cylinder)",
            formulaOrFact = "পার্শ্বতলের ক্ষেত্রফল = 2πrh  |  সমগ্রতলের ক্ষেত্রফল = 2πr(r + h)  |  আয়তন = πr²h\nফাঁপা চোঙের আয়তন = π(R² - r²)h",
            examTip = "ক্ষেত্রফলের একক বর্গ একক ও আয়তনের একক ঘন একক।"
        ),
        PocketBookEntry(
            id = "pb_math_6",
            category = PocketBookCategory.MATH_FORMULAS,
            subject = WbbseSubject.MATHEMATICS,
            title = "লম্ব বৃত্তাকার শঙ্কু (Right Circular Cone)",
            formulaOrFact = "তির্যক উচ্চতা l = √(r² + h²)  |  পার্শ্বতল = πrl  |  সমগ্রতল = πr(r + l)  |  আয়তন = (1/3)πr²h",
            examTip = "তাঁবুর ত্রিপল বের করতে কেবল পার্শ্বতল (πrl) লাগে, ভূমি নয়।"
        ),
        PocketBookEntry(
            id = "pb_math_7",
            category = PocketBookCategory.MATH_FORMULAS,
            subject = WbbseSubject.MATHEMATICS,
            title = "VVI মাধ্যমিক উপপাদ্য তালিকা (৫ নম্বর)",
            formulaOrFact = "• উপপাদ্য ৩৪: কেন্দ্রস্থ কোণ পরিধিস্থ কোণের দ্বিগুণ।\n• উপপাদ্য ৩৭: অর্ধবৃত্তস্থ কোণ সমকোণ।\n• উপপাদ্য ৩৮: বৃত্তস্থ চতুর্ভুজের বিপরীত কোণ পরস্পর সম্পূরক।\n• উপপাদ্য ৪১/৪২: বহিঃস্থ বিন্দু থেকে অঙ্কিত স্পর্শকদ্বয় সমান।\n• উপপাদ্য ৪৮/৪৯/৫০: পিথাগোরাসের উপপাদ্য ও বিপরীত উপপাদ্য।",
            examTip = "উপপাদ্যে প্রদত্ত → প্রামাণ্য → অঙ্কন → প্রমাণ + পেনসিল চিত্র বাধ্যতামূলক।"
        ),
        PocketBookEntry(
            id = "pb_math_8",
            category = PocketBookCategory.MATH_FORMULAS,
            subject = WbbseSubject.MATHEMATICS,
            title = "ত্রিকোণমিতি মূল অভেদ ও মান",
            formulaOrFact = "sin²θ + cos²θ = 1  |  sec²θ - tan²θ = 1  |  cosec²θ - cot²θ = 1\ntan 30° = 1/√3, tan 45° = 1, tan 60° = √3  |  π রেডিয়ান = 180°",
            examTip = "উচ্চতা ও দূরত্ব অঙ্কে উন্নতি কোণ ও অবনতি কোণের চিত্র আঁকবে।"
        ),

        // ==================== PHYSICS FORMULAS & REACTIONS ====================
        PocketBookEntry(
            id = "pb_phy_1",
            category = PocketBookCategory.PHYSICS_REACTIONS,
            subject = WbbseSubject.PHYSICAL_SCIENCE,
            title = "গ্যাসের সূত্র ও আদর্শ গ্যাস সমীকরণ",
            formulaOrFact = "বয়েল: P₁V₁ = P₂V₂ (T স্থির)  |  চার্লস: V₁/T₁ = V₂/T₂ (P স্থির)\nসমন্বয়: (P₁V₁)/T₁ = (P₂V₂)/T₂  |  আদর্শ গ্যাস: PV = nRT\nR = 0.082 L·atm·mol⁻¹·K⁻¹ = 8.314 J·mol⁻¹·K⁻¹",
            examTip = "সেলসিয়াস তাপমাত্রাকে অবশ্যই কেলভিনে (T = t + 273) রূপান্তর করবে।"
        ),
        PocketBookEntry(
            id = "pb_phy_2",
            category = PocketBookCategory.PHYSICS_REACTIONS,
            subject = WbbseSubject.PHYSICAL_SCIENCE,
            title = "আলো: দর্পণ, প্রিজম ও স্নেলের সূত্র",
            formulaOrFact = "f = R / 2  |  স্নেলের সূত্র: μ = sin i / sin r = c / v\nপ্রিজমের চ্যুতিকোণ: δ = i₁ + i₂ - A  (যেখানে A = r₁ + r₂)",
            examTip = "বেগুনি আলোর চ্যুতি সর্বাধিক এবং লাল আলোর চ্যুতি সর্বনিম্ন।"
        ),
        PocketBookEntry(
            id = "pb_phy_3",
            category = PocketBookCategory.PHYSICS_REACTIONS,
            subject = WbbseSubject.PHYSICAL_SCIENCE,
            title = "চলতড়িৎ: ওহম, রোধ, জুল ও BOT একক",
            formulaOrFact = "V = IR  |  R = ρ(l/A)  |  শ্রেণি: R = R₁+R₂  |  সমান্তরাল: 1/R = 1/R₁+1/R₂\nজুল: H = I²Rt / J  |  ক্ষমতা P = VI = I²R = V²/R\n1 BOT = 1 kWh = (Watt × Hour) / 1000",
            examTip = "ফিউজ তার: সিসা (Pb) + টিন (Sn) — উচ্চ রোধাঙ্ক ও নিম্ন গলনাঙ্ক।"
        ),
        PocketBookEntry(
            id = "pb_phy_4",
            category = PocketBookCategory.PHYSICS_REACTIONS,
            subject = WbbseSubject.PHYSICAL_SCIENCE,
            title = "পরীক্ষাগারে অ্যামোনিয়া, H₂S ও N₂ বিক্রিয়া",
            formulaOrFact = "• NH₃ প্রস্তুতি: 2NH₄Cl + Ca(OH)₂ → CaCl₂ + 2NH₃↑ + 2H₂O (শোষক: CaO)\n• H₂S প্রস্তুতি: FeS + H₂SO₄ (লঘু) → FeSO₄ + H₂S↑\n• হেবার পদ্ধতি: N₂ + 3H₂ ⇌ 2NH₃ (Fe চূর্ণ অনুঘটক, 450-500°C, 200 atm)\n• নেসলার বিকারক + NH₃ → বাদামি অধঃক্ষেপ",
            examTip = "NH₃ ক্ষারধর্মী তাই গাঢ় H₂SO₄ বা P₂O₅ দিয়ে শুষ্ক করা যায় না, CaO লাগে।"
        ),
        PocketBookEntry(
            id = "pb_phy_5",
            category = PocketBookCategory.PHYSICS_REACTIONS,
            subject = WbbseSubject.PHYSICAL_SCIENCE,
            title = "জৈব রসায়ন (IUPAC ও VVI বিক্রিয়া)",
            formulaOrFact = "• অ্যালকেন: CₙH₂ₙ₊₂ | অ্যালকিন: CₙH₂ₙ | অ্যালকাইন: CₙH₂ₙ₋₂\n• ইথিলিন + H₂ (Ni অনুঘটক) → ইথেন (যুত বিক্রিয়া)\n• CH₃COOH + C₂H₅OH (গাঢ় H₂SO₄) → CH₃COOC₂H₅ + H₂O (এস্টারিফিকেশন)\n• ক্যালশিয়াম কার্বাইড + জল: CaC₂ + 2H₂O → C₂H₂↑ + Ca(OH)₂",
            examTip = "সঠিক অনুঘটক ও শর্ত তীরচিহ্নের উপরে লিখলে পূর্ণ নম্বর পাওয়া যায়।"
        ),

        // ==================== LIFE SCIENCE KEY FACTS ====================
        PocketBookEntry(
            id = "pb_bio_1",
            category = PocketBookCategory.LIFE_SCIENCE_KEYS,
            subject = WbbseSubject.LIFE_SCIENCE,
            title = "উদ্ভিদ ও প্রাণী হরমোন (১০০% VVI)",
            formulaOrFact = "• অক্সিন (IAA): অগ্রস্থ প্রকটতা, ফটোট্রপিক চলন।\n• জিব্বেরেলিন (GA): বীজের সুপ্তাবস্থা ভঙ্গ, পর্বমধ্যের দৈর্ঘ্য বৃদ্ধি।\n• সাইটোকাইনিন: কোষ বিভাজন, পত্রমোচন বিলম্বিত করা।\n• ইনসুলিন (β-কোষ): রক্তে শর্করা কমায় (অভাবে ডায়াবেটিস মেলিটাস)।\n• অ্যাড্রিনালিন: জরুরীকালীন/আপৎকালীন হরমোন।",
            examTip = "অন্তঃক্ষরা ও বহিঃক্ষরা গ্রন্থির উদাহরণসহ পার্থক্য মনে রাখবে।"
        ),
        PocketBookEntry(
            id = "pb_bio_2",
            category = PocketBookCategory.LIFE_SCIENCE_KEYS,
            subject = WbbseSubject.LIFE_SCIENCE,
            title = "মেন্ডেলের অনুপাত ও বংশগতি রোগ",
            formulaOrFact = "• একসংকর জনন: ফিনোটাইপ = 3:1, জিনোটাইপ = 1:2:1\n• অসম্পূর্ণ প্রকটতা (সন্ধ্যামালতী): ফিনোটাইপ = জিনোটাইপ = 1:2:1\n• দ্বিসংকর জনন: ফিনোটাইপ = 9:3:3:1\n• অটোজোমাল রোগ: থ্যালাসেমিয়া | X-ক্রোমোজোমবাহিত: হিমোফিলিয়া, বর্ণান্ধতা",
            examTip = "চেকারবোর্ডে গ্যামেটগুলি বৃত্তের ভেতর অবশ্যই দেখাবে।"
        ),
        PocketBookEntry(
            id = "pb_bio_3",
            category = PocketBookCategory.LIFE_SCIENCE_KEYS,
            subject = WbbseSubject.LIFE_SCIENCE,
            title = "নাইট্রোজেন চক্র ও জৈববৈচিত্র্য হটস্পট",
            formulaOrFact = "• নাইট্রোজেন স্থিতিকরণ: রাইজোবিয়াম, অ্যাজোটোব্যাকটর, অ্যানাবিনা।\n• নাইট্রিফিকেশন: নাইট্রোসোমোনাস (NH₃→NO₂⁻), নাইট্রোব্যাকটর (NO₂⁻→NO₃⁻)।\n• ডিনাইট্রিফিকেশন: সিউডোমোনাস, থায়োব্যাসিলাস।\n• ভারতের ৪টি হটস্পট: পূর্ব হিমালয়, ইন্দো-বার্মা, পশ্চিমঘাট-শ্রীলঙ্কা, সুন্দাল্যান্ড।",
            examTip = "ইন-সিটু (জাতীয় উদ্যান, অভয়ারণ্য) ও এক্স-সিটু (চিড়িয়াখানা, ক্রায়োসংরক্ষণ) উদাহরণ VVI।"
        ),

        // ==================== HISTORY DATES, TREATIES & LEADERS ====================
        PocketBookEntry(
            id = "pb_his_1",
            category = PocketBookCategory.HISTORY_DATES,
            subject = WbbseSubject.HISTORY,
            title = "ঊনিশ শতকের নবজাগরণ ও শিক্ষা (সাল)",
            formulaOrFact = "• ১৮১৭: হিন্দু কলেজ প্রতিষ্ঠা  |  • ১৮১৮: বামাবোধিনী নয় — দিগদর্শন ও সমাচার দর্পণ\n• ১৮২৯: সতীদাহ প্রথা রদ (রামমোহন ও লর্ড বেন্টিঙ্ক)\n• ১৮৩৫: কলকাতা মেডিক্যাল কলেজ প্রতিষ্ঠা\n• ১৮৫৬: বিধবাবিবাহ আইন পাস (ঈশ্বরচন্দ্র বিদ্যাসাগর)\n• ১৮৫৭: কলকাতা বিশ্ববিদ্যালয় প্রতিষ্ঠা  |  • ১৮৬৩: বামাবোধিনী পত্রিকা (উমেশচন্দ্র দত্ত)",
            examTip = "প্রতিটি ৪ ও ৮ নম্বরের উত্তরে কমপক্ষে ৩–৪টি সঠিক সাল আন্ডারলাইন করো।"
        ),
        PocketBookEntry(
            id = "pb_his_2",
            category = PocketBookCategory.HISTORY_DATES,
            subject = WbbseSubject.HISTORY,
            title = "প্রতিরোধ ও মহাবিদ্রোহ (সাল ও নেতা)",
            formulaOrFact = "• ১৭৬৩–১৮০০: সন্ন্যাসী-ফকির বিদ্রোহ (ভবানী পাঠক, দেবী চৌধুরানী, মজনু শাহ)\n• ১৭৯৮–৯৯: চুয়াড় বিদ্রোহ (দুর্জন সিংহ, রানি শিরোমণি)\n• ১৮৩১–৩২: কোল বিদ্রোহ (বুদ্ধ ভগৎ, জোয়া ভগৎ) ও বারাসত বিদ্রোহ (তিতুমির)\n• ১৮৫৫: সাঁওতাল বিদ্রোহ / হুল (সিধু ও কানহু)\n• ১৮৫৭: মহাবিদ্রোহ (মঙ্গল পাণ্ডে, রানি লক্ষ্মীবাঈ, নানা সাহেব, কুনওয়ার সিং)\n• ১৮৫৯–৬০: নীল বিদ্রোহ (বিষ্ণুচরণ ও দিগম্বর বিশ্বাস; ‘নীলদর্পণ’ — দীনবন্ধু মিত্র)",
            examTip = "ম্যাপ পয়েন্টিংয়ে মিরাট, ব্যারাকপুর, ঝাঁসি, কানপুর, দিল্লি, সাঁওতাল পরগনা VVI।"
        ),
        PocketBookEntry(
            id = "pb_his_3",
            category = PocketBookCategory.HISTORY_DATES,
            subject = WbbseSubject.HISTORY,
            title = "বিংশ শতকের আন্দোলন ও স্বাধীনোত্তর ভারত",
            formulaOrFact = "• ১৯০৫ (১৬ অক্টোবর): বঙ্গভঙ্গ ও অরন্ধন/রাখীবন্ধন (রবীন্দ্রনাথ)\n• ১৯২০: অসহযোগ আন্দোলন ও AITUC (লালা লাজপত রায়)\n• ১৯২৮: বারদৌলি সত্যাগ্রহ (বল্লভভাই প্যাটেল — ‘সর্দার’ উপাধি)\n• ১৯৩০: আইন অমান্য ও চট্টগ্রাম অস্ত্রাগার লুণ্ঠন (মাস্টারদা সূর্য সেন, প্রীতিলতা)\n• ১৯৪২: ভারত ছাড়ো আন্দোলন (মাতঙ্গিনী হাজরা — তাম্রলিপ্ত জাতীয় সরকার)\n• ১৯৪৭–৫০: দেশীয় রাজ্য ভারতভুক্তি (কাশ্মীর, হায়দ্রাবাদ, জুনাগড়)",
            examTip = "হরি সিং (কাশ্মীর), অপারেশন পোলো (হায়দ্রাবাদ), গণভোট (জুনাগড়) ১ নম্বরে আসে।"
        ),

        // ==================== BENGALI & ENGLISH AUTHOR-SOURCE LISTS ====================
        PocketBookEntry(
            id = "pb_lit_1",
            category = PocketBookCategory.LITERATURE_AUTHORS,
            subject = WbbseSubject.BENGALI,
            title = "বাংলা গদ্য, নাটক ও সহায়ক পাঠ (লেখক ও উৎস)",
            formulaOrFact = "• জ্ঞানচক্ষু — আশাপূর্ণা দেবী (কুমকুম গল্পগ্রন্থ)\n• বহুরূপী — সুবোধ ঘোষ (গল্পসমগ্র ৩য় খণ্ড)\n• পথের দাবী — শরৎচন্দ্র চট্টোপাধ্যায় (‘পথের দাবী’ উপন্যাস)\n• অদল বদল — পান্নালাল প্যাটেল (অনুবাদ: অর্ঘ্যকুসুম দত্তগুপ্ত)\n• নদীর বিদ্রোহ — মানিক বন্দ্যোপাধ্যায় (সরীসৃপ গল্পগ্রন্থ)\n• হারিয়ে যাওয়া কালি কলম — শ্রীপান্থ (নিখিল সরকার)\n• বাংলা ভাষায় বিজ্ঞান — রাজশেখর বসু (বিচিন্তা)\n• সিরাজদ্দৌলা (নাট্যাংশ) — শচীন্দ্রনাথ সেনগুপ্ত\n• কোনি (উপন্যাস) — মতি নন্দী",
            examTip = "প্রসঙ্গ উল্লেখ (Reference) লেখার সময় লেখক ও মূল গ্রন্থের নাম নির্ভুলভাবে লিখবে।"
        ),
        PocketBookEntry(
            id = "pb_lit_2",
            category = PocketBookCategory.LITERATURE_AUTHORS,
            subject = WbbseSubject.BENGALI,
            title = "বাংলা কবিতা (কবি ও মূল কাব্যগ্রন্থ)",
            formulaOrFact = "• অসুখী একজন — পাবলো নেরুদা (অনুবাদ: নবারুণ ভট্টাচার্য — ‘বিদেশি ফুলে রক্তের ছিটে’)\n• আয় আরো বেঁধে বেঁধে থাকি — শঙ্খ ঘোষ (‘জলই পাষাণ হয়ে আছে’)\n• আফ্রিকা — রবীন্দ্রনাথ ঠাকুর (‘পত্রপুট’)\n• অভিষেক — মাইকেল মধুসূদন দত্ত (‘মেঘনাদবধ কাব্য’ ১ম সর্গ)\n• প্রলয়োল্লাস — কাজী নজরুল ইসলাম (‘অগ্নিবীণা’)\n• সিন্ধুতীরে — সৈয়দ আলাওল (‘পদ্মাবতী’)\n• অস্ত্রের বিরুদ্ধে গান — জয় গোস্বামী (‘পাতার পোশাক’)",
            examTip = "অনুবাদ কবিতায় মূল কবি ও অনুবাদক — দুজনের নামই MCQ-তে আসে!"
        ),
        PocketBookEntry(
            id = "pb_lit_3",
            category = PocketBookCategory.LITERATURE_AUTHORS,
            subject = WbbseSubject.ENGLISH,
            title = "English Bliss — All 8 Lessons (Author & Genre)",
            formulaOrFact = "• L1: Father's Help — R. K. Narayan (Prose, from Malgudi Days)\n• L2: Fable — Ralph Waldo Emerson (Poem)\n• L3: The Passing Away of Bapu — Nayantara Sehgal (Prose, Prison and Chocolate Cake)\n• L4: My Own True Family — Ted Hughes (Poem)\n• L5: Our Runaway Kite — Lucy Maud Montgomery (Prose)\n• L6: Sea Fever — John Masefield (Poem)\n• L7: The Cat — Andrew Barton Paterson (Prose)\n• L8: The Snail — William Cowper (Poem)",
            examTip = "Seen passage-এর লেখক ও কবিতার কবির বানান একদম নির্ভুল মুখস্থ রেখো।"
        )
    )

    // ==================== #8. কনফিউশন ক্লিনার (All 7 Subjects) ====================

    val confusionPairs: List<ConfusionPairItem> = listOf(
        // Physical Science
        ConfusionPairItem(
            id = "conf_phy_1",
            subject = WbbseSubject.PHYSICAL_SCIENCE,
            leftTitle = "তড়িৎচালক বল (EMF)",
            rightTitle = "বিভব প্রভেদ (Potential Diff.)",
            rows = listOf(
                ConfusionComparisonRow("সংজ্ঞা", "মুক্ত বর্তনীতে কোষের দুই মেরুর বিভব পার্থক্য", "বদ্ধ বর্তনীতে যেকোনো দুটি বিন্দুর বিভব পার্থক্য"),
                ConfusionComparisonRow("মান", "সর্বদা বিভব প্রভেদের চেয়ে বড় (E > V)", "কোষের অভ্যন্তরীণ রোধের জন্য E অপেক্ষা ছোট (V = E - Ir)"),
                ConfusionComparisonRow("কারণ/ফল", "তড়িৎ প্রবাহের মূল কারণ", "তড়িৎ প্রবাহের ফল")
            ),
            memoryTrickBn = "মনে রাখার ট্রিক: E মানে ‘Engine’ (মুক্ত বর্তনীর মূল শক্তি, সবসময় বড়), V মানে ‘Value in circuit’ (বদ্ধ বর্তনীতে কিছুটা খরচ হওয়ার পর পাওয়া মান)!"
        ),
        ConfusionPairItem(
            id = "conf_phy_2",
            subject = WbbseSubject.PHYSICAL_SCIENCE,
            leftTitle = "তড়িৎযোজী যৌগ (Ionic)",
            rightTitle = "সমযোজী যৌগ (Covalent)",
            rows = listOf(
                ConfusionComparisonRow("বন্ধন গঠন", "ইলেকট্রন গ্রহণ-বর্জনের মাধ্যমে (যেমন NaCl)", "ইলেকট্রন জোড় সমভাবে ব্যবহারের মাধ্যমে (যেমন CH₄, H₂O)"),
                ConfusionComparisonRow("গলনাঙ্ক/স্ফুটনাঙ্ক", "অত্যন্ত উচ্চ, কঠিন কেলাসাকার", "তুলনামূলক কম, সাধারণত গ্যাস বা তরল"),
                ConfusionComparisonRow("তড়িৎ পরিবাহিতা", "গলিত বা দ্রবীভূত অবস্থায় তড়িৎ পরিবাহী", "সাধারণত তড়িৎ অবিশ্লেষ্য")
            ),
            memoryTrickBn = "মনে রাখার ট্রিক: ‘তড়িৎ-যোজী’ = ধাতু + অধাতু (দান-গ্রহণ), ‘সম-যোজী’ = অধাতু + অধাতু (সমান শেয়ারিং)!"
        ),
        ConfusionPairItem(
            id = "conf_phy_3",
            subject = WbbseSubject.PHYSICAL_SCIENCE,
            leftTitle = "অবতল দর্পণ (Concave)",
            rightTitle = "উত্তল দর্পণ (Convex)",
            rows = listOf(
                ConfusionComparisonRow("প্রতিবিম্ব", "সদ ও অসদ উভয়ই; বড় প্রতিবিম্ব তৈরি করে", "সর্বদা অসদ, সমশীর্ষ ও খর্বাকার প্রতিবিম্ব"),
                ConfusionComparisonRow("ব্যবহার", "গাড়ির হেডলাইট, দন্তচিকিৎসক, দাড়ি কামানো", "গাড়ির রিয়ার-ভিউ মিরর (দৃষ্টিক্ষেত্র বড়)")
            ),
            memoryTrickBn = "ট্রিক: অব-তল (গুহার মতো ভেতরে ঢোকা → দন্তচিকিৎসক ও হেডলাইট), উ-ত্তল (উঁচু → দূর থেকে বড় রাস্তা দেখায় = ভিউ-ফাইন্ডার)!"
        ),

        // Life Science
        ConfusionPairItem(
            id = "conf_bio_1",
            subject = WbbseSubject.LIFE_SCIENCE,
            leftTitle = "মাইটোসিস (Mitosis)",
            rightTitle = "মিয়োসিস (Meiosis)",
            rows = listOf(
                ConfusionComparisonRow("ঘটনাস্থল", "দেহকোষে ঘটে", "জনন মাতৃকোষে (শুক্রাশয়/ডিম্বাশয়) ঘটে"),
                ConfusionComparisonRow("ক্রোমোজোম সংখ্যা", "অপত্য কোষে সমান থাকে (2n → 2n) — সমবিভাজন", "অর্ধেক হয়ে যায় (2n → n) — হ্রাসবিভাজন"),
                ConfusionComparisonRow("অপত্য কোষ ও ক্রসিং ওভার", "২টি অপত্য কোষ; ক্রসিং ওভার ঘটে না", "৪টি অপত্য কোষ; প্যাকাইটিনে ক্রসিং ওভার ঘটে")
            ),
            memoryTrickBn = "ট্রিক: ‘মাই-টো’ = টু (২টি সমান কোষ), ‘মি-ও’ = হ্রাস (ক্রোমোজোম অর্ধেক, ৪টি গ্যামেট)!"
        ),
        ConfusionPairItem(
            id = "conf_bio_2",
            subject = WbbseSubject.LIFE_SCIENCE,
            leftTitle = "ট্রপিক চলন (Tropic)",
            rightTitle = "ন্যাস্টিক চলন (Nastic)",
            rows = listOf(
                ConfusionComparisonRow("নিয়ন্ত্রক", "উদ্দীপকের গতিপথ দ্বারা নিয়ন্ত্রিত", "উদ্দীপকের তীব্রতা দ্বারা নিয়ন্ত্রিত"),
                ConfusionComparisonRow("প্রকৃতি", "বৃদ্ধিজ চলন (অক্সিন হরমোন যুক্ত)", "রসস্ফীতিজনিত চলন"),
                ConfusionComparisonRow("উদাহরণ", "সূর্যের দিকে কাণ্ডের বৃদ্ধি (ফটোট্রপিক)", "স্পর্শে লজ্জাবতীর পাতা মুদে যাওয়া (সিসমোন্যাস্টিক)")
            ),
            memoryTrickBn = "ট্রিক: ট্র-পি-ক = ‘পথ’ (গতিপথ), ন্যা-স্টি-ক = ‘তীব্রতা’ (লজ্জাবতী ও সূর্যমুখী)!"
        ),
        ConfusionPairItem(
            id = "conf_bio_3",
            subject = WbbseSubject.LIFE_SCIENCE,
            leftTitle = "ইন-সিটু সংরক্ষণ (In-situ)",
            rightTitle = "এক্স-সিটু সংরক্ষণ (Ex-situ)",
            rows = listOf(
                ConfusionComparisonRow("স্থান", "নিজস্ব প্রাকৃতিক বাসস্থানের ভেতরেই সংরক্ষণ", "প্রাকৃতিক বাসস্থানের বাইরে কৃত্রিম পরিবেশে"),
                ConfusionComparisonRow("উদাহরণ", "জাতীয় উদ্যান, অভয়ারণ্য, বায়োস্ফিয়ার রিজার্ভ", "চিড়িয়াখানা, বোটানিক্যাল গার্ডেন, ক্রায়োসংরক্ষণ (-196°C তরল N₂)")
            ),
            memoryTrickBn = "ট্রিক: ‘In’ মানে ঘরের ভেতরে (জঙ্গল/অভয়ারণ্য), ‘Ex’ মানে বাইরে (চিড়িয়াখানা/ল্যাব)!"
        ),

        // History
        ConfusionPairItem(
            id = "conf_his_1",
            subject = WbbseSubject.HISTORY,
            leftTitle = "সন্ন্যাসী-ফকির বিদ্রোহ",
            rightTitle = "চুয়াড় বিদ্রোহ",
            rows = listOf(
                ConfusionComparisonRow("সময়কাল ও অঞ্চল", "১৭৬৩–১৮০০ খ্রি. • বাংলা ও বিহার (ঢাকা, রংপুর, দিনাজপুর)", "১৭৬৮–১৭৯৯ খ্রি. • জঙ্গলমহল (মেদিনীপুর, বাঁকুড়া, ধলভূম)"),
                ConfusionComparisonRow("মূল কারণ", "তীর্থকর আরোপ ও ছিয়াত্তরের মন্বন্তর", "পাইকান জমি বাজেয়াপ্ত ও উচ্চ রাজস্ব"),
                ConfusionComparisonRow("প্রধান নেতা", "ভবানী পাঠক, দেবী চৌধুরানী, মজনু শাহ", "জগন্নাথ সিংহ, দুর্জন সিংহ, রানি শিরোমণি")
            ),
            memoryTrickBn = "ট্রিক: ‘সন্ন্যাসী-ফকির’ = আনন্দমঠ + তীর্থকর; ‘চুয়াড়’ = জঙ্গলমহলের পাইক + রানি শিরোমণি!"
        ),
        ConfusionPairItem(
            id = "conf_his_2",
            subject = WbbseSubject.HISTORY,
            leftTitle = "সাঁওতাল বিদ্রোহ (১৮৫৫)",
            rightTitle = "মুন্ডা বিদ্রোহ (১৮৯৯–১৯০০)",
            rows = listOf(
                ConfusionComparisonRow("অন্য নাম ও অঞ্চল", "‘হুল’ • দামিন-ই-কোহ (সাঁওতাল পরগনা)", "‘উলগুলান’ • ছোটনাগপুর ও রাঁচি অঞ্চল"),
                ConfusionComparisonRow("নেতা", "সিধু ও কানহু (চাঁদ, ভৈরব)", "বিরসা মুন্ডা (‘ধরতি আবা’)"),
                ConfusionComparisonRow("প্রধান কারণ", "দিকু (মহাজন-জমিদার) শোষণ ও রেলপথ নির্মাণ", "খুঁটকাঠি প্রথা বিলোপ ও বেঠবেগারি")
            ),
            memoryTrickBn = "ট্রিক: সাঁওতাল = হুল + সিধু-কানহু; মুন্ডা = উলগুলান + বিরসা + খুঁটকাঠি!"
        ),

        // Geography
        ConfusionPairItem(
            id = "conf_geo_1",
            subject = WbbseSubject.GEOGRAPHY,
            leftTitle = "নদীর ক্ষয়কার্য (উচ্চগতি)",
            rightTitle = "নদীর সঞ্চয়কার্য (নিম্নগতি)",
            rows = listOf(
                ConfusionComparisonRow("ভূমিরূপ", "I ও V আকৃতির উপত্যকা, গিরিখাত, ক্যানিয়ন, জলপ্রপাত, মন্থকূপ", "প্লাবনভূমি, স্বাভাবিক বাঁধ, অশ্বক্ষুরাকৃতি হ্রদ, বদ্বীপ"),
                ConfusionComparisonRow("কারণ", "নদীর প্রবল ঢাল ও তীব্র স্রোতবেগ", "মৃদু ঢাল, বোঝার আধিক্য ও বেগ হ্রাস")
            ),
            memoryTrickBn = "ট্রিক: পাহাড়ে কাটাকুটি (V-উপত্যকা, জলপ্রপাত = ক্ষয়), সমতলে জমাজমি (প্লাবনভূমি, বদ্বীপ = সঞ্চয়)!"
        ),
        ConfusionPairItem(
            id = "conf_geo_2",
            subject = WbbseSubject.GEOGRAPHY,
            leftTitle = "ভাঙর ও খাদার (উত্তর ভারত)",
            rightTitle = "পশ্চিমঘাট ও পূর্বঘাট পর্বত",
            rows = listOf(
                ConfusionComparisonRow("বৈশিষ্ট্য ১", "ভাঙর = প্রাচীন পলি (কম উর্বর), খাদার = নবীন পলি (অতি উর্বর)", "পশ্চিমঘাট = অবিচ্ছিন্ন ও উঁচু (সহ্যাদ্রি), পূর্বঘাট = বিচ্ছিন্ন ও নিচু"),
                ConfusionComparisonRow("সর্বোচ্চ শৃঙ্গ/গুরুত্ব", "খাদার প্লাবনভূমিতে প্রতি বছর নতুন পলি জমে", "পশ্চিমঘাট: আনাইমুদি (দক্ষিণ ভারত), পূর্বঘাট: জিন্দাগাড়া/মহেন্দ্রগিরি")
            ),
            memoryTrickBn = "ট্রিক: ‘ভাঙা’ জিনিস পুরনো = ভাঙর (প্রাচীন পলি); ‘খাদ্য’ জন্মায় ভালো = খাদার (নবীন উর্বর পলি)!"
        ),

        // Bengali
        ConfusionPairItem(
            id = "conf_ben_1",
            subject = WbbseSubject.BENGALI,
            leftTitle = "কর্মধারয় সমাস",
            rightTitle = "তৎপুরুষ সমাস",
            rows = listOf(
                ConfusionComparisonRow("মূল পরিচয়", "বিশেষণ ও বিশেষ্যের সমাস; বিভক্তি লোপ পায় না", "পূর্বপদের কারক-বিভক্তি (কে, দ্বারা, জন্য, থেকে, এর) লোপ পায়"),
                ConfusionComparisonRow("পরপদের অর্থ", "পরপদের অর্থ প্রাধান্য পায়", "পরপদের অর্থ প্রাধান্য পায়"),
                ConfusionComparisonRow("উদাহরণ", "নীল যে পদ্ম = নীলপদ্ম; সিংহ চিহ্নিত আসন = সিংহাসন", "বিপদকে আপন্ন = বিপদাপন্ন; গাছ থেকে পাকা = গাছপাকা")
            ),
            memoryTrickBn = "ট্রিক: ব্যাসবাক্যে ‘যিনি/যে/রূপ/ন্যায়’ থাকলেই কর্মধারয়; আর ‘কে/দ্বারা/জন্য/থেকে/র/এ’ বিভক্তি উড়ে গেলেই তৎপুরুষ!"
        ),
        ConfusionPairItem(
            id = "conf_ben_2",
            subject = WbbseSubject.BENGALI,
            leftTitle = "করণ কারক (‘দ্বারা’)",
            rightTitle = "অপাদান কারক (‘হইতে/থেকে’)",
            rows = listOf(
                ConfusionComparisonRow("প্রশ্ন করার সূত্র", "ক্রিয়াকে ‘কীসের দ্বারা / কী উপায়ে’ প্রশ্ন করো", "ক্রিয়াকে ‘কোথা থেকে / কী থেকে (বিচ্যুত, ভয়, উৎপন্ন)’ প্রশ্ন করো"),
                ConfusionComparisonRow("উদাহরণ", "কলমে (কলমের দ্বারা) লিখি; চোখে দেখি", "গাছ থেকে পাতা পড়ে; বাঘে (বাঘ থেকে) ভয় পায়")
            ),
            memoryTrickBn = "ট্রিক: ‘যন্ত্র বা উপায়’ = করণ (কলমে), ‘আলাদা হওয়া / ভয় / শুরু’ = অপাদান (গাছ থেকে)!"
        ),

        // English
        ConfusionPairItem(
            id = "conf_eng_1",
            subject = WbbseSubject.ENGLISH,
            leftTitle = "Simple vs Complex vs Compound",
            rightTitle = "Connectors & Clauses",
            rows = listOf(
                ConfusionComparisonRow("Simple Sentence", "১টি মাত্র Finite Verb থাকে (In spite of / Being / Too...to)", "কোনো clause connector থাকে না"),
                ConfusionComparisonRow("Complex Sentence", "১টি Main + ১টি Subordinate Clause", "who, which, that, because, though, if, unless, so...that"),
                ConfusionComparisonRow("Compound Sentence", "২টি স্বাধীন Main Clause যুক্ত", "and, but, or, so, yet, therefore")
            ),
            memoryTrickBn = "ট্রিক: FANBOYS (For, And, Nor, But, Or, Yet, So) থাকলেই Compound; WH-word/if/though/because = Complex!"
        ),
        ConfusionPairItem(
            id = "conf_eng_2",
            subject = WbbseSubject.ENGLISH,
            leftTitle = "Active Voice",
            rightTitle = "Passive Voice",
            rows = listOf(
                ConfusionComparisonRow("কাঠামো", "Subject নিজে কাজ করে (Sub + Verb + Obj)", "Object সামনে আসে + Be-verb + V3 + by + Sub"),
                ConfusionComparisonRow("উদাহরণ", "He wrote a letter.", "A letter was written by him.")
            ),
            memoryTrickBn = "ট্রিক: Passive মানেই সর্বদা Be-verb (am/is/are/was/were/been/being) + V3 (Past Participle) বাধ্যতামূলক!"
        ),

        // Mathematics
        ConfusionPairItem(
            id = "conf_math_1",
            subject = WbbseSubject.MATHEMATICS,
            leftTitle = "সরল সুদ (Simple Interest)",
            rightTitle = "চক্রবৃদ্ধি সুদ (Compound Interest)",
            rows = listOf(
                ConfusionComparisonRow("আসল (Principal)", "প্রতি বছর একই স্থির আসলের ওপর সুদ গণনা হয়", "প্রতি পর্বের শেষে সুদ আসলের সাথে যোগ হয়ে নতুন আসল হয়"),
                ConfusionComparisonRow("সূত্র", "I = Ptr / 100", "A = P(1 + r/100)ⁿ,  CI = A - P"),
                ConfusionComparisonRow("১ বছরের ক্ষেত্রে", "১ বছরের সরল সুদ = ১ বছরের বার্ষিক চক্রবৃদ্ধি সুদ", "২য় বছর থেকে CI > SI (২ বছরের পার্থক্য = P(r/100)²)")
            ),
            memoryTrickBn = "ট্রিক: ২ বছরের CI ও SI-এর পার্থক্য শর্টকাট সূত্র = P × (r/100)² — MCQ-তে ৫ সেকেন্ডে উত্তর বের হবে!"
        ),
        ConfusionPairItem(
            id = "conf_math_2",
            subject = WbbseSubject.MATHEMATICS,
            leftTitle = "পরিবৃত্ত (Circumcircle)",
            rightTitle = "অন্তর্বৃত্ত (Incircle)",
            rows = listOf(
                ConfusionComparisonRow("কেন্দ্র নির্ণয়", "ত্রিভুজের দুটি বাহুর লম্ব সমদ্বিখণ্ডকের ছেদবিন্দু (পরিকেন্দ্র)", "ত্রিভুজের দুটি কোণের সমদ্বিখণ্ডকের ছেদবিন্দু (অন্তঃকেন্দ্র)"),
                ConfusionComparisonRow("বৃত্তের অবস্থান", "ত্রিভুজের তিনটি শীর্ষবিন্দু দিয়ে যায় (বাইরে)", "ত্রিভুজের তিনটি বাহুকে স্পর্শ করে (ভেতরে)")
            ),
            memoryTrickBn = "ট্রিক: ‘পরি’ = বাহুর লম্ব সমদ্বিখণ্ডক (বাইরের বৃত্ত); ‘অন্তর’ = কোণের সমদ্বিখণ্ডক (ভেতরের বৃত্ত)!"
        )
    )

    fun getPocketBookByCategory(category: PocketBookCategory): List<PocketBookEntry> =
        pocketBookEntries.filter { it.category == category }

    fun getConfusionsForSubject(subject: WbbseSubject): List<ConfusionPairItem> =
        confusionPairs.filter { it.subject == subject }
}
