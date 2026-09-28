package com.elhajri.noor.ai

import com.elhajri.noor.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * مساعد نُور الذكي — متصل مباشرة بـ Google Gemini API (AI Studio).
 * مستقل تماماً عن رصيد تكاملات Base44: لا يستهلك أي فلس من الرصيد الشهري.
 * المفتاح يُحقن وقت البناء من سرّ GitHub Actions، ولا يظهر في الكود.
 */
object GeminiClient {
    private const val MODEL = "gemini-1.5-flash"
    private const val URL =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    /**
     * @param systemPrompt تعليمات النظام (شخصية المساعد)
     * @param history أزواج (دور، نص) بالترتيب: "user" أو "model"
     * @return نص الرد من Gemini
     * @throws IOException برسالة عربية واضحة عند كل خطأ (مفتاح غير صالح، لا اتصال، حصة منتهية...)
     */
    fun ask(systemPrompt: String, history: List<Pair<String, String>>): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isBlank()) {
            throw IOException("🌙 المساعد الذكي غير مفعّل بعد: لم يُضبط مفتاح Gemini في هذه النسخة.")
        }

        val contents = JSONArray()
        for ((role, text) in history) {
            contents.put(
                JSONObject()
                    .put("role", if (role == "user") "user" else "model")
                    .put("parts", JSONArray().put(JSONObject().put("text", text)))
            )
        }
        val body = JSONObject()
            .put(
                "system_instruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            )
            .put("contents", contents)
            .put(
                "generationConfig",
                JSONObject().put("temperature", 0.7).put("maxOutputTokens", 2048)
            )

        val request = Request.Builder()
            .url(URL)
            .post(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .header("x-goog-api-key", key)
            .build()

        try {
            client.newCall(request).execute().use { resp ->
                val raw = resp.body?.string() ?: ""
                if (!resp.isSuccessful) {
                    val reason = try {
                        JSONObject(raw).getJSONObject("error").optString("message", "")
                    } catch (_: Exception) { "" }
                    throw IOException(friendlyApiError(resp.code, reason))
                }
                val json = JSONObject(raw)
                val candidates = json.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    throw IOException(blockedOrEmpty(raw))
                }
                val first = candidates.getJSONObject(0)
                val parts = first.optJSONObject("content")?.optJSONArray("parts")
                val sb = StringBuilder()
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        sb.append(parts.getJSONObject(i).optString("text", ""))
                    }
                }
                val reply = sb.toString().trim()
                if (reply.isBlank()) throw IOException(blockedOrEmpty(raw))
                return reply
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            // فشل شبكة (DNS، مهلة، إلخ)
            throw IOException("🌙 المساعد الذكي يحتاج إلى اتصال بالإنترنت. باقي خصائص التطبيق (القرآن، الأذكار، القبلة...) تعمل بدون إنترنت.")
        }
    }

    private fun friendlyApiError(code: Int, reason: String): String = when {
        code == 400 && reason.contains("API key not valid", true) ->
            "🌙 مفتاح Gemini غير صالح. أرسل مفتاحاً صحيحاً من aistudio.google.com/apikey (يبدأ بـ AIza) لإعادة تفعيل المساعد."
        code == 429 ->
            "🌙 وصلنا الحد المجاني المؤقت لخدمة Gemini. جرّب بعد دقيقة."
        code in 500..599 ->
            "🌙 خدمة Gemini مشغولة حالياً. جرّب مرة أخرى بعد قليل."
        else ->
            "🌙 المساعد الذكي غير متاح حالياً ($code). جرّب لاحقاً."
    }

    private fun blockedOrEmpty(raw: String): String {
        val finish = try {
            JSONObject(raw).getJSONArray("candidates").getJSONObject(0).optString("finishReason", "")
        } catch (_: Exception) { "" }
        return if (finish.contains("SAFETY", true) || finish.contains("RECITATION", true)) {
            "🌙 أعتذر، لا يمكنني الإجابة على هذا الطلب بصيغته الحالية. جرّب صياغة أخرى."
        } else {
            "🌙 تعذّر توليد رد. جرّب صياغة سؤالك بصورة أخرى."
        }
    }
}
