package com.elhajri.noor.theme

import android.content.Context
import android.content.SharedPreferences

/**
 * مستودع متجر الثيمات — نقاط، ثيمات مفتوحة، الثيم الحالي، تقدم جلسة الإعلانات.
 * تخزين محلي منظم قابل للاستبدال لاحقاً بخلفية Backend:
 * كل العمليات تمر من هنا فقط، فتبديل SharedPreferences بـ API بعيد
 * لا يمس أي شاشة.
 */
object ThemeStore {

    const val THEME_PRICE = 0  // مؤقتاً مجاني للتجربة — يُعاد لـ1000 بعد ربط معرفات الإعلانات
    const val POINTS_PER_AD = 100
    const val ADS_PER_SESSION = 3

    private const val PREFS = "noor_theme_store_v1"
    private const val KEY_POINTS = "points"
    private const val KEY_UNLOCKED = "unlocked_ids"
    private const val KEY_CURRENT = "current_theme_id"
    private const val KEY_SESSION_ADS = "session_ads_completed"
    private const val KEY_SESSION_DONE = "session_done"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ───────────────────────── النقاط ─────────────────────────

    fun getPoints(context: Context): Int =
        prefs(context).getInt(KEY_POINTS, 0)

    /** إضافة نقاط — تُستدعى فقط عند مكافأة SDK فعلية. يعيد الرصيد الجديد. */
    fun addPoints(context: Context, amount: Int): Int {
        val newBalance = getPoints(context) + amount
        prefs(context).edit().putInt(KEY_POINTS, newBalance).apply()
        return newBalance
    }

    /**
     * خصم نقاط — يفحص الرصيد أولاً ويرفض إن لم يكن كافياً (لا رصيد سالب أبداً).
     * يعيد true فقط إذا تم الخصم فعلاً.
     */
    fun spendPoints(context: Context, amount: Int): Boolean {
        val balance = getPoints(context)
        if (balance < amount) return false
        prefs(context).edit().putInt(KEY_POINTS, balance - amount).apply()
        return true
    }

    // ───────────────────────── الثيمات المفتوحة ─────────────────────────

    fun getUnlockedIds(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_UNLOCKED, emptySet()) ?: emptySet()

    /** هل الثيم مفتوح؟ المجاني مفتوح دوماً. */
    fun isUnlocked(context: Context, def: NoorThemeDef): Boolean =
        def.price == 0 || getUnlockedIds(context).contains(def.id)

    /** شراء ثيم: يتحقق من الرصيد ثم يخصم ثم يفتح — عمليات ذرية متتابعة. */
    fun purchase(context: Context, def: NoorThemeDef): PurchaseResult {
        if (isUnlocked(context, def)) return PurchaseResult.ALREADY_UNLOCKED
        if (getPoints(context) < def.price) return PurchaseResult.INSUFFICIENT_POINTS
        if (!spendPoints(context, def.price)) return PurchaseResult.INSUFFICIENT_POINTS
        val unlocked = getUnlockedIds(context).toMutableSet()
        unlocked.add(def.id)
        prefs(context).edit().putStringSet(KEY_UNLOCKED, unlocked).apply()
        return PurchaseResult.OK
    }

    /** تعليم ثيم كمجرب (تجربة مجانية للثيم الأول تمنحها شاشة المتجر). */
    fun markUnlocked(context: Context, themeId: String) {
        val unlocked = getUnlockedIds(context).toMutableSet()
        unlocked.add(themeId)
        prefs(context).edit().putStringSet(KEY_UNLOCKED, unlocked).apply()
    }

    // ───────────────────────── الثيم الحالي ─────────────────────────

    fun getCurrentThemeId(context: Context): String =
        prefs(context).getString(KEY_CURRENT, "noor_default") ?: "noor_default"

    fun setCurrentTheme(context: Context, themeId: String) {
        prefs(context).edit().putString(KEY_CURRENT, themeId).apply()
        NoorThemeState.applyById(themeId)
    }

    /** استعادة الثيم المحفوظ عند إقلاع التطبيق. */
    fun restoreAtStartup(context: Context) {
        val id = getCurrentThemeId(context)
        val def = NoorThemes.byId(id) ?: NoorThemes.all.first()
        NoorThemeState.apply(def)
    }

    // ───────────────────────── جلسة الإعلانات ─────────────────────────

    fun getAdsCompletedInSession(context: Context): Int =
        prefs(context).getInt(KEY_SESSION_ADS, 0)

    /** تسجيل إعلان مكافأة مكتمل ضمن الجلسة الحالية. */
    fun recordAdCompleted(context: Context): Int {
        val n = (getAdsCompletedInSession(context) + 1).coerceAtMost(ADS_PER_SESSION)
        prefs(context).edit()
            .putInt(KEY_SESSION_ADS, n)
            .putBoolean(KEY_SESSION_DONE, n >= ADS_PER_SESSION)
            .apply()
        return n
    }

    fun isSessionComplete(context: Context): Boolean =
        getAdsCompletedInSession(context) >= ADS_PER_SESSION

    /** بدء جلسة مكافآت جديدة — يصفر تقدم الجلسة السابقة. */
    fun startNewSession(context: Context) {
        prefs(context).edit()
            .putInt(KEY_SESSION_ADS, 0)
            .putBoolean(KEY_SESSION_DONE, false)
            .apply()
    }

    enum class PurchaseResult { OK, INSUFFICIENT_POINTS, ALREADY_UNLOCKED }
}
