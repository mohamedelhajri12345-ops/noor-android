package com.elhajri.noor.exams

import android.content.Context
import org.json.JSONObject

object ExamStorage {
    private const val PREFS_NAME = "noor_exams_storage"
    private const val KEY_USER_NAME = "exam_user_name"

    fun getUserName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_NAME, "قارئ نور") ?: "قارئ نور"
    }

    fun setUserName(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER_NAME, name.trim()).apply()
    }

    private fun getProgressKey(track: ExamTrack, diff: ExamDifficulty): String {
        return "progress_${track.id}_${diff.id}"
    }

    fun loadProgress(context: Context, track: ExamTrack, diff: ExamDifficulty): ExamProgress {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(getProgressKey(track, diff), null) ?: return ExamProgress(unlockedLevel = 1)
        return try {
            val json = JSONObject(raw)
            val unlocked = json.optInt("unlockedLevel", 1).coerceIn(1, diff.totalLevels)
            
            val starsObj = json.optJSONObject("stars")
            val starsMap = mutableMapOf<Int, Int>()
            if (starsObj != null) {
                val keys = starsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val lvl = k.toIntOrNull()
                    if (lvl != null) {
                        starsMap[lvl] = starsObj.getInt(k)
                    }
                }
            }

            val scoresObj = json.optJSONObject("scores")
            val scoresMap = mutableMapOf<Int, Int>()
            if (scoresObj != null) {
                val keys = scoresObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val lvl = k.toIntOrNull()
                    if (lvl != null) {
                        scoresMap[lvl] = scoresObj.getInt(k)
                    }
                }
            }

            ExamProgress(
                unlockedLevel = unlocked,
                starsMap = starsMap,
                highestScoreMap = scoresMap
            )
        } catch (_: Exception) {
            ExamProgress(unlockedLevel = 1)
        }
    }

    fun saveLevelResult(
        context: Context,
        track: ExamTrack,
        diff: ExamDifficulty,
        levelNumber: Int,
        percentage: Int,
        passed: Boolean,
        starsEarned: Int
    ) {
        val current = loadProgress(context, track, diff)
        val nextUnlocked = if (passed) maxOf(current.unlockedLevel, minOf(levelNumber + 1, diff.totalLevels)) else current.unlockedLevel

        val newStars = current.starsMap.toMutableMap()
        val prevStars = newStars[levelNumber] ?: 0
        if (starsEarned > prevStars) {
            newStars[levelNumber] = starsEarned
        }

        val newScores = current.highestScoreMap.toMutableMap()
        val prevScore = newScores[levelNumber] ?: 0
        if (percentage > prevScore) {
            newScores[levelNumber] = percentage
        }

        try {
            val json = JSONObject()
            json.put("unlockedLevel", nextUnlocked)

            val starsObj = JSONObject()
            newStars.forEach { (k, v) -> starsObj.put(k.toString(), v) }
            json.put("stars", starsObj)

            val scoresObj = JSONObject()
            newScores.forEach { (k, v) -> scoresObj.put(k.toString(), v) }
            json.put("scores", scoresObj)

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(getProgressKey(track, diff), json.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun isTierCompleted(context: Context, track: ExamTrack, diff: ExamDifficulty): Boolean {
        val progress = loadProgress(context, track, diff)
        // A tier is considered unlocked for certificate if completed at least level 10 or 100% or reached level > totalLevels
        return progress.unlockedLevel > 10 || progress.starsMap.size >= 10
    }
}
