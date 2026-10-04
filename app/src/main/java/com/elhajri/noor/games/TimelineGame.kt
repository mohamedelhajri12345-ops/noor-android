package com.elhajri.noor.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * "خط الزمن النبوي" — المس الأحداث بالترتيب الزمني الصحيح: الأقدم أولاً.
 * ثلاثة قلوب في الجولة، وحدث جديد يظهر كل مرة مع سنة الحدث ومعلومة ميسرة.
 */
private data class SeerahEvent(val title: String, val yearNum: Double, val year: String, val note: String)

private val seerahEvents = listOf(
    SeerahEvent("مولد النبي ﷺ في مكة", 571.0, "٥٧١م", "وُلد عام الفيل، في بني هاشم، ويُتيم أبوه عبد الله"),
    SeerahEvent("رحلة الشام وحريرة الراهب", 583.0, "٥٨٣م", "بكى بحيرة الراهب وقال: هذا نبي هذه الأمة"),
    SeerahEvent("زواج النبي ﷺ من خديجة", 595.0, "٥٩٥م", "كان عمره خمسة وعشرين عاماً وهي أول من آمن به"),
    SeerahEvent("بدء الوحي في غار حراء", 610.0, "٦١٠م", "نزلت عليه أول آيات سورة العلق: اقْرَأْ بِاسْمِ رَبِّكَ"),
    SeerahEvent("الدعوة السرية في مكة", 612.0, "٦١٢م", "استمرت ثلاث سنين سراً حتى نزول ﴿فَاصْدَعْ بِمَا تُؤْمَرُ﴾"),
    SeerahEvent("الهجرة إلى يثرب", 622.0, "٦٢٢م", "وفيها بدأ التقويم الهجري، وبدأ التاريخ الإسلامي"),
    SeerahEvent("بناء المسجد النبوي", 623.0, "٦٢٣م", "أول مسجد في الإسلام وأساس المدينة الجديد"),
    SeerahEvent("غزوة بدر الكبرى", 624.0, "٦٢٤م", "يوم الفرقان: ثلاثمئة مقاتل ضد ألف بمعية الملائكة"),
    SeerahEvent("غزوة أحد", 625.0, "٦٢٥م", "استُشهد فيها حمزة رضي الله عنه سيد الشهداء"),
    SeerahEvent("غزوة الخندق", 627.0, "٦٢٧م", "حفر المسلمون الخندق بمشورة سلمان الفارسي"),
    SeerahEvent("صلح الحديبية", 628.3, "٦٢٨م", "فتح مبين: ﴿إِنَّا فَتَحْنَا لَكَ فَتْحًا مُّبِينًا﴾"),
    SeerahEvent("فتح خيبر", 628.5, "٦٢٨م", "بعد الحديبية بشهور، وفيها قال ﷺ: لأعطين الراية لرجل يحبه الله"),
    SeerahEvent("فتح مكة", 630.0, "٦٣٠م", "عفو النبي ﷺ عن قريش: اذهبوا فأنتم الطلقاء"),
    SeerahEvent("حجة الوداع", 632.0, "٦٣٢م", "خطب ﷺ في عرفات: لا يفخر أحد على أحد ولا فضل لعربي على أعجمي إلا بالتقوى"),
    SeerahEvent("وفاة النبي ﷺ", 633.0, "٦٣٢م", "توفي ورأسه في حجر عائشة، آخر ما وصى به الصلاة وما ملكت أيمانكم"),
    SeerahEvent("جمع المصحف في عهد أبي بكر", 634.0, "٦٣٣م", "أمر أبو بكر زيد بن ثابت فجمع القرآن في مصحف واحد"),
    SeerahEvent("فتح بيت المقدس", 637.0, "٦٣٧م", "في عهد عمر بن الخطاب الذي صلى فيه عند الصخرة"),
    SeerahEvent("فتح الأندلس", 711.0, "٧١١م", "قاد طارق بن زياد الجيش وقال: البحر من ورائكم والعدو من أمامكم")
)

@Composable
fun TimelineGameScreen(onBack: () -> Unit) {
    var round by remember { mutableStateOf(0) }
    var hearts by remember { mutableIntStateOf(3) }
    var score by remember { mutableIntStateOf(0) }
    var placed by remember { mutableStateOf(listOf<SeerahEvent>()) }
    var pool by remember { mutableStateOf(listOf<SeerahEvent>()) }
    var wrong by remember { mutableStateOf<Int?>(null) }
    var roundDone by remember { mutableStateOf(false) }
    var gameDone by remember { mutableStateOf(false) }

    // بدء جولة: ٥ أحداث عشوائية، يُطلب ترتيبها بالمس من الأقدم
    fun newRound(r: Int) {
        val seed = Random(r)
        val picks = seerahEvents.shuffled(seed).take(5)
        pool = picks
        placed = emptyList()
        hearts = 3
        roundDone = false
        wrong = null
    }
    LaunchedEffect(Unit) { newRound(0) }

    fun tap(event: SeerahEvent) {
        if (roundDone || gameDone) return
        val remaining = pool - placed.toSet()
        val oldest = remaining.minByOrNull { it.yearNum }
        if (event == oldest) {
            placed = placed + event
            score += 30
            if (remaining.size == 1) { roundDone = true; if (round >= 2) gameDone = true }
        } else {
            wrong = pool.indexOf(event)
            hearts -= 1
            if (hearts <= 0) { roundDone = true; if (round >= 2) gameDone = true }
        }
    }

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold) }
                Column(Modifier.weight(1f)) {
                    Text("خط الزمن النبوي", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("المس الأقدم حدثاً أولاً", color = GoldSoft, fontSize = 11.sp)
                }
                Text("❤️".repeat(hearts.coerceAtLeast(0)), fontSize = 14.sp)
            }

            Row(Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                Text("النقاط: $score", color = GoldSoft, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Text("الجولة ${round + 1}/٣", color = GoldSoft, fontSize = 12.sp)
            }

            // الخط الزمني: الأحداث المرتبة
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("✔ مرتبة", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
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
                                .background(Gold.copy(alpha = 0.10f))
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
                        Text("اختر الأقدم:", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                                .border(1.dp, if (isWrong) Color(0xFFEF4444).copy(alpha = 0.6f) else Gold.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .clickable { tap(ev) }
                                .padding(13.dp)
                        ) {
                            Text(ev.title, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (hearts > 0) "أحسنت! أكملت الجولة 🌙" else "انتهت القلوب — الجولة التالية",
                                color = Gold, fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Gold)
                                    .clickable { if (!gameDone) { round++; newRound(round) } else onBack() }
                                    .padding(horizontal = 26.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    if (gameDone) "إنهاء الخط الزمني" else "الجولة التالية",
                                    color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp
                                )
                            }
                            if (gameDone) {
                                Text("النتيجة النهائية: $score نقطة", color = GoldSoft, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
