package com.elhajri.noor.prayer

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PrayerTimings(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val hijriDate: String
)

object PrayerRepository {
    private const val PREFS_NAME = "noor_prayer_cache"
    private const val KEY_CACHED_TIMINGS = "cached_timings_json"

    private val client: OkHttpClient by lazy { OkHttpClient() }

    suspend fun getTimings(
        context: Context,
        date: String = "",
        lat: Double = 21.4225,
        lng: Double = 39.8262
    ): PrayerTimings = withContext(Dispatchers.IO) {
        val targetDate = if (date.isBlank()) {
            SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        } else {
            date
        }

        val url = "https://api.aladhan.com/v1/timings/$targetDate?latitude=$lat&longitude=$lng"
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                val timings = parseTimingsJson(bodyStr)
                if (timings != null) {
                    saveToCache(context, bodyStr)
                    return@withContext timings
                }
            }
        } catch (_: Exception) {
            // Fetch failed (offline) -> fallback to cache
        }

        // Fallback to cache or default values
        getFromCache(context) ?: PrayerTimings(
            fajr = "05:00",
            sunrise = "06:15",
            dhuhr = "12:15",
            asr = "15:30",
            maghrib = "18:15",
            isha = "19:45",
            hijriDate = "15 ربيع الأول 1448 هـ"
        )
    }

    suspend fun getTimings(
        date: String,
        lat: Double,
        lng: Double
    ): PrayerTimings = withContext(Dispatchers.IO) {
        val targetDate = if (date.isBlank()) {
            SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        } else date

        val url = "https://api.aladhan.com/v1/timings/$targetDate?latitude=$lat&longitude=$lng"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val bodyStr = response.body?.string() ?: ""
        parseTimingsJson(bodyStr) ?: throw Exception("Failed to parse prayer timings")
    }

    fun getCachedTimings(context: Context): PrayerTimings? {
        return getFromCache(context)
    }

    private fun saveToCache(context: Context, jsonStr: String) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_CACHED_TIMINGS, jsonStr).apply()
    }

    private fun getFromCache(context: Context): PrayerTimings? {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedJson = sp.getString(KEY_CACHED_TIMINGS, null) ?: return null
        return parseTimingsJson(cachedJson)
    }

    private fun cleanTime(rawTime: String): String {
        return rawTime.split(" ")[0].trim()
    }

    private fun parseTimingsJson(jsonStr: String): PrayerTimings? {
        return try {
            val root = JSONObject(jsonStr)
            val dataObj = root.getJSONObject("data")
            val timingsObj = dataObj.getJSONObject("timings")

            val fajr = cleanTime(timingsObj.getString("Fajr"))
            val sunrise = cleanTime(timingsObj.getString("Sunrise"))
            val dhuhr = cleanTime(timingsObj.getString("Dhuhr"))
            val asr = cleanTime(timingsObj.getString("Asr"))
            val maghrib = cleanTime(timingsObj.getString("Maghrib"))
            val isha = cleanTime(timingsObj.getString("Isha"))

            var hijriStr = ""
            if (dataObj.has("date")) {
                val dateObj = dataObj.getJSONObject("date")
                if (dateObj.has("hijri")) {
                    val hijriObj = dateObj.getJSONObject("hijri")
                    val day = hijriObj.optString("day", "")
                    val monthObj = hijriObj.optJSONObject("month")
                    val monthName = monthObj?.optString("ar", monthObj.optString("en", "")) ?: ""
                    val year = hijriObj.optString("year", "")
                    val weekdayObj = hijriObj.optJSONObject("weekday")
                    val dayName = weekdayObj?.optString("ar", "") ?: ""
                    hijriStr = listOf(dayName, day, monthName, year).filter { it.isNotBlank() }.joinToString(" ") + " هـ"
                }
            }

            if (hijriStr.isBlank()) {
                hijriStr = "15 ربيع الأول 1448 هـ"
            }

            PrayerTimings(fajr, sunrise, dhuhr, asr, maghrib, isha, hijriStr)
        } catch (e: Exception) {
            null
        }
    }
}
