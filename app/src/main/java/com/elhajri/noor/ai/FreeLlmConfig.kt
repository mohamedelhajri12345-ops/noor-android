package com.elhajri.noor.ai

import com.elhajri.noor.BuildConfig

/**
 * إعدادات مفتاح ورابط API للذكاء الاصطناعي المجاني (Free LLM API).
 *
 * =========================================================================
 * دليل المالك / المطور لتحديث إعدادات الذكاء الاصطناعي (Free LLM API)
 * =========================================================================
 * يمكنك تغيير أي من القيم التالية في مكان واحد هنا أو عبر app/build.gradle.kts:
 *
 * 1. BASE_URL:
 *    • الافتراضي العام (Public Open Router): "https://text.pollinations.ai/v1/chat/completions"
 *    • الخادم المحلي (FreeLLMAPI Container): "http://localhost:3001/v1/chat/completions"
 *    • خادمك الخاص (Self-Hosted Proxy): "https://your-domain.com/v1/chat/completions"
 *
 * 2. API_KEY:
 *    • مفتاح التوثيق للخدمة (Authorization: Bearer <API_KEY>)
 *    • الافتراضي: "freellmapi-your-unified-key"
 *
 * 3. MODEL_NAME:
 *    • اسم النموذج المطلوب للتوليد.
 *    • أمثلة متاحة:
 *      - "openai-fast" (الافتراضي السريع للعموم)
 *      - "groq/llama-3.3-70b-versatile" (للخادم المحلي)
 *      - "cerebras/llama-3.3-70b"
 *      - "google/gemini-2.0-flash"
 * =========================================================================
 */
object FreeLlmConfig {

    val baseUrl: String
        get() = try {
            BuildConfig.FREE_LLM_API_BASE_URL.ifEmpty { DEFAULT_BASE_URL }
        } catch (_: Throwable) {
            DEFAULT_BASE_URL
        }

    val apiKey: String
        get() = try {
            BuildConfig.FREE_LLM_API_KEY.ifEmpty { DEFAULT_API_KEY }
        } catch (_: Throwable) {
            DEFAULT_API_KEY
        }

    val modelName: String
        get() = try {
            BuildConfig.FREE_LLM_API_MODEL.ifEmpty { DEFAULT_MODEL_NAME }
        } catch (_: Throwable) {
            DEFAULT_MODEL_NAME
        }

    const val DEFAULT_BASE_URL = "https://text.pollinations.ai/v1/chat/completions"
    const val DEFAULT_API_KEY = "freellmapi-your-unified-key"
    const val DEFAULT_MODEL_NAME = "openai-fast"

    /**
     * الموجه الموحد (System Prompt) الخاص بالمساعد الإسلامي الذكي "نور".
     */
    const val SYSTEM_PROMPT = """أنت "نور" — مساعد إسلامي ذكي، مؤدب، ونافع لتطبيق نور (NOOR).
تجيب دائمًا باللغة العربية الفصحى فقط بأسلوب راقٍ وموجز.
تقدم إجابات مستندة إلى كتاب الله وسنة رسوله ﷺ، وتهتم بدقة المعلومات الشرعية والأخلاق الفاضلة.
إذا سُئلت عن أمور غير متعلقة بالمعرفة والعلوم النافعة أو الإسلام، وجه السائل بلطف وإيجابية للأسئلة النافعة."""
}
