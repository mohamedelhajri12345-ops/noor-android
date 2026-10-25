package com.elhajri.noor.games.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.StarEconomyStore
import kotlinx.coroutines.isActive
import kotlin.math.abs

/**
 * Game state representation for NOOR interactive Canvas games.
 */
enum class GamePlayState {
    IDLE,
    PLAYING,
    PAUSED,
    WON,
    LOST
}

enum class SwipeDirection {
    UP, DOWN, LEFT, RIGHT
}

enum class GameHapticType {
    LIGHT, MEDIUM, HEAVY, SUCCESS, ERROR
}

class GameEngineState(
    initialLevel: Int = 1,
    initialLives: Int = 3,
    val timeLimitMs: Long = 0L,
    val targetScore: Int = 100
) {
    var playState by mutableStateOf(GamePlayState.IDLE)
    var score by mutableIntStateOf(0)
    var level by mutableIntStateOf(initialLevel)
    var lives by mutableIntStateOf(initialLives)
    var maxLives by mutableIntStateOf(initialLives)
    var starsEarned by mutableIntStateOf(0)
    var timeRemainingMs by mutableLongStateOf(timeLimitMs)
    var comboCount by mutableIntStateOf(0)

    fun startGame() {
        playState = GamePlayState.PLAYING
        score = 0
        lives = maxLives
        starsEarned = 0
        timeRemainingMs = timeLimitMs
        comboCount = 0
    }

    fun pauseGame() {
        if (playState == GamePlayState.PLAYING) {
            playState = GamePlayState.PAUSED
        }
    }

    fun resumeGame() {
        if (playState == GamePlayState.PAUSED) {
            playState = GamePlayState.PLAYING
        }
    }

    fun addScore(points: Int) {
        score += points
        comboCount++
    }

    fun resetCombo() {
        comboCount = 0
    }

    fun loseLife(): Boolean {
        lives--
        comboCount = 0
        if (lives <= 0) {
            playState = GamePlayState.LOST
            return true
        }
        return false
    }

    fun completeLevel(context: Context, stars: Int = 3) {
        starsEarned = stars.coerceIn(1, 3)
        playState = GamePlayState.WON
        StarEconomyStore.addStars(context, starsEarned * 10 + (score / 10))
        SfxManager.playCelebration(context)
    }

    fun failLevel(context: Context) {
        playState = GamePlayState.LOST
        SfxManager.playWrong(context)
    }

    fun nextLevel(newTimeLimitMs: Long = timeLimitMs) {
        level++
        startGame()
    }
}

@Composable
fun rememberGameEngineState(
    initialLevel: Int = 1,
    initialLives: Int = 3,
    timeLimitMs: Long = 0L,
    targetScore: Int = 100
): GameEngineState {
    return remember(initialLevel) {
        GameEngineState(
            initialLevel = initialLevel,
            initialLives = initialLives,
            timeLimitMs = timeLimitMs,
            targetScore = targetScore
        )
    }
}

/**
 * 60 FPS Game Loop ticker calling onUpdate(deltaTimeSeconds).
 */
@Composable
fun CanvasGameLoop(
    engineState: GameEngineState,
    onUpdate: (Float) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(engineState.playState) {
        if (engineState.playState != GamePlayState.PLAYING) return@LaunchedEffect

        var lastTime = withFrameNanos { it }
        while (isActive && engineState.playState == GamePlayState.PLAYING) {
            withFrameNanos { currentTime ->
                val deltaNano = currentTime - lastTime
                lastTime = currentTime
                val deltaSec = (deltaNano / 1_000_000_000f).coerceIn(0.001f, 0.1f)

                // Timer countdown
                if (engineState.timeLimitMs > 0L) {
                    val newTime = engineState.timeRemainingMs - (deltaSec * 1000).toLong()
                    if (newTime <= 0L) {
                        engineState.timeRemainingMs = 0L
                        if (engineState.score >= engineState.targetScore) {
                            engineState.completeLevel(context, 3)
                        } else {
                            engineState.failLevel(context)
                        }
                    } else {
                        engineState.timeRemainingMs = newTime
                    }
                }

                onUpdate(deltaSec)
            }
        }
    }
}

/**
 * Extension modifier for rich gesture detection in Canvas games.
 */
fun Modifier.gameGestureDetector(
    onTap: ((Offset) -> Unit)? = null,
    onDoubleTap: ((Offset) -> Unit)? = null,
    onLongPress: ((Offset) -> Unit)? = null,
    onSwipe: ((SwipeDirection) -> Unit)? = null,
    onDragStart: ((Offset) -> Unit)? = null,
    onDragMove: ((Offset, Offset) -> Unit)? = null, // currentOffset, dragAmount
    onDragEnd: (() -> Unit)? = null
): Modifier = this.pointerInput(Unit) {
    detectTapGestures(
        onTap = onTap,
        onDoubleTap = onDoubleTap,
        onLongPress = onLongPress
    )
}.pointerInput(Unit) {
    var startPos = Offset.Zero
    detectDragGestures(
        onDragStart = { offset ->
            startPos = offset
            onDragStart?.invoke(offset)
        },
        onDragEnd = {
            onDragEnd?.invoke()
        },
        onDragCancel = {
            onDragEnd?.invoke()
        },
        onDrag = { change, dragAmount ->
            change.consume()
            onDragMove?.invoke(change.position, dragAmount)

            // Detect fast swipe threshold if swipe callback present
            if (onSwipe != null) {
                val totalX = change.position.x - startPos.x
                val totalY = change.position.y - startPos.y
                if (abs(totalX) > 80f || abs(totalY) > 80f) {
                    if (abs(totalX) > abs(totalY)) {
                        if (totalX > 0) onSwipe(SwipeDirection.RIGHT) else onSwipe(SwipeDirection.LEFT)
                    } else {
                        if (totalY > 0) onSwipe(SwipeDirection.DOWN) else onSwipe(SwipeDirection.UP)
                    }
                    startPos = change.position // reset after trigger
                }
            }
        }
    )
}

/**
 * Trigger tactile haptics across Android OS versions.
 */
fun triggerGameHaptic(context: Context, type: GameHapticType) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            val vibrator = vibratorManager?.defaultVibrator
            val effect = when (type) {
                GameHapticType.LIGHT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                GameHapticType.MEDIUM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                GameHapticType.HEAVY -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                GameHapticType.SUCCESS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                GameHapticType.ERROR -> VibrationEffect.createOneShot(100L, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            val duration = when (type) {
                GameHapticType.LIGHT -> 15L
                GameHapticType.MEDIUM -> 30L
                GameHapticType.HEAVY -> 50L
                GameHapticType.SUCCESS -> 70L
                GameHapticType.ERROR -> 120L
            }
            vibrator?.vibrate(duration)
        }
    } catch (_: Exception) {}
}
