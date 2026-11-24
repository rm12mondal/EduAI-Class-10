package com.example.data.map

import com.example.model.MapBoardDomain
import com.example.model.MapPointingLocation
import com.example.model.MapSymbolType

/**
 * 44 Authentic WBBSE Class 10 (Madhyamik) Map Pointing Locations:
 * - Geography (ভূগোল — ১০ নম্বর): 26 Board-Verified Locations
 * - History (ইতিহাস — ৪ নম্বর): 18 Board-Verified Locations
 *
 * Normalized coordinates (normX, normY in 0f..1f) are calibrated to the custom India outline
 * rendered in InteractiveMapBoardScreen (lat ~ 8°N..37°N mapped to y ~ 0.94..0.06,
 * lon ~ 68°E..97°E mapped to x ~ 0.08..0.94).
 */
object MapPointingBank {

    val allLocations: List<MapPointingLocation> = geographyLocations() + historyLocations()

    fun getByDomain(domain: MapBoardDomain): List<MapPointingLocation> =
        allLocations.filter { it.domain == domain }

    private fun geographyLocations(): List<MapPointingLocation> = listOf(
        MapPointingLocation(
            id = "geo_karakoram", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "কারাকোরাম পর্বত ও গডউইন অস্টিন (K2)", nameEn = "Karakoram / K2",
            symbolType = MapSymbolType.MOUNTAIN, normX = 0.34f, normY = 0.11f,
            regionHintBn = "উত্তরতম লাদাখ-জম্মু কাশ্মীর সীমান্ত",
            boardTipBn = "বাদামি ত্রিভুজ (▲) দিয়ে শৃঙ্গ K2 এবং বাঁকানো রেখা দিয়ে কারাকোরাম শ্রেণী দেখাবে।"
        ),
        MapPointingLocation(
            id = "geo_shivalik", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "শিবালিক পর্বতশ্রেণি", nameEn = "Shivalik Range",
            symbolType = MapSymbolType.MOUNTAIN, normX = 0.42f, normY = 0.25f,
            regionHintBn = "হিমালয়ের দক্ষিণতম পাদদেশ (উত্তরাখণ্ড-হিমাচল)",
            boardTipBn = "হিমালয়ের সবচেয়ে বাইরের/নিচের ধাপ বরাবর ঢেউখেলানো রেখায় দেখাবে।"
        ),
        MapPointingLocation(
            id = "geo_aravalli", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "আরাবল্লী পর্বত", nameEn = "Aravalli Range",
            symbolType = MapSymbolType.MOUNTAIN, normX = 0.28f, normY = 0.41f,
            regionHintBn = "রাজস্থান থেকে দিল্লি অভিমুখে তির্যক প্রাচীন ভঙ্গিল পর্বত",
            boardTipBn = "দক্ষিণ-পশ্চিম থেকে উত্তর-পূর্বে রাজস্থানের মাঝ বরাবর চিহ্নিত করবে।"
        ),
        MapPointingLocation(
            id = "geo_vindhya", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "বিন্ধ্য পর্বত", nameEn = "Vindhya Range",
            symbolType = MapSymbolType.MOUNTAIN, normX = 0.38f, normY = 0.50f,
            regionHintBn = "মধ্যপ্রদেশে নর্মদা নদীর ঠিক উত্তর তীর বরাবর",
            boardTipBn = "নর্মদা নদীর ঠিক ওপরে অনুভূমিক পর্বত চিহ্ন বসাবে।"
        ),
        MapPointingLocation(
            id = "geo_satpura", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "সাতপুরা স্তূপ পর্বত", nameEn = "Satpura Range",
            symbolType = MapSymbolType.MOUNTAIN, normX = 0.37f, normY = 0.55f,
            regionHintBn = "নর্মদা ও তাপ্তি নদীর মধ্যবর্তী স্তূপ পর্বত",
            boardTipBn = "নর্মদার নিচে এবং তাপ্তির ওপরে সাতপুরা দেখাবে।"
        ),
        MapPointingLocation(
            id = "geo_nilgiri", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "নীলগিরি পর্বত (দোদাবেতা)", nameEn = "Nilgiri Hills",
            symbolType = MapSymbolType.MOUNTAIN, normX = 0.36f, normY = 0.83f,
            regionHintBn = "পশ্চিমঘাট ও পূর্বঘাট পর্বতের মিলনস্থল (তামিলনাড়ু-কেরল)",
            boardTipBn = "দক্ষিণ ভারতে পশ্চিমঘাট ও পূর্বঘাটের সংযোগস্থলে ত্রিভুজ চিহ্ন দেবে।"
        ),
        MapPointingLocation(
            id = "geo_chotanagpur", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "ছোটনাগপুর মালভূমি", nameEn = "Chota Nagpur Plateau",
            symbolType = MapSymbolType.REGION, normX = 0.63f, normY = 0.50f,
            regionHintBn = "ঝাড়খণ্ড ও পশ্চিমবঙ্গের পশ্চিমাংশ (খনিজ ভাণ্ডার)",
            boardTipBn = "শেডিং করে এবং সূচক বক্সে (Index) মালভূমি দেখিয়ে নাম লিখবে।"
        ),
        MapPointingLocation(
            id = "geo_meghalaya", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "মেঘালয় মালভূমি (গারো-খাসি-জয়ন্তিয়া)", nameEn = "Meghalaya Plateau",
            symbolType = MapSymbolType.REGION, normX = 0.81f, normY = 0.41f,
            regionHintBn = "অসমের দক্ষিণে উত্তর-পূর্ব ভারতের মেঘালয় রাজ্য",
            boardTipBn = "বাংলাদেশের উত্তর সীমান্তের ঠিক ওপরে চিহ্নিত করবে।"
        ),
        MapPointingLocation(
            id = "geo_ganga", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "গঙ্গা নদী", nameEn = "Ganga River",
            symbolType = MapSymbolType.RIVER, normX = 0.55f, normY = 0.39f,
            regionHintBn = "উত্তরাখণ্ড → উত্তরপ্রদেশ → বিহার → পশ্চিমবঙ্গ হয়ে বঙ্গোপসাগর",
            boardTipBn = "নীল প্রবাহরেখা এঁকে প্রবাহের দিক তীরচিহ্ন দিয়ে দেখাবে।"
        ),
        MapPointingLocation(
            id = "geo_brahmaputra", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "ব্রহ্মপুত্র নদী", nameEn = "Brahmaputra River",
            symbolType = MapSymbolType.RIVER, normX = 0.85f, normY = 0.36f,
            regionHintBn = "অরুণাচল ও অসম উপত্যকা দিয়ে প্রবাহিত নদ",
            boardTipBn = "উত্তর-পূর্ব ভারতে পূর্ব থেকে পশ্চিমে প্রবাহিত হয়ে দক্ষিণে বাঁক নেওয়া রেখা দেখাবে।"
        ),
        MapPointingLocation(
            id = "geo_narmada", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "নর্মদা নদী (পশ্চিমবাহিনী)", nameEn = "Narmada River",
            symbolType = MapSymbolType.RIVER, normX = 0.35f, normY = 0.53f,
            regionHintBn = "অমরকণ্টক থেকে খাম্বাত উপসাগর (আরব সাগর)",
            boardTipBn = "পশ্চিমদিকে তীরচিহ্ন দিয়ে আরব সাগরের খাম্বাত উপসাগরে মেলাবে।"
        ),
        MapPointingLocation(
            id = "geo_mahanadi", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "মহানদী", nameEn = "Mahanadi River",
            symbolType = MapSymbolType.RIVER, normX = 0.60f, normY = 0.59f,
            regionHintBn = "ছত্তিশগড় ও ওড়িশা হয়ে বঙ্গোপসাগর",
            boardTipBn = "চিল্কা হ্রদের সামান্য উত্তরে বঙ্গোপসাগরে পতিত রেখা আঁকবে।"
        ),
        MapPointingLocation(
            id = "geo_godavari", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "গোদাবরী নদী (দক্ষিণ ভারতের গঙ্গা)", nameEn = "Godavari River",
            symbolType = MapSymbolType.RIVER, normX = 0.48f, normY = 0.66f,
            regionHintBn = "মহারাষ্ট্রের নাসিক থেকে অন্ধ্রপ্রদেশ হয়ে বঙ্গোপসাগর",
            boardTipBn = "দক্ষিণ ভারতের দীর্ঘতম নদী—পূর্বদিকে তীরচিহ্ন দেবে।"
        ),
        MapPointingLocation(
            id = "geo_kaveri", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "কাবেরী নদী", nameEn = "Kaveri River",
            symbolType = MapSymbolType.RIVER, normX = 0.43f, normY = 0.82f,
            regionHintBn = "কর্ণাটক ও তামিলনাড়ু হয়ে বঙ্গোপসাগর",
            boardTipBn = "দক্ষিণ-পূর্ব উপকূলের তামিলনাড়ু বদ্বীপে প্রবাহরেখা দেখাবে।"
        ),
        MapPointingLocation(
            id = "geo_chilika", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "চিল্কা হ্রদ (বৃহত্তম উপহ্রদ)", nameEn = "Chilika Lake",
            symbolType = MapSymbolType.REGION, normX = 0.63f, normY = 0.62f,
            regionHintBn = "ওড়িশা উপকূলে বঙ্গোপসাগরের তীরে",
            boardTipBn = "ওড়িশার উপকূলীয় খাঁজে নীল রং করে ‘চিল্কা হ্রদ’ লিখবে।"
        ),
        MapPointingLocation(
            id = "geo_loktak", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "লোকটাক হ্রদ (উত্তর-পূর্ব ভারত)", nameEn = "Loktak Lake",
            symbolType = MapSymbolType.REGION, normX = 0.88f, normY = 0.45f,
            regionHintBn = "মণিপুর রাজ্যের মিষ্টি জলের হ্রদ",
            boardTipBn = "উত্তর-পূর্বের মণিপুর অংশে ছোট বৃত্তাকার নীল চিহ্ন দেবে।"
        ),
        MapPointingLocation(
            id = "geo_sundarban", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "সুন্দরবন ম্যানগ্রোভ অরণ্য", nameEn = "Sundarbans Mangrove",
            symbolType = MapSymbolType.REGION, normX = 0.72f, normY = 0.53f,
            regionHintBn = "পশ্চিমবঙ্গের দক্ষিণ প্রান্তে গঙ্গা-ব্রহ্মপুত্র বদ্বীপ",
            boardTipBn = "সবুজ শেডিং দিয়ে সূচক বক্সে ‘ম্যানগ্রোভ অরণ্য (সুন্দরবন)’ লিখবে।"
        ),
        MapPointingLocation(
            id = "geo_thar", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "থর মরুভূমি (মরু মৃত্তিকা অঞ্চল)", nameEn = "Thar Desert",
            symbolType = MapSymbolType.REGION, normX = 0.21f, normY = 0.39f,
            regionHintBn = "রাজস্থানের পশ্চিম অংশে আরাবল্লীর পশ্চিমে",
            boardTipBn = "হলুদ/বিন্দু শেডিং দিয়ে মরু অঞ্চল চিহ্নিত করবে।"
        ),
        MapPointingLocation(
            id = "geo_mawsynram", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "মৌসিনরাম (সর্বাধিক বৃষ্টিপাত কেন্দ্র)", nameEn = "Mawsynram",
            symbolType = MapSymbolType.CITY_CENTER, normX = 0.82f, normY = 0.42f,
            regionHintBn = "মেঘালয়ের খাসি পাহাড়ের দক্ষিণ ঢাল",
            boardTipBn = "ভারতের সর্বাধিক বৃষ্টিপাত যুক্ত স্থান এলে বিন্দু দিয়ে মৌসিনরাম দেখাবে।"
        ),
        MapPointingLocation(
            id = "geo_coromandel", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "করমণ্ডল উপকূল (বছরে দুবার বৃষ্টি)", nameEn = "Coromandel Coast",
            symbolType = MapSymbolType.REGION, normX = 0.48f, normY = 0.82f,
            regionHintBn = "তামিলনাড়ুর পূর্ব উপকূল বরাবর",
            boardTipBn = "‘বছরে দুবার বৃষ্টিপাত অঞ্চল’ এলে তামিলনাড়ু উপকূল শেডিং করবে।"
        ),
        MapPointingLocation(
            id = "geo_malabar", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "মালাবার উপকূল (কয়াল অঞ্চল)", nameEn = "Malabar Coast",
            symbolType = MapSymbolType.REGION, normX = 0.34f, normY = 0.85f,
            regionHintBn = "কেরল রাজ্যের দক্ষিণ-পশ্চিম উপকূল",
            boardTipBn = "দক্ষিণ-পশ্চিম উপকূল রেখা বরাবর মালাবার উপকূল দেখাবে।"
        ),
        MapPointingLocation(
            id = "geo_durgapur", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "দুর্গাপুর (ভারতের রূঢ়)", nameEn = "Durgapur",
            symbolType = MapSymbolType.CITY_CENTER, normX = 0.69f, normY = 0.49f,
            regionHintBn = "পশ্চিমবঙ্গের পশ্চিম বর্ধমান জেলা",
            boardTipBn = "‘ভারতের রূঢ়’ বা পূর্ব ভারতের লৌহ-ইস্পাত কেন্দ্র এলে বিন্দু (●) দিয়ে দেখাবে।"
        ),
        MapPointingLocation(
            id = "geo_jamshedpur", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "জামশেদপুর (টাটা লৌহ-ইস্পাত)", nameEn = "Jamshedpur",
            symbolType = MapSymbolType.CITY_CENTER, normX = 0.65f, normY = 0.52f,
            regionHintBn = "ঝাড়খণ্ডে সুবর্ণরেখা ও খরকাই নদীর সঙ্গমে",
            boardTipBn = "ভারতের প্রথম বেসরকারি লৌহ-ইস্পাত কেন্দ্র (TISCO)।"
        ),
        MapPointingLocation(
            id = "geo_haldia", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "হলদিয়া (পেট্রোরসায়ন ও বন্দর)", nameEn = "Haldia",
            symbolType = MapSymbolType.CITY_CENTER, normX = 0.71f, normY = 0.54f,
            regionHintBn = "পশ্চিমবঙ্গের পূর্ব মেদিনীপুরে হলদি ও হুগলি নদীর সঙ্গমে",
            boardTipBn = "‘পূর্ব ভারতের পেট্রোরসায়ন শিল্পকেন্দ্র’ এলে হলদিয়া চিহ্নিত করবে।"
        ),
        MapPointingLocation(
            id = "geo_bengaluru", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "বেঙ্গালুরু (ভারতের সিলিকন ভ্যালি)", nameEn = "Bengaluru",
            symbolType = MapSymbolType.CITY_CENTER, normX = 0.39f, normY = 0.78f,
            regionHintBn = "দক্ষিণ কর্ণাটক — ভারতের প্রধান তথ্যপ্রযুক্তি কেন্দ্র",
            boardTipBn = "‘ভারতের সিলিকন ভ্যালি’ বা IT হাব এলে বিন্দু (●) দেবে।"
        ),
        MapPointingLocation(
            id = "geo_kandla", domain = MapBoardDomain.GEOGRAPHY,
            nameBn = "কান্দালা (শুল্কমুক্ত বন্দর)", nameEn = "Kandla Port",
            symbolType = MapSymbolType.CITY_CENTER, normX = 0.14f, normY = 0.50f,
            regionHintBn = "গুজরাটের কচ্ছ উপসাগরের উত্তর তীরে",
            boardTipBn = "‘ভারতের একটি শুল্কমুক্ত বন্দর’ প্রশ্নে কচ্ছ উপসাগরে কান্দালা দেখাবে।"
        )
    )

