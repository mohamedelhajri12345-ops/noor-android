package com.elhajri.noor.data

import android.content.Context

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
}
