package com.example.ui.master

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MathSolverModule
import com.example.model.MathSolverUiState
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Feature #19: "স্মার্ট ম্যাথ ও পরিমিতি সলভার (100% Offline Step-by-Step Solver + Visualizer)"
 * Solves Arithmetic (Simple/Compound Interest), Quadratic (Sridhar Acharya), and
 * Mensuration (Sphere, Cylinder, Cone) with exact CGS/SI units, live Canvas shape drawing, & PDF export.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathMensurationSolverScreen(
    state: MathSolverUiState,
    onSelectModule: (MathSolverModule) -> Unit,
    onExportPdf: (suggestedName: String, title: String, subtitle: String, body: String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    // Arithmetic inputs
    var principalStr by remember { mutableStateOf("5000") }
    var rateStr by remember { mutableStateOf("8") }
    var timeStr by remember { mutableStateOf("2") }
    var isCompound by remember { mutableStateOf(true) }
    var halfYearly by remember { mutableStateOf(false) }

    // Quadratic inputs
    var aStr by remember { mutableStateOf("1") }
    var bStr by remember { mutableStateOf("-5") }
    var cStr by remember { mutableStateOf("6") }

    // Mensuration inputs
    var radiusStr by remember { mutableStateOf("7") }
    var heightStr by remember { mutableStateOf("24") }
    var unitLabel by remember { mutableStateOf("সেমি") }

    val stepByStepSolution = remember(
        state.module, principalStr, rateStr, timeStr, isCompound, halfYearly,
        aStr, bStr, cStr, radiusStr, heightStr, unitLabel
    ) {
        when (state.module) {
            MathSolverModule.ARITHMETIC_INTEREST -> solveInterest(
                principalStr.toDoubleOrNull(),
                rateStr.toDoubleOrNull(),
                timeStr.toDoubleOrNull(),
                isCompound,
                halfYearly
            )
            MathSolverModule.QUADRATIC_SRIDHAR -> solveQuadratic(
                aStr.toDoubleOrNull(),
                bStr.toDoubleOrNull(),
                cStr.toDoubleOrNull()
            )
            MathSolverModule.SPHERE -> solveSphere(radiusStr.toDoubleOrNull(), unitLabel)
            MathSolverModule.CYLINDER -> solveCylinder(radiusStr.toDoubleOrNull(), heightStr.toDoubleOrNull(), unitLabel)
            MathSolverModule.CONE -> solveCone(radiusStr.toDoubleOrNull(), heightStr.toDoubleOrNull(), unitLabel)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "স্মার্ট ম্যাথ ও পরিমিতি সলভার",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "১০০% অফলাইন • ধাপে ধাপে WBBSE বোর্ড সমাধান ও চিত্র",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("math_solver_back")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "ফিরে যান")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Module chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MathSolverModule.entries.forEach { mod ->
                        FilterChip(
                            selected = state.module == mod,
                            onClick = { onSelectModule(mod) },
                            label = { Text(mod.titleBn, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            // Inputs Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (state.module) {
                            MathSolverModule.ARITHMETIC_INTEREST -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = !isCompound,
                                        onClick = { isCompound = false },
                                        label = { Text("সরল সুদ") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = isCompound && !halfYearly,
                                        onClick = { isCompound = true; halfYearly = false },
                                        label = { Text("চক্রবৃদ্ধি (বার্ষিক)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = isCompound && halfYearly,
                                        onClick = { isCompound = true; halfYearly = true },
                                        label = { Text("ষাণ্মাসিক") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    NumericField("আসল (P টাকা)", principalStr, { principalStr = it }, Modifier.weight(1f))
                                    NumericField("হার (r %)", rateStr, { rateStr = it }, Modifier.weight(1f))
                                    NumericField("সময় (t বছর)", timeStr, { timeStr = it }, Modifier.weight(1f))
                                }
                            }

                            MathSolverModule.QUADRATIC_SRIDHAR -> {
                                Text(
                                    text = "সমীকরণ: ax² + bx + c = 0 (a ≠ 0)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    NumericField("a", aStr, { aStr = it }, Modifier.weight(1f))
                                    NumericField("b", bStr, { bStr = it }, Modifier.weight(1f))
                                    NumericField("c", cStr, { cStr = it }, Modifier.weight(1f))
                                }
                            }

                            MathSolverModule.SPHERE, MathSolverModule.CYLINDER, MathSolverModule.CONE -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("সেমি", "মিটার").forEach { u ->
                                        FilterChip(
                                            selected = unitLabel == u,
                                            onClick = { unitLabel = u },
                                            label = { Text("একক: $u") }
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    NumericField("ব্যাসার্ধ (r $unitLabel)", radiusStr, { radiusStr = it }, Modifier.weight(1f))
                                    if (state.module != MathSolverModule.SPHERE) {
                                        NumericField("উচ্চতা (h $unitLabel)", heightStr, { heightStr = it }, Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Live 3D-Style Geometric Diagram for Mensuration Shapes
            if (state.module == MathSolverModule.SPHERE ||
                state.module == MathSolverModule.CYLINDER ||
                state.module == MathSolverModule.CONE
            ) {
                item {
                    val rVal = radiusStr.toDoubleOrNull() ?: 0.0
                    val hVal = heightStr.toDoubleOrNull() ?: 0.0
                    val lVal = if (rVal > 0 && hVal > 0) sqrt(rVal * rVal + hVal * hVal) else 0.0
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFDFB))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(175.dp)
                            ) {
                                when (state.module) {
                                    MathSolverModule.SPHERE -> drawSphereShape()
                                    MathSolverModule.CYLINDER -> drawCylinderShape()
                                    MathSolverModule.CONE -> drawConeShape()
                                    else -> Unit
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                val dimSummary = when (state.module) {
                                    MathSolverModule.SPHERE -> "ব্যাসার্ধ r = ${fmt(rVal)} $unitLabel\nব্যাস d = ${fmt(2 * rVal)} $unitLabel"
                                    MathSolverModule.CYLINDER -> "ব্যাসার্ধ r = ${fmt(rVal)} $unitLabel\nউচ্চতা h = ${fmt(hVal)} $unitLabel"
                                    MathSolverModule.CONE -> "ব্যাসার্ধ r = ${fmt(rVal)} $unitLabel\nউচ্চতা h = ${fmt(hVal)} $unitLabel\nতির্যক উচ্চতা l = ${fmt(lVal)} $unitLabel"
                                    else -> ""
                                }
                                Text(
                                    text = dimSummary,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Step-by-Step Solution Output + PDF Export
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "📝 ধাপে ধাপে বোর্ড সমাধান (${state.module.titleBn})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stepByStepSolution,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                        )
                        Button(
                            onClick = {
                                onExportPdf(
                                    "গণিত_সমাধান_${state.module.titleBn}",
                                    "গণিত ধাপে ধাপে সমাধান: ${state.module.titleBn}",
                                    "WBBSE Class 10 • ১০০% অফলাইন সলভার",
                                    stepByStepSolution
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("math_solver_pdf_btn")
                        ) {
                            Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("সমাধান PDF তৈরি করুন")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumericField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, maxLines = 1) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

private fun fmt(v: Double): String = "%.2f".format(v).removeSuffix(".00")

private fun solveInterest(p: Double?, r: Double?, t: Double?, compound: Boolean, halfYearly: Boolean): String {
    if (p == null || r == null || t == null || p <= 0 || r <= 0 || t <= 0) {
        return "⚠️ অনুগ্রহ করে আসল (P), সুদের হার (r) ও সময় (t)-এর ধনাত্মক মান দাও।"
    }
    return if (!compound) {
        val si = (p * r * t) / 100.0
        val amount = p + si
        """
        ১. প্রদত্ত তথ্য:
           • আসল (P) = ${fmt(p)} টাকা
           • বার্ষিক সরল সুদের হার (r) = ${fmt(r)}%
           • সময় (t) = ${fmt(t)} বছর

        ২. সূত্র:
           • মোট সুদ (I) = (P × r × t) / 100
           • সুদ-আসল (A) = P + I

        ৩. গণনা:
           • I = (${fmt(p)} × ${fmt(r)} × ${fmt(t)}) / 100 = ${fmt(si)} টাকা
           • A = ${fmt(p)} + ${fmt(si)} = ${fmt(amount)} টাকা

        ৪. নির্ণেয় উত্তর:
           ✅ মোট সরল সুদ = ${fmt(si)} টাকা এবং সুদ-আসল = ${fmt(amount)} টাকা।
        """.trimIndent()
    } else {
        val factor = if (halfYearly) 1.0 + (r / 200.0) else 1.0 + (r / 100.0)
        val power = if (halfYearly) 2.0 * t else t
        val amount = p * factor.pow(power)
        val ci = amount - p
        val formulaStr = if (halfYearly) "A = P × (1 + r/200)^(2t)" else "A = P × (1 + r/100)^t"
        """
        ১. প্রদত্ত তথ্য:
           • আসল (P) = ${fmt(p)} টাকা
           • বার্ষিক চক্রবৃদ্ধি সুদের হার (r) = ${fmt(r)}% (${if (halfYearly) "৬ মাস অন্তর দেয়" else "বার্ষিক"})
           • সময় (t) = ${fmt(t)} বছর

        ২. সূত্র:
           • সমূল চক্রবৃদ্ধি: $formulaStr
           • চক্রবৃদ্ধি সুদ = A - P

        ৩. গণনা:
           • A = ${fmt(p)} × (${fmt(factor)})^${fmt(power)} = ${fmt(amount)} টাকা
           • চক্রবৃদ্ধি সুদ = ${fmt(amount)} - ${fmt(p)} = ${fmt(ci)} টাকা

        ৪. নির্ণেয় উত্তর:
           ✅ সমূল চক্রবৃদ্ধি = ${fmt(amount)} টাকা এবং চক্রবৃদ্ধি সুদ = ${fmt(ci)} টাকা।
        """.trimIndent()
    }
}

private fun solveQuadratic(a: Double?, b: Double?, c: Double?): String {
    if (a == null || b == null || c == null || a == 0.0) {
        return "⚠️ দ্বিঘাত সমীকরণের জন্য a ≠ 0 এবং a, b, c-এর সঠিক বাস্তব মান দাও।"
    }
    val d = b * b - 4.0 * a * c
    val rootSteps = when {
        d > 0 -> {
            val sq = sqrt(d)
            val x1 = (-b + sq) / (2 * a)
            val x2 = (-b - sq) / (2 * a)
            "যেহেতু নিরূপক D > 0, তাই বীজদ্বয় বাস্তব ও অসমান।\n• x₁ = (${fmt(-b)} + ${fmt(sq)}) / ${fmt(2 * a)} = ${fmt(x1)}\n• x₂ = (${fmt(-b)} - ${fmt(sq)}) / ${fmt(2 * a)} = ${fmt(x2)}"
        }
        d == 0.0 -> {
            val x = -b / (2 * a)
            "যেহেতু নিরূপক D = 0, তাই বীজদ্বয় বাস্তব ও সমান।\n• x₁ = x₂ = ${fmt(x)}"
        }
        else -> "যেহেতু নিরূপক D < 0 (${fmt(d)}), তাই এই সমীকরণের কোনো বাস্তব বীজ নেই।"
    }
    return """
        ১. প্রদত্ত দ্বিঘাত সমীকরণ:
           (${fmt(a)})x² + (${fmt(b)})x + (${fmt(c)}) = 0
           যেখানে a = ${fmt(a)}, b = ${fmt(b)}, c = ${fmt(c)}

        ২. শ্রীধর আচার্যের সূত্র ও নিরূপক:
           • নিরূপক (D) = b² - 4ac
           • x = [ -b ± √(b² - 4ac) ] / 2a

        ৩. গণনা:
           • D = (${fmt(b)})² - 4 × (${fmt(a)}) × (${fmt(c)}) = ${fmt(d)}
           $rootSteps

        ৪. নির্ণেয় উত্তর:
           ✅ বীজদ্বয়ের প্রকৃতি ও মান উপরে দেখানো হলো।
    """.trimIndent()
}

private fun solveSphere(r: Double?, unit: String): String {
    if (r == null || r <= 0) return "⚠️ গোলকের ব্যাসার্ধ (r)-এর ধনাত্মক মান দাও।"
    val csa = 4.0 * PI * r * r
    val vol = (4.0 / 3.0) * PI * r * r * r
    val hemiTsa = 3.0 * PI * r * r
    return """
        ১. প্রদত্ত তথ্য:
           • গোলকের ব্যাসার্ধ (r) = ${fmt(r)} $unit

        ২. সূত্র (π ≈ 22/7 ≈ 3.1416):
           • সমগ্রতলের ক্ষেত্রফল = 4πr²
           • আয়তন (ঘনফল) = (4/3)πr³
           • নিরেট অর্ধগোলকের সমগ্রতলের ক্ষেত্রফল = 3πr²

        ৩. গণনা:
           • সমগ্রতলের ক্ষেত্রফল = 4 × π × (${fmt(r)})² = ${fmt(csa)} বর্গ $unit
           • আয়তন = (4/3) × π × (${fmt(r)})³ = ${fmt(vol)} ঘন $unit
           • অর্ধগোলকের সমগ্রতল = 3 × π × (${fmt(r)})² = ${fmt(hemiTsa)} বর্গ $unit

        ৪. নির্ণেয় উত্তর (এককসহ):
           ✅ ক্ষেত্রফল = ${fmt(csa)} বর্গ $unit এবং আয়তন = ${fmt(vol)} ঘন $unit।
    """.trimIndent()
}

private fun solveCylinder(r: Double?, h: Double?, unit: String): String {
    if (r == null || h == null || r <= 0 || h <= 0) return "⚠️ চোঙের ব্যাসার্ধ (r) ও উচ্চতা (h)-এর ধনাত্মক মান দাও।"
    val csa = 2.0 * PI * r * h
    val tsa = 2.0 * PI * r * (r + h)
    val vol = PI * r * r * h
    return """
        ১. প্রদত্ত তথ্য:
           • ভূমির ব্যাসার্ধ (r) = ${fmt(r)} $unit
           • উচ্চতা (h) = ${fmt(h)} $unit

        ২. সূত্র:
           • বক্রতলের ক্ষেত্রফল = 2πrh
           • সমগ্রতলের ক্ষেত্রফল = 2πr(r + h)
           • আয়তন = πr²h

        ৩. গণনা:
           • বক্রতল = 2 × π × ${fmt(r)} × ${fmt(h)} = ${fmt(csa)} বর্গ $unit
           • সমগ্রতল = 2 × π × ${fmt(r)} × (${fmt(r)} + ${fmt(h)}) = ${fmt(tsa)} বর্গ $unit
           • আয়তন = π × (${fmt(r)})² × ${fmt(h)} = ${fmt(vol)} ঘন $unit

        ৪. নির্ণেয় উত্তর (এককসহ):
           ✅ বক্রতল = ${fmt(csa)} বর্গ $unit | সমগ্রতল = ${fmt(tsa)} বর্গ $unit | আয়তন = ${fmt(vol)} ঘন $unit।
    """.trimIndent()
}

private fun solveCone(r: Double?, h: Double?, unit: String): String {
    if (r == null || h == null || r <= 0 || h <= 0) return "⚠️ শঙ্কুর ব্যাসার্ধ (r) ও উচ্চতা (h)-এর ধনাত্মক মান দাও।"
    val l = sqrt(r * r + h * h)
    val csa = PI * r * l
    val tsa = PI * r * (r + l)
    val vol = (1.0 / 3.0) * PI * r * r * h
    return """
        ১. প্রদত্ত তথ্য:
           • ভূমির ব্যাসার্ধ (r) = ${fmt(r)} $unit
           • লম্ব উচ্চতা (h) = ${fmt(h)} $unit

        ২. সূত্র:
           • তির্যক উচ্চতা (l) = √(r² + h²)
           • বক্রতলের ক্ষেত্রফল = πrl
           • সমগ্রতলের ক্ষেত্রফল = πr(r + l)
           • আয়তন = (1/3)πr²h

        ৩. গণনা:
           • l = √(${fmt(r)}² + ${fmt(h)}²) = ${fmt(l)} $unit
           • বক্রতল = π × ${fmt(r)} × ${fmt(l)} = ${fmt(csa)} বর্গ $unit
           • সমগ্রতল = π × ${fmt(r)} × (${fmt(r)} + ${fmt(l)}) = ${fmt(tsa)} বর্গ $unit
           • আয়তন = (1/3) × π × (${fmt(r)})² × ${fmt(h)} = ${fmt(vol)} ঘন $unit

        ৪. নির্ণেয় উত্তর (এককসহ):
           ✅ তির্যক উচ্চতা l = ${fmt(l)} $unit | সমগ্রতল = ${fmt(tsa)} বর্গ $unit | আয়তন = ${fmt(vol)} ঘন $unit।
    """.trimIndent()
}

private fun DrawScope.drawSphereShape() {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val rad = (size.minDimension * 0.40f)
    drawCircle(Color(0xFFE3F2FD), radius = rad, center = Offset(cx, cy))
    drawCircle(Color(0xFF1565C0), radius = rad, center = Offset(cx, cy), style = Stroke(4f))
    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
    drawOval(
        color = Color(0xFF1976D2),
        topLeft = Offset(cx - rad, cy - rad * 0.28f),
        size = Size(rad * 2f, rad * 0.56f),
        style = Stroke(2.5f, pathEffect = dash)
    )
    drawLine(Color(0xFFD32F2F), Offset(cx, cy), Offset(cx + rad, cy), strokeWidth = 4f)
    drawCircle(Color(0xFFD32F2F), radius = 5f, center = Offset(cx, cy))
}

private fun DrawScope.drawCylinderShape() {
    val cx = size.width / 2f
    val topY = size.height * 0.20f
    val botY = size.height * 0.80f
    val rx = size.width * 0.19f
    val ry = 18.dp.toPx()
    drawRect(
        color = Color(0xFFE8F5E9),
        topLeft = Offset(cx - rx, topY),
        size = Size(rx * 2f, botY - topY)
    )
    drawLine(Color(0xFF2E7D32), Offset(cx - rx, topY), Offset(cx - rx, botY), strokeWidth = 4f)
    drawLine(Color(0xFF2E7D32), Offset(cx + rx, topY), Offset(cx + rx, botY), strokeWidth = 4f)
    drawOval(Color(0xFFC8E6C9), topLeft = Offset(cx - rx, topY - ry / 2f), size = Size(rx * 2f, ry))
    drawOval(Color(0xFF2E7D32), topLeft = Offset(cx - rx, topY - ry / 2f), size = Size(rx * 2f, ry), style = Stroke(3.5f))
    drawOval(Color(0xFF2E7D32), topLeft = Offset(cx - rx, botY - ry / 2f), size = Size(rx * 2f, ry), style = Stroke(3.5f))
    // Radius r & Height h markers
    drawLine(Color(0xFFD32F2F), Offset(cx, topY), Offset(cx + rx, topY), strokeWidth = 3.5f)
    val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
    drawLine(Color(0xFF1565C0), Offset(cx, topY), Offset(cx, botY), strokeWidth = 3f, pathEffect = dash)
}

private fun DrawScope.drawConeShape() {
    val cx = size.width / 2f
    val apexY = size.height * 0.14f
    val baseY = size.height * 0.80f
    val rx = size.width * 0.22f
    val ry = 18.dp.toPx()
    // Slant edges (l)
    drawLine(Color(0xFF6A1B9A), Offset(cx, apexY), Offset(cx - rx, baseY), strokeWidth = 4f)
    drawLine(Color(0xFF6A1B9A), Offset(cx, apexY), Offset(cx + rx, baseY), strokeWidth = 4f)
    drawOval(Color(0xFFF3E5F5), topLeft = Offset(cx - rx, baseY - ry / 2f), size = Size(rx * 2f, ry))
    drawOval(Color(0xFF6A1B9A), topLeft = Offset(cx - rx, baseY - ry / 2f), size = Size(rx * 2f, ry), style = Stroke(3.5f))
    // Height (h) dashed & Radius (r)
    val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
    drawLine(Color(0xFF1565C0), Offset(cx, apexY), Offset(cx, baseY), strokeWidth = 3f, pathEffect = dash)
    drawLine(Color(0xFFD32F2F), Offset(cx, baseY), Offset(cx + rx, baseY), strokeWidth = 3.5f)
}
