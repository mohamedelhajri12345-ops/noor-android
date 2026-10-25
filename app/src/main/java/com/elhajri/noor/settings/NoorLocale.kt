package com.elhajri.noor.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * App-wide language state. Screens call t(ar, en) for every user-facing string;
 * switching the language in Settings flips this state and recomposes everything.
 */
object NoorLocale {
    var lang by mutableStateOf("ar")
        private set

    val isEnglish: Boolean get() = lang == "en"

    fun init(context: Context) {
        lang = NoorSettings.getLang(context)
    }

    fun set(context: Context, code: String) {
        lang = code
        NoorSettings.setLang(context, code)
    }

    /** Pick the string for the active language. */
    fun t(ar: String, en: String): String = if (lang == "en") en else ar

    /** Format digits based on active language. */
    fun formatDigits(v: Any): String {
        if (lang == "en") return v.toString()
        val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
        return v.toString().map { map[it] ?: it }.joinToString("")
    }
}

/** Composable-friendly top-level shortcut. */
fun t(ar: String, en: String): String = NoorLocale.t(ar, en)

/** Global helper for digit formatting sensitive to language setting. */
fun toArabicDigits(v: Any): String = NoorLocale.formatDigits(v)
fun toArabicNumber(v: Any): String = NoorLocale.formatDigits(v)

fun Int.toArabicDigits(): String = NoorLocale.formatDigits(this)
fun Long.toArabicDigits(): String = NoorLocale.formatDigits(this)
fun String.toArabicDigits(): String = NoorLocale.formatDigits(this)
