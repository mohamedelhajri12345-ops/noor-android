package com.elhajri.noor.games

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import com.elhajri.noor.ui.NavyLight
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import com.elhajri.noor.ui.NoorGradients

private const val PREFS_KEY = "nur_prophets_journey"

private fun toArabicDigits(number: Any): String {
    val str = number.toString()
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return str.map { ch ->
        if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
    }.joinToString("")
}

private data class JourneyQuestion(val q: String, val options: List<String>, val correct: Int)
private data class JourneyLevel(val prophet: String, val title: String, val questions: List<JourneyQuestion>)

private data class JourneyProgress(val completedLevels: Set<Int>, val currentLevel: Int)

private fun loadProgress(context: Context): JourneyProgress {
    val sp = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
    val str = sp.getString(PREFS_KEY, "{}") ?: "{}"
    return try {
        val obj = JSONObject(str)
        val arr = obj.optJSONArray("completedLevels") ?: JSONArray()
        val completed = mutableSetOf<Int>()
        for (i in 0 until arr.length()) completed.add(arr.getInt(i))
        val current = obj.optInt("currentLevel", 0)
        JourneyProgress(completed, current)
    } catch (_: Exception) {
        JourneyProgress(emptySet(), 0)
    }
}

private fun saveProgress(context: Context, progress: JourneyProgress) {
    val sp = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
    val obj = JSONObject()
    val arr = JSONArray()
    progress.completedLevels.forEach { arr.put(it) }
    obj.put("completedLevels", arr)
    obj.put("currentLevel", progress.currentLevel)
    sp.edit().putString(PREFS_KEY, obj.toString()).apply()
}

