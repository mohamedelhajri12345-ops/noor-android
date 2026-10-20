package com.elhajri.noor.prayer

import com.elhajri.noor.settings.NoorLocale
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits

import android.content.Context
import com.elhajri.noor.data.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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

/** Local astronomical prayer time calculator (PrayTimes algorithm) for offline computing */
object LocalPrayerCalculator {

    private data class SunPosition(val declination: Double, val equationOfTime: Double)

    private fun fixAngle(a: Double): Double {
        var b = a - 360.0 * Math.floor(a / 360.0)
        if (b < 0) b += 360.0
        return b
    }

    private fun fixHour(a: Double): Double {
        var b = a - 24.0 * Math.floor(a / 24.0)
        if (b < 0) b += 24.0
        return b
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = Math.floor(y / 100.0)
        val b = 2 - a + Math.floor(a / 4.0)
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): SunPosition {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * Math.sin(Math.toRadians(g)) + 0.020 * Math.sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(Math.atan2(Math.cos(Math.toRadians(e)) * Math.sin(Math.toRadians(l)), Math.cos(Math.toRadians(l)))) / 15.0
        val raFixed = fixHour(ra)
        val eqt = (q / 15.0) - raFixed
        val decl = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(e)) * Math.sin(Math.toRadians(l))))
        return SunPosition(decl, eqt)
    }

    private fun hourAngle(angle: Double, lat: Double, decl: Double): Double {
        val radLat = Math.toRadians(lat)
        val radDecl = Math.toRadians(decl)
        val radAngle = Math.toRadians(angle)
        val cosH = (Math.sin(radAngle) - Math.sin(radLat) * Math.sin(radDecl)) / (Math.cos(radLat) * Math.cos(radDecl))
        if (cosH > 1.0 || cosH < -1.0) return 0.0
        return Math.toDegrees(Math.acos(cosH)) / 15.0
    }

    private fun asrHourAngle(lat: Double, decl: Double): Double {
        val phi = Math.toRadians(lat)
        val delta = Math.toRadians(decl)
        val d = Math.abs(phi - delta)
        val acot = Math.atan(1.0 / (1.0 + Math.tan(d)))
        val cosH = (Math.sin(acot) - Math.sin(phi) * Math.sin(delta)) / (Math.cos(phi) * Math.cos(delta))
        if (cosH > 1.0 || cosH < -1.0) return 0.0
        return Math.toDegrees(Math.acos(cosH)) / 15.0
    }

    fun calculate(
        year: Int,
        month: Int,
        day: Int,
        lat: Double,
        lng: Double,
        country: String = "",
        timezone: String = ""
    ): PrayerTimings {
        val tzOffset = if (timezone.isNotBlank()) {
            try {
                val tz = java.util.TimeZone.getTimeZone(timezone)
                val cal = Calendar.getInstance().apply { set(year, month - 1, day, 12, 0) }
                tz.getOffset(cal.timeInMillis) / 3600000.0
            } catch (_: Exception) {
                Calendar.getInstance().timeZone.getOffset(System.currentTimeMillis()) / 3600000.0
            }
        } else {
            Calendar.getInstance().timeZone.getOffset(System.currentTimeMillis()) / 3600000.0
        }

        val method = PrayerMethods.methodFor(country)
        val (fajrAngle, ishaAngle, isUmmAlQura) = when (method) {
            4, 8, 9, 10 -> Triple(18.5, 17.5, true)
            2 -> Triple(15.0, 15.0, false)
            5 -> Triple(19.5, 17.5, false)
            1 -> Triple(18.0, 18.0, false)
            12 -> Triple(12.0, 12.0, false)
            20 -> Triple(20.0, 18.0, false)
            else -> Triple(18.0, 17.0, false)
        }

        val jd = julianDate(year, month, day) - (lng / (15.0 * 24.0))
        val sunPos = sunPosition(jd)

        val noon = fixHour(12.0 + tzOffset - (lng / 15.0) - sunPos.equationOfTime)

        val haSunrise = hourAngle(-0.8333, lat, sunPos.declination)
        val sunriseTime = noon - haSunrise
        val sunsetTime = noon + haSunrise

        val haFajr = hourAngle(-fajrAngle, lat, sunPos.declination)
        val fajrTime = noon - haFajr

        val haAsr = asrHourAngle(lat, sunPos.declination)
        val asrTime = noon + haAsr

        val maghribTime = sunsetTime

        val ishaTime = if (isUmmAlQura) {
            maghribTime + 1.5
        } else {
            val haIsha = hourAngle(-ishaAngle, lat, sunPos.declination)
            noon + haIsha
        }

        fun formatTime(t: Double): String {
            val totalSec = Math.round(fixHour(t) * 3600).toInt()
            val h = (totalSec / 3600) % 24
            val m = (totalSec % 3600) / 60
            return String.format(Locale.US, "%02d:%02d", h, m)
        }

        val hijriStr = try {
            val icuTz = if (timezone.isNotBlank()) android.icu.util.TimeZone.getTimeZone(timezone) else android.icu.util.TimeZone.getDefault()
            val cal = android.icu.util.IslamicCalendar(icuTz)
            val gCal = Calendar.getInstance().apply { set(year, month - 1, day) }
            cal.time = gCal.time
            val hDay = cal.get(android.icu.util.IslamicCalendar.DAY_OF_MONTH)
            val months = listOf(
                "محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الآخرة",
                "رجب", "شعبان", "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
            )
            val hMonth = months.getOrNull(cal.get(android.icu.util.IslamicCalendar.MONTH)) ?: ""
            val hYear = cal.get(android.icu.util.IslamicCalendar.YEAR)
            if (NoorLocale.isEnglish) "$hDay $hMonth $hYear AH" else "$hDay $hMonth $hYear هـ"
        } catch (_: Exception) {
            "15 ربيع الأول 1448 هـ"
        }

        return PrayerTimings(
            fajr = formatTime(fajrTime),
            sunrise = formatTime(sunriseTime),
            dhuhr = formatTime(noon),
            asr = formatTime(asrTime),
            maghrib = formatTime(maghribTime),
            isha = formatTime(ishaTime),
            hijriDate = hijriStr
        )
    }
}

