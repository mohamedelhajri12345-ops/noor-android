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
 * المساعد الذكي — نفس فكرة الكود المصدري (InvokeLLM مع prompt واحد)،
 * مبنية على Google AI Studio (Gemini) بمفتاح المستخدم المجاني:
 * موثوقة وسريعة ولا تعتمد على أي رصيد تكاملات.
 */
object AssistantClient {

    private val effectiveKey: String
        get() = if (com.elhajri.noor.BuildConfig.GEMINI_API_KEY.startsWith("AIza")) com.elhajri.noor.BuildConfig.GEMINI_API_KEY else "Ab8RN6IN0mGQrY0c9iTSyTZ_1Vrh6tjXHbu7z1I4ktosOMTEUw"
    private const val MODEL = "gemini-2.0-flash"

    private val json = "application/json; charset=utf-8".toMediaType()
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /** مفتاح Gemini صالح فقط إن بدأ بـ AIza (مفتاح Google AI Studio) */
    private fun hasValidKey() = effectiveKey.startsWith("AIza") && effectiveKey.length > 30

    /**
     * نفس توقيع الواجهة: systemPrompt + سجل المحادثة (user/model, نص).
     * المزوّد الأول: Gemini (إن توفر مفتاح صالح) — موثوق وفوري.
     * المزوّد الاحتياطي: خدمة مجانية بلا مفتاح مع 3 محاولات.
     */
    fun ask(systemPrompt: String, history: List<Pair<String, String>>): String {
        if (hasValidKey()) {
            try {
                return askGemini(systemPrompt, history)
            } catch (e: IOException) {
                if ((e.message ?: "").contains("غير صالح")) {
                    // مفتاح خاطئ — لا نكرر، انتقل للمزوّد الاحتياطي
                } else if ((e.message ?: "").contains("الإنترنت")) throw e
            }
        }
        var lastError: IOException? = null
        for (attempt in 1..3) {
            try {
                return askPollinations(systemPrompt, history)
            } catch (e: IOException) {
                lastError = e
                val msg = e.message ?: ""
                if (msg.contains("الإنترنت")) throw e
                if (attempt < 3) Thread.sleep(if (attempt == 1) 1200L else 3000L)
            }
        }
        throw lastError ?: IOException("🌙 تعذّر الوصول إلى المساعد الذكي. جرّب مرة أخرى بعد قليل.")
    }

    /** خدمة مجانية بلا مفتاح — نفس أسلوب الكود المصدري: prompt واحد بالسجل كاملاً */
    private fun askPollinations(systemPrompt: String, history: List<Pair<String, String>>): String {
        val conversation = history.joinToString("\n") { (role, text) ->
            (if (role == "user") "المستخدم: " else "المساعد: ") + text
        }
        val prompt = systemPrompt + "\n\n" + conversation + "\n\nالمساعد:"
        val body = JSONObject().apply {
            put("model", "openai")
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt))
                put(JSONObject().put("role", "user").put("content", conversation + "\n\nالمساعد:"))
            })
        }
        val request = Request.Builder()
            .url("https://text.pollinations.ai/openai")
            .post(body.toString().toRequestBody(json))
            .build()
        try {
            http.newCall(request).execute().use { res ->
                val text = res.body?.string() ?: throw IOException("استجابة فارغة")
                if (!res.isSuccessful) throw IOException("الخدمة مشغولة (رمز ${res.code})")
                val parsed = JSONObject(text)
                    .optJSONArray("choices")?.optJSONObject(0)
                    ?.optJSONObject("message")?.optString("content", "") ?: ""
                val out = parsed.trim()
                if (out.isEmpty()) throw IOException("رد فارغ — أعد المحاولة")
                return out
            }
        } catch (e: java.net.UnknownHostException) {
            throw IOException("🌙 عذرًا، المساعد الذكي يحتاج إلى اتصال بالإنترنت.")
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
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 1024)
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$effectiveKey")
            .post(body.toString().toRequestBody(json))
            .header("Content-Type", "application/json")
            .build()

        val response = try { http.newCall(request).execute() } catch (e: java.net.UnknownHostException) {
            throw IOException("🌙 عذرًا، المساعد الذكي يحتاج إلى اتصال بالإنترنت.")
        } catch (e: IOException) {
            throw e
        }

        response.use { res ->
            val text = res.body?.string() ?: throw IOException("🌙 استجابة فارغة من الخدمة.")
            if (!res.isSuccessful) {
                when (res.code) {
                    400, 403 -> throw IOException("مفتاح المساعد الذكي غير صالح — أعد توليده من Google AI Studio.")
                    429 -> throw IOException("تم تجاوز حد الطلبات المجانية مؤقتاً — انتظر دقيقة وحاول مجدداً.")
                    in 500..599 -> throw IOException("الخدمة مشغولة حالياً — أعد المحاولة.")
                    else -> throw IOException("تعذر الوصول إلى المساعد الذكي (رمز ${res.code}).")
                }
            }
            val out = JSONObject(text)
                .optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")
            val reply = StringBuilder()
            out?.let { parts ->
                for (i in 0 until parts.length()) {
                    reply.append(parts.optJSONObject(i)?.optString("text", "") ?: "")
                }
            }
            val result = reply.toString().trim()
            if (result.isEmpty()) throw IOException("لم يصل رد من المساعد الذكي — أعد المحاولة.")
            return result
        }
    }
}
