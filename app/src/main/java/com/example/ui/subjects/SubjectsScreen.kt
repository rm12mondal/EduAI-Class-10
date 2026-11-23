package com.example.ui.subjects

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.MasterDataEntity
import com.example.data.local.TopicImportance
import com.example.model.WbbseSubject
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline

@Composable
fun SubjectsScreen(
    masterNotes: List<MasterDataEntity>,
    selectedSubject: WbbseSubject?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectSubject: (WbbseSubject?) -> Unit,
    onAskTutorFromNote: (MasterDataEntity) -> Unit,
    onOpenSmartLearningForSubject: (WbbseSubject?) -> Unit,
    onOpenSmartLearningForNote: (MasterDataEntity) -> Unit,
    onToggleBookmark: (MasterDataEntity) -> Unit,
    onAddCustomNote: (WbbseSubject, String, String, String, String) -> Unit,
    onDeleteCustomNote: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddNoteDialog by remember { mutableStateOf(false) }

    if (selectedSubject != null) {
        BackHandler { onSelectSubject(null) }

        val allSubjectNotes = remember(masterNotes, selectedSubject) {
            masterNotes.filter { it.subjectId == selectedSubject.id }
        }

        SubjectDetailView(
            subject = selectedSubject,
            allSubjectNotes = allSubjectNotes,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onBack = { onSelectSubject(null) },
            onAskTutorFromNote = onAskTutorFromNote,
            onOpenSmartLearningForSubject = { onOpenSmartLearningForSubject(selectedSubject) },
            onOpenSmartLearningForNote = onOpenSmartLearningForNote,
            onToggleBookmark = onToggleBookmark,
            onDeleteCustomNote = onDeleteCustomNote,
            onOpenAddDialog = { showAddNoteDialog = true },
            modifier = modifier
        )

        if (showAddNoteDialog) {
            AddStudyNoteDialog(
                subject = selectedSubject,
                onDismiss = { showAddNoteDialog = false },
                onConfirm = { chapter, title, content, formula ->
                    onAddCustomNote(selectedSubject, chapter, title, content, formula)
                    showAddNoteDialog = false
                }
            )
        }
    } else {
        SubjectsOverviewList(
            masterNotes = masterNotes,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onOpenSubject = onSelectSubject,
            onAskTutorFromNote = onAskTutorFromNote,
            onOpenSmartLearningForSubject = onOpenSmartLearningForSubject,
            onOpenSmartLearningForNote = onOpenSmartLearningForNote,
            onToggleBookmark = onToggleBookmark,
            modifier = modifier
        )
    }
}

