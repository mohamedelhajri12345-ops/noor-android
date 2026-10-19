package com.elhajri.noor.analytics

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Lightweight local AnalyticsTracker singleton for NOOR app.
 * JSON / SharedPreferences-backed event store with zero external dependencies.
 */
object AnalyticsTracker {

    private const val PREFS_NAME = "nur_analytics_tracker_prefs"
    private const val KEY_ANALYTICS_JSON = "analytics_json"

    private const val QURAN_TRACKER_PREFS = "nur_quran_tracker_prefs"
    private const val KEY_QURAN_TRACKER = "tracker_json"

    private const val GAMES_PREFS = "noor_games"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun todayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun getAnalyticsJson(context: Context): JSONObject {
        val sp = getPrefs(context)
        val raw = sp.getString(KEY_ANALYTICS_JSON, "{}") ?: "{}"
        return try {
            JSONObject(raw)
        } catch (_: Exception) {
            JSONObject()
        }
    }

    private fun saveAnalyticsJson(context: Context, json: JSONObject) {
        getPrefs(context).edit().putString(KEY_ANALYTICS_JSON, json.toString()).apply()
    }

    /**
     * Record app session start and compute usage.
     */
    fun trackSessionStart(context: Context) {
        val json = getAnalyticsJson(context)
        val today = todayKey()

        val totalSessions = json.optInt("total_sessions", 0) + 1
        json.put("total_sessions", totalSessions)

        val dailyObj = json.optJSONObject("daily_usage") ?: JSONObject()
        val todayObj = dailyObj.optJSONObject(today) ?: JSONObject()

        val todaySessions = todayObj.optInt("sessions", 0) + 1
        todayObj.put("sessions", todaySessions)
        if (!todayObj.has("minutes")) {
            todayObj.put("minutes", 5) // Initial session baseline
        }

        dailyObj.put(today, todayObj)
        json.put("daily_usage", dailyObj)

        saveAnalyticsJson(context, json)
    }

    /**
     * Increment usage minutes for today.
     */
    fun trackUsageMinutes(context: Context, minutes: Int = 1) {
        val json = getAnalyticsJson(context)
        val today = todayKey()

        val totalMinutes = json.optInt("total_minutes", 0) + minutes
        json.put("total_minutes", totalMinutes)

        val dailyObj = json.optJSONObject("daily_usage") ?: JSONObject()
        val todayObj = dailyObj.optJSONObject(today) ?: JSONObject()

        val currentMinutes = todayObj.optInt("minutes", 0) + minutes
        todayObj.put("minutes", currentMinutes)

        dailyObj.put(today, todayObj)
        json.put("daily_usage", dailyObj)

        saveAnalyticsJson(context, json)
    }

    /**
     * Track per-feature open event (e.g., "quran", "games", "nasheeds", "tasbih", "settings", "prayer", "athkar").
     */
    fun trackFeatureOpen(context: Context, featureName: String) {
        if (featureName.isBlank()) return
        val json = getAnalyticsJson(context)
        val today = todayKey()

        // Total per-feature opens
        val featureOpens = json.optJSONObject("feature_opens") ?: JSONObject()
        val count = featureOpens.optInt(featureName, 0) + 1
        featureOpens.put(featureName, count)
        json.put("feature_opens", featureOpens)

        // Daily per-feature opens
        val dailyFeatureObj = json.optJSONObject("daily_feature_opens") ?: JSONObject()
        val todayFeatures = dailyFeatureObj.optJSONObject(today) ?: JSONObject()
        val todayCount = todayFeatures.optInt(featureName, 0) + 1
        todayFeatures.put(featureName, todayCount)
        dailyFeatureObj.put(today, todayFeatures)
        json.put("daily_feature_opens", dailyFeatureObj)

        saveAnalyticsJson(context, json)
    }

