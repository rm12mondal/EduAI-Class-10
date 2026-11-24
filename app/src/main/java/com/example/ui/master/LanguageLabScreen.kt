package com.example.ui.master

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.master.EnglishUnseenPassage
import com.example.data.master.GrammarRuleAndQuiz
import com.example.data.master.LanguageMasteryBank
import com.example.model.LanguageLabMode
import com.example.model.LanguageLabUiState

/**
 * Feature #18: "ইংলিশ আনসিন ও গ্রামার + বাংলা ব্যাকরণ ল্যাব"
 * - ENGLISH_LAB: Seen Index (8 Bliss lessons), Unseen Practice Lab + Custom Unseen Solver, Grammar & Vocabulary Studio.
 * - BENGALI_GRAMMAR_LAB: বাংলা ব্যাকরণ গুরু with instant ব্যাসবাক্য/কারক analyzer + interactive grammar cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageLabScreen(
    state: LanguageLabUiState,
    onUpdateBengaliInput: (String) -> Unit,
    onRunBengaliAnalyzer: () -> Unit,
    onUpdateCustomUnseen: (String) -> Unit,
    onSolveCustomUnseen: () -> Unit,
    onExportPdf: (suggestedName: String, title: String, subtitle: String, body: String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    var englishTabIndex by remember { mutableIntStateOf(1) } // 0=Seen, 1=Unseen Lab, 2=Grammar Studio

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (state.mode == LanguageLabMode.ENGLISH_LAB)
                                "ইংলিশ সিন, আনসিন ও গ্রামার ল্যাব"
                            else
                                "বাংলা ব্যাকরণ গুরু (Grammar Lab)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.mode == LanguageLabMode.ENGLISH_LAB)
                                "Seen • Unseen T/F+SS & Vocab • Voice/Narration/Phrasal"
                            else
                                "কারক • সমাস • বাক্য • বাচ্য + ইনস্ট্যান্ট ব্যাসবাক্য বিশ্লেষক",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("language_lab_back")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "ফিরে যান")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.mode == LanguageLabMode.ENGLISH_LAB) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Seen (8)", "Unseen Lab", "Grammar").forEachIndexed { idx, label ->
                        FilterChip(
                            selected = englishTabIndex == idx,
                            onClick = { englishTabIndex = idx },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                when (englishTabIndex) {
                    0 -> EnglishSeenIndexTab()
                    1 -> EnglishUnseenLabTab(
                        customInput = state.customUnseenInput,
                        customOutput = state.customUnseenOutput,
                        isSolving = state.isSolvingUnseen,
                        onUpdateInput = onUpdateCustomUnseen,
                        onSolve = onSolveCustomUnseen,
                        onExportPdf = onExportPdf
                    )
                    else -> GrammarModulesListTab(
                        modules = LanguageMasteryBank.englishGrammarModules,
                        pdfPrefix = "English_Grammar",
                        onExportPdf = onExportPdf
                    )
                }
            }
        } else {
            BengaliGrammarLabContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                analyzerInput = state.analyzerInput,
                analyzerOutput = state.analyzerOutput,
                isAnalyzing = state.isAnalyzing,
                onUpdateInput = onUpdateBengaliInput,
                onAnalyze = onRunBengaliAnalyzer,
                onExportPdf = onExportPdf
            )
        }
    }
}

@Composable
private fun EnglishSeenIndexTab() {
    val blissLessons = remember {
        listOf(
            Triple("Lesson 1: Father's Help", "R. K. Narayan (Prose)", "Swami, Samuel sir, Father's letter, Headmaster — VVI T/F+SS ও 2-mark Q&A।"),
            Triple("Lesson 2: Fable", "Ralph Waldo Emerson (Poem)", "Mountain & Squirrel (Bun) debate — Talents differ; small vs big theme।"),
            Triple("Lesson 3: The Passing Away of Bapu", "Nayantara Sehgal (Prose)", "30 Jan 1948, Birla House, funeral walk, immersion of ashes at Allahabad।"),
            Triple("Lesson 4: My Own True Family", "Ted Hughes (Poem)", "Oakwood dream, old woman with knobby stick, planting two trees for one felled।"),
            Triple("Lesson 5: Our Runaway Kite", "Lucy Maud Montgomery (Prose)", "Big Half Moon island, Claude & Philippa, red paper kite patched with Uncle's letter।"),
            Triple("Lesson 6: Sea Fever", "John Masefield (Poem)", "Longing for lonely sea, tall ship, star to steer her by, vagrant gypsy life।"),
            Triple("Lesson 7: The Cat", "Andrew Barton Paterson (Prose)", "True nature of cat vs daytime laziness, backyard warrior at night, family attachment।"),
            Triple("Lesson 8: The Snail", "William Cowper (Poem)", "Self-contained life inside shell, shrinks at slightest touch, satisfied with own treasure।")
        )
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(blissLessons) { (title, author, focus) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(author, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(focus, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun EnglishUnseenLabTab(
    customInput: String,
    customOutput: String,
    isSolving: Boolean,
    onUpdateInput: (String) -> Unit,
    onSolve: () -> Unit,
    onExportPdf: (String, String, String, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Custom Unseen Passage Solver
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🧩 Custom Unseen Passage Solver",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "যেকোনো ইংরেজি Unseen Passage পেস্ট করো — T/F + Supporting Statement, WH-প্রশ্ন ও Vocabulary উত্তর পেয়ে যাবে!",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = onUpdateInput,
                        placeholder = { Text("Paste any English Unseen Passage here...") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unseen_custom_input")
                    )
                    Button(
                        onClick = onSolve,
                        enabled = !isSolving && customInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unseen_solve_btn")
                    ) {
                        if (isSolving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unseen সমাধান তৈরি হচ্ছে...")
                        } else {
                            Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("T/F + SS, WH-Q ও Vocab তৈরি করো")
                        }
                    }
                    if (customOutput.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(customOutput, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        onExportPdf(
                                            "English_Unseen_Solution",
                                            "English Unseen Passage Solution",
                                            "WBBSE Class 10 • T/F+SS, WH-Q & Vocabulary",
                                            "PASSAGE:\n$customInput\n\nSOLUTION:\n$customOutput"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PDF তৈরি করুন")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Built-in WBBSE Unseen Passages
        items(LanguageMasteryBank.builtInUnseenPassages, key = { it.id }) { passage ->
            BuiltInUnseenPassageCard(passage = passage, onExportPdf = onExportPdf)
        }
    }
}

@Composable
private fun BuiltInUnseenPassageCard(
    passage: EnglishUnseenPassage,
    onExportPdf: (String, String, String, String) -> Unit
) {
    var showSolutions by remember(passage.id) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📰 ${passage.title}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = passage.passageText,
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showSolutions = !showSolutions },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (showSolutions) "উত্তর লুকান" else "T/F + SS, WH-Q ও Vocab দেখো")
                }
                IconButton(
                    onClick = {
                        val body = buildString {
                            appendLine(passage.passageText)
                            appendLine("\n1. TRUE / FALSE & SUPPORTING STATEMENTS:")
                            passage.tfStatements.forEach { (st, tf, ss) ->
                                appendLine("• $st -> ${if (tf) "TRUE" else "FALSE"}\n  $ss")
                            }
                            appendLine("\n2. WH-QUESTIONS:")
                            passage.whQuestions.forEach { (q, a) ->
                                appendLine("Q: $q\nA: $a")
                            }
                            appendLine("\n3. VOCABULARY:")
                            passage.vocabularyMatches.forEach { (clue, word) ->
                                appendLine("• $clue -> $word")
                            }
                        }
                        onExportPdf("Unseen_${passage.title}", passage.title, "WBBSE English Unseen Lab", body)
                    }
                ) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = "PDF তৈরি করুন")
                }
            }

            if (showSolutions) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("১. True / False + Supporting Statement:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        passage.tfStatements.forEach { (statement, isTrue, ss) ->
                            Text(
                                text = "• $statement → [${if (isTrue) "TRUE" else "FALSE"}]\n   $ss",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text("২. WH-Questions:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        passage.whQuestions.forEach { (q, a) ->
                            Text("Q: $q\nAns: $a", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("৩. Vocabulary (Synonym from Passage):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        passage.vocabularyMatches.forEach { (clue, word) ->
                            Text("• $clue → $word", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BengaliGrammarLabContent(
    modifier: Modifier,
    analyzerInput: String,
    analyzerOutput: String,
    isAnalyzing: Boolean,
    onUpdateInput: (String) -> Unit,
    onAnalyze: () -> Unit,
    onExportPdf: (String, String, String, String) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Instant Samas / Karak Analyzer
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🔍 ইনস্ট্যান্ট ব্যাসবাক্য ও কারক বিশ্লেষক",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("দশানন", "মনমাঝি", "পঙ্কজ", "ত্রিফলা", "যথাবিধি", "মেঘ থেকে বৃষ্টি পড়ে").forEach { sample ->
                            AssistChip(
                                onClick = {
                                    onUpdateInput(sample)
                                    onAnalyze()
                                },
                                label = { Text(sample) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = analyzerInput,
                        onValueChange = onUpdateInput,
                        placeholder = { Text("যেকোনো বাংলা সমাসবদ্ধ পদ বা বাক্য লিখুন...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bengali_grammar_input")
                    )
                    Button(
                        onClick = onAnalyze,
                        enabled = !isAnalyzing && analyzerInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bengali_grammar_analyze_btn")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ব্যাকরণ বিশ্লেষণ হচ্ছে...")
                        } else {
                            Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ব্যাসবাক্য / কারক বিশ্লেষণ করো")
                        }
                    }

                    if (analyzerOutput.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(analyzerOutput, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        onExportPdf(
                                            "বাংলা_ব্যাকরণ_${analyzerInput.take(18)}",
                                            "বাংলা ব্যাকরণ বিশ্লেষণ: $analyzerInput",
                                            "WBBSE Class 10 বাংলা ব্যাকরণ গুরু",
                                            analyzerOutput
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PDF তৈরি করুন")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Built-in Bengali Grammar Cards
        items(LanguageMasteryBank.bengaliGrammarModules, key = { it.id }) { mod ->
            GrammarModuleCard(
                module = mod,
                onExportPdf = {
                    val body = buildString {
                        appendLine(mod.titleBn)
                        appendLine("\nনিয়ম:\n${mod.ruleSummary}")
                        appendLine("\nশর্টকাট ট্রিক:\n${mod.trickBn}")
                        appendLine("\nবোর্ড উদাহরণ:")
                        mod.examples.forEach { (q, a) -> appendLine("• $q → $a") }
                    }
                    onExportPdf("বাংলা_ব্যাকরণ_${mod.category}", mod.titleBn, "WBBSE Class 10 বাংলা ব্যাকরণ", body)
                }
            )
        }
    }
}

@Composable
private fun GrammarModulesListTab(
    modules: List<GrammarRuleAndQuiz>,
    pdfPrefix: String,
    onExportPdf: (String, String, String, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(modules, key = { it.id }) { mod ->
            GrammarModuleCard(
                module = mod,
                onExportPdf = {
                    val body = buildString {
                        appendLine(mod.titleBn)
                        appendLine("\nনিয়ম:\n${mod.ruleSummary}")
                        appendLine("\nশর্টকাট ট্রিক:\n${mod.trickBn}")
                        appendLine("\nবোর্ড উদাহরণ:")
                        mod.examples.forEach { (q, a) -> appendLine("• $q → $a") }
                    }
                    onExportPdf("${pdfPrefix}_${mod.category}", mod.titleBn, "WBBSE Class 10 Grammar Studio", body)
                }
            )
        }
    }
}

@Composable
private fun GrammarModuleCard(
    module: GrammarRuleAndQuiz,
    onExportPdf: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(module.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(module.titleBn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onExportPdf) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = "PDF তৈরি করুন")
                }
            }
            Text(module.ruleSummary, style = MaterialTheme.typography.bodySmall)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
            ) {
                Text(
                    text = "💡 শর্টকাট: ${module.trickBn}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(10.dp)
                )
            }
            module.examples.forEach { (q, a) ->
                Text(
                    text = "• $q  →  $a",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