    private fun historyLocations(): List<MapPointingLocation> = listOf(
        MapPointingLocation(
            id = "his_barrackpore", domain = MapBoardDomain.HISTORY,
            nameBn = "ব্যারাকপুর (মঙ্গল পাণ্ডে — ১৮৫৭)", nameEn = "Barrackpore",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.72f, normY = 0.51f,
            regionHintBn = "পশ্চিমবঙ্গের উত্তর ২৪ পরগনা (কলকাতার ঠিক উত্তরে)",
            boardTipBn = "১৮৫৭ মহাবিদ্রোহের প্রথম স্ফুলিঙ্গ ব্যারাকপুরে বিন্দু/তারা (★) চিহ্ন দেবে।"
        ),
        MapPointingLocation(
            id = "his_meerut", domain = MapBoardDomain.HISTORY,
            nameBn = "মিরাট (সিপাহী বিদ্রোহের সূচনা)", nameEn = "Meerut",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.39f, normY = 0.32f,
            regionHintBn = "দিল্লির সামান্য উত্তর-পূর্বে উত্তরপ্রদেশে",
            boardTipBn = "১০ মে ১৮৫৭ মহাবিদ্রোহের আনুষ্ঠানিক সূচনাস্থল—দিল্লির ঠিক পাশে দেখাবে।"
        ),
        MapPointingLocation(
            id = "his_delhi", domain = MapBoardDomain.HISTORY,
            nameBn = "দিল্লি (বাহাদুর শাহ জাফর)", nameEn = "Delhi",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.37f, normY = 0.33f,
            regionHintBn = "যমুনা নদীর তীরে ভারতের রাজধানী",
            boardTipBn = "১৮৫৭ মহাবিদ্রোহের প্রধান কেন্দ্র দিল্লি।"
        ),
        MapPointingLocation(
            id = "his_kanpur", domain = MapBoardDomain.HISTORY,
            nameBn = "কানপুর (নানাসাহেব ও তাঁতিয়া তোপি)", nameEn = "Kanpur",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.47f, normY = 0.39f,
            regionHintBn = "মধ্য উত্তরপ্রদেশে গঙ্গার দক্ষিণ তীরে",
            boardTipBn = "লখনউয়ের ঠিক দক্ষিণ-পশ্চিমে কানপুর চিহ্নিত করবে।"
        ),
        MapPointingLocation(
            id = "his_lucknow", domain = MapBoardDomain.HISTORY,
            nameBn = "লখনউ (বেগম হজরত মহল)", nameEn = "Lucknow",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.49f, normY = 0.38f,
            regionHintBn = "অযোধ্যা / উত্তরপ্রদেশের কেন্দ্রস্থল",
            boardTipBn = "১৮৫৭-এ বেগম হজরত মহলের নেতৃত্বাধীন কেন্দ্র।"
        ),
        MapPointingLocation(
            id = "his_jhansi", domain = MapBoardDomain.HISTORY,
            nameBn = "ঝাঁসি (রানি লক্ষ্মীবাঈ)", nameEn = "Jhansi",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.42f, normY = 0.43f,
            regionHintBn = "বুন্দেলখণ্ড (উত্তরপ্রদেশ-মধ্যপ্রদেশ সীমান্ত)",
            boardTipBn = "১৮৫৭ মহাবিদ্রোহে রানি লক্ষ্মীবাঈয়ের বীরত্বের কেন্দ্র।"
        ),
        MapPointingLocation(
            id = "his_santal", domain = MapBoardDomain.HISTORY,
            nameBn = "সাঁওতাল বিদ্রোহ এলাকা (সাঁওতাল পরগনা)", nameEn = "Santal Pargana",
            symbolType = MapSymbolType.REGION, normX = 0.67f, normY = 0.46f,
            regionHintBn = "রাজমহল পাহাড় ও ঝাড়খণ্ড-বীরভূম সীমান্ত (দামিন-ই-কোহ)",
            boardTipBn = "১৮৫৫ হুল বিদ্রোহের এলাকা—ছোট বৃত্ত বা শেডিং করে দেখাবে।"
        ),
        MapPointingLocation(
            id = "his_munda", domain = MapBoardDomain.HISTORY,
            nameBn = "মুন্ডা বিদ্রোহ এলাকা (রাঁচি / ছোটনাগপুর)", nameEn = "Munda Revolt (Ranchi)",
            symbolType = MapSymbolType.REGION, normX = 0.62f, normY = 0.51f,
            regionHintBn = "ঝাড়খণ্ডের রাঁচি-ছোটনাগপুর মালভূমি",
            boardTipBn = "১৮৯৯-১৯০০ বিরসা মুন্ডার উলগুলান এলাকা।"
        ),
        MapPointingLocation(
            id = "his_chuar", domain = MapBoardDomain.HISTORY,
            nameBn = "চুয়াড় বিদ্রোহ এলাকা (মেদিনীপুর-বাঁকুড়া)", nameEn = "Chuar Revolt",
            symbolType = MapSymbolType.REGION, normX = 0.68f, normY = 0.53f,
            regionHintBn = "পশ্চিমবঙ্গের জঙ্গলমহল (মেদিনীপুর, বাঁকুড়া, পুরুলিয়া)",
            boardTipBn = "মেদিনীপুর অঞ্চলে শেডিং দিয়ে ‘চুয়াড় বিদ্রোহ এলাকা’ লিখবে।"
        ),
        MapPointingLocation(
            id = "his_barasat", domain = MapBoardDomain.HISTORY,
            nameBn = "বারাসত বিদ্রোহ (তিতুমীরের বাঁশের কেল্লা)", nameEn = "Barasat Revolt",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.73f, normY = 0.50f,
            regionHintBn = "কলকাতার উত্তর-পূর্বে উত্তর ২৪ পরগনা (নারকেলবেড়িয়া)",
            boardTipBn = "১৮৩১ ওয়াহাবি / বারাসত বিদ্রোহের কেন্দ্র।"
        ),
        MapPointingLocation(
            id = "his_indigo", domain = MapBoardDomain.HISTORY,
            nameBn = "নীল বিদ্রোহের কেন্দ্র (নদীয়া / যশোর)", nameEn = "Indigo Revolt (Nadia)",
            symbolType = MapSymbolType.REGION, normX = 0.73f, normY = 0.49f,
            regionHintBn = "পশ্চিমবঙ্গের নদীয়া ও সংলগ্ন যশোর অঞ্চল (চৌগাছা)",
            boardTipBn = "১৮৫৯-৬০ দিগম্বর ও বিষ্ণুচরণ বিশ্বাসের নেতৃত্বে নীল বিদ্রোহ কেন্দ্র।"
        ),
        MapPointingLocation(
            id = "his_champaran", domain = MapBoardDomain.HISTORY,
            nameBn = "চম্পারণ (গান্ধীজির প্রথম সত্যাগ্রহ)", nameEn = "Champaran",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.61f, normY = 0.38f,
            regionHintBn = "উত্তর-পশ্চিম বিহার (নেপাল সীমান্ত সংলগ্ন)",
            boardTipBn = "১৯১৭ তিনকাঠিয়া প্রথার বিরুদ্ধে নীলচাষিদের সত্যাগ্রহ কেন্দ্র।"
        ),
        MapPointingLocation(
            id = "his_bardoli", domain = MapBoardDomain.HISTORY,
            nameBn = "বারদৌলি (সর্দার প্যাটেলের সত্যাগ্রহ)", nameEn = "Bardoli",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.24f, normY = 0.56f,
            regionHintBn = "দক্ষিণ গুজরাট (সুরাট জেলা)",
            boardTipBn = "১৯২৮ খাজনা বৃদ্ধির বিরুদ্ধে বারদৌলি সত্যাগ্রহ—গুজরাটের দক্ষিণ উপকূলের কাছে।"
        ),
        MapPointingLocation(
            id = "his_dandi", domain = MapBoardDomain.HISTORY,
            nameBn = "ডান্ডি (আইন অমান্য ও লবণ সত্যাগ্রহ)", nameEn = "Dandi",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.23f, normY = 0.58f,
            regionHintBn = "গুজরাটের নভসারি উপকূলে আরব সাগরের তীরে",
            boardTipBn = "৬ এপ্রিল ১৯৩০ গান্ধীজি এখানে লবণ আইন ভঙ্গ করেন।"
        ),
        MapPointingLocation(
            id = "his_chauri_chaura", domain = MapBoardDomain.HISTORY,
            nameBn = "চৌরিচৌরা (উত্তরপ্রদেশ — ১৯২২)", nameEn = "Chauri Chaura",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.57f, normY = 0.39f,
            regionHintBn = "পূর্ব উত্তরপ্রদেশের গোরখপুর জেলা",
            boardTipBn = "১৯২২ সালের এই ঘটনার পর গান্ধীজি অসহযোগ আন্দোলন প্রত্যাহার করেন।"
        ),
        MapPointingLocation(
            id = "his_chittagong", domain = MapBoardDomain.HISTORY,
            nameBn = "চট্টগ্রাম (মাস্টারদা সূর্য সেনের অস্ত্রাগার লুণ্ঠন)", nameEn = "Chittagong",
            symbolType = MapSymbolType.REVOLT_STAR, normX = 0.84f, normY = 0.53f,
            regionHintBn = "পূর্ববঙ্গ / বাংলাদেশের দক্ষিণ-পূর্ব উপকূল",
            boardTipBn = "১৮ এপ্রিল ১৯৩০ সশস্ত্র বিপ্লবী আন্দোলনের ঐতিহাসিক কেন্দ্র।"
        ),
        MapPointingLocation(
            id = "his_junagadh", domain = MapBoardDomain.HISTORY,
            nameBn = "দেশীয় রাজ্য জুনাগড় (১৯৪৮)", nameEn = "Junagadh",
            symbolType = MapSymbolType.CITY_CENTER, normX = 0.15f, normY = 0.56f,
            regionHintBn = "গুজরাটের সৌরাষ্ট্র উপদ্বীপের দক্ষিণ-পশ্চিম প্রান্ত",
            boardTipBn = "অধ্যায় ৮ থেকে প্রায় প্রতি বছর জুনাগড় বা হায়দ্রাবাদ ম্যাপে আসে!"
        ),
        MapPointingLocation(
            id = "his_hyderabad", domain = MapBoardDomain.HISTORY,
            nameBn = "দেশীয় রাজ্য হায়দ্রাবাদ (অপারেশন পোলো)", nameEn = "Hyderabad",
            symbolType = MapSymbolType.CITY_CENTER, normX = 0.44f, normY = 0.68f,
            regionHintBn = "দাক্ষিণাত্যের কেন্দ্রস্থল (বর্তমান তেলেঙ্গানা)",
            boardTipBn = "১৯৪৮ সালে পুলিশি অভিযানে ভারতভুক্ত বৃহত্তম দেশীয় রাজ্য।"
        )
    )
}
