package com.elhajri.noor.hajj

import com.elhajri.noor.settings.NoorLocale
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits

import com.elhajri.noor.ui.themeScreenBackground
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Refresh
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
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import org.json.JSONObject
import com.elhajri.noor.ui.NoorGradients
import com.elhajri.noor.theme.noorGlassCard
import com.elhajri.noor.ui.TextMain

private fun toArabicDigits(number: Any): String {
    val str = number.toString()
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return str.map { ch ->
        if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
    }.joinToString("")
}

private data class Step(val titleAr: String, val titleEn: String, val descAr: String, val descEn: String)
private data class Dua(val text: String, val refAr: String, val refEn: String)
private data class HajjQuizQuestion(val q: String, val options: List<String>, val answer: Int)

private val umrahSteps = listOf(
    Step(
        "الإحرام", "Ihram",
        "الإحرام هو نية الدخول في النسك. يُستحب الاغتسال والتطيب قبل الإحرام. يلبس الرجل إزاراً ورداءً أبيضين، أما المرأة فتلبس ما تشاء من ثيابها الشرعية. ثم ينوي بقلبه ويلبي بلسانه قائلاً: لبيك عمرة. ويبدأ بالتلبية: لبيك اللهم لبيك، لبيك لا شريك لك لبيك، إن الحمد والنعمة لك والملك، لا شريك لك.",
        "Ihram is the sacred intention to enter Umrah. Ghusl and applying perfume beforehand is recommended. Men wear two unstitched white garments, while women wear modest Islamic clothing. Make the intention from the heart and say: Labbayk Umrah, then repeat the Talbiyah."
    ),
    Step(
        "الطواف", "Tawaf",
        "بعد الوصول للمسجد الحرام، يطوف حول الكعبة سبعة أشواط. يبدأ الطواف من الحجر الأسود ويجعل الكعبة عن يساره. يُستحب للرجل أن يسرع المشي في الأشواط الثلاثة الأولى (الرمل) ويكشف كتفه الأيمن (الاضطباع). يبدأ كل شوط بالحجر الأسود وينتهي به. يُستحب مسح الحجر الأسود إن أمكن، وإلا أشار إليه.",
        "Upon arriving at Al-Masjid Al-Haram, perform 7 circuits around the Ka'bah starting from the Black Stone with the Ka'bah on your left. Men uncover their right shoulder (Idtiba) and walk at a brisk pace during the first 3 circuits (Raml)."
    ),
    Step(
        "السعي", "Sa'i",
        "بعد الطواف، يخرج إلى الصفا ويسعى بين الصفا والمروة سبعة أشواط. يبدأ من الصفا ويمشي إلى المروة (شوط أول)، ثم يعود من المروة إلى الصفا (شوط ثاني)، وهكذا حتى يكمل سبعة أشواط. يُستحب الإسراع بين العلمين الأخضرين للرجال. ينتهي السعي عند المروة.",
        "After Tawaf, proceed to Safa and walk between Safa and Marwah for 7 circuits. Start from Safa to Marwah (1st circuit), then Marwah to Safa (2nd circuit), finishing the 7th circuit at Marwah."
    ),
    Step(
        "الحلق أو التقصير", "Shaving or Trimming Hair",
        "بعد إكمال السعي، يحلق المحرم شعر رأسه أو يقصره. الحلق أفضل للرجال، أما المرأة فتقصر من أطراف شعرها بمقدار أنملة. وبذلك تتم العمرة ويتحلل المحرم من إحرامه، ويباح له كل ما كان محظوراً بالإحرام.",
        "After Sa'i, men shave or shorten their hair (shaving is superior for men), while women trim a fingertip's length from the ends. This completes Umrah and leaves the state of Ihram."
    )
)