private val allLevels = listOf(
    JourneyLevel("آدم عليه السلام", "أبو البشر", listOf(
        JourneyQuestion("من هو أول إنسان خلقه الله؟", listOf("آدم", "نوح", "إبراهيم", "موسى"), 0),
        JourneyQuestion("مم خُلق آدم عليه السلام؟", listOf("من تراب", "من نور", "من ماء", "من نار"), 0),
        JourneyQuestion("من زوجة آدم عليه السلام؟", listOf("حواء", "سارة", "هاجر", "نوجة"), 0),
        JourneyQuestion("في أي جنة كان آدم عليه السلام؟", listOf("جنة الخلد", "جنة عدن", "الجنة", "جنة النعيم"), 2),
        JourneyQuestion("ماذا أمر الله آدم أن لا يأكل؟", listOf("التفاح", "الشجرة", "العنب", "التمر"), 1)
    )),
    JourneyLevel("نوح عليه السلام", "شيخ المرسلين", listOf(
        JourneyQuestion("كم سنة دعا نوح قومه؟", listOf("٥٠٠", "٧٥٠", "٩٥٠", "١٠٠٠"), 2),
        JourneyQuestion("ماذا صنع نوح عليه السلام؟", listOf("قصرًا", "سفينة", "بيتًا", "مسجدًا"), 1),
        JourneyQuestion("كم عدد أبناء نوح؟", listOf("٢", "٣", "٤", "٥"), 2),
        JourneyQuestion("ماذا كان قوم نوح يعبدون؟", listOf("الله", "الأصنام", "النجوم", "الملائكة"), 1),
        JourneyQuestion("ما اسم السورة التي تذكر قصة نوح؟", listOf("البقرة", "نوح", "هود", "كل ما سبق"), 3)
    )),
    JourneyLevel("إبراهيم عليه السلام", "خليل الرحمن", listOf(
        JourneyQuestion("من هو أبو الأنبياء؟", listOf("نوح", "إبراهيم", "موسى", "عيسى"), 1),
        JourneyQuestion("ماذا ألقِي إبراهيم فيه؟", listOf("الماء", "النار", "الكهف", "السجن"), 1),
        JourneyQuestion("من ابن إبراهيم الذي أمر بذبحه؟", listOf("إسماعيل", "إسحاق", "يعقوب", "يوسف"), 0),
        JourneyQuestion("ماذا بنى إبراهيم مع ابنه؟", listOf("السفينة", "الكعبة", "المسجد", "القصر"), 1),
        JourneyQuestion("ما لقب إبراهيم عليه السلام؟", listOf("خليل الله", "نبي الله", "رسول الله", "كل ما سبق"), 3)
    )),
    JourneyLevel("إسماعيل عليه السلام", "الصديق الذبيح", listOf(
        JourneyQuestion("من والد إسماعيل؟", listOf("إبراهيم", "نوح", "موسى", "آدم"), 0),
        JourneyQuestion("أين عاشت هاجر وإسماعيل؟", listOf("مكة", "المدينة", "الطائف", "الشام"), 0),
        JourneyQuestion("ماذا فجر الله لإسماعيل وأمه؟", listOf("بئر زمزم", "نهر", "عين", "لا شيء"), 0),
        JourneyQuestion("ماذا كان إسماعيل عليه السلام؟", listOf("صبّار", "صادق الوعد", "حليم", "كل ما سبق"), 3),
        JourneyQuestion("كم مرة ذُكر إسماعيل في القرآن؟", listOf("٥", "٨", "١٠", "١٢"), 3)
    )),
    JourneyLevel("لوط عليه السلام", "نبي الله", listOf(
        JourneyQuestion("أين كان قوم لوط يعيشون؟", listOf("مكة", "سدوم", "الشام", "اليمن"), 1),
        JourneyQuestion("ماذا فعل الله بقوم لوط؟", listOf("أغرقهم", "أهلكهم", "أنزل عليهم حجارة", "كل ما سبق"), 2),
        JourneyQuestion("من أرسل الله لإنقاذ لوط؟", listOf("ملائكة", "إبراهيم", "نوح", "موسى"), 0),
        JourneyQuestion("ماذا كان ذنب قوم لوط؟", listOf("الشرك", "الفاحشة", "الكذب", "السرقة"), 1),
        JourneyQuestion("من نجا من قوم لوط معه؟", listOf("أهله كلهم", "بناته فقط", "زوجته لم تنج", "لا أحد"), 2)
    )),
    JourneyLevel("إسحاق عليه السلام", "نبي الله", listOf(
        JourneyQuestion("من والد إسحاق؟", listOf("إبراهيم", "إسماعيل", "نوح", "موسى"), 0),
        JourneyQuestion("من والدته؟", listOf("سارة", "حواء", "هاجر", "نوجة"), 0),
        JourneyQuestion("ماذا بشر الله إبراهيم به؟", listOf("بإسحاق", "بإسماعيل", "بيوسف", "بموسى"), 0),
        JourneyQuestion("من ابن إسحاق؟", listOf("يعقوب", "يوسف", "موسى", "عيسى"), 0),
        JourneyQuestion("كم مرة ذُكر إسحاق في القرآن؟", listOf("٥", "١٠", "١٥", "٢٠"), 2)
    )),
    JourneyLevel("يعقوب عليه السلام", "إسرائيل", listOf(
        JourneyQuestion("ما لقب يعقوب عليه السلام؟", listOf("إسرائيل", "إسحاق", "إبراهيم", "نوح"), 0),
        JourneyQuestion("كم عدد أبناء يعقوب؟", listOf("١٠", "١٢", "١٤", "١٦"), 1),
        JourneyQuestion("من ابن يعقوب الذي أحبه كثيرًا؟", listOf("يوسف", "بنيامين", "يهوذا", "لا أحد"), 0),
        JourneyQuestion("ماذا حدث ليعقوب بسبب يوسف؟", listOf("فقد بصره", "مرض", "سافر", "لا شيء"), 0),
        JourneyQuestion("كم سنة بكي يعقوب على يوسف؟", listOf("٥", "١٠", "٢٠", "٤٠"), 3)
    )),
    JourneyLevel("يوسف عليه السلام", "صفوة الأنبياء", listOf(
        JourneyQuestion("من والد يوسف؟", listOf("يعقوب", "إبراهيم", "إسحاق", "نوح"), 0),
        JourneyQuestion("ماذا رأى يوسف في منامه؟", listOf("١١ نجمة", "٩ نجوم", "٧ نجوم", "٥ نجوم"), 0),
        JourneyQuestion("أين أُلقي يوسف في البئر؟", listOf("مصر", "الشام", "كنعان", "مكة"), 2),
        JourneyQuestion("من اشترى يوسف في مصر؟", listOf("العزيز", "الملك", "التاجر", "لا أحد"), 0),
        JourneyQuestion("كم سنة بقي يوسف في السجن؟", listOf("٣", "٥", "٧", "١٠"), 2)
    )),
    JourneyLevel("شعيب عليه السلام", "نبي الله", listOf(
        JourneyQuestion("إلى أي قوم أُرسل شعيب؟", listOf("أصحاب الأيكة", "قوم عاد", "قوم ثمود", "قوم نوح"), 0),
        JourneyQuestion("ماذا كان ذنب قوم شعيب؟", listOf("التطفيف", "الشرك", "الكذب", "السرقة"), 0),
        JourneyQuestion("ماذا حدث لقوم شعيب؟", listOf("أخذتهم الصيحة", "أغرقوا", "أحرقوا", "لا شيء"), 0),
        JourneyQuestion("من هو والد زوجة موسى؟", listOf("شعيب", "إبراهيم", "نوح", "يوسف"), 0),
        JourneyQuestion("كم مرة ذُكر شعيب في القرآن؟", listOf("٥", "٨", "١١", "١٥"), 2)
    )),
    JourneyLevel("موسى عليه السلام", "كليم الله", listOf(
        JourneyQuestion("من والد موسى؟", listOf("عمران", "إبراهيم", "نوح", "يوسف"), 0),
        JourneyQuestion("ماذا أمر الله أم موسى؟", listOf("أرضعيه ثم ألقِيه في اليم", "اختبئي به", "سافري به", "لا شيء"), 0),
        JourneyQuestion("من ربى موسى؟", listOf("فرعون", "العزيز", "الملك", "لا أحد"), 0),
        JourneyQuestion("ما معجزة موسى الأولى؟", listOf("العصا", "اليد", "الطوفان", "الغرق"), 0),
        JourneyQuestion("ماذا فعل موسى بالعصا؟", listOf("ألقاها فتحولت ثعبانًا", "ضرب بها البحر", "كلاهما", "لا شيء"), 2)
    )),
    JourneyLevel("هارون عليه السلام", "نبي الله", listOf(
        JourneyQuestion("من هو هارون عليه السلام؟", listOf("أخو موسى", "ابن موسى", "عم موسى", "لا شيء"), 0),
        JourneyQuestion("ماذا طلب موسى من الله؟", listOf("أن يرسل معه هارون", "أن ينصره", "أن يرحمه", "لا شيء"), 0),
        JourneyQuestion("كم مرة ذُكر هارون في القرآن؟", listOf("١٠", "١٥", "٢٠", "٢٥"), 2),
        JourneyQuestion("ماذا كان هارون؟", listOf("نبي", "رسول", "وزير", "كل ما سبق"), 3),
        JourneyQuestion("من خلف موسى حين ذهب للقاء ربه؟", listOf("هارون", "يوشع", "العزيز", "لا أحد"), 0)
    )),
    JourneyLevel("داود عليه السلام", "نبي الله", listOf(
        JourneyQuestion("من قتل جالوت؟", listOf("داود", "موسى", "سليمان", "طالوت"), 0),
        JourneyQuestion("ماذا أنزل الله على داود؟", listOf("الزبور", "التوراة", "الإنجيل", "الصحف"), 0),
        JourneyQuestion("ماذا كان يصنع داود؟", listOf("الدرع", "السفن", "البيوت", "السيوف"), 0),
        JourneyQuestion("كم مرة ذُكر داود في القرآن؟", listOf("١٠", "١٦", "٢٠", "٢٥"), 1),
        JourneyQuestion("من ابن داود؟", listOf("سليمان", "موسى", "عيسى", "لا شيء"), 0)
    )),
    JourneyLevel("سليمان عليه السلام", "نبي الله", listOf(
        JourneyQuestion("ماذا سخر الله لسليمان؟", listOf("الرياح", "الجن", "الطير", "كل ما سبق"), 3),
        JourneyQuestion("ماذا كان يفعل سليمان بالطير؟", listOf("يكلمها", "يأكلها", "يبيعها", "لا شيء"), 0),
        JourneyQuestion("ما قصة سليمان وبلقيس؟", listOf("ملكة سبأ", "ملكة مصر", "ملكة فارس", "لا شيء"), 0),
        JourneyQuestion("ماذا كان يفعل الجن لسليمان؟", listOf("يبنون له", "يحفرون له", "يصنعون له", "كل ما سبق"), 3),
        JourneyQuestion("كم مرة ذُكر سليمان في القرآن؟", listOf("١٠", "١٧", "٢٠", "٢٥"), 1)
    )),
    JourneyLevel("عيسى عليه السلام", "روح الله", listOf(
        JourneyQuestion("من والدة عيسى؟", listOf("مريم", "خديجة", "آسية", "سارة"), 0),
        JourneyQuestion("كم مرة ذُكر عيسى في القرآن؟", listOf("١٥", "٢٥", "٣٥", "٤٠"), 1),
        JourneyQuestion("ماذا أنزل الله على عيسى؟", listOf("الإنجيل", "التوراة", "الزبور", "القرآن"), 0),
        JourneyQuestion("ما معجزة عيسى الأولى؟", listOf("الكلام في المهد", "إحياء الموتى", "خلق الطير", "شفاء المرضى"), 0),
        JourneyQuestion("ماذا يحدث لعيسى في آخر الزمان؟", listOf("ينزل إلى الأرض", "يرفع إلى السماء", "يموت", "لا شيء"), 0)
    )),
    JourneyLevel("محمد ﷺ", "خاتم الأنبياء", listOf(
        JourneyQuestion("أين وُلد النبي ﷺ؟", listOf("مكة", "المدينة", "الطائف", "الرياض"), 0),
        JourneyQuestion("كم كان عمره عند البعثة؟", listOf("٢٥", "٣٠", "٤٠", "٥٠"), 2),
        JourneyQuestion("في أي غار نزل الوحي؟", listOf("حراء", "ثور", "الكهف", "لا شيء"), 0),
        JourneyQuestion("كم سنة مكث في مكة؟", listOf("١٠", "١٣", "١٥", "٢٠"), 1),
        JourneyQuestion("كم سنة مكث في المدينة؟", listOf("٥", "٨", "١٠", "١٣"), 2)
    ))
)

