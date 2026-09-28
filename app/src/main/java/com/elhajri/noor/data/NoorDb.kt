package com.elhajri.noor.ai

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * قاعدة بيانات محلية للتطبيق — الجزء الأول: محادثات المساعد الذكي.
 * تحل مشكلتين:
 *  1) الرسائل تُحفظ على الجهاز فلا تُفقد المحادثة عند إغلاق التطبيق.
 *  2) يُرسل للمخدم آخر 10 رسائل فقط بدل المحادثة كاملة — استهلاك أقل
 *     وسرعة أعلى، مع بقاء السياق كاملاً محفوظاً في القاعدة.
 */
class NoorDb(context: Context) : SQLiteOpenHelper(
    context.applicationContext, "noor.db", null, 1
) {
    companion object {
        private const val T_AI = "ai_messages"
        fun get(context: Context): NoorDb = NoorDb(context)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $T_AI (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                role TEXT NOT NULL,
                content TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $T_AI")
        onCreate(db)
    }

    // -------------------------------------------------- رسائل المساعد

    fun addAiMessage(role: String, content: String) {
        val v = ContentValues().apply {
            put("role", role)
            put("content", content)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insert(T_AI, null, v)
    }

    /** آخر limit رسالة مرتبة زمنياً (الأقدم أولاً) */
    fun recentAiMessages(limit: Int = 10): List<Pair<String, String>> {
        val out = mutableListOf<Pair<String, String>>()
        readableDatabase.rawQuery(
            "SELECT role, content FROM $T_AI ORDER BY id DESC LIMIT ?", arrayOf("$limit")
        ).use { c ->
            while (c.moveToNext()) out.add(c.getString(0) to c.getString(1))
        }
        return out.reversed()
    }

    fun allAiMessages(): List<Pair<String, String>> {
        val out = mutableListOf<Pair<String, String>>()
        readableDatabase.rawQuery(
            "SELECT role, content FROM $T_AI ORDER BY id ASC", null
        ).use { c ->
            while (c.moveToNext()) out.add(c.getString(0) to c.getString(1))
        }
        return out
    }

    fun clearAiMessages() {
        writableDatabase.delete(T_AI, null, null)
    }
}
