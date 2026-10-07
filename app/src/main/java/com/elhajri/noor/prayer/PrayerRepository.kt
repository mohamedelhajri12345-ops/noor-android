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

object PrayerMethods {
    /** Aladhan calculation-method IDs per country — official religious authorities */
    fun methodFor(country: String): Int? = when (country) {
        "السعودية" -> 4      // Umm al-Qura
        "الإمارات", "عمان", "البحرين" -> 8   // Gulf Region
        "الكويت" -> 9
        "قطر" -> 10
        "مصر" -> 5
        "المغرب" -> 21
        "الجزائر" -> 19
        "تونس" -> 18
        "الأردن" -> 23
        "تركيا" -> 13
        "فرنسا" -> 12
        "أمريكا", "كندا" -> 2 // ISNA
        "باكستان", "الهند", "بنغلاديش", "أفغانستان", "أوزبكستان", "كازاخستان", "قيرغيزستان", "طاجيكستان", "تركمانستان" -> 1 // Karachi
        "إندونيسيا" -> 20
        "ماليزيا" -> 17
        "سنغافورة" -> 11
        "إيران" -> 7
        "روسيا" -> 14
        else -> null
    }
}

object PrayerRepository {
    private const val PREFS_NAME = "noor_prayer_cache"
    private const val KEY_CACHED_TIMINGS = "cached_timings_json"
    private const val KEY_CACHED_CITY = "cached_city_key"

    private val client: OkHttpClient by lazy { OkHttpClient() }

    suspend fun getTimings(
        context: Context,
        date: String = "",
        lat: Double = 21.4225,
        lng: Double = 39.8262,
        country: String = ""
    ): PrayerTimings = withContext(Dispatchers.IO) {
        val targetDate = if (date.isBlank()) {
            SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        } else {
            date
        }

        val method = PrayerMethods.methodFor(country)
        val url = StringBuilder()
            .append("https://api.aladhan.com/v1/timings/$targetDate?latitude=$lat&longitude=$lng")
            .apply { method?.let { append("&method=$it") } }
            .toString()
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                val timings = parseTimingsJson(bodyStr)
                if (timings != null) {
                    // Cache is stamped with its city so a location change never reuses old times
                    saveToCache(context, bodyStr, lat, lng)
                    return@withContext timings
                }
            }
        } catch (_: Exception) {
            // Fetch failed (offline) -> fallback to cache for THIS city only
        }

        // Fallback: cache of the same city only, else neutral defaults
        getFromCache(context, lat, lng) ?: PrayerTimings(
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

    /** Cached timings for the CURRENT saved city — used by the adhan scheduler */
    fun getCachedTimings(context: Context): PrayerTimings? {
        val city = com.elhajri.noor.data.Prefs.getCity(context) ?: return getFromCache(context, null, null)
        return getFromCache(context, city.lat, city.lng)
    }

    private fun saveToCache(context: Context, jsonStr: String, lat: Double, lng: Double) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putString(KEY_CACHED_TIMINGS, jsonStr)
            .putString(KEY_CACHED_CITY, "$lat|$lng")
            .apply()
    }

    /** Returns cached timings only when they belong to the requested city (tolerance 0.05 deg) */
    private fun getFromCache(context: Context, lat: Double?, lng: Double?): PrayerTimings? {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedJson = sp.getString(KEY_CACHED_TIMINGS, null) ?: return null
        if (lat != null && lng != null) {
            val cachedCity = sp.getString(KEY_CACHED_CITY, null) ?: return null
            val parts = cachedCity.split("|")
            val cLat = parts.getOrNull(0)?.toDoubleOrNull() ?: return null
            val cLng = parts.getOrNull(1)?.toDoubleOrNull() ?: return null
            if (kotlin.math.abs(cLat - lat) > 0.05 || kotlin.math.abs(cLng - lng) > 0.05) return null
        }
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