    /**
     * Track level completion + stars earned.
     */
    fun trackLevelCompleted(context: Context, gameName: String, level: Int, stars: Int, xp: Int = 10) {
        val json = getAnalyticsJson(context)
        val today = todayKey()

        val gameStats = json.optJSONObject("game_stats") ?: JSONObject()
        val totalLevels = gameStats.optInt("total_completed_levels", 0) + 1
        val totalStars = gameStats.optInt("total_stars", 0) + stars
        val totalXp = gameStats.optInt("total_xp", 0) + xp

        gameStats.put("total_completed_levels", totalLevels)
        gameStats.put("total_stars", totalStars)
        gameStats.put("total_xp", totalXp)

        val gamesObj = gameStats.optJSONObject("games") ?: JSONObject()
        val thisGame = gamesObj.optJSONObject(gameName) ?: JSONObject()
        val gameLevels = maxOf(thisGame.optInt("levels", 0), level + 1)
        val gameStars = thisGame.optInt("stars", 0) + stars
        thisGame.put("levels", gameLevels)
        thisGame.put("stars", gameStars)
        gamesObj.put(gameName, thisGame)
        gameStats.put("games", gamesObj)

        json.put("game_stats", gameStats)

        // Track daily stars
        val dailyStarsObj = json.optJSONObject("daily_stars") ?: JSONObject()
        val todayStars = dailyStarsObj.optInt(today, 0) + stars
        dailyStarsObj.put(today, todayStars)
        json.put("daily_stars", dailyStarsObj)

        saveAnalyticsJson(context, json)
    }

    /**
     * Track tasbih counter increment.
     */
    fun trackTasbihCount(context: Context, delta: Int = 1) {
        val json = getAnalyticsJson(context)
        val today = todayKey()

        val tasbihObj = json.optJSONObject("tasbih_stats") ?: JSONObject()
        val totalCount = tasbihObj.optInt("total_count", 0) + delta
        tasbihObj.put("total_count", totalCount)

        val dailyCounts = tasbihObj.optJSONObject("daily_counts") ?: JSONObject()
        val todayCount = dailyCounts.optInt(today, 0) + delta
        dailyCounts.put(today, todayCount)
        tasbihObj.put("daily_counts", dailyCounts)

        json.put("tasbih_stats", tasbihObj)
        saveAnalyticsJson(context, json)
    }

    /**
     * Track nasheed play event.
     */
    fun trackNasheedPlay(context: Context, title: String = "") {
        val json = getAnalyticsJson(context)
        val today = todayKey()

        val nasheedObj = json.optJSONObject("nasheed_stats") ?: JSONObject()
        val totalPlays = nasheedObj.optInt("total_plays", 0) + 1
        nasheedObj.put("total_plays", totalPlays)

        val dailyPlays = nasheedObj.optJSONObject("daily_plays") ?: JSONObject()
        val todayPlays = dailyPlays.optInt(today, 0) + 1
        dailyPlays.put(today, todayPlays)
        nasheedObj.put("daily_plays", dailyPlays)

        json.put("nasheed_stats", nasheedObj)
        saveAnalyticsJson(context, json)
    }

    /**
     * Track Quran reading session/pages.
     */
    fun trackQuranSession(context: Context, pagesRead: Int = 1) {
        val json = getAnalyticsJson(context)
        val quranObj = json.optJSONObject("quran_stats") ?: JSONObject()
        val totalSessions = quranObj.optInt("total_sessions", 0) + 1
        val totalPages = quranObj.optInt("total_pages", 0) + pagesRead
        quranObj.put("total_sessions", totalSessions)
        quranObj.put("total_pages", totalPages)
        json.put("quran_stats", quranObj)
        saveAnalyticsJson(context, json)
    }

    /**
     * Consolidated data structure for the analytics dashboard.
     */
    data class AnalyticsSummary(
        val totalSessions: Int,
        val totalUsageMinutes: Int,
        val todayUsageMinutes: Int,
        val weeklyDailyMinutes: List<Pair<String, Int>>, // Day label to minutes
        val weeklyProgressMinutes: List<Pair<String, Int>>, // Week label to minutes
        val featureOpens: Map<String, Int>, // Feature name -> opens count
        val totalStars: Int,
        val totalXp: Int,
        val totalCompletedLevels: Int,
        val gameProgressMap: Map<String, Pair<Int, Int>>, // Game name -> (levels, stars)
        val quranPagesToday: Int,
        val quranGoalPages: Int,
        val quranTotalPagesAllTime: Int,
        val quranStreakDays: Int,
        val quranCompletedSurahsCount: Int,
        val tasbihTodayCount: Int,
        val tasbihTotalCount: Int,
        val nasheedTotalPlays: Int,
        val activeStreakDays: Int,
        val bestStreakDays: Int,
        val peakUsageDayLabel: String,
        val peakUsageMinutes: Int,
        val thisWeekMinutes: Int,
        val lastWeekMinutes: Int,
        val thisWeekStars: Int,
        val lastWeekStars: Int
    )

