package com.elhajri.noor.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import java.util.Calendar

/**
 * "كلمة من الذكر" — لغز يومي بأسلوب Wordle: تخمين اسم من أسماء الله الحسنى.
 * الذهبي: حرف صحيح في مكانه. الأصفر الفاتح: موجود بمكان آخر. الكحلي: غير موجود.
 * كلمة جديدة كل يوم مع سلسلة انتصارات يومية.
 */
private data class Kalima(val word: String, val meaning: String, val dalil: String)

private val kalimat = listOf(
    Kalima("الرزاق", "المتكفل بأرزاق العباد جميعاً", "إِنَّ اللَّهَ هُوَ الرَّزَّاقُ ذُو الْقُوَّةِ الْمَتِينُ"),
    Kalima("الوهاب", "كثير العطاء بلا مقابل", "إِنَّكَ لَأَنتَ الْهَادِي إِلَى صِرَاطٍ مُّسْتَقِيمٍ"),
    Kalima("الفتاح", "الذي يفتح أبواب الرحمة والرزق لعباده", "مَّا يَفْتَحِ اللَّهُ لِلنَّاسِ مِن رَّحْمَةٍ فَلَا مُمْسِكَ لَهَا"),
    Kalima("العليم", "المحيط علمه بكل شيء ظاهراً وباطناً", "وَهُوَ الْعَلِيمُ الْحَكِيمُ"),
    Kalima("الكريم", "الذي يعطي بلا سؤال ولا يبالي", "يَا أَيُّهَا الْإِنسَانُ مَا غَرَّكَ بِرَبِّكَ الْكَرِيمِ"),
    Kalima("الودود", "يحب عباده الصالحين ويحبونه", "وَاسْتَغْفِرُوا رَبَّكُمْ ثُمَّ تُوبُوا إِلَيْهِ إِنَّ رَبِّي رَحِيمٌ وَدُودٌ"),
    Kalima("الشكور", "يجزي على القليل بالكثير ويثبت عباده", "إِنَّا كُلَّ شَيْءٍ خَلَقْنَاهُ بِقَدَرٍ"),
    Kalima("الحفيظ", "يحفظ عباده وأعمالهم ولا يضيع عنده شيء", "إِنَّ رَبِّي عَلَىٰ كُلِّ شَيْءٍ حَفِيظٌ"),
    Kalima("النصير", "الناصر لأوليائه على أعدائهم", "وَكَفَىٰ بِرَبِّكَ وَلِيًّا وَنَصِيرًا"),
    Kalima("الوكيل", "المتكفل بمصالح خلقه بأحسن تدبير", "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ"),
    Kalima("القوي", "كامل القوة لا يعجزه شيء", "إِنَّ اللَّهَ هُوَ الرَّزَّاقُ ذُو الْقُوَّةِ الْمَتِينُ"),
    Kalima("المتين", "شديد القوة لا تنقطع قدرته", "ذُو الْقُوَّةِ الْمَتِينِ"),
    Kalima("الحكيم", "يضع كل شيء في محله بأحسن حكمة", "أَلَا لَهُ الْخَلْقُ وَالْأَمْرُ ۚ تَبَارَكَ اللَّهُ رَبُّ الْعَالَمِينَ"),
    Kalima("الخبير", "يعلم دقائق الأمور وخفاياها", "وَهُوَ الْخَبِيرُ الْخَبِيرُ"),
    Kalima("اللطيف", "يصل لطفه لعباده من طرق لا يشعرون بها", "اللَّهُ لَطِيفٌ بِعِبَادِهِ"),
    Kalima("الشهيد", "الحاضر الذي لا تغيب عنه غائبة", "وَاللَّهُ عَلَىٰ كُلِّ شَيْءٍ شَهِيدٌ"),
    Kalima("المغني", "يغني من يشاء من فضله", "وَإِن يَتَفَرَّقُوا يُغْنِ كُلًّا مِّن سَعَتِهِ"),
    Kalima("الحسيب", "الكافي عباده حسابه", "وَكَفَىٰ بِاللَّهِ حَسِيبًا"),
    Kalima("البصير", "يرى كل شيء وإن دق", "أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ"),
    Kalima("السلام", "السالم من كل نقص والعافية من عنده", "هُوَ اللَّهُ الَّذِي لَا إِلَٰهَ إِلَّا هُوَ الْمَلِكُ الْقُدُّوسُ السَّلَامُ"),
    Kalima("العزيز", "الغالب الذي لا يُقهر", "وَلِلَّهِ الْعِزَّةُ وَلِرَسُولِهِ"),
    Kalima("الجبار", "الذي يجبر الكسير ويقهر الجبارين", "وَهُوَ الْجَبَّارُ الْمُتَكَبِّرُ"),
    Kalima("الرؤوف", "شديد الرحمة والعطف على عباده", "إِنَّ رَبَّكُمْ لَذُو رَحْمَةٍ وَاسِعَةٍ"),
    Kalima("الماجد", "واسع الكرم عظيم الشأن", "قُلْ إِنَّمَا أَنَا بَشَرٌ مِّثْلُكُمْ")
)

