package com.elhajri.noor.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * تعريف نموذج ذكاء اصطناعي محلي — قابل للتوسع: إضافة نموذج جديد = عنصر واحد.
 */
data class AiModelDef(
    val id: String,
    val name: String,
    val description: String,
    val url: String,
    val file: String,
    val sizeBytes: Long,
    /** قالب المحادثة الرسمي للنموذج */
    val promptTemplate: String // GEMMA / QWEN
) {
    fun sizeLabel(): String =
        if (sizeBytes >= 1_000_000_000) String.format("%.1f جيجابايت", sizeBytes / 1_000_000_000.0)
        else "${sizeBytes / 1_048_576} ميجابايت"
}

/**
 * سجل النماذج المحلية المتاحة — نموذجان من Google/ LiteRT المجتمعية:
 * كامل (Gemma 4 E2B — الأقوى) وخفيف (Bonsai 1.7B ثلاثي الأوزان — أخف وأسرع).
 */
object AiModels {
    val FULL = AiModelDef(
        id = "full",
        name = "النموذج الكامل",
        description = "Gemma 4 E2B — الأقوى والأشمل للأجوبة المعمّقة",
        url = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it-web.task",
        file = "gemma-4-e2b-it.task",
        sizeBytes = 2003697664L,
        promptTemplate = "GEMMA"
    )

    val LIGHT = AiModelDef(
        id = "light",
        name = "النموذج الخفيف",
        description = "Bonsai 1.7B — أخف وأسرع للأسئلة اليومية (~750 ميجابايت)",
        url = "https://huggingface.co/litert-community/Ternary-Bonsai-1.7B/resolve/main/bonsai-1.7b-int2pc-32k-mp-crashfix.litertlm",
        file = "bonsai-1.7b.litertlm",
        sizeBytes = 786313170L,
        promptTemplate = "QWEN"
    )

    val all = listOf(FULL, LIGHT)

    fun byId(id: String): AiModelDef = all.find { it.id == id } ?: FULL
}

/**
 * محرك الذكاء الاصطناعي المحلي — يعمل بالكامل على هاتف المستخدم.
 * يُنزَّل النموذج المختار مرة واحدة، ثم يعمل بلا إنترنت وبلا حدود وبلا أي مفتاح API.
 * الأسئلة والأجوبة لا تترك هاتف المستخدم إطلاقاً.
 */
object LocalAiEngine {

    private const val PREFS = "noor_ai_prefs"
    private const val KEY_MODEL = "selected_model"

    // سجل المحادثة — يُحفظ في الذاكرة ويعاد بناء السياق لكل سؤال
    private val history = mutableListOf<Pair<String, String>>() // user, model
    private const val MAX_TURNS = 8

    @Volatile
    private var cancelRequested = false

    // ───────────────── اختيار النموذج ─────────────────

