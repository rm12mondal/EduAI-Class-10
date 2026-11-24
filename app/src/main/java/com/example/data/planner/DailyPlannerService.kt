package com.example.data.planner

import android.util.Log
import com.example.data.gguf.GgufModelManager
import com.example.data.local.DailyPlanTaskEntity
import com.example.data.local.MasterDataEntity
import com.example.data.local.TopicImportance
import com.example.model.GgufModelStatus
import com.example.model.InferenceMode
import com.example.model.PlanActionType
import com.example.model.WbbseSubject
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar

private const val PLANNER_MODEL_NAME = "gemini-3.8-flash"

/**
 * Hybrid Online (Firebase AI Logic `gemini-3.8-flash`) + Offline (WBBSE Syllabus Rotational Engine)
 * Daily Study Planner generator for Madhyamik Class 10 students (2, 4, or 6 study hours/day).
 */
class DailyPlannerService(
    private val ggufModelManager: GgufModelManager
) {
    private val planSchema = Schema.obj(
        properties = mapOf(
            "tasks" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "subjectId" to Schema.string("One of: bengali, english, mathematics, physical_science, life_science, history, geography"),
                        "titleBn" to Schema.string("Short actionable Bengali task title"),
                        "detailBn" to Schema.string("Specific chapter/topic instructions in Bengali"),
                        "durationMinutes" to Schema.integer("Minutes allocated to this task"),
                        "actionType" to Schema.string("One of: SMART_LEARNING, SUBJECT_NOTE, TOPPER_CAPSULE, RAPID_FIRE, MAP_BOARD")
                    )
                )
            )
        )
    )

    private val plannerModel by lazy {
        Firebase.ai.generativeModel(
            modelName = PLANNER_MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = planSchema
                temperature = 0.6f
            }
        )
    }

    suspend fun generateDailyPlan(
        dateStamp: String,
        targetHours: Int,
        inferenceMode: InferenceMode,
        allMasterNotes: List<MasterDataEntity>
    ): Pair<List<DailyPlanTaskEntity>, String> = withContext(Dispatchers.IO) {
        if (inferenceMode == InferenceMode.ONLINE) {
            val onlineTasks = runCatching {
                generateOnlinePlan(dateStamp, targetHours, allMasterNotes)
            }.onFailure { err ->
                Log.w("DailyPlannerService", "Online planner failed, using offline rotational plan", err)
            }.getOrNull()

            if (!onlineTasks.isNullOrEmpty()) {
                return@withContext onlineTasks to "🌐 Gemini AI স্মার্ট রুটিন"
            }
        }

        val offlineTasks = generateOfflineRotationalPlan(dateStamp, targetHours, allMasterNotes)
        val badge = if (ggufModelManager.state.value.status == GgufModelStatus.READY_LOADED) {
            "⚡ অফলাইন Llama + WBBSE স্মার্ট রুটিন"
        } else {
            "⚡ অফলাইন WBBSE স্মার্ট রুটিন"
        }
        offlineTasks to badge
    }

    private suspend fun generateOnlinePlan(
        dateStamp: String,
        targetHours: Int,
        allMasterNotes: List<MasterDataEntity>
    ): List<DailyPlanTaskEntity> {
        val taskCount = when (targetHours) {
            2 -> 4
            6 -> 6
            else -> 5
        }
        val sampleVviChapters = allMasterNotes
            .filter { it.importanceLevel == TopicImportance.VVI }
            .shuffled()
            .take(12)
            .joinToString("; ") { "${it.subjectId}: ${it.chapterName}" }

        val prompt = """
            Create a balanced West Bengal Board (WBBSE Class 10 / Madhyamik) Daily Study Routine for date $dateStamp.
            Total available study time: $targetHours hours (${targetHours * 60} minutes).
            Create exactly $taskCount tasks covering 3 to 4 DIFFERENT subjects (mix Science/Math with Languages/History/Geography).
            Include:
            - At least 1 SMART_LEARNING concept task
            - At least 1 TOPPER_CAPSULE or RAPID_FIRE speed practice task
            - If History or Geography is included, optionally include 1 MAP_BOARD task
            Reference WBBSE VVI chapters: $sampleVviChapters
            Write natural, motivating Bengali titles and crisp action steps.
        """.trimIndent()

        val response = plannerModel.generateContent(content { text(prompt) })
        val rawJson = response.text?.trim().orEmpty()
        if (rawJson.isBlank()) return emptyList()

        val root = JSONObject(rawJson)
        val arr = root.optJSONArray("tasks") ?: return emptyList()
        val validSubjects = WbbseSubject.entries.map { it.id }.toSet()
        val validActions = setOf(
            PlanActionType.SMART_LEARNING,
            PlanActionType.SUBJECT_NOTE,
            PlanActionType.TOPPER_CAPSULE,
            PlanActionType.RAPID_FIRE,
            PlanActionType.MAP_BOARD
        )

        val result = mutableListOf<DailyPlanTaskEntity>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val rawSubj = obj.optString("subjectId").lowercase()
            val subjId = if (rawSubj in validSubjects) rawSubj else WbbseSubject.PHYSICAL_SCIENCE.id
            val title = obj.optString("titleBn").trim()
            val detail = obj.optString("detailBn").trim()
            val mins = obj.optInt("durationMinutes", 30).coerceIn(15, 120)
            val rawAction = obj.optString("actionType").uppercase()
            val action = if (rawAction in validActions) rawAction else PlanActionType.SMART_LEARNING
            if (title.isNotBlank()) {
                result.add(
                    DailyPlanTaskEntity(
                        dateStamp = dateStamp,
                        targetHours = targetHours,
                        subjectId = subjId,
                        titleBn = title,
                        detailBn = detail.ifBlank { "অধ্যায়ের VVI পয়েন্ট ও সূত্র মন দিয়ে রিভিশন করো।" },
                        durationMinutes = mins,
                        actionType = action,
                        isCompleted = false,
                        orderIndex = i
                    )
                )
            }
        }
        return result
    }

    private fun generateOfflineRotationalPlan(
        dateStamp: String,
        targetHours: Int,
        allMasterNotes: List<MasterDataEntity>
    ): List<DailyPlanTaskEntity> {
        val daySeed = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val vviBySubject = allMasterNotes
            .filter { it.importanceLevel == TopicImportance.VVI }
            .groupBy { it.subjectId }

        fun pickChapter(subject: WbbseSubject, offset: Int): String {
            val list = vviBySubject[subject.id].orEmpty().map { it.chapterName }.distinct()
            if (list.isEmpty()) return subject.bengaliName
            return list[(daySeed + offset) % list.size]
        }

        // Rotate primary subjects daily so students cover all 7 subjects systematically
        val sciSubj = if (daySeed % 2 == 0) WbbseSubject.PHYSICAL_SCIENCE else WbbseSubject.LIFE_SCIENCE
        val humSubj = if (daySeed % 2 == 0) WbbseSubject.GEOGRAPHY else WbbseSubject.HISTORY
        val langSubj = if (daySeed % 2 == 0) WbbseSubject.BENGALI else WbbseSubject.ENGLISH

        val sciChapter = pickChapter(sciSubj, 0)
        val mathChapter = pickChapter(WbbseSubject.MATHEMATICS, 1)
        val langChapter = pickChapter(langSubj, 2)
        val humChapter = pickChapter(humSubj, 3)

        val baseTasks = when (targetHours) {
            2 -> listOf(
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 2, subjectId = sciSubj.id,
                    titleBn = "${sciSubj.bengaliName}: স্মার্ট লার্নিংয়ে অধ্যায় বুঝুন",
                    detailBn = "$sciChapter — সহজ বাংলায় মূল ধারণা ও সূত্র ঝালিয়ে নাও।",
                    durationMinutes = 40, actionType = PlanActionType.SMART_LEARNING, orderIndex = 0
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 2, subjectId = WbbseSubject.MATHEMATICS.id,
                    titleBn = "গণিত: ২টি VVI অঙ্ক ও উপপাদ্য প্র্যাকটিস",
                    detailBn = "$mathChapter থেকে মূল সূত্র ও ২টি বোর্ড-মানের সমস্যা লিখে অনুশীলন করো।",
                    durationMinutes = 40, actionType = PlanActionType.SUBJECT_NOTE, orderIndex = 1
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 2, subjectId = humSubj.id,
                    titleBn = "${humSubj.bengaliName}: ম্যাপ পয়েন্টিং প্র্যাকটিস (১০টি পিন)",
                    detailBn = "ইন্টারেক্টিভ ম্যাপ বোর্ডে ১০টি বোর্ড লোকেশন ট্যাপ করে নির্ভুলতা যাচাই করো।",
                    durationMinutes = 20, actionType = PlanActionType.MAP_BOARD, orderIndex = 2
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 2, subjectId = langSubj.id,
                    titleBn = "র্যাপিড ফায়ার MCQ ও ১৫-মি. টপার ক্যাপসুল",
                    detailBn = "২০টি দ্রুত MCQ দিয়ে পরীক্ষার হলের স্পিড বাড়াও।",
                    durationMinutes = 20, actionType = PlanActionType.RAPID_FIRE, orderIndex = 3
                )
            )

            6 -> listOf(
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 6, subjectId = sciSubj.id,
                    titleBn = "${sciSubj.bengaliName}: গভীর ধারণা ও ডায়াগ্রাম (স্মার্ট লার্নিং)",
                    detailBn = "$sciChapter — ২ডি ডায়াগ্রাম ও অডিও রিভিশনসহ সম্পূর্ণ আয়ত্ত করো।",
                    durationMinutes = 75, actionType = PlanActionType.SMART_LEARNING, orderIndex = 0
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 6, subjectId = WbbseSubject.MATHEMATICS.id,
                    titleBn = "গণিত: উপপাদ্য, সম্পাদ্য ও ৫টি বোর্ড অঙ্ক সমাধান",
                    detailBn = "$mathChapter — ধাপে ধাপে খাতায় লিখে প্র্যাকটিস করো।",
                    durationMinutes = 80, actionType = PlanActionType.SUBJECT_NOTE, orderIndex = 1
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 6, subjectId = langSubj.id,
                    titleBn = "${langSubj.bengaliName}: সাহিত্য/Grammar ও রাইটিং রিভিশন",
                    detailBn = "$langChapter — VVI প্রশ্নোত্তর ও কোটেশন মুখস্থ করো।",
                    durationMinutes = 65, actionType = PlanActionType.SUBJECT_NOTE, orderIndex = 2
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 6, subjectId = humSubj.id,
                    titleBn = "${humSubj.bengaliName}: অধ্যায় নোট + ম্যাপ পয়েন্টিং (১৪ নম্বর নিশ্চিত)",
                    detailBn = "$humChapter পড়ে ম্যাপ বোর্ডে বোর্ডের VVI স্থানগুলো চিহ্নিত করো।",
                    durationMinutes = 60, actionType = PlanActionType.MAP_BOARD, orderIndex = 3
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 6, subjectId = sciSubj.id,
                    titleBn = "টপার মোড: ১৫-মিনিট অফলাইন ক্যাপসুল + ঘোরানো প্রশ্ন",
                    detailBn = "বোর্ড প্যাটার্নে সময় ধরে টপার ক্যাপসুল সমাধান করো।",
                    durationMinutes = 45, actionType = PlanActionType.TOPPER_CAPSULE, orderIndex = 4
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 6, subjectId = WbbseSubject.LIFE_SCIENCE.id,
                    titleBn = "র্যাপিড ফায়ার: ৩০টি MCQ স্পিড চ্যালেঞ্জ",
                    detailBn = "৩০ সেকেন্ড/প্রশ্ন টাইমারে ৩০টি MCQ দিয়ে ভুলের খাতা আপডেট করো।",
                    durationMinutes = 35, actionType = PlanActionType.RAPID_FIRE, orderIndex = 5
                )
            )

            else -> listOf( // Default 4 hours
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 4, subjectId = sciSubj.id,
                    titleBn = "${sciSubj.bengaliName}: অধ্যায় স্মার্ট লার্নিংয়ে বুঝুন",
                    detailBn = "$sciChapter — সহজ বাংলায় ব্যাখ্যা, সূত্র ও ২-মিনিটের কুইজ শেষ করো।",
                    durationMinutes = 60, actionType = PlanActionType.SMART_LEARNING, orderIndex = 0
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 4, subjectId = WbbseSubject.MATHEMATICS.id,
                    titleBn = "গণিত: ২টি উপপাদ্য ও VVI অঙ্ক প্র্যাকটিস",
                    detailBn = "$mathChapter — সূত্র ও ধাপে ধাপে সমাধান খাতায় লেখো।",
                    durationMinutes = 60, actionType = PlanActionType.SUBJECT_NOTE, orderIndex = 1
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 4, subjectId = langSubj.id,
                    titleBn = "${langSubj.bengaliName}: VVI টপিক ও অডিও রিভিশন",
                    detailBn = "$langChapter — গুরুত্বপূর্ণ পয়েন্টগুলো পড়ে ও শুনে মুখস্থ করো।",
                    durationMinutes = 45, actionType = PlanActionType.SUBJECT_NOTE, orderIndex = 2
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 4, subjectId = humSubj.id,
                    titleBn = "${humSubj.bengaliName}: ম্যাপ পয়েন্টিং প্র্যাকটিস",
                    detailBn = "ইতিহাস/ভূগোল ম্যাপ বোর্ডে বোর্ডের নিশ্চিত লোকেশনগুলো অনুশীলন করো।",
                    durationMinutes = 35, actionType = PlanActionType.MAP_BOARD, orderIndex = 3
                ),
                DailyPlanTaskEntity(
                    dateStamp = dateStamp, targetHours = 4, subjectId = sciSubj.id,
                    titleBn = "১৫-মিনিট টপার ক্যাপসুল + ২০টি র্যাপিড ফায়ার MCQ",
                    detailBn = "পরীক্ষার হলের স্পিড ও নির্ভুলতা বাড়াতে দ্রুত প্র্যাকটিস করো।",
                    durationMinutes = 40, actionType = PlanActionType.RAPID_FIRE, orderIndex = 4
                )
            )
        }
        return baseTasks
    }
}
