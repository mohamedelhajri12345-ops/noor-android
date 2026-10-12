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
}

/** Composable-friendly top-level shortcut. */
fun t(ar: String, en: String): String = NoorLocale.t(ar, en)
