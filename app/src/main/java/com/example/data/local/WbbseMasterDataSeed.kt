package com.example.data.local

import com.example.data.local.seed.BengaliEnglishSyllabusSeed
import com.example.data.local.seed.HistoryGeoSyllabusSeed
import com.example.data.local.seed.MathSyllabusSeed
import com.example.data.local.seed.ScienceSyllabusSeed

/**
 * Aggregator for 100% Complete, Authentic West Bengal Board of Secondary Education (WBBSE)
 * Class 10 (Madhyamik) Master Data across all 7 compulsory subjects:
 *  1. বাংলা — ২৪টি অধ্যায় (সাহিত্য সঞ্চয়ন ১৫ + সহায়ক পাঠ কোনি + ব্যাকরণ ও নির্মিতি ৮)
 *  2. ইংরেজি — ১৯টি অধ্যায় (Bliss ৮ + Grammar ৫ + Writing Skills ৬)
 *  3. গণিত — সম্পূর্ণ ২৬টি অধ্যায় (গণিত প্রকাশ)
 *  4. ভৌত বিজ্ঞান — সম্পূর্ণ ১৩টি অধ্যায়
 *  5. জীবন বিজ্ঞান — সম্পূর্ণ ৫টি অধ্যায় (সমস্ত উপ-অধ্যায়সহ)
 *  6. ইতিহাস — সম্পূর্ণ ৮টি অধ্যায় + ইতিহাস ম্যাপ পয়েন্টিং (৪ নম্বর)
 *  7. ভূগোল — সম্পূর্ণ ৬টি অধ্যায় + ভূগোল ম্যাপ পয়েন্টিং (১০ নম্বর)
 *
 * Used for structured offline study, priority-tagged Madhyamik revision,
 * Smart Learning chapter picker, and offline RAG grounding with the local Llama GGUF pipeline.
 */
object WbbseMasterDataSeed {

    fun createInitialRecords(): List<MasterDataEntity> = buildList {
        addAll(BengaliEnglishSyllabusSeed.createBengaliRecords())
        addAll(BengaliEnglishSyllabusSeed.createEnglishRecords())
        addAll(MathSyllabusSeed.createMathRecords())
        addAll(ScienceSyllabusSeed.createPhysicalScienceRecords())
        addAll(ScienceSyllabusSeed.createLifeScienceRecords())
        addAll(HistoryGeoSyllabusSeed.createHistoryRecords())
        addAll(HistoryGeoSyllabusSeed.createGeographyRecords())
    }
}
