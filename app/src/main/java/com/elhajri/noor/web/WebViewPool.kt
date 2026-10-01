package com.elhajri.noor.web

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elhajri.noor.settings.NoorSettings
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * مجمع WebView — لتحميل صفحات الموقع مسبقاً عند إقلاع التطبيق،
 * فتفتح فوراً عند الضغط عليها كأنها جزء أصيل من التطبيق بلا انتظار.
 *
 * نفس WebViews المسبقة تُعاد إلى المجمع عند إغلاق الشاشة،
 * فيكون فتحها في المرة التالية لحظياً.
 */
object WebViewPool {

    interface LoadListener {
        fun onStarted()
        fun onFinished()
        fun onError()
    }

    private val ready = ConcurrentHashMap<String, WebView>()

    /** هل يوجد اتصال بالإنترنت الآن؟ */
    fun isOnline(context: Context): Boolean = try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val net = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(net)
        caps != null && caps.hasCapability(android.net.NetCapabilities.NET_CAPABILITY_INTERNET)
    } catch (_: Exception) { false }

    /** مسار الأرشيف المحلي للصفحة — لفتحها دون إنترنت */
    private fun archivePath(context: Context, url: String): File? {
        val name = when {
            url.startsWith(NoorWeb.QURAN) -> "quran"
            url.startsWith(NoorWeb.LIBRARY) -> "library"
            else -> return null
        }
        return File(context.filesDir, "arch-$name.webarchive")
    }

    /** حفظ لقطة الصفحة عند اكتمالها — تصبح متاحة دون إنترنت لاحقاً */
    fun saveArchive(context: Context, view: WebView?) {
        val view = view ?: return
        val current = try { view.url ?: return } catch (_: Exception) { return }
        val file = archivePath(context, current) ?: return
        try { view.saveWebArchive(file.absolutePath) } catch (_: Exception) {}
    }

    /** فتح الأرشيف المحلي — يعيد true إن وُجد وفتُح */
    fun loadOffline(context: Context, view: WebView, url: String): Boolean {
        val file = archivePath(context, url) ?: return false
        if (!file.exists()) return false
        return try { view.loadUrl("file://" + file.absolutePath); true } catch (_: Exception) { false }
    }

    /**
     * عميل WebView موحّد لكل صفحات الموقع:
     * - HTTPS فقط: يُمنع أي محتوى HTTP غير الآمن.
     * - روابط الموقع تفتح داخل التطبيق، والروابط الخارجية المناسبة (https/mailto/tel)
     *   تفتح في المتصفح — لا شيء مجهول يفتح داخل التطبيق.
     */
    fun noorClient(context: Context, listener: LoadListener?): WebViewClient =
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                listener?.onStarted()
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                view?.evaluateJavascript(NoorSettings.webSyncJs(context), null)
                view?.evaluateJavascript(NoorWeb.hideSiteChromeJs, null)
                view?.evaluateJavascript(NoorWeb.audioHooksJs, null)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                listener?.onFinished()
                view?.evaluateJavascript(NoorSettings.webSyncJs(context), null)
                view?.evaluateJavascript(NoorWeb.hideSiteChromeJs, null)
                view?.evaluateJavascript(NoorWeb.audioHooksJs, null)
                // لقطة محفوظة: تفتح الصفحة لاحقاً حتى دون إنترنت
                saveArchive(context, view)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) listener?.onError()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return true
                val urlStr = uri.toString()
                // صفحات موقعنا: داخل التطبيق
                if (urlStr.startsWith(NoorWeb.BASE)) return false
                // روابط خارجية مقصودة: بريد وهاتف وروابط https موثوقة — في المتصفح
                val scheme = (uri.scheme ?: "").lowercase()
                if (scheme == "mailto" || scheme == "tel") {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (_: Exception) {}
                    return true
                }
                if (scheme == "https") {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (_: Exception) {}
                    return true
                }
                // http غير الآمن وأي مخطط غريب: ممنوع
                return true
            }
        }

    /** بناء WebView كامل الإعداد — نفس إعدادات الشاشة تماماً */
    fun build(context: Context, url: String, listener: LoadListener?): WebView {
        return WebView(context).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#0A0E14"))
            addJavascriptInterface(NoorAudioBridge(context), "NoorAudio")
            WebPlayerBus.bind(this)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(false)
            settings.mediaPlaybackRequiresUserGesture = false
            // تحميل فوري: الكاش أولاً ثم تحديث الشبكة في الخلفية
            settings.cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            webViewClient = noorClient(context, listener)
            loadUrl(url)
        }
    }

    /** إحماء الصفحات الأساسية عند إقلاع التطبيق — في خيط خلفي */
    fun warmUp(context: Context) {
        val appContext = context.applicationContext
        thread(name = "noor-web-pool", isDaemon = true) {
            try {
                if (com.elhajri.noor.settings.NoorSettings.getDataSaver(appContext)) return@thread
                for (url in listOf(NoorWeb.QURAN, NoorWeb.LIBRARY)) {
                    if (ready.containsKey(url)) continue
                    try {
                        ready[url] = build(appContext, url, null)
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * طلب WebView لصفحة: يُعاد جاهزاً من المجمع إن وُجد (فتح لحظي)،
     * وإلا يُبنى من جديد. يعيد (الويب فيو، هل اكتمل تحميله مسبقاً).
     */
    fun acquire(context: Context, url: String, listener: LoadListener): Pair<WebView, Boolean> {
        val cached = ready.remove(url)
        if (cached != null) {
            cached.webViewClient = noorClient(context, listener)
            val complete = cached.progress >= 100
            return cached to complete
        }
        return build(context, url, listener) to false
    }

    /** إعادة صفحة إلى المجمع عند إغلاق الشاشة — للفتح اللحظي لاحقاً */
    fun release(url: String, webView: WebView?) {
        if (webView == null) return
        val current = try { webView.url ?: url } catch (_: Exception) { url }
        // نعيد فقط الصفحات الأساسية إلى المجمع (قرآن / أناشيد)
        if (current.startsWith(NoorWeb.QURAN)) ready[NoorWeb.QURAN] = webView
        else if (current.startsWith(NoorWeb.LIBRARY)) ready[NoorWeb.LIBRARY] = webView
    }
}
