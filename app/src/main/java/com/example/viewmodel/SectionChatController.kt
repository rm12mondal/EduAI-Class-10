package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.ai.AiRepository
import com.example.data.ai.AiStreamEvent
import com.example.data.local.ChatMessageEntity
import com.example.data.local.StudyRepository
import com.example.data.media.NotebookImageHelper
import com.example.model.ChatPromptOptions
import com.example.model.ChatSection
import com.example.model.FastLearningContextHelper
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.SectionChatUiState
import com.example.model.SyllabusPlanOption
import com.example.model.WbbseSubject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SECTION_CHAT_TAG = "SectionChatController"

private data class SectionChatTransientState(
    val isGenerating: Boolean = false,
    val streamingDraft: StreamingDraft? = null,
    val pendingPhoto: PendingPhotoAttachment? = null,
    val errorMessage: String? = null
)

/**
 * Manages completely separate, Room-persisted chat sessions for:
 * - ChatSection.BRAHMASTRAM
 * - ChatSection.MIND_MAP
 * - ChatSection.FAST_LEARNING
 *
 * Each section has its own independent Room history, photo attachment buffer, streaming draft,
 * and Clear Chat action so conversations across sections never mix up and resume anytime.
 */
class SectionChatController(
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val studyRepository: StudyRepository,
    private val aiRepository: AiRepository,
    private val getInferenceMode: () -> InferenceMode,
    private val getLlamaConfig: () -> LlamaInferenceConfig,
    private val getExactTextbookMode: () -> Boolean,
    private val getTargetExamYear: () -> Int,
    private val getSyllabusPlanLabel: () -> String
) {
    private val transientStates = mapOf(
        ChatSection.BRAHMASTRAM to MutableStateFlow(SectionChatTransientState()),
        ChatSection.MIND_MAP to MutableStateFlow(SectionChatTransientState()),
        ChatSection.FAST_LEARNING to MutableStateFlow(SectionChatTransientState()),
        ChatSection.STEP_BY_STEP_MEMORY to MutableStateFlow(SectionChatTransientState())
    )

    private val activeJobs = mutableMapOf<ChatSection, Job?>()

    val brahmastramChatState: StateFlow<SectionChatUiState> = buildSectionStateFlow(ChatSection.BRAHMASTRAM)
    val mindMapChatState: StateFlow<SectionChatUiState> = buildSectionStateFlow(ChatSection.MIND_MAP)
    val fastLearningChatState: StateFlow<SectionChatUiState> = buildSectionStateFlow(ChatSection.FAST_LEARNING)
    val stepByStepMemoryChatState: StateFlow<SectionChatUiState> = buildSectionStateFlow(ChatSection.STEP_BY_STEP_MEMORY)

    private fun buildSectionStateFlow(section: ChatSection): StateFlow<SectionChatUiState> {
        val transientFlow = transientStates.getValue(section)
        return combine(
            studyRepository.observeSectionMessages(section),
            transientFlow
        ) { savedMessages, transient ->
            SectionChatUiState(
                section = section,
                messages = savedMessages,
                isGenerating = transient.isGenerating,
                streamingDraft = transient.streamingDraft,
                pendingPhoto = transient.pendingPhoto,
                errorMessage = transient.errorMessage
            )
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SectionChatUiState(section = section)
        )
    }

    fun attachPhotoUri(section: ChatSection, uri: Uri) {
        val transientFlow = transientStates[section] ?: return
        scope.launch {
            val bitmap = NotebookImageHelper.loadScaledBitmap(appContext, uri)
            if (bitmap != null) {
                transientFlow.update {
                    it.copy(
                        pendingPhoto = PendingPhotoAttachment(uri = uri, previewBitmap = bitmap),
                        errorMessage = null
                    )
                }
            } else {
                transientFlow.update {
                    it.copy(errorMessage = "ছবিটি লোড করা যায়নি। পরিষ্কার একটি ছবি বেছে নাও।")
                }
            }
        }
    }

    fun clearPendingPhoto(section: ChatSection) {
        transientStates[section]?.update { it.copy(pendingPhoto = null) }
    }

    fun clearError(section: ChatSection) {
        transientStates[section]?.update { it.copy(errorMessage = null) }
    }

    fun clearSectionChat(section: ChatSection) {
        activeJobs[section]?.cancel()
        scope.launch {
            studyRepository.clearSectionChat(section)
            transientStates[section]?.update {
                SectionChatTransientState()
            }
        }
    }

    fun sendMessage(
        section: ChatSection,
        rawText: String,
        subject: WbbseSubject?,
        sectionContextHint: String = "",
        fastLearningMicroMode: Boolean = (section == ChatSection.FAST_LEARNING),
        stepByStepInteractiveMode: Boolean = (section == ChatSection.STEP_BY_STEP_MEMORY),
        memoryTrickMode: Boolean = false
    ) {
        val transientFlow = transientStates[section] ?: return
        val current = transientFlow.value
        val pendingPhoto = current.pendingPhoto
        val trimmed = rawText.trim()
        if ((trimmed.isEmpty() && pendingPhoto == null) || current.isGenerating) return

        val effectiveQuery = trimmed.ifBlank {
            when (section) {
                ChatSection.FAST_LEARNING ->
                    "এই ছবির মূল ধারণাটি মাধ্যমিক ${getTargetExamYear()} পরীক্ষার জন্য ফাস্ট মাইক্রো-সামারি আকারে বুঝিয়ে দাও।"
                ChatSection.BRAHMASTRAM ->
                    "এই ছবির প্রশ্ন/নোটটি ব্রহ্মাস্ত্রম শিওর-শট বোর্ড মান অনুযায়ী সমাধান ও মূল্যায়ন করো।"
                ChatSection.MIND_MAP ->
                    "এই ছবির মূল পয়েন্টগুলো মাইন্ড-ম্যাপের শাখা আকারে সহজভাবে সাজিয়ে বুঝিয়ে দাও।"
                ChatSection.STEP_BY_STEP_MEMORY ->
                    if (memoryTrickMode) {
                        "এই ছবির সূত্র, তারিখ, সংজ্ঞা বা নিয়মগুলো সহজে মনে রাখার জন্য ছড়া, মজার গল্প ও শর্টকাট মেমোরি ট্রিক বানিয়ে দাও।"
                    } else {
                        "এই ছবির সমস্যাটি পুরো উত্তর একসাথে না দিয়ে শুধুমাত্র ধাপ ১ (Step 1) সহজভাবে বোঝাও এবং ধাপ ২ আনলক করার জন্য আমাকে একটি A বা B প্রশ্ন করো।"
                    }
                else ->
                    "এই ছবিটি WBBSE ক্লাস ১০ অনুযায়ী সহজভাবে বুঝিয়ে দাও।"
            }
        }

        val activeMode = getInferenceMode()
        val llamaConfig = getLlamaConfig()
        val targetExamYear = getTargetExamYear()
        val planLabel = getSyllabusPlanLabel()

        activeJobs[section]?.cancel()
        activeJobs[section] = scope.launch {
            transientFlow.update {
                it.copy(
                    pendingPhoto = null,
                    isGenerating = true,
                    errorMessage = null,
                    streamingDraft = StreamingDraft(
                        text = "",
                        sourceLabel = when (section) {
                            ChatSection.FAST_LEARNING -> "🚀 ফাস্ট লার্নিং ($targetExamYear) মাইক্রো-সামারি তৈরি হচ্ছে…"
                            ChatSection.BRAHMASTRAM -> "🔥 ব্রহ্মাস্ত্রম AI ডাউট সলভার লিখছে…"
                            ChatSection.MIND_MAP -> "🌳 মাইন্ড-ম্যাপ AI বিশ্লেষক লিখছে…"
                            ChatSection.STEP_BY_STEP_MEMORY -> if (memoryTrickMode) {
                                "💡 ছড়া ও মনে রাখার টেকনিক তৈরি হচ্ছে…"
                            } else {
                                "🧠 ধাপে ধাপে ব্যাখ্যা (Step-by-Step) তৈরি হচ্ছে…"
                            }
                            else -> "EduAI উত্তর লিখছে…"
                        },
                        mode = activeMode,
                        subject = subject
                    )
                )
            }

            try {
                val recentHistoryContext = if (section == ChatSection.STEP_BY_STEP_MEMORY) {
                    studyRepository.getSectionMessagesSnapshot(ChatSection.STEP_BY_STEP_MEMORY)
                        .takeLast(6)
                        .joinToString("\n") { msg ->
                            val speaker = if (msg.role == "user") "Student" else "Tutor"
                            "$speaker: ${msg.content.take(420)}"
                        }
                } else {
                    ""
                }

                val options = ChatPromptOptions(
                    smartToThePoint = section == ChatSection.BRAHMASTRAM,
                    visualDiagramRequested = false,
                    translateMode = false,
                    targetMarks = null,
                    exactTextbookDefinition = getExactTextbookMode(),
                    targetExamYear = targetExamYear,
                    syllabusPlanLabel = planLabel,
                    fastLearningMicroMode = fastLearningMicroMode,
                    sectionContextHint = sectionContextHint,
                    stepByStepInteractiveMode = stepByStepInteractiveMode,
                    memoryTrickMode = memoryTrickMode,
                    recentConversationContext = recentHistoryContext
                )

                val savedImagePath = pendingPhoto?.let {
                    NotebookImageHelper.persistThumbnail(appContext, it.previewBitmap)
                }

                studyRepository.saveChatMessage(
                    ChatMessageEntity(
                        role = "user",
                        content = effectiveQuery,
                        subjectId = subject?.id,
                        mode = activeMode.storageValue,
                        sourceLabel = subject?.bengaliName ?: section.titleBn,
                        imagePath = savedImagePath,
                        sectionId = section.id,
                        topicLabel = effectiveQuery.take(54)
                    )
                )

                var completedEvent: AiStreamEvent.Completed? = null

                aiRepository.generateAnswerStream(
                    query = effectiveQuery,
                    selectedSubject = subject,
                    mode = activeMode,
                    config = llamaConfig,
                    imageBitmap = pendingPhoto?.previewBitmap,
                    options = options
                ).collect { event ->
                    when (event) {
                        is AiStreamEvent.Partial -> {
                            transientFlow.update { s ->
                                s.copy(
                                    streamingDraft = StreamingDraft(
                                        text = event.accumulatedText,
                                        sourceLabel = event.sourceLabel,
                                        mode = activeMode,
                                        subject = subject
                                    )
                                )
                            }
                        }

                        is AiStreamEvent.Completed -> completedEvent = event
                    }
                }

                val finalEvent = completedEvent ?: throw IllegalStateException("উত্তর পাওয়া যায়নি।")

                studyRepository.saveChatMessage(
                    ChatMessageEntity(
                        role = "assistant",
                        content = finalEvent.fullText,
                        subjectId = (finalEvent.resolvedSubject ?: subject)?.id,
                        mode = activeMode.storageValue,
                        sourceLabel = finalEvent.sourceLabel,
                        latencyMs = finalEvent.latencyMs,
                        sectionId = section.id,
                        topicLabel = effectiveQuery.take(54)
                    )
                )

                transientFlow.update {
                    it.copy(
                        isGenerating = false,
                        streamingDraft = null
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(SECTION_CHAT_TAG, "${section.id} timed out", e)
                transientFlow.update {
                    it.copy(
                        isGenerating = false,
                        streamingDraft = null,
                        errorMessage = "অনলাইন উত্তর ৬০ সেকেন্ডের মধ্যে আসেনি। আবার চেষ্টা করো বা অফলাইন মোড ব্যবহার করো।"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(SECTION_CHAT_TAG, "${section.id} failed", e)
                transientFlow.update {
                    it.copy(
                        isGenerating = false,
                        streamingDraft = null,
                        errorMessage = e.localizedMessage ?: "উত্তর তৈরি করতে সমস্যা হয়েছে।"
                    )
                }
            }
        }
    }

    fun stopAll() {
        activeJobs.values.forEach { it?.cancel() }
        activeJobs.clear()
    }
}
