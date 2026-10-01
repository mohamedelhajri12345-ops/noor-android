package com.elhajri.noor.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * محرك الذكاء الاصطناعي المحلي — يعمل بالكامل على هاتف المستخدم.
 *
 * نموذج Gemma 4 E2B الرسمي من Google (LiteRT/MediaPipe) بحجم ~٢ جيجابايت:
 * يُنزَّل مرة واحدة داخل التطبيق، ثم يعمل بلا إنترنت وبلا حدود وبلا أي مفتاح API.
 * الأسئلة والأجوبة لا ت leave هاتف المستخدم إطلاقاً.
 */
object LocalAiEngine {

    private const val MODEL_URL =
        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it-web.task"
    private const val MODEL_SIZE = 2003697664L
    private const val MODEL_FILE = "gemma-4-e2b-it.task"

    // سجل المحادثة — يُحفظ في الذاكرة ويعاد بناء السياق لكل سؤال
    private val history = mutableListOf<Pair<String, String>>() // user, model
    private const val MAX_TURNS = 8

    @Volatile
    private var cancelRequested = false

    fun modelFile(context: Context): File = File(context.filesDir, MODEL_FILE)

    /** هل اكتمل تنزيل النموذج؟ */
    fun isReady(context: Context): Boolean {
        val f = modelFile(context)
        return f.exists() && f.length() >= MODEL_SIZE - 4096
    }

    /** المساحة الحرة المتاحة للتخزين الداخلي */
    fun freeBytes(context: Context): Long = try {
        android.os.StatFs(context.filesDir.absolutePath).availableBytes
    } catch (_: Exception) { 0L }

    fun modelSizeLabel(): String = "٢ جيجابايت تقريباً"

    class DownloadException(message: String) : IOException(message)

    /**
     * تنزيل النموذج مع استئناف تلقائي إن وُجد جزء سابق، وتقرير تقدم دوري.
     * يُنفَّذ على خيط الإدخال/الإخراج. يعيد true عند اكتمال التنزيل.
     */
    suspend fun download(
        context: Context,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        cancelRequested = false
        val partFile = File(context.filesDir, "$MODEL_FILE.part")
        var downloaded = if (partFile.exists()) partFile.length() else 0L
        if (downloaded >= MODEL_SIZE) {
            partFile.renameTo(modelFile(context))
            return@withContext true
        }

        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(MODEL_URL)
            .apply { if (downloaded > 0) header("Range", "bytes=$downloaded-") }
            .build()

        val response = try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            throw DownloadException("تعذّر الاتصال بالشبكة — تأكد من الإنترنت وحاول مجدداً")
        }

        try {
            if (!response.isSuccessful) {
                throw DownloadException("تعذّر بدء التنزيل (رمز ${response.code})")
            }
            val body = response.body ?: throw DownloadException("استجابة تنزيل فارغة")
            val total = if (downloaded > 0) MODEL_SIZE else (body.contentLength().takeIf { it > 0 } ?: MODEL_SIZE)

            val input = body.byteStream()
            val output: java.io.FileOutputStream =
                if (downloaded > 0) java.io.FileOutputStream(partFile, true)
                else java.io.FileOutputStream(partFile)
            val buffer = ByteArray(64 * 1024)
            var sinceLastReport = 0L
            while (true) {
                if (cancelRequested) {
                    output.close(); input.close(); response.close()
                    return@withContext false
                }
                val read = input.read(buffer)
                if (read == -1) break
                output.write(buffer, 0, read)
                downloaded += read
                sinceLastReport += read
                if (sinceLastReport >= 512 * 1024 || read < buffer.size) {
                    sinceLastReport = 0
                    onProgress(downloaded, total)
                }
            }
            output.flush(); output.close(); input.close(); response.close()

            if (downloaded >= MODEL_SIZE - 4096) {
                partFile.renameTo(modelFile(context))
                true
            } else {
                // اكتمل الاتصال قبل اكتمال الملف — سيُستأنف في المحاولة التالية
                throw DownloadException("انقطع التنزيل قبل الاكتمال — أعد المحاولة")
            }
        } finally {
            response.close()
        }
    }

    fun cancelDownload() { cancelRequested = true }

    fun deleteModel(context: Context) {
        close()
        modelFile(context).delete()
        File(context.filesDir, "$MODEL_FILE.part").delete()
    }

    // ===================== محرك الاستدلال (MediaPipe GenAI) =====================

    private var llm: com.google.mediapipe.tasks.genai.llminference.LlmInference? = null
    private var initialized = false

    /**
     * تهيئة المحرك — تحميل النموذج إلى الذاكرة (قد يستغرق ثواني).
     */
    suspend fun ensureInitialized(context: Context) = withContext(Dispatchers.IO) {
        if (initialized) return@withContext
        val options = com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelFile(context).absolutePath)
            .setMaxTokens(1024)
            .build()
        llm = com.google.mediapipe.tasks.genai.llminference.LlmInference.createFromOptions(
            context, options
        )
        initialized = true
    }

    /**
     * إرسال سؤال والحصول على الإجابة — تشغيل محلي كامل على معالج الهاتف.
     * يحمل سياق المحادثة بتنسيق Gemma الرسمي.
     */
    suspend fun ask(context: Context, question: String): String = withContext(Dispatchers.IO) {
        val engine = llm ?: throw IllegalStateException("المحرك غير مهيأ")
        val systemPrompt = "أنت «نور»، مساعد إسلامي لطيف ومحترم على هاتف المستخدم. " +
            "أجب بالعربية الفصحى الواضحة باختصار وعناية، مستنداً إلى القرآن الكريم والسنة النبوية الصحيحة، " +
            "وإن جهلت شيئاً فقل بصراحة إنك لا تعرفه ولا تختلق أحاديث ولا نصوصاً."

        val sb = StringBuilder()
        sb.append(systemPrompt).append("\n\n")
        val recent = history.takeLast(MAX_TURNS)
        for ((u, m) in recent) {
            sb.append("<start_of_turn>user\n").append(u).append("<end_of_turn>\n")
            sb.append("<start_of_turn>model\n").append(m).append("<end_of_turn>\n")
        }
        sb.append("<start_of_turn>user\n").append(question).append("<end_of_turn>\n")
        sb.append("<start_of_turn>model\n")

        val answer = engine.generateResponse(sb.toString())
        history.add(question to answer)
        answer.trim()
    }

    fun clearConversation() { history.clear() }

    fun close() {
        try { llm?.close() } catch (_: Exception) {}
        llm = null
        initialized = false
    }
}