private val hajjSteps = listOf(
    Step("الإحرام من الميقات", "Ihram from Miqat", "يحرم الحاج من الميقات الذي يمر به. ينوي الحج بقلبه ويلبي بلسانه...", "The pilgrim enters Ihram at the designated Miqat with intention for Hajj and recites the Talbiyah."),
    Step("طواف القدوم", "Tawaf al-Qudum", "بعد الوصول لمكة، يطوف الحاج طواف القدوم سبعة أشواط حول الكعبة...", "Upon arrival in Makkah, perform Tawaf al-Qudum (Arrival Tawaf) 7 circuits around the Ka'bah."),
    Step("السعي", "Sa'i between Safa and Marwah", "بعد طواف القدوم، يسعى الحاج بين الصفا والمروة سبعة أشواط...", "Perform Sa'i 7 circuits between Safa and Marwah."),
    Step("التوجه إلى منى (٨ ذو الحجة)", "Heading to Mina (8th Dhu al-Hijjah)", "في يوم التروية (٨ ذو الحجة) يتوجه الحاج إلى منى...", "On Tarwiyah Day (8th Dhu al-Hijjah), proceed to Mina to pray Dhuhr, Asr, Maghrib, Isha, and Fajr shortened."),
    Step("يوم عرفة (٩ ذو الحجة)", "Day of Arafah (9th Dhu al-Hijjah)", "هو أعظم أركان الحج. بعد شروق شمس يوم عرفة، يتوجه الحاج من منى إلى عرفة...", "The peak pillar of Hajj. Pilgrims stand at Arafah from noon until sunset engaged in prayer and supplication."),
    Step("المبيت بمزدلفة", "Overnight in Muzdalifah", "بعد غروب شمس يوم عرفة، يتوجه الحاج إلى مزدلفة...", "After sunset on Arafah Day, proceed to Muzdalifah to pray Maghrib and Isha combined and stay overnight."),
    Step("رمي جمرة العقبة (١٠ ذو الحجة)", "Stoning Jamrat al-Aqabah (10th Dhu al-Hijjah)", "بعد الفجر في يوم النحر (١٠ ذو الحجة)، يتوجه الحاج إلى منى...", "On Eid Day (10th Dhu al-Hijjah), stone Jamrat al-Aqabah with 7 pebbles, offer sacrifice, and shave/trim hair."),
    Step("طواف الإفاضة", "Tawaf al-Ifadah", "بعد الرمي والحلق، يذهب الحاج إلى مكة لطواف الإفاضة...", "Perform Tawaf al-Ifadah (7 circuits) in Makkah, an essential pillar of Hajj."),
    Step("أيام التشريق (١١-١٣ ذو الحجة)", "Days of Tashreeq (11th-13th Dhu al-Hijjah)", "في أيام التشريق الثلاثة (١١-١٣ ذو الحجة)، يبيت الحاج في منى...", "Stay overnight in Mina during Tashreeq Days and stone all three Jamarat daily after noon."),
    Step("طواف الوداع", "Tawaf al-Wada' (Farewell Tawaf)", "آخر أعمال الحج. قبل مغادرة مكة، يطوف الحاج طواف الوداع...", "Before leaving Makkah, perform the Farewell Tawaf (7 circuits) around the Ka'bah to complete Hajj.")
)

private val duas = listOf(
    Dua(
        "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ",
        "التلبية", "Talbiyah"
    ),
    Dua(
        "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
        "دعاء الطواف", "Dua of Tawaf"
    )
)

