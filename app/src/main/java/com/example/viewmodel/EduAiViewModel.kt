package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NetworkMonitor
import com.example.data.ai.AiRepository
import com.example.data.ai.AiStreamEvent
import com.example.data.ai.HybridEduAiRepository
import com.example.data.gguf.GgufManagerState
import com.example.data.gguf.GgufModelManager
import com.example.data.local.ChatMessageEntity
import com.example.data.local.EduAiDatabase
import com.example.data.local.MasterDataEntity
import com.example.data.local.SettingsDataStore
import com.example.data.local.StoredAppSettings
import com.example.data.local.StudyRepository
import com.example.model.AppTab
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.WbbseSubject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StreamingDraft(
    val text: String,
    val sourceLabel: String,
    val mode: InferenceMode,
    val subject: WbbseSubject?
)

data class EduAiUiState(
    val activeTab: AppTab = AppTab.CHAT,
    val selectedChatSubject: WbbseSubject? = null,
    val selectedDetailSubject: WbbseSubject? = null,
    val subjectSearchQuery: String = "",
    val inferenceMode: InferenceMode = InferenceMode.OFFLINE,
    val isDeviceOnline: Boolean = false,
    val llamaConfig: LlamaInferenceConfig = LlamaInferenceConfig(),
    val ggufState: GgufManagerState = GgufManagerState(),
    val masterNotes: List<MasterDataEntity> = emptyList(),
    val chatMessages: List<ChatMessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingDraft: StreamingDraft? = null,
    val errorMessage: String? = null
)

private data class LocalUiNavigationState(
    val activeTab: AppTab = AppTab.CHAT,
    val selectedChatSubject: WbbseSubject? = null,
    val selectedDetailSubject: WbbseSubject? = null,
    val subjectSearchQuery: String = "",
    val isGenerating: Boolean = false,
    val streamingDraft: StreamingDraft? = null,
    val errorMessage: String? = null
)

class EduAiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = EduAiDatabase.getInstance(application)
    private val studyRepository = StudyRepository(
        masterDataDao = database.masterDataDao(),
        chatMessageDao = database.chatMessageDao()
    )
    private val settingsDataStore = SettingsDataStore(application)
    private val ggufModelManager = GgufModelManager(application)
    private val networkMonitor = NetworkMonitor(application)
    private val aiRepository: AiRepository = HybridEduAiRepository(
        studyRepository = studyRepository,
        ggufModelManager = ggufModelManager
    )

    private val localState = MutableStateFlow(LocalUiNavigationState())
    private var generationJob: Job? = null

    private val dataSourcesFlow = combine(
        settingsDataStore.settingsFlow,
        ggufModelManager.state,
        studyRepository.allMasterNotes,
        studyRepository.allChatMessages,
        networkMonitor.isOnline
    ) { settings, gguf, notes, messages, isOnline ->
        DataBundle(settings, gguf, notes, messages, isOnline)
    }

    val uiState: StateFlow<EduAiUiState> = combine(
        localState,
        dataSourcesFlow
    ) { nav, data ->
        EduAiUiState(
            activeTab = nav.activeTab,
            selectedChatSubject = nav.selectedChatSubject,
            selectedDetailSubject = nav.selectedDetailSubject,
            subjectSearchQuery = nav.subjectSearchQuery,
            inferenceMode = data.settings.inferenceMode,
            isDeviceOnline = data.isOnline,
            llamaConfig = data.settings.llamaConfig,
            ggufState = data.gguf,
            masterNotes = data.notes,
            chatMessages = data.messages,
            isGenerating = nav.isGenerating,
            streamingDraft = nav.streamingDraft,
            errorMessage = nav.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = EduAiUiState()
    )

    init {
        viewModelScope.launch {
            studyRepository.ensureSeeded()
            val stored = settingsDataStore.settingsFlow.first()
            val detectedPath = ggufModelManager.scanAndAutoDetect(
                savedActivePath = stored.activeModelPath,
                config = stored.llamaConfig
            )
            if (detectedPath != stored.activeModelPath) {
                settingsDataStore.setActiveModelPath(detectedPath)
            }
        }
    }

    fun selectTab(tab: AppTab) {
        localState.update { it.copy(activeTab = tab) }
    }

    fun selectChatSubject(subject: WbbseSubject?) {
        localState.update { it.copy(selectedChatSubject = subject) }
    }

    fun openSubjectDetail(subject: WbbseSubject?) {
        localState.update { it.copy(selectedDetailSubject = subject) }
    }

    fun updateSubjectSearchQuery(query: String) {
        localState.update { it.copy(subjectSearchQuery = query) }
    }

    fun toggleInferenceMode(isOnlineMode: Boolean) {
        val target = if (isOnlineMode) InferenceMode.ONLINE else InferenceMode.OFFLINE
        viewModelScope.launch {
            settingsDataStore.setInferenceMode(target)
        }
    }

    fun sendQuestion(rawQuestion: String, explicitSubject: WbbseSubject? = null) {
        val trimmed = rawQuestion.trim()
        if (trimmed.isEmpty() || localState.value.isGenerating) return

        val snapshot = uiState.value
        val activeSubject = explicitSubject ?: snapshot.selectedChatSubject
        val activeMode = snapshot.inferenceMode
        val config = snapshot.llamaConfig

        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            localState.update {
                it.copy(
                    activeTab = AppTab.CHAT,
                    selectedChatSubject = activeSubject,
                    isGenerating = true,
                    errorMessage = null,
                    streamingDraft = StreamingDraft(
                        text = "",
                        sourceLabel = if (activeMode == InferenceMode.OFFLINE) {
                            "অফলাইন WBBSE ও Llama ইঞ্জিন…"
                        } else {
                            "অনলাইন স্যাম্পল প্রিভিউ…"
                        },
                        mode = activeMode,
                        subject = activeSubject
                    )
                )
            }

            try {
                studyRepository.saveChatMessage(
                    ChatMessageEntity(
                        role = "user",
                        content = trimmed,
                        subjectId = activeSubject?.id,
                        mode = activeMode.storageValue,
                        sourceLabel = activeSubject?.bengaliName ?: "সব বিষয়"
                    )
                )

                aiRepository.generateAnswerStream(
                    query = trimmed,
                    selectedSubject = activeSubject,
                    mode = activeMode,
                    config = config
                ).collect { event ->
                    when (event) {
                        is AiStreamEvent.Partial -> {
                            localState.update { state ->
                                state.copy(
                                    streamingDraft = StreamingDraft(
                                        text = event.accumulatedText,
                                        sourceLabel = event.sourceLabel,
                                        mode = activeMode,
                                        subject = activeSubject
                                    )
                                )
                            }
                        }

                        is AiStreamEvent.Completed -> {
                            studyRepository.saveChatMessage(
                                ChatMessageEntity(
                                    role = "assistant",
                                    content = event.fullText,
                                    subjectId = event.resolvedSubject?.id,
                                    mode = activeMode.storageValue,
                                    sourceLabel = event.sourceLabel,
                                    latencyMs = event.latencyMs
                                )
                            )
                            localState.update { state ->
                                state.copy(
                                    isGenerating = false,
                                    streamingDraft = null
                                )
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                localState.update { state ->
                    state.copy(
                        isGenerating = false,
                        streamingDraft = null,
                        errorMessage = e.localizedMessage ?: "উত্তর তৈরি করতে সমস্যা হয়েছে।"
                    )
                }
            }
        }
    }

    fun askFromMasterNote(note: MasterDataEntity) {
        val subject = WbbseSubject.fromId(note.subjectId)
        val prompt = "${note.topicTitle} (${note.chapterName}) সহজভাবে বুঝিয়ে দাও এবং পরীক্ষার মূল পয়েন্ট বলো।"
        localState.update {
            it.copy(
                activeTab = AppTab.CHAT,
                selectedChatSubject = subject,
                selectedDetailSubject = null
            )
        }
        sendQuestion(prompt, explicitSubject = subject)
    }

    fun clearChatHistory() {
        generationJob?.cancel()
        viewModelScope.launch {
            studyRepository.clearChatHistory()
            localState.update {
                it.copy(isGenerating = false, streamingDraft = null, errorMessage = null)
            }
        }
    }

    fun toggleBookmark(note: MasterDataEntity) {
        viewModelScope.launch {
            studyRepository.toggleBookmark(note.id, !note.isBookmarked)
        }
    }

    fun addCustomNote(
        subject: WbbseSubject,
        chapterName: String,
        topicTitle: String,
        content: String,
        keyFact: String
    ) {
        if (topicTitle.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            studyRepository.addCustomStudyNote(
                MasterDataEntity(
                    subjectId = subject.id,
                    chapterName = chapterName.ifBlank { "আমার নোট (${subject.bengaliName})" },
                    topicTitle = topicTitle.trim(),
                    keywords = "$topicTitle, $chapterName, ${subject.bengaliName}, ${subject.englishName}",
                    content = content.trim(),
                    keyFormulaOrFact = keyFact.ifBlank { "নিজস্ব স্টাডি নোট" },
                    examTip = "নিজের তৈরি নোট নিয়মিত রিভিশন করলে মাধ্যমিকে ভালো ফল হয়।",
                    isUserCreated = true
                )
            )
        }
    }

    fun deleteCustomNote(noteId: Long) {
        viewModelScope.launch {
            studyRepository.deleteCustomStudyNote(noteId)
        }
    }

    fun importGgufFromUri(uri: Uri) {
        val config = uiState.value.llamaConfig
        viewModelScope.launch {
            val result = ggufModelManager.importFromSafUri(uri, config)
            result.onSuccess { metadata ->
                settingsDataStore.setActiveModelPath(metadata.absolutePath)
            }
        }
    }

    fun scanGgufModels() {
        val snapshot = uiState.value
        viewModelScope.launch {
            val loadedPath = ggufModelManager.scanAndAutoDetect(
                savedActivePath = snapshot.ggufState.activeMetadata?.absolutePath,
                config = snapshot.llamaConfig
            )
            settingsDataStore.setActiveModelPath(loadedPath)
        }
    }

    fun createDiagnosticGgufTestFile() {
        val config = uiState.value.llamaConfig
        viewModelScope.launch {
            val metadata = ggufModelManager.createAndLoadDiagnosticGguf(config)
            settingsDataStore.setActiveModelPath(metadata.absolutePath)
        }
    }

    fun loadModelFile(path: String) {
        val config = uiState.value.llamaConfig
        viewModelScope.launch {
            val meta = ggufModelManager.loadModelByPath(path, config)
            if (meta != null && meta.isValidMagic) {
                settingsDataStore.setActiveModelPath(meta.absolutePath)
            }
        }
    }

    fun unloadModel() {
        viewModelScope.launch {
            ggufModelManager.unloadCurrentModel()
            settingsDataStore.setActiveModelPath(null)
        }
    }

    fun deleteModelFile(path: String) {
        viewModelScope.launch {
            ggufModelManager.deleteModelFile(path)
            if (uiState.value.ggufState.activeMetadata?.absolutePath == path) {
                settingsDataStore.setActiveModelPath(null)
            }
        }
    }

    fun updateLlamaConfig(config: LlamaInferenceConfig) {
        viewModelScope.launch {
            settingsDataStore.updateLlamaConfig(config)
        }
    }

    fun dismissError() {
        localState.update { it.copy(errorMessage = null) }
    }

    fun dismissModelStatusBanner() {
        ggufModelManager.clearStatusBanner()
    }

    private data class DataBundle(
        val settings: StoredAppSettings,
        val gguf: GgufManagerState,
        val notes: List<MasterDataEntity>,
        val messages: List<ChatMessageEntity>,
        val isOnline: Boolean
    )
}
