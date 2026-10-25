package com.elhajri.noor.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * رسالة محادثة بين المستخدم والمساعد الذكي.
 */
data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

/**
 * محرك الذكاء الاصطناعي الذكي باستعمال Free LLM API.
 * يتضمن:
 * 1. OkHttp للاتصال بالشبكة مع إعدادات مهلة زمنية دقيقة.
 * 2. إعادة المحاولة التلقائية مرة واحدة (Retry Once) عند حدوث خطأ أو انقطاع.
 * 3. رسالة خطأ بديلة باللغة العربية وعدم الانهيار الإطلاقًا (Never Crash).
 * 4. حفظ سياق المحادثة المباشر.
 */
object FreeLlmApiEngine {

    private const val MAX_HISTORY_TURNS = 10
    private val history = mutableListOf<ChatMessage>()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    const val FALLBACK_ERROR_MESSAGE = "عذراً، تعذّر الاتصال بالمساعد الذكي حالياً. يرجى التأكد من الاتصال بالإنترنت وإعادة المحاولة."

    /**
     * إرسال سؤال المستخدم واستلام الإجابة مع حماية شاملة ضد الأخطاء.
     */
    suspend fun ask(userQuestion: String): String = withContext(Dispatchers.IO) {
        val trimmed = userQuestion.trim()
        if (trimmed.isEmpty()) {
            return@withContext "يرجى كتابة سؤالك قبل الإرسال."
        }

        // المحاولة الأولى
        try {
            val response = executeChatRequest(trimmed)
            if (response != null && response.isNotBlank()) {
                saveTurn(trimmed, response)
                return@withContext response
            }
        } catch (_: Throwable) {
            // تجاهل خطأ المحاولة الأولى والاستعداد لإعادة المحاولة
        }

        // إعادة المحاولة مرة واحدة بعد تأخير قصير
        try {
            delay(1000)
            val retryResponse = executeChatRequest(trimmed)
            if (retryResponse != null && retryResponse.isNotBlank()) {
                saveTurn(trimmed, retryResponse)
                return@withContext retryResponse
            }
        } catch (_: Throwable) {
            // خطأ في إعادة المحاولة
        }

        return@withContext FALLBACK_ERROR_MESSAGE
    }

    /**
     * تنفيذ طلب HTTP الشريطي بأسلوب OpenAI-compatible chat completions.
     */
    private fun executeChatRequest(question: String): String? {
        val jsonPayload = JSONObject().apply {
            put("model", FreeLlmConfig.modelName)

            val messagesArray = JSONArray()

            // 1. النظام (System Prompt)
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", FreeLlmConfig.SYSTEM_PROMPT)
            })

            // 2. سياق المحادثة السابقة
            val recentHistory = history.takeLast(MAX_HISTORY_TURNS)
            for (msg in recentHistory) {
                messagesArray.put(JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                })
            }

            // 3. السؤال الحالي
            messagesArray.put(JSONObject().apply {
                put("role", "user")
                put("content", question)
            })

            put("messages", messagesArray)
        }

        val requestBody = jsonPayload.toString().toRequestBody(mediaTypeJson)

        val requestBuilder = Request.Builder()
            .url(FreeLlmConfig.baseUrl)
            .post(requestBody)
            .header("Content-Type", "application/json")

        if (FreeLlmConfig.apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer ${FreeLlmConfig.apiKey}")
        }

        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return null
            }

            val bodyString = response.body?.string() ?: return null
            val jsonObj = JSONObject(bodyString)

            val choices = jsonObj.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val firstChoice = choices.optJSONObject(0)
                val messageObj = firstChoice?.optJSONObject("message")
                val content = messageObj?.optString("content")
                if (!content.isNullOrBlank()) {
                    return content.trim()
                }
            }
        }

        return null
    }

    private fun saveTurn(question: String, answer: String) {
        synchronized(history) {
            history.add(ChatMessage("user", question))
            history.add(ChatMessage("assistant", answer))
            while (history.size > MAX_HISTORY_TURNS * 2) {
                history.removeAt(0)
            }
        }
    }

    /**
     * مسح سجل المحادثة.
     */
    fun clearHistory() {
        synchronized(history) {
            history.clear()
        }
    }

    /**
     * الحصول على سجل المحادثة الحالي.
     */
    fun getHistory(): List<ChatMessage> {
        synchronized(history) {
            return history.toList()
        }
    }
}
