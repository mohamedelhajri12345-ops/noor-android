package com.elhajri.noor.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * المساعد الذكي — نفس أسلوب الكود المصدري (InvokeLLM بسجل المحادثة كاملاً).
 * مزوّد مجاني بلا مفتاح ولا يستهلك أي رصيد، مع إعادة محاولة عبر نماذج متعددة.
 * إن أُضيف مفتاح Gemini صالح (AIza...) لاحقاً فهو المسار الأول تلقائياً.
 */
object AssistantClient {

    private val geminiKey: String
        get() {
            val k = com.elhajri.noor.BuildConfig.GEMINI_API_KEY
            return if (k != null && k.startsWith("AIza") && k.length > 30) k else ""
        }
    private const val GEMINI_MODEL = "gemini-2.0-flash"

    // نماذج مجانية بالترتيب الأفضل أولاً — بلا مفتاح إطلاقاً
    private val FREE_MODELS = listOf("openai", "openai-fast", "mistral")

    private val json = "application/json; charset=utf-8".toMediaType()
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    fun ask(systemPrompt: String, history: List<Pair<String, String>>): String {
        // المسار الأول: Gemini إن وُجد مفتاح صالح
        if (geminiKey.isNotEmpty()) {
            try { return askGemini(systemPrompt, history) } catch (_: IOException) {}
        }
        // المسار المجاني: عدة نماذج × جولتان — تحمّل ازدحام الخدمة
        var lastError: IOException? = null
        for (round in 1..2) {
            for (model in FREE_MODELS) {
                try {
                    return askPollinations(model, systemPrompt, history)
                } catch (e: IOException) {
                    lastError = e
                    val msg = e.message ?: ""
                    if (msg.contains("الإنترنت")) throw e
                }
            }
        }
        throw lastError ?: IOException("🌙 تعذّر الوصول إلى المساعد الذكي. جرّب مرة أخرى بعد قليل.")
    }

    /** نموذج مجاني بلا مفتاح — بلا أي ترويسة تصريح (الترويسة الخاطئة تعطّل الخدمة) */
    private fun askPollinations(model: String, systemPrompt: String, history: List<Pair<String, String>>): String {
        val conversation = history.joinToString("\n") { (role, text) ->
            (if (role == "user") "المستخدم: " else "المساعد: ") + text
        }
        val body = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt))
                put(JSONObject().put("role", "user").put("content", conversation + "\n\nالمساعد:"))
            })
        }
        val request = Request.Builder()
            .url("https://text.pollinations.ai/openai")
            .post(body.toString().toRequestBody(json))
            .build()
        http.newCall(request).execute().use { res ->
            val text = res.body?.string() ?: throw IOException("استجابة فارغة")
            if (!res.isSuccessful) throw IOException("الخدمة مشغولة (رمز ${res.code})")
            val parsed = try { JSONObject(text) } catch (_: org.json.JSONException) {
                throw IOException("تنسيق استجابة غير صالح")
            }.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content", "") ?: ""
            val out = parsed.trim()
            if (out.isEmpty()) throw IOException("رد فارغ من النموذج")
            return out
        }
    }

    private fun askGemini(systemPrompt: String, history: List<Pair<String, String>>): String {
        val body = JSONObject().apply {
            put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
            put("contents", JSONArray().apply {
                history.forEach { (role, text) ->
                    put(JSONObject().apply {
                        put("role", if (role == "user") "user" else "model")
                        put("parts", JSONArray().put(JSONObject().put("text", text)))
                    })
                }
            })
            put("generationConfig", JSONObject().apply { put("temperature", 0.7); put("maxOutputTokens", 1024) })
        }
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent?key=$geminiKey")
            .post(body.toString().toRequestBody(json))
            .header("Content-Type", "application/json")
            .build()
        http.newCall(request).execute().use { res ->
            val text = res.body?.string() ?: throw IOException("استجابة فارغة")
            if (!res.isSuccessful) throw IOException("مفتاح Gemini غير صالح (رمز ${res.code})")
            val parts = try { JSONObject(text) } catch (_: org.json.JSONException) { throw IOException("تنسيق غير صالح") }
                .optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
            val reply = StringBuilder()
            parts?.let { ps -> for (i in 0 until ps.length()) reply.append(ps.optJSONObject(i)?.optString("text", "") ?: "") }
            val result = reply.toString().trim()
            if (result.isEmpty()) throw IOException("لم يصل رد من Gemini")
            return result
        }
    }
}