@Composable
fun ProphetsJourneyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var progress by remember { mutableStateOf(loadProgress(context)) }
    var activeLevelIdx by remember { mutableStateOf<Int?>(null) }

    val handleCompleteLevel = { levelIdx: Int ->
        val newCompleted = progress.completedLevels + levelIdx
        val newCurrent = maxOf(progress.currentLevel, levelIdx + 1)
        val updated = JourneyProgress(newCompleted, newCurrent)
        progress = updated
        saveProgress(context, updated)
        activeLevelIdx = null
    }

    if (activeLevelIdx != null) {
        val level = allLevels[activeLevelIdx!!]
        LevelPlayView(
            level = level,
            levelIdx = activeLevelIdx!!,
            onBack = { activeLevelIdx = null },
            onComplete = { handleCompleteLevel(activeLevelIdx!!) }
        )
    } else {
        LevelSelectView(
            progress = progress,
            onSelectLevel = { idx -> activeLevelIdx = idx },
            onBack = onBack
        )
    }
}

@Composable
private fun LevelSelectView(
    progress: JourneyProgress,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoorGradients.ScreenBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("رحلة الأنبياء", style = MaterialTheme.typography.titleLarge, color = Gold, fontWeight = FontWeight.Bold)
                Text("${toArabicDigits(allLevels.size)} مستوى · ${toArabicDigits(allLevels.size * 5)} سؤال", style = MaterialTheme.typography.bodySmall, color = GoldSoft)
            }
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(allLevels) { idx, level ->
                val isCompleted = idx in progress.completedLevels
                val isUnlocked = idx <= progress.currentLevel

                val cardBg = when {
                    isCompleted -> NavyCard
                    isUnlocked -> NavyLight
                    else -> NavyCard.copy(alpha = 0.5f)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clickable(enabled = isUnlocked) { onSelectLevel(idx) },
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "المستوى ${toArabicDigits(idx + 1)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldSoft
                            )
                            if (isCompleted) {
                                Icon(Icons.Default.Star, contentDescription = "مكتمل", tint = Gold, modifier = Modifier.size(20.dp))
                            } else if (!isUnlocked) {
                                Icon(Icons.Default.Lock, contentDescription = "مقفل", tint = GoldSoft.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                            }
                        }

                        Column {
                            Text(
                                level.prophet,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                level.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isUnlocked) GoldSoft else GoldSoft.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelPlayView(
    level: JourneyLevel,
    levelIdx: Int,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    var qIdx by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    val currentQ = level.questions.getOrNull(qIdx)

    LaunchedEffect(selectedOption) {
        if (selectedOption != null) {
            delay(1200)
            if (qIdx + 1 >= level.questions.size) {
                finished = true
            } else {
                qIdx++
                selectedOption = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoorGradients.ScreenBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
            }
            Text(level.prophet, style = MaterialTheme.typography.titleLarge, color = Gold, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(48.dp))
        }

        if (finished) {
            val passed = score >= 3
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (passed) Icons.Default.EmojiEvents else Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (passed) "أحسنت! أكملت المستوى 🌟" else "حاول مرة أخرى 📚",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Gold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${toArabicDigits(score)}/${toArabicDigits(level.questions.size)} إجابات صحيحة",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(Modifier.height(24.dp))
                if (passed) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("متابعة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            qIdx = 0
                            score = 0
                            selectedOption = null
                            finished = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إعادة المستوى", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (currentQ != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("السؤال ${toArabicDigits(qIdx + 1)}/${toArabicDigits(level.questions.size)}", style = MaterialTheme.typography.bodyMedium, color = GoldSoft)
                Text("النتيجة: ${toArabicDigits(score)}", style = MaterialTheme.typography.bodyMedium, color = Gold)
            }

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(currentQ.q, style = MaterialTheme.typography.titleLarge, color = Color.White, textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentQ.options.forEachIndexed { i, opt ->
                    val isCorrect = i == currentQ.correct
                    val isSelected = i == selectedOption

                    val cardBg = when {
                        selectedOption != null && isCorrect -> Gold.copy(alpha = 0.25f)
                        isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                        else -> NavyCard
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = selectedOption == null) {
                                selectedOption = i
                                if (i == currentQ.correct) score++
                            },
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(opt, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
