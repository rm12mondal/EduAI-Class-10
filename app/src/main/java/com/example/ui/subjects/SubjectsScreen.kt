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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.MasterDataEntity
import com.example.model.WbbseSubject
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import androidx.compose.ui.graphics.Brush

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

        val subjectNotes = remember(masterNotes, selectedSubject, searchQuery) {
            masterNotes
                .filter { it.subjectId == selectedSubject.id }
                .filter { note ->
                    if (searchQuery.isBlank()) true
                    else {
                        val q = searchQuery.trim().lowercase()
                        note.chapterName.lowercase().contains(q) ||
                            note.topicTitle.lowercase().contains(q) ||
                            note.content.lowercase().contains(q) ||
                            note.keywords.lowercase().contains(q)
                    }
                }
        }

        SubjectDetailView(
            subject = selectedSubject,
            notes = subjectNotes,
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
                        text = "${stringResource(R.string.subjects_subtitle)} • মোট ${masterNotes.size}টি অফলাইন নোট",
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
                    val count = masterNotes.count { it.subjectId == subject.id }
                    val bookmarkedCount = masterNotes.count { it.subjectId == subject.id && it.isBookmarked }
                    SubjectOverviewCard(
                        subject = subject,
                        chapterCount = count,
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
                        text = "${chapterCount}টি অধ্যায় নোট" + if (bookmarkedCount > 0) " • $bookmarkedCount বুকমার্ক" else "",
                        style = MaterialTheme.typography.labelMedium,
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
    notes: List<MasterDataEntity>,
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
                            text = subject.descriptionBn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
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

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("${subject.bengaliName} অধ্যায় খুঁজো…") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items(notes, key = { it.id }) { note ->
                    MasterNoteCard(
                        note = note,
                        accentColor = subject.accentColor,
                        subjectBadge = null,
                        onAskTutor = { onAskTutorFromNote(note) },
                        onOpenSmartLearning = { onOpenSmartLearningForNote(note) },
                        onToggleBookmark = { onToggleBookmark(note) },
                        onDeleteCustom = if (note.isUserCreated) {
                            { onDeleteCustomNote(note.id) }
                        } else {
                            null
                        }
                    )
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

@Composable
private fun MasterNoteCard(
    note: MasterDataEntity,
    accentColor: Color,
    subjectBadge: String?,
    onAskTutor: () -> Unit,
    onOpenSmartLearning: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDeleteCustom: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (subjectBadge != null) {
                            Surface(
                                color = accentColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = subjectBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = note.chapterName,
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = note.topicTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    if (onDeleteCustom != null) {
                        IconButton(onClick = onDeleteCustom) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete custom note",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (note.isBookmarked) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(R.string.subjects_bookmark_cd),
                            tint = if (note.isBookmarked) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                color = accentColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note.keyFormulaOrFact,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(16.dp)
                )
                Text(
                    text = note.examTip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenSmartLearning,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("chapter_smart_learning_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.School,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("স্মার্ট লার্নিং (সহজ বাংলায় বোঝো)")
                }

                FilledTonalButton(
                    onClick = onAskTutor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.subjects_ask_tutor))
                }
            }
        }
    }
}

@Composable
private fun AddStudyNoteDialog(
    subject: WbbseSubject,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var chapter by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var formula by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${subject.bengaliName} — নতুন অফলাইন নোট") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("অধ্যায়ের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("টপিক / শিরোনাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("মূল বিবরণ *") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formula,
                    onValueChange = { formula = it },
                    label = { Text("গুরুত্বপূর্ণ সূত্র / সাল / তথ্য") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(chapter, topic, content, formula) },
                enabled = topic.isNotBlank() && content.isNotBlank()
            ) {
                Text("সংরক্ষণ করো")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
