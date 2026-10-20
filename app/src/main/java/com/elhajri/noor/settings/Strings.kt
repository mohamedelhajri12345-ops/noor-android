package com.elhajri.noor.settings

import androidx.compose.runtime.compositionLocalOf

/**
 * Central string table data class for NOOR app strings.
 * Accessible in Compose via LocalStrings.current or NoorLocale.t(ar, en).
 */
data class NoorStrings(
    val appName: String,
    val navHome: String,
    val navQuran: String,
    val navLibrary: String,
    val navMore: String,
    val settings: String,
    val profile: String,
    val search: String,
    val back: String,
    val cancel: String,
    val confirm: String,
    val save: String,
    val delete: String,
    val share: String,
    val download: String,
    val read: String,
    val play: String,
    val pause: String,
    val retry: String,
    val close: String,
    val loading: String,
    val offlineMode: String,
    val appLanguage: String
)

val NoorStringsAr = NoorStrings(
    appName = "القرآن الكريم - نور",
    navHome = "الرئيسية",
    navQuran = "المصحف",
    navLibrary = "المكتبة",
    navMore = "المزيد",
    settings = "الإعدادات",
    profile = "ملفي الشخصي",
    search = "بحث...",
    back = "رجوع",
    cancel = "إلغاء",
    confirm = "تأكيد",
    save = "حفظ",
    delete = "حذف",
    share = "مشاركة",
    download = "تنزيل",
    read = "اقرأ",
    play = "تشغيل",
    pause = "إيقاف مؤقت",
    retry = "إعادة المحاولة",
    close = "إغلاق",
    loading = "جاري التحميل...",
    offlineMode = "الوضع بدون إنترنت",
    appLanguage = "لغة التطبيق"
)

val NoorStringsEn = NoorStrings(
    appName = "NOOR - Holy Qur'an",
    navHome = "Home",
    navQuran = "Qur'an",
    navLibrary = "Library",
    navMore = "More",
    settings = "Settings",
    profile = "My Profile",
    search = "Search...",
    back = "Back",
    cancel = "Cancel",
    confirm = "Confirm",
    save = "Save",
    delete = "Delete",
    share = "Share",
    download = "Download",
    read = "Read",
    play = "Play",
    pause = "Pause",
    retry = "Retry",
    close = "Close",
    loading = "Loading...",
    offlineMode = "Offline Mode",
    appLanguage = "App Language"
)

val LocalStrings = compositionLocalOf { NoorStringsAr }