@Composable
private fun SubjectsOverviewList(
    masterNotes: List<MasterDataEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenSubject: (WbbseSubject) -> Unit,
    onAskTutorFromNote: (MasterDataEntity) -> Unit,
    onOpenSmartLearningForSubject: (WbbseSubject?) -> Unit,
    onOpenSmartLearningForNote: (MasterDataEntity) -> Unit,
    onToggleBookmark: (MasterDataEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDistinctChapters = remember(masterNotes) {
        masterNotes.map { "${it.subjectId}|${it.chapterName}" }.distinct().size
    }

    val searchResults = remember(masterNotes, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            masterNotes.filter {
                it.topicTitle.lowercase().contains(q) ||
                    it.chapterName.lowercase().contains(q) ||
                    it.keywords.lowercase().contains(q) ||
                    it.content.lowercase().contains(q)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("subjects_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.subjects_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "সম্পূর্ণ WBBSE ক্লাস ১০ সিলেবাস • ${totalDistinctChapters}টি অধ্যায় • ${masterNotes.size}টি অফলাইন টপিক",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Flagship Smart Learning Launcher Banner on Subjects Overview
            item(key = "subjects_smart_learning_banner") {
                Card(
                    onClick = { onOpenSmartLearningForSubject(null) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.2.dp, OnlineCobalt.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subjects_smart_learning_banner")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        OnlineCobalt.copy(alpha = 0.16f),
                                        SageOffline.copy(alpha = 0.14f)
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = OnlineCobalt,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.School,
                                        contentDescription = "Smart Learning",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.smart_learning_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "সহজ বাংলায় AI টিউটর • বইয়ের ছবি দিয়ে সমাধান • ২ডি ডায়াগ্রাম • ২-মিনিট কুইজ ও মেমোরাইজার",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(stringResource(R.string.subjects_search_hint)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subjects_search_field")
                )
            }

            if (searchQuery.isNotBlank()) {
                item {
                    Text(
                        text = "অনুসন্ধানের ফলাফল (${searchResults.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(searchResults, key = { it.id }) { note ->
                    val subj = WbbseSubject.fromId(note.subjectId) ?: WbbseSubject.BENGALI
                    MasterNoteCard(
                        note = note,
                        accentColor = subj.accentColor,
                        subjectBadge = subj.bengaliName,
                        onAskTutor = { onAskTutorFromNote(note) },
                        onOpenSmartLearning = { onOpenSmartLearningForNote(note) },
                        onToggleBookmark = { onToggleBookmark(note) },
                        onDeleteCustom = null
                    )
                }
            } else {
                items(WbbseSubject.entries, key = { it.id }) { subject ->
                    val subjectNotes = remember(masterNotes, subject.id) {
                        masterNotes.filter { it.subjectId == subject.id }
                    }
                    val distinctChapterCount = remember(subjectNotes) {
                        subjectNotes.map { it.chapterName }.distinct().size
                    }
                    val vviCount = remember(subjectNotes) {
                        subjectNotes.count { it.importanceLevel == TopicImportance.VVI }
                    }
                    val bookmarkedCount = remember(subjectNotes) {
                        subjectNotes.count { it.isBookmarked }
                    }
                    SubjectOverviewCard(
                        subject = subject,
                        chapterCount = distinctChapterCount,
                        topicCount = subjectNotes.size,
                        vviCount = vviCount,
                        bookmarkedCount = bookmarkedCount,
                        onClick = { onOpenSubject(subject) },
                        onSmartLearningClick = { onOpenSmartLearningForSubject(subject) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectOverviewCard(
    subject: WbbseSubject,
    chapterCount: Int,
    topicCount: Int,
    vviCount: Int,
    bookmarkedCount: Int,
    onClick: () -> Unit,
    onSmartLearningClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(subject.accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = subject.icon,
                        contentDescription = subject.englishName,
                        tint = subject.accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = subject.bengaliName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = subject.accentColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = subject.shortCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = subject.accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = subject.descriptionBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = buildString {
                            append("${chapterCount}টি অধ্যায় • ${topicCount}টি টপিক • ${vviCount}টি VVI")
                            if (bookmarkedCount > 0) append(" • $bookmarkedCount বুকমার্ক")
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = subject.accentColor
                    )
                }
            }

            // Direct Smart Learning entry inside EVERY WBBSE Subject Card
            FilledTonalButton(
                onClick = onSmartLearningClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = subject.accentColor.copy(alpha = 0.12f),
                    contentColor = subject.accentColor
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("subject_smart_learning_${subject.id}")
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${subject.bengaliName} — স্মার্ট লার্নিং (সহজ বাংলায় পড়া বোঝো)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SubjectDetailView(
    subject: WbbseSubject,
    allSubjectNotes: List<MasterDataEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onAskTutorFromNote: (MasterDataEntity) -> Unit,
    onOpenSmartLearningForSubject: () -> Unit,
    onOpenSmartLearningForNote: (MasterDataEntity) -> Unit,
    onToggleBookmark: (MasterDataEntity) -> Unit,
    onDeleteCustomNote: (Long) -> Unit,
    onOpenAddDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var vviOnly by remember(subject.id) { mutableStateOf(false) }

    val searchFilteredNotes = remember(allSubjectNotes, searchQuery) {
        if (searchQuery.isBlank()) allSubjectNotes
        else {
            val q = searchQuery.trim().lowercase()
            allSubjectNotes.filter { note ->
                note.chapterName.lowercase().contains(q) ||
                    note.topicTitle.lowercase().contains(q) ||
                    note.content.lowercase().contains(q) ||
                    note.keywords.lowercase().contains(q)
            }
        }
    }

    val vviCount = remember(searchFilteredNotes) {
        searchFilteredNotes.count { it.importanceLevel == TopicImportance.VVI }
    }
    val mediumCount = remember(searchFilteredNotes) {
        searchFilteredNotes.count { it.importanceLevel == TopicImportance.MEDIUM }
    }
    val lowCount = remember(searchFilteredNotes) {
        searchFilteredNotes.count { it.importanceLevel == TopicImportance.LOW }
    }

    val displayedNotes = remember(searchFilteredNotes, vviOnly) {
        if (vviOnly) searchFilteredNotes.filter { it.importanceLevel == TopicImportance.VVI }
        else searchFilteredNotes
    }

    val chapterGroups = remember(displayedNotes) {
        displayedNotes
            .groupBy { it.chapterName }
            .map { (chapterName, items) ->
                SyllabusChapterGroup(
                    chapterOrder = items.minOfOrNull { it.chapterOrder } ?: 0,
                    chapterName = chapterName,
                    topics = items
                )
            }
            .sortedWith(compareBy<SyllabusChapterGroup> { it.chapterOrder }.thenBy { it.chapterName })
    }

    val totalDistinctChapters = remember(allSubjectNotes) {
        allSubjectNotes.map { it.chapterName }.distinct().size
    }

    var expandedChapters by remember(subject.id) {
        mutableStateOf(
            chapterGroups.firstOrNull()?.chapterName?.let { setOf(it) } ?: emptySet()
        )
    }

    val allExpanded = chapterGroups.isNotEmpty() && chapterGroups.all { it.chapterName in expandedChapters }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("subject_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.subjects_back_cd)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${subject.bengaliName} (${subject.englishName})",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "মোট ${totalDistinctChapters}টি অধ্যায় • ${allSubjectNotes.size}টি সিলেবাস টপিক",
                            style = MaterialTheme.typography.bodySmall,
                            color = subject.accentColor,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("subject_chapters_list"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dedicated Smart Learning Banner inside Subject Detail
                item(key = "subject_detail_smart_learning_banner") {
                    Card(
                        onClick = onOpenSmartLearningForSubject,
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = subject.accentColor.copy(alpha = 0.12f)),
                        border = BorderStroke(1.2.dp, subject.accentColor.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_detail_smart_learning_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = subject.accentColor,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.School, contentDescription = null, tint = Color.White)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${subject.bengaliName} — স্মার্ট লার্নিং AI টিউটর",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = subject.accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "যেকোনো অধ্যায় সহজ বাংলায় গল্পের মতো বোঝো, বইয়ের ছবি দাও বা ২ডি ডায়াগ্রাম তৈরি করো।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item(key = "subject_search_field") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("${subject.bengaliName} অধ্যায় বা টপিক খুঁজো…") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_detail_search_field")
                    )
                }

                item(key = "subject_importance_filter_bar") {
                    TopicImportanceFilterBar(
                        vviOnly = vviOnly,
                        totalTopicCount = searchFilteredNotes.size,
                        vviTopicCount = vviCount,
                        mediumTopicCount = mediumCount,
                        lowTopicCount = lowCount,
                        allExpanded = allExpanded,
                        accentColor = subject.accentColor,
                        onSelectVviOnly = { vviOnly = it },
                        onToggleExpandAll = {
                            expandedChapters = if (allExpanded) {
                                emptySet()
                            } else {
                                chapterGroups.map { it.chapterName }.toSet()
                            }
                        }
                    )
                }

                if (chapterGroups.isEmpty()) {
                    item(key = "empty_syllabus_results") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = "এই ফিল্টারে কোনো অধ্যায় বা টপিক পাওয়া যায়নি। ‘সব টপিক দেখুন’ বেছে নাও অথবা সার্চ ক্লিয়ার করো।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = chapterGroups,
                        key = { _, group -> "${group.chapterOrder}_${group.chapterName}" }
                    ) { index, group ->
                        val isExpanded = searchQuery.isNotBlank() || group.chapterName in expandedChapters
                        ChapterAccordionCard(
                            chapterIndex = index + 1,
                            group = group,
                            isExpanded = isExpanded,
                            accentColor = subject.accentColor,
                            onToggleExpand = {
                                expandedChapters = if (group.chapterName in expandedChapters) {
                                    expandedChapters - group.chapterName
                                } else {
                                    expandedChapters + group.chapterName
                                }
                            },
                            onAskTutorFromNote = onAskTutorFromNote,
                            onOpenSmartLearningForNote = onOpenSmartLearningForNote,
                            onToggleBookmark = onToggleBookmark,
                            onDeleteCustomNote = onDeleteCustomNote
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onOpenAddDialog,
            containerColor = subject.accentColor,
            contentColor = Color.White,
            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.subjects_add_note)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_custom_note_fab")
        )
    }
}
