package com.example.ui.master

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.master.PocketBookAndConfusionBank
import com.example.model.ConfusionCleanerUiState
import com.example.model.PocketBookCategory
import com.example.model.PocketBookUiState
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent

/**
 * Feature #5: "পকেট বুক (Pocket Book)" — 100% Offline Vault of Math Formulas/Theorems,
 * Physics Reactions/Formulas, Life Science Key Facts, History Dates/Treaties/Leaders,
 * and Bengali/English Author-Source Lists. Includes 1-tap PDF Export on every card AND
 * direct "আমার সেভ করা PDF" Library launcher!
 */
@Composable
fun PocketBookScreen(
    state: PocketBookUiState,
    savedPdfCount: Int,
    onSelectCategory: (PocketBookCategory) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenSavedPdfLibrary: () -> Unit,
    onExportCardPdf: (String, String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val filtered = remember(state.selectedCategory, state.searchQuery) {
        val base = if (state.searchQuery.isBlank()) {
            PocketBookAndConfusionBank.getPocketBookByCategory(state.selectedCategory)
        } else {
            val q = state.searchQuery.trim().lowercase()
            PocketBookAndConfusionBank.pocketBookEntries.filter {
                it.title.lowercase().contains(q) ||
                    it.formulaOrFact.lowercase().contains(q) ||
                    it.examTip.lowercase().contains(q)
            }
        }
        base
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
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
                            .testTag("pocket_book_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "পকেট বুক (১০০% অফলাইন ভল্ট)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "সূত্র, বিক্রিয়া, সাল-তারিখ ও লেখক-উৎস এক জায়গায়",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        onClick = onOpenSavedPdfLibrary,
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFFC62828).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("pocket_book_saved_pdfs_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                            Text(
                                text = "আমার PDF ($savedPdfCount)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("সূত্র, সাল, বিক্রিয়া বা লেখকের নাম খুঁজো…") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PocketBookCategory.entries, key = { it.name }) { cat ->
                            val selected = cat == state.selectedCategory && state.searchQuery.isBlank()
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onSearchChange("")
                                    onSelectCategory(cat)
                                },
                                label = { Text(cat.titleBn) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OnlineCobalt,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                item {
                    FilledTonalButton(
                        onClick = {
                            val combined = filtered.joinToString("\n\n────────────────────\n\n") { e ->
                                "■ ${e.title}\n${e.formulaOrFact}\n💡 পরীক্ষার টিপ: ${e.examTip}"
                            }
                            onExportCardPdf(
                                "পকেট_বুক_${state.selectedCategory.titleBn}",
                                "পকেট বুক — ${state.selectedCategory.titleBn}",
                                combined
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("এই সম্পূর্ণ বিভাগের PDF তৈরি করুন", fontWeight = FontWeight.Bold)
                    }
                }

                items(filtered, key = { it.id }) { entry ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, entry.subject.accentColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MenuBook,
                                        contentDescription = null,
                                        tint = entry.subject.accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        onExportCardPdf(
                                            "পকেট_বুক_${entry.title}",
                                            entry.title,
                                            "${entry.formulaOrFact}\n\n💡 পরীক্ষার টিপ:\n${entry.examTip}"
                                        )
                                    },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PictureAsPdf,
                                        contentDescription = "Save Card as PDF",
                                        tint = Color(0xFFC62828),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Surface(
                                color = entry.subject.accentColor.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = entry.formulaOrFact,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = MonoFontFamily),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    tint = TerracottaAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = entry.examTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Feature #8: "কনফিউশন ক্লিনার (Confusion Cleaner)"
 * Side-by-side comparison tables and Bengali memory tricks for confusing WBBSE pairs inside every subject.
 */
@Composable
fun ConfusionCleanerScreen(
    state: ConfusionCleanerUiState,
    onExportPdf: (String, String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val pairs = remember(state.subject) {
        PocketBookAndConfusionBank.getConfusionsForSubject(state.subject)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
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
                            .testTag("confusion_cleaner_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "কনফিউশন ক্লিনার — ${state.subject.bengaliName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "পাশাপাশি পার্থক্য ছক ও চটজলদি মনে রাখার মেমোরি ট্রিক",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(pairs, key = { it.id }) { pair ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.2.dp, state.subject.accentColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.CompareArrows, contentDescription = null, tint = state.subject.accentColor)
                                    Text(
                                        text = "${pair.leftTitle}  vs  ${pair.rightTitle}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val body = buildString {
                                            appendLine("■ ${pair.leftTitle} বনাম ${pair.rightTitle}")
                                            appendLine()
                                            pair.rows.forEach { r ->
                                                appendLine("🔹 ${r.basis}:")
                                                appendLine("   • ${pair.leftTitle}: ${r.leftValue}")
                                                appendLine("   • ${pair.rightTitle}: ${r.rightValue}")
                                                appendLine()
                                            }
                                            appendLine("💡 ${pair.memoryTrickBn}")
                                        }
                                        onExportPdf(
                                            "পার্থক্য_${pair.leftTitle}_${pair.rightTitle}",
                                            "${pair.leftTitle} বনাম ${pair.rightTitle}",
                                            body
                                        )
                                    }
                                ) {
                                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = "PDF", tint = Color(0xFFC62828))
                                }
                            }

                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(state.subject.accentColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "বিষয়",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.75f)
                                )
                                Text(
                                    text = pair.leftTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnlineCobalt,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = pair.rightTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaAccent,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            pair.rows.forEachIndexed { idx, row ->
                                if (idx > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = row.basis,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(0.75f)
                                    )
                                    Text(
                                        text = row.leftValue,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = row.rightValue,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Surface(
                                color = SageOffline.copy(alpha = 0.13f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🧠 ${pair.memoryTrickBn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SageOffline,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