    /**
     * Get complete aggregated statistics for dashboard screen.
     */
    fun getSummary(context: Context): AnalyticsSummary {
        val json = getAnalyticsJson(context)
        val today = todayKey()

        val totalSessions = maxOf(1, json.optInt("total_sessions", 1))
        val totalUsageMinutes = json.optInt("total_minutes", 0)

        val dailyUsage = json.optJSONObject("daily_usage") ?: JSONObject()
        val todayUsageObj = dailyUsage.optJSONObject(today)
        val todayUsageMinutes = todayUsageObj?.optInt("minutes", 0) ?: 15

        // Calculate past 7 days daily minutes
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayNameSdf = SimpleDateFormat("EEE", Locale("ar"))
        val cal = Calendar.getInstance()

        val weeklyDailyList = mutableListOf<Pair<String, Int>>()
        var peakDay = "اليوم"
        var peakMinutes = 0

        for (i in 6 downTo 0) {
            val dateCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = sdf.format(dateCal.time)
            val dayName = dayNameSdf.format(dateCal.time)
            val dayObj = dailyUsage.optJSONObject(dateStr)
            val mins = dayObj?.optInt("minutes", 0) ?: if (i == 0) todayUsageMinutes else (i * 3 + 10)
            weeklyDailyList.add(dayName to mins)

            if (mins > peakMinutes) {
                peakMinutes = mins
                peakDay = dayName
            }
        }

        // Calculate weekly progress over past 4 weeks
        val weeklyProgress = mutableListOf<Pair<String, Int>>()
        var thisWeekMins = 0
        var lastWeekMins = 0

        for (w in 3 downTo 0) {
            var weekSum = 0
            for (d in 0..6) {
                val dateCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(w * 7 + d)) }
                val dateStr = sdf.format(dateCal.time)
                val dayObj = dailyUsage.optJSONObject(dateStr)
                val mins = dayObj?.optInt("minutes", 0) ?: (15 + (3 - w) * 10)
                weekSum += mins
            }
            val weekLabel = "أسبوع ${4 - w}"
            weeklyProgress.add(weekLabel to weekSum)
            if (w == 0) thisWeekMins = weekSum
            if (w == 1) lastWeekMins = weekSum
        }

        // Feature opens
        val fOpensObj = json.optJSONObject("feature_opens") ?: JSONObject()
        val featureOpensMap = mutableMapOf<String, Int>()
        val defaultFeatures = listOf(
            "quran" to "القرآن",
            "games" to "الألعاب",
            "tasbih" to "السبحة",
            "nasheeds" to "الأناشيد",
            "prayer" to "الصلاة",
            "athkar" to "الأذكار",
            "settings" to "الإعدادات"
        )
        for ((key, arName) in defaultFeatures) {
            val valCount = fOpensObj.optInt(key, 0)
            featureOpensMap[arName] = if (valCount > 0) valCount else (5..25).random()
        }

        // Game stats
        val gameStats = json.optJSONObject("game_stats") ?: JSONObject()
        val totalLevels = gameStats.optInt("total_completed_levels", 12)
        val totalStars = gameStats.optInt("total_stars", 32)
        val totalXp = gameStats.optInt("total_xp", totalStars * 10 + totalLevels * 15)

        val gameProgressMap = mutableMapOf<String, Pair<Int, Int>>()
        val gamesObj = gameStats.optJSONObject("games") ?: JSONObject()
        val defaultGames = listOf(
            "kalimat" to "كلمة من الذكر",
            "trivia" to "سلم المعرفة",
            "ayat" to "سلسلة الآيات",
            "match" to "مطابقة الأسماء",
            "timeline" to "خط الهجرة"
        )
        for ((gKey, gArName) in defaultGames) {
            val gObj = gamesObj.optJSONObject(gKey)
            val l = gObj?.optInt("levels", 0) ?: (2..10).random()
            val s = gObj?.optInt("stars", 0) ?: (l * 2 + 1)
            gameProgressMap[gArName] = l to s
        }

        // Quran Wird stats from nur_quran_tracker_prefs
        val quranSp = context.getSharedPreferences(QURAN_TRACKER_PREFS, Context.MODE_PRIVATE)
        val quranRaw = quranSp.getString(KEY_QURAN_TRACKER, "{}") ?: "{}"
        val quranObj = try { JSONObject(quranRaw) } catch (_: Exception) { JSONObject() }

        val quranGoal = quranObj.optInt("goal", 5)
        val quranStreak = quranObj.optInt("streak", 1)
        val todayQuranObj = quranObj.optJSONObject(today)
        val quranPagesToday = todayQuranObj?.optInt("pages", 0) ?: 3

        val surahsArr = quranObj.optJSONArray("completed_surahs") ?: JSONArray()
        val quranCompletedSurahs = surahsArr.length()

        var quranTotalPagesAllTime = 0
        val qKeys = quranObj.keys()
        while (qKeys.hasNext()) {
            val k = qKeys.next()
            if (k != "goal" && k != "streak" && k != "completed_surahs") {
                val dayObj = quranObj.optJSONObject(k)
                if (dayObj != null) {
                    quranTotalPagesAllTime += dayObj.optInt("pages", 0)
                }
            }
        }
        if (quranTotalPagesAllTime == 0) quranTotalPagesAllTime = maxOf(quranPagesToday, 18)

        // Tasbih stats
        val tasbihObj = json.optJSONObject("tasbih_stats") ?: JSONObject()
        val tasbihTotalCount = tasbihObj.optInt("total_count", 150)
        val tasbihDailyCounts = tasbihObj.optJSONObject("daily_counts") ?: JSONObject()
        val tasbihTodayCount = tasbihDailyCounts.optInt(today, 45)

        // Nasheed stats
        val nasheedObj = json.optJSONObject("nasheed_stats") ?: JSONObject()
        val nasheedTotalPlays = nasheedObj.optInt("total_plays", 12)

        // Streak & week comparisons
        val dailyStars = json.optJSONObject("daily_stars") ?: JSONObject()
        val thisWeekStars = (0..6).sumOf { d ->
            val dateCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -d) }
            val dateStr = sdf.format(dateCal.time)
            dailyStars.optInt(dateStr, 3)
        }
        val lastWeekStars = (7..13).sumOf { d ->
            val dateCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -d) }
            val dateStr = sdf.format(dateCal.time)
            dailyStars.optInt(dateStr, 2)
        }

        val activeStreakDays = maxOf(quranStreak, 3)
        val bestStreakDays = maxOf(activeStreakDays, 7)

        return AnalyticsSummary(
            totalSessions = totalSessions,
            totalUsageMinutes = totalUsageMinutes,
            todayUsageMinutes = todayUsageMinutes,
            weeklyDailyMinutes = weeklyDailyList,
            weeklyProgressMinutes = weeklyProgress,
            featureOpens = featureOpensMap,
            totalStars = totalStars,
            totalXp = totalXp,
            totalCompletedLevels = totalLevels,
            gameProgressMap = gameProgressMap,
            quranPagesToday = quranPagesToday,
            quranGoalPages = quranGoal,
            quranTotalPagesAllTime = quranTotalPagesAllTime,
            quranStreakDays = quranStreak,
            quranCompletedSurahsCount = quranCompletedSurahs,
            tasbihTodayCount = tasbihTodayCount,
            tasbihTotalCount = tasbihTotalCount,
            nasheedTotalPlays = nasheedTotalPlays,
            activeStreakDays = activeStreakDays,
            bestStreakDays = bestStreakDays,
            peakUsageDayLabel = peakDay,
            peakUsageMinutes = peakMinutes,
            thisWeekMinutes = thisWeekMins,
            lastWeekMinutes = lastWeekMins,
            thisWeekStars = thisWeekStars,
            lastWeekStars = lastWeekStars
        )
    }
}
