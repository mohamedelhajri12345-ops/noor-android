package com.elhajri.noor.tasbih

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TasbihPrefs {
    private const val PREF_NAME = "noor_tasbih_prefs"
    private const val KEY_TOTAL_ALL_TIME = "tasbih_total_all_time"
    private const val KEY_SELECTED_STYLE = "tasbih_selected_style"
    private const val KEY_SELECTED_DHIKR = "tasbih_selected_dhikr"
    private const val KEY_SELECTED_TARGET = "tasbih_selected_target"
    private const val KEY_CUSTOM_DHIKRS = "tasbih_custom_dhikrs"

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun getTotalAllTime(context: Context): Int {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getInt(KEY_TOTAL_ALL_TIME, 0)
    }

    fun getDailyCount(context: Context): Int {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getInt("tasbih_daily_${getTodayDateString()}", 0)
    }

    fun getDhikrCount(context: Context, dhikr: String): Int {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getInt("tasbih_dhikr_${dhikr.hashCode()}", 0)
    }

    fun incrementCounters(context: Context, dhikr: String, delta: Int = 1) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val edit = sp.edit()

        val newTotal = sp.getInt(KEY_TOTAL_ALL_TIME, 0) + delta
        edit.putInt(KEY_TOTAL_ALL_TIME, newTotal)

        val dailyKey = "tasbih_daily_${getTodayDateString()}"
        val newDaily = sp.getInt(dailyKey, 0) + delta
        edit.putInt(dailyKey, newDaily)

        val dhikrKey = "tasbih_dhikr_${dhikr.hashCode()}"
        val newDhikr = sp.getInt(dhikrKey, 0) + delta
        edit.putInt(dhikrKey, newDhikr)

        edit.apply()
    }

    fun getSelectedStyle(context: Context): Int {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getInt(KEY_SELECTED_STYLE, 0)
    }

    fun saveSelectedStyle(context: Context, styleIndex: Int) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putInt(KEY_SELECTED_STYLE, styleIndex).apply()
    }

    fun getSelectedDhikr(context: Context): String {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getString(KEY_SELECTED_DHIKR, "سبحان الله") ?: "سبحان الله"
    }

    fun saveSelectedDhikr(context: Context, dhikr: String) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_SELECTED_DHIKR, dhikr).apply()
    }

    fun getSelectedTarget(context: Context): Int {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getInt(KEY_SELECTED_TARGET, 33)
    }

    fun saveSelectedTarget(context: Context, target: Int) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putInt(KEY_SELECTED_TARGET, target).apply()
    }

    fun getCustomDhikrList(context: Context): List<String> {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val set = sp.getStringSet(KEY_CUSTOM_DHIKRS, emptySet()) ?: emptySet()
        return set.toList()
    }

    fun addCustomDhikr(context: Context, dhikr: String) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val set = sp.getStringSet(KEY_CUSTOM_DHIKRS, emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(dhikr)
        sp.edit().putStringSet(KEY_CUSTOM_DHIKRS, set).apply()
    }
}
