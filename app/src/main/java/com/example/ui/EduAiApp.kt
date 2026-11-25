package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.AppTab
import com.example.model.ChatSection
import com.example.model.DailyPlannerUiState
import com.example.model.PocketBookCategory
import com.example.model.SpacedRevisionUiState
import com.example.model.WbbseSubject
import com.example.ui.chat.ChatScreen
import com.example.ui.exam.ExamScreen
import com.example.ui.extras.AudioRevisionPlayerModal
import com.example.ui.extras.DailyPlannerSheetDialog
import com.example.ui.extras.InteractiveMapBoardScreen
import com.example.ui.extras.RapidFireScreen
import com.example.ui.hub.FloatingDActionHub
import com.example.ui.hub.VisualMindMapModal
import com.example.ui.learning.FastLearningScreen
import com.example.ui.learning.SmartLearningScreen
import com.example.ui.master.AiVivaOralScreen
import com.example.ui.master.BrahmastramScreen
import com.example.ui.master.ConfusionCleanerScreen
import com.example.ui.master.DiagramLabellingScreen
import com.example.ui.master.LanguageLabScreen
import com.example.ui.master.MathMensurationSolverScreen
import com.example.ui.master.PdfExportDialog
import com.example.ui.master.PocketBookScreen
import com.example.ui.master.SavedPdfLibraryScreen
import com.example.ui.master.WritingStudioScreen
import com.example.ui.model.ModelManagerScreen
import com.example.ui.subjects.SubjectsScreen
import com.example.ui.topper.TopperScreen
import com.example.viewmodel.EduAiUiState
import com.example.viewmodel.EduAiViewModel
import com.example.viewmodel.TopperUiState
import kotlinx.coroutines.launch

