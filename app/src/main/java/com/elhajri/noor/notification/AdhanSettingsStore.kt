package com.elhajri.noor.notification

import android.content.Context
import android.content.SharedPreferences

object AdhanSettingsStore {
    private const val PREFS_NAME = "noor_adhan_customization_prefs"

    private const val KEY_MASTER_ENABLED = "adhan_master_enabled"
    private const val KEY_SELECTED_SOUND = "adhan_selected_sound"
    private const val KEY_PRE_REMINDER_ENABLED = "pre_adhan_reminder_enabled"
    private const val KEY_PRE_REMINDER_MINUTES = "pre_adhan_reminder_minutes"

    private const val PREFIX_PRAYER_ENABLED = "prayer_enabled_"

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAdhanEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_MASTER_ENABLED, com.elhajri.noor.data.Prefs.getAdhanEnabled(context))
    }

    fun setAdhanEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MASTER_ENABLED, enabled).apply()
        com.elhajri.noor.data.Prefs.setAdhanEnabled(context, enabled)
    }

    fun getSelectedAdhanSound(context: Context): String {
        return prefs(context).getString(KEY_SELECTED_SOUND, "makkah") ?: "makkah"
    }

    fun setSelectedAdhanSound(context: Context, soundId: String) {
        prefs(context).edit().putString(KEY_SELECTED_SOUND, soundId).apply()
    }

    fun isPrayerEnabled(context: Context, prayerNameAr: String): Boolean {
        val key = PREFIX_PRAYER_ENABLED + prayerNameAr
        return prefs(context).getBoolean(key, true)
    }

    fun setPrayerEnabled(context: Context, prayerNameAr: String, enabled: Boolean) {
        val key = PREFIX_PRAYER_ENABLED + prayerNameAr
        prefs(context).edit().putBoolean(key, enabled).apply()
    }

    fun isPreAdhanReminderEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_PRE_REMINDER_ENABLED, true)
    }

    fun setPreAdhanReminderEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_PRE_REMINDER_ENABLED, enabled).apply()
    }

    fun getPreAdhanMinutes(context: Context): Int {
        return prefs(context).getInt(KEY_PRE_REMINDER_MINUTES, 10)
    }

    fun setPreAdhanMinutes(context: Context, minutes: Int) {
        prefs(context).edit().putInt(KEY_PRE_REMINDER_MINUTES, minutes).apply()
    }
}
