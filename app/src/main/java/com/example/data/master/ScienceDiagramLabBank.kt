package com.example.data.master

import com.example.model.DiagramCalloutPoint
import com.example.model.ScienceDiagramSpec
import com.example.model.ScienceDiagramType
import com.example.model.WbbseSubject

/**
 * Feature #15: "ছবি চিহ্নিতকরণ প্র্যাকটিস (Interactive Science Diagram Labelling)"
 * Authentic WBBSE Class 10 Board-Standard Diagrams for Life Science & Physical Science
 * with calibrated Canvas anchor points and blank callout boxes for 100% offline practice.
 */
object ScienceDiagramLabBank {

    val allDiagrams: List<ScienceDiagramSpec> = listOf(
        // ==================== LIFE SCIENCE (4 BOARD-STANDARD DIAGRAMS) ====================
        ScienceDiagramSpec(
            id = "diag_neuron",
            type = ScienceDiagramType.NEURON,
            subject = WbbseSubject.LIFE_SCIENCE,
            titleBn = "আদর্শ নিউরন (স্নায়ুকোষ)",
            titleEn = "Structure of a Typical Neuron",
            boardMarksNote = "WBBSE VVI • ৫ নম্বর (চিত্র ৩ + চিহ্নিতকরণ ২)",
            callouts = listOf(
                DiagramCalloutPoint(
                    id = "n1", numberLabel = 1,
                    labelBn = "ডেনড্রন / ডেনড্রাইট", labelEn = "Dendron / Dendrite",
                    anchorX = 0.17f, anchorY = 0.25f, boxX = 0.16f, boxY = 0.10f,
                    hintBn = "কোষদেহ থেকে নির্গত শাখা-প্রশাখা যা স্নায়ুস্পন্দন গ্রহণ করে।"
                ),
                DiagramCalloutPoint(
                    id = "n2", numberLabel = 2,
                    labelBn = "কোষদেহ ও নিউক্লিয়াস", labelEn = "Cell Body & Nucleus",
                    anchorX = 0.24f, anchorY = 0.48f, boxX = 0.20f, boxY = 0.82f,
                    hintBn = "তারকাকৃতি কেন্দ্র অংশ যাতে নিউক্লিয়াস ও নিজল দানা থাকে।"
                ),
                DiagramCalloutPoint(
                    id = "n3", numberLabel = 3,
                    labelBn = "মায়েলিন আবরণী", labelEn = "Myelin Sheath",
                    anchorX = 0.48f, anchorY = 0.44f, boxX = 0.48f, boxY = 0.16f,
                    hintBn = "অ্যাক্সনের চারপাশের স্নেহপদার্থ নির্মিত অন্তরক আবরণ।"
                ),
                DiagramCalloutPoint(
                    id = "n4", numberLabel = 4,
                    labelBn = "র‍্যানভিয়ারের পর্ব", labelEn = "Node of Ranvier",
                    anchorX = 0.57f, anchorY = 0.50f, boxX = 0.56f, boxY = 0.82f,
                    hintBn = "দুটি মায়েলিন আবরণীর মাঝের খাঁজ বা ফাঁকা স্থান।"
                ),
                DiagramCalloutPoint(
                    id = "n5", numberLabel = 5,
                    labelBn = "প্রান্তবুরুশ (অ্যাক্সন প্রান্ত)", labelEn = "Axon Terminal / End Brush",
                    anchorX = 0.84f, anchorY = 0.48f, boxX = 0.82f, boxY = 0.18f,
                    hintBn = "অ্যাক্সনের শেষ প্রান্তের সূক্ষ্ম শাখা যা সাইন্যাপস গঠন করে।"
                )
            )
        ),
        ScienceDiagramSpec(
            id = "diag_reflex_arc",
            type = ScienceDiagramType.REFLEX_ARC,
            subject = WbbseSubject.LIFE_SCIENCE,
            titleBn = "সরল প্রতিবর্ত চাপ (Reflex Arc)",
            titleEn = "Simple Reflex Arc Pathway",
            boardMarksNote = "WBBSE VVI • ৫ নম্বর (চিত্র ৩ + চিহ্নিতকরণ ২)",
            callouts = listOf(
                DiagramCalloutPoint(
                    id = "r1", numberLabel = 1,
                    labelBn = "গ্রাহক (ত্বক)", labelEn = "Receptor (Skin)",
                    anchorX = 0.14f, anchorY = 0.34f, boxX = 0.15f, boxY = 0.12f,
                    hintBn = "উদ্দীপনা (তাপ/ব্যথা) প্রথমে যেখানে গৃহীত হয়।"
                ),
                DiagramCalloutPoint(
                    id = "r2", numberLabel = 2,
                    labelBn = "সংজ্ঞাবহ নিউরন", labelEn = "Sensory Neuron",
                    anchorX = 0.38f, anchorY = 0.28f, boxX = 0.42f, boxY = 0.10f,
                    hintBn = "গ্রাহক থেকে স্নায়ুস্পন্দন সুষুম্নাকাণ্ডে নিয়ে যায়।"
                ),
                DiagramCalloutPoint(
                    id = "r3", numberLabel = 3,
                    labelBn = "সুষুম্নাকাণ্ড ও সহযোগী নিউরন", labelEn = "Spinal Cord & Interneuron",
                    anchorX = 0.72f, anchorY = 0.48f, boxX = 0.78f, boxY = 0.14f,
                    hintBn = "ধূসর পদার্থে সংজ্ঞাবহ ও আজ্ঞাবহ নিউরনের সংযোগ কেন্দ্র।"
                ),
                DiagramCalloutPoint(
                    id = "r4", numberLabel = 4,
                    labelBn = "আজ্ঞাবহ নিউরন", labelEn = "Motor Neuron",
                    anchorX = 0.42f, anchorY = 0.68f, boxX = 0.46f, boxY = 0.86f,
                    hintBn = "সুষুম্নাকাণ্ড থেকে নির্দেশ কারক পেশিতে পৌঁছে দেয়।"
                ),
                DiagramCalloutPoint(
                    id = "r5", numberLabel = 5,
                    labelBn = "কারক (পেশি)", labelEn = "Effector (Muscle)",
                    anchorX = 0.16f, anchorY = 0.66f, boxX = 0.16f, boxY = 0.86f,
                    hintBn = "নির্দেশ পেয়ে হাত সরিয়ে নেওয়ার কাজটি করে।"
                )
            )
        ),
        ScienceDiagramSpec(
            id = "diag_chromosome",
            type = ScienceDiagramType.CHROMOSOME,
            subject = WbbseSubject.LIFE_SCIENCE,
            titleBn = "ইউক্যারিওটিক ক্রোমোজোম",
            titleEn = "Eukaryotic Chromosome Morphology",
            boardMarksNote = "WBBSE VVI • ৫ নম্বর (চিত্র ৩ + চিহ্নিতকরণ ২)",
            callouts = listOf(
                DiagramCalloutPoint(
                    id = "c1", numberLabel = 1,
                    labelBn = "স্যাটেলাইট", labelEn = "Satellite",
                    anchorX = 0.50f, anchorY = 0.15f, boxX = 0.22f, boxY = 0.12f,
                    hintBn = "গৌণ খাঁজের উপরের ফোলা বাল্বের মতো অংশ।"
                ),
                DiagramCalloutPoint(
                    id = "c2", numberLabel = 2,
                    labelBn = "গৌণ খাঁজ (NOR)", labelEn = "Secondary Constriction",
                    anchorX = 0.50f, anchorY = 0.25f, boxX = 0.80f, boxY = 0.20f,
                    hintBn = "নিউক্লিওলাস গঠনে সাহায্য করে।"
                ),
                DiagramCalloutPoint(
                    id = "c3", numberLabel = 3,
                    labelBn = "সেন্ট্রোমিয়ার (মুখ্য খাঁজ)", labelEn = "Centromere",
                    anchorX = 0.50f, anchorY = 0.50f, boxX = 0.20f, boxY = 0.50f,
                    hintBn = "দুটি ক্রোমাটিড যেখানে যুক্ত থাকে ও বেমতন্তু লাগে।"
                ),
                DiagramCalloutPoint(
                    id = "c4", numberLabel = 4,
                    labelBn = "ক্রোমাটিড", labelEn = "Chromatid",
                    anchorX = 0.43f, anchorY = 0.70f, boxX = 0.80f, boxY = 0.62f,
                    hintBn = "ক্রোমোজোমের লম্বালম্বি দুটি সমান বাহু।"
                ),
                DiagramCalloutPoint(
                    id = "c5", numberLabel = 5,
                    labelBn = "টেলোমিয়ার", labelEn = "Telomere",
                    anchorX = 0.50f, anchorY = 0.86f, boxX = 0.22f, boxY = 0.86f,
                    hintBn = "ক্রোমোজোমের দুই শেষ প্রান্ত যা প্রান্তীয় সংযুক্তি রোধ করে।"
                )
            )
        ),
        ScienceDiagramSpec(
            id = "diag_human_eye",
            type = ScienceDiagramType.HUMAN_EYE,
            subject = WbbseSubject.LIFE_SCIENCE,
            titleBn = "মানব চক্ষুর অক্ষিগোলক",
            titleEn = "Vertical Section of Human Eyeball",
            boardMarksNote = "WBBSE VVI • ৫ নম্বর (চিত্র ৩ + চিহ্নিতকরণ ২)",
            callouts = listOf(
                DiagramCalloutPoint(
                    id = "e1", numberLabel = 1,
                    labelBn = "কর্নিয়া", labelEn = "Cornea",
                    anchorX = 0.20f, anchorY = 0.48f, boxX = 0.16f, boxY = 0.16f,
                    hintBn = "চোখের সামনের স্বচ্ছ প্রতিসারক মাধ্যম।"
                ),
                DiagramCalloutPoint(
                    id = "e2", numberLabel = 2,
                    labelBn = "আইরিশ ও লেন্স", labelEn = "Iris & Lens",
                    anchorX = 0.34f, anchorY = 0.48f, boxX = 0.36f, boxY = 0.84f,
                    hintBn = "দ্বি-উত্তল স্বচ্ছ লেন্স যা আলোকরশ্মিকে ফোকাস করে।"
                ),
                DiagramCalloutPoint(
                    id = "e3", numberLabel = 3,
                    labelBn = "ভিট্রিয়াস হিউমর", labelEn = "Vitreous Humour",
                    anchorX = 0.54f, anchorY = 0.50f, boxX = 0.54f, boxY = 0.14f,
                    hintBn = "লেন্স ও রেটিনার মাঝের জেলির মতো তরল।"
                ),
                DiagramCalloutPoint(
                    id = "e4", numberLabel = 4,
                    labelBn = "রেটিনা ও পীতবিন্দু", labelEn = "Retina & Yellow Spot",
                    anchorX = 0.72f, anchorY = 0.44f, boxX = 0.82f, boxY = 0.20f,
                    hintBn = "রড ও কোণ কোষযুক্ত আলোক-সুবেদী পর্দা যেখানে প্রতিবিম্ব গঠিত হয়।"
                ),
                DiagramCalloutPoint(
                    id = "e5", numberLabel = 5,
                    labelBn = "অপটিক স্নায়ু", labelEn = "Optic Nerve",
                    anchorX = 0.82f, anchorY = 0.58f, boxX = 0.80f, boxY = 0.84f,
                    hintBn = "অন্ধবিন্দু দিয়ে বের হয়ে দর্শন অনুভূতি মস্তিষ্কে পাঠায়।"
                )
            )
        ),

        // ==================== PHYSICAL SCIENCE (2 BOARD-STANDARD OPTICS DIAGRAMS) ====================
        ScienceDiagramSpec(
            id = "diag_prism",
            type = ScienceDiagramType.PRISM_REFRACTION,
            subject = WbbseSubject.PHYSICAL_SCIENCE,
            titleBn = "প্রিজমের মধ্য দিয়ে আলোর প্রতিসরণ",
            titleEn = "Refraction of Light Through a Glass Prism",
            boardMarksNote = "WBBSE VVI • ৩ নম্বর (δ = i₁ + i₂ - A প্রমাণ)",
            callouts = listOf(
                DiagramCalloutPoint(
                    id = "p1", numberLabel = 1,
                    labelBn = "প্রতিসারক কোণ (A)", labelEn = "Angle of Prism (A)",
                    anchorX = 0.50f, anchorY = 0.24f, boxX = 0.50f, boxY = 0.10f,
                    hintBn = "প্রিজমের শীর্ষ কোণ (A = r₁ + r₂)।"
                ),
                DiagramCalloutPoint(
                    id = "p2", numberLabel = 2,
                    labelBn = "আপতিত রশ্মি (i₁)", labelEn = "Incident Ray (i₁)",
                    anchorX = 0.22f, anchorY = 0.58f, boxX = 0.18f, boxY = 0.84f,
                    hintBn = "বায়ু থেকে প্রিজমের প্রথম তলে আপতিত রশ্মি।"
                ),
                DiagramCalloutPoint(
                    id = "p3", numberLabel = 3,
                    labelBn = "প্রতিসৃত রশ্মি", labelEn = "Refracted Ray",
                    anchorX = 0.50f, anchorY = 0.50f, boxX = 0.50f, boxY = 0.86f,
                    hintBn = "প্রিজমের কাচের ভেতর দিয়ে ভূমির প্রায় সমান্তরালে যাওয়া রশ্মি।"
                ),
                DiagramCalloutPoint(
                    id = "p4", numberLabel = 4,
                    labelBn = "চ্যুতিকোণ (δ)", labelEn = "Angle of Deviation (δ)",
                    anchorX = 0.62f, anchorY = 0.36f, boxX = 0.82f, boxY = 0.20f,
                    hintBn = "আপতিত রশ্মির বর্ধিতাংশ ও নির্গত রশ্মির মধ্যবর্তী কোণ।"
                ),
                DiagramCalloutPoint(
                    id = "p5", numberLabel = 5,
                    labelBn = "নির্গত রশ্মি (i₂)", labelEn = "Emergent Ray (i₂)",
                    anchorX = 0.78f, anchorY = 0.62f, boxX = 0.82f, boxY = 0.84f,
                    hintBn = "দ্বিতীয় তল থেকে ভূমির দিকে বেঁকে নির্গত রশ্মি।"
                )
            )
        ),
        ScienceDiagramSpec(
            id = "diag_convex_lens",
            type = ScienceDiagramType.CONVEX_LENS,
            subject = WbbseSubject.PHYSICAL_SCIENCE,
            titleBn = "উত্তল লেন্স ও প্রতিবিম্ব গঠন",
            titleEn = "Image Formation by a Convex Lens",
            boardMarksNote = "WBBSE VVI • ৩ নম্বর (রশ্মিচিত্র ও আলোককেন্দ্র)",
            callouts = listOf(
                DiagramCalloutPoint(
                    id = "l1", numberLabel = 1,
                    labelBn = "বস্তু (2F এর বাইরে)", labelEn = "Object (Beyond 2F)",
                    anchorX = 0.16f, anchorY = 0.38f, boxX = 0.16f, boxY = 0.14f,
                    hintBn = "প্রধান অক্ষের ওপর লম্বভাবে স্থাপিত লক্ষ্যবস্তু।"
                ),
                DiagramCalloutPoint(
                    id = "l2", numberLabel = 2,
                    labelBn = "আলোককেন্দ্র (O)", labelEn = "Optical Centre (O)",
                    anchorX = 0.50f, anchorY = 0.50f, boxX = 0.50f, boxY = 0.14f,
                    hintBn = "যে বিন্দু দিয়ে গেলে রশ্মির কোনো চ্যুতি ঘটে না।"
                ),
                DiagramCalloutPoint(
                    id = "l3", numberLabel = 3,
                    labelBn = "প্রধান ফোকাস (F₂)", labelEn = "Principal Focus (F₂)",
                    anchorX = 0.66f, anchorY = 0.50f, boxX = 0.68f, boxY = 0.16f,
                    hintBn = "সমান্তরাল রশ্মি প্রতিসরণের পর যে বিন্দুতে মিলিত হয়।"
                ),
                DiagramCalloutPoint(
                    id = "l4", numberLabel = 4,
                    labelBn = "সদ ও অবশীর্ষ প্রতিবিম্ব", labelEn = "Real & Inverted Image",
                    anchorX = 0.78f, anchorY = 0.62f, boxX = 0.80f, boxY = 0.84f,
                    hintBn = "F₂ ও 2F₂ এর মাঝে গঠিত উল্টো ও সদ প্রতিবিম্ব।"
                )
            )
        )
    )

    fun getDiagramsForSubject(subject: WbbseSubject): List<ScienceDiagramSpec> =
        allDiagrams.filter { it.subject == subject }
}