@Composable
fun HajjGuideScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val sp = remember { context.getSharedPreferences("nur_hajj_understood", Context.MODE_PRIVATE) }

    var selectedTab by remember { mutableStateOf("umrah") } // "umrah", "hajj", "duas"
    var isQuizMode by remember { mutableStateOf(false) }

    var understoodMap by remember {
        mutableStateOf(
            sp.all.mapValues { it.value as? Boolean ?: false }
        )
    }

    fun markUnderstood(key: String) {
        sp.edit().putBoolean(key, true).apply()
        understoodMap = understoodMap.toMutableMap().apply { put(key, true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (isQuizMode) isQuizMode = false else onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = t("رجوع", "Back"),
                    tint = Gold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isQuizMode) t("اختبار المناسك", "Rituals Quiz") else t("دليل الحج والعمرة", "Hajj & Umrah Guide"),
                style = MaterialTheme.typography.titleLarge,
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        }

        if (isQuizMode) {
            HajjQuizView(context = context, onFinish = { isQuizMode = false })
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Header Banner
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Gold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = t("دليل الحج والعمرة", "Hajj & Umrah Guide"),
                        style = MaterialTheme.typography.titleLarge,
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = t("اقرأ الخطوات واضغط \"فهمت\" لإتمام الاختبار", "Read the steps and tap \"Understood\" to unlock the quiz"),
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldSoft
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabButton(t("العمرة", "Umrah"), selectedTab == "umrah", Modifier.weight(1f)) { selectedTab = "umrah" }
                    TabButton(t("الحج", "Hajj"), selectedTab == "hajj", Modifier.weight(1f)) { selectedTab = "hajj" }
                    TabButton(t("أدعية", "Duas"), selectedTab == "duas", Modifier.weight(1f)) { selectedTab = "duas" }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val steps = if (selectedTab == "umrah") umrahSteps else if (selectedTab == "hajj") hajjSteps else emptyList()
                val allUnderstood = steps.isNotEmpty() && steps.indices.all { i -> understoodMap["${selectedTab}_$i"] == true }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (selectedTab != "duas") {
                        itemsIndexed(steps) { index, step ->
                            val key = "${selectedTab}_$index"
                            val isUnderstood = understoodMap[key] == true
                            StepCard(
                                index = index,
                                step = step,
                                isUnderstood = isUnderstood,
                                onUnderstand = { markUnderstood(key) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            if (allUnderstood) {
                                Button(
                                    onClick = { isQuizMode = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        text = t("ابدأ اختبار ", "Start ") + (if (selectedTab == "umrah") t("العمرة", "Umrah") else t("الحج", "Hajj")) + t("", " Quiz"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = t("أكمل قراءة جميع الخطوات واضغط \"فهمت\" لفتح الاختبار", "Complete reading all steps and tap \"Understood\" to unlock the quiz"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldSoft.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        itemsIndexed(duas) { _, dua ->
                            DuaCard(dua = dua)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = t("هذا دليل مبسط. يُنصح بمراجعة كتب المناسك المتخصصة واستشارة العلماء.", "This is a simplified guide. Consult scholar references for detailed rulings."),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Gold else NavyCard,
            contentColor = if (isSelected) Navy else GoldSoft
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StepCard(index: Int, step: Step, isUnderstood: Boolean, onUnderstand: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().noorGlassCard(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isUnderstood) Gold else Gold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUnderstood) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Navy, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = toArabicDigits(index + 1),
                                style = MaterialTheme.typography.titleMedium,
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = if (NoorLocale.isEnglish) step.titleEn else step.titleAr,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = GoldSoft,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = Gold.copy(alpha = 0.15f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (NoorLocale.isEnglish) step.descEn else step.descAr,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 26.sp),
                        color = GoldSoft
                    )

                    if (!isUnderstood) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onUnderstand,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.2f), contentColor = Gold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(t("فهمت", "Understood"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuaCard(dua: Dua) {
    Card(
        modifier = Modifier.fillMaxWidth().noorGlassCard(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = dua.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AmiriFamily,
                    fontSize = 20.sp,
                    lineHeight = 36.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (NoorLocale.isEnglish) dua.refEn else dua.refAr,
                style = MaterialTheme.typography.bodySmall,
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HajjQuizView(context: Context, onFinish: () -> Unit) {
    val questions = remember { loadHajjQuizQuestions(context) }
    var currentIdx by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedIdx by remember { mutableStateOf<Int?>(null) }
    var isFinished by remember { mutableStateOf(false) }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(t("لا توجد أسئلة متاح حالياً", "No questions available currently"), color = GoldSoft)
        }
        return
    }

    if (isFinished) {
        val pct = Math.round((score.toFloat() / questions.size) * 100)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(t("انتهى الاختبار", "Quiz Finished"), style = MaterialTheme.typography.headlineMedium, color = Gold, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${toArabicDigits(score)} / ${toArabicDigits(questions.size)}",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (pct >= 80) t("ممتاز! أداء رائع 🌟", "Excellent! Great job 🌟") else if (pct >= 60) t("جيد جداً 👍", "Very Good 👍") else t("تحتاج لمراجعة الخطوات 📚", "Review the steps 📚"),
                style = MaterialTheme.typography.bodyLarge,
                color = GoldSoft
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    currentIdx = 0
                    score = 0
                    selectedIdx = null
                    isFinished = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(t("إعادة الاختبار", "Retake Quiz"), fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val q = questions[currentIdx]
    val isAnswered = selectedIdx != null

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(t("سؤال ", "Question ") + "${toArabicDigits(currentIdx + 1)} " + t("من ", "of ") + "${toArabicDigits(questions.size)}", style = MaterialTheme.typography.bodyMedium, color = GoldSoft)
            Text(t("النتيجة: ", "Score: ") + "${toArabicDigits(score)}", style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (currentIdx + 1).toFloat() / questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Gold,
            trackColor = NavyLight
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth().noorGlassCard(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = q.q,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    q.options.forEachIndexed { i, opt ->
                        val isCorrect = i == q.answer
                        val isSelected = i == selectedIdx

                        val containerColor = when {
                            isAnswered -> {
                                if (isCorrect) Color(0xFF2E7D32)
                                else if (isSelected) Color(0xFFC62828)
                                else NavyLight.copy(alpha = 0.5f)
                            }
                            else -> NavyLight
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isAnswered) {
                                    selectedIdx = i
                                    if (i == q.answer) score++
                                },
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(opt, style = MaterialTheme.typography.bodyLarge, color = TextMain, modifier = Modifier.weight(1f))
                                if (isAnswered) {
                                    if (isCorrect) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    else if (isSelected) Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isAnswered) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (currentIdx + 1 >= questions.size) {
                        isFinished = true
                    } else {
                        currentIdx++
                        selectedIdx = null
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = if (currentIdx + 1 >= questions.size) t("عرض النتيجة", "Show Result") else t("السؤال التالي", "Next Question"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun loadHajjQuizQuestions(context: Context): List<HajjQuizQuestion> {
    val list = mutableListOf<HajjQuizQuestion>()
    try {
        val jsonStr = context.assets.open("data/hajjQuiz.json").bufferedReader().use { it.readText() }
        val arr = JSONObject(jsonStr).getJSONArray("hajjQuizQuestions")
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val optsArr = obj.getJSONArray("options")
            val opts = List(optsArr.length()) { j -> optsArr.getString(j) }
            list.add(HajjQuizQuestion(obj.getString("q"), opts, obj.getInt("answer")))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}
