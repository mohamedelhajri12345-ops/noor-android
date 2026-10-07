package com.elhajri.noor.games

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import kotlin.random.Random

/**
 * "خط الزمن النبوي" — مسار السيرة النبوية والتاريخ الإسلامي عبر 20 مرحلة زمنية.
 * رتّب الأحداث التاريخية من الأقدم إلى الأحدث مع تدرّج الصعوبة وضيق الفوارق الزمنية.
 */
private data class SeerahEvent(val title: String, val yearNum: Double, val year: String, val note: String)

private val seerahEvents = listOf(
    SeerahEvent("مولد النبي ﷺ في مكة", 571.0, "٥٧١م", "وُلد عام الفيل، في بني هاشم، وتوفي أبوه عبد الله قبيل ولادته"),
    SeerahEvent("رحلة الشام وبحيرة الراهب", 583.0, "٥٨٣م", "بكى بحيرة الراهب وقال لعمّه أبي طالب: هذا سيد العالمين"),
    SeerahEvent("حلف الفضول بمكة", 590.0, "٥٩٠م", "حلف لنصرة المظلوم شهد النبي ﷺ وقال: لو دُعيت به في الإسلام لأجبت"),
    SeerahEvent("زواج النبي ﷺ من خديجة", 595.0, "٥٩٥م", "كان عمره 25 سنة وهي أول أمهات المؤمنين وأول من آمن به"),
    SeerahEvent("تحكيم النبي ﷺ في وضع الحجر الأسود", 605.0, "٦٠٥م", "وضعه في ثوب وأمر رؤوس القبائل برفعه معاً فوضعوه بمكانه"),
    SeerahEvent("بدء الوحي في غار حراء", 610.0, "٦١٠م", "نزلت عليه أول آيات سورة العلق: ﴿اقْرَأْ بِاسْمِ رَبِّكَ الَّذِي خَلَقَ﴾"),
    SeerahEvent("الدعوة الجهرية من فوق الصفا", 613.0, "٦١٣م", "نادى قريشاً: أرأيتم لو أخبرتكم أن خيلاً بالوادي أكنتم مصدقي؟"),
    SeerahEvent("الهجرة الأولى إلى الحبشة", 615.0, "٦١٥م", "قال ﷺ: إن بها ملكاً لا يُظلم عنده أحد وهي أرض صدق"),
    SeerahEvent("عام الحزن (وفاة خديجة وأبي طالب)", 619.0, "٦١٩م", "توفيت فيه أم المؤمنين خديجة وعمه أبو طالب حامي الدعوة"),
    SeerahEvent("رحلة الإسراء والمعراج", 621.0, "٦٢١م", "أُسري به من المسجد الحرام إلى المسجد الأقصى وفرضت الصلاة"),
    SeerahEvent("الهجرة النبوية إلى يثرب", 622.0, "٦٢٢م", "بداية التقويم الهجري وتأسيس مجتمع المدينة المنورة"),
    SeerahEvent("بناء المسجد النبوي الشريف", 623.0, "٦٢٣م", "أول مسجد أسس على التقوى في المدينة وكان مركز الدولة"),
    SeerahEvent("تحويل القبلة إلى الكعبة المشرفة", 623.5, "٦٢٣م", "﴿فَوَلِّ وَجْهَكَ شَطْرَ الْمَسْجِدِ الْحَرَامِ﴾ بعد 16 شهراً نحو الأقصى"),
    SeerahEvent("غزوة بدر الكبرى", 624.0, "٦٢٤م", "يوم الفرقان: انتصار 313 مسلم على ألف من مشركي قريش"),
    SeerahEvent("غزوة أحد", 625.0, "٦٢٥م", "استُشهد فيها سيد الشهداء حمزة بن عبد المطلب رضي الله عنه"),
    SeerahEvent("غزوة الخندق (الأحزاب)", 627.0, "٦٢٧م", "حفر الخندق بمشورة سلمان الفارسي وانتهت بريح صرصر أرسلها الله"),
    SeerahEvent("صلح الحديبية", 628.0, "٦٢٨م", "فتح مبين ودعوة الملوك والرؤساء للإسلام في العالم"),
    SeerahEvent("فتح خيبر", 628.5, "٦٢٨م", "قاد الفتح علي بن أبي طالب رضي الله عنه وسقوط حصون اليهود"),
    SeerahEvent("غزوة مؤتة", 629.0, "٦٢٩م", "أول مواجهة مع الروم واستشهاد قادتها الثلاثة واستبسال خالد بن الوليد"),
    SeerahEvent("فتح مكة المكرمة", 630.0, "٦٣٠م", "دخلها ﷺ فاتحاً متواضعاً وقال لأهلها: اذهبوا فأنتم الطلقاء"),
    SeerahEvent("غزوة تبوك (جيش العسرة)", 630.5, "٦٣٠م", "آخر غزاة النبي ﷺ لتأمين حدود الجزيرة من الروم"),
    SeerahEvent("حجة الوداع", 632.0, "٦٣٢م", "خطبة عرفات الشهيرة لإرساء حقوق الإنسان وتكامل أركان الدين"),
    SeerahEvent("وفاة النبي ﷺ", 632.3, "٦٣٢م", "توفي في بيت عائشة رضي الله عنها وأدى الأمانة وبلغ الرسالة"),
    SeerahEvent("حروب الردة في عهد الصديق", 633.0, "٦٣٣م", "ثبات أبي بكر الصديق في تثبيت دعائم الإسلام والقضاء على مسيلمة"),
    SeerahEvent("جمع القرآن الكريم الأول", 634.0, "٦٣٤م", "أمر به أبو بكر وجمع الصحابي زيد بن ثابت المصحف في صحف واحدة"),
    SeerahEvent("معركة اليرموك الخالدة", 636.0, "٦٣٦م", "انتصار المسلمين بقيادة خالد بن الوليد وكسر شوكة الدولة البيزنطية"),
    SeerahEvent("فتح بيت المقدس وتسلم عمر لمفاتيحها", 637.0, "٦٣٧م", "حضر الفاروق عمر بن الخطاب بنفسه وكتب الوثيقة العمرية لأهلها"),
    SeerahEvent("معركة القادسية", 638.0, "٦٣٨م", "انتصار المسلمين بقيادة سعد بن أبي وقاص وسقوط الإمبراطورية الفارسية"),
    SeerahEvent("فتح مصر بقيادة عمرو بن العاص", 641.0, "٦٤١م", "تأسيس مدينة الفسطاط وانضمام مصر للحضارة الإسلامية"),
    SeerahEvent("نسخ المصاحف العثمانية وتوزيعها", 650.0, "٦٥٠م", "توحيد قراءة القرآن على حرف قريش في عهد عثمان بن عفان")
)

