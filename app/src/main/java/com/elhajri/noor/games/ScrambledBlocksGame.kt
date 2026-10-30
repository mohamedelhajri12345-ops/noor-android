package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Game 23: Scrambled Letter Blocks (مكعبات الحروف)
 * Category: كلمات
 * Mechanic: Anagram block unscrambler. Drag scrambled Arabic wooden letter blocks
 * from bottom tray into correct ordered slots to assemble target words.
 */
private data class ScrambledWord(
    val word: String,
    val hintAr: String
)

private data class LetterBlock(
    val id: Int,
    val char: Char,
    val targetIndex: Int,
    var currentSlot: Int = -1, // -1 means in tray
    var trayPos: Offset = Offset.Zero,
    var currentPos: Offset = Offset.Zero,
    var isDragging: Boolean = false
)

@Composable
fun ScrambledBlocksGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 60000L, targetScore = 150)

    val wordList = remember {
        listOf(
            ScrambledWord("مكة", "العاصمة المقدسة ومهمد الإسلام"),
            ScrambledWord("نور", "الضياء والهداية الإلهية"),
            ScrambledWord("سلام", "شعار الإسلام والأمان"),
            ScrambledWord("صيام", "ركن الرابعة من أركان الإسلام"),
            ScrambledWord("مسجد", "بيت الله ومكان الصلاة"),
            ScrambledWord("رمضان", "شهر القرآن والرحمة"),
            ScrambledWord("إيمان", "التصديق بالقلب والإقرار باللسان"),
            ScrambledWord("القرآن", "كتاب الله المجيد المعجز")
        )
    }

    var wordIndex by remember { mutableStateOf(0) }
    var currentWord by remember { mutableStateOf(wordList[0]) }
    val blocks = remember { mutableStateListOf<LetterBlock>() }
    val particles = remember { ParticleSystem() }

    var activeBlockId by remember { mutableStateOf<Int?>(null) }
    var isCheckingWord by remember { mutableStateOf(false) }

    fun setupWordRound(index: Int) {
        val wordData = wordList[index % wordList.size]
        currentWord = wordData
        blocks.clear()

        val chars = wordData.word.toCharArray()
        val scrambledIndices = chars.indices.shuffled()

        // Create letter blocks
        chars.forEachIndexed { i, ch ->
            val trayIdx = scrambledIndices.indexOf(i)
            blocks.add(
                LetterBlock(
                    id = i,
                    char = ch,
                    targetIndex = i,
                    currentSlot = -1,
                    trayPos = Offset.Zero,
                    currentPos = Offset.Zero
                )
            )
        }
        isCheckingWord = false
    }

    fun initGame() {
        wordIndex = 0
        setupWordRound(0)
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun checkWordCompletion() {
        if (blocks.any { it.currentSlot == -1 }) return

        val assembledChars = CharArray(currentWord.word.length)
        blocks.forEach { b ->
            if (b.currentSlot in assembledChars.indices) {
                assembledChars[b.currentSlot] = b.char
            }
        }
        val assembledString = String(assembledChars)

        if (assembledString == currentWord.word) {
            // Correct word!
            particles.spawnBurst(Offset(350f, 250f), count = 45)
            triggerGameHaptic(context, GameHapticType.SUCCESS)
            SfxManager.playSuccess(context)
            engineState.addScore(25 + currentWord.word.length * 5)

            if (engineState.score >= engineState.targetScore) {
                engineState.completeLevel(context, 3)
            } else {
                wordIndex++
                setupWordRound(wordIndex)
            }
        } else {
            // Wrong attempt - return blocks to tray with feedback
            triggerGameHaptic(context, GameHapticType.LIGHT)
            SfxManager.playTap(context)
            blocks.forEach { b ->
                b.currentSlot = -1
                b.currentPos = b.trayPos
            }
        }
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
                title = t("مكعبات الحروف", "Scrambled Blocks"),
                level = wordIndex + 1,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = currentWord.hintAr,
                color = theme.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Text(
                text = t("اسحب مكعبات الحروف الخشبية إلى الخانات العلوية لتركيب الكلمة الصحيحة!", "Drag wooden letter blocks to the top slots to assemble the word!"),
                color = theme.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
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
                        .pointerInput(currentWord.word) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (engineState.playState != GamePlayState.PLAYING) return@detectDragGestures
                                    val block = blocks.find { b ->
                                        val center = b.currentPos
                                        hypot(offset.x - center.x, offset.y - center.y) < 40f
                                    }
                                    if (block != null) {
                                        activeBlockId = block.id
                                        block.isDragging = true
                                        if (block.currentSlot != -1) {
                                            block.currentSlot = -1
                                        }
                                        triggerGameHaptic(context, GameHapticType.LIGHT)
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val id = activeBlockId ?: return@detectDragGestures
                                    val b = blocks.find { it.id == id } ?: return@detectDragGestures
                                    b.currentPos += dragAmount
                                },
                                onDragEnd = {
                                    val id = activeBlockId ?: return@detectDragGestures
                                    val b = blocks.find { it.id == id } ?: return@detectDragGestures
                                    b.isDragging = false
                                    activeBlockId = null

                                    // Check snap to slots
                                    val wordLen = currentWord.word.length
                                    val slotWidth = 64f
                                    val slotGap = 12f
                                    val totalWidth = wordLen * slotWidth + (wordLen - 1) * slotGap
                                    val startX = (size.width - totalWidth) / 2f
                                    val slotY = 160f

                                    var snappedSlot = -1
                                    for (s in 0 until wordLen) {
                                        val sx = startX + s * (slotWidth + slotGap) + slotWidth / 2f
                                        val sy = slotY + slotWidth / 2f
                                        if (hypot(b.currentPos.x - sx, b.currentPos.y - sy) < 50f) {
                                            snappedSlot = s
                                            break
                                        }
                                    }

                                    if (snappedSlot != -1) {
                                        // Evict block if slot occupied
                                        blocks.find { it.currentSlot == snappedSlot && it.id != b.id }?.let { existing ->
                                            existing.currentSlot = -1
                                            existing.currentPos = existing.trayPos
                                        }

                                        b.currentSlot = snappedSlot
                                        val sx = startX + snappedSlot * (slotWidth + slotGap) + slotWidth / 2f
                                        val sy = slotY + slotWidth / 2f
                                        b.currentPos = Offset(sx, sy)
                                        SfxManager.playTap(context)
                                        triggerGameHaptic(context, GameHapticType.LIGHT)

                                        checkWordCompletion()
                                    } else {
                                        // Return to tray
                                        b.currentSlot = -1
                                        b.currentPos = b.trayPos
                                    }
                                }
                            )
                        }
                ) {
                    val canvasW = size.width
                    val wordLen = currentWord.word.length

                    // Compute layout slots
                    val slotWidth = 64f
                    val slotGap = 12f
                    val totalWidth = wordLen * slotWidth + (wordLen - 1) * slotGap
                    val startX = (canvasW - totalWidth) / 2f
                    val slotY = 160f

                    // Draw Slot Boxes
                    for (i in 0 until wordLen) {
                        val sx = startX + i * (slotWidth + slotGap)
                        drawRoundRect(
                            color = theme.accent.copy(alpha = 0.15f),
                            topLeft = Offset(sx, slotY),
                            size = Size(slotWidth, slotWidth),
                            cornerRadius = CornerRadius(14f)
                        )
                        drawRoundRect(
                            color = theme.accent.copy(alpha = 0.5f),
                            topLeft = Offset(sx, slotY),
                            size = Size(slotWidth, slotWidth),
                            cornerRadius = CornerRadius(14f),
                            style = Stroke(width = 2f)
                        )
                    }

                    // Tray Area
                    val trayY = size.height - 180f
                    val trayHeight = 110f
                    drawRoundRect(
                        color = Color(0xFF3E2723).copy(alpha = 0.4f),
                        topLeft = Offset(20f, trayY),
                        size = Size(canvasW - 40f, trayHeight),
                        cornerRadius = CornerRadius(20f)
                    )

                    // Position tray blocks initially
                    val trayBlockGap = (canvasW - 80f) / (wordLen + 1)
                    blocks.forEachIndexed { idx, b ->
                        val tx = 40f + (idx + 1) * trayBlockGap
                        val ty = trayY + trayHeight / 2f
                        b.trayPos = Offset(tx, ty)

                        if (b.currentSlot == -1 && !b.isDragging) {
                            b.currentPos = b.trayPos
                        }
                    }

                    // Draw Letter Blocks
                    blocks.forEach { b ->
                        val isDragged = b.isDragging
                        val scale = if (isDragged) 1.15f else 1.0f
                        val bw = slotWidth * scale
                        val bh = slotWidth * scale
                        val topLeft = Offset(b.currentPos.x - bw / 2f, b.currentPos.y - bh / 2f)

                        // Block wooden base
                        drawRoundRect(
                            color = Color(0xFFD7CCC8),
                            topLeft = topLeft,
                            size = Size(bw, bh),
                            cornerRadius = CornerRadius(14f * scale)
                        )
                        drawRoundRect(
                            color = Color(0xFF8D6E63),
                            topLeft = topLeft,
                            size = Size(bw, bh),
                            cornerRadius = CornerRadius(14f * scale),
                            style = Stroke(width = 3f * scale)
                        )

                        // Draw Arabic Letter Text
                        val textResult = textMeasurer.measure(
                            text = b.char.toString(),
                            style = TextStyle(
                                color = Color(0xFF3E2723),
                                fontSize = (26 * scale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = textResult,
                            topLeft = Offset(
                                b.currentPos.x - textResult.size.width / 2f,
                                b.currentPos.y - textResult.size.height / 2f
                            )
                        )
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
