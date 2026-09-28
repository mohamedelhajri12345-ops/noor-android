package com.elhajri.noor.hajj

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

private fun toArabicDigits(number: Any): String {
    val str = number.toString()
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return str.map { ch ->
        if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
    }.joinToString("")
}

private data class Step(val title: String, val desc: String)
private data class Dua(val text: String, val ref: String)
private data class HajjQuizQuestion(val q: String, val options: List<String>, val answer: Int)

private val umrahSteps = listOf(
    Step("الإحرام", "الإحرام هو نية الدخول في النسك. يُستحب الاغتسال والتطيب قبل الإحرام. يلبس الرجل إزاراً ورداءً أبيضين، أما المرأة فتلبس ما تشاء من ثيابها الشرعية. ثم ينوي بقلبه ويلبي بلسانه قائلاً: لبيك عمرة. ويبدأ بالتلبية: لبيك اللهم لبيك، لبيك لا شريك لك لبيك، إن الحمد والنعمة لك والملك، لا شريك لك."),
    Step("الطواف", "بعد الوصول للمسجد الحرام، يطوف حول الكعبة سبعة أشواط. يبدأ الطواف من الحجر الأسود ويجعل الكعبة عن يساره. يُستحب للرجل أن يسرع المشي في الأشواط الثلاثة الأولى (الرمل) ويكشف كتفه الأيمن (الاضطباع). يبدأ كل شوط بالحجر الأسود وينتهي به. يُستحب مسح الحجر الأسود إن أمكن، وإلا أشار إليه."),
    Step("السعي", "بعد الطواف، يخرج إلى الصفا ويسعى بين الصفا والمروة سبعة أشواط. يبدأ من الصفا ويمشي إلى المروة (شوط أول)، ثم يعود من المروة إلى الصفا (شوط ثاني)، وهكذا حتى يكمل سبعة أشواط. يُستحب الإسراع بين العلمين الأخضرين للرجال. ينتهي السعي عند المروة."),
    Step("الحلق أو التقصير", "بعد إكمال السعي، يحلق المحرم شعر رأسه أو يقصره. الحلق أفضل للرجال، أما المرأة فتقصر من أطراف شعرها بمقدار أنملة. وبذلك تتم العمرة ويتحلل المحرم من إحرامه، ويباح له كل ما كان محظوراً بالإحرام.")
)

private val hajjSteps = listOf(
    Step("الإحرام من الميقات", "يحرم الحاج من الميقات الذي يمر به. ينوي الحج بقلبه ويلبي بلسانه. إذا كان متمتعاً (أدى عمرة قبل الحج) فإنه يحرم بالحج في يوم التروية (٨ ذو الحجة) من مكة. يلبس ثياب الإحرام ويبدأ بالتلبية."),
    Step("طواف القدوم", "بعد الوصول لمكة، يطوف الحاج طواف القدوم سبعة أشواط حول الكعبة. هذا الطواف سنة للحاج المفرد والقارن. يصلي ركعتين خلف مقام إبراهيم بعد الطواف."),
    Step("السعي", "بعد طواف القدوم، يسعى الحاج بين الصفا والمروة سبعة أشواط. يبدأ من الصفا وينتهي عند المروة. المتمتع يسعى سعياً واحداً عن عمرته وحجه."),
    Step("التوجه إلى منى (٨ ذو الحجة)", "في يوم التروية (٨ ذو الحجة) يتوجه الحاج إلى منى قبل الزوال. يصلي الظهر والعصر والمغرب والعشاء والفجر قصراً (الرباعية ركعتين) دون جمع. يبيت في منى ليلة عرفة استعداداً ليوم الوقوف بعرفة."),
    Step("يوم عرفة (٩ ذو الحجة)", "هو أعظم أركان الحج. بعد شروق شمس يوم عرفة، يتوجه الحاج من منى إلى عرفة. يقف بعرفة بعد زوال الشمس حتى غروبها. يُكثر من الدعاء والتلبية والذكر. يجب الوقوف داخل حدود عرفة وليس في وادي عرنة. إذا لم يقف الحاج بعرفة فلا حج له."),
    Step("المبيت بمزدلفة", "بعد غروب شمس يوم عرفة، يتوجه الحاج إلى مزدلفة. يصلي المغرب والعشاء جمعاً وقصراً. يبيت في مزدلفة حتى الفجر. يلتقط ٧٠ حصاة لرمي الجمرات. يجوز للضعفاء والنساء الانصراف بعد منتصف الليل."),
    Step("رمي جمرة العقبة (١٠ ذو الحجة)", "بعد الفجر في يوم النحر (١٠ ذو الحجة)، يتوجه الحاج إلى منى. يرمي جمرة العقبة الكبرى بسبع حصيات متعاقبات، يكبر مع كل حصاة. بعد الرمي يذبح الهدي (للمتمتع والقارن)، ثم يحلق أو يقصر. بذلك يتحلل التحلل الأول، فيباح له كل شيء إلا النساء."),
    Step("طواف الإفاضة", "بعد الرمي والحلق، يذهب الحاج إلى مكة لطواف الإفاضة (طواف الزيارة) سبعة أشواط. هذا الطواف ركن من أركان الحج. بعده يتحلل التحلل الثاني فيباح له كل شيء بما في ذلك النساء. إن لم يكن قد سعى قبل، يسعى بعد هذا الطواف."),
    Step("أيام التشريق (١١-١٣ ذو الحجة)", "في أيام التشريق الثلاثة (١١-١٣ ذو الحجة)، يبيت الحاج في منى. يرمي الجمرات الثلاث كل يوم بعد الزوال: الصغرى ثم الوسطى ثم الكبرى (العقبة)، كل واحدة بسبع حصيات. يجوز التعجيل (الانصراف بعد يومين) أو التأخير (المبيت لليوم الثالث)."),
    Step("طواف الوداع", "آخر أعمال الحج. قبل مغادرة مكة، يطوف الحاج طواف الوداع سبعة أشواط حول الكعبة. هذا الطواف واجب لكل حاج إلا الحائض. بعده يغادر مكة. وبذلك يكتمل الحج.")
)