// لوحة المفاتيح العربية
private val row1 = "ضصثقفغعهخحجد".map { it.toString() }
private val row2 = "شسيبلاتنمكط".map { it.toString() }
private val row3 = listOf("ئ","ء","ؤ","ر","ى","ة","و","ز","ظ")

@Composable
fun KalimatGameScreen(onBack: () -> Unit) {
    val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val target = kalimat[dayIndex % kalimat.size]
    val len = target.word.length
    val maxTries = 6

    var guesses by remember { mutableStateOf(listOf<String>()) }
    var current by remember { mutableStateOf("") }
    var won by remember { mutableStateOf(false) }
    var lost by remember { mutableStateOf(false) }
    var shake by remember { mutableStateOf(false) }

    fun submit() {
        if (current.length != len) { shake = true; return }
        guesses = guesses + current
        if (current == target.word) won = true
        else if (guesses.size >= maxTries) lost = true
        current = ""
    }

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("كلمة من الذكر", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("لغز اليوم — اسم من أسماء الله الحسنى", color = GoldSoft, fontSize = 11.sp)
                }
                Spacer(Modifier.width(48.dp))
            }
            Spacer(Modifier.height(18.dp))

            // شبكة المحاولات
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (r in 0 until maxTries) {
                    val rowWord = when {
                        r < guesses.size -> guesses[r]
                        r == guesses.size && !won && !lost -> current
                        else -> ""
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (c in 0 until len) {
                            val ch = rowWord.getOrNull(c)?.toString() ?: ""
                            val (bg, fg) = when {
                                r < guesses.size -> {
                                    val t = target.word
                                    when {
                                        t.getOrNull(c)?.toString() == ch -> Color(0xFFD4AF37) to Navy
                                        ch.isNotEmpty() && t.contains(ch) -> Color(0xFFF3E5AB) to Navy
                                        ch.isNotEmpty() -> NavyCard to Color.White.copy(alpha = 0.35f)
                                        else -> NavyCard to Color.Transparent
                                    }
                                }
                                else -> NavyCard to if (ch.isEmpty()) Color.Transparent else Color.White
                            }
                            val flip by animateFloatAsState(
                                targetValue = if (r < guesses.size) 1f else 0f,
                                animationSpec = tween(400), label = "flip"
                            )
                            Box(
                                Modifier
                                    .size(46.dp)
                                    .graphicsLayer { rotationX = (1f - flip) * 90f }
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(ch, color = fg, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            if (won || lost) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 10.dp)) {
                    Text(if (won) "أحسنت! 🌙" else "الكلمة كانت: ${target.word}", color = if (won) Gold else Color.White.copy(alpha = 0.8f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(target.meaning, color = GoldSoft, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Text("﴿${target.dalil}﴾", color = GoldSoft.copy(alpha = 0.75f), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                }
            } else {
                ArabicKeyboard(
                    onLetter = { if (current.length < len) current += it },
                    onDelete = { if (current.isNotEmpty()) current = current.dropLast(1) },
                    onEnter = { submit() },
                    target = target.word,
                    guesses = guesses
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ArabicKeyboard(
    onLetter: (String) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit,
    target: String,
    guesses: List<String>
) {
    fun letterState(ch: String): Color {
        val inTarget = target.contains(ch)
        val correctSpot = guesses.any { g -> g.mapIndexedNotNull { i, c -> if (c.toString() == ch && target.getOrNull(i)?.toString() == ch) ch else null }.isNotEmpty() }
        val inAny = guesses.any { it.contains(ch) }
        return when {
            correctSpot -> Color(0xFFD4AF37)
            inTarget && inAny -> Color(0xFFF3E5AB)
            inAny -> NavyCard
            else -> NavyLight
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        listOf(row1, row2, row3).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                row.forEach { ch ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(letterState(ch))
                            .clickable { onLetter(ch) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ch, color = if (letterState(ch) == NavyCard) Color.White.copy(alpha = 0.3f) else Navy, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.clip(RoundedCornerShape(8.dp)).background(NavyCard).clickable { onDelete() }.padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Backspace, contentDescription = "حذف", tint = Gold, modifier = Modifier.size(20.dp))
            }
            Box(
                Modifier.clip(RoundedCornerShape(8.dp)).background(Gold).clickable { onEnter() }.padding(horizontal = 26.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("تخمين", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
