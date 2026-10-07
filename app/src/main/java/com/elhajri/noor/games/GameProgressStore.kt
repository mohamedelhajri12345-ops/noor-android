package com.elhajri.noor.games

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.elhajri.noor.theme.ThemeStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * GameProgressStore: SharedPreferences storage for per-game progress, streaks, scores, stars,
 * level unlocking, and ThemeStore points integration across all games in Noor app.
 */
object GameProgressStore {

    const val GAME_KALIMAT = "game_kalimat"
    const val GAME_TRIVIA = "game_trivia"
    const val GAME_AYAT = "game_ayat"
    const val GAME_MATCH = "game_match"
    const val GAME_TIMELINE = "game_timeline"
    const val GAME_TARTIL = "game_tartil"

    private const val PREFS_NAME = "noor_game_progress_v2"
    private const val KEY_HIGHEST_LEVEL = "highest_level_"
    private const val KEY_TOTAL_SCORE = "total_score_"
    private const val KEY_STREAK = "streak_"
    private const val KEY_COMPLETED = "completed_"
    private const val KEY_LAST_PLAYED = "last_played_"
    private const val KEY_STARS_PREFIX = "stars_"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun todayDateString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // ───────────────────────── Highest Level ─────────────────────────

    fun getHighestLevel(context: Context, gameId: String): Int =
        prefs(context).getInt(KEY_HIGHEST_LEVEL + gameId, 0)

    fun setHighestLevel(context: Context, gameId: String, level: Int) {
        val current = getHighestLevel(context, gameId)
        if (level > current) {
            prefs(context).edit().putInt(KEY_HIGHEST_LEVEL + gameId, level).apply()
        }
    }

    // ───────────────────────── Level Stars ─────────────────────────

    fun getLevelStars(context: Context, gameId: String, level: Int): Int =
        prefs(context).getInt("${KEY_STARS_PREFIX}${gameId}_$level", 0)

    fun setLevelStars(context: Context, gameId: String, level: Int, stars: Int) {
        val current = getLevelStars(context, gameId, level)
        if (stars > current) {
            prefs(context).edit().putInt("${KEY_STARS_PREFIX}${gameId}_$level", stars).apply()
        }
    }

    fun getTotalStars(context: Context, gameId: String, maxLevels: Int = 30): Int {
        var sum = 0
        for (lvl in 0 until maxLevels) {
            sum += getLevelStars(context, gameId, lvl)
        }
        return sum
    }

    // ───────────────────────── Total Score ─────────────────────────

    fun getTotalScore(context: Context, gameId: String): Int =
        prefs(context).getInt(KEY_TOTAL_SCORE + gameId, 0)

    fun addScore(context: Context, gameId: String, points: Int): Int {
        val current = getTotalScore(context, gameId)
        val newScore = current + points
        prefs(context).edit().putInt(KEY_TOTAL_SCORE + gameId, newScore).apply()
        return newScore
    }

    // ───────────────────────── Streak ─────────────────────────

    fun getStreak(context: Context, gameId: String): Int =
        prefs(context).getInt(KEY_STREAK + gameId, 0)

    fun getLastPlayedDate(context: Context, gameId: String): String =
        prefs(context).getString(KEY_LAST_PLAYED + gameId, "") ?: ""

    /**
     * Records a play session for today and updates streak.
     * Returns the new streak count.
     */
    fun recordPlayAndUpdateStreak(context: Context, gameId: String): Int {
        val today = todayDateString()
        val last = getLastPlayedDate(context, gameId)
        var streak = getStreak(context, gameId)

        if (last == today) {
            // Already played today, streak remains unchanged
            if (streak == 0) streak = 1
        } else if (isYesterday(last)) {
            streak += 1
        } else {
            // Missed a day or first time
            streak = 1
        }

        prefs(context).edit()
            .putInt(KEY_STREAK + gameId, streak)
            .putString(KEY_LAST_PLAYED + gameId, today)
            .apply()

        return streak
    }

    private fun isYesterday(dateStr: String): Boolean {
        if (dateStr.isBlank()) return false
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = format.parse(dateStr) ?: return false
            val calToday = Calendar.getInstance()
            val calDate = Calendar.getInstance().apply { time = date }

            calToday.add(Calendar.DAY_OF_YEAR, -1)
            calToday.get(Calendar.YEAR) == calDate.get(Calendar.YEAR) &&
                    calToday.get(Calendar.DAY_OF_YEAR) == calDate.get(Calendar.DAY_OF_YEAR)
        } catch (_: Exception) {
            false
        }
    }

    // ───────────────────────── Completion Flag ─────────────────────────

    fun isCompleted(context: Context, gameId: String): Boolean =
        prefs(context).getBoolean(KEY_COMPLETED + gameId, false)

    fun setCompleted(context: Context, gameId: String, completed: Boolean = true) {
        prefs(context).edit().putBoolean(KEY_COMPLETED + gameId, completed).apply()
    }

    // ───────────────────────── Rewards Integration ─────────────────────────

    /**
     * Adds rewards points directly to ThemeStore and records game progress.
     */
    fun rewardPoints(context: Context, gameId: String, level: Int, stars: Int, scoreGained: Int): Int {
        setHighestLevel(context, gameId, level + 1)
        setLevelStars(context, gameId, level, stars)
        addScore(context, gameId, scoreGained)
        val currentStreak = recordPlayAndUpdateStreak(context, gameId)

        // Base reward points = stars * 10 + scoreGained / 5
        var reward = (stars * 10) + (scoreGained / 5)
        // Bonus for streak
        if (currentStreak >= 3) reward += 15
        if (currentStreak >= 7) reward += 30

        if (reward > 0) {
            ThemeStore.addPoints(context, reward)
        }
        return reward
    }

    // ───────────────────────── Progress Data Snapshot ─────────────────────────

    data class GameProgressData(
        val highestLevel: Int,
        val totalStars: Int,
        val totalScore: Int,
        val streak: Int,
        val isCompleted: Boolean,
        val lastPlayedDate: String
    )

    fun getGameProgress(context: Context, gameId: String, maxLevels: Int = 30): GameProgressData {
        return GameProgressData(
            highestLevel = getHighestLevel(context, gameId),
            totalStars = getTotalStars(context, gameId, maxLevels),
            totalScore = getTotalScore(context, gameId),
            streak = getStreak(context, gameId),
            isCompleted = isCompleted(context, gameId),
            lastPlayedDate = getLastPlayedDate(context, gameId)
        )
    }
}

/**
 * ForcePortraitOrientation: Forces the host activity into portrait orientation
 * while active and restores original orientation on dispose.
 */
@Composable
fun ForcePortraitOrientation() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