object PrayerRepository {
    private const val PREFS_NAME = "noor_prayer_cache"
    private const val KEY_CACHED_TIMINGS = "cached_timings_json"
    private const val KEY_CACHED_CITY = "cached_city_key"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
    }

    suspend fun getTimings(
        context: Context,
        date: String = "",
        lat: Double = 21.4225,
        lng: Double = 39.8262,
        country: String = "",
        timezone: String = ""
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
                    saveToCache(context, bodyStr, lat, lng)
                    return@withContext timings
                }
            }
        } catch (_: Exception) {
            // Fetch failed (offline) -> fallback to cache or local calculation
        }

        // 1. Try local cache for this location
        val cached = getFromCache(context, lat, lng)
        if (cached != null) return@withContext cached

        // 2. Compute locally using astronomical algorithm
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val computed = LocalPrayerCalculator.calculate(year, month, day, lat, lng, country, timezone)
        val jsonStr = buildJsonFromTimings(computed)
        saveToCache(context, jsonStr, lat, lng)
        computed
    }



    /** Cached timings for the CURRENT saved city — guaranteed non-null with offline calculation fallback */
    fun getCachedTimings(context: Context): PrayerTimings {
        val city = Prefs.getCity(context) ?: com.elhajri.noor.data.City("مكة المكرمة", 21.4225, 39.8262, "السعودية", "Asia/Riyadh")
        val cached = getFromCache(context, city.lat, city.lng)
        if (cached != null) return cached

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val computed = LocalPrayerCalculator.calculate(year, month, day, city.lat, city.lng, city.country, city.timezone)
        val jsonStr = buildJsonFromTimings(computed)
        saveToCache(context, jsonStr, city.lat, city.lng)
        return computed
    }

    private fun buildJsonFromTimings(timings: PrayerTimings): String {
        return try {
            val json = JSONObject()
            val dataObj = JSONObject()
            val timingsObj = JSONObject()
            timingsObj.put("Fajr", timings.fajr)
            timingsObj.put("Sunrise", timings.sunrise)
            timingsObj.put("Dhuhr", timings.dhuhr)
            timingsObj.put("Asr", timings.asr)
            timingsObj.put("Maghrib", timings.maghrib)
            timingsObj.put("Isha", timings.isha)
            dataObj.put("timings", timingsObj)

            val dateObj = JSONObject()
            val hijriObj = JSONObject()
            val parts = timings.hijriDate.replace("هـ", "").trim().split(" ")
            if (parts.size >= 3) {
                hijriObj.put("day", parts[0])
                val monthAr = parts.subList(1, parts.size - 1).joinToString(" ")
                hijriObj.put("month", JSONObject().put("ar", monthAr))
                hijriObj.put("year", parts.last())
            }
            dateObj.put("hijri", hijriObj)
            dataObj.put("date", dateObj)
            json.put("data", dataObj)
            json.toString()
        } catch (_: Exception) {
            ""
        }
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
                    hijriStr = listOf(dayName, day, monthName, year).filter { it.isNotBlank() }.joinToString(" ") + (if (NoorLocale.isEnglish) " AH" else " هـ")
                }
            }

            if (hijriStr.isBlank()) {
                hijriStr = "15 ربيع الأول 1448 هـ"
            }

            PrayerTimings(fajr, sunrise, dhuhr, asr, maghrib, isha, hijriStr)
        } catch (_: Exception) {
            null
        }
    }
}
