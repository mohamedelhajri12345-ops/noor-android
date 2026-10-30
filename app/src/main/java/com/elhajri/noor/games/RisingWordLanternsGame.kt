package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * Game 27: Rising Word Lanterns (فوانيس الحروف)
 * Category: كلمات
 * Mechanic: Floating letter balloon pop. Tap rising sky lanterns carrying Arabic letters
 * in the correct sequence to spell out target words before lanterns float offscreen.
 */
private data class WordLantern(
    val id: Int,
    val char: Char,
    val isDecoy: Boolean,
    var center: Offset,
    val speedY: Float,
    val wobbleFreq: Float,
    val wobbleAmp: Float,
    val lanternColor: Color,
    var isPopped: Boolean = false
)

@Composable
fun RisingWordLanternsGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 60000L, targetScore = 120)

    val wordList = remember {
        listOf("مكة", "نور", "سلام", "صيام", "رمضان", "قرآن", "كعبة")
    }

    var wordIndex by remember { mutableStateOf(0) }
    var currentWord by remember { mutableStateOf(wordList[0]) }
    var charIndex by remember { mutableStateOf(0) }

    val lanterns = remember { mutableStateListOf<WordLantern>() }
    val particles = remember { ParticleSystem() }
    var totalTimeSec by remember { mutableStateOf(0f) }
    var nextLanternId by remember { mutableStateOf(1) }

    fun spawnLantern(neededChar: Char? = null) {
        val cx = 60f + Random.nextFloat() * 500f
        val cy = 720f

        val charToUse: Char
        val isDecoy: Boolean
        if (neededChar != null) {
            charToUse = neededChar
            isDecoy = false
        } else {
            val pickNeeded = Random.nextFloat() < 0.6f && charIndex < currentWord.length
            if (pickNeeded) {
                charToUse = currentWord[charIndex]
                isDecoy = false
            } else {
                val decoyAlphabet = "أبتثجحخدذرزسشصضطظعغفقكلمنهوي"
                charToUse = decoyAlphabet[Random.nextInt(decoyAlphabet.length)]
                isDecoy = charToUse != currentWord.getOrNull(charIndex)
            }
        }

        val colors = listOf(Color(0xFFFFD700), Color(0xFFFF9800), Color(0xFF00E676), Color(0xFF80D8FF), Color(0xFFFF80AB))
        lanterns.add(
            WordLantern(
                id = nextLanternId++,
                char = charToUse,
                isDecoy = isDecoy,
                center = Offset(cx, cy),
                speedY = 90f + Random.nextFloat() * 100f,
                wobbleFreq = 1.5f + Random.nextFloat() * 2f,
                wobbleAmp = 20f + Random.nextFloat() * 20f,
                lanternColor = colors[Random.nextInt(colors.size)]
            )
        )
    }

    fun initWordRound(idx: Int) {
        currentWord = wordList[idx % wordList.size]
        charIndex = 0
        lanterns.clear()
        repeat(5) { spawnLantern() }
    }

    fun initGame() {
        wordIndex = 0
        initWordRound(0)
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun handleTap(tapPos: Offset) {
        if (engineState.playState != GamePlayState.PLAYING) return

        val targetChar = currentWord.getOrNull(charIndex) ?: return

        var handled = false
        val iterator = lanterns.iterator()
        while (iterator.hasNext()) {
            val l = iterator.next()
            if (!l.isPopped && hypot(tapPos.x - l.center.x, tapPos.y - l.center.y) < 45f) {
                l.isPopped = true
                handled = true

                if (l.char == targetChar) {
                    // Correct letter!
                    particles.spawnBurst(l.center, count = 35)
                    triggerGameHaptic(context, GameHapticType.SUCCESS)
                    SfxManager.playSuccess(context)
                    engineState.addScore(15)

                    charIndex++
                    if (charIndex >= currentWord.length) {
                        // Word complete!
                        particles.spawnBurst(Offset(300f, 250f), count = 50)
                        if (engineState.score >= engineState.targetScore) {
                            engineState.completeLevel(context, 3)
                        } else {
                            wordIndex++
                            initWordRound(wordIndex)
                        }
                    }
                } else {
                    // Wrong letter tapped
                    triggerGameHaptic(context, GameHapticType.LIGHT)
                    SfxManager.playTap(context)
                    if (engineState.loseLife()) {
                        engineState.failLevel(context)
                    }
                }
                break
            }
        }

        if (handled && lanterns.count { !it.isPopped } < 4) {
            spawnLantern()
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        totalTimeSec += deltaSec
        particles.update(deltaSec)

        if (engineState.playState == GamePlayState.PLAYING) {
            val iterator = lanterns.iterator()
            while (iterator.hasNext()) {
                val l = iterator.next()
                if (!l.isPopped) {
                    val wobbleX = sin(totalTimeSec * l.wobbleFreq) * l.wobbleAmp
                    val newY = l.center.y - l.speedY * deltaSec
                    val newX = l.center.x + wobbleX * deltaSec
                    l.center = Offset(newX, newY)

                    // Exit top check
                    if (l.center.y < -40f) {
                        l.isPopped = true
                        val needed = currentWord.getOrNull(charIndex)
                        if (l.char == needed) {
                            // Missed needed letter
                            if (engineState.loseLife()) {
                                engineState.failLevel(context)
                            }
                            spawnLantern(neededChar = needed)
                        } else {
                            spawnLantern()
                        }
                    }
                }
            }

            if (lanterns.count { !it.isPopped } < 5) {
                spawnLantern()
            }
        }
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
                title = t("فوانيس الحروف", "Rising Word Lanterns"),
                level = wordIndex + 1,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            // Display Word Progress
            Row(
                modifier = Modifier
                    .noorGlassCard(cornerRadius = 16.dp)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("الكلمة المستهدفة: ", "Target Word: "),
                    color = theme.textSecondary,
                    fontSize = 14.sp
                )
                currentWord.forEachIndexed { idx, ch ->
                    val isRevealed = idx < charIndex
                    Text(
                        text = if (isRevealed) "$ch " else "_ ",
                        color = if (isRevealed) theme.accent else theme.textSecondary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("انقر الفوانيس الصاعدة بالتسلسل الصحيح للحروف لتشكيل الكلمة!", "Tap floating lanterns carrying letters in sequence to spell the word!"),
                color = theme.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .gameGestureDetector(
                            onTap = { offset -> handleTap(offset) }
                        )
                ) {
                    lanterns.forEach { l ->
                        if (!l.isPopped) {
                            val radius = 32f
                            val center = l.center

                            // Lantern Outer Dome
                            drawCircle(
                                color = l.lanternColor,
                                radius = radius,
                                center = center
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.9f),
                                radius = radius * 0.8f,
                                center = center
                            )

                            // Inner Glow Flame
                            drawCircle(
                                color = Color(0xFFFFD54F),
                                radius = radius * 0.35f,
                                center = center
                            )

                            // Arabic Letter Text
                            val textResult = textMeasurer.measure(
                                text = l.char.toString(),
                                style = TextStyle(
                                    color = Color(0xFF3E2723),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = textResult,
                                topLeft = Offset(
                                    center.x - textResult.size.width / 2f,
                                    center.y - textResult.size.height / 2f
                                )
                            )
                        }
                    }

                    particles.draw(this)
                }
            }
        }

        when (engineState.playState) {
            GamePlayState.PAUSED -> PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { initGame() },
                onQuit = onBack
            )
            GamePlayState.WON -> GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onRestart = { initGame() },
                onQuit = onBack
            )
            GamePlayState.LOST -> GameLoseOverlay(
                score = engineState.score,
                onRestart = { initGame() },
                onQuit = onBack
            )
            else -> {}
        }
    }
}