@Composable
fun TimelineGameScreen(onBack: () -> Unit) {
    ForcePortraitOrientation()

    val context = LocalContext.current
    var currentLevelIdx by remember { mutableIntStateOf(0) }
    var inPlay by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val totalLevels = 20
    val highestLevel = remember(refreshKey) { GameProgressStore.getHighestLevel(context, GameProgressStore.GAME_TIMELINE) }
    val totalStars = remember(refreshKey) { GameProgressStore.getTotalStars(context, GameProgressStore.GAME_TIMELINE, totalLevels) }
    val totalScore = remember(refreshKey) { GameProgressStore.getTotalScore(context, GameProgressStore.GAME_TIMELINE) }
    val streak = remember(refreshKey) { GameProgressStore.getStreak(context, GameProgressStore.GAME_TIMELINE) }

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (inPlay) inPlay = false else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(Modifier.weight(1f)) {
                    Text("خط الزمن النبوي", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("المستوى ${currentLevelIdx + 1} / $totalLevels | ⭐ $totalStars | 🌙 $totalScore | 🔥 $streak أعياد", color = GoldSoft, fontSize = 11.sp)
                }
            }

            if (!inPlay) {
                Text(
                    "خريطة الأحداث التاريخية",
                    color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(List(totalLevels) { it }) { index, _ ->
                        val isUnlocked = index <= highestLevel
                        val starsEarned = GameProgressStore.getLevelStars(context, GameProgressStore.GAME_TIMELINE, index)

                        Box(
                            Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isUnlocked) NavyCard else NavyCard.copy(alpha = 0.4f))
                                .border(
                                    1.dp,
                                    if (starsEarned > 0) Gold else if (isUnlocked) Gold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = isUnlocked) {
                                    currentLevelIdx = index
                                    inPlay = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isUnlocked) {
                                    Text("${index + 1}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(2.dp))
                                    Row {
                                        for (s in 1..3) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (s <= starsEarned) Gold else Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = "مقفل", tint = GoldSoft.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                TimelinePlayScreen(
                    levelIdx = currentLevelIdx,
                    onLevelComplete = { heartsLeft, scoreGained ->
                        val stars = when (heartsLeft) {
                            3 -> 3
                            2 -> 2
                            else -> 1
                        }
                        GameProgressStore.rewardPoints(context, GameProgressStore.GAME_TIMELINE, currentLevelIdx, stars, scoreGained)
                        refreshKey++
                    },
                    onNextLevel = {
                        if (currentLevelIdx + 1 < totalLevels) {
                            currentLevelIdx++
                        } else {
                            inPlay = false
                        }
                    },
                    onExit = { inPlay = false }
                )
            }
        }
    }
}

@Composable
private fun TimelinePlayScreen(
    levelIdx: Int,
    onLevelComplete: (heartsLeft: Int, scoreGained: Int) -> Unit,
    onNextLevel: () -> Unit,
    onExit: () -> Unit
) {
    val eventCount = when {
        levelIdx < 5 -> 3
        levelIdx < 12 -> 4
        else -> 5
    }

    val pool = remember(levelIdx) {
        val seed = (levelIdx * 23 + 7).toLong()
        seerahEvents.shuffled(Random(seed)).take(eventCount)
    }

    var hearts by remember(levelIdx) { mutableIntStateOf(3) }
    var score by remember(levelIdx) { mutableIntStateOf(0) }
    var placed by remember(levelIdx) { mutableStateOf(listOf<SeerahEvent>()) }
    var wrong by remember(levelIdx) { mutableStateOf<Int?>(null) }
    var roundDone by remember(levelIdx) { mutableStateOf(false) }

    fun tap(event: SeerahEvent) {
        if (roundDone) return
        val remaining = pool - placed.toSet()
        val oldest = remaining.minByOrNull { it.yearNum }
        if (event == oldest) {
            placed = placed + event
            score += 40
            if (remaining.size == 1) {
                placed = placed + remaining.first()
                roundDone = true
                onLevelComplete(hearts, score)
            }
        } else {
            wrong = pool.indexOf(event)
            hearts -= 1
            if (hearts <= 0) {
                roundDone = true
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("النقاط: $score", color = GoldSoft, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text("❤️".repeat(hearts.coerceAtLeast(0)), fontSize = 14.sp)
        }

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("الأحداث المرتبة زمنياً (الأقدم أولاً):", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
            }

            items(placed.size) { i ->
                val e = placed[i]
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(Gold))
                    Spacer(Modifier.width(8.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold.copy(alpha = 0.12f))
                            .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(e.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("سنة ${e.year} — ${e.note}", color = GoldSoft, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            if (!roundDone) {
                item {
                    Spacer(Modifier.height(10.dp))
                    Text("اختر الحدث الأقدم تاريخياً:", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                }
                items((pool - placed.toSet()).size) { idx ->
                    val ev = (pool - placed.toSet())[idx]
                    val isWrong = wrong == pool.indexOf(ev) && hearts > 0
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isWrong) Color(0xFF7F1D1D).copy(alpha = 0.35f) else NavyCard)
                            .border(1.dp, if (isWrong) Color(0xFFEF4444).copy(alpha = 0.6f) else Gold.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                            .clickable { tap(ev) }
                            .padding(13.dp)
                    ) {
                        Text(ev.title, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            } else {
                item {
                    Spacer(Modifier.height(16.dp))
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (hearts > 0) "ما شاء الله! ترتيب تاريخي دقيق 🌙" else "انتهت القلوب — حاول مجدداً",
                            color = Gold, fontWeight = FontWeight.Bold, fontSize = 15.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NavyCard)
                                    .border(1.dp, Gold, RoundedCornerShape(12.dp))
                                    .clickable { onExit() }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text("القائمة", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            if (hearts > 0) {
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Gold)
                                        .clickable { onNextLevel() }
                                        .padding(horizontal = 26.dp, vertical = 10.dp)
                                ) {
                                    Text("المستوى التالي", color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
