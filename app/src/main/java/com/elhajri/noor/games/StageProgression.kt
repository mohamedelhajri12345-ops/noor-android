package com.elhajri.noor.games

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import org.json.JSONObject

/**
 * Extension helper to convert integers to Arabic-Indic digits (٠١٢٣٤٥٦٧٨٩).
 */
fun Int.toArabicDigits(): String {
    val arDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = this.toString()
    val builder = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') builder.append(arDigits[ch - '0']) else builder.append(ch)
    }
    return builder.toString()
}

/**
 * Stage data model for ordering tasks.
 */
data class GameOrderingStage(
    val id: Int,
    val title: String,
    val description: String,
    val steps: List<String>
)

/**
 * Stage progress snapshot for a game.
 */
data class StageProgressData(
    val unlockedStage: Int = 0,
    val starsMap: Map<Int, Int> = emptyMap()
)

/**
 * Store and load stage progression from SharedPreferences('noor_games') key 'levels_<GameName>'.
 */
object StageProgressStore {
    private const val PREFS_NAME = "noor_games"

    fun loadProgress(context: Context, gameName: String, totalStages: Int): StageProgressData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("levels_$gameName", null) ?: return StageProgressData(unlockedStage = 0)
        return try {
            val json = JSONObject(raw)
            val unlocked = json.optInt("unlocked", 0)
            val starsObj = json.optJSONObject("stars")
            val starsMap = mutableMapOf<Int, Int>()
            if (starsObj != null) {
                val keys = starsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val stageIdx = k.toIntOrNull()
                    if (stageIdx != null) {
                        starsMap[stageIdx] = starsObj.getInt(k)
                    }
                }
            }
            StageProgressData(
                unlockedStage = unlocked.coerceIn(0, totalStages - 1),
                starsMap = starsMap
            )
        } catch (_: Exception) {
            StageProgressData(unlockedStage = 0)
        }
    }

    fun saveProgress(context: Context, gameName: String, stageIdx: Int, stars: Int, totalStages: Int) {
        val current = loadProgress(context, gameName, totalStages)
        val newUnlocked = maxOf(current.unlockedStage, minOf(stageIdx + 1, totalStages - 1))
        val newStars = current.starsMap.toMutableMap()
        val prevStars = newStars[stageIdx] ?: 0
        if (stars > prevStars) {
            newStars[stageIdx] = stars
        }

        try {
            val json = JSONObject()
            json.put("unlocked", newUnlocked)
            val starsObj = JSONObject()
            newStars.forEach { (k, v) ->
                starsObj.put(k.toString(), v)
            }
            json.put("stars", starsObj)

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("levels_$gameName", json.toString()).apply()
        } catch (_: Exception) {
            // Ignore error gracefully
        }
    }
}

/**
 * Reusable stage map screen displaying grid of numbered glass buttons.
 */
@Composable
fun StageMapScreen(
    gameTitle: String,
    gameSubtitle: String,
    totalStages: Int,
    progressData: StageProgressData,
    onSelectStage: (Int) -> Unit,
    onBack: () -> Unit
) {
    val totalStars = progressData.starsMap.values.sum()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Gold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = gameTitle,
                        color = Gold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = gameSubtitle,
                        color = GoldSoft,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NavyCard,
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = totalStars.toArabicDigits(),
                            color = Gold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 16.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "اختر المرحلة لبدء التحدي والترتيب الصحيح",
                    color = TextMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stage Buttons Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(List(totalStages) { it }) { stageIdx, _ ->
                    val isUnlocked = stageIdx <= progressData.unlockedStage
                    val stars = progressData.starsMap[stageIdx] ?: 0

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .then(
                                if (isUnlocked) {
                                    Modifier
                                        .noorGlassCard(cornerRadius = 18.dp)
                                        .border(2.dp, Gold, RoundedCornerShape(18.dp))
                                        .clickable { onSelectStage(stageIdx) }
                                } else {
                                    Modifier
                                        .noorGlassCard(cornerRadius = 18.dp)
                                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isUnlocked) {
                                Text(
                                    text = (stageIdx + 1).toArabicDigits(),
                                    color = Gold,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (s in 1..3) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (s <= stars) Gold else GoldSoft.copy(alpha = 0.25f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "مغلق",
                                    tint = TextSecondaryLinen.copy(alpha = 0.5f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = (stageIdx + 1).toArabicDigits(),
                                    color = TextSecondaryLinen.copy(alpha = 0.5f),
                                    fontSize = 12.sp
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
 * Reusable stage win dialog delegating to WinCelebrationOverlay with gold star burst & confetti.
 */
@Composable
fun StageWinDialog(
    stageNumber: Int,
    totalStages: Int,
    stars: Int,
    stageTitle: String,
    onNextStage: () -> Unit,
    onReplayStage: () -> Unit,
    onStageMap: () -> Unit
) {
    WinCelebrationOverlay(
        stageNumber = stageNumber,
        totalStages = totalStages,
        stars = stars,
        stageTitle = stageTitle,
        onNextStage = onNextStage,
        onReplayStage = onReplayStage,
        onStageMap = onStageMap
    )
}
