package com.elhajri.noor.settings

import android.content.Context

/**
 * إعدادات نور — نفس مفاتيح الموقع حرفياً (localStorage في الويب،
 * SharedPreferences في الأصيل) حتى تتطابق إعدادات التطبيق والموقع
 * وتُطبَّق على محتوى الويب فيو تلقائياً عبر الحقن.
 */
object NoorSettings {

    private const val PREFS = "noor_settings"
    // نفس أسماء مفاتيح localStorage في الكود المصدري للموقع
    const val KEY_LANG = "lang"
    const val KEY_RECITER = "quran_reciter"
    const val KEY_AUDIO_QUALITY = "nur_audio_quality"
    const val KEY_FONT_SIZE = "nur_font_size"
    const val KEY_DATA_SAVER = "nur_data_saver"
    const val KEY_THEME = "nur_theme"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getLang(context: Context): String = prefs(context).getString(KEY_LANG, "ar") ?: "ar"
    fun setLang(context: Context, v: String) = prefs(context).edit().putString(KEY_LANG, v).apply()

    fun getReciter(context: Context): String = prefs(context).getString(KEY_RECITER, "alafasy") ?: "alafasy"
    fun setReciter(context: Context, v: String) = prefs(context).edit().putString(KEY_RECITER, v).apply()

    fun getAudioQuality(context: Context): String = prefs(context).getString(KEY_AUDIO_QUALITY, "high") ?: "high"
    fun setAudioQuality(context: Context, v: String) = prefs(context).edit().putString(KEY_AUDIO_QUALITY, v).apply()

    fun getFontSize(context: Context): String = prefs(context).getString(KEY_FONT_SIZE, "medium") ?: "medium"
    fun setFontSize(context: Context, v: String) = prefs(context).edit().putString(KEY_FONT_SIZE, v).apply()

    fun getDataSaver(context: Context): Boolean = prefs(context).getBoolean(KEY_DATA_SAVER, false)
    fun setDataSaver(context: Context, v: Boolean) = prefs(context).edit().putBoolean(KEY_DATA_SAVER, v).apply()

    fun reset(context: Context) {
        prefs(context).edit().clear().apply()
    }

    /** مضاعف حجم الخط للأصيل — يُقرأ عند إقلاع التطبيق */
    fun fontScale(context: Context): Float = when (getFontSize(context)) {
        "small" -> 0.9f
        "medium" -> 1.0f
        "large" -> 1.12f
        "xlarge" -> 1.25f
        else -> 1.0f
    }

    /**
     * حقن الإعدادات داخل صفحات الموقع (WebView) — نفس مفاتيح localStorage
     * التي يقرؤها الموقع فعلياً، فتتطابق التجربة بين الأصيل والويب.
     */
    fun webSyncJs(context: Context): String {
        val lang = getLang(context)
        val reciter = getReciter(context)
        val quality = getAudioQuality(context)
        val font = getFontSize(context)
        val saver = if (getDataSaver(context)) "true" else "false"
        return """
            (function(){try{
                localStorage.setItem('lang', '$lang');
                localStorage.setItem('quran_reciter', '$reciter');
                localStorage.setItem('nur_audio_quality', '$quality');
                localStorage.setItem('nur_font_size', '$font');
                localStorage.setItem('nur_data_saver', '$saver');
                localStorage.setItem('nur_theme', 'dark');
            }catch(e){}})();
        """.trimIndent()
    }
}
