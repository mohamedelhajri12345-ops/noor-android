package com.elhajri.noor.ai

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * مساعد نُور الذكي — متصل بخدمة ذكاء اصطناعي *مجانية 100% وبدون أي مفتاح API*
 * (Pollinations.ai — واجهة متوافقة مع OpenAI). لا يستهلك أي رصيد تكاملات
 * ولا يحتاج تسجيلاً ولا بطاقة بنكية ولا مفتاحاً على الإطلاق.
 */
object AssistantClient {
    private const val URL = "https://text.pollinations.ai/openai"
    private const val MODEL = "openai"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    /**
     * @param systemPrompt تعليمات النظام (شخصية المساعد)
     * @param history أزواج (دور، نص) بالترتيب: "user" أو "model"
     * @return نص الرد
     * @throws IOException برسالة عربية واضحة عند كل خطأ (لا اتصال، خدمة مشغولة...)
     */
    /**
     * نداء واحد مع إعادة محاولة تلقائية: الخدمة المجانية قد "تستيقظ ببطء"
     * في أول نداء (وهذا سبب خطأ الرسالة الأولى) — نجرب حتى 3 مرات
     * مع مهلة متزايدة قبل الاستسلام.
     */
    fun ask(systemPrompt: String, history: List<Pair<String, String>>): String {
        var lastError: IOException? = null
        for (attempt in 1..3) {
            try {
                return askOnce(systemPrompt, history)
            } catch (e: IOException) {
                lastError = e
                // لا نعيد المحاولة في أخطاء "لا إنترنت" الحتمية
                val msg = e.message ?: ""
                if (msg.contains("الإنترنت")) throw e
                if (attempt < 3) Thread.sleep(if (attempt == 1) 1200L else 3000L)
            }
        }
        throw lastError ?: IOException("🌙 تعذّر الوصول إلى المساعد الذكي. جرّب مرة أخرى بعد قليل.")
    }

    private fun askOnce(systemPrompt: String, history: List<Pair<String, String>>): String {
        val messages = JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
        for ((role, text) in history) {
            messages.put(
                JSONObject()
                    .put("role", if (role == "user") "user" else "assistant")
                    .put("content", text)
            )
        }

        val body = JSONObject()
            .put("model", MODEL)
            .put("messages", messages)
            .put("temperature", 0.7)

        val request = Request.Builder()
            .url(URL)
            .post(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { resp ->
                val raw = resp.body?.string() ?: ""
                if (!resp.isSuccessful) {
                    throw IOException(
                        if (resp.code in 500..599 || resp.code == 429)
                            "🌙 خدمة المساعد الذكي مشغولة حالياً. جرّب مرة أخرى بعد قليل."
                        else
                            "🌙 المساعد الذكي غير متاح حالياً ($resp.code). جرّب لاحقاً."
                    )
                }
                val json = JSONObject(raw)
                val content = json
                    .optJSONArray("choices")?.optJSONObject(0)
                    ?.optJSONObject("message")?.optString("content", "") ?: ""
                val reply = content.trim()
                if (reply.isBlank()) {
                    throw IOException("🌙 تعذّر توليد رد. جرّب صياغة سؤالك بصورة أخرى.")
                }
                return reply
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("🌙 المساعد الذكي يحتاج إلى اتصال بالإنترنت. باقي خصائص التطبيق (القرآن، الأذكار، القبلة...) تعمل بدون إنترنت.")
        }
    }
}
