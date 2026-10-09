package com.elhajri.noor.data

import android.content.Context
import java.util.Calendar

object Prefs {
    private const val FILE = "noor_prefs"

    private fun sp(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getCity(context: Context): City? {
        val name = sp(context).getString("city_name", null) ?: return null
        val lat = sp(context).getFloat("city_lat", 0f).toDouble()
        val lng = sp(context).getFloat("city_lng", 0f).toDouble()
        val country = sp(context).getString("city_country", "") ?: ""
        return City(name, lat, lng, country)
    }

    fun setCity(context: Context, city: City) {
        sp(context).edit()
            .putString("city_name", city.name)
            .putFloat("city_lat", city.lat.toFloat())
            .putFloat("city_lng", city.lng.toFloat())
            .putString("city_country", city.country)
            .apply()
    }

    fun getReciter(context: Context): String = sp(context).getString("reciter", "") ?: ""

    fun setReciter(context: Context, id: String) = sp(context).edit().putString("reciter", id).apply()

    fun getAdhanEnabled(context: Context): Boolean = sp(context).getBoolean("adhan_enabled", true)

    fun setAdhanEnabled(context: Context, v: Boolean) = sp(context).edit().putBoolean("adhan_enabled", v).apply()

    // صوت الأذان (إن أُطفئ تظهر الإشارة فقط بلا صوت)
    fun getAdhanSound(context: Context): Boolean = sp(context).getBoolean("adhan_sound", true)
    fun setAdhanSound(context: Context, v: Boolean) = sp(context).edit().putBoolean("adhan_sound", v).apply()

    // اهتزاز الجهاز مع الأذان
    fun getAdhanVibrate(context: Context): Boolean = sp(context).getBoolean("adhan_vibrate", true)
    fun setAdhanVibrate(context: Context, v: Boolean) = sp(context).edit().putBoolean("adhan_vibrate", v).apply()

    // حجم خط المصحف: S / M / L
    fun getReaderFontSize(context: Context): String = sp(context).getString("reader_font_size", "M") ?: "M"
    fun setReaderFontSize(context: Context, v: String) = sp(context).edit().putString("reader_font_size", v).apply()

    // ───────────────────────── Kids Videos Prefs ─────────────────────────

    fun getLastWatchedVideoId(context: Context): String? =
        sp(context).getString("last_video_id", null)

    fun getLastWatchedVideoTitle(context: Context): String? =
        sp(context).getString("last_video_title", null)

    fun getLastWatchedVideoPosition(context: Context): Long =
        sp(context).getLong("last_video_pos", 0L)

    fun saveLastWatchedVideo(context: Context, videoId: String, title: String, positionSec: Long) {
        sp(context).edit()
            .putString("last_video_id", videoId)
            .putString("last_video_title", title)
            .putLong("last_video_pos", positionSec)
            .apply()
    }

    fun getDailyWatchedVideosCount(context: Context): Int {
        checkDailyReset(context)
        return sp(context).getInt("daily_watched_videos_count", 0)
    }

    fun incrementDailyWatchedVideosCount(context: Context) {
        checkDailyReset(context)
        val current = getDailyWatchedVideosCount(context)
        sp(context).edit().putInt("daily_watched_videos_count", current + 1).apply()
    }

    private fun checkDailyReset(context: Context) {
        val cal = Calendar.getInstance()
        val today = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
        val lastDay = sp(context).getString("daily_videos_date", "")

        if (today != lastDay) {
            sp(context).edit()
                .putString("daily_videos_date", today)
                .putInt("daily_watched_videos_count", 0)
                .apply()
        }
    }
    // ===== Kids Videos: registry of videos that fail to embed/play =====
    private const val BROKEN_VIDEOS_KEY = "broken_video_ids"

    fun getBrokenVideos(context: Context): Set<String> {
        return sp(context).getStringSet(BROKEN_VIDEOS_KEY, emptySet()) ?: emptySet()
    }

    fun addBrokenVideo(context: Context, videoId: String) {
        val p = sp(context)
        val current = p.getStringSet(BROKEN_VIDEOS_KEY, emptySet()) ?: emptySet()
        if (videoId !in current) {
            p.edit().putStringSet(BROKEN_VIDEOS_KEY, current + videoId).apply()
        }
    }
}
