package com.elhajri.noor.ai

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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

    // تنزيل النظام (DownloadManager) — يبقى جارياً حتى لو أُغلق التطبيق أو قُفل الشاشة
    private const val KEY_DM_ID_PREFIX = "dm_download_id_"

    private fun dmPrefs(context: Context) = context.applicationContext.getSharedPreferences("noor_dm_prefs", Context.MODE_PRIVATE)

    private fun dmId(context: Context, model: AiModelDef): Long =
        dmPrefs(context).getLong(KEY_DM_ID_PREFIX + model.id, -1L)

    private fun saveDmId(context: Context, model: AiModelDef, id: Long) =
        dmPrefs(context).edit().putLong(KEY_DM_ID_PREFIX + model.id, id).apply()

    private fun clearDmId(context: Context, model: AiModelDef) =
        dmPrefs(context).edit().remove(KEY_DM_ID_PREFIX + model.id).apply()

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
     * تنزيل النموذج عبر DownloadManager الخاص بالنظام:
     * يستمر التنزيل حتى لو أُغلق التطبيق أو قُفل الشاشة أو انطفأ الهاتف مؤقتاً،
     * ويُستأنف تلقائياً عند عودة الشبكة، مع إشعار تقدم يعرضه النظام.
     */
    suspend fun download(
        context: Context,
        model: AiModelDef = getSelectedModel(context),
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        cancelRequested = false
        val app = context.applicationContext
        val dm = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // اكتمل ملف جزئي سابقاً؟ أنجزه فوراً
        val partFile = File(app.filesDir, "${model.file}.part")
        if (partFile.exists() && partFile.length() >= model.sizeBytes - 4096) {
            partFile.renameTo(modelFile(app, model))
            return@withContext true
        }
        if (isReady(app, model)) return@withContext true

        // تنزيل جارٍ من جلسة سابقة؟ تابع مراقبته (استئناف بعد إعادة فتح التطبيق)
        var id = dmId(app, model)
        if (id != -1L) {
            val q = DownloadManager.Query().setFilterById(id)
            dm.query(q)?.use { c ->
                if (!c.moveToFirst()) {
                    id = -1L
                    clearDmId(app, model)
                } else {
                    val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_FAILED || status == DownloadManager.STATUS_PAUSED && cancelRequested) {
                        // لا شيء — نتابع المراقبة
                    }
                }
            }
        }

        if (id == -1L) {
            val req = DownloadManager.Request(Uri.parse(model.url))
                .setTitle("تنزيل ${model.name}")
                .setDescription("نور — نموذج الذكاء الاصطناعي المحلي")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalFilesDir(app, null, model.file + ".dm")
            id = dm.enqueue(req)
            saveDmId(app, model, id)
        }

        // مراقبة التقدم — التنزيل نفسه في عملية النظام، والمراقبة هنا فقط
        val target = model.sizeBytes
        try {
            while (true) {
                val done = queryStatus(dm, id)
                when (done.first) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        // انقل الملف من مخزن التطبيق الخارجي إلى الداخلي
                        val externalPart = File(app.getExternalFilesDir(null), model.file + ".dm")
                        val finalFile = modelFile(app, model)
                        if (externalPart.exists()) {
                            externalPart.copyTo(finalFile, overwrite = true)
                            externalPart.delete()
                        } else if (partFile.exists()) {
                            partFile.renameTo(finalFile)
                        }
                        clearDmId(app, model)
                        onProgress(target, target)
                        return@withContext true
                    }
                    DownloadManager.STATUS_FAILED -> {
                        dm.remove(id)
                        clearDmId(app, model)
                        throw DownloadException("فشل التنزيل — تحقق من الإنترنت وأعد المحاولة (سيُستأنف من حيث توقف)")
                    }
                    else -> {
                        if (cancelRequested) {
                            // إلغاء المستخدم: نوقف التنزيل ونحفظ ما نُزّل
                            onProgress(done.second, target)
                            return@withContext false
                        }
                        onProgress(done.second, target)
                        delay(600)
                    }
                }
            }
            // الحلقة لا تنتهي طبيعياً — هذا خط دفاعي لرضا المترجم النوعي فقط
            return@withContext false
        } catch (e: DownloadException) {
            throw e
        } catch (e: Exception) {
            // انقطعت المراقبة (مثلاً أُغلق التطبيق) — التنزيل نفسه مستمر في النظام
            throw DownloadException("التنزيل مستمر في الخلفية — أعد فتح التطبيق لمتابعة التقدم")
        }
    }

    private fun queryStatus(dm: DownloadManager, id: Long): Pair<Int, Long> {
        val q = DownloadManager.Query().setFilterById(id)
        dm.query(q)?.use { c ->
            if (c.moveToFirst()) {
                val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val sofar = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                return Pair(status, sofar)
            }
        }
        return Pair(DownloadManager.STATUS_FAILED, 0L)
    }

    /**
     * عند إعادة فتح التطبيق: إن كان هناك تنزيل اكتمل أثناء الإغلاق، أنجز نقله.
     */
    fun finalizePendingDownload(context: Context, model: AiModelDef = getSelectedModel(context)) {
        val app = context.applicationContext
        val id = dmId(app, model)
        if (id == -1L) return
        val dm = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val (status, _) = queryStatus(dm, id)
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            val externalPart = File(app.getExternalFilesDir(null), model.file + ".dm")
            val finalFile = modelFile(app, model)
            if (externalPart.exists()) {
                externalPart.copyTo(finalFile, overwrite = true)
                externalPart.delete()
                clearDmId(app, model)
            }
        } else if (status == DownloadManager.STATUS_FAILED) {
            dm.remove(id)
            clearDmId(app, model)
        }
    }

    /** إلغاء المستخدم الصريح: يوقف مراقبة التقدم وينهي تنزيل النظام */
    fun cancelDownload(context: Context, model: AiModelDef = getSelectedModel(context)) {
        cancelRequested = true
        val app = context.applicationContext
        val id = dmId(app, model)
        if (id != -1L) {
            try {
                val dm = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.remove(id)
            } catch (_: Exception) { }
            clearDmId(app, model)
        }
    }

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