    fun getSelectedModel(context: Context): AiModelDef =
        AiModels.byId(
            context.applicationContext
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_MODEL, AiModels.FULL.id) ?: AiModels.FULL.id
        )

    fun setSelectedModel(context: Context, id: String) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_MODEL, id).apply()
        // تبديل النموذج يتطلب إعادة تهيئة المحرك
        close()
    }

    fun modelFile(context: Context, model: AiModelDef = getSelectedModel(context)): File =
        File(context.filesDir, model.file)

    /** هل اكتمل تنزيل النموذج المحدد؟ */
    fun isReady(context: Context, model: AiModelDef = getSelectedModel(context)): Boolean {
        val f = modelFile(context, model)
        return f.exists() && f.length() >= model.sizeBytes - 4096
    }

    /** المساحة الحرة المتاحة للتخزين الداخلي */
    fun freeBytes(context: Context): Long = try {
        android.os.StatFs(context.filesDir.absolutePath).availableBytes
    } catch (_: Exception) { 0L }

    class DownloadException(message: String) : IOException(message)

    /**
     * تنزيل النموذج مع استئناف تلقائي إن وُجد جزء سابق، وتقرير تقدم دوري.
     * يُنفَّذ على خيط الإدخال/الإخراج. يعيد true عند اكتمال التنزيل.
     */
    suspend fun download(
        context: Context,
        model: AiModelDef = getSelectedModel(context),
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        cancelRequested = false
        val partFile = File(context.filesDir, "${model.file}.part")
        var downloaded = if (partFile.exists()) partFile.length() else 0L
        if (downloaded >= model.sizeBytes) {
            partFile.renameTo(modelFile(context, model))
            return@withContext true
        }

        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(model.url)
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
            val total =
                if (downloaded > 0) model.sizeBytes
                else (body.contentLength().takeIf { it > 0 } ?: model.sizeBytes)

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

            if (downloaded >= model.sizeBytes - 4096) {
                partFile.renameTo(modelFile(context, model))
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

    fun deleteModel(context: Context, model: AiModelDef = getSelectedModel(context)) {
        close()
        modelFile(context, model).delete()
        File(context.filesDir, "${model.file}.part").delete()
    }

    // ===================== محرك الاستدلال (MediaPipe GenAI) =====================

    private var llm: com.google.mediapipe.tasks.genai.llminference.LlmInference? = null
    private var initialized = false
    private var initializedModelId: String? = null

    /**
     * تهيئة المحرك — تحميل النموذج المختار إلى الذاكرة (قد يستغرق ثواني).
     */
    suspend fun ensureInitialized(context: Context) = withContext(Dispatchers.IO) {
        val model = getSelectedModel(context)
        if (initialized && initializedModelId == model.id) return@withContext
        close()
        val options = com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelFile(context, model).absolutePath)
            .setMaxTokens(1024)
            .build()
        llm = com.google.mediapipe.tasks.genai.llminference.LlmInference.createFromOptions(
            context, options
        )
        initialized = true
        initializedModelId = model.id
    }

    private const val SYSTEM_PROMPT = "أنت «نور»، مساعد إسلامي لطيف ومحترم على هاتف المستخدم. " +
        "أجب بالعربية الفصحى الواضحة باختصار وعناية، مستنداً إلى القرآن الكريم والسنة النبوية الصحيحة، " +
        "وإن جهلت شيئاً فقل بصراحة إنك لا تعرفه ولا تختلق أحاديث ولا نصوصاً."

    /**
     * إرسال سؤال والحصول على الإجابة — تشغيل محلي كامل على معالج الهاتف.
     * يحمل سياق المحادثة بقالب النموذج المختار.
     */
    suspend fun ask(context: Context, question: String): String = withContext(Dispatchers.IO) {
        val engine = llm ?: throw IllegalStateException("المحرك غير مهيأ")
        val model = getSelectedModel(context)

        val sb = StringBuilder()
        when (model.promptTemplate) {
            "QWEN" -> {
                sb.append("<|im_start|>system\n").append(SYSTEM_PROMPT).append("<|im_end|>\n")
                for ((u, m) in history.takeLast(MAX_TURNS)) {
                    sb.append("<|im_start|>user\n").append(u).append("<|im_end|>\n")
                    sb.append("<|im_start|>assistant\n").append(m).append("<|im_end|>\n")
                }
                sb.append("<|im_start|>user\n").append(question).append("<|im_end|>\n")
                sb.append("<|im_start|>assistant\n")
            }
            else -> { // GEMMA
                sb.append(SYSTEM_PROMPT).append("\n\n")
                for ((u, m) in history.takeLast(MAX_TURNS)) {
                    sb.append("<start_of_turn>user\n").append(u).append("<end_of_turn>\n")
                    sb.append("<start_of_turn>model\n").append(m).append("<end_of_turn>\n")
                }
                sb.append("<start_of_turn>user\n").append(question).append("<end_of_turn>\n")
                sb.append("<start_of_turn>model\n")
            }
        }

        val answer = engine.generateResponse(sb.toString())
        history.add(question to answer)
        answer.trim()
    }

    fun clearConversation() { history.clear() }

    fun close() {
        try { llm?.close() } catch (_: Exception) {}
        llm = null
        initialized = false
        initializedModelId = null
    }
}
