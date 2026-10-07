package com.elhajri.noor.games

import android.content.Context
import android.content.SharedPreferences

data class GameProgressData(
    val level: Int = 1,
    val score: Int = 0,
    val streak: Int = 0,
    val stars: Int = 0,
    val customData: String = ""
)

/**
 * مدير حفظ تقدم الألعاب الجديدة — يحفظ المستوى والنتيجة والنجوم والبيانات الإضافية.
 */
object NewGameProgress {
    private const val PREFS_NAME = "noor_new_games_progress_v1"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun save(
        context: Context,
        gameId: String,
        level: Int,
        score: Int,
        streak: Int = 0,
        stars: Int = 0,
        customData: String = ""
    ) {
        getPrefs(context).edit()
            .putInt("${gameId}_level", level)
            .putInt("${gameId}_score", score)
            .putInt("${gameId}_streak", streak)
            .putInt("${gameId}_stars", stars)
            .putString("${gameId}_custom", customData)
            .apply()
    }

    fun load(context: Context, gameId: String): GameProgressData {
        val prefs = getPrefs(context)
        val level = prefs.getInt("${gameId}_level", 1)
        val score = prefs.getInt("${gameId}_score", 0)
        val streak = prefs.getInt("${gameId}_streak", 0)
        val stars = prefs.getInt("${gameId}_stars", 0)
        val customData = prefs.getString("${gameId}_custom", "") ?: ""
        return GameProgressData(level, score, streak, stars, customData)
    }
}
