package com.example.model

import com.example.viewmodel.PendingPhotoAttachment
import com.example.viewmodel.StreamingDraft

/**
 * Single chat turn inside the "স্মার্ট লার্নিং (Smart Learning)" AI Tutor interface.
 */
data class SmartLearningMessage(
    val id: Long,
    val role: String, // "user" or "assistant"
    val content: String,
    val topicLabel: String,
    val subject: WbbseSubject?,
    val chapterName: String?,
    val mode: InferenceMode,
    val sourceLabel: String,
    val userPhotoPath: String? = null,
    val generatedDiagramPath: String? = null,
    val diagramError: String? = null,
    val isGeneratingDiagram: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuickQuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanationBn: String
)

data class QuickTopicQuiz(
    val topicTitle: String,
    val subject: WbbseSubject?,
    val chapterName: String?,
    val sourceLabel: String,
    val questions: List<QuickQuizQuestion>,
    val durationSeconds: Int = 120
)

data class MemorizerPointCard(
    val stepNumber: Int,
    val headingBn: String,
    val bodyBn: String,
    val recallCueBn: String
)

data class SmartMemorizerDeck(
    val topicTitle: String,
    val subject: WbbseSubject?,
    val broadQuestionBn: String,
    val targetMarks: Int,
    val memoryHookBn: String,
    val keywords: List<String>,
    val points: List<MemorizerPointCard>,
    val examTipBn: String,
    val sourceLabel: String
)

data class SmartLearningUiState(
    val isOpen: Boolean = false,
    val selectedSubject: WbbseSubject? = null,
    val selectedChapter: String? = null,
    val messages: List<SmartLearningMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingDraft: StreamingDraft? = null,
    val pendingPhoto: PendingPhotoAttachment? = null,
    val generateDiagramToggle: Boolean = false,
    val offlinePhotoNotice: String? = null,
    // 2-Minute Quick Quiz State
    val isLoadingQuiz: Boolean = false,
    val activeQuiz: QuickTopicQuiz? = null,
    val quizRemainingSeconds: Int = 120,
    val quizSelectedAnswers: Map<String, Int> = emptyMap(),
    val quizSubmitted: Boolean = false,
    // Smart Memorizer State
    val isLoadingMemorizer: Boolean = false,
    val activeMemorizer: SmartMemorizerDeck? = null,
    val activeRecallHiddenMode: Boolean = false,
    val revealedCardSteps: Set<Int> = emptySet(),
    val errorMessage: String? = null
)