private data class NavDestination(
    val tab: AppTab,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val appDestinations = listOf(
    NavDestination(
        tab = AppTab.CHAT,
        labelRes = R.string.nav_chat,
        selectedIcon = Icons.Rounded.Forum,
        unselectedIcon = Icons.Outlined.Forum,
        testTag = "nav_tab_chat"
    ),
    NavDestination(
        tab = AppTab.SUBJECTS,
        labelRes = R.string.nav_subjects,
        selectedIcon = Icons.Rounded.LibraryBooks,
        unselectedIcon = Icons.Outlined.LibraryBooks,
        testTag = "nav_tab_subjects"
    ),
    NavDestination(
        tab = AppTab.EXAMS,
        labelRes = R.string.nav_exams,
        selectedIcon = Icons.Rounded.Quiz,
        unselectedIcon = Icons.Outlined.Quiz,
        testTag = "nav_tab_exams"
    ),
    NavDestination(
        tab = AppTab.TOPPER,
        labelRes = R.string.nav_topper,
        selectedIcon = Icons.Rounded.WorkspacePremium,
        unselectedIcon = Icons.Outlined.WorkspacePremium,
        testTag = "nav_tab_topper"
    ),
    NavDestination(
        tab = AppTab.MODEL_HUB,
        labelRes = R.string.nav_model,
        selectedIcon = Icons.Rounded.Memory,
        unselectedIcon = Icons.Outlined.Memory,
        testTag = "nav_tab_model"
    )
)

@Composable
fun EduAiApp(
    viewModel: EduAiViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val topperState by viewModel.topperState.collectAsStateWithLifecycle()
    val smartLearningState by viewModel.smartLearningState.collectAsStateWithLifecycle()
    val plannerState by viewModel.plannerState.collectAsStateWithLifecycle()
    val mapBoardState by viewModel.mapBoardState.collectAsStateWithLifecycle()
    val rapidFireState by viewModel.rapidFireState.collectAsStateWithLifecycle()
    val audioRevisionState by viewModel.audioRevisionState.collectAsStateWithLifecycle()
    val pdfState by viewModel.pdfState.collectAsStateWithLifecycle()
    val brahmastramState by viewModel.brahmastramState.collectAsStateWithLifecycle()
    val pocketBookState by viewModel.pocketBookState.collectAsStateWithLifecycle()
    val vivaState by viewModel.vivaState.collectAsStateWithLifecycle()
    val confusionState by viewModel.confusionState.collectAsStateWithLifecycle()
    val spacedRevisionState by viewModel.spacedRevisionState.collectAsStateWithLifecycle()
    val diagramLabState by viewModel.diagramLabState.collectAsStateWithLifecycle()
    val writingStudioState by viewModel.writingStudioState.collectAsStateWithLifecycle()
    val languageLabState by viewModel.languageLabState.collectAsStateWithLifecycle()
    val mathSolverState by viewModel.mathSolverState.collectAsStateWithLifecycle()
    val backupStatusMessage by viewModel.backupStatusMessage.collectAsStateWithLifecycle()
    val speechState by viewModel.speechState.collectAsStateWithLifecycle()
    val podcastState by viewModel.podcastState.collectAsStateWithLifecycle()
    val mindMapTree by viewModel.mindMapTree.collectAsStateWithLifecycle()
    val voiceInjectedText by viewModel.voiceInjectedText.collectAsStateWithLifecycle()
    val fastLearningState by viewModel.fastLearningState.collectAsStateWithLifecycle()
    val allBrahmastramProgress by viewModel.allBrahmastramProgress.collectAsStateWithLifecycle()
    val brahmastramChatState by viewModel.brahmastramChatState.collectAsStateWithLifecycle()
    val mindMapChatState by viewModel.mindMapChatState.collectAsStateWithLifecycle()
    val fastLearningChatState by viewModel.fastLearningChatState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val hasFullScreenOverlay = fastLearningState.isOpen ||
        smartLearningState.isOpen ||
        mapBoardState.isOpen ||
        rapidFireState.isOpen ||
        audioRevisionState.isOpen ||
        pdfState.isOpen ||
        brahmastramState.isOpen ||
        pocketBookState.isOpen ||
        vivaState.isOpen ||
        confusionState.isOpen ||
        diagramLabState.isOpen ||
        writingStudioState.isOpen ||
        languageLabState.isOpen ||
        mathSolverState.isOpen

    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error)
            viewModel.dismissError()
        }
    }

    LaunchedEffect(topperState.errorMessage) {
        val error = topperState.errorMessage
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error)
            viewModel.dismissTopperError()
        }
    }

    LaunchedEffect(smartLearningState.errorMessage) {
        val error = smartLearningState.errorMessage
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error)
            viewModel.dismissSmartLearningError()
        }
    }

    LaunchedEffect(pdfState.statusMessage) {
        val status = pdfState.statusMessage
        if (!status.isNullOrBlank() && pdfState.pendingExportRequest == null) {
            snackbarHostState.showSnackbar(status)
            viewModel.clearPdfStatusMessage()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavRail && !hasFullScreenOverlay) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        appDestinations.forEach { dest ->
                            val selected = uiState.activeTab == dest.tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(dest.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelRes),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            when {
                audioRevisionState.isOpen -> {
                    AudioRevisionPlayerModal(
                        state = audioRevisionState,
                        onTogglePlayPause = viewModel::toggleAudioRevisionPlayPause,
                        onNext = viewModel::nextAudioRevisionTrack,
                        onPrevious = viewModel::previousAudioRevisionTrack,
                        onSelectTrack = viewModel::selectAudioRevisionTrack,
                        onSelectSpeed = viewModel::setAudioRevisionSpeed,
                        onToggleAutoAdvance = viewModel::toggleAudioRevisionAutoAdvance,
                        onClose = viewModel::closeAudioRevision,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                mapBoardState.isOpen -> {
                    InteractiveMapBoardScreen(
                        domain = mapBoardState.domain,
                        onSwitchDomain = viewModel::switchMapBoardDomain,
                        onBack = viewModel::closeMapBoard,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                rapidFireState.isOpen -> {
                    RapidFireScreen(
                        state = rapidFireState,
                        onSelectSubject = viewModel::selectRapidFireSubject,
                        onSelectQuestionCount = viewModel::selectRapidFireQuestionCount,
                        onSelectTimerMode = viewModel::selectRapidFireTimerMode,
                        onStartDrill = viewModel::startRapidFireDrill,
                        onAnswerOption = viewModel::answerRapidFireOption,
                        onNextQuestion = viewModel::advanceRapidFireNext,
                        onFinishEarly = viewModel::finishRapidFireEarly,
                        onOpenMistakeRecovery = viewModel::openMistakeRecoveryFromRapidFire,
                        onBack = viewModel::closeRapidFire,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                pdfState.isOpen -> {
                    SavedPdfLibraryScreen(
                        state = pdfState,
                        onRename = viewModel::renameSavedPdf,
                        onDelete = viewModel::deleteSavedPdf,
                        onBack = viewModel::closeSavedPdfLibrary,
                        onShowToast = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                fastLearningState.isOpen -> {
                    FastLearningScreen(
                        state = fastLearningState,
                        chatState = fastLearningChatState,
                        masterNotes = uiState.masterNotes,
                        examAttempts = uiState.examAttempts,
                        brahmastramProgress = allBrahmastramProgress,
                        inferenceMode = uiState.inferenceMode,
                        speakingMessageId = uiState.ttsState.speakingMessageId,
                        voiceInjectedText = voiceInjectedText,
                        onToggleInferenceMode = viewModel::toggleInferenceMode,
                        onSelectTab = viewModel::selectFastLearningTab,
                        onSelectSubject = viewModel::selectFastLearningSubject,
                        onSelectExamYear = viewModel::setFastLearningExamYear,
                        onSelectPlan = viewModel::setFastLearningPlan,
                        onToggleChapterCompleted = viewModel::toggleFastLearningChapterCompleted,
                        onQuickReviseChapter = viewModel::quickReviseChapterInFastLearning,
                        onSendChatMessage = { msg ->
                            viewModel.sendSectionChatMessage(
                                section = ChatSection.FAST_LEARNING,
                                rawText = msg,
                                subject = fastLearningState.selectedSubject,
                                sectionContextHint = "Fast Learning Hub • Target Madhyamik ${fastLearningState.targetExamYear} (${fastLearningState.syllabusPlan.labelEn})"
                            )
                        },
                        onAttachChatPhoto = { uri -> viewModel.attachSectionChatPhoto(ChatSection.FAST_LEARNING, uri) },
                        onClearPendingPhoto = { viewModel.clearSectionChatPendingPhoto(ChatSection.FAST_LEARNING) },
                        onClearChat = { viewModel.clearSectionChat(ChatSection.FAST_LEARNING) },
                        onToggleSpeak = viewModel::toggleSpeakMessage,
                        onDismissChatError = { viewModel.dismissSectionChatError(ChatSection.FAST_LEARNING) },
                        onConsumeVoiceInjectedText = viewModel::consumeVoiceInjectedText,
                        onShowSnackbar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                        onClose = viewModel::closeFastLearning,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                brahmastramState.isOpen -> {
                    BrahmastramScreen(
                        state = brahmastramState,
                        chatState = brahmastramChatState,
                        inferenceMode = uiState.inferenceMode,
                        speakingMessageId = uiState.ttsState.speakingMessageId,
                        voiceInjectedText = voiceInjectedText,
                        onSelectSubject = viewModel::selectBrahmastramSubject,
                        onRegenerateNotes = viewModel::regenerateBrahmastramNotes,
                        onToggleNoteCompleted = viewModel::toggleBrahmastramNoteCompleted,
                        onToggleSpeak = viewModel::toggleSpeakMessage,
                        onExportNotesPdf = { name, title, body ->
                            viewModel.requestPdfExport(name, title, "ব্রহ্মাস্ত্রম নোটস • WBBSE", body)
                        },
                        onStartTimedExam = viewModel::startBrahmastramExam,
                        onSelectMcqOption = viewModel::selectBrahmastramMcqOption,
                        onUpdateTypedAnswer = viewModel::updateBrahmastramTypedAnswer,
                        onSubmitExam = viewModel::submitBrahmastramExam,
                        onBackToNotes = viewModel::returnToBrahmastramNotes,
                        onSendChatMessage = { msg ->
                            viewModel.sendSectionChatMessage(
                                section = ChatSection.BRAHMASTRAM,
                                rawText = msg,
                                subject = brahmastramState.selectedSubject,
                                sectionContextHint = "Brahmastram Sure-Shot Suggestions (${brahmastramState.selectedSubject.bengaliName})"
                            )
                        },
                        onAttachChatPhoto = { uri -> viewModel.attachSectionChatPhoto(ChatSection.BRAHMASTRAM, uri) },
                        onClearChatPendingPhoto = { viewModel.clearSectionChatPendingPhoto(ChatSection.BRAHMASTRAM) },
                        onClearChat = { viewModel.clearSectionChat(ChatSection.BRAHMASTRAM) },
                        onDismissChatError = { viewModel.dismissSectionChatError(ChatSection.BRAHMASTRAM) },
                        onConsumeVoiceInjectedText = viewModel::consumeVoiceInjectedText,
                        onShowSnackbar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                        onClose = viewModel::closeBrahmastram,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                pocketBookState.isOpen -> {
                    PocketBookScreen(
                        state = pocketBookState,
                        savedPdfCount = pdfState.items.size,
                        onSelectCategory = viewModel::selectPocketBookCategory,
                        onSearchChange = viewModel::updatePocketBookSearch,
                        onOpenSavedPdfLibrary = viewModel::openSavedPdfLibrary,
                        onExportCardPdf = { name, title, body ->
                            viewModel.requestPdfExport(name, title, "পকেট বুক • WBBSE", body)
                        },
                        onBack = viewModel::closePocketBook,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                confusionState.isOpen -> {
                    ConfusionCleanerScreen(
                        state = confusionState,
                        onExportPdf = { name, title, body ->
                            viewModel.requestPdfExport(name, title, "কনফিউশন ক্লিনার • WBBSE", body)
                        },
                        onBack = viewModel::closeConfusionCleaner,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                vivaState.isOpen -> {
                    AiVivaOralScreen(
                        state = vivaState,
                        onRepeatQuestionAloud = viewModel::speakCurrentVivaQuestion,
                        onSubmitAnswer = viewModel::submitVivaAnswer,
                        onClose = viewModel::closeAiViva,
                        onShowToast = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                diagramLabState.isOpen -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        DiagramLabellingScreen(
                            state = diagramLabState,
                            onSwitchSubject = viewModel::switchDiagramLabSubject,
                            onSelectDiagram = viewModel::selectDiagramIndex,
                            onExportPdf = viewModel::requestPdfExport,
                            onClose = viewModel::closeDiagramLab
                        )
                    }
                }

                writingStudioState.isOpen -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        WritingStudioScreen(
                            state = writingStudioState,
                            onSelectFormat = viewModel::selectWritingFormat,
                            onUpdateTopic = viewModel::updateWritingTopic,
                            onGenerate = viewModel::generateWritingOutput,
                            onExportPdf = viewModel::requestPdfExport,
                            onClose = viewModel::closeWritingStudio
                        )
                    }
                }

                languageLabState.isOpen -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        LanguageLabScreen(
                            state = languageLabState,
                            onUpdateBengaliInput = viewModel::updateBengaliAnalyzerInput,
                            onRunBengaliAnalyzer = viewModel::runBengaliGrammarAnalyzer,
                            onUpdateCustomUnseen = viewModel::updateCustomUnseenInput,
                            onSolveCustomUnseen = viewModel::solveCustomUnseenPassage,
                            onExportPdf = viewModel::requestPdfExport,
                            onClose = viewModel::closeLanguageLab
                        )
                    }
                }

                mathSolverState.isOpen -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        MathMensurationSolverScreen(
                            state = mathSolverState,
                            onSelectModule = viewModel::selectMathSolverModule,
                            onExportPdf = viewModel::requestPdfExport,
                            onClose = viewModel::closeMathSolver
                        )
                    }
                }

                smartLearningState.isOpen -> {
                    SmartLearningScreen(
                        state = smartLearningState,
                        masterNotes = uiState.masterNotes,
                        inferenceMode = uiState.inferenceMode,
                        speakingMessageId = uiState.ttsState.speakingMessageId,
                        onBack = viewModel::closeSmartLearning,
                        onToggleInferenceMode = viewModel::toggleInferenceMode,
                        onSelectSubject = viewModel::selectSmartLearningSubject,
                        onSelectChapter = viewModel::selectSmartLearningChapter,
                        onToggleDiagramMode = viewModel::toggleSmartLearningDiagramMode,
                        onShowOfflinePhotoNotice = viewModel::showSmartLearningOfflinePhotoNotice,
                        onDismissOfflinePhotoNotice = viewModel::dismissSmartLearningOfflinePhotoNotice,
                        onAttachPhotoUri = viewModel::attachSmartLearningPhotoUri,
                        onClearPendingPhoto = viewModel::clearSmartLearningPendingPhoto,
                        onClearSession = viewModel::clearSmartLearningSession,
                        onSendMessage = viewModel::sendSmartLearningMessage,
                        onGenerateDiagramForMessage = viewModel::generateSmartLearningDiagramForMessage,
                        onStartTwoMinuteQuiz = viewModel::startSmartLearningQuickQuiz,
                        onSelectQuizOption = viewModel::selectSmartLearningQuizOption,
                        onSubmitQuickQuiz = viewModel::submitSmartLearningQuickQuiz,
                        onCloseQuickQuiz = viewModel::closeSmartLearningQuickQuiz,
                        onStartSmartMemorizer = viewModel::startSmartLearningMemorizer,
                        onToggleMemorizerRecall = viewModel::toggleSmartLearningMemorizerRecall,
                        onToggleMemorizerRevealStep = viewModel::toggleSmartLearningMemorizerStep,
                        onCloseSmartMemorizer = viewModel::closeSmartLearningMemorizer,
                        onToggleSpeak = viewModel::toggleSpeakMessage,
                        onOpenAudioRevision = viewModel::openAudioRevisionForSmartLearning,
                        onExportPdf = viewModel::requestPdfExport,
                        onShowSnackbar = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        },
                        voiceInjectedText = voiceInjectedText,
                        onConsumeVoiceInjectedText = viewModel::consumeVoiceInjectedText,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                appDestinations.forEach { dest ->
                                    val selected = uiState.activeTab == dest.tab
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(dest.tab) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = stringResource(dest.labelRes)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(dest.labelRes),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            ActiveScreenHost(
                                uiState = uiState,
                                topperState = topperState,
                                plannerState = plannerState,
                                spacedRevisionState = spacedRevisionState,
                                savedPdfCount = pdfState.items.size,
                                backupStatusMessage = backupStatusMessage,
                                voiceInjectedText = voiceInjectedText,
                                viewModel = viewModel,
                                onShowSnackbar = { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (plannerState.isSheetOpen) {
            DailyPlannerSheetDialog(
                state = plannerState,
                inferenceMode = uiState.inferenceMode,
                onDismiss = { viewModel.setDailyPlannerSheetOpen(false) },
                onSelectHoursAndGenerate = viewModel::generateDailyPlan,
                onToggleTask = viewModel::toggleDailyPlanTask,
                onLaunchTaskAction = viewModel::launchDailyPlanTask
            )
        }

        if (pdfState.pendingExportRequest != null) {
            PdfExportDialog(
                state = pdfState,
                onConfirmGenerate = viewModel::confirmGeneratePdf,
                onDismiss = viewModel::dismissPdfExportDialog,
                onOpenLibrary = viewModel::openSavedPdfLibrary,
                onShowToast = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
            )
        }

        // Draggable AssistiveTouch-Style Floating "D" Action Hub
        FloatingDActionHub(
            speechState = speechState,
            exactTextbookMode = uiState.exactTextbookMode,
            podcastState = podcastState,
            hasLatestAnswerForMindMap = uiState.chatMessages.any { it.role != "user" } ||
                smartLearningState.messages.any { it.role != "user" } ||
                uiState.examChat.messages.any { it.role != "user" } ||
                fastLearningChatState.messages.any { it.role != "user" } ||
                brahmastramChatState.messages.any { it.role != "user" },
            onSelectVoiceLanguage = viewModel::setVoiceInputLanguage,
            onToggleVoiceListening = viewModel::toggleVoiceListening,
            onToggleExactTextbookMode = viewModel::toggleExactTextbookMode,
            onOpenMindMap = viewModel::openMindMapForLatestAnswer,
            onTogglePodcastPlayPause = viewModel::toggleQuickPodcastPlayPause,
            onStopPodcast = viewModel::stopQuickPodcast,
            onSetPodcastSpeed = viewModel::setQuickPodcastSpeed,
            onOpenFullPodcastPlayer = viewModel::openFullPodcastFromDHub,
            onShortcutFastLearning = viewModel::shortcutOpenFastLearning,
            onShortcutSmartLearning = viewModel::shortcutOpenSmartLearning,
            onShortcutBrahmastram = viewModel::shortcutOpenBrahmastram,
            onShortcutGrammarLab = viewModel::shortcutOpenGrammarLab,
            onShortcutExportPdf = viewModel::exportLatestToPdfFromDHub,
            onShortcutSettings = viewModel::shortcutOpenSettings,
            onShowMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
        )

        // Interactive Visual Mind-Map / Flowchart Modal with Dedicated Persistent Mind-Map Chat
        mindMapTree?.let { tree ->
            VisualMindMapModal(
                tree = tree,
                chatState = mindMapChatState,
                inferenceMode = uiState.inferenceMode,
                speakingMessageId = uiState.ttsState.speakingMessageId,
                voiceInjectedText = voiceInjectedText,
                onExportPdf = viewModel::exportMindMapToPdf,
                onSendChatMessage = { msg ->
                    viewModel.sendSectionChatMessage(
                        section = ChatSection.MIND_MAP,
                        rawText = msg,
                        subject = uiState.selectedChatSubject,
                        sectionContextHint = "Visual Mind-Map Topic: ${tree.rootTitle} (${tree.subjectLabel})"
                    )
                },
                onAttachChatPhoto = { uri -> viewModel.attachSectionChatPhoto(ChatSection.MIND_MAP, uri) },
                onClearChatPendingPhoto = { viewModel.clearSectionChatPendingPhoto(ChatSection.MIND_MAP) },
                onClearChat = { viewModel.clearSectionChat(ChatSection.MIND_MAP) },
                onToggleSpeak = viewModel::toggleSpeakMessage,
                onDismissChatError = { viewModel.dismissSectionChatError(ChatSection.MIND_MAP) },
                onConsumeVoiceInjectedText = viewModel::consumeVoiceInjectedText,
                onRebuildMindMapFromReply = viewModel::openMindMapFromText,
                onShowToast = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                onDismiss = viewModel::closeMindMap
            )
        }
    }
}

private fun pocketCategoryForSubject(subject: WbbseSubject): PocketBookCategory = when (subject) {
    WbbseSubject.MATHEMATICS -> PocketBookCategory.MATH_FORMULAS
    WbbseSubject.PHYSICAL_SCIENCE -> PocketBookCategory.PHYSICS_REACTIONS
    WbbseSubject.LIFE_SCIENCE -> PocketBookCategory.LIFE_SCIENCE_KEYS
    WbbseSubject.HISTORY, WbbseSubject.GEOGRAPHY -> PocketBookCategory.HISTORY_DATES
    WbbseSubject.BENGALI, WbbseSubject.ENGLISH -> PocketBookCategory.LITERATURE_AUTHORS
}

@Composable
private fun ActiveScreenHost(
    uiState: EduAiUiState,
    topperState: TopperUiState,
    plannerState: DailyPlannerUiState,
    spacedRevisionState: SpacedRevisionUiState,
    savedPdfCount: Int,
    backupStatusMessage: String?,
    voiceInjectedText: String?,
    viewModel: EduAiViewModel,
    onShowSnackbar: (String) -> Unit
) {
    when (uiState.activeTab) {
        AppTab.CHAT -> ChatScreen(
            uiState = uiState,
            plannerState = plannerState,
            spacedRevisionState = spacedRevisionState,
            savedPdfCount = savedPdfCount,
            onToggleMode = viewModel::toggleInferenceMode,
            onSelectSubject = viewModel::selectChatSubject,
            onSendQuestion = { q -> viewModel.sendQuestion(q) },
            onAttachPhotoUri = viewModel::attachChatPhotoUri,
            onClearPendingPhoto = viewModel::clearPendingChatPhoto,
            onToggleSmartAnswer = viewModel::toggleChatSmartToThePoint,
            onToggleVisualMode = viewModel::toggleChatVisualMode,
            onToggleTranslateMode = viewModel::toggleChatTranslateMode,
            onSelectTargetMarks = viewModel::setChatTargetMarks,
            onToggleSpeakMessage = viewModel::toggleSpeakMessage,
            onClearChat = viewModel::clearChatHistory,
            onOpenModelHub = { viewModel.selectTab(AppTab.MODEL_HUB) },
            onOpenSmartLearning = { subj -> viewModel.openSmartLearning(subject = subj) },
            onOpenPlannerSheet = { viewModel.setDailyPlannerSheetOpen(true) },
            onSelectDailyHours = viewModel::generateDailyPlan,
            onToggleDailyTask = viewModel::toggleDailyPlanTask,
            onLaunchDailyTask = viewModel::launchDailyPlanTask,
            onMarkRevisionDone = viewModel::markSpacedRevisionCompleted,
            onFocusWeakSubject = { subj ->
                viewModel.selectTab(AppTab.SUBJECTS)
                viewModel.openSubjectDetail(subj)
            },
            onOpenBrahmastram = {
                viewModel.openBrahmastram(uiState.selectedChatSubject ?: WbbseSubject.PHYSICAL_SCIENCE)
            },
            onOpenPocketBook = { viewModel.openPocketBook() },
            onOpenSavedPdfs = viewModel::openSavedPdfLibrary,
            onExportPdf = viewModel::requestPdfExport,
            onShowToast = onShowSnackbar,
            voiceInjectedText = voiceInjectedText,
            onConsumeVoiceInjectedText = viewModel::consumeVoiceInjectedText
        )

        AppTab.SUBJECTS -> SubjectsScreen(
            masterNotes = uiState.masterNotes,
            selectedSubject = uiState.selectedDetailSubject,
            searchQuery = uiState.subjectSearchQuery,
            onSearchQueryChange = viewModel::updateSubjectSearchQuery,
            onSelectSubject = viewModel::openSubjectDetail,
            onAskTutorFromNote = viewModel::askFromMasterNote,
            onOpenSmartLearningForSubject = { subj -> viewModel.openSmartLearning(subject = subj) },
            onOpenSmartLearningForNote = { note -> viewModel.openSmartLearning(autoExplainNote = note) },
            onOpenMapBoard = viewModel::openMapBoard,
            onOpenRapidFire = viewModel::openRapidFire,
            onOpenAudioRevisionForSubject = viewModel::openAudioRevisionForSubject,
            onOpenAudioRevisionForChapter = viewModel::openAudioRevisionForChapter,
            onOpenDiagramLab = viewModel::openDiagramLab,
            onOpenWritingStudio = viewModel::openWritingStudio,
            onOpenLanguageLab = viewModel::openLanguageLab,
            onOpenMathSolver = viewModel::openMathSolver,
            onOpenAiViva = { subj -> viewModel.openAiViva(subj) },
            onOpenConfusionCleaner = viewModel::openConfusionCleaner,
            onOpenPocketBook = { subj -> viewModel.openPocketBook(pocketCategoryForSubject(subj)) },
            onExportPdf = viewModel::requestPdfExport,
            onToggleBookmark = viewModel::toggleBookmark,
            onAddCustomNote = viewModel::addCustomNote,
            onDeleteCustomNote = viewModel::deleteCustomNote
        )

        AppTab.EXAMS -> ExamScreen(
            uiState = uiState,
            onSelectSubject = viewModel::selectExamSubject,
            onSelectFormat = viewModel::selectExamFormat,
            onRecordMcqResult = viewModel::recordMcqResult,
            onAttachExamPhotoUri = viewModel::attachExamPhotoUri,
            onClearExamPhoto = viewModel::clearPendingExamPhoto,
            onEvaluateSubjective = viewModel::evaluateSubjectiveExam,
            onClearEvaluation = viewModel::clearLatestExamEvaluation,
            onClearExamHistory = viewModel::clearExamHistory,
            onToggleExamSmartMode = viewModel::toggleExamSmartMode,
            onToggleExamVisualMode = viewModel::toggleExamVisualMode,
            onToggleExamTranslateMode = viewModel::toggleExamTranslateMode,
            onSelectExamTargetMarks = viewModel::setExamTargetMarks,
            onAttachExamChatPhotoUri = viewModel::attachExamChatPhotoUri,
            onClearExamChatPhoto = viewModel::clearPendingExamChatPhoto,
            onToggleExamSessionPause = viewModel::toggleExamSessionPause,
            onEndExamSession = viewModel::endExamSession,
            onClearExamChat = viewModel::clearExamChat,
            onSendExamChatMessage = viewModel::sendExamChatMessage,
            onToggleSpeakMessage = viewModel::toggleSpeakMessage,
            onOpenRapidFire = viewModel::openRapidFire,
            onShowSnackbar = onShowSnackbar,
            voiceInjectedText = voiceInjectedText,
            onConsumeVoiceInjectedText = viewModel::consumeVoiceInjectedText
        )

        AppTab.TOPPER -> TopperScreen(
            topperState = topperState,
            inferenceMode = uiState.inferenceMode,
            scorePredictor = uiState.scorePredictor,
            savedPdfCount = savedPdfCount,
            onSelectTrack = viewModel::selectTopperTrack,
            onToggleTwistedChallenge = viewModel::toggleTopperTwistedChallenge,
            onToggleFullBlueprint = viewModel::toggleTopperFullBlueprint,
            onStartDailySet = viewModel::startDailyTopperSet,
            onStart15MinCapsule = viewModel::start15MinOfflineCapsule,
            onStartMistakeRecovery = viewModel::startMistakeRecoveryTest,
            onSelectTimingBucket = viewModel::setTopperTimingBucket,
            onToggleTimerPause = viewModel::toggleTopperTimerPause,
            onExitActivePaper = viewModel::exitTopperActivePaper,
            onSelectMcqOption = viewModel::selectTopperMcqAnswer,
            onUpdateTypedAnswer = viewModel::updateTopperTypedAnswer,
            onAddPhotoUri = viewModel::addTopperQuestionPhotoUri,
            onRemovePhoto = viewModel::removeTopperQuestionPhoto,
            onSubmitPaper = viewModel::submitTopperPaper,
            onOpenMapBoard = viewModel::openMapBoard,
            onOpenRapidFire = viewModel::openRapidFire,
            onOpenBrahmastram = {
                viewModel.openBrahmastram(topperState.selectedTrack.linkedSubject)
            },
            onOpenPocketBook = { viewModel.openPocketBook() },
            onOpenSavedPdfs = viewModel::openSavedPdfLibrary,
            onFocusSubject = { subj ->
                viewModel.selectTab(AppTab.SUBJECTS)
                viewModel.openSubjectDetail(subj)
            },
            onShowSnackbar = onShowSnackbar
        )

        AppTab.MODEL_HUB -> ModelManagerScreen(
            uiState = uiState,
            onImportGgufUri = viewModel::importGgufFromUri,
            onScanModels = viewModel::scanGgufModels,
            onCreateDiagnosticGguf = viewModel::createDiagnosticGgufTestFile,
            onLoadModel = viewModel::loadModelFile,
            onUnloadModel = viewModel::unloadModel,
            onDeleteModel = viewModel::deleteModelFile,
            onToggleInferenceMode = viewModel::toggleInferenceMode,
            onUpdateLlamaConfig = viewModel::updateLlamaConfig,
            onUpdateTtsVoiceSettings = viewModel::updateTtsVoiceSettings,
            onTestTtsVoice = viewModel::testTtsVoice,
            onRefreshTtsVoices = viewModel::refreshTtsVoices,
            onClearChatHistory = viewModel::clearChatHistory,
            onDismissStatusBanner = viewModel::dismissModelStatusBanner,
            backupStatusMessage = backupStatusMessage,
            onExportBackupUri = viewModel::exportProgressBackup,
            onImportBackupUri = viewModel::importProgressBackup,
            onDismissBackupStatus = viewModel::clearBackupStatus
        )
    }
}
