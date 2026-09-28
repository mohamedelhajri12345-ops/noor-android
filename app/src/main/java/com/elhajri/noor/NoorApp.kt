package com.elhajri.noor

import android.app.Application
import android.os.Process
import java.io.File

/**
 * يسجل أي انهيار مستقبلي في ملف crash_log.txt داخل مجلد التطبيق
 * حتى نشخّص المشكلة بدقة بدل التخمين.
 */
class NoorApp : Application() {

    override fun onCreate() {
        super.onCreate()
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
