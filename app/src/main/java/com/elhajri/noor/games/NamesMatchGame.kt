package com.elhajri.noor.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import kotlin.random.Random

/**
 * "رباط معاني الله" — ذاكرة ومطابقة: اقلب بطاقتين وحاول مطابقة اسم الله مع معناه.
 * كل مرحلة ٦ أزواج جديدة من أسماء الله الحسنى.
 */
private data class MatchCard(val pairId: Int, val label: String, val isMeaning: Boolean)

@Composable
fun NamesMatchGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var level by remember { mutableIntStateOf(0) }
    var cards by remember { mutableStateOf(listOf<MatchCard>()) }
    var flipped by remember { mutableStateOf(setOf<Int>()) }
    var matched by remember { mutableStateOf(setOf<Int>()) }
    var moves by remember { mutableIntStateOf(0) }
    var wrongPair by remember { mutableStateOf(setOf<Int>()) }

    fun setup(l: Int) {
        val names = DataLoader.namesOfAllah(context)
            .filter { it.meaning.isNotBlank() && it.name != "الله" }
            .distinctBy { it.name }
        val picks = names.shuffled(Random(l * 7 + 3)).take(6)
        val deck = picks.flatMapIndexed { i, n ->
            listOf(
                MatchCard(i, n.name, isMeaning = false),
                MatchCard(i, n.meaning, isMeaning = true)
            )
        }.shuffled(Random(l * 13 + 5))
        cards = deck
        flipped = emptySet()
        matched = emptySet()
        moves = 0
        wrongPair = emptySet()
    }
    LaunchedEffect(Unit) { setup(level) }

    val allMatched = cards.isNotEmpty() && matched.size == cards.size

    fun flip(idx: Int) {
        if (allMatched || idx in matched || idx in flipped || flipped.size >= 2) return
        flipped = flipped + idx
        if (flipped.size == 2) {
            moves++
            val (a, b) = flipped.toList()
            val ca = cards[a]; val cb = cards[b]
            if (ca.pairId == cb.pairId && ca.isMeaning != cb.isMeaning) {
                matched = matched + setOf(a, b)
                flipped = emptySet()
            } else {
                wrongPair = setOf(a, b)
            }
        }
    }
    LaunchedEffect(wrongPair) {
        if (wrongPair.isNotEmpty()) {
            kotlinx.coroutines.delay(700)
            flipped = flipped - wrongPair
            wrongPair = emptySet()
        }
    }

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold) }
                Column(Modifier.weight(1f)) {
                    Text("رباط معاني الله", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("طابق اسم الله مع معناه", color = GoldSoft, fontSize = 11.sp)
                }
                IconButton(onClick = { setup(level) }) { Icon(Icons.Default.Replay, contentDescription = "إعادة", tint = GoldSoft) }
            }
            Row(Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                Text("المرحلة ${level + 1}", color = GoldSoft, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Text("محاولات: $moves", color = GoldSoft, fontSize = 12.sp)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(cards) { idx, card ->
                    val isOpen = idx in flipped || idx in matched
                    val isWrong = idx in wrongPair
                    val pop by animateFloatAsState(
                        targetValue = if (isOpen) 1f else 0.94f,
                        animationSpec = tween(320), label = "pop"
                    )
                    val gold = idx in matched
                    Box(
                        Modifier
                            .height(108.dp)
                            .graphicsLayer {
                                scaleX = pop
                                scaleY = pop
                            }
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    gold -> Gold
                                    isWrong -> Color(0xFF7F1D1D).copy(alpha = 0.5f)
                                    isOpen -> NavyCard
                                    else -> NavyCard.copy(alpha = 0.7f)
                                }
                            )
                            .border(
                                1.dp,
                                if (gold) Gold else Gold.copy(alpha = if (isOpen) 0.5f else 0.18f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { flip(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isOpen) {
                            Text(
                                card.label,
                                color = if (gold) Navy else Color.White,
                                fontSize = if (card.isMeaning) 11.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        } else {
                            Text("﷽", color = Gold.copy(alpha = 0.30f), fontSize = 15.sp)
                        }
                    }
                }
            }

            if (allMatched) {
                Column(Modifier.fillMaxWidth().padding(bottom = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ما شاء الله! أتممت الرباط 🌙", color = Gold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold)
                            .clickable { level++; setup(level) }
                            .padding(horizontal = 30.dp, vertical = 10.dp)
                    ) {
                        Text("المستوى التالي", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
