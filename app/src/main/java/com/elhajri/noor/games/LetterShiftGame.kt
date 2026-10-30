package com.elhajri.noor.games
import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard

/**
 * Game 25: Letter Shift Ladder (سلسلة الحروف)
 * Category: كلمات
 * Mechanic: Combination lock letter reels. Shift vertical letter columns up or down until the target word aligns across the center frame.
 */
private data class ShiftReelLevel(
    val id: Int,
    val categoryHint: String,
    val targetWord: String,
    val reels: List<List<Char>>
)

@Composable
fun LetterShiftGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 120000L, targetScore = 300)

    val levels = remember {
        listOf(
            ShiftReelLevel(
                id = 1,
                categoryHint = "أجواء رمضان المبارك",
                targetWord = "سحور",
                reels = listOf(
                    listOf('ف', 'س', 'ن', 'م'),
                    listOf('ج', 'ح', 'ع', 'ل'),
                    listOf('ا', 'و', 'ي', 'ر'),
                    listOf('د', 'ر', 'ز', 'ة')
                )
            ),
            ShiftReelLevel(
                id = 2,
                categoryHint = "عَمود الدين وركن الإسلام",
                targetWord = "صلاة",
                reels = listOf(
                    listOf('ح', 'ص', 'م', 'ط'),
                    listOf('ك', 'ل', 'ع', 'ن'),
                    listOf('ا', 'و', 'ي', 'ب'),
                    listOf('ت', 'ة', 'ه', 'م')
                )
            ),
            ShiftReelLevel(
                id = 3,
                categoryHint = "زينة وأنوار شهر الصيام",
                targetWord = "فانوس",
                reels = listOf(
                    listOf('ق', 'ف', 'م', 'س'),
                    listOf('ا', 'و', 'ي', 'ل'),
                    listOf('م', 'ن', 'ش', 'ك'),
                    listOf('ا', 'و', 'ر', 'ح'),
                    listOf('ص', 'س', 'ت', 'د')
                )
            )
        )
    }

    var currentLevelIdx by remember { mutableIntStateOf(0) }
    val currentLevel = levels[currentLevelIdx.coerceIn(0, levels.size - 1)]

    val reelIndices = remember { mutableStateListOf<Int>() }
    var movesCount by remember { mutableIntStateOf(0) }
    var isUnlocked by remember { mutableStateOf(false) }
    val particles = remember { ParticleSystem() }

    fun initLevel() {
        reelIndices.clear()
        // Offset starting indices so target word is not aligned initially
        currentLevel.reels.forEach { reel ->
            reelIndices.add((1 until reel.size).random())
        }
        movesCount = 0
        isUnlocked = false
        engineState.startGame()
    }

    LaunchedEffect(currentLevelIdx) {
        initLevel()
    }

    fun checkAlignment() {
        val activeWord = currentLevel.reels.indices.map { col ->
            val idx = reelIndices[col]
            currentLevel.reels[col][idx]
        }.joinToString("")

        if (activeWord == currentLevel.targetWord && !isUnlocked) {
            isUnlocked = true
            engineState.addScore(150)
            SfxManager.playCelebration(context)
            triggerGameHaptic(context, GameHapticType.SUCCESS)

            if (currentLevelIdx < levels.size - 1) {
                currentLevelIdx++
            } else {
                engineState.completeLevel(context, 3)
            }
        }
    }

    fun shiftReel(col: Int, delta: Int) {
        if (engineState.playState != GamePlayState.PLAYING || isUnlocked) return
        val reelSize = currentLevel.reels[col].size
        val newIdx = (reelIndices[col] + delta + reelSize) % reelSize
        reelIndices[col] = newIdx
        movesCount++
        SfxManager.playTap(context)
        triggerGameHaptic(context, GameHapticType.LIGHT)
        checkAlignment()
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameHeaderBar(
                title = t("سلسلة الحروف", "Letter Shift Ladder"),
                level = currentLevelIdx + 1,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            // Hint Card & Move Count Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .noorGlassCard(cornerRadius = 16.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t("التلميح: ", "Hint: ") + currentLevel.categoryHint,
                            color = theme.accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = t("النقلات: ", "Moves: ") + movesCount.toArabicDigits(),
                            color = theme.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = t("انقل أسطوانات الحروف محاذاة الكلمة الصحيحة في الخط الأوسط", "Shift letter reels to align target word in the middle frame"),
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Current Formed Word Status Card
            val activeWordPreview = currentLevel.reels.indices.map { col ->
                val idx = reelIndices.getOrElse(col) { 0 }
                currentLevel.reels[col][idx]
            }.joinToString("")

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isUnlocked) Color(0xFF4CAF50) else theme.surface,
                border = BorderStroke(2.dp, if (isUnlocked) Color(0xFF81C784) else theme.accent.copy(alpha = 0.5f)),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isUnlocked) Color.White else theme.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = activeWordPreview,
                        color = if (isUnlocked) Color.White else theme.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Combination Lock Tumbler UI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background Active Frame Highlight (Middle Horizontal Bar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            color = if (isUnlocked) Color(0xFF4CAF50).copy(alpha = 0.25f) else theme.accent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = if (isUnlocked) Color(0xFF4CAF50) else theme.accent,
                            shape = RoundedCornerShape(16.dp)
                        )
                )

                // Reel Columns
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    currentLevel.reels.forEachIndexed { colIdx, reel ->
                        val currentIdx = reelIndices.getOrElse(colIdx) { 0 }
                        val prevIdx = (currentIdx - 1 + reel.size) % reel.size
                        val nextIdx = (currentIdx + 1) % reel.size

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Up Arrow Button
                            IconButton(
                                onClick = { shiftReel(colIdx, -1) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(theme.surface.copy(alpha = 0.8f), CircleShape)
                                    .border(1.dp, theme.accent.copy(alpha = 0.4f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Shift Up",
                                    tint = theme.accent
                                )
                            }

                            // Reel Cylinder Box (3 visible items)
                            Column(
                                modifier = Modifier
                                    .width(56.dp)
                                    .background(theme.surface.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .border(1.dp, theme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Top Character (Preview)
                                Text(
                                    text = reel[prevIdx].toString(),
                                    color = theme.textSecondary.copy(alpha = 0.4f),
                                    fontSize = 18.sp
                                )

                                // Middle Character (Active Alignment)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(theme.accent.copy(alpha = 0.3f), CircleShape)
                                        .border(2.dp, theme.accent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = reel[currentIdx].toString(),
                                        color = theme.textPrimary,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Bottom Character (Preview)
                                Text(
                                    text = reel[nextIdx].toString(),
                                    color = theme.textSecondary.copy(alpha = 0.4f),
                                    fontSize = 18.sp
                                )
                            }

                            // Down Arrow Button
                            IconButton(
                                onClick = { shiftReel(colIdx, 1) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(theme.surface.copy(alpha = 0.8f), CircleShape)
                                    .border(1.dp, theme.accent.copy(alpha = 0.4f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Shift Down",
                                    tint = theme.accent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        when (engineState.playState) {
            GamePlayState.PAUSED -> PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { initLevel() },
                onQuit = onBack
            )
            GamePlayState.WON -> GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onRestart = { initLevel() },
                onQuit = onBack
            )
            GamePlayState.LOST -> GameLoseOverlay(
                score = engineState.score,
                onRestart = { initLevel() },
                onQuit = onBack
            )
            else -> {}
        }
    }
}