private val duas = listOf(
    Dua("لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ", "التلبية"),
    Dua("رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ", "دعاء الطواف")
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
            .background(NoorGradients.ScreenBackground)
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
                    contentDescription = "رجوع",
                    tint = Gold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isQuizMode) "اختبار المناسك" else "دليل الحج والعمرة",
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
                        text = "دليل الحج والعمرة",
                        style = MaterialTheme.typography.titleLarge,
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "اقرأ الخطوات واضغط \"فهمت\" لإتمام الاختبار",
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
                    TabButton("العمرة", selectedTab == "umrah", Modifier.weight(1f)) { selectedTab = "umrah" }
                    TabButton("الحج", selectedTab == "hajj", Modifier.weight(1f)) { selectedTab = "hajj" }
                    TabButton("أدعية", selectedTab == "duas", Modifier.weight(1f)) { selectedTab = "duas" }
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
                                        text = "ابدأ اختبار ${if (selectedTab == "umrah") "العمرة" else "الحج"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "أكمل قراءة جميع الخطوات واضغط \"فهمت\" لفتح الاختبار",
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
                            text = "هذا دليل مبسط. يُنصح بمراجعة كتب المناسك المتخصصة واستشارة العلماء.",
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
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
                        text = step.title,
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
                        text = step.desc,
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
                            Text("فهمت", fontWeight = FontWeight.Bold)
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
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
                text = dua.ref,
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
            Text("لا توجد أسئلة متاح حالياً", color = GoldSoft)
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
            Text("انتهى الاختبار", style = MaterialTheme.typography.headlineMedium, color = Gold, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${toArabicDigits(score)} / ${toArabicDigits(questions.size)}",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (pct >= 80) "ممتاز! أداء رائع 🌟" else if (pct >= 60) "جيد جداً 👍" else "تحتاج لمراجعة الخطوات 📚",
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
                Text("إعادة الاختبار", fontWeight = FontWeight.Bold)
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
            Text("سؤال ${toArabicDigits(currentIdx + 1)} من ${toArabicDigits(questions.size)}", style = MaterialTheme.typography.bodyMedium, color = GoldSoft)
            Text("النتيجة: ${toArabicDigits(score)}", style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
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
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
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
                                Text(opt, style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.weight(1f))
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
                    text = if (currentIdx + 1 >= questions.size) "عرض النتيجة" else "السؤال التالي",
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
