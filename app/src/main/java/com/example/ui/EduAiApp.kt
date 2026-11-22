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
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Memory
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.AppTab
import com.example.ui.chat.ChatScreen
import com.example.ui.model.ModelManagerScreen
import com.example.ui.subjects.SubjectsScreen
import com.example.viewmodel.EduAiUiState
import com.example.viewmodel.EduAiViewModel
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error)
            viewModel.dismissError()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavRail) {
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
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
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
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    ActiveScreenHost(
                        uiState = uiState,
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

@Composable
private fun ActiveScreenHost(
    uiState: EduAiUiState,
    viewModel: EduAiViewModel,
    onShowSnackbar: (String) -> Unit
) {
    when (uiState.activeTab) {
        AppTab.CHAT -> ChatScreen(
            uiState = uiState,
            onToggleMode = viewModel::toggleInferenceMode,
            onSelectSubject = viewModel::selectChatSubject,
            onSendQuestion = { q -> viewModel.sendQuestion(q) },
            onClearChat = viewModel::clearChatHistory,
            onOpenModelHub = { viewModel.selectTab(AppTab.MODEL_HUB) },
            onShowToast = onShowSnackbar
        )

        AppTab.SUBJECTS -> SubjectsScreen(
            masterNotes = uiState.masterNotes,
            selectedSubject = uiState.selectedDetailSubject,
            searchQuery = uiState.subjectSearchQuery,
            onSearchQueryChange = viewModel::updateSubjectSearchQuery,
            onSelectSubject = viewModel::openSubjectDetail,
            onAskTutorFromNote = viewModel::askFromMasterNote,
            onToggleBookmark = viewModel::toggleBookmark,
            onAddCustomNote = viewModel::addCustomNote,
            onDeleteCustomNote = viewModel::deleteCustomNote
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
            onClearChatHistory = viewModel::clearChatHistory,
            onDismissStatusBanner = viewModel::dismissModelStatusBanner
        )
    }
}
