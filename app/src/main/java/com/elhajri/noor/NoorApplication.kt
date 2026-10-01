package com.elhajri.noor

import android.app.Application
import android.os.Process
import java.io.File

/**
 * يسجل أي انهيار مستقبلي في ملف crash_log.txt داخل مجلد التطبيق
 * حتى نشخّص المشكلة بدقة بدل التخمين.
 */
class NoorApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // إعلانات AdMob — تهيئة مرة واحدة في خيط خلفي (لا تعيق الإقلاع)
        Thread {
            try {
                com.google.android.gms.ads.MobileAds.initialize(this)
            } catch (_: Exception) {}
        }.start()

        // حجم الخط من الإعدادات — ينعكس على كامل التطبيق فور الإقلاع
        try {
            com.elhajri.noor.settings.FontScaleHolder.scale =
                com.elhajri.noor.settings.NoorSettings.fontScale(this)
        } catch (_: Exception) {}

        // تسخين صفحات الموقع: القرآن والأناشيد جاهزتان قبل أن يضغط المستخدم
        // فتفتحان فوراً كأنهما جزء أصيل من التطبيق
        // استعادة الثيم المحفوظ من متجر الثيمات قبل عرض أي واجهة
        try {
            com.elhajri.noor.theme.ThemeStore.restoreAtStartup(this)
        } catch (_: Exception) {}
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                File(filesDir, "crash_log.txt").appendText(
                    "=== ${'$'}{java.util.Date()} thread=${'$'}{t.name} ===\n" +
                    android.util.Log.getStackTraceString(e) + "\n\n"
                )
            } catch (_: Exception) {}
            previous?.uncaughtException(t, e) ?: Process.killProcess(Process.myPid())
        }
    }
}
